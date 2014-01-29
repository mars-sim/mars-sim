/**
 * Mars Simulation Project
 * AmountResourceConfig.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import org.jdom.Document;
import org.jdom.Element;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides configuration information about amount resources. Uses a DOM document to get the information.
 */
public class AmountResourceConfig implements Serializable {

    // Element names
    private static final String RESOURCE = "resource";
    private static final String NAME = "name";
    private static final String PHASE = "phase";
    private static final String LIFE_SUPPORT = "life-support";
    
    // Data members.
    private Set<AmountResource> resources = new HashSet<AmountResource>();

    /**
     * Constructor
     * @param amountResourceDoc the amount resource XML document.
     * @throws Exception if error reading XML document
     */
    public AmountResourceConfig(Document amountResourceDoc) {
        loadAmountResources(amountResourceDoc);
    }

    /**
     * Loads amount resources from the resources.xml config document.
     * @param amountResourceDoc the configuration XML document.
     * @throws Exception if error loading amount resources.
     */
    @SuppressWarnings("unchecked")
    private void loadAmountResources(Document amountResourceDoc) {
        Element root = amountResourceDoc.getRootElement();
        List<Element> resourceNodes = root.getChildren(RESOURCE);

        for (Element resourceElement : resourceNodes) {
            String name = "";

            // Get name.
            name = resourceElement.getAttributeValue(NAME);

            // Get phase.
            String phaseString = resourceElement.getAttributeValue(PHASE);
            Phase phase = Phase.findPhase(phaseString);

            // Get life support
            Boolean lifeSupport = Boolean.parseBoolean(resourceElement
                    .getAttributeValue(LIFE_SUPPORT));

            // Create new amount resource.
            AmountResource resource = new AmountResource(name, phase, lifeSupport);
            resources.add(resource);
        }
    }
    
    /**
     * Gets a set of all amount resources.
     * @return set of resources.
     */
    public Set<AmountResource> getAmountResources() {
        return resources;
    }
}