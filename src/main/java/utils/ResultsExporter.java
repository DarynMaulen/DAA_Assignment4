package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

// Export per-dataset JSON payloads and a summary CSV.
// On construction the summary.csv is created and header written.
public class ResultsExporter {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path outDir;
    private final Path summaryCsv;

    private static final String CSV_HEADER =
            "dataset,n,m,weight_model,scc_count,"
                    + "time_scc_ns,time_condense_ns,time_topo_ns,time_dag_ns,dag_relaxations,"
                    + "critical_path_len,critical_path_nodes,"
                    + "shortest_path_length,shortest_path_nodes,"
                    + "component_order,derived_task_order\n";

    public ResultsExporter(String outDirPath) throws IOException {
        this.outDir = Paths.get(outDirPath);
        if (!Files.exists(outDir)) Files.createDirectories(outDir);
        this.summaryCsv = outDir.resolve("summary.csv");

        // create or truncate the CSV and write header
        Files.write(summaryCsv,
                CSV_HEADER.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    // Write detailed per-dataset JSON.
    public void writeDatasetJson(String datasetName, Map<String, Object> payload) throws IOException {
        Path out = outDir.resolve(datasetName + ".json");
        try (FileWriter fw = new FileWriter(out.toFile(), StandardCharsets.UTF_8)) {
            gson.toJson(payload, fw);
        }
    }

    // Append one CSV line to summary.csv.
    public void appendSummaryCsv(String datasetName,
                                 int n, int m, String weightModel, int sccCount,
                                 long timeScc, long timeCondensation, long timeTopo, long timeDag,
                                 long dagRelaxations,
                                 long criticalLen, String criticalPathNodes,
                                 long shortestPathLen, String shortestPathNodes,
                                 String componentOrder, String derivedTaskOrder) throws IOException {

        // replace commas with semicolons so they don't break CSV column splitting
        String sanitizedCritical = (criticalPathNodes == null) ? "" : criticalPathNodes.replace(",", ";");
        String sanitizedShortest = (shortestPathNodes == null) ? "" : shortestPathNodes.replace(",", ";");
        String sanitizedComponentOrder = (componentOrder == null) ? "" : componentOrder.replace(",", ";");
        String sanitizedDerivedTaskOrder = (derivedTaskOrder == null) ? "" : derivedTaskOrder.replace(",", ";");

        // build CSV line exactly matching header columns
        String line = String.format("%s,%d,%d,%s,%d,%d,%d,%d,%d,%d,%d,%s,%d,%s,%s,%s",
                datasetName,
                n,
                m,
                weightModel,
                sccCount,
                timeScc,
                timeCondensation,
                timeTopo,
                timeDag,
                dagRelaxations,
                criticalLen,
                sanitizedCritical,
                shortestPathLen,
                sanitizedShortest,
                sanitizedComponentOrder,
                sanitizedDerivedTaskOrder
        );

        Files.write(summaryCsv,
                (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.APPEND);
    }
}