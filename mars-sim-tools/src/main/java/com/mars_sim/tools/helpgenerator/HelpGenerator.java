/*
 * Mars Simulation Project
 * HelpGenerator.java
 * @date 2024-02-17
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Version;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

public class HelpGenerator {

	// POJO for a named value pair
	public record ValuePair(String name, double value) {}

	// Represents where a Resource is used in a manufacturing process
	private record ResourceUse(List<ManufactureProcessInfo> asInput, List<ManufactureProcessInfo> asOutput) {}

	private static Logger logger = Logger.getLogger(HelpGenerator.class.getName());
	private static final String TEMPLATES = "templates/";
	private static final String VEHICLE_DIR = "vehicles/";
	private static final String RESOURCE_DIR = "resources/";
	private static final String PROCESS_DIR = "processes/";
	private static final String PART_DIR = "parts/";

	/**
	 * Function that converts a string into a valid file name
	 * @return
	 */
	public Function<Object, Object> getFilename() {
		return (obj-> generateFileName((String) obj));
	}

	private String outputDir;
    private String fileSuffix;
	private Object dateTimeString;
	private DefaultMustacheFactory mf;
	private String templateDir;
	private Mustache indexTemplate;

	private Map<String, ResourceUse> resourceUses = null;


    private HelpGenerator(String outputDir, String templateSet, String fileSuffix) {
        this.outputDir = outputDir + "/";
        this.fileSuffix = "." + fileSuffix;
		this.templateDir = TEMPLATES + templateSet + "/";

		this.dateTimeString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());

		this.mf = new DefaultMustacheFactory();
		this.indexTemplate = mf.compile(templateDir + "entity-list.mustache");

    }

	/**
	 * Generate a valid file name for a name of a entity
	 * @param name Name of config item
	 * @return
	 */
	private String generateFileName(String name) {
		String simpleName = name.toLowerCase().replace(" ", "-");
		return simpleName + fileSuffix;
	}

	/**
	 * Create the default scope properties for the generation.
	 * @param pageTitle Title of page
	 * @return
	 */
	private Map<String,Object> createScopeMap(String pageTitle) {
		Map<String,Object> scope = new HashMap<>();

		// Metadata
		scope.put("version", Version.getVersion());
		scope.put("generatedOn", dateTimeString);

		// Locatino of other generated file; used for links
		scope.put("vehiclefolder", "../" + VEHICLE_DIR);
		scope.put("partfolder", "../" + PART_DIR);
		scope.put("resourcefolder", "../" + RESOURCE_DIR);
		scope.put("processfolder", "../" + PROCESS_DIR);


		// Add a lambda so a entity name can be converted into aa valid filename
		scope.put("filename", getFilename());

		// Title of the file being created
		scope.put("title", pageTitle);

		return scope;
	}
    
    /**
	 * The main starting method for generating html files.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// Load config files
		var config = SimulationConfig.instance();
		config.loadConfig();

		// This will be expaned to support other template sets
		var gen = new HelpGenerator("target/help-files", "html-help", "html");
		try {
			gen.generateAll(config);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Problem generating files", e);
		}
	}

	/**
	 * Generate all configurations.
	 * @param config
	 * @throws IOException
	 */
	private void generateAll(SimulationConfig config) throws IOException {
		generateVehicles(config);
		generateParts(config);
		generateResources(config);
	}

	private static ResourceUse buildEmptyResourceUse() {
		return new ResourceUse(new ArrayList<>(), new ArrayList<>());
	}

	/**
	 * Get the usage of a Resoruce by it's name. This will return where is an inout or output
	 * to a process.
	 * @param config
	 */
	private ResourceUse getResourceUsageByName(SimulationConfig config, String name) {
		if (resourceUses == null) {
			resourceUses = new HashMap<>();
			for (var m: config.getManufactureConfiguration().getManufactureProcessList()) {
				for (var r: m.getInputNames()) {
					resourceUses.computeIfAbsent(r.toLowerCase(), k-> buildEmptyResourceUse()).asInput().add(m);
				}
				for (var r: m.getOutputNames()) {
					resourceUses.computeIfAbsent(r.toLowerCase(), k-> buildEmptyResourceUse()).asOutput().add(m);
				}
			}
		}

		return resourceUses.get(name.toLowerCase());
	}
	
	/**
	 * This populates the scope to support generating the partial process-flow template.
	 * @param config
	 * @param name
	 * @param pScope
	 */
	private void addProcessFlows(SimulationConfig config, String name, Map<String, Object> pScope) {
		var resourceUsed = getResourceUsageByName(config, name);

		if (resourceUsed != null) {
			pScope.put("inputProcesses", resourceUsed.asInput());
			pScope.put("hasInputProcesses", !resourceUsed.asInput.isEmpty());
			pScope.put("outputProcesses", resourceUsed.asInput());
			pScope.put("hasOutputProcesses", !resourceUsed.asInput.isEmpty());
		}
	}

	/**
	 * Generate the files for the Parts
	 * @throws IOException
	 */
	private void generateResources(SimulationConfig config) throws IOException {

		String resourceDir = outputDir + RESOURCE_DIR;
		new File(resourceDir).mkdirs();

		var rTypes = ResourceUtil.getAmountResources().stream()
		 							.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
									 .toList();
	
		// Create vehicle index
		createIndex("Resources",
					"Resources that can be stored and used for manufacturing and cooking.",
					rTypes, RESOURCE_DIR);

		// Individual Part pages
    	Mustache detailTemplate = mf.compile(templateDir + "resource-detail.mustache");
		logger.info("Generating details file for Resources : " + rTypes.size());
		for(var p : rTypes) {
			var scope = createScopeMap(p.getName());
			scope.put("resource", p);
			addProcessFlows(config, p.getName(), scope);

			// Generate the file
			try(var detailFile = new FileWriter(resourceDir + generateFileName(p.getName()));) {
				detailTemplate.execute(detailFile, scope);
			}
		}
	}

	/**
	 * Generate the files for the Parts
	 * @throws IOException
	 */
	private void generateParts(SimulationConfig config) throws IOException {

		String partsDir = outputDir + PART_DIR;
		new File(partsDir).mkdirs();

		var pTypes = ItemResourceUtil.getItemResources().stream()
		 							.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
									 .toList();
	
		// Create vehicle index
		createIndex("Parts", "Parts used for repairs and processes",
					pTypes, PART_DIR);

		// Individual Part pages
    	Mustache detailTemplate = mf.compile(templateDir + "part-detail.mustache");
		logger.info("Generating details file for " + pTypes.size() + " Parts");
		for(var p : pTypes) {
			var pScope = createScopeMap(p.getName());
			pScope.put("part", p);
			addProcessFlows(config, p.getName(), pScope);

			// Generate the file
			try(var detailFile = new FileWriter(partsDir + generateFileName(p.getName()));) {
				detailTemplate.execute(detailFile, pScope);
			}
		}
	}

	/**
	 * Generate the files for the Vehicle specifications.
	 * @param config Source configuration
	 * @throws IOException
	 */
	private void generateVehicles(SimulationConfig config) throws IOException {

		String vehicleDir = outputDir + VEHICLE_DIR;
		new File(vehicleDir).mkdirs();
		
		var vTypes = config.getVehicleConfiguration().getVehicleSpecs().stream()
		 							.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
									 .toList();
	
		// Create vehicle index
		createIndex("Vehicle Specs", "Types of Vehicles Featured for Mars Surface Operations",
					vTypes, VEHICLE_DIR);

		// Individual vehicle pages
    	Mustache detailTemplate = mf.compile(templateDir + "vehicle-detail.mustache");
		logger.info("Generating details file for " + vTypes.size() + " Vehicles");
		for(var v : vTypes) {
			var vScope = createScopeMap(v.getName());
			vScope.put("vehicle", v);

			// Convert capacity to a list that contains the resource name
			var cargos = v.getCargoCapacityMap().entrySet().stream()
								.map(e -> new ValuePair(ResourceUtil.findAmountResourceName(e.getKey()),
										e.getValue()))
								.toList();
			vScope.put("cargo", cargos);

			// Generate the file
			try(var detailFile = new FileWriter(vehicleDir + generateFileName(v.getName()));) {
				detailTemplate.execute(detailFile, vScope);
			}
		}
	}

	private void createIndex(String title, String description,
					List<? extends Object> entities, String targetDir)
				throws IOException {
		var scope = createScopeMap(title);
		scope.put("description", description);
		scope.put("entities", entities);
		scope.put("typefolder", "../" + targetDir);

		File outDir = new File(outputDir + targetDir);
		outDir.mkdir();
		File indexFile = new File(outDir, "index" + fileSuffix);

		logger.info("Generating index file for " + title);
		try(var listFile = new FileWriter(indexFile);) {
    		indexTemplate.execute(listFile, scope);
		}
	}
}
