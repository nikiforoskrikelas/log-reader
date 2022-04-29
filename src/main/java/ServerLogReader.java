import com.fasterxml.jackson.databind.ObjectMapper;
import models.LogMessage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;


public class ServerLogReader {
    private static final Logger LOGGER = LogManager.getLogger(ServerLogReader.class);

    @Option(name = "-file", usage = "input file", required = true)
    private String inputFilePath;

    public static void main(String[] args) throws IOException {
        new ServerLogReader().doMain(args);
    }

    private void doMain(String[] args) throws IOException {
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


        try (LineIterator it = FileUtils.lineIterator(serverLogFile, "UTF-8")) {
            while (it.hasNext()) {
                String line = it.nextLine();
                LOGGER.debug(line);

                LogMessage logMessage = mapper.readValue(line, LogMessage.class);

                LOGGER.debug(logMessage);
                LOGGER.info(logMessage);
            }
        }
    }
}


