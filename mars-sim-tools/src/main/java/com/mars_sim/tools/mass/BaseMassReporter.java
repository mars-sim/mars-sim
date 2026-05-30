/*
 * Mars Simulation Project
 * BaseMassReporter.java
 * @date 2026-05-30
 * @author Barry Evans
 */
package com.mars_sim.tools.mass;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.robot.RobotSpec;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.vehicle.VehicleSpec;

/**
 * Command line utility to compare configured and manufacture-derived base mass values
 * for Robot and Vehicle specs.
 */
public final class BaseMassReporter {

	private static final String CONFIG_ARG = "configdir";
	private static final String HELP_ARG = "help";
	private static final String USAGE = "BaseMassReporter [options]";

	private BaseMassReporter() {
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
			printUsage(System.out, options, "Compares configured robot and vehicle base masses with manufacture process input mass.");
			return;
		}

		if (line.hasOption(CONFIG_ARG)) {
			SimulationRuntime.setDataDir(line.getOptionValue(CONFIG_ARG));
		}

		SimulationConfig config = SimulationConfig.loadConfig();
		report(config, System.out);
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
		return options;
	}

	static void report(SimulationConfig config, PrintStream out) {
		var processes = config.getManufactureConfiguration().getManufactureProcessList();

		out.println("Vehicle spec mass comparison");
		out.println("Spec Name | Process | Defined Mass (kg) | Calculated Mass (kg) | Delta (kg)");
		config.getVehicleConfiguration().getVehicleSpecs().stream()
				.sorted(Comparator.comparing(VehicleSpec::getName))
				.forEach(spec -> printVehicle(spec, processes, out));

		out.println();
		out.println("Robot spec mass comparison");
		out.println("Spec Name | Process | Defined Mass (kg) | Calculated Mass (kg) | Delta (kg)");
		config.getRobotConfiguration().getRobotSpecs().stream()
				.sorted(Comparator.comparing(RobotSpec::getName))
				.forEach(spec -> printRobot(spec, processes, out));
	}

	private static void printVehicle(VehicleSpec spec, List<ManufactureProcessInfo> processes,
			PrintStream out) {
		Optional<ManufactureProcessInfo> process = findProcessByOutputName(processes, ItemType.VEHICLE, spec.getName());
		String processName = process.map(ManufactureProcessInfo::getName).orElse("not found");
		Double calculated = process.map(ManufactureProcessInfo::calculateTotalInputMass).orElse(null);
		printResult(spec.getName(), processName, spec.getEmptyMass(), calculated, out);
	}

	private static void printRobot(RobotSpec spec, List<ManufactureProcessInfo> processes, PrintStream out) {
		Optional<ManufactureProcessInfo> process = findProcessByOutputName(processes, ItemType.ROBOT,
				spec.getName());
		String processName = process.map(ManufactureProcessInfo::getName).orElse("not found");
		Double calculated = process.map(ManufactureProcessInfo::calculateTotalInputMass).orElse(null);
		printResult(spec.getName(), processName, spec.getMass(), calculated, out);
	}

	private static Optional<ManufactureProcessInfo> findProcessByOutputName(
			List<ManufactureProcessInfo> processes, ItemType type, String outputName) {
		return processes.stream()
				.filter(process -> process.getOutputList().stream()
						.anyMatch(item -> (item.getType() == type) && item.getName().equalsIgnoreCase(outputName)))
				.findFirst();
	}

	private static void printResult(String specName, String processName, double definedMass, Double calculated,
			PrintStream out) {
		String calculatedValue = (calculated == null ? "N/A" : String.format(Locale.ROOT, "%.2f", calculated));
		String deltaValue = (calculated == null ? "N/A"
				: String.format(Locale.ROOT, "%.2f", (definedMass - calculated.doubleValue())));
		out.printf(Locale.ROOT, "%s | %s | %.2f | %s | %s%n",
				specName, processName, definedMass, calculatedValue, deltaValue);
	}

	private static void printUsage(PrintStream out, Options options, String message) {
		HelpFormatter formatter = HelpFormatter.builder().get();
		String header = "\n" + message + "\n";

		PrintWriter writer = new PrintWriter(out, true);
		try {
			formatter.printHelp(USAGE, header, options, "", true);
			writer.flush();
		}
		catch (IOException e) {
			out.println(message);
			out.println("usage: " + USAGE);
		}
	}
}
