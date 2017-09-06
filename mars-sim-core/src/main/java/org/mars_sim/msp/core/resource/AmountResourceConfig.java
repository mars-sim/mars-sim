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
	private static final long serialVersionUID = 1L;

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
	Set<AmountResource> arSet;

	/**
	 * Constructor
	 * @param amountResourceDoc the amount resource XML document.
	 * @throws Exception if error reading XML document
	 */
	public AmountResourceConfig(Document amountResourceDoc) {
		id = 0;
		loadAmountResources(amountResourceDoc);
	}

	/**
	 * Loads amount resources from the resources.xml config document.
	 * @param amountResourceDoc the configuration XML document.
	 * @throws Exception if error loading amount resources.
	 */
	@SuppressWarnings("unchecked")
	private void loadAmountResources(Document amountResourceDoc) {
		//System.out.println("arSet :\t" + arSet.hashCode());
		arSet = new TreeSet<AmountResource>();
		//System.out.println("arSet :\t" + arSet.hashCode());

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
			arSet.add(new AmountResource(id, name, type, description, phase, lifeSupport, edible));

			if (type != null && type.toLowerCase().equals(CROP)) {
				id++;
				// Create the tissue culture for each crop.
				arSet.add(new AmountResource(id, name + " " + TISSUE_CULTURE, TISSUE_CULTURE, description, phase, lifeSupport, false));
				// TODO: may set edible to true
			}

		}
	}

	/**
	 * Gets a set of all amount resources.
	 * @return set of resources.
	 */
	public Set<AmountResource> getAmountResources() {
		//System.out.println("arSet :\t" + arSet.hashCode());
		return arSet;
	}



	public void destroy() {
		arSet = null;
	}
}