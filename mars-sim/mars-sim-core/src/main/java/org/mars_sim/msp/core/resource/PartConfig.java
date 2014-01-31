/**
 * Mars Simulation Project
 * PartConfig.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Provides configuration information about parts. Uses a DOM document to get the information.
 */
public final class PartConfig implements Serializable {

    /** default serial id. */
	private static final long serialVersionUID = 1L;

	// Element names
    private static final String PART = "part";
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final String MASS = "mass";
    private static final String MAINTENANCE_ENTITY_LIST = "maintenance-entity-list";
    private static final String ENTITY = "entity";
    private static final String PROBABILITY = "probability";
    private static final String MAX_NUMBER = "max-number";
    
    // Data members.
    private Set<ItemResource> itemResources = new HashSet<ItemResource>();
    private TreeMap<String,String> descriptions = new TreeMap<String,String>();

    /**
     * Constructor
     * @param itemResourceDoc the item resource XML document.
     * @throws Exception if error reading XML document
     */
    public PartConfig(Document itemResourceDoc) {
        loadItemResources(itemResourceDoc);
    }

    /**
     * Loads item resources from the parts.xml config document.
     * @param itemResourceDoc the configuration XML document.
     * @throws Exception if error loading item resources.
     */
    @SuppressWarnings("unchecked")
    private void loadItemResources(Document itemResourceDoc) {
        Element root = itemResourceDoc.getRootElement();
        List<Element> partNodes = root.getChildren(PART);
        descriptions.clear();
        for (Element partElement : partNodes) {
            String name = "";
            String description = "";

            // Get name.
            name = partElement.getAttributeValue(NAME);
            
            // get description
            Element descriptElem = partElement.getChild(DESCRIPTION);
            if (descriptElem != null) {
            	description = descriptElem.getText();
            }
            descriptions.put(name,description);

            // Get mass.
            double mass = Double.parseDouble(partElement
                    .getAttributeValue(MASS));

            // Add part to item resources.
            Part part = new Part(name, mass);
            itemResources.add(part);

            // Add maintenance entities for part.
            Element entityListElement = partElement
                    .getChild(MAINTENANCE_ENTITY_LIST);
            if (entityListElement != null) {
                List<Element> entityNodes = entityListElement
                        .getChildren(ENTITY);
                for (Element entityElement : entityNodes) {
                    String entityName = entityElement.getAttributeValue(NAME);
                    int probability = Integer.parseInt(entityElement
                            .getAttributeValue(PROBABILITY));
                    int maxNumber = Integer.parseInt(entityElement
                            .getAttributeValue(MAX_NUMBER));
                    part.addMaintenanceEntity(entityName, probability,
                            maxNumber);
                }
            }
        }
    }

	/**
	 * Gets a collection of all item resources.
	 * @return collection of item resources.
	 */
	public Set<ItemResource> getItemResources() {
		return itemResources;
	}

	public String getDescription(ItemResource resource) {
		String description = descriptions.get(resource.getName());
		if (description != null && description.length() > 0) {
			return description;
		} else return "no description available.";
	}

	/**
	 * gives back an alphabetically ordered map of all
	 * item resources.
	 * @return {@link TreeMap}<{@link String},{@link ItemResource}>
	 */
	public TreeMap<String,ItemResource> getItemResourcesMap() {
		TreeMap<String,ItemResource> map = new TreeMap<String,ItemResource>();
		for (ItemResource resource : itemResources) {
			map.put(resource.getName(),resource);
		}
		return map;
	}
}