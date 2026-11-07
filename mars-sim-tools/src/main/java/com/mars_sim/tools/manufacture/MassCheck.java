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
	private static final double MASS_EPSILON = 1e-6D;

	private record MassValue(Double massKg, String warning) { }

	private record MassBreakdown(double totalMass, List<String> lines, List<String> warnings) { }

	private record CategoryReport(ProcessCategory category, int checkedCount, List<FailedProcess> failures) { }

	private record FailedProcess(String name, MassBreakdown inputs, MassBreakdown outputs) { }

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

    public void report(PrintStream out, Path outputPath) {
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
		List<FailedProcess> failures = new ArrayList<>();

		for (var process : processes) {
			MassBreakdown inputs = calculateMass(process.getInputList(), config);
			MassBreakdown outputs = calculateMass(process.getOutputList(), config);

			if ((outputs.totalMass - inputs.totalMass) > MASS_EPSILON) {
				failures.add(new FailedProcess(process.getName(), inputs, outputs));
			}
		}

		return new CategoryReport(category, processes.size(), failures);
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

	private static String buildIssueDetails(String processName, MassBreakdown inputs, MassBreakdown outputs) {
		double delta = outputs.totalMass - inputs.totalMass;
		StringBuilder text = new StringBuilder();

		text.append(String.format(Locale.ROOT, "Process in error: %s%n", processName));
		text.append(String.format(Locale.ROOT, "Input total: %.3f kg%n", inputs.totalMass));
		text.append(String.format(Locale.ROOT, "Output total: %.3f kg%n", outputs.totalMass));
		text.append(String.format(Locale.ROOT, "Delta: %.3f kg%n", delta));
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
							writer.write(buildIssueDetails(failure.name(), failure.inputs(), failure.outputs()));
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
