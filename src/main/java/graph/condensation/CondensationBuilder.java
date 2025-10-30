package graph.condensation;

import graph.model.Edge;
import graph.model.Graph;
import metrics.Metrics;

import java.util.*;

// Build condensation from SCCs and original graph.
public class CondensationBuilder {
    public CondensationResult build(Graph graph, List<List<Integer>> SCCs, Metrics metrics) {
        if (metrics != null) {
            metrics.startTimer();
        }

        int n = graph.getN();
        int k = SCCs.size();

        // node -> component mapping
        int[] nodeToComponent = new int[n];
        Arrays.fill(nodeToComponent, -1);
        for (int componentId = 0; componentId < SCCs.size(); componentId++) {
            for (int node : SCCs.get(componentId)) {
                if (node >= 0 && node < n) {
                    nodeToComponent[node] = componentId;
                }
            }
        }

        // adjacency sets for components
        List<Set<Integer>> adjacentComponents = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            adjacentComponents.add(new HashSet<>());
        }

        long processedEdges = 0L;
        long addedEdges = 0L;

        if (graph.getEdges() != null) {
            for (Edge edge : graph.getEdges()) {
                processedEdges++;
                int u = edge.getU();
                int v = edge.getV();
                if (u < 0 || v < 0 || u >= n || v >= n) {
                    continue;
                }
                int componentU = nodeToComponent[u];
                int componentV = nodeToComponent[v];
                if (componentU == -1 || componentV == -1) {
                    continue;
                }
                if (componentU != componentV) {
                    // Set.add returns true if element was not present
                    boolean added = adjacentComponents.get(componentU).add(componentV);
                    if (added) {
                        addedEdges++;
                    }
                }
            }
        }

        // metrics: report processed/added counts and time
        if (metrics != null) {
            metrics.incBy("condensation.edges.processed", processedEdges);
            metrics.incBy("condensation.edges.added", addedEdges);
            metrics.stopTimer();
            metrics.putLong("condensation.time.nanos", metrics.getElapsedNanos());
        }

        return new CondensationResult(SCCs, nodeToComponent, adjacentComponents);
    }
}
