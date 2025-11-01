package graph.dagsp;

import graph.condensation.CondensationResult;
import graph.model.Graph;
import graph.topo.TopoResult;
import metrics.Metrics;

import java.util.List;
import java.util.Map;
import java.util.Set;

// Finds the longest paths (critical paths) in the Condensation Graph.
public class LongestPath {
    // Defined a very small value to represent negative infinity, avoiding overflow when added.
    private static final long NEG_INF = Long.MIN_VALUE / 4;

    // Main method to compute longest paths from a source node.
    public SPResult longestPath(CondensationResult condensationResult, Graph graph, TopoResult topo,
                                int sourceNode, String weightModel, Metrics metrics) {
        if(metrics != null){
            metrics.startTimer();
        }

        // Input validation checks.
        if(sourceNode < 0 || sourceNode >= graph.getN()){
            throw new IllegalArgumentException("sourceNode must be a positive integer");
        }
        int[] nodeToComponent = condensationResult.getNodeToComponent();
        int k = condensationResult.getComponentCount();
        int sourceComponent = nodeToComponent[sourceNode];
        if(sourceComponent < 0 || sourceComponent >= k){
            throw new IllegalArgumentException("Invalid source node/component");
        }

        // Precalculate component weights and edge weights.
        long[]componentWeight = ComponentUtils.buildComponentWeight(condensationResult,graph);
        List<Map<Integer, Integer>> componentEdgeWeight = ComponentUtils.buildComponentEdgeWeight(condensationResult, graph);

        // Initialize best and parent arrays.
        long[] best = new long[k];
        int[] parent = new int[k];
        for(int i =0; i<k; i++){
            best[i] = NEG_INF;
            parent[i] = -1;
        }

        // Set initial distance for the source component.
        if("node".equals(weightModel)){
            best[sourceComponent] = componentWeight[sourceComponent];
        }else{
            best[sourceComponent] = 0L;
        }

        List<Integer> componentOrder = topo.getComponentOrder();
        List<Set<Integer>> adj = condensationResult.getAdjacentComponents();

        // Iterate through components in topological order to relax edges.
        for(int u : componentOrder){
            if(best[u] == NEG_INF){
                continue; // Skip unreachable components.
            }
            // Relax outgoing edges from the current component.
            for(int v : adj.get(u)){
                if(metrics != null){
                    metrics.inc("dag.relaxations");
                }
                long weight;
                // Determine the weight of the edge.
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
                // Relaxation step: check for longer path.
                if(candidate > best[v]){
                    best[v] = candidate;
                    parent[v] = u;
                    if(metrics != null){
                        metrics.inc("dag.relaxations");
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