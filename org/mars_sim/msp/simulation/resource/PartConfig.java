/**
 * Mars Simulation Project
 * PartConfig.java
 * @version 2.84 2008-05-17
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.resource;

import java.io.Serializable;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;


/**
 * Provides configuration information about parts.
 * Uses a DOM document to get the information. 
 */
public final class PartConfig implements Serializable {

	// Element names
	private static final String PART = "part";
	private static final String NAME = "name";
	private static final String MASS = "mass";
	private static final String MAINTENANCE_ENTITY_LIST = "maintenance-entity-list";
	private static final String ENTITY = "entity";
	private static final String PROBABILITY = "probability";
	private static final String MAX_NUMBER = "max-number";
	
	/**
	 * Constructor
	 * @param itemResourceDoc the item resource XML document.
	 * @throws Exception if error reading XML document
	 */
	public PartConfig(Document itemResourceDoc) throws Exception {
		loadItemResources(itemResourceDoc);
	}
	
	/**
	 * Loads item resources from the parts.xml config document.
	 * @param itemResourceDoc the configuration XML document.
	 * @throws Exception if error loading item resources.
	 */
    @SuppressWarnings("unchecked")
	private void loadItemResources(Document itemResourceDoc) throws Exception {
		Element root = itemResourceDoc.getRootElement();
		List<Element> partNodes = root.getChildren(PART);
		for (Element partElement : partNodes) {
			String name = "";
			
			try {
				// Get name.
				name = partElement.getAttributeValue(NAME);
				
				// Get mass.
				double mass = Double.parseDouble(partElement.getAttributeValue(MASS));
				
				// Add part to item resources.
				Part part = new Part(name, mass);
				
				// Add maintenance entities for part.
				Element entityListElement = partElement.getChild(MAINTENANCE_ENTITY_LIST);
				if (entityListElement != null) {
					List<Element> entityNodes = entityListElement.getChildren(ENTITY);
					for (Element entityElement : entityNodes) {
						String entityName = entityElement.getAttributeValue(NAME);
						int probability = Integer.parseInt(entityElement.getAttributeValue(PROBABILITY));
						int maxNumber = Integer.parseInt(entityElement.getAttributeValue(MAX_NUMBER));
						part.addMaintenanceEntity(entityName, probability, maxNumber);
					}
				}
			}
			catch (Exception e) {
				throw new Exception("Error reading parts " + name + ": " + e.getMessage());
			}
		}
	}
}