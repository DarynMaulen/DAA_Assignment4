package graph.scc;

import graph.model.Edge;
import graph.model.Graph;
import metrics.Metrics;

import java.util.*;

// Implements Tarjan's algorithm to find Strongly Connected Components (SCCs) in a directed graph.
public class Tarjan {
    private final Graph graph;
    // A list where each inner list represents an SCC.
    private final List<List<Integer>> components = new ArrayList<>();

    // Adjacency list for efficient neighbor lookup.
    private final List<List<Integer>> adj;

    // Helper variables for Tarjan's algorithm
    private int time;
    private int[] disc; // Discovery time of a node
    private int[] low; // Lowest disc-index reachable from a node u
    private boolean[] onStack;
    private Deque<Integer> stack; // Stack of nodes for forming SCCs

    // Initializes the graph and builds the adjacency list.
    public Tarjan(Graph graph) {
        this.graph = graph;
        this.adj = buildAdj(graph);
    }

    // Builds the adjacency list from the graph's list of edges.
    private List<List<Integer>> buildAdj(Graph graph) {
        int n = graph.getN();
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for(Edge edge : graph.getEdges()) {
            int u = edge.getU();
            int v = edge.getV();
            // Basic bounds checking
            if(u >= 0 && v >= 0 && u < n && v < n) {
                adj.get(u).add(v);
            }
        }
        return adj;
    }

    // Finds all Strongly Connected Components of the graph.
    public List<List<Integer>> findSCCs(Metrics metrics) {
        int n = graph.getN();
        // Initialize helper arrays
        disc = new int[n];
        Arrays.fill(disc, -1); // -1 means node is unvisited
        low = new int[n];
        onStack = new boolean[n];
        stack = new ArrayDeque<>();
        time = 0;
        components.clear();

        if(metrics!=null) {
            metrics.startTimer();
        }

        // Run DFS from every unvisited node
        for(int i=0; i<n; i++) {
            if(disc[i]==-1) {
                dfs(i, metrics);
            }
        }

        if(metrics!=null) {
            metrics.stopTimer();
            metrics.putLong("time.scc", metrics.getElapsedNanos());
        }

        return Collections.unmodifiableList(components);
    }

    // Recursive Depth First Search (DFS) traversal for Tarjan's algorithm.
    private void dfs(int u, Metrics metrics) {
        // Set discovery time and low-link value for node u
        disc[u] = low[u] = time++;
        if(metrics!=null) {
            metrics.inc("scc.dfs.visits");
        }

        // Push node u onto the stack and mark it as being on the stack
        stack.push(u);
        onStack[u] = true;
        if(metrics!=null) {
            metrics.inc("scc.stack.push");
        }

        // Traverse all neighbors
        for(int v : adj.get(u)) {
            if(metrics!=null) {
                metrics.inc("scc.dfs.edges");
            }
            if(disc[v]==-1) {
                // Recurse, then update low[u] based on low[v]
                dfs(v, metrics);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                // Found a back edge, update low[u] based on disc[v]
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        // If low[u] == disc[u], then u is the root of an SCC, pop component from stack
        if(low[u]==disc[u]) {
            List<Integer> component = new ArrayList<>();
            while (true){
                int w = stack.pop();
                if(metrics!=null) {
                    metrics.inc("scc.stack.pop");
                }
                onStack[w] = false;
                component.add(w);
                if(w == u ){
                    break;
                }
            }
            components.add(component);
        }
    }
}