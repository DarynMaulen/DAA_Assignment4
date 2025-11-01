package graph.condensation;

import graph.model.Edge;
import graph.model.Graph;
import metrics.Metrics;

import java.util.*;

// Builds the Condensation Graph from the original graph and its SCCs.
public class CondensationBuilder {
    // Builds the condensation graph structure.
    public CondensationResult build(Graph graph, List<List<Integer>> SCCs, Metrics metrics) {
        if (metrics != null) {
            metrics.startTimer();
        }

        int n = graph.getN(); // Number of original nodes
        int k = SCCs.size(); // Number of components

        // Array mapping original node ID to its Component ID.
        int[] nodeToComponent = new int[n];
        Arrays.fill(nodeToComponent, -1);
        for (int componentId = 0; componentId < SCCs.size(); componentId++) {
            for (int node : SCCs.get(componentId)) {
                if (node >= 0 && node < n) {
                    nodeToComponent[node] = componentId;
                }
            }
        }

        // List of adjacency sets for components.
        List<Set<Integer>> adjacentComponents = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            adjacentComponents.add(new HashSet<>());
        }

        long processedEdges = 0L;
        long addedEdges = 0L;

        // Iterate through all original edges to build condensation edges.
        if (graph.getEdges() != null) {
            for (Edge edge : graph.getEdges()) {
                processedEdges++;
                int u = edge.getU();
                int v = edge.getV();
                if (u < 0 || v < 0 || u >= n || v >= n) {
                    continue; // Skip invalid nodes
                }
                int componentU = nodeToComponent[u];
                int componentV = nodeToComponent[v];
                if (componentU == -1 || componentV == -1) {
                    continue; // Skip unmapped nodes
                }
                if (componentU != componentV) {
                    // add edge to condensation graph.
                    boolean added = adjacentComponents.get(componentU).add(componentV);
                    if (added) {
                        addedEdges++; // Count unique condensation edges
                    }
                }
            }
        }

        // Report metrics and stop timer.
        if (metrics != null) {
            metrics.incBy("condensation.edges.processed", processedEdges);
            metrics.incBy("condensation.edges.added", addedEdges);
            metrics.stopTimer();
            metrics.putLong("condensation.time.nanos", metrics.getElapsedNanos());
        }

        return new CondensationResult(SCCs, nodeToComponent, adjacentComponents);
    }
}