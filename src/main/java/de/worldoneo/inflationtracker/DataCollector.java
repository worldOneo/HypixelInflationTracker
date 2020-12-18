package de.worldoneo.inflationtracker;

import de.worldoneo.inflationtracker.sql.SQLManager;
import lombok.Getter;
import lombok.Setter;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.reply.skyblock.BazaarReply;

import java.sql.SQLException;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class DataCollector extends TimerTask {
    private final HypixelAPI hypixelAPI;
    private final SQLManager sqlManager;
    @Getter
    @Setter
    private Config config;

    public DataCollector(HypixelAPI hypixelAPI, SQLManager sqlManager, Config config) {
        this.hypixelAPI = hypixelAPI;
        this.sqlManager = sqlManager;
        this.config = config;
    }


    @Override
    public void run() {
        try {
            Map<String, BazaarReply.Product> products = hypixelAPI.getBazaar().get().getProducts();
            sqlManager.insert(config.sqlTableName, products);
            InflationTracker.logger.info("Queried hypixel bazaar successful");
        } catch (SQLException | InterruptedException | ExecutionException exception) {
            InflationTracker.logger.error("Failed Hypixel bazaar tracking!", exception);
        }
    }

    public double calculateInflationIndex(Map<String, BazaarReply.Product> products) {
        double totalPrice = 0;
        for (BazaarReply.Product value : products.values()) {
            totalPrice += value.getQuickStatus().getSellPrice();
        }
        return totalPrice;
    }
}
