/**
 * Mars Simulation Project
 * AmountResourceConfig.java
 * @version 3.1.0 2017-03-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
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

	private static int resource_id = 0;

	// Data members.
	private static Set<AmountResource> resourceSet;

	/**
	 * Constructor
	 * @param amountResourceDoc the amount resource XML document.
	 * @throws Exception if error reading XML document
	 */
	public AmountResourceConfig(Document amountResourceDoc) {
		resourceSet = new TreeSet<AmountResource>();
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
			resource_id++;
			// 2015-02-24 Added toLowerCase() just in case
			String name = resourceElement.getAttributeValue(NAME).toLowerCase();
			// 2016-06-28 Added type
			String type = resourceElement.getAttributeValue(TYPE);

			String description = resourceElement.getText();
			// Get phase.
			String phaseString = resourceElement.getAttributeValue(PHASE).toLowerCase();

			//PhaseType phase = PhaseType.valueOf(phaseString);
			PhaseType phaseType = PhaseType.fromString(phaseString);

			// Get life support
			Boolean lifeSupport = Boolean.parseBoolean(resourceElement.getAttributeValue(LIFE_SUPPORT));
			// 2014-11-25 Added edible
			Boolean edible = Boolean.parseBoolean(resourceElement.getAttributeValue(EDIBLE));
			// 2014-11-25 Added edible
			
			resourceSet.add(new AmountResource(resource_id, name, type, description, phaseType, lifeSupport, edible));

			if (type != null && type.toLowerCase().equals(CROP)) {
				resource_id++;
				// Create the tissue culture for each crop.
				resourceSet.add(new AmountResource(resource_id, name + " " + TISSUE_CULTURE, TISSUE_CULTURE, description, phaseType, lifeSupport, false));
				// TODO: may set edible to true
			}

		}
	}

	/**
	 * Gets a set of all amount resources.
	 * @return set of resources.
	 */
	public Set<AmountResource> getAmountResources() {
		return resourceSet;
	}



	public void destroy() {
		resourceSet = null;
	}
}