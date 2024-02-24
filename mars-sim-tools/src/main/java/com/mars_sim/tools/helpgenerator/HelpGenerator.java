/*
 * Mars Simulation Project
 * HelpGenerator.java
 * @date 2024-02-17
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import com.mars_sim.core.resource.ItemType;

public class HelpGenerator {

	// POJO for a named value pair
	public record ValuePair(String name, double value) {}

	// Represents where a Resource is used in a manufacturing process
	record ResourceUse(List<ManufactureProcessInfo> asInput, List<ManufactureProcessInfo> asOutput) {}

	record ProcessItem(String name, String type, String typefolder, double amount) {}

	private static Logger logger = Logger.getLogger(HelpGenerator.class.getName());
	private static final String TEMPLATES_DIR = "templates/";

	/**
	 * Function that converts a string into a valid file name
	 * @return
	 */
	public Function<Object, Object> getFilename() {
		return (obj-> generateFileName((String) obj));
	}

	private String outputDir;
    private String fileSuffix;
	private DefaultMustacheFactory mf;
	private String templateDir;
	private Map<String,Mustache> templates = new HashMap<>();
	private Map<String, Object> baseScope = null;

	private Map<String, ResourceUse> resourceUses = null;
	private SimulationConfig config;


    HelpGenerator(SimulationConfig config, String outputDir, String templateSet, String fileSuffix) {
        this.outputDir = outputDir + "/";
        this.fileSuffix = "." + fileSuffix;
		this.templateDir = TEMPLATES_DIR + templateSet + "/";
		this.config = config;

		this.mf = new DefaultMustacheFactory();

		this.baseScope = new HashMap<>();
		this.baseScope.put("version", Version.getVersion());
		this.baseScope.put("generatedOn",
						DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
		
		// Locatio of other generated file; used for links
		this.baseScope.put("vehiclefolder", "../" + VehicleGenerator.TYPE_NAME + "/");
		this.baseScope.put("partfolder", "../" + PartGenerator.TYPE_NAME + "/");
		this.baseScope.put("resourcefolder", "../" + ResourceGenerator.TYPE_NAME + "/");
		this.baseScope.put("processfolder", "../" + ProcessGenerator.TYPE_NAME + "/");
		this.baseScope.put("foodfolder", "../" + FoodGenerator.TYPE_NAME + "/");

		// Add a lambda so a entity name can be converted into aa valid filename
		this.baseScope.put("filename", getFilename());
    }

	/**
	 * Generate a valid file name for a name of a entity
	 * @param name Name of config item
	 * @return
	 */
	String generateFileName(String name) {
		String simpleName = name.toLowerCase().replaceAll("\\W", "-");
		return simpleName + fileSuffix;
	}

	/**
	 * Create the default scope properties for the generation.
	 * @param pageTitle Title of page
	 * @return
	 */
	Map<String,Object> createScopeMap(String pageTitle) {
		Map<String,Object> scope = new HashMap<>(baseScope);

		// Title of the file being created
		scope.put("title", pageTitle);

		return scope;
	}

	/**
	 * Create an index page for a set of named entities. This will use the 
	 * 'entity-list' template.
	 * @param title Page title
	 * @param description Description of the page
	 * @param entities List of entities
	 * @param targetDir Target directory for the index file
	 * 
	 */
	 void createIndex(String title, String description,
			List<? extends Object> entities, String targetDir)
		throws IOException {
		var scope = createScopeMap(title);
		scope.put("description", description);
		scope.put("entities", entities);
		scope.put("typefolder", "../" + targetDir + "/");

		File outDir = new File(outputDir + targetDir);
		outDir.mkdir();

		logger.info("Generating index file for " + title);
		File indexFile = new File(outDir, generateFileName("index"));
		try (FileOutputStream dest = new FileOutputStream(indexFile)) {
			generateContent("entity-list", scope, dest);
		}
	}

	private static ResourceUse buildEmptyResourceUse() {
		return new ResourceUse(new ArrayList<>(), new ArrayList<>());
	}

	/**
	 * Get the usage of a Resoruce by it's name. This will return where is an inout or output
	 * to a process.
	 */
	ResourceUse getResourceUsageByName(String name) {
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
	 * Factor method to create a ProcessItem from a set of attributes
	 * @param name
	 * @param type
	 * @param amount
	 * @return
	 */
	static ProcessItem toProcessItem(String name, ItemType type, double amount) {
		String typeFolder = switch(type) {
			case AMOUNT_RESOURCE -> ResourceGenerator.TYPE_NAME;
			case BIN -> null;
			case EQUIPMENT -> null;
			case PART -> PartGenerator.TYPE_NAME;
			case VEHICLE -> VehicleGenerator.TYPE_NAME;
		};
		return new ProcessItem(name, type.getName(), "../" + typeFolder + "/", amount);
	}

	/**
	 * Generate all configurations.
	 * @param config
	 * @throws IOException
	 */
	private void generateAll() throws IOException {
		VehicleGenerator vg = new VehicleGenerator(this);
		vg.generateAll();

		var pg = new ProcessGenerator(this);
		pg.generateAll();

		var ptg = new PartGenerator(this);
		ptg.generateAll();

		var rg = new ResourceGenerator(this);
		rg.generateAll();

		var fg = new FoodGenerator(this);
		fg.generateAll();
	}

    public SimulationConfig getConfig() {
        return config;
    }

	/**
	 * Look up a template. This will select the appropriate Mustache templaet for the defined
	 * template set.
	 * 
	 * @param template
	 * @return
	 */
	Mustache getTemplate(String template) {
		Mustache m = templates.get(template);
		if (m == null) {
			m = mf.compile(templateDir + template + ".mustache");
			templates.put(template, m);
		}

		 return m;
	}

    public String getOutputDir() {
		return outputDir;
    }

	/**
	 * Creates a final output by applying a template to a scope.

	 * @param templateName Name of the template to apply. Expanded into file by usign template set.
	 * @param scope Scope of properties to apply
	 * @param output Destination for generated content
	 * @throws IOException
	 */
	void generateContent(String templateName, Map<String, Object> scope, OutputStream output)
				throws IOException {
		var template = getTemplate(templateName);
		var writer = new OutputStreamWriter(output);
        template.execute(writer, scope);
        writer.flush();
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
		var gen = new HelpGenerator(config, "target/help-files", "html-help", "html");
		try {
			gen.generateAll();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Problem generating files", e);
		}
	}
}
