package graph;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import graph.model.Edge;
import graph.model.Graph;
import graph.model.Node;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

// Simple parser to read graph data from a JSON file in the assignment format.
public class TasksJsonParser {

    // Helper class for deserializing an edge from JSON.
    private static class EdgeDTO {
        int u;
        int v;
        Integer w; // Weight
    }

    // Helper class for deserializing a node from JSON.
    private static class NodeDTO {
        int id;
        Integer duration;
    }

    // Root helper class for deserializing the entire graph JSON structure.
    private static class TasksDTO {
        boolean directed;
        int n; // Number of nodes
        List<EdgeDTO> edges;
        List<NodeDTO> nodes;
        Integer source;
        // Serialized name matches the 'weight_model' field in JSON.
        @SerializedName("weight_model")
        String weightModel;
    }

    // Parses the JSON file and constructs a Graph object.
    public Graph parse(String filename) throws Exception {
        Gson gson = new Gson();
        try (Reader r = new FileReader(filename)) {
            TasksDTO dto = gson.fromJson(r, TasksDTO.class);
            // use provided durations if available, otherwise create default nodes.
            List<Node> nodes = new ArrayList<>();
            if (dto.nodes != null && !dto.nodes.isEmpty()) {
                // Use provided nodes and their durations.
                for (NodeDTO nd : dto.nodes) {
                    nodes.add(new Node(nd.id, nd.duration));
                }
                // Fill in any missing nodes up to dto.n with null duration.
                for (int i = nodes.size(); i < dto.n; i++) {
                    nodes.add(new Node(i, null));
                }
            } else {
                // Create all nodes with null duration if 'nodes' array is missing.
                for (int i = 0; i < dto.n; i++) {
                    nodes.add(new Node(i, null));
                }
            }

            // Build edges list from DTOs.
            List<Edge> edges = new ArrayList<>();
            if (dto.edges != null) {
                for (EdgeDTO e : dto.edges) {
                    edges.add(new Edge(e.u, e.v, e.w));
                }
            }

            // Construct and return the final Graph object.
            return new Graph(dto.directed, dto.n, nodes, edges, dto.source, dto.weightModel);
        }
    }
}