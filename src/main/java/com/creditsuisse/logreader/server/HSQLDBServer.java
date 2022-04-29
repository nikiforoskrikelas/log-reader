package com.creditsuisse.logreader.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HSQLDBServer implements Server {
    private static final Logger LOGGER = LogManager.getLogger(HSQLDBServer.class);

    private org.hsqldb.Server server;

    public HSQLDBServer() {
        this(new org.hsqldb.Server());
    }

    //Parametrized constructor allows testing to mock server
    public HSQLDBServer(org.hsqldb.Server server) {
        this.server = server;
        //These should be in a properties file
        server.setDatabaseName(0, "logsdb");
        server.setDatabasePath(0, "file:db/logsdb");
        server.setAddress("localhost");
        server.setPort(9001); // this is the default port
    }

    public int start() {
        LOGGER.info("Starting HSQL server");
        server.start();
        return server.getState();
    }

    public int stop() {
        LOGGER.info("Stopping HSQL server");
        server.stop();
        return server.getState();

    }
}