package de.worldoneo.inflationtracker.sql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariDataSource;
import net.hypixel.api.reply.skyblock.BazaarReply;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * CREATE TABLE `hypixeldata` (
 * `id` BIGINT(19,0) NOT NULL AUTO_INCREMENT,
 * `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 * `data` JSON NULL DEFAULT NULL,
 * PRIMARY KEY (`id`) USING BTREE
 * )
 * COLLATE='utf8mb4_bin'
 * ENGINE=InnoDB;
 */
public class SQLManager {
    private HikariDataSource hikariDataSource;
    public static final Gson gson = new GsonBuilder().create();

    public SQLManager(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    public void createTable(String table) throws SQLException {
        try (Connection connection = this.hikariDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("CREATE TABLE IF NOT EXISTS `%s` (" +
                             "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                             "`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                             "`data` JSON NULL DEFAULT NULL," +
                             "PRIMARY KEY (`id`) USING BTREE" +
                             ")" +
                             "COLLATE='utf8mb4_bin'" +
                             "ENGINE=InnoDB;", table)
             )) {
            preparedStatement.execute();
        }
    }

    public void insert(String table, Map<String, BazaarReply.Product> products) throws SQLException {
        String json = gson.toJson(products);
        try (Connection connection = this.hikariDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("INSERT INTO `%s` (data) VALUES (?);", table)
             )) {
            preparedStatement.setString(1, json);
            preparedStatement.execute();
        }
    }

    public List<SQLEntry> get(String table, String productType) throws SQLException {
        List<SQLEntry> sqlEntries = new LinkedList<>();
        try (Connection connection = this.hikariDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     String.format("SELECT time, data->'$.%s' AS data FROM `%s`;", productType, table)
             )) {
            ResultSet rowSet = preparedStatement.executeQuery();
            while (rowSet.next()) {
                String data = rowSet.getString("data");
                BazaarReply.Product product = gson.fromJson(data, BazaarReply.Product.class);
                sqlEntries.add(new SQLEntry(rowSet.getTimestamp("time"), product));
            }
        }
        return sqlEntries;
    }

    public Map<String, List<SQLEntry>> get(String table, String... productTypes) throws SQLException {
        Map<String, List<SQLEntry>> sqlEntries = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder("SELECT time");
        for (String productType : productTypes) {
            stringBuilder.append(", data->'$.").append(productType).append("' AS ").append(productType).append(" ");
            sqlEntries.put(productType, new LinkedList<>());
        }
        stringBuilder.append(" FROM ").append(table).append(";");
        try (Connection connection = this.hikariDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString())) {
            ResultSet rowSet = preparedStatement.executeQuery();
            while (rowSet.next()) {
                Timestamp time = rowSet.getTimestamp("time");
                for (String productType : productTypes) {
                    String data = rowSet.getString(productType);
                    BazaarReply.Product product = gson.fromJson(data, BazaarReply.Product.class);
                    sqlEntries.get(productType).add(new SQLEntry(time, product));
                }
            }
        }
        return sqlEntries;
    }
}

