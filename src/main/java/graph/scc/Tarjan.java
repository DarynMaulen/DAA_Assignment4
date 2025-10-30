package graph.scc;

import graph.model.Edge;
import graph.model.Graph;
import metrics.Metrics;

import java.util.*;

public class Tarjan {
    private final Graph graph;
    private final List<List<Integer>> components = new ArrayList<>();

    private final List<List<Integer>> adj;

    private int time;
    private int[] disc;
    private int[] low;
    private boolean[] onStack;
    private Deque<Integer> stack;

    public Tarjan(Graph graph) {
        this.graph = graph;
        this.adj = buildAdj(graph);
    }

    private List<List<Integer>> buildAdj(Graph graph) {
        int n = graph.getN();
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for(Edge edge : graph.getEdges()) {
            int u = edge.getU();
            int v = edge.getV();
            if(u >= 0 && v >= 0 && u < n && v < n) {
                adj.get(u).add(v);
            }
        }
        return adj;
    }

    public List<List<Integer>> findSCCs(Metrics metrics) {
        int n = graph.getN();
        disc = new int[n];
        Arrays.fill(disc, -1);
        low = new int[n];
        onStack = new boolean[n];
        stack = new ArrayDeque<>();
        time = 0;
        components.clear();

        if(metrics!=null) {
            metrics.startTimer();
        }

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

    private void dfs(int u, Metrics metrics) {
        disc[u] = low[u] = time++;
        if(metrics!=null) {
            metrics.inc("scc.dfs.visits");
        }

        stack.push(u);
        onStack[u] = true;
        if(metrics!=null) {
            metrics.inc("scc.stack.push");
        }

        for(int v : adj.get(u)) {
            if(metrics!=null) {
                metrics.inc("scc.dfs.edges");
            }
            if(disc[v]==-1) {
                dfs(v, metrics);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }

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
