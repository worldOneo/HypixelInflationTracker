package de.worldoneo.inflationtracker.util;

import com.google.gson.Gson;
import de.worldoneo.inflationtracker.api.APIResponse;
import de.worldoneo.inflationtracker.calculator.InflationCalculator;
import de.worldoneo.inflationtracker.sql.SQLManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;

public class Exporter {
    public static void writeTruncateFile(File file, byte[] data) throws IOException {
        File absoluteFile = file.getAbsoluteFile();
        if (!absoluteFile.exists()) {
            absoluteFile.getParentFile().mkdirs();
        }
        Files.write(absoluteFile.toPath(), data);
    }

    public static void exportCSV(File file, List<InflationCalculator.Point> results) throws IOException {
        byte[] data = exportByteCSV(results);
        writeTruncateFile(file, data);
    }

    public static void exportJSON(File file, List<InflationCalculator.Point> results, boolean prettyPrint) throws IOException {
        byte[] data = exportByteJSON(results, prettyPrint);
        writeTruncateFile(file, data);
    }

    public static void exportCSV(File file, InflationCalculator inflationCalculator, String... productTypes) throws SQLException, IOException {
        List<InflationCalculator.Point> results = inflationCalculator.getInflationPoints(productTypes);
        exportCSV(file, results);
    }

    public static void exportJSON(File file, InflationCalculator inflationCalculator,
                                  boolean prettyPrint, String... productTypes) throws SQLException, IOException {
        List<InflationCalculator.Point> results = inflationCalculator.getInflationPoints(productTypes);
        exportJSON(file, results, prettyPrint);
    }

    public static void exportJSONAPIResponse(File file, InflationCalculator inflationCalculator, String... productTypes) throws SQLException, IOException {
        List<InflationCalculator.Point> results = inflationCalculator.getInflationPoints(productTypes);
        byte[] data = SQLManager.gson.toJson(new APIResponse(results, productTypes)).getBytes();
        writeTruncateFile(file, data);
    }

    public static byte[] exportByteCSV(List<InflationCalculator.Point> points) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("time,value").append(System.lineSeparator());
        for (InflationCalculator.Point point : points) {
            stringBuilder.append(point.getTime())
                    .append(',')
                    .append(point.getValue())
                    .append(System.lineSeparator());
        }
        return stringBuilder.toString().getBytes();
    }


    public static byte[] exportByteJSON(List<InflationCalculator.Point> points, boolean prettyPrint) {
        Gson gson = prettyPrint ? Configurator.gson : SQLManager.gson;
        return gson.toJson(points).getBytes();
    }

}
