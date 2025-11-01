package graph.scc;


import graph.model.*;
import metrics.SimpleMetrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

// Minimal tests for TarjanSCC.
// testCycle: graph with 0->1->2->0 should produce one SCC of size 3.
// testDAG: graph 0->1->2 should produce 3 SCCs of size 1 each.
public class TarjanTest {

    @Test
    public void testCycle() {
        List<Node> nodes = Arrays.asList(new Node(0, 1), new Node(1, 1), new Node(2, 1));
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, null),
                new Edge(1, 2, null),
                new Edge(2, 0, null)
        );
        Graph g = new Graph(true, 3, nodes, edges, 0, "node");

        Tarjan tarjan = new Tarjan(g);
        SimpleMetrics metrics = new SimpleMetrics();
        List<List<Integer>> comps = tarjan.findSCCs(metrics);

        // Expect exactly 1 component of size 3 containing {0,1,2}
        Assertions.assertEquals(1, comps.size(), "Expected single SCC for 3-cycle");
        Assertions.assertEquals(3, comps.get(0).size(), "SCC size should be 3");

        Set<Integer> merged = new HashSet<>(comps.get(0));
        Assertions.assertEquals(new HashSet<>(Arrays.asList(0,1,2)), merged, "SCC should contain all nodes");

        // basic metric sanity: should have visited each node at least once
        Assertions.assertTrue(metrics.get("scc.dfs.visits") >= 3, "dfs visits should be >= number of nodes");
    }

    @Test
    public void testDAG() {
        List<Node> nodes = Arrays.asList(new Node(0, 1), new Node(1, 1), new Node(2, 1));
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, null),
                new Edge(1, 2, null)
        );
        Graph g = new Graph(true, 3, nodes, edges, 0, "node");

        Tarjan tarjan = new Tarjan(g);
        SimpleMetrics metrics = new SimpleMetrics();
        List<List<Integer>> comps = tarjan.findSCCs(metrics);

        // Expect 3 components (each node separate)
        Assertions.assertEquals(3, comps.size(), "Expected 3 SCCs for simple chain DAG");

        // Each component must be size 1 and total nodes must be {0,1,2}
        Set<Integer> seen = new HashSet<>();
        for (List<Integer> c : comps) {
            Assertions.assertEquals(1, c.size(), "Each SCC should be of size 1 in this DAG");
            seen.add(c.get(0));
        }
        Assertions.assertEquals(new HashSet<>(Arrays.asList(0,1,2)), seen, "All nodes must appear in SCCs");

        // metric sanity
        Assertions.assertTrue(metrics.get("scc.dfs.visits") >= 3, "dfs visits should be >= number of nodes");
    }
}
