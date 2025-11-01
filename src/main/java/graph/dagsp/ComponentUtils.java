package graph.dagsp;

import graph.condensation.CondensationResult;
import graph.model.Edge;
import graph.model.Graph;
import graph.model.Node;

import java.util.*;

public class ComponentUtils {
    public static long[] buildComponentWeight(CondensationResult condensationResult, Graph graph) {
        List<List<Integer>> components = condensationResult.getComponents();
        int k = components.size();
        long[] componentWeights = new long[k];

        // map nodeId -> duration
        Map<Integer, Integer> nodeDuration = new HashMap<>();
        for (Node node : graph.getNodes()) {
            nodeDuration.put(node.getId(), node.getDuration() == null ? 0 : node.getDuration());
        }

        for (int i = 0; i < k; i++) {
            long sum = 0L;
            for (int nodeId : components.get(i)) {
                sum += nodeDuration.getOrDefault(nodeId, 0);
            }
            componentWeights[i] = sum;
        }
        return componentWeights;
    }

    // return Map: index = fromComponent, map: toComponent->weight
    public static List<Map<Integer,Integer>> buildComponentEdgeWeight(CondensationResult condensationResult, Graph graph) {
        int k = condensationResult.getComponentCount();
        List<Map<Integer,Integer>> map = new ArrayList<>(k);
        for (int i = 0; i < k; i++) map.add(new HashMap<>());

        int[] nodeToComponent = condensationResult.getNodeToComponent();
        for (Edge edge : graph.getEdges()) {
            int u = edge.getU(), v = edge.getV();
            if (u < 0 || v < 0 || u >= graph.getN() || v >= graph.getN()){
                continue;
            }

            int componentU = nodeToComponent[u], componentV = nodeToComponent[v];
            if (componentU == -1 || componentV == -1 || componentU == componentV){
                continue;
            }

            int w = edge.getWeight() == null ? 0 : edge.getWeight();
            map.get(componentU).merge(componentV, w, Integer::min);
        }
        return map;
    }

    public static List<Integer> reconstructComponentPath(int targetComponent, int sourceComponent, int[] parent) {
        if(targetComponent < 0 || sourceComponent < 0) {
            return Collections.emptyList();
        }
        LinkedList<Integer> rev = new LinkedList<>();
        int current = targetComponent;
        while (current != -1){
            rev.addFirst(current);
            if(current == sourceComponent) {
                break;
            }
            current = parent[current];
        }
        if(rev.isEmpty() || rev.getFirst() != sourceComponent) {
            return Collections.emptyList();
        }

        return rev;
    }

    public static List<Integer> expandToNodePath(List<Integer> componentPath, CondensationResult condensationResult) {
        if(componentPath == null || componentPath.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>();
        List<List<Integer>> components = condensationResult.getComponents();
        for(int componentId : componentPath) {
            List<Integer> nodes = new ArrayList<>(components.get(componentId));
            Collections.sort(nodes);
            result.addAll(nodes);
        }
        return result;
    }
}
