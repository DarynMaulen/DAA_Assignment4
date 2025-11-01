package graph.dagsp;

import graph.condensation.CondensationResult;
import graph.model.Graph;
import graph.topo.TopoResult;
import metrics.Metrics;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShortestPaths {
    // infinity derived to avoid overflow
    private static final long INF = Long.MAX_VALUE/4;

    public SPResult shortestPaths(CondensationResult condensationResult, Graph graph, TopoResult topo,
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

        long[] componentWeight = ComponentUtils.buildComponentWeight(condensationResult, graph);
        List<Map<Integer, Integer>> componentEdgeWeight = ComponentUtils.buildComponentEdgeWeight(condensationResult, graph);

        long[] dist = new long[k];
        int[] parent = new int[k];
        for(int i = 0; i < k; i++){
            dist[i] = INF;
            parent[i] = -1;
        }

        if("node".equalsIgnoreCase(weightModel)){
            dist[sourceComponent] = componentWeight[sourceComponent];
        }else{
            dist[sourceComponent] = 0L;
        }

        List<Integer> componentOrder = topo.getComponentOrder();
        List<Set<Integer>> adj = condensationResult.getAdjacentComponents();

        for(int component : componentOrder){
            if(dist[component] == INF){
                continue;
            }
            for(int to : adj.get(component)){
                if(metrics != null){
                    metrics.inc("dag.relaxations");
                }
                long weight;
                if("node".equalsIgnoreCase(weightModel)){
                    weight = componentWeight[to];
                }else{
                    Map<Integer,Integer> inner = componentEdgeWeight.get(component);
                    Integer edgeWeight = (inner == null) ? null : inner.get(to);
                    if(edgeWeight == null){
                        edgeWeight = 0;
                    }
                    weight = edgeWeight;
                }
                long candidate = dist[component] + weight;
                if(candidate < dist[to]){
                    dist[to] = candidate;
                    parent[to] = component;
                    if(metrics != null){
                        metrics.inc("dag.relaxations.success");
                    }
                }
            }
        }
        if(metrics != null){
            metrics.stopTimer();
            metrics.putLong("dag.time.nanos", metrics.getElapsedNanos());
        }
        return new SPResult(dist, parent);
    }
}
