package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Utility class for generating various graph datasets in JSON format.
public class DataGenerator {
    private static final Random rand = new Random(100);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static class NodeDTO{
        public int id;
        public Integer duration;
    }

    public static class EdgeDTO{
        public int u;
        public int v;
        public Integer weight;
    }

    public static class GraphDTO{
        public boolean directed = true;
        public int n;
        public List<NodeDTO> nodes;
        public List<EdgeDTO> edges;
        public Integer source;
        public String weight_model;
    }

    public static void main(String[] args) throws Exception {
        String out = args != null && args.length > 0 ? args[0] : "data";
        File outDir = new File(out);
        outDir.mkdirs();

        // Small (3 files): DAG, small-cycles, dense-with-cycle
        writeDataset(outDir, "small_dag.json", 7, 0.12, false, "node");
        writeDataset(outDir, "small_cycles.json", 8, 0.15, true, "node");
        writeDataset(outDir, "small_dense_cycle.json", 9, 0.5, true, "node");

        // Medium (3 files)
        writeDataset(outDir, "medium_mix1.json", 12, 0.12, true, "node");
        writeDataset(outDir, "medium_mix2.json", 15, 0.25, true, "node");
        writeDataset(outDir, "medium_dag.json", 14, 0.18, false, "node");

        // Large (3 files)
        writeDataset(outDir, "large_sparse.json", 25, 0.06, true, "node");
        writeDataset(outDir, "large_medium.json", 35, 0.12, true, "node");
        writeDataset(outDir, "large_dense.json", 45, 0.22, true, "node");

        // metadata file
        writeMetadata(outDir);

        System.out.println("Datasets written to " + outDir.getAbsolutePath());
    }

    private static void writeDataset(File outDir, String name, int n, double p,
                                      boolean allowCycles, String weightModel) throws Exception{
        GraphDTO graph = new GraphDTO();
        graph.n = n;
        graph.weight_model = weightModel;
        graph.source = 0;
        graph.nodes = new ArrayList<>();
        // nodes: durations 1..10 for node model
        for(int i = 0; i < n; i++){
            NodeDTO node = new NodeDTO();
            node.id = i;
            node.duration = weightModel.equals("node") ? 1 + rand.nextInt(10) : null;
            graph.nodes.add(node);
        }

        graph.edges = new ArrayList<>();
        if(!allowCycles){
            // only add edges i -> j for i<j with probability p
            for (int i = 0; i < n; i++){
                for(int j = i+1; j < n; j++){
                    if(rand.nextDouble() < p){
                        EdgeDTO edge = new EdgeDTO();
                        edge.u = i;
                        edge.v = j;
                        if("edge".equals(weightModel)){
                            edge.weight = 1 + rand.nextInt(10);
                        }
                        graph.edges.add(edge);
                    }
                }
            }
        }else {
            // General directed graph with probability p for any ordered pair (i!=j)
            for(int i = 0; i < n; i++){
                for(int j = 0; j < n; j++){
                    if(i !=j){
                        if(rand.nextDouble() < p){
                            EdgeDTO edge = new EdgeDTO();
                            edge.u = i;
                            edge.v = j;
                            if("edge".equals(weightModel)){
                                edge.weight = 1 + rand.nextInt(10);
                            }
                            graph.edges.add(edge);
                        }
                    }
                }
            }
            // ensure at least one cycle exists: pick random triple and make cycle
            if (n >= 3) {
                int a = rand.nextInt(n), b = (a+1)%n, c = (a+2)%n;
                EdgeDTO edge1 = new EdgeDTO(); edge1.u = a; edge1.v = b; graph.edges.add(edge1);
                EdgeDTO edge2 = new EdgeDTO(); edge2.u = b; edge2.v = c; graph.edges.add(edge1);
                EdgeDTO edge3 = new EdgeDTO(); edge3.u = c; edge2.v = a; graph.edges.add(edge3);
            }
        }
        // write file
        try (FileWriter fw = new FileWriter(new File(outDir, name))) {
            gson.toJson(graph, fw);
        }
    }

    private static void writeMetadata(File outDir) throws Exception {
        File meta = new File(outDir, "README_Data.md");
        try (FileWriter fw = new FileWriter(meta)) {
            fw.write("# Generated datasets\n\n");
            fw.write("- small_dag.json: n=7, DAG, sparse\n");
            fw.write("- small_cycles.json: n=8, 1-2 small cycles\n");
            fw.write("- small_dense_cycle.json: n=9, dense with cycles\n");
            fw.write("- medium_mix1.json: n=12, mixed\n");
            fw.write("- medium_mix2.json: n=15, mixed\n");
            fw.write("- medium_dag.json: n=14, DAG\n");
            fw.write("- large_sparse.json: n=25, sparse, mixed\n");
            fw.write("- large_medium.json: n=35, medium density\n");
            fw.write("- large_dense.json: n=45, denser\n");
            fw.write("\nEach file contains fields: directed, n, nodes (with durations for node-model), edges (u,v,w), source, weight_model.\n");
        }
    }
}
