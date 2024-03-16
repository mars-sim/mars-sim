/*
 * Mars Simulation Project
 * RatingLog.java
 * @date 2023-08-21
 * @author Barry Evans
 */
package com.mars_sim.core.data;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.logging.SimLogger;

/**
 * Static class to log the use of Ratings for later analysis.
 * It uses JSONLines format which is a structured compressed style of output commonly used for large log files.
 * @see https://jsonlines.org/
 * It can be parsed using the JQ tools @see https://jqlang.github.io/jq/
 * Use filter as '.' and enable Slurp which will convert into seperate Json records.
 */
public class RatingLog {
    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.###");
	private static final SimLogger logger = SimLogger.getLogger(RatingLog.class.getName());

    private static PrintWriter diagnosticFile;
    private static Map<String,Set<String>> modules = new HashMap<>();

    private RatingLog() {
        // Prevent instance creation
    }

    /**
	 * Enable the detailed diagnostics according to a diagnostic spec. 
     * @param spec Ths has the format of a module & optional selector
     * @param enabled Are the diagnostics enabled for this specification
	 * @throws FileNotFoundException 
	 */
	public static void setDiagnostics(String spec, boolean enabled) throws FileNotFoundException {
        String []parts = spec.toLowerCase().split(":");
        String module = parts[0];

        // Different logic according if there is a selector to the module
        if (parts.length == 2) {
            // Has a selector
            Set<String> existing = modules.computeIfAbsent(module, m -> new HashSet<>());
            String selector = parts[1].toLowerCase(); // Selector always converted to lower case
            if (enabled) {
                existing.add(selector); 
            }
            else {
                existing.remove(selector);
            }
        }
        // Process just on a module wildcard
        else if (enabled) {
            // No selector so add a catch all new Set
            modules.put(module, new HashSet<>());
        }
        else {
            // No selector so remove everything
            modules.remove(module);
        }

        logger.info((enabled ? "Start" : "Stop") + " Ratings logging for " + spec);

        // Decide on action
        if (!modules.isEmpty() && (diagnosticFile == null)) {
            String filename = SimulationRuntime.getLogDir() + "/ratings-log.jsonl";
            logger.info("Ratings log file = " + filename);
            diagnosticFile  = new PrintWriter(filename);
        }
		else if (modules.isEmpty() && (diagnosticFile != null)) {
			diagnosticFile.close();
			diagnosticFile = null;
		}
	}

    /**
     * Log th selection of a Rating from a set of potential options. Each Rateable option
     * is scored with a Rating.
     * @param module Module used as a filter
     * @param selector Description of the entity doing the selection
     * @param selected Rateable that was selected by the requestor
     * @param options The options selected from
     */
    public static void logSelectedRating(String module, String selector,
                        Rating selected, List<? extends Rating> options) {
        Set<String> selectors = modules.get(module);

        // Active if this module has selectors and the selectors are emtpy meaning match on module
        // or selectors are not empty and the selector has to be present
        if ((selectors != null) && (selectors.isEmpty()
                            || selectors.contains(selector.toLowerCase()))) {

            StringBuilder output = new StringBuilder();
            output.append("{\"time\":\"")
                        .append(History.getMarsTime().getDateTimeStamp())
                        .append("\",\"type\":\"")
                        .append(module)
                        .append("\",\"selector\":\"")			
                        .append(selector)
                        .append("\",\"selected\":\"")
                        .append(selected.getName())
                        .append("\"");
            
            if (!options.isEmpty()) {
                // Output each output
                output.append(",\"options\":[");

                output.append(options.stream()
                    .map(entry -> "{\"rated\":\"" + entry.getName() + "\",\"rating\":"
                                + ratingToJsonLines(entry.getScore()) + "}")
                                .collect(Collectors.joining(",")));

                output.append(']');
            }
            output.append('}');

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
    private static String ratingToJsonLines(RatingScore r) {
        StringBuilder output = new StringBuilder();

        output.append("{\"score\":").append(SCORE_FORMAT.format(r.getScore()));
        output.append(",\"bases\":{");
        output.append(r.getBases().entrySet().stream()
                        .map(entry -> "\"" + entry.getKey() + "\":"
                                    + SCORE_FORMAT.format(entry.getValue()))
                        .collect(Collectors.joining(",")));
        output.append('}');

        var modifiers = r.getModifiers();     
        if (!modifiers.isEmpty()) {
            output.append(",\"modifiers\":{");
            output.append(modifiers.entrySet().stream()
                            .map(entry -> "\"" + entry.getKey() + "\":"
                                        + SCORE_FORMAT.format(entry.getValue()))
                            .collect(Collectors.joining(",")));
            output.append('}');
        };
        output.append('}');

        return output.toString();
    }
}