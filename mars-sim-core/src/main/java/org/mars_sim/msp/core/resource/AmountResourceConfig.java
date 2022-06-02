/*
 * Mars Simulation Project
 * AmountResourceConfig.java
 * @date 2021-08-28
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.food.FoodType;

/**
 * Provides configuration information about amount resources. Uses a DOM
 * document to get the information.
 */
public class AmountResourceConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Element names
	private static final String TISSUE_CULTURE = FoodType.TISSUE.getName();
	private static final String RESOURCE = "resource";
	private static final String NAME = "name";
	private static final String PHASE = "phase";
	private static final String LIFE_SUPPORT = "life-support";
	private static final String EDIBLE = "edible";
	private static final String TYPE = "type";
	private static final String CROP = "crop";
	private static int nextID = ResourceUtil.FIRST_AMOUNT_RESOURCE_ID;

	// Data members.
	private static Set<AmountResource> resourceSet = new TreeSet<>();
	private static Set<Integer> tissueCultureSet = new TreeSet<>();

	/**
	 * Constructor
	 *
	 * @param amountResourceDoc the amount resource XML document.
	 * @throws Exception if error reading XML document
	 */
	public AmountResourceConfig(Document amountResourceDoc) {
		loadAmountResources(amountResourceDoc);
	}

	/**
	 * Loads amount resources from the resources.xml config document.
	 *
	 * @param amountResourceDoc the configuration XML document.
	 * @throws Exception if error loading amount resources.
	 */
	private static void loadAmountResources(Document amountResourceDoc) {
		if (resourceSet.isEmpty()) {
			Element root = amountResourceDoc.getRootElement();
			List<Element> resourceNodes = root.getChildren(RESOURCE);
			for (Element resourceElement : resourceNodes) {
				nextID++;
				String name = resourceElement.getAttributeValue(NAME).toLowerCase();

				String type = resourceElement.getAttributeValue(TYPE);

				String description = resourceElement.getText();
				// Get phase.
				String phaseString = resourceElement.getAttributeValue(PHASE).toLowerCase();

				// PhaseType phase = PhaseType.valueOf(phaseString);
				PhaseType phaseType = PhaseType.fromString(phaseString);

				// Get life support
				Boolean lifeSupport = Boolean.parseBoolean(resourceElement.getAttributeValue(LIFE_SUPPORT));

				Boolean edible = Boolean.parseBoolean(resourceElement.getAttributeValue(EDIBLE));

				AmountResource resource = new AmountResource(nextID, name, type, description, phaseType, lifeSupport, edible);

				if (phaseString == null || phaseType == null)
					throw new IllegalStateException(
							"AmountResourceConfig detected invalid PhaseType in resources.xml : " + resource.getName());

				for (AmountResource r: resourceSet) {
					if (r.getName().equalsIgnoreCase(resource.getName()))
						throw new IllegalStateException(
								"AmountResourceConfig detected an duplicated resource entry in resources.xml : " + resource.getName());
				}

				resourceSet.add(resource);

				if (type != null && type.toLowerCase().equals(CROP)) {
					nextID++;
					// Create the tissue culture for each crop.
					// Note: may set edible to true
					AmountResource tissue = new AmountResource(nextID, name + " " + TISSUE_CULTURE, TISSUE_CULTURE,
							description, phaseType, lifeSupport, false);
					tissueCultureSet.add(nextID);

					for (AmountResource r: resourceSet) {
						if (r.getName().equalsIgnoreCase(tissue.getName()))
							throw new IllegalStateException(
									"AmountResourceConfig detected an duplicated resource entry in resources.xml : " + tissue.getName());
					}

					resourceSet.add(tissue);
				}
			}
		}
	}

	/**
	 * Gets a set of all amount resources.
	 *
	 * @return set of resources.
	 */
	public Set<AmountResource> getAmountResources() {
		return resourceSet;
	}

	public Set<Integer> getTissueCultures() {
		return tissueCultureSet;
	}

	public int getNextID() {
		return nextID;
	}

	public void destroy() {
		resourceSet = null;
		tissueCultureSet = null;
	}
}
