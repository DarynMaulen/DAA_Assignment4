package graph.dagsp;

import graph.condensation.*;
import graph.model.*;
import graph.scc.Tarjan;
import graph.topo.*;
import metrics.SimpleMetrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

// Minimal tests for ShortestPaths and LongestPath on small DAGs.
// testNodeModelChain: 0->1->2 with node durations 2,3,4.
// testEdgeModelChain: 0->1 (w=3), 1->2 (w=5).
public class ShortestLongestPathsTest {

    @Test
    public void testNodeModelChain() {
        // nodes with durations: 0:2, 1:3, 2:4
        List<Node> nodes = Arrays.asList(
                new Node(0, 2),
                new Node(1, 3),
                new Node(2, 4)
        );
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, null),
                new Edge(1, 2, null)
        );
        Graph g = new Graph(true, 3, nodes, edges, 0, "node");

        // SCC -> condensation -> topo
        Tarjan tarjan = new Tarjan(g);
        SimpleMetrics m1 = new SimpleMetrics();
        List<List<Integer>> SCCs = tarjan.findSCCs(m1);

        CondensationBuilder cb = new CondensationBuilder();
        SimpleMetrics m2 = new SimpleMetrics();
        CondensationResult cr = cb.build(g, SCCs, m2);

        TopologicalSorter sorter = new TopologicalSorter();
        SimpleMetrics m3 = new SimpleMetrics();
        TopoResult topo = sorter.KahnSort(cr, m3);

        // Shortest (node model)
        ShortestPaths spAlgo = new ShortestPaths();
        SimpleMetrics m4 = new SimpleMetrics();
        SPResult spRes = spAlgo.shortestPaths(cr, g, topo, 0, "node", m4);

        // For node model expected distances:
        // node 0 -> 2
        // node 1 -> 2 + 3 = 5
        // node 2 -> 2 + 3 + 4 = 9
        int[] nodeToComp = cr.getNodeToComponent();
        Assertions.assertEquals(2L, spRes.componentDistance[nodeToComp[0]]);
        Assertions.assertEquals(5L, spRes.componentDistance[nodeToComp[1]]);
        Assertions.assertEquals(9L, spRes.componentDistance[nodeToComp[2]]);

        // Longest path, should be same in this simple chain
        LongestPath lpAlgo = new LongestPath();
        SimpleMetrics m5 = new SimpleMetrics();
        SPResult lpRes = lpAlgo.longestPath(cr, g, topo, 0, "node", m5);

        Assertions.assertEquals(2L, lpRes.componentDistance[nodeToComp[0]]);
        Assertions.assertEquals(5L, lpRes.componentDistance[nodeToComp[1]]);
        Assertions.assertEquals(9L, lpRes.componentDistance[nodeToComp[2]]);
    }

    @Test
    public void testEdgeModelChain() {
        // edge weights: 0->1 (3), 1->2 (5)
        List<Node> nodes = Arrays.asList(
                new Node(0, null),
                new Node(1, null),
                new Node(2, null)
        );
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, 3),
                new Edge(1, 2, 5)
        );
        Graph g = new Graph(true, 3, nodes, edges, 0, "edge");

        // SCC -> condensation -> topo
        Tarjan tarjan = new Tarjan(g);
        SimpleMetrics m1 = new SimpleMetrics();
        List<List<Integer>> SCCs = tarjan.findSCCs(m1);

        CondensationBuilder cb = new CondensationBuilder();
        SimpleMetrics m2 = new SimpleMetrics();
        CondensationResult cr = cb.build(g, SCCs, m2);

        TopologicalSorter sorter = new TopologicalSorter();
        SimpleMetrics m3 = new SimpleMetrics();
        TopoResult topo = sorter.KahnSort(cr, m3);

        // Shortest (edge model): source dist = 0 convention
        ShortestPaths spAlgo = new ShortestPaths();
        SimpleMetrics m4 = new SimpleMetrics();
        SPResult spRes = spAlgo.shortestPaths(cr, g, topo, 0, "edge", m4);

        int[] nodeToComp = cr.getNodeToComponent();
        // source
        Assertions.assertEquals(0L, spRes.componentDistance[nodeToComp[0]]);
        // 0->1 = 3
        Assertions.assertEquals(3L, spRes.componentDistance[nodeToComp[1]]);
        // 0->1->2 = 3+5 = 8
        Assertions.assertEquals(8L, spRes.componentDistance[nodeToComp[2]]);

        // Longest path (edge model) - same here
        LongestPath lpAlgo = new LongestPath();
        SimpleMetrics m5 = new SimpleMetrics();
        SPResult lpRes = lpAlgo.longestPath(cr, g, topo, 0, "edge", m5);

        Assertions.assertEquals(0L, lpRes.componentDistance[nodeToComp[0]]);
        Assertions.assertEquals(3L, lpRes.componentDistance[nodeToComp[1]]);
        Assertions.assertEquals(8L, lpRes.componentDistance[nodeToComp[2]]);
    }
}
