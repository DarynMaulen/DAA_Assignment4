import data.DataGenerator;
import graph.TasksJsonParser;
import graph.condensation.CondensationBuilder;
import graph.condensation.CondensationResult;
import graph.dagsp.ComponentUtils;
import graph.dagsp.LongestPath;
import graph.dagsp.SPResult;
import graph.dagsp.ShortestPaths;
import graph.model.Graph;
import graph.scc.Tarjan;
import graph.topo.TopologicalSorter;
import graph.topo.TopoResult;
import metrics.SimpleMetrics;
import utils.ResultsExporter;

import java.io.File;
import java.util.*;

// Pipeline that produces JSON + CSV results, includes component_order and derived_task_order in JSON and CSV.
public class Runner {
    public static void main(String[] args) throws Exception {
        String dataDir = args.length > 0 ? args[0] : "data";
        String resultsDir = args.length > 1 ? args[1] : "results";

        File dataD = new File(dataDir);
        // If data dir missing or empty -> run DataGenerator to create sample datasets
        if (!dataD.exists() || (dataD.isDirectory() && dataD.listFiles((d, name) -> name.endsWith(".json")) == null)
                || (dataD.isDirectory() && Objects.requireNonNull(dataD.listFiles((d, name) -> name.endsWith(".json"))).length == 0)) {
            System.out.println("No data files found in '" + dataDir + "'. Running DataGenerator...");
            DataGenerator.main(new String[]{dataDir});
            System.out.println("Data generation finished.");
        }

        ResultsExporter exporter = new ResultsExporter(resultsDir);
        TasksJsonParser parser = new TasksJsonParser();

        File[] files = dataD.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.err.println("Still no data files in " + dataDir + " -> aborting.");
            return;
        }
        Arrays.sort(files);

        for (File file : files) {
            System.out.println("--- Processing " + file.getName() + " ---");
            Graph graph = parser.parse(file.getAbsolutePath());

            int n = graph.getN();
            int m = graph.getEdges().size();
            String weightModel = graph.getWeightModel() == null ? "node" : graph.getWeightModel();

            // SCC
            SimpleMetrics sccMetrics = new SimpleMetrics();
            Tarjan tarjan = new Tarjan(graph);
            List<List<Integer>> SCCs = tarjan.findSCCs(sccMetrics);
            long timeScc = sccMetrics.get("time.scc") != 0 ? sccMetrics.get("time.scc") : sccMetrics.getElapsedNanos();

            // Condensation
            SimpleMetrics condensedMetrics = new SimpleMetrics();
            CondensationBuilder condensationBuilder = new CondensationBuilder();
            CondensationResult condensationResult = condensationBuilder.build(graph, SCCs, condensedMetrics);
            long timeCondensation = condensedMetrics.get("condensation.time.nanos");

            // Topo
            SimpleMetrics topoMetrics = new SimpleMetrics();
            TopologicalSorter topoSorter = new TopologicalSorter();
            TopoResult topoResult = topoSorter.KahnSort(condensationResult, topoMetrics);
            long timeTopo = topoMetrics.get("topo.time.nanos");

            // component order and derived task order
            List<Integer> componentOrder = topoResult.getComponentOrder();

            // Build derived task order by expanding components in topological order.
            // For determinism, sort nodes inside each component.
            List<List<Integer>> components = condensationResult.getComponents();
            List<Integer> derivedTaskOrder = new ArrayList<>();
            for (int compId : componentOrder) {
                List<Integer> nodes = new ArrayList<>(components.get(compId));
                Collections.sort(nodes);
                derivedTaskOrder.addAll(nodes);
            }

            // DAG SP
            SimpleMetrics dagMetrics = new SimpleMetrics();
            ShortestPaths sp = new ShortestPaths();
            SPResult spResult = sp.shortestPaths(condensationResult, graph, topoResult, graph.getSource(), weightModel, dagMetrics);
            LongestPath lp = new LongestPath();
            SPResult lpResult = lp.longestPath(condensationResult, graph, topoResult, graph.getSource(), weightModel, dagMetrics);
            long timeDag = dagMetrics.get("dag.time.nanos");
            long relaxations = dagMetrics.get("dag.relaxations");

            // critical path (longest)
            int k = condensationResult.getComponentCount();
            long bestVal = Long.MIN_VALUE;
            int bestComp = -1;
            long[] lpCompDist = lpResult.componentDistance;
            for (int i = 0; i < k; i++) {
                long v = lpCompDist[i];
                if (v > bestVal) {
                    bestVal = v;
                    bestComp = i;
                }
            }
            List<Integer> compPath = ComponentUtils.reconstructComponentPath(
                    bestComp, condensationResult.getNodeToComponent()[graph.getSource()], lpResult.parentComponent);
            List<Integer> nodePath = ComponentUtils.expandToNodePath(compPath, condensationResult);

            // shortest path: choose target
            long[] compDist = spResult.componentDistance;
            int[] compParent = spResult.parentComponent;
            int sourceComp = condensationResult.getNodeToComponent()[graph.getSource()];

            final long INF_THRESHOLD = Long.MAX_VALUE / 8;
            // try farthest reachable component excluding source
            int targetComp = -1;
            long maxFinite = Long.MIN_VALUE;
            for (int i = 0; i < compDist.length; i++) {
                long d = compDist[i];
                if (d >= INF_THRESHOLD) continue;
                if (i == sourceComp) continue;
                if (d > maxFinite) {
                    maxFinite = d;
                    targetComp = i;
                }
            }
            // fallback to source if nothing else
            if (targetComp == -1) {
                long d = compDist[sourceComp];
                if (d < INF_THRESHOLD) {
                    targetComp = sourceComp;
                    maxFinite = d;
                }
            }

            List<Integer> shortestCompPath = Collections.emptyList();
            List<Integer> shortestNodePath = Collections.emptyList();
            Long shortestPathLen = null;
            if (targetComp != -1) {
                shortestCompPath = ComponentUtils.reconstructComponentPath(targetComp, sourceComp, compParent);
                shortestNodePath = ComponentUtils.expandToNodePath(shortestCompPath, condensationResult);
                shortestPathLen = compDist[targetComp];
            }

            // prepare payload JSON
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("dataset", file.getName());
            payload.put("n", n);
            payload.put("m", m);
            payload.put("weight_model", weightModel);
            payload.put("scc_count", SCCs.size());
            payload.put("scc_sizes", SCCs.stream().map(List::size).toArray());
            payload.put("time_scc_ns", timeScc);
            payload.put("time_condensation_ns", timeCondensation);
            payload.put("time_topo_ns", timeTopo);
            payload.put("time_dag_ns", timeDag);
            payload.put("dag_relaxations", relaxations);

            // component order + derived task order
            payload.put("component_order", componentOrder);
            payload.put("derived_task_order", derivedTaskOrder);

            payload.put("critical_path_component_ids", compPath);
            payload.put("critical_path_node_ids", nodePath);
            payload.put("critical_path_length", bestVal);

            payload.put("shortest_distances_component", compDist);
            payload.put("shortest_path_component_ids", shortestCompPath);
            payload.put("shortest_path_node_ids", shortestNodePath);
            payload.put("shortest_path_length", shortestPathLen);

            // write outputs
            exporter.writeDatasetJson(file.getName().replace(".json", ""), payload);

            long shortestLenForCsv = (shortestPathLen == null) ? -1L : shortestPathLen;
            String shortestNodePathStr = shortestNodePath.toString();

            String componentOrderStr = componentOrder.toString();
            String derivedTaskOrderStr = derivedTaskOrder.toString();

            exporter.appendSummaryCsv(
                    file.getName(),
                    n,
                    m,
                    weightModel,
                    SCCs.size(),
                    timeScc,
                    timeCondensation,
                    timeTopo,
                    timeDag,
                    relaxations,
                    bestVal,
                    nodePath.toString(),
                    shortestLenForCsv,
                    shortestNodePathStr,
                    componentOrderStr,
                    derivedTaskOrderStr
            );

            System.out.println("-> done: scc=" + SCCs.size() + " criticalLen=" + bestVal
                    + " shortestLen=" + (shortestPathLen == null ? "N/A" : shortestPathLen)
                    + " shortestNodes=" + shortestNodePath);
        }

        System.out.println("All done. Results in " + resultsDir);
    }
}