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

// Simple parser for the assignment JSON format.
// Supports edge weight model and node durations.
public class TasksJsonParser {

    private static class EdgeDTO {
        int u;
        int v;
        Integer w;
    }

    private static class NodeDTO {
        int id;
        Integer duration;
    }

    private static class TasksDTO {
        boolean directed;
        int n;
        List<EdgeDTO> edges;
        List<NodeDTO> nodes;
        Integer source;
        @SerializedName("weight_model")
        String weightModel;
    }

    public Graph parse(String filename) throws Exception {
        Gson gson = new Gson();
        try (Reader r = new FileReader(filename)) {
            TasksDTO dto = gson.fromJson(r, TasksDTO.class);
            // build nodes (if nodes provided, use durations; otherwise create default nodes)
            List<Node> nodes = new ArrayList<>();
            if (dto.nodes != null && !dto.nodes.isEmpty()) {
                // use provided nodes; ensure count matches dto.n or fill missing
                for (NodeDTO nd : dto.nodes) {
                    nodes.add(new Node(nd.id, nd.duration));
                }
                // fill missing ids if any
                for (int i = nodes.size(); i < dto.n; i++) {
                    nodes.add(new Node(i, null));
                }
            } else {
                for (int i = 0; i < dto.n; i++) {
                    nodes.add(new Node(i, null));
                }
            }

            List<Edge> edges = new ArrayList<>();
            if (dto.edges != null) {
                for (EdgeDTO e : dto.edges) {
                    edges.add(new Edge(e.u, e.v, e.w));
                }
            }

            return new Graph(dto.directed, dto.n, nodes, edges, dto.source, dto.weightModel);
        }
    }
}
