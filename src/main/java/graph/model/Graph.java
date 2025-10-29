package graph.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final boolean directed;
    private final int n;
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final Integer source;
    private final String weightModel;

    public Graph(boolean directed, int n, List<Node> nodes, List<Edge> edges, Integer source, String weightModel) {
        this.directed = directed;
        this.n = n;
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.edges = edges != null ? edges : new ArrayList<>();
        this.source = source;
        this.weightModel = weightModel;
    }

    public boolean isDirected() {
        return directed;
    }

    public int getN() {
        return n;
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public Integer getSource() {
        return source;
    }

    public String getWeightModel() {
        return weightModel;
    }

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
