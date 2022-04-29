package com.creditsuisse.logreader;

import com.creditsuisse.logreader.models.LogMessage;
import com.creditsuisse.logreader.server.HSQLDBServer;
import com.creditsuisse.logreader.server.Server;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.Level.DEBUG;

public class ServerLogReader {
    private static final Logger LOGGER = LogManager.getLogger(ServerLogReader.class);
    private static final int SLA = 4;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static Server server = new HSQLDBServer();

    @Option(name = "-file", usage = "input file", required = true)
    private String inputFilePath;


    protected ServerLogReader() {
    }

    public static void main(String[] args) {
        LOGGER.info("Running Log Reader");

        try {
            server.start();

            new ServerLogReader().doMain(args);

            server.stop();
            System.exit(0);

        } catch (Exception e) {
            LOGGER.error(e);
            server.stop();
            System.exit(-1);

        }

    }

    private void doMain(String[] args) throws Exception {
        File serverLogFile = parseCmdArgs(args);

        if (serverLogFile == null) return;

        Map<String, LogMessage> idToMessage = new HashMap<>();

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // start a transaction
            transaction = session.beginTransaction();

            //Stream through the file
            //Since the entire file is not fully in memory this will  result in low memory consumption
            //Process file and place in logsdb, also notify of long events
            try (LineIterator it = FileUtils.lineIterator(serverLogFile, "UTF-8")) {
                while (it.hasNext()) {
                    String line = it.nextLine();
                    LOGGER.debug(line);

                    LogMessage logMessage = MAPPER.readValue(line, LogMessage.class);
                    LOGGER.info("Processing " + logMessage);

                    // Combine events
                    String currentId = logMessage.getId();
                    if (idToMessage.containsKey(logMessage.getId())) {
                        Long duration = Math.abs(Math.subtractExact(logMessage.getTimestamp(), idToMessage.get(currentId).getTimestamp()));

                        logMessage.setDuration(duration);

                        if (duration > SLA)
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
            LOGGER.info("SUCCESS: " + inputFilePath + " has been processed!");

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

        }




        //Display db contents for ease of verifying results
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<LogMessage> students = session.createQuery("from LogMessage", LogMessage.class).list();
            LOGGER.info("-------------------------------------Database Contents START-----------------------------");
            for (LogMessage i : students) {
                LOGGER.info(i);
            }
            LOGGER.info("-------------------------------------Database Contents END-------------------------------");

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.error(e.getMessage(), e);
        }

    }

    private File parseCmdArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);

        } catch (CmdLineException e) {
            LOGGER.error(e.getMessage());
            // print the list of available options
            parser.printUsage(System.err);

            return null;
        }

        File serverLogFile = new File(inputFilePath);
        LOGGER.info("Reading input from file: " + serverLogFile);
        return serverLogFile;
    }


}


