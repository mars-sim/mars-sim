/**
 * Mars Simulation Project
 * EquipmentFactory.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {

	// Cache maps.
	/** The equipment map cache. */
	private static final Map<String, Equipment> equipmentTypeCache = new ConcurrentHashMap<String, Equipment>(6);
	/** The equipment name set cache. */
	private static Set<String> equipmentNamesCache;

	/**
	 * Private constructor for static factory class.
	 */
	private EquipmentFactory() {
	}

	/**
	 * Gets a set of all equipment names.
	 * 
	 * @return set of equipment name strings.
	 */
	public static Set<String> getEquipmentNames() {

		if (equipmentNamesCache == null) {
			equipmentNamesCache = EquipmentType.getNameSet();
		}

		return equipmentNamesCache;

	}

	/**
	 * Gets the equipment object
	 * 
	 * @param id		the equipment resource id.
	 * @param location 	the location of the equipment.
	 * @param temp 		is this equipment only temporary?
	 * @return {@link Equipment}
	 */
	public static Equipment createEquipment(int id, Coordinates location, boolean temp) {
		return createEquipment(EquipmentType.convertID2Enum(id).getName(), location, temp);
	}

	/**
	 * Gets an equipment instance from an equipment type string.
	 * 
	 * @param type     the equipment type string.
	 * @param location the location of the equipment.
	 * @param temp     is this equipment only temporary?
	 * @return {@link Equipment}
	 * @throws Exception if error creating equipment instance.
	 */
	public static Equipment createEquipment(String type, Coordinates location, boolean temp) {
		if (temp) {
			if (equipmentTypeCache.containsKey(type))
				// since it's temporary, it doesn't matter if the location has been defined
				return equipmentTypeCache.get(type);
			else {
				Equipment equipment = createEquipment(type, location, false);
				equipmentTypeCache.put(type, equipment);
				return equipment;
			}
		}
		
		Equipment newEqm = null;
		if (Bag.TYPE.equalsIgnoreCase(type))
			newEqm =  new Bag(location);
		else if (Barrel.TYPE.equalsIgnoreCase(type))
			newEqm =  new Barrel(location);
		else if (EVASuit.TYPE.equalsIgnoreCase(type))
			newEqm =  new EVASuit(location);
		else if (GasCanister.TYPE.equalsIgnoreCase(type))
			newEqm =  new GasCanister(location);
		else if (LargeBag.TYPE.equalsIgnoreCase(type))
			newEqm =  new LargeBag(location);
		else if (SpecimenBox.TYPE.equalsIgnoreCase(type))
			newEqm =  new SpecimenBox(location);
		else
			throw new IllegalStateException("Equipment: " + type + " could not be constructed.");
		
		return newEqm;
	}

	/**
	 * Gets an equipment instance from an equipment class.
	 * 
	 * @param equipmentClass the equipment class to use.
	 * @param location       the location of the equipment.
	 * @param temp           is this equipment only temporary?
	 * @return  {@link Equipment}
	 * @throws Exception if error creating equipment instance.
	 */
	public static Equipment createEquipment(Class<? extends Equipment> equipmentClass, Coordinates location,
			boolean temp) {
		return createEquipment(EquipmentType.convertClass2Type(equipmentClass).getName(), location, temp);
	}

	/**
	 * Gets the class of equipment.
	 * 
	 * @param type the equipment type string.
	 * @return the equipment class.
	 * @throws Exception if equipment class could not be found.
	 */
	public static Class<? extends Equipment> getEquipmentClass(String type) {
		if (Bag.TYPE.equalsIgnoreCase(type))
			return Bag.class;
		else if (Barrel.TYPE.equalsIgnoreCase(type))
			return Barrel.class;
		else if (EVASuit.TYPE.equalsIgnoreCase(type))
			return EVASuit.class;
		else if (GasCanister.TYPE.equalsIgnoreCase(type))
			return GasCanister.class;
		else if (LargeBag.TYPE.equalsIgnoreCase(type))
			return LargeBag.class;
		else if (SpecimenBox.TYPE.equalsIgnoreCase(type))
			return SpecimenBox.class;
		else
			throw new IllegalStateException("Class for equipment: " + type + " could not be found.");
	}

	/**
	 * Gets the equipment class with its resource id
	 * 
	 * @param id  	resource id.
	 * @return the 	equipment class.
	 */
	public static Class<? extends Equipment> getEquipmentClass(int id) {
		return getEquipmentClass(EquipmentType.convertID2Enum(id).getName());
	}

	/**
	 * Gets the empty mass of the equipment.
	 * 
	 * @param type the equipment type string.
	 * @return empty mass (kg).
	 * @throws Exception if equipment mass could not be determined.
	 */
	public static double getEquipmentMass(String type) {
		if (Bag.TYPE.equalsIgnoreCase(type))
			return Bag.EMPTY_MASS;
		else if (Barrel.TYPE.equalsIgnoreCase(type))
			return Barrel.EMPTY_MASS;
		else if (EVASuit.TYPE.equalsIgnoreCase(type))
			return EVASuit.emptyMass;
		else if (GasCanister.TYPE.equalsIgnoreCase(type))
			return GasCanister.EMPTY_MASS;
		else if (LargeBag.TYPE.equalsIgnoreCase(type))
			return LargeBag.EMPTY_MASS;
		else if (SpecimenBox.TYPE.equalsIgnoreCase(type))
			return SpecimenBox.EMPTY_MASS;
		else
			throw new IllegalStateException("Class for equipment: " + type + " could not be found.");
	}
}
