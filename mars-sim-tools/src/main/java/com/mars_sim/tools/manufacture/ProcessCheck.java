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
	private static final String MASS_ARG = "mass";
	private static final String CREATE_ARG = "create";
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

		// Build checkers
		SimulationConfig config = SimulationConfig.loadConfig();
		var outputPath = Path.of(line.getOptionValue(OUTPUT_ARG, "."));
		var mass = new MassCheck(config);
		var createResourceCheck = new ResourceCheck(config);

		var out = System.out;

		// Decide which checks to perform based on the provided check type argument
		if (line.hasOption(CREATE_ARG)) {
			createResourceCheck.report(out, outputPath);
		}
		else if (line.hasOption(MASS_ARG)) {
			var name = line.getOptionValue(MASS_ARG);
			if (name == null) {
				mass.reportAll(out, outputPath);
			}
			else {
				mass.reportDetailed(out, name);
			}
		}
		else {
			createResourceCheck.report(out, outputPath);
			mass.reportAll(out, outputPath);
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
		options.addOption(Option.builder(CREATE_ARG)
				.hasArg(false)
				.desc("Perform create resource check")
				.get());
		options.addOption(Option.builder(MASS_ARG)
				.optionalArg(true)
				.argName("process name")
				.desc("Perform mass check for the specified process type, or all if not specified")
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