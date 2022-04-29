package com.creditsuisse.logreader;

import com.creditsuisse.logreader.models.LogMessage;
import com.creditsuisse.logreader.uitl.HibernateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hsqldb.Server;

public class ServerLogReader {
    private static final Logger LOGGER = LogManager.getLogger(ServerLogReader.class);
    private static final int SLA = 4;

    @Option(name = "-file", usage = "input file", required = true)
    private String inputFilePath;

    public static void main(String[] args){
        Server server = new Server();

        try {
            server.setDatabaseName(0, "logsdb");
            server.setDatabasePath(0, "file:db/logsdb");
            server.setAddress("localhost");
            server.setPort(9001); // this is the default port
            server.start();

            new ServerLogReader().doMain(args);
        } catch (Exception e) {
            LOGGER.error(e);
            server.stop();
            System.exit(-1);

        }

        server.stop();
        System.exit(0);


    }

    private void doMain(String[] args){
        LOGGER.info("Running Log Reader");
        CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);

        } catch (CmdLineException e) {
            LOGGER.error(e.getMessage());
            // print the list of available options
            parser.printUsage(System.err);

            return;
        }

        File serverLogFile = new File(inputFilePath);
        LOGGER.info("Reading input from file: " + serverLogFile);


        ObjectMapper mapper = new ObjectMapper();


        Map<String, LogMessage> idToMessage = new HashMap<>();

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // start a transaction
            transaction = session.beginTransaction();

            //Process file and place in logsdb, also notify of long events
            try (LineIterator it = FileUtils.lineIterator(serverLogFile, "UTF-8")) {
                while (it.hasNext()) {
                    String line = it.nextLine();
                    LOGGER.debug(line);

                    LogMessage logMessage = mapper.readValue(line, LogMessage.class);
                    LOGGER.info("Processing " + logMessage);

                    // Combine events
                    String currentId = logMessage.getId();
                    if (idToMessage.containsKey(logMessage.getId())) {
                        Long duration = Math.abs(Math.subtractExact(logMessage.getTimestamp(), idToMessage.get(currentId).getTimestamp()));

                        logMessage.setDuration(duration);

                        if(duration>SLA)
                            LOGGER.info("Event with id " + logMessage.getId() + " breached SLA of " + SLA + " milliseconds");

                        logMessage.setAlert(duration > SLA);

                        session.save(logMessage);
                        LOGGER.info("Stored " + logMessage);

                    } else {
                        idToMessage.put(logMessage.getId(), logMessage);
                    }

                }
            }

            transaction.commit();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<LogMessage> students = session.createQuery("from LogMessage", LogMessage.class).list();
            for (LogMessage i : students) {
                LOGGER.info(i);
            }

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.error(e.getMessage(), e);
        }
    }
}


