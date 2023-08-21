package org.mars_sim.msp.core.data;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.mars_sim.msp.core.SimulationFiles;

/**
 * Static class to log the use of Ratings for later analysis
 */
public class RatingLog {
    
    private static PrintWriter diagnosticFile;

    /**
	 * Enable the detailed diagnostics
	 * @throws FileNotFoundException 
	 */
	public static void setDiagnostics(boolean diagnostics) throws FileNotFoundException {
		if (diagnostics) {
			if (diagnosticFile == null) {
				String filename = SimulationFiles.getLogDir() + "/ratings-log.txt";
				diagnosticFile  = new PrintWriter(filename);
			}
		}
		else if (diagnosticFile != null){
			diagnosticFile.close();
			diagnosticFile = null;
		}
	}

    public static void logSelectedRating(String selectionType, String requestor,
                        Object selected, Map<? extends Object,Rating> options) {
        // Remove this once further developed
        if (diagnosticFile == null) {
            try {
                setDiagnostics(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            // Must be thread safe
            synchronized(diagnosticFile) {
			    diagnosticFile.println(History.getMarsTime().getDateTimeStamp());
                diagnosticFile.println("  Worker:" + requestor);
                diagnosticFile.println("  Type:"+ selectionType);				
                diagnosticFile.println("  Selected:" + selected);
                for (Entry<? extends Object, Rating> e : options.entrySet()) {
                    diagnosticFile.println("  " + e.getKey() + ":" + e.getValue().getOutput());
                }
                diagnosticFile.flush();                
            }
        }
    }
}
