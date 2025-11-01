package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

// Write per-dataset JSON result and summary CSV line.
public class ResultsExporter {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File outDir;
    private final File summaryCsv;

    public ResultsExporter(String outDirPath) throws Exception {
        outDir = new File(outDirPath);
        if(!outDir.exists()){
            outDir.mkdirs();
        }
        summaryCsv = new File(outDir, "summary.csv");
        if(!summaryCsv.exists()){
            try(PrintWriter pw = new PrintWriter(summaryCsv)) {
                pw.println("dataset,n,m,weight_model,scc_count,time_scc_ns,time_condense_ns,time_topo_ns,time_dag_ns,dag_relaxations,critical_path_len,critical_path_nodes");
            }
        }
    }

    public void writeDatasetJson(String datasetName, Map<String,Object> payload) throws Exception {
        File out = new File(outDir, datasetName + ".json");
        try(FileWriter fw = new FileWriter(out)) {
            gson.toJson(payload, fw);
        }
    }

    public void appendSummaryCsv(String datasetName, int n, int m, String weightModel,
                                 int sccCount, long timeScc, long timeCondensation, long timeTopo, long timeDag,
                                 long dagRelaxations, long criticalLen, String criticalPathNodes) throws Exception {
        String line = String.format("%s,%d,%d,%s,%d,%d,%d,%d,%d,%d,%d,%s",
                datasetName, n, m, weightModel, sccCount, timeScc, timeCondensation, timeTopo, timeDag, dagRelaxations, criticalLen,
                criticalPathNodes.replaceAll(",", ";"));
        Files.write(summaryCsv.toPath(), (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
    }
}
