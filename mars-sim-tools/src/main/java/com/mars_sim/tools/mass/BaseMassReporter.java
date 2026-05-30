/*
 * Mars Simulation Project
 * BaseMassReporter.java
 * @date 2026-05-30
 * @author Barry Evans
 */
package com.mars_sim.tools.mass;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.robot.RobotSpec;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.vehicle.VehicleSpec;

/**
 * Command line utility to compare configured and manufacture-derived base mass values
 * for Robot and Vehicle specs.
 */
public final class BaseMassReporter {

	private static final String CONFIG_ARG = "--configdir";
	private static final String HELP_ARG = "--help";

	private BaseMassReporter() {
		// Utility class.
	}

	public static void main(String[] args) {
		if (args.length == 1 && HELP_ARG.equalsIgnoreCase(args[0])) {
			printUsage(System.out);
			return;
		}

		for (int i = 0; i < args.length; i++) {
			if (CONFIG_ARG.equalsIgnoreCase(args[i]) && (i + 1 < args.length)) {
				SimulationRuntime.setDataDir(args[i + 1]);
				i++;
			}
		}

		SimulationConfig config = SimulationConfig.loadConfig();
		report(config, System.out);
	}

	static void report(SimulationConfig config, PrintStream out) {
		var processes = config.getManufactureConfiguration().getManufactureProcessList().stream()
				.collect(Collectors.toMap(p -> p.getName().toLowerCase(Locale.ROOT), Function.identity(),
						(a, b) -> a));

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

	private static void printVehicle(VehicleSpec spec, Map<String, ManufactureProcessInfo> processes,
			PrintStream out) {
		String processName = "Assemble " + spec.getType().name().replace("_", " ");
		Double calculated = findCalculatedMass(processes, processName);
		printResult(spec.getName(), processName, spec.getEmptyMass(), calculated, out);
	}

	private static void printRobot(RobotSpec spec, Map<String, ManufactureProcessInfo> processes, PrintStream out) {
		String processName = "Assemble a " + formatRobotType(spec.getRobotType());
		Double calculated = findCalculatedMass(processes, processName);
		printResult(spec.getName(), processName, spec.getMass(), calculated, out);
	}

	private static Double findCalculatedMass(Map<String, ManufactureProcessInfo> processes, String processName) {
		ManufactureProcessInfo process = processes.get(processName.toLowerCase(Locale.ROOT));
		if (process == null) {
			return null;
		}
		return process.calculateTotalInputMass();
	}

	private static String formatRobotType(RobotType type) {
		String raw = type.name().toLowerCase(Locale.ROOT);
		return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
	}

	private static void printResult(String specName, String processName, double definedMass, Double calculated,
			PrintStream out) {
		String calculatedValue = (calculated == null ? "N/A" : String.format(Locale.ROOT, "%.2f", calculated));
		String deltaValue = (calculated == null ? "N/A"
				: String.format(Locale.ROOT, "%.2f", (definedMass - calculated.doubleValue())));
		out.printf(Locale.ROOT, "%s | %s | %.2f | %s | %s%n",
				specName, processName, definedMass, calculatedValue, deltaValue);
	}

	private static void printUsage(PrintStream out) {
		out.println("Usage: BaseMassReporter [--configdir <dir>]");
		out.println("Compares configured robot and vehicle base masses with manufacture process input mass.");
	}
}
