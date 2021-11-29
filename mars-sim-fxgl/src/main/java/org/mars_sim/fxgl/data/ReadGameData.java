package org.mars_sim.fxgl.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.logging.SimLogger;

public class ReadGameData {
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ReadGameData.class.getName());

     /*
        data format:
            #0: nickname
            #1: score
            #2: pointOperation level
            #3: machineOperation level
            #4: excavatorOperation level
            #5: mineOperation level
            #6: factoryOperation level
     */

	private ReadGameData() {
	}

    public static Optional<List<String>> readData() {
        try {
            //if data.txt does not exist it will be created and filled with initial values
            if (!DataFile.dataFile().exists()) {
                Files.createFile(Paths.get(DataFile.dataFile().getPath()));
                initFile(DataFile.dataFile());
            }

        } catch (IOException e) {
        	logger.severe(null, "Exception: ", e);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(DataFile.dataFile()))) {
            return Optional.of(reader.lines().collect(Collectors.toList()));
        } catch (Exception e) {
        	logger.severe(null, "Exception: ", e);
        } finally {
            	// Multiple streams were opened. Only the last is closed.
        }

        return Optional.empty();
    }

    private static void initFile(File file) throws IOException {
        try (PrintStream writer = new PrintStream(new FileOutputStream(file, true))) {
        	writer.append("<nickname>\n" + "0\n" + "1\n" + "0\n" + "0\n" + "0\n" + "0");
            boolean result = file.setWritable(false);
        	if (result)
        		logger.info(null, "writable");
        	else
        		logger.info(null, "Not writable");
	    } catch (Exception e) {
        	logger.severe(null, "Exception: ", e);
	    } finally {
	        	// Multiple streams were opened. Only the last is closed.
	    }
    }
}
