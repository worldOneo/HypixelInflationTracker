package de.worldoneo.inflationtracker.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.worldoneo.inflationtracker.Config;
import de.worldoneo.inflationtracker.InflationTracker;
import de.worldoneo.inflationtracker.calculator.InflationCalculator;
import de.worldoneo.inflationtracker.sql.SQLManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class APIHandler implements HttpHandler {
    public final Config config;
    private byte[] data;
    private final String[] products;
    private InflationCalculator inflationCalculator;

    public APIHandler(SQLManager sqlManager, Config config) {
        this.config = config;
        this.products = config.basket;
        inflationCalculator = new InflationCalculator(sqlManager, config);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(this::crunch, 0, config.httpAPICrunchPeriod, TimeUnit.MILLISECONDS);
    }

    public void crunch() {
        try {
            List<InflationCalculator.Point> table = inflationCalculator.getInflationPoints(products);
            data = SQLManager.gson.toJson(new APIResponse(table, products)).getBytes();
        } catch (SQLException sqlException) {
            InflationTracker.logger.error("Failed to calculate Inflation!", sqlException);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        InflationTracker.logger.info("API request from {}:{}",
                exchange.getRemoteAddress().getAddress().toString(),
                exchange.getRemoteAddress().getPort());
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, data.length);
        exchange.getResponseBody().write(data);
        exchange.getResponseBody().flush();
        exchange.close();
    }

    public Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
}
