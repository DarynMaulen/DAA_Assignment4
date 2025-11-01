package graph.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final boolean directed; // True if the graph is directed
    private final int n;
    private final List<Node> nodes; // List of all nodes
    private final List<Edge> edges; // List of all edges
    private final Integer source;
    private final String weightModel;

    // Constructor to initialize the graph structure.
    public Graph(boolean directed, int n, List<Node> nodes, List<Edge> edges, Integer source, String weightModel) {
        this.directed = directed;
        this.n = n;
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.edges = edges != null ? edges : new ArrayList<>();
        this.source = source;
        this.weightModel = weightModel;
    }

    // Returns whether the graph is directed.
    public boolean isDirected() {
        return directed;
    }

    // Returns the total number of nodes.
    public int getN() {
        return n;
    }

    // Returns an unmodifiable list of all nodes.
    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    // Returns an unmodifiable list of all edges.
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    // Returns the ID of the source node.
    public Integer getSource() {
        return source;
    }

    // Returns the weight model used.
    public String getWeightModel() {
        return weightModel;
    }

    // String representation of the Graph object.
    @Override
    public String toString() {
        return "Graph{" +
                "directed=" + directed +
                ", n=" + n +
                ", nodes=" + nodes +
                ", edges=" + edges +
                ", source=" + source +
                ", weightModel='" + weightModel + '\'' +
                '}';
    }
}