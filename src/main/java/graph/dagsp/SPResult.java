package graph.dagsp;

// Stores the results of a Shortest Path calculation on the condensation graph.
public class SPResult {
    public final long[] componentDistance; // Array of calculated distances to each component
    public final int[] parentComponent; // Array to store parent component ID for path reconstruction

    // Constructor to initialize results.
    public SPResult(final long[] componentDistance, final int[] parentComponent) {
        this.componentDistance = componentDistance;
        this.parentComponent = parentComponent;
    }

    // Returns the number of components processed.
    public int componentCount(){
        return componentDistance == null ? 0 : componentDistance.length;
    }

    // String representation of the result.
    @Override
    public String toString() {
        return "SPResult{compCount=" + componentCount() + "}";
    }
}