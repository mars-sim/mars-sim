/*
 * Mars Simulation Project
 * PartConfig.java
 * @date 2025-09-02
 * @author Scott Davis
 */
package com.mars_sim.core.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.SystemType;
import com.mars_sim.core.building.utility.heating.HeatSourceType;
import com.mars_sim.core.building.utility.power.PowerSourceType;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.goods.GoodType;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.VehicleType;


/**
 * Provides configuration information about parts. Uses a DOM document to get
 * the information.
 */
public final class PartConfig  {

	// Element names
	private static final String PART = "part";
	private static final String DESCRIPTION = "description";
	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String MASS = "mass";
	private static final String MAINTENANCE_ENTITY_LIST = "maintenance-entity-list";
	private static final String ENTITY = "entity";
	private static final String PROBABILITY = "probability";
	private static final String MAX_NUMBER = "max-number";

	/** The next global part ID. */
	private int nextID;
	
	/** The set of parts. */
	private Set<Part> partSet;
	/** The map of maintenance scopes. */
	private Map<String, List<MaintenanceScope>> scopeMap = new HashMap<>();
	/** The collection of part scopes (as defined for each part in parts.xml. */
	private Set<String> partScopesRegistry = new TreeSet<>();
	
	private Map<Collection<String>, List<MaintenanceScope>> scopeCollection = new HashMap<>();
	
	/**
	 * Constructor.
	 *
	 * @param itemResourceDoc the item resource XML document.
	 * @throws Exception if error reading XML document
	 */
	public PartConfig(Document itemResourceDoc) {
		// Pick up from the last resource id
		nextID = ResourceUtil.FIRST_ITEM_RESOURCE_ID;

		// First build a standard scope set for scope comparison.
		createPartScopesRegistry();
		// Load all item resources from the parts.xml.
		loadItemResources(itemResourceDoc);
		// Register all the parts in ItemResourceUtil.
		ItemResourceUtil.registerParts(partSet);
	}

	/**
	 * Gets the part scopes registry.
	 */
	public Set<String> getPartScopesRegistry() {
		return partScopesRegistry;	 
	}
	
	/**
	 * Creates the part scopes registry.
	 */
	private void createPartScopesRegistry() {
		for (VehicleType type: VehicleType.values()) {		
			registerScope(type.getName());
		}
			
		for (SystemType type: SystemType.values()) {
			registerScope(type.getName());
		}
		
		for (FunctionType type: FunctionType.values()) {
			registerScope(type.getName());
		}
		
		for (PowerSourceType type: PowerSourceType.values()) {
			registerScope(type.getName());
		}
		
		for (HeatSourceType type: HeatSourceType.values()) {
			registerScope(type.getName());
		}
		
		partScopesRegistry.add(Flyer.DRONE);
	}
	
	/**
	 * Registers as a system scope.
	 *  
	 * @param name
	 */
	private void registerScope(String name) {
		if (!containsIgnoreCase(partScopesRegistry, name))
			partScopesRegistry.add(name);
	}
	
	/**
	 * Checks if a collection of string contains a string while ignoring the case.
	 * 
	 * @param col
	 * @param name
	 * @return
	 */
	private boolean containsIgnoreCase(Collection<String> col, String name) {
		return col
		.stream()
		.anyMatch(s -> s.equalsIgnoreCase(name));   
	}
	
	/**
	 * Adds a set of scopes.
	 * 
	 * @param newScopes
	 */
	public void addScopes(Set<String> newScopes) {
		partScopesRegistry.addAll(newScopes);
	}
	
	/**
	 * Adds a scope.
	 * 
	 * @param newScope
	 */
	public void addScopes(String newScope) {
		partScopesRegistry.add(newScope);
	}
	
	/**
	 * Loads item resources from the parts.xml config document.
	 *
	 * @param itemResourceDoc the configuration XML document.
	 * @throws Exception if error loading item resources.
	 */
	private synchronized void loadItemResources(Document itemResourceDoc) {
		if (partSet != null) {
			// just in case if another thread is being created
			return;
		}
		
		// Build the global list in a temp to avoid access before it is built
		Set<Part> newPartSet = new TreeSet<>();
		
		Element root = itemResourceDoc.getRootElement();
		List<Element> partNodes = root.getChildren(PART);
		for (Element partElement : partNodes) {
			nextID++;
			String description = "no description available.";

			// Get name.
			String name = partElement.getAttributeValue(NAME);

			boolean contaisName = newPartSet
					.stream()
					.anyMatch(p -> p.getName().equalsIgnoreCase(name));   
			
			if (contaisName) {
				throw new IllegalArgumentException(
						"PartConfig detected an duplicated part name entry in parts.xml: " + name);
			}
			
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

			// Get part
			Part p = new Part(name, nextID, description, goodType, mass, 1);

			// Add maintenance entities for part.
			Element entityListElement = partElement.getChild(MAINTENANCE_ENTITY_LIST);
			if (entityListElement != null) {
				List<Element> entityNodes = entityListElement.getChildren(ENTITY);
				for (Element entityElement : entityNodes) {
					String entityName = entityElement.getAttributeValue(NAME);
					
					if (!containsIgnoreCase(partScopesRegistry, entityName)) {
						throw new IllegalArgumentException("The system scope '" + entityName + "' was found in parts.xml but "
								+ "was not being defined in part scope registry.");
					}
					else {
						double probability = Double.parseDouble(entityElement.getAttributeValue(PROBABILITY));
						int maxNumber = Integer.parseInt(entityElement.getAttributeValue(MAX_NUMBER));
						addPartScope(entityName, new MaintenanceScope(p, entityName, probability, maxNumber));
					}
				}
			}

			// Add part to newPartSet.
			newPartSet.add(p);			
		}
		
		// Assign the partSet now built
		partSet = Collections.unmodifiableSet(newPartSet);	
	}

	/**
	 * Adds to a list of MaintenanceScope.
	 * 
	 * @param scope
	 * @param newMaintenance
	 */
	private void addPartScope(String scope, MaintenanceScope newMaintenance) {

		List<MaintenanceScope> maintenance = scopeMap.computeIfAbsent(scope.toLowerCase(), k -> new ArrayList<>());
		// Double-checking the size of the list : logger.info(scope + " 1. # of scopes: " + scopes.get(scope).size());
		maintenance.add(newMaintenance);
		// Double-checking the size of the list : logger.info(scope + " 2. # of scopes: " + scopes.get(scope).size());
	}

	/**
	 * Gets the maintenance scope list for a specific collection of scopes, e.g. type of vehicle or function.
	 * 
	 * @param scope Possible scopes
	 * @return
	 */
	public List<MaintenanceScope> getMaintenanceScopeList(Collection<String> scopes) {
		if (scopeCollection.containsKey(scopes)) {
			return scopeCollection.get(scopes);
		}
		else {
			List<MaintenanceScope> results = new ArrayList<>();
			for (String s : scopes) {
				List<MaintenanceScope> list = scopeMap.get(s.toLowerCase());
				if (list != null) {
					// It needs to allow duplicate MaintenanceScope objects
					results.addAll(list);
				}
			}
			scopeCollection.put(scopes, results); 
			return results ;
		}
	}

	/**
	 * Gets the maintenance schedules for scopes that contains a particular part.
	 * 
	 * @param scope Possible scopes
	 * @param part Filter to part
	 * @return
	 */
	public List<MaintenanceScope> getMaintenance(Collection<String> scopes, Part part) {
//		List<MaintenanceScope> results = new ArrayList<>();
//		for (String s : scopes) {
//			List<MaintenanceScope> list = scopeMap.get(s.toLowerCase());
//			for (MaintenanceScope m: list) {
//				if (list != null && (m.getPart().getID() == part.getID())) {
//					results.add(m);
//				}
//			}
//		}
//		return results ;
		return getMaintenanceScopeList(scopes).stream()
				.filter(m -> m.getPart().getID() == part.getID())
				.toList();
	}

	/**
	 * Gets a set of all parts.
	 *
	 * @return a set of parts.
	 */
	public Set<Part> getPartSet() {
		return partSet;
	}

	/**
	 * Find a part by name
	 * @param name
	 * @return
	 */
    public Part getPartByName(String name) {
		return partSet.stream().filter(item -> item.getName().equalsIgnoreCase(name)).findFirst()
		.orElse(null);
    }
}
