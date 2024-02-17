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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Version;
import com.mars_sim.core.resource.ResourceUtil;

public class HelpGenerator {

	// POJO for a named value pair
	public record ValuePair(String name, double value) {}

	private static Logger logger = Logger.getLogger(HelpGenerator.class.getName());
	private static final String TEMPLATES = "templates/";
	private static final String VEHICLE_DIR = "vehicles/";
	private static final String RESOURCE_DIR = "resources/";
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

    private HelpGenerator(String outputDir, String templateSet, String fileSuffix) {
        this.outputDir = outputDir + "/";
        this.fileSuffix = "." + fileSuffix;
		this.templateDir = TEMPLATES + templateSet + "/";

		this.dateTimeString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());

		this.mf = new DefaultMustacheFactory();
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
		Map<String,Object> scope = createScopeMap("Vehicle List");
		scope.put("vehicles", vTypes);
	
		// Create vehicle index
		createIndex(scope, "vehicle-list", vehicleDir);

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

	private void createIndex(Map<String, Object> scope, String fileSeed, String targetDir)
				throws IOException {
    	Mustache indexTemplate = mf.compile(templateDir + fileSeed + ".mustache");
		logger.info("Generating index file for " + fileSeed);
		try(var listFile = new FileWriter(targetDir + fileSeed + fileSuffix);) {
    		indexTemplate.execute(listFile, scope);
		}
	}
}
