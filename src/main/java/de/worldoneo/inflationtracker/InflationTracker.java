package de.worldoneo.inflationtracker;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.worldoneo.inflationtracker.api.APIServer;
import de.worldoneo.inflationtracker.calculator.InflationCalculator;
import de.worldoneo.inflationtracker.gui.TrackerGUI;
import de.worldoneo.inflationtracker.sql.SQLManager;
import de.worldoneo.inflationtracker.util.Configurator;
import net.hypixel.api.HypixelAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

    public static void main(String[] args) throws IOException {
        Config config = Configurator.loadJson(new File("./config.json"), Config.class, new Config());
        InflationTracker inflationTracker = new InflationTracker(config);
        inflationTracker.start();

    }

    public void start() {
        try {
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
                APIServer apiServer = new APIServer(sqlManager, threadPoolExecutor, config);
                apiServer.start();
                logger.info("Started HTTP-API at {}:{}", config.httpBindAddress, config.httpBindPort);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down");
                hypixelAPI.shutdown();
            }));
            logger.info("Start complete!");

            if (config.enableGUI) {
                logger.info("Starting GUI");
                new TrackerGUI(new InflationCalculator(sqlManager, config), config);
            }
        } catch (Exception e) {
            logger.error("Something went wrong!", e);
            System.exit(1);
        }
    }

    private HikariDataSource createDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.jdbcurl);
        hikariConfig.setUsername(config.sqlUsername);
        hikariConfig.setPassword(config.sqlPassword);
        return new HikariDataSource(hikariConfig);
    }
}
