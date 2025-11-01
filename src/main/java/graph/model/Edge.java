package graph.model;

public class Edge {
    private final int u;
    private final int v;
    private final Integer weight;

    public Edge(int u, int v, Integer weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
    }

    public int getU() {
        return u;
    }

    public int getV() {
        return v;
    }

    public Integer getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "Edge { u= " + u + ", v= " + v + ", weight= " + weight + " }";
    }
}
