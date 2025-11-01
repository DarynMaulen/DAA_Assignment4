package graph.topo;

import graph.condensation.CondensationResult;
import graph.condensation.CondensationBuilder;
import graph.model.Edge;
import graph.model.Graph;
import graph.model.Node;
import metrics.SimpleMetrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

// Minimal test for topological sorter on a small condensation.
public class TopologicalSorterTest {

    @Test
    public void testKahnSimple() {
        // Construct a small condensation manually:
        // components:
        // comp0 -> comp1
        // comp0 -> comp2
        // comp1 -> comp2
        // we will create a Graph and SCCs like: comp0={0,1}, comp1={2}, comp2={3,4}
        List<Node> nodes = Arrays.asList(new Node(0,1), new Node(1,1), new Node(2,1), new Node(3,1), new Node(4,1));
        List<Edge> edges = Arrays.asList(
                new Edge(0,2,null),
                new Edge(1,2,null),
                new Edge(0,3,null),
                new Edge(2,3,null),
                new Edge(3,4,null)
        );
        Graph g = new Graph(true, 5, nodes, edges, 0, "node");

        List<List<Integer>> sccs = Arrays.asList(
                Arrays.asList(0,1), // comp0
                Collections.singletonList(2), // comp1
                Arrays.asList(3,4) // comp2
        );

        CondensationBuilder builder = new CondensationBuilder();
        SimpleMetrics metrics = new SimpleMetrics();
        CondensationResult cr = builder.build(g, sccs, metrics);

        TopologicalSorter sorter = new TopologicalSorter();
        TopoResult res = sorter.KahnSort(cr, metrics);

        // compOrder should be [0,1,2] for this construction (priority queue chooses smallest id)
        Assertions.assertEquals(Arrays.asList(0,1,2), res.getComponentOrder());

        // derived order: nodes from comp0 sorted [0,1], then comp1 [2], then comp2 [3,4]
        Assertions.assertEquals(Arrays.asList(0,1,2,3,4), res.getDerivedOrder());

        // validate topo property: for each comp edge u->v pos(u) < pos(v)
        List<Integer> pos = Arrays.asList(new Integer[cr.getComponentCount()]);
        for (int i = 0; i < res.getComponentOrder().size(); i++) {
            pos.set(res.getComponentOrder().get(i), i);
        }
        for (int u = 0; u < cr.getComponentCount(); u++) {
            for (int v : cr.getAdjacentComponents().get(u)) {
                Assertions.assertTrue(pos.get(u) < pos.get(v), "topo order violated for edge " + u + "->" + v);
            }
        }
    }
}
