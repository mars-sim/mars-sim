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
import java.util.Properties;
import java.util.function.Function;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.resource.ItemType;

/**
 * This is the context of a session to generate a set of help files is a specific format
 */
public class HelpContext {
	// This is the style associated with the html based help
    public static final String HTML_STYLE = "html-help";

	// Name of the index file for lists of entities
	private static final String INDEX = "index";

	// POJO for a named value pair
	public record ValuePair(String name, double value) {}

	// Represents where a Resource is used in a process
	record ResourceUse(List<ProcessInfo> asInput, List<ProcessInfo> asOutput) {}

	// A quantity of an item 
	record ItemQuantity(String name, String type, String typefolder, String amount) {}

	// This is the standard Title property used for page title
	static final String TITLE_ATTR = "title";
	private static final String TEMPLATES_DIR = "templates/";
	private static final String TITLE_PREFIX = " Configurations";
	private static final String FILE_SUFFIX_PROP = "file_suffix";
	private static final String TOP_PROP = "generate_top";
	private static final String PROPS_FILE = "template.properties";

	// All generators are declared here
	static final String[] GENERATORS = {BuildingGenerator.TYPE_NAME,
												ComplaintGenerator.TYPE_NAME,
												CrewGenerator.TYPE_NAME,
												FoodGenerator.TYPE_NAME,
												PartGenerator.TYPE_NAME,
												ProcessGenerator.TYPE_NAME,
												ResourceGenerator.TYPE_NAME,
												ManifestGenerator.TYPE_NAME,
												ScenarioGenerator.TYPE_NAME,
												SettlementGenerator.TYPE_NAME,
												TreatmentGenerator.TYPE_NAME,
												VehicleGenerator.TYPE_NAME};

	/**
	 * Function that converts a string into a valid file name for an HTML link.
	 * This is called by the Mustache templates
	 * 
	 * @return
	 */
	public Function<Object, Object> getFilename() {
		return (obj-> generateFileName((String) obj, ".html"));
	}
	
	// Loaded from the properties
	private String fileSuffix;
	private boolean generateTopLevel;

	private DefaultMustacheFactory mf;
	private String templateDir;
	private Map<String, Object> baseScope = null;

	private Mustache indexTemplate;
	private Mustache groupedTemplate;
	private Map<String, ResourceUse> resourceUses = null;
	private SimulationConfig config;


    HelpContext(SimulationConfig config, String templateSet) {
		this.templateDir = TEMPLATES_DIR + templateSet + "/";

		// Load the proeprties of this template
		Properties templateProps = new Properties();
		try(var propStream = HelpContext.class.getResourceAsStream("/" + templateDir + PROPS_FILE)) {
			templateProps.load(propStream);
		} catch (IOException e) {
			throw new IllegalArgumentException("Problem loading template props", e);
		}
		this.fileSuffix = "." + templateProps.get(FILE_SUFFIX_PROP);
		this.generateTopLevel = Boolean.parseBoolean(templateProps.getProperty(TOP_PROP, "true"));

		this.config = config;

		this.mf = new DefaultMustacheFactory();

		this.baseScope = new HashMap<>();
		this.baseScope.put("version", SimulationRuntime.VERSION.getVersionTag());
		this.baseScope.put("generatedOn",
						DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
		
		// Location of other generated file; used for links
		for(var f : GENERATORS) {
			this.baseScope.put(f + "folder", "../" + f + "/");

		}
		
		// Add a lambda so a entity name can be converted into aa valid filename
		this.baseScope.put("filename", getFilename());
    }

	/**
	 * Generates a valid file name for a name of a entity.
	 * 
	 * @param name Name of config item
	 * @return
	 */
	String generateFileName(String name) {
		return generateFileName(name, fileSuffix);
	}

	private static String generateFileName(String name, String suffix) {
		String simpleName = name.toLowerCase().replaceAll("\\W", "-");
		return simpleName + suffix;
	}

	/**
	 * Creates the default scope properties for the generation.
	 * 
	 * @param pageTitle Title of page
	 * @return
	 */
	Map<String,Object> createScopeMap(String pageTitle) {
		Map<String,Object> scope = new HashMap<>(baseScope);

		// Title of the file being created
		if (pageTitle != null) {
			scope.put(TITLE_ATTR, pageTitle);
		}

		return scope;
	}

	/**
	 * Creates an index page for a set of named entities. This will use the 
	 * 'entity-list' template.
	 * 
	 * @param title Page title
	 * @param description Description of the page
	 * @param entities List of entities
	 * @param typeFolder Folder for this type
	 * @param outputDir Target root folder for the file
	 * 
	 */
	 void createFlatIndex(String title, String description,
			List<? extends Object> entities, String typeFolder, File outputDir) 
		throws IOException {
		var scope = createScopeMap(title + TITLE_PREFIX);
		scope.put("listtitle", title);
		scope.put("description", description);
		scope.put("entities", entities);
		scope.put("typefolder", "../" + typeFolder + "/");

		// Load the template
		if (indexTemplate == null) {
			indexTemplate = getTemplate("entity-list");
		}

		File indexFile = new File(outputDir, generateFileName(INDEX));
		try (FileOutputStream dest = new FileOutputStream(indexFile)) {
			generateContent(indexTemplate, scope, dest);
		}
	}

	/**
	 * Creates an index page for a set of named entities that are grouped. This will use the 
	 * 'entity-grouped' template.
	 * 
	 * @param title Page title
	 * @param description Description of the page
	 * @param groupName Name of the groups
	 * @param groups List of groups
	 * @param typeFolder Folder for this type
	 * @param outputDir Target root folder for the file
	 * 
	 */
	 void createGroupedIndex(String title, String description,
			String groupName, List<?> groups,  String typeFolder, File outputDir) 
		throws IOException {
		var scope = createScopeMap(title + TITLE_PREFIX);
		scope.put("listtitle", title);
		scope.put("description", description);
		scope.put("groups", groups);
		scope.put("groupname", groupName);

		scope.put("typefolder", "../" + typeFolder + "/");

		// Load the template
		if (groupedTemplate == null) {
			groupedTemplate = getTemplate("entity-grouped");
		}

		File indexFile = new File(outputDir, generateFileName(INDEX));
		try (FileOutputStream dest = new FileOutputStream(indexFile)) {
			generateContent(groupedTemplate, scope, dest);
		}
	}

	static ResourceUse buildEmptyResourceUse() {
		return new ResourceUse(new ArrayList<>(), new ArrayList<>());
	}

	/**
	 * Gets the usage of a Resource by it's name. This will return where is an input or output
	 * to a process.
	 * 
	 * @param name
	 * @return
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
	 * Factor method to create a ProcessItem from a set of attributes.
	 * 
	 * @param name
	 * @param type
	 * @param amount
	 * @return
	 */
	static ItemQuantity createItemQuantity(String name, ItemType type, double amount) {
		String typeFolder = switch(type) {
			case AMOUNT_RESOURCE -> ResourceGenerator.TYPE_NAME;
			case BIN -> null;
			case EQUIPMENT -> null;
			case PART -> PartGenerator.TYPE_NAME;
			case VEHICLE -> VehicleGenerator.TYPE_NAME;
		};

		String amountTxt = (type == ItemType.AMOUNT_RESOURCE ? amount + " kg"
													: Integer.toString((int) amount));
		return new ItemQuantity(name, type.getName(), "../" + typeFolder + "/", amountTxt);
	}

	/**
	 * Generates all configurations.
	 * 
	 * @param outputDir Root location for generated files
	 * @throws IOException
	 */
	public void generateAll(File outputDir) throws IOException {
		List<TypeGenerator<? extends Object>> gens = new ArrayList<>();

		for(var f : GENERATORS) {
			gens.add(getGenerator(f));
		}

		// Generate all subtype
		for(var g : gens) {
			g.generateAll(outputDir);
		}

		Map<String,Object> topScope = createScopeMap("Configurations");
		topScope.put("generators", gens);

		if (generateTopLevel) {
			var topTemplate = getTemplate("top-list");

			// Generate configuration overview page
			try (FileOutputStream topIndex = new FileOutputStream(new File(outputDir,
															generateFileName(INDEX)))) {
				generateContent(topTemplate, topScope, topIndex);
			}
		}
	}

    public SimulationConfig getConfig() {
        return config;
    }

	/**
	 * Looks up a template. This will select the appropriate Mustache template for the defined
	 * template set.
	 * 
	 * @param template
	 * @return
	 */
	Mustache getTemplate(String template) {
		return mf.compile(templateDir + template + ".mustache");
	}

	/**
	 * Creates a final output by applying a template to a scope.

	 * @param template Template to use to generate content
	 * @param scope Scope of properties to apply
	 * @param output Destination for generated content
	 * @throws IOException
	 */
	void generateContent(Mustache template, Map<String, Object> scope, OutputStream output)
				throws IOException {
		var writer = new OutputStreamWriter(output);
        template.execute(writer, scope);
        writer.flush();
	}

	/**
	 * Factory method to create a Type Generator
	 * @param name Name of the generator
	 * @return
	 */
	TypeGenerator<? extends Object> getGenerator(String name) {
		return switch(name.toLowerCase()) {
			case BuildingGenerator.TYPE_NAME -> new BuildingGenerator(this);
			case ComplaintGenerator.TYPE_NAME -> new ComplaintGenerator(this);
			case CrewGenerator.TYPE_NAME -> new CrewGenerator(this);
			case FoodGenerator.TYPE_NAME -> new FoodGenerator(this);
			case PartGenerator.TYPE_NAME -> new PartGenerator(this);
			case ProcessGenerator.TYPE_NAME -> new ProcessGenerator(this);
			case ResourceGenerator.TYPE_NAME -> new ResourceGenerator(this);
			case ManifestGenerator.TYPE_NAME -> new ManifestGenerator(this);
			case ScenarioGenerator.TYPE_NAME -> new ScenarioGenerator(this);
			case SettlementGenerator.TYPE_NAME -> new SettlementGenerator(this);
			case TreatmentGenerator.TYPE_NAME -> new TreatmentGenerator(this);
			case VehicleGenerator.TYPE_NAME -> new VehicleGenerator(this);
			default -> null;
		};
	}
}
