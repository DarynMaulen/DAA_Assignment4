package graph.dagsp;

import graph.condensation.CondensationResult;
import graph.model.Graph;
import graph.topo.TopoResult;
import metrics.Metrics;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class LongestPath {
    // derive to avoid overflow
    private static final long NEG_INF = Long.MIN_VALUE / 4;

    public SPResult longestPath(CondensationResult condensationResult, Graph graph, TopoResult topo,
                                int sourceNode, String weightModel, Metrics metrics) {
        if(metrics != null){
            metrics.startTimer();
        }

        if(sourceNode < 0 || sourceNode >= graph.getN()){
            throw new IllegalArgumentException("sourceNode must be a positive integer");
        }
        int[] nodeToComponent = condensationResult.getNodeToComponent();
        int k = condensationResult.getComponentCount();
        int sourceComponent = nodeToComponent[sourceNode];
        if(sourceComponent < 0 || sourceComponent >= k){
            throw new IllegalArgumentException("Invalid source node/component");
        }

        long[]componentWeight = ComponentUtils.buildComponentWeight(condensationResult,graph);
        List<Map<Integer, Integer>> componentEdgeWeight = ComponentUtils.buildComponentEdgeWeight(condensationResult, graph);

        long[] best = new long[k];
        int[] parent = new int[k];
        for(int i =0; i<k; i++){
            best[i] = NEG_INF;
            parent[i] = -1;
        }

        if("node".equals(weightModel)){
            best[sourceComponent] = componentWeight[sourceComponent];
        }else{
            best[sourceComponent] = 0L;
        }

        List<Integer> componentOrder = topo.getComponentOrder();
        List<Set<Integer>> adj = condensationResult.getAdjacentComponents();

        for(int u : componentOrder){
            if(best[u] == NEG_INF){
                continue;
            }
            for(int v : adj.get(u)){
                if(metrics != null){
                    metrics.inc("dag.relaxations");
                }
                long weight;
                if("node".equals(weightModel)){
                    weight = componentWeight[v];
                }else {
                    Map<Integer,Integer> inner = componentEdgeWeight.get(u);
                    Integer edgeWeight = (inner == null) ? null : inner.get(v);
                    if(edgeWeight == null){
                        edgeWeight = 0;
                    }
                    weight = edgeWeight;
                }
                long candidate = best[u] + weight;
                if(candidate > best[v]){
                    best[v] = candidate;
                    parent[v] = u;
                    if(metrics != null){
                        metrics.inc("dag.rel.success");
                    }
                }
            }
        }
        if (metrics != null) {
            metrics.stopTimer();
            metrics.putLong("dag.time.nanos", metrics.getElapsedNanos());
        }

        return new SPResult(best, parent);
    }
}
