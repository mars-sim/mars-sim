/*
 * Mars Simulation Project
 * TypeGenerator.java
 * @date 2024-02-23
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import com.github.mustachejava.Mustache;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.tools.helpgenerator.GenericsGrouper.NamedGroup;
import com.mars_sim.tools.helpgenerator.HelpContext.ItemQuantity;
import com.mars_sim.tools.helpgenerator.HelpContext.ResourceUse;

/**
 * This is an abstract generator for a configuration type. It provides generic methods.
 */
public abstract class TypeGenerator<T> {
    
	// Name of the index file for lists of entities
    private static final String TITLE_PREFIX = " Configurations";

    private static Logger logger = Logger.getLogger(TypeGenerator.class.getName());

    // CReate an empty reosurce use
    protected static final ResourceUse EMPTY_USE = HelpContext.buildEmptyResourceUse();

	private static Mustache indexTemplate;
	private static Mustache groupedTemplate;

    private HelpContext parent;
    private String typeName;
    private String title;
    private String description;
    private String configXML;
    private boolean changedViaEditor;
    private Mustache detailsTemplate;

    // Used to group entities for grouped index
    private Function<T,String> grouper;
    private String groupName;

    /**
     * Converts a set of Process inputs/outputs to a generic item quantity
     */
    protected List<ItemQuantity> toQuantityItems(List<ProcessItem> list) {
        var hc = getParent();
		return list.stream()
					.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
					.map(v -> hc.createItemQuantity(v.getName(), v.getType(), v.getAmount()))
					.toList();
	}

    
    /**
     * Converts a set of resource value pairs of a specific type to a generic item quantity
     */
    protected List<ItemQuantity> toQuantityItems(Map<String,Integer> items, ItemType type) {
        var hc = getParent();
        return toQuantityItems(hc, items, type);
    }
    
    static List<ItemQuantity> toQuantityItems(HelpContext hc, Map<String,Integer> items, ItemType type) {
        return items.entrySet().stream()
					.map(v -> hc.createItemQuantity(v.getKey(), type, v.getValue()))
					.toList();
	}

    /**
     * Create an instance.
     * @param parent The parent generator to provide context.
     * @param typeName The type name used for the folder and reference.
     * @param title Title in  the index
     * @param description Description of the entity being rendered.
     * @param configXML Name of teh assoicated config XML file; can be null
     */
    protected TypeGenerator(HelpContext parent, String typeName, String title, String description,
                            String configXML) {
        this.typeName = typeName;
        this.parent = parent;
        this.title = title;
        this.description = description;
        this.detailsTemplate = parent.getTemplate(typeName + "-detail");
        this.configXML = configXML;
        this.changedViaEditor = false;
    }

    /**
     * Can this type of confguration entity be chaned via the UI Scenario Editor
     * @param newValue New setting
     */
    protected void setChangeViaEditor(boolean newValue) {
        changedViaEditor = newValue;
    }

    /**
     * Define an grouper to create a groupe index page.
     * @param grouper Function to return a String category for a T
     */
    protected void setGrouper(String name, Function<T,String> grouper) {
        this.groupName = name;
        this.grouper = grouper;
    }
    
    protected HelpContext getParent() {
        return parent;
    }

    /**
	 * Creates an index page for a set of named entities.
	 * 
	 * @param entites List of Entities to render
	 * @param outputDir Target root folder for the file
	 * 
	 */
	 private void createIndex(List<T> entities, File outputDir) 
                    throws IOException {
        var context = getParent();
        Mustache template;

        var scope = context.createScopeMap(title + TITLE_PREFIX);
        scope.put("listtitle", title);
        scope.put("description", description);
        scope.put("typefolder", "../" + typeName + "/");
        if (configXML != null) {
            scope.put("type.changexml", configXML);
        }
        if (changedViaEditor) {
            scope.put("type.changeeditor", Boolean.TRUE);
        }

        if (grouper != null) {
            List<NamedGroup<T>> groups = GenericsGrouper.getGroups(entities, grouper);
            scope.put("groups", groups);
            scope.put("groupname", groupName);
    
            // Load the template
            if (groupedTemplate == null) {
                groupedTemplate = context.getTemplate("entity-grouped");
            }
            template = groupedTemplate;
        }
        else {
            scope.put("entities", entities);

            // Load the template
            if (indexTemplate == null) {
                indexTemplate = getParent().getTemplate("entity-list");
            }
            template = indexTemplate;
        }
       
        // Generate file
        File indexFile = new File(outputDir, getParent().generateFileName(getParent().getIndexName()));
        try (FileOutputStream dest = new FileOutputStream(indexFile)) {
            getParent().generateContent(template, scope, dest);
        }
    }

    /**
	 * Generate the files for all the entities of this type including an index.
     * @param outputDir The top levle folder of generated files
	 * @throws IOException
	 */
	public void generateAll(File outputDir) throws IOException {
		logger.info("Generating files for " + typeName);

		File targetDir = new File(outputDir, typeName);
		targetDir.mkdirs();

		List<T> vTypes = getEntities(); 
	
		// Create index
        createIndex(vTypes, targetDir);

		// Individual entity pages
		for(T v : vTypes) {
            File targetFile = new File(targetDir, parent.generateFileName(getEntityName(v)));
            try(FileOutputStream dest = new FileOutputStream(targetFile)) {
                generateEntity(v, dest);
            }
		}
	}

    /**
	 * Prepare the scope to support the process-inout template
	 * @param scope Scop of properties
	 * @param inputTitle Title of inputs
	 * @param inputs
	 * @param outputTitle Title of outputs
	 * @param outputs
	 */
	protected void addProcessInputOutput(Map<String, Object> scope,
			String inputTitle, List<ItemQuantity> inputs,
			String outputTitle, List<ItemQuantity> outputs) {

		scope.put("inputs", inputs);
		scope.put("inputsTitle", inputTitle);
		scope.put("outputs", outputs);
		scope.put("outputsTitle", outputTitle);
	}

    /**
	 * Prepare the scope to support the process-flow template for a specific resource. Adds
     * the required props to hold any processes that uses the resource as either an input or output
     * @param resourceName Name of the resource for flows
	 * @param scope Scope of properties
	 */
    protected void addProcessFlows(String resourceName, Map<String, Object> scope) {
		var resourceUsed = getParent().getResourceUsageByName(resourceName);

		if (resourceUsed == null) {
            resourceUsed = EMPTY_USE;
        }
        scope.put("inputProcesses", resourceUsed.asInput());
        scope.put("outputProcesses", resourceUsed.asOutput());	
    }

    /**
     * Generate a help page for a specific entity using the context generator.
     * @param e Entity to render
     * @param output Destinatino of content
     * @throws IOException
     */
    public void generateEntity(T e, OutputStream output) throws IOException
    {
        // Add base properties
        var pageName = getEntityName(e);
        var vScope = parent.createScopeMap(title + " - " + pageName);
        vScope.put(typeName, e);
        vScope.put("shorttitle", pageName);

        // Add any customer properties
        addEntityProperties(e, vScope);

        // Generate the file
        parent.generateContent(detailsTemplate, vScope, output);
    }

    /**
     * Add any entity specific properties. Should be overriden by subclasses
     * @param entity The entity to display
     * @param scope Scope of the properties to use for the template
     * @return
     */
    protected void addEntityProperties(T entity, Map<String,Object> scope) {
        // Default implement needs no extra properties
    }

    /**
     * Get a list of the entities to be rendered
     * @return
     */
    protected abstract List<T> getEntities();
    
    /**
     * Get the identifable/unique name for this entity.
     * If there was a common internface this would not be required.
     * @param v entity
     * @return
     */
    protected abstract String getEntityName(T v);

}