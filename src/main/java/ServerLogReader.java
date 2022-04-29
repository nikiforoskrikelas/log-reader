import com.fasterxml.jackson.databind.ObjectMapper;
import models.LogMessage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;


public class ServerLogReader {
    private static final Logger LOGGER = LogManager.getLogger(ServerLogReader.class);

    private static final String SERVER_LOG_FILE_PATH = "logfile.txt";


    public static void main(String[] args) throws IOException {
        LOGGER.info("Running Log Reader");

        File serverLogFile = new File(SERVER_LOG_FILE_PATH);
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
