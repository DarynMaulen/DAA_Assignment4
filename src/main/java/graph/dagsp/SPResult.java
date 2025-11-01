package graph.dagsp;

public class SPResult {
    public final long[] componentDistance;
    public final int[] parentComponent;

    public SPResult(final long[] componentDistance, final int[] parentComponent) {
        this.componentDistance = componentDistance;
        this.parentComponent = parentComponent;
    }

    public int componentCount(){
        return componentDistance == null ? 0 : componentDistance.length;
    }

    @Override
    public String toString() {
        return "SPResult{compCount=" + componentCount() + "}";
    }
}
