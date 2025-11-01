package graph.model;

public class Edge {
    private final int u; // Start node ID
    private final int v; // End node ID
    private final Integer weight; // Edge weight

    // Constructor to create an edge.
    public Edge(int u, int v, Integer weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
    }

    // Getter for the start node ID.
    public int getU() {
        return u;
    }

    // Getter for the end node ID.
    public int getV() {
        return v;
    }

    // Getter for the edge weight.
    public Integer getWeight() {
        return weight;
    }

    // String representation of the Edge.
    @Override
    public String toString() {
        return "Edge { u= " + u + ", v= " + v + ", weight= " + weight + " }";
    }
}