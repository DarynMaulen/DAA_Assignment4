import graph.TasksJsonParser;
import graph.condensation.*;
import graph.dagsp.*;
import graph.model.Graph;
import graph.scc.Tarjan;
import graph.topo.*;
import metrics.SimpleMetrics;
import utils.ResultsExporter;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//Run full pipeline on all .json files inside dataDir, export per-dataset JSON and summary CSV into resultsDir.
public class Runner {
    public static void main(String[] args) throws Exception {
        String dataDir = args.length > 0 ? args[0] : "data";
        String resultsDir = args.length > 1 ? args[1] : "results";
        ResultsExporter exporter = new ResultsExporter(resultsDir);
        TasksJsonParser parser = new TasksJsonParser();

        File dataD = new File(dataDir);
        File[] files = dataD.listFiles((data,name) -> name.endsWith(".json"));
        if(files == null){
            System.err.println("No data files found in " + dataDir);
            return;
        }
        Arrays.sort(files);

        for(File file : files){
            System.out.println("=== Processing " + file.getName() + " ===");
            Graph graph = parser.parse(file.getAbsolutePath());

            // basic graph stats
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

            // DAG SP
            SimpleMetrics dagMetrics = new SimpleMetrics();
            ShortestPaths sp = new ShortestPaths();
            SPResult spResult = sp.shortestPaths(condensationResult,graph,topoResult, graph.getSource(), weightModel, dagMetrics);
            LongestPath lp = new LongestPath();
            SPResult lpResult = lp.longestPath(condensationResult,graph,topoResult, graph.getSource(), weightModel, dagMetrics);
            long timeDag = dagMetrics.get("dag.time.nanos");
            long relaxations = dagMetrics.get("dag.relaxations");

            // critical path reconstruction
            int k = condensationResult.getComponentCount();
            long bestVal = Long.MIN_VALUE;
            int bestComp = -1;
            for(int i = 0; i<k; i++){
                long v = lpResult.componentDistance[i];
                if(v > bestVal){
                    bestVal = v;
                    bestComp = i;
                }
            }
            List<Integer> compPath = ComponentUtils.reconstructComponentPath(bestComp, condensationResult.getNodeToComponent()[graph.getSource()], lpResult.parentComponent);
            List<Integer> nodePath = ComponentUtils.expandToNodePath(compPath, condensationResult);

            // prepare payload
            Map<String,Object> payload = new LinkedHashMap<>();
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
            payload.put("critical_path_component_ids", compPath);
            payload.put("critical_path_node_ids", nodePath);
            payload.put("critical_path_length", bestVal);

            exporter.writeDatasetJson(file.getName().replace(".json",""), payload);
            exporter.appendSummaryCsv(file.getName(), n, m, weightModel, SCCs.size(), timeScc, timeCondensation, timeTopo, timeDag, relaxations, bestVal, nodePath.toString());

            System.out.println("-> done: scc=" + SCCs.size() + " criticalLen=" + bestVal + " pathNodes=" + nodePath);
        }
        System.out.println("All done. Results in " + resultsDir);
    }
}
