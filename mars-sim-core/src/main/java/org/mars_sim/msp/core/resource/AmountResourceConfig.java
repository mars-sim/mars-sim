/**
 * Mars Simulation Project
 * AmountResourceConfig.java
 * @version 3.1.0 2017-03-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Provides configuration information about amount resources. Uses a DOM document to get the information.
 */
public class AmountResourceConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 123L;

	// Element names
	private static final String TISSUE_CULTURE = "tissue culture";
	private static final String RESOURCE = "resource";
	private static final String NAME = "name";
	private static final String PHASE = "phase";
	private static final String LIFE_SUPPORT = "life-support";

	// 2014-11-25 Added EDIBLE
	private static final String EDIBLE = "edible";
	// 2016-06-28 Added TYPE
	private static final String TYPE = "type";

	private static final String CROP = "crop";

	private static int id;

	// Data members.
	private Set<AmountResource> resources = new TreeSet<AmountResource>();
	private Map<String, AmountResource> amountResourceMap;
	private Map<Integer, AmountResource> amountResourceIDMap;
	private Map<Integer, String> IDNameMap;


	/**
	 * Constructor
	 * @param amountResourceDoc the amount resource XML document.
	 * @throws Exception if error reading XML document
	 */
	public AmountResourceConfig(Document amountResourceDoc) {
		id = 0;

		loadAmountResources(amountResourceDoc);

		amountResourceMap = new HashMap<String,AmountResource>();
		for (AmountResource resource : resources) {
			amountResourceMap.put(resource.getName(), resource);
		}

		amountResourceIDMap = new HashMap<Integer, AmountResource>();
		for (AmountResource resource : resources) {
			amountResourceIDMap.put(resource.getID(), resource);
		}

		IDNameMap = new HashMap<Integer, String>();
		for (AmountResource resource : resources) {
			IDNameMap.put(resource.getID(), resource.getName());
		}

    	// 2016-12-03 Call to just initialize amountResourceConfig in this constructor
    	//new AmountResource();
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
			id++;
			// 2015-02-24 Added toLowerCase() just in case
			String name = resourceElement.getAttributeValue(NAME).toLowerCase();
			// 2016-06-28 Added type
			String type = resourceElement.getAttributeValue(TYPE);

			String description = resourceElement.getText();
			// Get phase.
			String phaseString = resourceElement.getAttributeValue(PHASE).toUpperCase();

			Phase phase = Phase.valueOf(phaseString);
			// Get life support
			Boolean lifeSupport = Boolean.parseBoolean(resourceElement.getAttributeValue(LIFE_SUPPORT));
			// 2014-11-25 Added edible
			Boolean edible = Boolean.parseBoolean(resourceElement.getAttributeValue(EDIBLE));
			// 2014-11-25 Added edible
			resources.add(new AmountResource(id, name, type, description, phase, lifeSupport, edible));

			if (type != null && type.toLowerCase().equals(CROP)) {
				id++;
				// Create new tissue culture for each crop.
				resources.add(new AmountResource(id, name + " " + TISSUE_CULTURE, TISSUE_CULTURE, description, phase, lifeSupport, false));
				// TODO: may set edible to true
			}

		}
	}

	/**
	 * Gets a set of all amount resources.
	 * @return set of resources.
	 */
	public Set<AmountResource> getAmountResources() {
		return resources;
	}

	/**
	 * an alphabetically ordered map of all amount resources by name.
	 * @return {@link Map}<{@link String},{@link AmountResource}>
	 */
	public Map<String, AmountResource> getAmountResourcesMap() {
//		if (!amountResourceMap.isEmpty())
			return amountResourceMap;
/*
		else {
			Map<String, AmountResource> map = new HashMap<String,AmountResource>();
			for (AmountResource resource : resources) {
				map.put(resource.getName(), resource);
			}
			amountResourceMap = map;
			//System.out.println("amountResourcesMap constructed");
			return map;
		}
*/
	}

	/**
	 * an alphabetically ordered map of all amount resources by id.
	 * @return {@link Map}<{@link Integer},{@link AmountResource}>
	 */
	public Map<Integer, AmountResource> getAmountResourcesIDMap() {
//		if (!amountResourceIDMap.isEmpty())
			return amountResourceIDMap;
/*
		else {
			Map<Integer, AmountResource> map = new HashMap<Integer, AmountResource>();
			for (AmountResource resource : resources) {
				map.put(resource.getID(), resource);
			}
			amountResourceIDMap = map;
			//System.out.println("amountResourcesIDMap constructed");
			return map;
		}
*/
	}

	/**
	 * an alphabetically ordered map of all resource names by id.
	 * @return {@link Map}<{@link Integer},{@link String}>
	 */
	public Map<Integer, String> getIDNameMap() {
//		if (!IDNameMap.isEmpty())
			return IDNameMap;
/*
		else {
			Map<Integer, String> map = new HashMap<Integer, String>();
			for (AmountResource resource : resources) {
				map.put(resource.getID(), resource.getName());
			}
			IDNameMap = map;
			//System.out.println("IDNameMap constructed");
			return map;
		}
*/
	}

	public void destroy() {
		resources = null;
		amountResourceMap = null;
		amountResourceIDMap = null;
		IDNameMap = null;
	}
}