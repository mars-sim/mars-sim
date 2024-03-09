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

import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.tools.helpgenerator.GenericsGrouper.NamedGroup;
import com.mars_sim.tools.helpgenerator.HelpGenerator.ItemQuantity;
import com.mars_sim.tools.helpgenerator.HelpGenerator.ResourceUse;

/**
 * This is an abstract generator for a configuration type. It provides generic methods.
 */
public abstract class TypeGenerator<T> {
    private static Logger logger = Logger.getLogger(TypeGenerator.class.getName());

    // CReate an empty reosurce use
    protected static final ResourceUse EMPTY_USE = HelpGenerator.buildEmptyResourceUse();

    private HelpGenerator parent;
    private String typeName;
    private String title;
    private String description;

    // Used to group entities for grouped index
    private Function<T,String> grouper;

    /**
     * Converts a set of Process inputs/outputs to a generic item quantity
     */
    protected static List<ItemQuantity> toQuantityItems(List<ProcessItem> list) {
		return list.stream()
					.sorted((o1, o2)->o1.getName().compareTo(o2.getName()))
					.map(v -> HelpGenerator.createItemQuantity(v.getName(), v.getType(), v.getAmount()))
					.toList();
	}

    
    /**
     * Converts a set of resource value pairs of a specific type to a generic item quantity
     */
    protected static List<ItemQuantity> toQuantityItems(Map<String,Integer> items, ItemType type) {
		return items.entrySet().stream()
					.map(v -> HelpGenerator.createItemQuantity(v.getKey(), type, v.getValue()))
					.toList();
	}

    /**
     * Create an instance.
     * @param parent The parent generator to provide context.
     * @param typeName The type name used for the folder and reference.
     * @param title Title in  the index
     * @param description Description of the entity being rendered.
     */
    protected TypeGenerator(HelpGenerator parent, String typeName, String title, String description) {
        this.typeName = typeName;
        this.parent = parent;
        this.title = title;
        this.description = description;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Define an grouper to create a groupe index page.
     * @param grouper
     */
    protected void setGrouper(Function<T,String> grouper) {
        this.grouper = grouper;
    }
    
    protected HelpGenerator getParent() {
        return parent;
    }

    /**
     * Creates an index page for the given entities. This can be overriden for a specialised index
     * @param entities List of entities
     * @param targetDir Target folder for output
     * @throws IOException
     */
	private void createIndex(List<T> entities, File targetDir)
		throws IOException {
        if (grouper == null) {
            parent.createFlatIndex(title, description, entities, typeName, targetDir);
        }
        else {
            List<NamedGroup<T>> groups = GenericsGrouper.getGroups(entities, grouper);
            parent.createGroupedIndex(title, description, groups, typeName, targetDir);
        }
	}

    /**
	 * Generate the files for all the entities of this type including an index.
     * @param outputDir The top levle folder of generated files
	 * @throws IOException
	 */
	public void generateAll(File outputDir) throws IOException {

		File targetDir = new File(outputDir, typeName);
		targetDir.mkdirs();

		List<T> vTypes = getEntities(); 
	
		// Create vehicle index
		createIndex(vTypes, targetDir);

		// Individual vehicle pages
		logger.info("Generating details files");
		for(T v : vTypes) {
            File targetFile = new File(targetDir, parent.generateFileName(getEntityName(v)));
            try(FileOutputStream dest = new FileOutputStream(targetFile)) {
                generateEntity(v, dest);
            }
		}
	}

    /**
     * Get the identifable/unique name for this entity.
     * If there was a common internface this would not be required.
     * @param v entity
     * @return
     */
    protected abstract String getEntityName(T v);

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
     * @param v Entity to render
     * @param output Destinatino of content
     * @throws IOException
     */
    public abstract void generateEntity(T v, OutputStream output) throws IOException;

    /**
     * Get a list of the entities to be rendered
     * @return
     */
    protected abstract List<T> getEntities();
}