/*
 * Mars Simulation Project
 * MassCheck.java
 * @author CoPilot
 * @date 2026-09-10
 */
package com.mars_sim.tools.manufacture;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.equipment.BinFactory;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.robot.RobotSpec;
import com.mars_sim.core.vehicle.VehicleSpec;

/**
 * This class checks configured manufacture processes for output mass greater than input mass.
 */
public class MassCheck {

    private static final String DETAILS_FILENAME = "process-check-details.txt";

	private record MassValue(Double massKg, String warning) { }

	private record MassBreakdown(double totalMass, List<String> lines, List<String> warnings) { }

	private record CategoryReport(ProcessCategory category, int checkedCount, List<ProcessBreakdown> failures) { }

	private record ProcessBreakdown(String name, MassBreakdown inputs, MassBreakdown outputs, double ratio) {}

	private static class ProcessCategory {

		private final String displayName;
		private final String summaryName;
		private final Function<SimulationConfig, List<? extends ProcessInfo>> loader;

		ProcessCategory(String displayName, String summaryName,
				Function<SimulationConfig, List<? extends ProcessInfo>> loader) {
			this.displayName = displayName;
			this.summaryName = summaryName;
			this.loader = loader;
		}

		private List<? extends ProcessInfo> getProcesses(SimulationConfig config) {
			return loader.apply(config);
		}

		private String displayName() {
			return displayName;
		}

		private String summaryName() {
			return summaryName;
		}
	}

    private static final ProcessCategory MANUFACTURE = new ProcessCategory("Manufacture Processes", "manufacture",
            c -> c.getManufactureConfiguration().getManufactureProcessList());
    private static final ProcessCategory FOOD = new ProcessCategory("Food Production Processes", "food production",
            c -> c.getFoodProductionConfiguration().getProcessList());
    private static final ProcessCategory SALVAGE = new ProcessCategory("Salvage Processes", "salvage",
            c -> c.getManufactureConfiguration().getSalvageInfoList());


    private final SimulationConfig config;

    public MassCheck(SimulationConfig config) {
        this.config = config;
    }

	/**
	 * Report detailed mass check information for a specific process.
	 * @param out Destination of report
	 * @param processName Name of the process for which to report detailed mass check information
	 */
	public void reportDetailed(PrintStream out, String processName) {
		List<ProcessCategory> categories = List.of(MANUFACTURE, FOOD, SALVAGE);


		var foundProcess = categories.stream()
				.flatMap(category -> category.getProcesses(config).stream())
				.filter(p -> p.getName().equals(processName))
				.findFirst()
				.orElse(null);
		if (foundProcess == null) {
			out.println("Process not found: " + processName);
			return;
		}

		var processReport = createProcessBreakdown(foundProcess, config);

		out.println(toString(processReport));
	}

	/**
	 * Report mass check results for all process categories. Summaries are printed to the provided PrintStream, and detailed information is written to a file in the specified output path.
	 * @param out the PrintStream to which summary information will be printed
	 * @param outputPath the path where the detailed report file will be written
	 */
    public void reportAll(PrintStream out, Path outputPath) {
	    List<ProcessCategory> categories = List.of(MANUFACTURE, FOOD, SALVAGE);
		
        List<CategoryReport> reports = categories.stream()
				.map(category -> scanCategory(config, category))
				.toList();

		for (CategoryReport report : reports) {
			printCategoryReport(out, report);
		}

		Path detailsFile = writeDetailsFile(outputPath, reports);
		int totalProcesses = 0;
		int totalFailures = 0;
		for (CategoryReport report : reports) {
			totalProcesses += report.checkedCount();
			totalFailures += report.failures().size();
		}
		String typeLabel = (categories.size() == 1) ? "process type" : "process types";

		out.printf(Locale.ROOT,
				"Checked %d processes across %d %s. Found %d failures. Details file: %s%n",
				totalProcesses, categories.size(), typeLabel, totalFailures, detailsFile.toAbsolutePath().normalize());
	}

	private CategoryReport scanCategory(SimulationConfig config, ProcessCategory category) {
		var processes = category.getProcesses(config).stream()
				.sorted(Comparator.comparing(p -> String.valueOf(p.getName()), String.CASE_INSENSITIVE_ORDER))
				.toList();

		List<ProcessBreakdown> failures = processes.stream()
				.map(p -> createProcessBreakdown(p, config))
				.filter(b -> 
					// Check if input mass is more than 20% greater than output mass or if output mass exceeds input mass
					(b.ratio > 102D) || (b.ratio < 100D)
				)
				.toList();

		return new CategoryReport(category, processes.size(), failures);
	}

	private static ProcessBreakdown createProcessBreakdown(ProcessInfo process, SimulationConfig config) {
		MassBreakdown inputs = calculateMass(process.getInputList(), config);
		MassBreakdown outputs = calculateMass(process.getOutputList(), config);

		var ratio = 0D;
		if (outputs.totalMass() > 0D) {
			ratio = (inputs.totalMass() * 100D) / outputs.totalMass();
			ratio = Math.round(ratio * 1_00D) / 1_00D;
		}

		return new ProcessBreakdown(process.getName(), inputs, outputs, ratio);
	}

	private static void printCategoryReport(PrintStream out, CategoryReport report) {
		out.printf(Locale.ROOT, "%n=== %s ===%n", report.category().displayName());
		out.printf(Locale.ROOT,
				"Checked %d %s processes. Found %d failures.%n",
				report.checkedCount(), report.category().summaryName(), report.failures().size());

		if (report.failures().isEmpty()) {
			out.println("No failed processes found.");
			return;
		}

		report.failures().forEach(f -> out.printf(Locale.ROOT,
				"FAIL | %s | input=%.3f kg | output=%.3f kg%n",
				f.name(), f.inputs().totalMass, f.outputs().totalMass));
	}

	private static String toString(ProcessBreakdown processDetails) {
		String processName = processDetails.name();
		MassBreakdown inputs = processDetails.inputs();
		MassBreakdown outputs = processDetails.outputs();
		double delta = inputs.totalMass - outputs.totalMass;
		double ratio = processDetails.ratio();
		StringBuilder text = new StringBuilder();

		text.append(String.format(Locale.ROOT, "Process: %s%n", processName));
		text.append(String.format(Locale.ROOT, "Input total: %.3f kg%n", inputs.totalMass));
		text.append(String.format(Locale.ROOT, "Output total: %.3f kg%n", outputs.totalMass));
		text.append(String.format(Locale.ROOT, "Input2Output: %.3f kg (%.2f%%)%n", delta, ratio));
		text.append("Input composition:\n");
		inputs.lines.forEach(line -> text.append("  ").append(line).append(System.lineSeparator()));
		text.append("Output composition:\n");
		outputs.lines.forEach(line -> text.append("  ").append(line).append(System.lineSeparator()));

		inputs.warnings.forEach(w -> text.append("  warning: ").append(w).append(System.lineSeparator()));
		outputs.warnings.forEach(w -> text.append("  warning: ").append(w).append(System.lineSeparator()));
		text.append(System.lineSeparator());

		return text.toString();
	}

	private static Path writeDetailsFile(Path outputDir, List<CategoryReport> reports) {
		try {
			Files.createDirectories(outputDir);
			Path detailsFile = outputDir.resolve(DETAILS_FILENAME);
			try (BufferedWriter writer = Files.newBufferedWriter(detailsFile,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
				for (int index = 0; index < reports.size(); index++) {
					CategoryReport report = reports.get(index);
					writer.write("=== " + report.category().displayName() + " ===");
					writer.newLine();
					writer.write(String.format(Locale.ROOT,
							"Checked %d %s processes. Found %d failures.",
							report.checkedCount(), report.category().summaryName(), report.failures().size()));
					writer.newLine();

					if (report.failures().isEmpty()) {
						writer.write("No failed processes found.");
						writer.newLine();
					}
					else {
						for (var failure : report.failures()) {
							writer.write(toString(failure));
						}
					}

					if (index < (reports.size() - 1)) {
						writer.newLine();
					}
				}
			}
			return detailsFile;
		}
		catch (IOException ex) {
			throw new IllegalStateException("Cannot write details report file in directory: " + outputDir, ex);
		}
	}

	private static MassBreakdown calculateMass(List<ProcessItem> items, SimulationConfig config) {
		double totalMass = 0D;
		List<String> lines = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		for (var item : items) {
			// Consumerable items are ignored for mass calculation
			if (!item.isConsumable()) {
				MassValue massValue = massForItem(item, config);
				if (massValue.massKg != null) {
					totalMass += massValue.massKg.doubleValue();
					lines.add(String.format(Locale.ROOT, "%s | amount=%.3f | mass=%.3f kg",
							buildItemLabel(item), item.getAmount(), massValue.massKg.doubleValue()));
				}
				else {
					lines.add(String.format(Locale.ROOT, "%s | amount=%.3f | mass=unknown",
							buildItemLabel(item), item.getAmount()));
				}
				
				if (massValue.warning != null) {
					warnings.add(massValue.warning);
				}
			}
		}
		
		// The mass has to be rounded to a reasonable precision for reporting
		return new MassBreakdown(totalMass, lines, warnings);
	}
    
	private static String buildItemLabel(ProcessItem item) {
		return item.getType() + ": " + item.getName();
	}

    private static MassValue massForItem(ProcessItem item, SimulationConfig config) {
		double amount = item.getAmount();
		ItemType type = item.getType();

		try {
			switch (type) {
				case AMOUNT_RESOURCE:
					return new MassValue(amount, null);

				case PART:
					ItemResource part = ItemResourceUtil.findItemResource(item.getId());
					if (part == null) {
						return new MassValue(null, "Part not found in config: " + item.getName());
					}
					return new MassValue(amount * part.getMassPerItem(), null);

				case EQUIPMENT:
					return new MassValue(amount * EquipmentFactory.getUsualMass(EquipmentType.convertName2Enum(item.getName())),
							null);

				case BIN:
					return new MassValue(amount * BinFactory.getUsualMass(BinType.convertName2Enum(item.getName())), null);

				case VEHICLE:
					VehicleSpec vehicleSpec = config.getVehicleConfiguration().getVehicleSpec(item.getName());
					if (vehicleSpec == null) {
						return new MassValue(null, "Vehicle spec not found: " + item.getName());
					}
					return new MassValue(amount * vehicleSpec.getEmptyMass(), null);

				case ROBOT:
					RobotSpec robotSpec = config.getRobotConfiguration().getRobotSpec(item.getName());
					if (robotSpec == null) {
						return new MassValue(null, "Robot spec not found: " + item.getName());
					}
					return new MassValue(amount * robotSpec.getMass(), null);

				default:
					return new MassValue(null, "Unsupported item type: " + item.getType());
			}
		}
		catch (RuntimeException ex) {
			return new MassValue(null,
					"Failed to calculate mass for " + buildItemLabel(item) + " because: " + ex.getMessage());
		}
	}
}
