/*
 * Mars Simulation Project
 * PartConfig.java
 * @date 2022-10-03
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
import org.mars_sim.msp.core.configuration.ConfigHelper;
import org.mars_sim.msp.core.goods.GoodType;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.HeatSourceType;
import org.mars_sim.msp.core.structure.building.function.PowerSourceType;
import org.mars_sim.msp.core.structure.building.function.SystemType;
import org.mars_sim.msp.core.vehicle.VehicleType;


/**
 * Provides configuration information about parts. Uses a DOM document to get
 * the information.
 */
public final class PartConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// May add back private static SimLogger logger = SimLogger.getLogger(PartConfig.class.getName())
	
	// Element names
	public static final String PART = "part";
	public static final String DESCRIPTION = "description";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String MASS = "mass";
	public static final String STORABLE = "storable";
	public static final String MAINTENANCE_ENTITY_LIST = "maintenance-entity-list";
	public static final String ENTITY = "entity";
	public static final String PROBABILITY = "probability";
	public static final String MAX_NUMBER = "max-number";

	/** The next global part ID. */
	private int nextID;
	/** The set of parts. */
	private Set<Part> partSet = new TreeSet<>();
	/** The map of maintenance scopes. */
	private Map<String, List<MaintenanceScope>> scopes = new HashMap<>();

	/** The collection of standard scopes. */
	private static Set<String> STANDARD_SCOPES = new TreeSet<>();
	
	/**
	 * Constructor.
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
	 * Creates a set of standard scopes.
	 */
	private void createStandardScope() {
		for (VehicleType type: VehicleType.values()) {
			if (!STANDARD_SCOPES.contains(type.getName()))
				STANDARD_SCOPES.add(type.getName());
		}
		
		for (SystemType type: SystemType.values()) {
			if (!STANDARD_SCOPES.contains(type.getName()))
				STANDARD_SCOPES.add(type.getName());
		}
		
		for (FunctionType type: FunctionType.values()) {
			if (!STANDARD_SCOPES.contains(type.getName()))
				STANDARD_SCOPES.add(type.getName());
		}
		
		for (PowerSourceType type: PowerSourceType.values()) {
			if (!STANDARD_SCOPES.contains(type.getName()))
				STANDARD_SCOPES.add(type.getName());
		}
		
		for (HeatSourceType type: HeatSourceType.values()) {
			if (!STANDARD_SCOPES.contains(type.getName()))
				STANDARD_SCOPES.add(type.getName());
		}
	}
	
	public static void addScopes(Set<String> newScopes) {
		STANDARD_SCOPES.addAll(newScopes);
	}
	
	public static void addScopes(String newScope) {
		STANDARD_SCOPES.add(newScope);
	}
	
	/**
	 * Loads item resources from the parts.xml config document.
	 *
	 * @param itemResourceDoc the configuration XML document.
	 * @throws Exception if error loading item resources.
	 */
	private synchronized void loadItemResources(Document itemResourceDoc) {
//		if (partSet != null) {
//			// just in case if another thread is being created
//			return;
//		}

		// First build a standard scope set for scope comparison
		createStandardScope();
		
		// Build the global list in a temp to avoid access before it is built
//		Set<Part> newPartSet = new TreeSet<>();
		
		Element root = itemResourceDoc.getRootElement();
		List<Element> partNodes = root.getChildren(PART);
		for (Element partElement : partNodes) {
			nextID++;
			String description = "no description available.";

			// Get name.
			String name = partElement.getAttributeValue(NAME);

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
			
			GoodType goodType = GoodType.valueOf(ConfigHelper.convertToEnumName(type));
			
			if (type == null || goodType == null)
				throw new IllegalStateException(
						"PartConfig detected invalid type in parts.xml : " + type);

			// Get storable
			
//			String storableString = partElement.getAttributeValue(STORABLE);
//			boolean isStorable = Boolean.parseBoolean(storableString);
			
			Part p = null;
			
//			if (isStorable) {
//				p = new StorableItem(name, nextID, description, goodType, mass, 1);
//			}
//			else {
				p = new Part(name, nextID, description, goodType, mass, 1);
//			}

			for (Part pp: partSet) {
				if (pp.getName().equalsIgnoreCase(name))
					throw new IllegalStateException(
						"PartConfig detected an duplicated part entry in parts.xml : " + name);
			}

			// Add maintenance entities for part.
			Element entityListElement = partElement.getChild(MAINTENANCE_ENTITY_LIST);
			if (entityListElement != null) {
				List<Element> entityNodes = entityListElement.getChildren(ENTITY);
				for (Element entityElement : entityNodes) {
					String entityName = entityElement.getAttributeValue(NAME);
					boolean validName = false;
					for (String s: STANDARD_SCOPES) {
						if (s.equalsIgnoreCase(entityName) ) {
							validName = true;
							double probability = Double.parseDouble(entityElement.getAttributeValue(PROBABILITY));
							int maxNumber = Integer.parseInt(entityElement.getAttributeValue(MAX_NUMBER));
	
							MaintenanceScope newMaintenance = new MaintenanceScope(p, entityName, probability, maxNumber);
							addPartScope(entityName, newMaintenance);
						}
					}
					
					if (!validName) {
//						logger.severe(entityName + " is not being clear defined in mars-sim.");
						throw new IllegalArgumentException(entityName + " is not being clearly defined in mars-sim.");
					}	
				}
			}
			

			// Add part to newPartSet.
//			newPartSet.add(p);
			partSet.add(p);
			
		}
		
		// Assign the partSet now built
//		partSet = Collections.unmodifiableSet(newPartSet);
		
	}

	/**
	 * Adds to a list of MaintenanceScope.
	 * 
	 * @param scope
	 * @param newMaintenance
	 */
	private void addPartScope(String scope, MaintenanceScope newMaintenance) {

		String key = scope.toLowerCase();
		List<MaintenanceScope> maintenance = scopes.computeIfAbsent(key, k -> new ArrayList<>());
		maintenance.add(newMaintenance);
	}

	/**
	 * Gets the maintenance schedules for a specific scopes, e.g. type of vehicle or function.
	 * 
	 * @param scope Possible scopes
	 * @return
	 */
	public List<MaintenanceScope> getMaintenance(Collection<String> scope) {
		List<MaintenanceScope> results = new ArrayList<>();
		for (String s : scope) {
			List<MaintenanceScope> m = scopes.get(s.toLowerCase());
			if (m != null) {
				results.addAll(m);
			}
		}
		return results ;
	}

	/**
	 * Gets the maintenance schedules for a specific scopes, e.g. type of vehicle or function.
	 * Apply a filter so only a certain part is taken.
	 * 
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
