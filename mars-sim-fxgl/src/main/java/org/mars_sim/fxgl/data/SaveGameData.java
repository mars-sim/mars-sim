package org.mars_sim.fxgl.data;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.mars_sim.fxgl.main.SoftknkioApp;
import org.mars_sim.msp.core.logging.SimLogger;

public class SaveGameData {
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(SaveGameData.class.getName());

	private SaveGameData() {
	}

    public static void saveData() {
    	boolean result = DataFile.dataFile().setWritable(true);
    	if (result)
    		logger.info(null, "writable");
    	else
    		logger.info(null, "Not writable");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DataFile.dataFile()))) {

            //delete old data
            writer.write("");

            //write current data
            writer.write(SoftknkioApp.matchfield.getPlayer().getNickname());
            writer.newLine();
            writer.write(String.valueOf(SoftknkioApp.matchfield.getPlayer().getScore()));

            for (int i = 0; i < 5; i++) {
                writer.newLine();
                writer.write(String.valueOf(SoftknkioApp.matchfield.getDashboard().getOperations()[i].getLevel()));
            }

            boolean result1 = DataFile.dataFile().setWritable(false);
            if (result1)
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
