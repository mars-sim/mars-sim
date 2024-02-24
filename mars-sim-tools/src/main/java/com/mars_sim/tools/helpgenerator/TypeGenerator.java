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
import java.util.logging.Logger;

import com.mars_sim.tools.helpgenerator.HelpGenerator.ProcessItem;

/**
 * This is an abstract generator for a configuration type. It provides generic methods.
 */
public abstract class TypeGenerator<T> {
    private static Logger logger = Logger.getLogger(TypeGenerator.class.getName());

    private HelpGenerator parent;
    private String typeName;
    private String title;
    private String description;

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
    
    protected HelpGenerator getParent() {
        return parent;
    }

    /**
     * Creates an index page for the given entities. This can be overriden for a specialised index
     * @param entities List of entities
     * @param targetDir Target folder for output
     * @throws IOException
     */
	protected void createIndex(List<T> entities, String targetDir)
		throws IOException {
            parent.createIndex(title, description, entities, targetDir);
	}

    /**
	 * Generate the files for all the entities of this type includign an index.
	 * @throws IOException
	 */
	public void generateAll() throws IOException {

		File targetDir = new File(parent.getOutputDir() + typeName);
		targetDir.mkdirs();

		List<T> vTypes = getEntities(); 
	
		// Create vehicle index
		createIndex(vTypes, typeName);

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
			String inputTitle, List<ProcessItem> inputs,
			String outputTitle, List<ProcessItem> outputs) {

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

		if (resourceUsed != null) {
			scope.put("inputProcesses", resourceUsed.asInput());
			scope.put("hasInputProcesses", !resourceUsed.asInput().isEmpty());
			scope.put("outputProcesses", resourceUsed.asOutput());
			scope.put("hasOutputProcesses", !resourceUsed.asOutput().isEmpty());
		}
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