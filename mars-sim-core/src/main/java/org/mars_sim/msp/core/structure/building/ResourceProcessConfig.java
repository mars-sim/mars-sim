/*
 * Mars Simulation Project
 * ResourceProcessConfig.java
 * @date 2022-10-23
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure.building;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.process.ProcessSpecConfig;

/**
 * Parses a definition of Resource processes from XML and creates a lookup library.
 */
public class ResourceProcessConfig extends ProcessSpecConfig<ResourceProcessSpec> {

    private static final String DEFAULT = "default";

    public ResourceProcessConfig(Document processDoc) {
        super(processDoc);
    }

    @Override
    protected ResourceProcessSpec createProcess(Element processElement, String name, double powerRequired,
            int processTime, int workTime) {

        String defaultString = processElement.getAttributeValue(DEFAULT);
        boolean defaultOn = (defaultString != null) && defaultString.equalsIgnoreCase("on");
    
        return new ResourceProcessSpec(name, powerRequired, processTime, workTime, defaultOn);
    }
}
