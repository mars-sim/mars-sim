/**
 * Mars Simulation Project
 * ConfFileProcessor.java
 * @version 2.71 2000-09-25
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.Vector;

/** The ConfFileProcessor class contains static methods for retrieving unit names from
 *  user configuration files.  This class will probably be replaced or extended later
 *  when more advanced configuration files are used.
 */

class ConfFileProcessor {

    /** Retrieves settlement names from "settlements.conf". */
    static String[] getSettlementNames() {
        return getNames("conf/settlements.conf");
    }

    /** Retrieves rover names from "rovers.conf". */
    static String[] getRoverNames() {
        return getNames("conf/rovers.conf");
    }

    /** Retrieves person names from "people.conf". */
    static String[] getPersonNames() {
        return getNames("conf/people.conf");
    }

    /** Retrieves names from a given configuration file. */
    static private String[] getNames(String configFile) {
        try {
            LineNumberReader configReader = new LineNumberReader(new FileReader(configFile));

            Vector namesVec = new Vector();
            String name;
            while ((name = configReader.readLine()) != null) {
                if (!name.trim().equals("")) {
                    namesVec.addElement(name.trim());
                }
            }

            return (String[]) namesVec.toArray(new String[0]);
        } catch (IOException e) {
            // If there is an error reading config file, kill the simulation.
            System.out.println("Error reading " + configFile);
            System.exit(0);
        }

        return null;
    }
}

