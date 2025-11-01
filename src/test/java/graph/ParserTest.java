package graph;

import graph.model.Graph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class ParserTest {

    @Test
    public void testParseSampleJson() throws Exception {
        Path tmp = Files.createTempFile("sample", ".json");
        String sampleJson = "{\n" +
                "  \"directed\": true,\n" +
                "  \"n\": 8,\n" +
                "  \"edges\": [\n" +
                "    {\"u\": 0, \"v\": 1, \"w\": 3},\n" +
                "    {\"u\": 1, \"v\": 2, \"w\": 2},\n" +
                "    {\"u\": 2, \"v\": 3, \"w\": 4},\n" +
                "    {\"u\": 3, \"v\": 1, \"w\": 1},\n" +
                "    {\"u\": 4, \"v\": 5, \"w\": 2},\n" +
                "    {\"u\": 5, \"v\": 6, \"w\": 5},\n" +
                "    {\"u\": 6, \"v\": 7, \"w\": 1}\n" +
                "  ],\n" +
                "  \"source\": 4,\n" +
                "  \"weight_model\": \"edge\"\n" +
                "}";
        Files.writeString(tmp, sampleJson);

        TasksJsonParser parser = new TasksJsonParser();
        Graph g = parser.parse(tmp.toString());

        Assertions.assertEquals(8, g.getN());
        Assertions.assertTrue(g.isDirected());
        Assertions.assertEquals("edge", g.getWeightModel());
        Assertions.assertEquals(7, g.getEdges().size());
        Assertions.assertEquals(4, g.getSource());
    }
}
