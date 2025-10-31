package graph.topo;

import graph.condensation.CondensationResult;
import metrics.Metrics;

import java.util.*;

public class TopologicalSorter {
    public TopoResult KahnSort(CondensationResult condensationResult, Metrics metrics) {
        if(metrics!=null){
            metrics.startTimer();
        }

        int k = condensationResult.getComponentCount();
        List<Set<Integer>> adj = condensationResult.getAdjacentComponents();
        // compute indegrees
        int[] indegree = new int[k];
        for(int u = 0; u < k; u++) {
            for(int v : adj.get(u)) {
                indegree[v]++;
            }
        }

        PriorityQueue<Integer> pq = new PriorityQueue<>();
        for(int i = 0;i < k;i++) {
            if(indegree[i]==0) {
                pq.add(i);
                if(metrics!=null){
                    metrics.inc("topo.kahn.push");
                }
            }
        }

        List<Integer> componentOrder = new ArrayList<>(k);
        while (!pq.isEmpty()) {
            int u = pq.poll();
            if(metrics!=null){
                metrics.inc("topo.kahn.pop");
            }
            componentOrder.add(u);
            for(int v : adj.get(u)) {
                indegree[v]--;
                if(indegree[v]==0) {
                    pq.add(v);
                    if(metrics!=null){
                        metrics.inc("topo.kahn.push");
                    }
                }
            }
        }

        if(componentOrder.size()!=k) {
            if(metrics!=null){
                metrics.stopTimer();
                metrics.putLong("topo.time.nanos", metrics.getElapsedNanos());
            }
            throw new IllegalStateException("Topological sort failed: component graph has a cycle.");
        }

        List<Integer> derivedOrder = new ArrayList<>();
        List<List<Integer>> components = condensationResult.getComponents();
        for(int componentId : componentOrder) {
            List<Integer> nodes = new ArrayList<>(components.get(componentId));
            Collections.sort(nodes);
            derivedOrder.addAll(nodes);
        }

        if(metrics!=null){
            metrics.stopTimer();
            metrics.putLong("topo.time.nanos", metrics.getElapsedNanos());
        }

        return new TopoResult(componentOrder, derivedOrder);
    }
}
