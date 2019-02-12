/**
 * Mars Simulation Project
 * PartConfig.java
 * @version 3.1.0 2017-09-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Document;
import org.jdom2.Element;


/**
 * Provides configuration information about parts. Uses a DOM document to get
 * the information.
 */
public final class PartConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
//	private static Logger logger = Logger.getLogger(PartConfig.class.getName());
//  private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());

	// Element names
	public static final String PART = "part";
	public static final String DESCRIPTION = "description";
	public static final String NAME = "name";
	public static final String MASS = "mass";
	public static final String MAINTENANCE_ENTITY_LIST = "maintenance-entity-list";
	public static final String ENTITY = "entity";
	public static final String PROBABILITY = "probability";
	public static final String MAX_NUMBER = "max-number";


	/** The next global part ID. */
	private static int nextID;

	private static Set<Part> partSet = new TreeSet<Part>();
	
	/**
	 * Constructor
	 * 
	 * @param itemResourceDoc the item resource XML document.
	 * @throws Exception if error reading XML document
	 */
	public PartConfig(Document itemResourceDoc) {
		// Pick up from the last resource id
		nextID = ResourceUtil.FIRST_ITEM_RESOURCE_ID;
		loadItemResources(itemResourceDoc);
	}

	/**
	 * Loads item resources from the parts.xml config document.
	 * 
	 * @param itemResourceDoc the configuration XML document.
	 * @throws Exception if error loading item resources.
	 */
	private void loadItemResources(Document itemResourceDoc) {
		if (partSet == null || partSet.isEmpty()) {
			Element root = itemResourceDoc.getRootElement();
			List<Element> partNodes = root.getChildren(PART);
			for (Element partElement : partNodes) {
				nextID++;
				String name = "";
				String description = "no description available.";
	
				// Get name.
				name = partElement.getAttributeValue(NAME).toLowerCase();
	
				// get description
				Element descriptElem = partElement.getChild(DESCRIPTION);
				if (descriptElem != null) {
					description = descriptElem.getText();
				}
	
				// Get mass.
				double mass = Double.parseDouble(partElement.getAttributeValue(MASS));
	
				if (mass == 0 || partElement.getAttributeValue(MASS) == null)
					throw new IllegalStateException(
							"PartConfig detected invalid mass in parts.xml : " + name);
			
				Part p = new Part(name, nextID, description, mass, 1);
				
				for (Part pp: partSet) {
					if (pp.getName().equalsIgnoreCase(name))
						throw new IllegalStateException(
								"PartConfig detected an duplicated part entry in parts.xml : " + name);
				}
				
				partSet.add(p);
//				System.out.println("part " + nextID + " " + name);
				
				// ItemResource r = new ItemResource(name, description, mass);
				// itemResources.add(r);
	
				// Add maintenance entities for part.
				Element entityListElement = partElement.getChild(MAINTENANCE_ENTITY_LIST);
				if (entityListElement != null) {
					List<Element> entityNodes = entityListElement.getChildren(ENTITY);
					for (Element entityElement : entityNodes) {
						String entityName = entityElement.getAttributeValue(NAME);
						int probability = Integer.parseInt(entityElement.getAttributeValue(PROBABILITY));
						int maxNumber = Integer.parseInt(entityElement.getAttributeValue(MAX_NUMBER));
						p.addMaintenanceEntity(entityName, probability, maxNumber);
					}
				}
			}
		}
	}

	/**
	 * Gets a set of all parts.
	 * 
	 * @return a set of parts.
	 */
	public Set<Part> getPartSet() {
		return partSet;
	}
	
//	/**
//	 * gives back an alphabetically ordered map of all item resources.
//	 * 
//	 * @return {@link TreeMap}<{@link String},{@link ItemResource}>
//	 */
	// public Map<String, ItemResource> getItemResourcesMap() {
	// return itemResourceMap;
	// }


//		TreeMap<String,Part> map = new TreeMap<String,Part>();
//		for (Part resource : itemResources) {
//			map.put(resource.getName(),resource);
//		}
//		return map;

//	}
}