package de.softknk.data;

import java.io.File;

public class DataFile {

    private static final File DATA_FILE = new File(System.getProperty("user.dir") + "\\data.txt");

    public static File dataFile() {
        return DATA_FILE;
    }
}
