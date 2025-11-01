package graph.model;

public class Node {
    private final int id;
    private final Integer duration;

    // Constructor to create a node.
    public Node(int id, Integer duration) {
        this.id = id;
        this.duration = duration;
    }

    // Getter for the node's unique ID.
    public int getId() {
        return id;
    }

    // Getter for the node's duration.
    public Integer getDuration() {
        return duration;
    }

    // String representation of the Node.
    @Override
    public String toString() {
        return "Node { id= " + id + ", duration= " + duration + " }";
    }
}