/*
 * Mars Simulation Project
 * HelpRunner.java
 * @date 2024-02-17
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.help.HelpFormatter; // non-deprecated formatter (CLI 1.10+)
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;

/**
 * This class is an class to provide an executable app that can generate files.
 */
public class HelpRunner {

    private static final String STYLE_ARG = "style";
    private static final String OUTPUT_ARG = "output";
    private static final String SCOPE_ARG = "scope";
    private static final String ALL_SCOPE = "all";

    // Note this is sync'ed swith SimualtionBuilder
    private static final String CONFIG_ARG = "configdir";

    private static final Logger logger = Logger.getLogger(HelpRunner.class.getName());

    /**
     * The main starting method for generating html files.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Setup commands
        Options options = new Options();

        options.addOption(CONFIG_ARG, true, "Directory for configurations");
        options.addOption(STYLE_ARG, true, "Defines the style of the output; default to HTML");
        options.addOption(OUTPUT_ARG, true, "Directory for generated files");
        options.addOption(SCOPE_ARG, true, "List of types to generate; defaults to 'all'");

        CommandLineParser commandline = new DefaultParser();
        CommandLine line;
        try {
            line = commandline.parse(options, args);
        }
        catch (ParseException pe) {
            // Use the new HelpFormatter API: printHelp(cmdLineSyntax, header, options, footer, autoUsage)
            HelpFormatter format = HelpFormatter.builder().get();
            String header = "Problem with commands: " + pe.getMessage();
            String footer = "";
            format.printHelp("helpgenerator", header, options, footer, true);
            return;
        }

        // Get details
        if (line.hasOption(CONFIG_ARG)) {
            SimulationRuntime.setDataDir(line.getOptionValue(CONFIG_ARG));
        }
        String outputDir = line.getOptionValue(OUTPUT_ARG, "'");
        String style = line.getOptionValue(STYLE_ARG, HelpContext.HTML_STYLE);
        String scope = line.getOptionValue(SCOPE_ARG, "all");

        // Build context and generate files
        var config = SimulationConfig.loadConfig();
        var context = new HelpContext(config, style);
        try {
            File output = new File(outputDir);

            // Generate everything or just a subset
            if (ALL_SCOPE.equals(scope)) {
                context.generateAll(output);
            }
            else {
                String[] scopes = scope.split(",");
                for (var s : scopes) {
                    var typeGen = context.getGenerator(s);
                    typeGen.generateAll(output);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Problem generating files", e);
        }
    }
}
