package de.worldoneo.inflationtracker.calculator;

import de.worldoneo.inflationtracker.Config;
import de.worldoneo.inflationtracker.sql.SQLEntry;
import de.worldoneo.inflationtracker.sql.SQLManager;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InflationCalculator {
    public final SQLManager sqlManager;
    public final Config config;

    public InflationCalculator(SQLManager sqlManager, Config config) {
        this.sqlManager = sqlManager;
        this.config = config;
    }

    public double getInflation(String... productTypes) throws SQLException {
        Map<String, List<SQLEntry>> stringListMap = sqlManager.get(config.sqlTableName, productTypes);
        return getInflation(stringListMap, productTypes);
    }

    private double getInflation(Map<String, List<SQLEntry>> stringListMap, String[] productTypes) {
        double totalInflation = 0;
        for (String productType : productTypes) {
            List<SQLEntry> sqlEntries = stringListMap.get(productType);
            double F = sqlEntries.get(0).product.getQuickStatus().getSellPrice();
            double I = sqlEntries.get(sqlEntries.size() - 1).product.getQuickStatus().getSellPrice();
            totalInflation += I / F;
        }
        return totalInflation / productTypes.length;
    }

    public List<Point> getInflationPoints(String... productTypes) throws SQLException {
        Map<String, List<SQLEntry>> stringListMap = sqlManager.get(config.sqlTableName, productTypes);
        List<Point> inflationPoints = new LinkedList<>();
        for (int i = 0; i < stringListMap.get(productTypes[0]).size(); i++) {
            double totalInflation = 0;
            for (String productType : productTypes) {
                List<SQLEntry> sqlEntries = stringListMap.get(productType);
                double F = sqlEntries.get(0).product.getQuickStatus().getSellPrice();
                double I = sqlEntries.get(i).product.getQuickStatus().getSellPrice();
                totalInflation += I / F;
            }
            inflationPoints.add(new Point(stringListMap.get(productTypes[0]).get(i).date,
                    totalInflation / productTypes.length));
        }
        return inflationPoints;
    }

    public static class Point {
        public final long time;
        public final double value;

        public Point(long time, double value) {
            this.time = time;
            this.value = value;
        }
    }
}
