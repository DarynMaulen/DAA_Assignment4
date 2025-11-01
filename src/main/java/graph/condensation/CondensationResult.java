package graph.condensation;

import java.util.*;

// Stores the structure of the resulting Condensation Graph.
public class CondensationResult {
    private final List<List<Integer>> components; // List of SCCs
    private final int[] nodeToComponent; // Original node ID -> Component ID
    private final List<Set<Integer>> adjacentComponents; // Adjacency list of the condensation graph
    private final List<Integer> componentSizes; // Size of each component

    // Initializes the result structure.
    public CondensationResult(List<List<Integer>> components, int[] nodeToComponent,
                              List<Set<Integer>> adjacentComponents) {
        this.components = Collections.unmodifiableList(components);
        this.nodeToComponent = nodeToComponent;
        this.adjacentComponents = adjacentComponents;
        this.componentSizes = new ArrayList<>(components.size());
        // Calculate and store the size of each component
        for(List<Integer> component : components){
            componentSizes.add(component.size());
        }
    }

    // Returns the list of SCCs.
    public List<List<Integer>> getComponents() {
        return components;
    }

    // Returns the array mapping nodes to their component ID.
    public int[] getNodeToComponent() {
        return nodeToComponent;
    }

    // Returns the adjacency list of the condensation graph.
    public List<Set<Integer>> getAdjacentComponents() {
        return adjacentComponents;
    }

    // Returns the list of component sizes.
    public List<Integer> getComponentSizes() {
        return componentSizes;
    }

    // Returns the total number of components.
    public int getComponentCount(){
        return components.size();
    }

    // String representation of the condensation graph result.
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