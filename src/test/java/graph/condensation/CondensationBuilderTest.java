package graph.condensation;

import graph.model.Edge;
import graph.model.Node;
import graph.model.Graph;
import metrics.SimpleMetrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CondensationBuilderTest {

    @Test
    public void testCondensationSimple() {
        // Build graph: nodes 0..4
        // edges: 0->1, 1->2, 2->0 (SCC {0,1,2}); 3->4 (separate); edge 2->3 (from SCC -> comp)
        List<Node> nodes = Arrays.asList(
                new Node(0,1), new Node(1,1), new Node(2,1), new Node(3,1), new Node(4,1)
        );
        List<Edge> edges = Arrays.asList(
                new Edge(0,1,null),
                new Edge(1,2,null),
                new Edge(2,0,null),
                new Edge(2,3,null),
                new Edge(3,4,null)
        );
        Graph g = new Graph(true, 5, nodes, edges, 0, "node");

        // Pretend Tarjan returned components in this order:
        List<List<Integer>> sccs = Arrays.asList(
                Arrays.asList(0,1,2),
                Collections.singletonList(3),
                Collections.singletonList(4)
        );

        CondensationBuilder builder = new CondensationBuilder();
        SimpleMetrics metrics = new SimpleMetrics();
        CondensationResult res = builder.build(g, sccs, metrics);

        // Expect 3 components
        Assertions.assertEquals(3, res.getComponentCount());
        // Node to comp mapping
        int[] map = res.getNodeToComponent();
        Assertions.assertEquals(0, map[0]);
        Assertions.assertEquals(0, map[1]);
        Assertions.assertEquals(0, map[2]);
        Assertions.assertEquals(1, map[3]);
        Assertions.assertEquals(2, map[4]);

        // compAdj: comp0 -> comp1, comp1 -> comp2
        Assertions.assertTrue(res.getAdjacentComponents().get(0).contains(1));
        Assertions.assertTrue(res.getAdjacentComponents().get(1).contains(2));
        // no self-loops
        Assertions.assertFalse(res.getAdjacentComponents().get(0).contains(0));
    }
}
