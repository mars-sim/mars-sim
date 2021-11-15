/**
 * Mars Simulation Project
 * PartConfig.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
//	private static final Logger logger = Logger.getLogger(PartConfig.class.getName());
//  private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());

	// Element names
	public static final String PART = "part";
	public static final String DESCRIPTION = "description";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String MASS = "mass";
	public static final String MAINTENANCE_ENTITY_LIST = "maintenance-entity-list";
	public static final String ENTITY = "entity";
	public static final String PROBABILITY = "probability";
	public static final String MAX_NUMBER = "max-number";


	/** The next global part ID. */
	private int nextID;

	private Set<Part> partSet = new TreeSet<>();
	private Map<String,List<MaintenanceScope>> scopes = new HashMap<>();

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

			// Get type.
			String type = partElement.getAttributeValue(TYPE);

			if (type == null || partElement.getAttributeValue(TYPE) == null)
				throw new IllegalStateException(
						"PartConfig detected invalid type in parts.xml : " + type);

			Part p = new Part(name, nextID, description, type, mass, 1);

			for (Part pp: partSet) {
				if (pp.getName().equalsIgnoreCase(name))
					throw new IllegalStateException(
							"PartConfig detected an duplicated part entry in parts.xml : " + name);
			}

			partSet.add(p);

			// Add maintenance entities for part.
			Element entityListElement = partElement.getChild(MAINTENANCE_ENTITY_LIST);
			if (entityListElement != null) {
				List<Element> entityNodes = entityListElement.getChildren(ENTITY);
				for (Element entityElement : entityNodes) {
					String entityName = entityElement.getAttributeValue(NAME);
					double probability = Double.parseDouble(entityElement.getAttributeValue(PROBABILITY));
					int maxNumber = Integer.parseInt(entityElement.getAttributeValue(MAX_NUMBER));

					MaintenanceScope newMaintenance = new MaintenanceScope(p, entityName, probability, maxNumber);
					addPartScope(entityName, newMaintenance);
				}
			}
		}
	}

	private void addPartScope(String scope, MaintenanceScope newMaintenance) {

		String key = scope.toLowerCase();
		List<MaintenanceScope> maintenance = scopes.computeIfAbsent(key, k -> new ArrayList<>());
		maintenance.add(newMaintenance);
	}

	/**
	 * Get the maintenance schedules for a specific scopes, e.g. type of vehicle or function.
	 * @param scope Possible scopes
	 * @return
	 */
	public List<MaintenanceScope> getMaintenance(Collection<String> scope) {
		List<MaintenanceScope> results = new ArrayList<>();
		for (String s : scope) {
			List<MaintenanceScope> m = scopes.get(s.toLowerCase());
			if (m != null) {
				results.addAll(scopes.get(s.toLowerCase()));
			}
		}
		return results ;
	}

	/**
	 * Get the maintenance schedules for a specific scopes, e.g. type of vehicle or function.
	 * Apply a filter so only a certain part is taken.
	 * @param scope Possible scopes
	 * @param part Filter to part
	 * @return
	 */
	public List<MaintenanceScope> getMaintenance(Collection<String> scope, Part part) {
		return getMaintenance(scope).stream().filter(m -> m.getPart().equals(part)).collect(Collectors.toList());
	}

	/**
	 * Gets a set of all parts.
	 *
	 * @return a set of parts.
	 */
	public Set<Part> getPartSet() {
		return partSet;
	}
}
