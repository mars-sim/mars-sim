/*
 * Mars Simulation Project
 * AmountResourceConfig.java
 * @date 2022-06-25
 * @author Scott Davis
 */

package com.mars_sim.core.resource;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Document;
import org.jdom2.Element;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.food.FoodType;
import com.mars_sim.core.goods.GoodType;

/**
 * Provides configuration information about amount resources. Uses a DOM
 * document to get the information.
 */
public class AmountResourceConfig {

	// Element names
	private static final String TISSUE = FoodType.TISSUE.getName();
	private static final String RESOURCE = "resource";
	private static final String NAME = "name";
	private static final String PHASE = "phase";
	private static final String DEMAND = "demand";
	private static final String LIFE_SUPPORT = "life-support";
	private static final String EDIBLE = "edible";
	private static final String TYPE = "type";
	
	/** The next global amount resource ID. */
	private static int nextID = ResourceUtil.FIRST_FREE_AMOUNT_RESOURCE_ID;

	// Data members.
	private Set<AmountResource> resourceSet = new TreeSet<>();
	private Set<Integer> tissueCultureSet = new TreeSet<>();

	/**
	 * Constructor.
	 *
	 * @param amountResourceDoc the amount resource XML document.
	 * @throws Exception if error reading XML document
	 */
	public AmountResourceConfig(Document amountResourceDoc) {
		loadAmountResources(amountResourceDoc);
		ResourceUtil.registerResources(resourceSet);
	}

	/**
	 * Loads amount resources from the resources.xml config document.
	 *
	 * @param amountResourceDoc the configuration XML document.
	 * @throws Exception if error loading amount resources.
	 */
	private synchronized void loadAmountResources(Document amountResourceDoc) {
		if (!resourceSet.isEmpty()) {
			// just in case if another thread is being created
			return;
		}

		// Build the global list in a temp to avoid access before it is built
		Set<AmountResource> newResources = new TreeSet<>();
		
		Element root = amountResourceDoc.getRootElement();
		List<Element> resourceNodes = root.getChildren(RESOURCE);
		for (Element resourceElement : resourceNodes) {

			String name = resourceElement.getAttributeValue(NAME);

			String type = resourceElement.getAttributeValue(TYPE);

			GoodType goodType = GoodType.valueOf(ConfigHelper.convertToEnumName(type));
					
			String description = resourceElement.getText();
			
			// Get phase 
			String phaseString = resourceElement.getAttributeValue(PHASE);
			PhaseType phaseType = PhaseType.valueOf(ConfigHelper.convertToEnumName(phaseString));

			// Get the demand modifier
			double demand = ConfigHelper.getOptionalAttributeDouble(resourceElement, DEMAND, 1);
			
			// Get life support
			Boolean lifeSupport = Boolean.parseBoolean(resourceElement.getAttributeValue(LIFE_SUPPORT));

			Boolean edible = Boolean.parseBoolean(resourceElement.getAttributeValue(EDIBLE));

			int newId = ResourceUtil.getFixedId(name);
			if (newId < 0) {
				newId = nextID++;
			}
			AmountResource resource = new AmountResource(newId, name, goodType, description, phaseType, demand, lifeSupport, edible);

			for (AmountResource r: resourceSet) {
				if (r.getName().equalsIgnoreCase(resource.getName()))
					throw new IllegalStateException(
							"AmountResourceConfig detected an duplicated resource entry in resources.xml : " + resource.getName());
			}

			newResources.add(resource);

			// Create a tissue culture object for each crop
			if (goodType != null && goodType == GoodType.CROP) {
				// Note: may set edible to true
				// Assume the demand multiplier of a crop tissue is twice as much
				AmountResource tissue = new AmountResource(nextID++, (name + " " + TISSUE), GoodType.TISSUE,
						description, phaseType, demand * 2, lifeSupport, false);
				tissueCultureSet.add(tissue.getID());

				for (AmountResource r: resourceSet) {
					if (r.getName().equalsIgnoreCase(tissue.getName()))
						throw new IllegalStateException(
								"AmountResourceConfig detected an duplicated resource entry in resources.xml : " + tissue.getName());
				}

				newResources.add(tissue);
			}
		}
		
		resourceSet = Collections.unmodifiableSet(newResources);
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
}
