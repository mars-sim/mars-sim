/*
 * Mars Simulation Project
 * ProcessCheck.java
 * @author CoPilot
 * @date 2026-09-10
 */
package com.mars_sim.tools.manufacture;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;

/**
 * Command line utility that checks configured manufacture processes for various checks.
 * Checks includes:
 * - Output mass does not exceed input mass for manufacture processes
 * - Output mass does not exceed input mass for food production processes
 * - Output mass does not exceed input mass for salvage processes
 * @see file:process-check.md for instructions on how to use this utility.
 */
public final class ProcessCheck {

	private static final String CONFIG_ARG = "configdir";
	private static final String OUTPUT_ARG = "output";
	private static final String CHECK_ARG = "check";
	private static final String HELP_ARG = "help";
	private static final String USAGE = "ProcessCheck [options]";


	private ProcessCheck() {
		// Utility class.
	}

	public static void main(String[] args) {
		Options options = createOptions();
		CommandLine line;
		try {
			line = new DefaultParser().parse(options, args);
		}
		catch (ParseException e) {
			printUsage(System.err, options, "Problem with arguments: " + e.getMessage());
			return;
		}

		if (line.hasOption(HELP_ARG)) {
			printUsage(System.out, options,
					"Checks manufacture, food production, and salvage process definitions and flags processes"
					+ " where output mass exceeds input mass. Output is grouped by process type, and detailed"
					+ " composition is written to file.");
			return;
		}

		if (line.hasOption(CONFIG_ARG)) {
			SimulationRuntime.setDataDir(line.getOptionValue(CONFIG_ARG));
		}

		var checkType = line.getOptionValue(CHECK_ARG);
		boolean doMass = false;
		boolean doResource = false;
		if (checkType == null || "all".equalsIgnoreCase(checkType)) {
			doMass = true;
			doResource = true;
		}
		else if ("mass".equalsIgnoreCase(checkType)) {
			doMass = true;
		}
		else if ("resource".equalsIgnoreCase(checkType)) {
			doResource = true;
		}
		else {
			printUsage(System.err, options, "Unknown check type: " + checkType);
			return;
		}

		SimulationConfig config = SimulationConfig.loadConfig();
		var outputPath = Path.of(line.getOptionValue(OUTPUT_ARG, "."));
		
		// Check mass balance for the specified process types
		if (doMass) {
			var mass = new MassCheck(config);
			mass.report(System.out, outputPath);
		}
		
		// Check that the various resources have a valid process
		if (doResource) {
			var createResourceCheck = new ResourceCheck(config);
			createResourceCheck.report(System.out, outputPath);
		}
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(Option.builder(HELP_ARG)
				.desc("Display help options")
				.get());
		options.addOption(Option.builder(CONFIG_ARG)
				.argName("dir")
				.hasArg()
				.desc("Alternative simulation data directory")
				.get());
		options.addOption(Option.builder(OUTPUT_ARG)
				.argName("dir")
				.hasArg()
				.desc("Directory for detailed failed-process report file")
				.get());
		options.addOption(Option.builder(CHECK_ARG)
				.argName("check")
				.hasArg()
				.desc("Check to execute: mass, resource or all (default)")
				.get());
		return options;
	}

	private static void printUsage(PrintStream out, Options options, String message) {
		HelpFormatter formatter = HelpFormatter.builder().get();
		String header = "\n" + message + "\n";

		PrintWriter writer = new PrintWriter(out, true);
		try {
			formatter.printHelp(USAGE, header, options, "", true);
			writer.flush();
		}
		catch (IOException _) {
			out.println(message);
			out.println("usage: " + USAGE);
		}
	}

}