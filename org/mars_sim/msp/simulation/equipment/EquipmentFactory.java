/**
 * Mars Simulation Project
 * EquipmentFactory.java
 * @version 2.83 2008-01-20
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.simulation.Coordinates;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {
	
	// Cache maps.
	private static final Map<String, Equipment> equipmentTypeCache = new HashMap<String, Equipment>(5);
	private static final Map<Class, Equipment> equipmentClassCache = new HashMap<Class, Equipment>(5);
	
	/**
	 * Private constructor for static factory class.
	 */
	private EquipmentFactory() {}
	
	/**
	 * Gets an equipment instance from an equipment type string.
	 * @param type the equipment type string.
	 * @param location the location of the equipment.
	 * @param temp is this equipment only temporary?
	 * @return the equipment instance.
	 * @throws Exception if error creating equipment instance.
	 */
	public static Equipment getEquipment(String type, Coordinates location, boolean temp) throws Exception {
		
		if (temp) {
			if (equipmentTypeCache.containsKey(type)) return equipmentTypeCache.get(type);
			else {
				Equipment equipment = getEquipment(type, location, false);
				equipmentTypeCache.put(type, equipment);
				return equipment;
			}
		}
		
		if (Bag.TYPE.equalsIgnoreCase(type)) return new Bag(location, 50D);
		else if (Barrel.TYPE.equalsIgnoreCase(type)) return new Barrel(location, 200D);
		else if (EVASuit.TYPE.equalsIgnoreCase(type)) return new EVASuit(location);
		else if (GasCanister.TYPE.equalsIgnoreCase(type)) return new GasCanister(location, 50D);
		else if (SpecimenContainer.TYPE.equalsIgnoreCase(type)) return new SpecimenContainer(location);
		else throw new Exception("Equipment: " + type + " could not be constructed.");
	}
	
	/**
	 * Gets an equipment instance from an equipment class.
	 * @param equipmentClass the equipment class to use.
	 * @param location the location of the equipment.
	 * @param temp is this equipment only temporary?
	 * @return the equipment instance.
	 * @throws Exception if error creating equipment instance.
	 */
	public static Equipment getEquipment(Class equipmentClass, Coordinates location, boolean temp) throws Exception {
		
		if (temp) {
			if (equipmentClassCache.containsKey(equipmentClass)) return equipmentClassCache.get(equipmentClass);
			else {
				Equipment equipment = getEquipment(equipmentClass, location, false);
				equipmentClassCache.put(equipmentClass, equipment);
				return equipment;
			}
		}
		
		if (Bag.class.equals(equipmentClass)) return new Bag(location, 50D);
		else if (Barrel.class.equals(equipmentClass)) return new Barrel(location, 200D);
		else if (EVASuit.class.equals(equipmentClass)) return new EVASuit(location);
		else if (GasCanister.class.equals(equipmentClass)) return new GasCanister(location, 50D);
		else if (SpecimenContainer.class.equals(equipmentClass)) return new SpecimenContainer(location);
		else throw new Exception("Equipment: " + equipmentClass + " could not be constructed.");
	}
	
	/**
	 * Gets the class of equipment.
	 * @param type the equipment type string.
	 * @return the equipment class.
	 * @throws Exception if equipment class could not be found.
	 */
	public static Class getEquipmentClass(String type) throws Exception {
		if (Bag.TYPE.equalsIgnoreCase(type)) return Bag.class;
		else if (Barrel.TYPE.equalsIgnoreCase(type)) return Barrel.class;
		else if (EVASuit.TYPE.equalsIgnoreCase(type)) return EVASuit.class;
		else if (GasCanister.TYPE.equalsIgnoreCase(type)) return GasCanister.class;
		else if (SpecimenContainer.TYPE.equalsIgnoreCase(type)) return SpecimenContainer.class;
		else throw new Exception("Class for equipment: " + type + " could not be found.");
	}
}