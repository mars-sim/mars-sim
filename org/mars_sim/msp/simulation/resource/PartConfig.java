/**
 * Mars Simulation Project
 * PartConfig.java
 * @version 2.82 2007-10-13
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.resource;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
	 * Private constructor for utility class.
	 */
	public PartConfig(Document itemResourceDoc) throws Exception {
		loadItemResources(itemResourceDoc);
	}
	
	/**
	 * Loads item resources from the parts.xml config document.
	 * @param itemResourceDoc the configuration XML document.
	 * @throws Exception if error loading item resources.
	 */
	private void loadItemResources(Document itemResourceDoc) throws Exception {
		Element root = itemResourceDoc.getDocumentElement();
		NodeList partNodes = root.getElementsByTagName(PART);
		for (int x=0; x < partNodes.getLength(); x++) {
			String name = "";
			
			try {
				Element partElement = (Element) partNodes.item(x);
				
				// Get name.
				name = partElement.getAttribute(NAME);
				
				// Get mass.
				double mass = Double.parseDouble(partElement.getAttribute(MASS));
				
				// Add part to item resources.
				Part part = new Part(name, mass);
				
				// Add maintenance entities for part.
				Element entityListElement = (Element) partElement.getElementsByTagName(MAINTENANCE_ENTITY_LIST).item(0);
				if (entityListElement != null) {
					NodeList entityNodes = entityListElement.getElementsByTagName(ENTITY);
					for (int y = 0; y < entityNodes.getLength(); y++) {
						Element entityElement = (Element) entityNodes.item(y);
						String entityName = entityElement.getAttribute(NAME);
						int probability = Integer.parseInt(entityElement.getAttribute(PROBABILITY));
						int maxNumber = Integer.parseInt(entityElement.getAttribute(MAX_NUMBER));
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