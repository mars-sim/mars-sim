/**
 * Mars Simulation Project
 * ConfFileProcessor.java
 * @version 2.70 2000-09-08
 * @author Scott Davis
 */

import java.io.*;
import java.util.*;

/** The ConfFileProcessor class contains static methods for retrieving unit names from
 *  user configuration files.  This class will probably be replaced or extended later
 *  when more advanced configuration files are used.
 */

public class ConfFileProcessor {

    /** Retrieves settlement names from "settlements.conf". */
    static public String[] getSettlementNames() { return getNames("settlements.conf"); }
    
    /** Retrieves rover names from "rovers.conf". */
    static public String[] getRoverNames() { return getNames("rovers.conf"); }
    
    /** Retrieves person names from "people.conf". */
    static public String[] getPersonNames() { return getNames("people.conf"); }
    
    /** Retrieves names from a given configuration file. */
    static private String[] getNames(String configFile) {
        String[] result = null;
        try {
            LineNumberReader configReader = new LineNumberReader(new FileReader(configFile));
       
            Vector settlementNames = new Vector();
            String name;
            while((name = configReader.readLine()) != null) settlementNames.addElement(name.trim());
       
            result = new String[settlementNames.size()];
            for (int x=0; x < settlementNames.size(); x++) result[x] = (String) settlementNames.elementAt(x);  
        }
        catch(IOException e) {
            // If there is an error reading config file, kill the simulation.
            System.out.println("Error reading " + configFile); 
            System.exit(0);
        }
        
        return result;
    }
}
        
        
        
