package de.worldoneo.inflationtracker.api;

import com.sun.net.httpserver.HttpServer;
import de.worldoneo.inflationtracker.Config;
import de.worldoneo.inflationtracker.sql.SQLManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public class APIServer {
    private boolean sealed = false;
    private SQLManager sqlManager;
    private Executor threadPoolExecutor;
    private final Config config;
    private HttpServer server;

    public APIServer(SQLManager sqlManager, Executor threadPoolExecutor, Config config) {
        this.sqlManager = sqlManager;
        this.threadPoolExecutor = threadPoolExecutor;
        this.config = config;
    }


    public void start() throws IOException {
        if (sealed) return;
        sealed = true;
        server = HttpServer.create(new InetSocketAddress(config.httpBindAddress, config.httpBindPort), 0);
        server.createContext(config.httpPath, new APIHandler(sqlManager, config));
        server.setExecutor(threadPoolExecutor);
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
