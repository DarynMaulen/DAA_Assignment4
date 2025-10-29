package graph.model;

public class Node {
    private final int id;
    private final Integer duration;

    public Node(int id, Integer duration) {
        this.id = id;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public Integer getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "Node { id= " + id + ", duration= " + duration + " }";
    }
}
