package org.mars_sim.fxgl.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.mars_sim.fxgl.main.SoftknkioApp;

public class SaveGameData {

	private SaveGameData() {
	}

    public static void saveData() {
    	boolean result = DataFile.dataFile().setWritable(true);
    	if (result)
    		System.out.println("writable");
    	else
    		System.out.println("Not writable");

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
            	writer.close();

        } catch (IOException e) {

        } finally {
            // Multiple streams were opened. Only the last is closed.
        }
    }
}
