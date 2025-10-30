package graph.condensation;

import java.util.*;

public class CondensationResult {
    private final List<List<Integer>> components;
    private final int[] nodeToComponent;
    private final List<Set<Integer>> adjacentComponents;
    private final List<Integer> componentSizes;

    public CondensationResult(List<List<Integer>> components, int[] nodeToComponent,
                              List<Set<Integer>> adjacentComponents) {
        this.components = Collections.unmodifiableList(components);
        this.nodeToComponent = nodeToComponent;
        this.adjacentComponents = adjacentComponents;
        this.componentSizes = new ArrayList<>(components.size());
        for(List<Integer> component : components){
            componentSizes.add(component.size());
        }
    }

    public List<List<Integer>> getComponents() {
        return components;
    }

    public int[] getNodeToComponent() {
        return nodeToComponent;
    }

    public List<Set<Integer>> getAdjacentComponents() {
        return adjacentComponents;
    }

    public List<Integer> getComponentSizes() {
        return componentSizes;
    }

    public int getComponentCount(){
        return components.size();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CondensationResult{components=").
                append(components.size()).append(", adjacentComponents=[");
        for (int i = 0; i < adjacentComponents.size(); i++) {
            stringBuilder.append(i).append("->").append(adjacentComponents.get(i)).append(", ");
        }
        stringBuilder.append("]}");
        return stringBuilder.toString();
    }
}
