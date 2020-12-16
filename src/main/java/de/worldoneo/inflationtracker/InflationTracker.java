package de.worldoneo.inflationtracker;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.worldoneo.inflationtracker.api.APIServer;
import de.worldoneo.inflationtracker.sql.SQLManager;
import de.worldoneo.inflationtracker.util.Configurator;
import net.hypixel.api.HypixelAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class InflationTracker {
    public final Config config;
    public static final Logger logger = LoggerFactory.getLogger("InflationTracker");

    public InflationTracker(Config config) {
        this.config = config;
    }

    public static void main(String[] args) throws IOException, SQLException {
        Config config = Configurator.loadJson(new File("./config.json"), Config.class, new Config());
        InflationTracker inflationTracker = new InflationTracker(config);
        inflationTracker.start();
    }

    public void start() throws SQLException, IOException {
        logger.info("Initializing SQL");
        HikariDataSource hikariDataSource = createDataSource();
        SQLManager sqlManager = new SQLManager(hikariDataSource);
        logger.info("Initializing SQL-Table");
        sqlManager.createTable(config.sqlTableName);
        logger.info("Initialized SQL");

        HypixelAPI hypixelAPI = new HypixelAPI(UUID.fromString(config.apiKey));

        logger.info("Starting DataCollector");
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new DataCollector(hypixelAPI, sqlManager, config), config.hypixelRequestPeriod, config.hypixelRequestPeriod);
        logger.info("Started DataCollector");

        if (config.enableHTTPAPI) {
            logger.info("Starting HTTP-API at {}:{}", config.httpBindAddress, config.httpBindPort);
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(12);
            new APIServer(sqlManager, threadPoolExecutor, config).start();
            logger.info("Started HTTP-API at {}:{}", config.httpBindAddress, config.httpBindPort);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down");
            hypixelAPI.shutdown();
        }));
        logger.info("Start complete!");
    }

    private HikariDataSource createDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.jdbcurl);
        hikariConfig.setUsername(config.sqlUsername);
        hikariConfig.setPassword(config.sqlPassword);
        return new HikariDataSource(hikariConfig);
    }
}
