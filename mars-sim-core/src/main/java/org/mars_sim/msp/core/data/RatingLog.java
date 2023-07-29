package org.mars_sim.msp.core.data;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.SimulationFiles;

/**
 * Static class to log the use of Ratings for later analysis.
 * It uses JSONLines format which is a structured compressed style of output commonly used for large log files.
 * @see https://jsonlines.org/
 * It can be parsed using the JQ tools @see https://jqlang.github.io/jq/
 */
public class RatingLog {
    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.###");
	private static final Logger logger = Logger.getLogger(RatingLog.class.getName());

    private static PrintWriter diagnosticFile;
    private static Set<String> modules = new HashSet<>();

    /**
	 * Enable the detailed diagnostics
	 * @throws FileNotFoundException 
	 */
	public static void setDiagnostics(String module, boolean diagnostics) throws FileNotFoundException {
        module = module.toLowerCase();
		if (diagnostics) {
            logger.info("Start Ratings logging for " + module);
            modules.add(module);
		}
        else {
            logger.info("Stop Ratings logging for " + module);
            modules.remove(module);
        }

        // Decide on action
        if (!modules.isEmpty() && (diagnosticFile == null)) {
            String filename = SimulationFiles.getLogDir() + "/ratings-log.jsonl";
            logger.info("Ratings log file = " + filename);
            diagnosticFile  = new PrintWriter(filename);
        }
		else if (modules.isEmpty() && (diagnosticFile != null)) {
			diagnosticFile.close();
			diagnosticFile = null;
		}
	}

    public static void logSelectedRating(String module, String requestor,
                        Rateable selected, Map<? extends Rateable,Rating> options) {
        if (modules.contains(module)) {
            StringBuilder output = new StringBuilder();
            output.append("{\"time\":\"")
                        .append(History.getMarsTime().getDateTimeStamp())
                        .append("\",\"type\":\"")
                        .append(module)
                        .append("\",\"requestor\":\"")			
                        .append(requestor)
                        .append("\",\"selected\":\"")
                        .append(selected.getName())
                        .append("\",\"options\":[");

            // Output each output
            output.append(options.entrySet().stream()
                .map(entry -> "{\"rated\":\"" + entry.getKey().getName() + "\",\"rating\":"
                             + ratingToJsonLines(entry.getValue()) + "}")
                            .collect(Collectors.joining(",")));

            output.append("]}");

            // Must be thread safe
            synchronized(diagnosticFile) {
                diagnosticFile.println(output.toString());
                diagnosticFile.flush();                
            }
        }
    }

    /**
     * Take a Ratings object and convert it into JSON lines format.
     * @param r Rating to be described
     * @return JSONLines fragment
     */
    private static String ratingToJsonLines(Rating r) {
        return "{\"score\":" + SCORE_FORMAT.format(r.getScore())
                + ",\"base\":" + SCORE_FORMAT.format(r.getBase())
                + ",\"modifiers\":{"
                + r.getModifiers().entrySet().stream()
                            .map(entry -> "\"" + entry.getKey() + "\":"
                                        + SCORE_FORMAT.format(entry.getValue()))
                            .collect(Collectors.joining(","))
                + "}}";
    }
}
 