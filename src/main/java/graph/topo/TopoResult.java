package graph.topo;

import java.util.Collections;
import java.util.List;

public class TopoResult {
    private final List<Integer> componentOrder;
    private final List<Integer> derivedOrder;

    public TopoResult(List<Integer> componentOrder, List<Integer> derivedOrder) {
        this.componentOrder = Collections.unmodifiableList(componentOrder);
        this.derivedOrder = Collections.unmodifiableList(derivedOrder);
    }

    public List<Integer> getComponentOrder() {
        return componentOrder;
    }

    public List<Integer> getDerivedOrder() {
        return derivedOrder;
    }

    @Override
    public String toString() {
        return "TopoResult{compOrder=" + componentOrder + ", derivedOrder=" + derivedOrder + "}";
    }
}
