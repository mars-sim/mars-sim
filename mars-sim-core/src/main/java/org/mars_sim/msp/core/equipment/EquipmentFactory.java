/**
 * Mars Simulation Project
 * EquipmentFactory.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.robot.Robot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {

	// Cache maps.
	private static final Map<String, Equipment> equipmentTypeCache = new HashMap<String, Equipment>(8);
	private static final Map<Class<? extends Equipment>, Equipment> equipmentClassCache =
	    new HashMap<Class<? extends Equipment>, Equipment>(8);
	private static Set<String> equipmentNamesCache;

	/**
	 * Private constructor for static factory class.
	 */
	private EquipmentFactory() {}

	/**
	 * Gets a set of all equipment names.
	 * @return set of equipment name strings.
	 */
	public static Set<String> getEquipmentNames() {
	    if (equipmentNamesCache == null) {
	        equipmentNamesCache = new HashSet<String>(8);
	        equipmentNamesCache.add(Bag.TYPE);
	        equipmentNamesCache.add(Barrel.TYPE);
	        equipmentNamesCache.add(EVASuit.TYPE);
	        equipmentNamesCache.add(GasCanister.TYPE);
	        equipmentNamesCache.add(LargeBag.TYPE);
	        equipmentNamesCache.add(SpecimenContainer.TYPE);
	        equipmentNamesCache.add(BuildingKit.TYPE);
	        equipmentNamesCache.add(Robot.TYPE);
	    }
	    return new HashSet<String>(equipmentNamesCache);
	}

	/**
	 * Gets an equipment instance from an equipment type string.
	 * @param type the equipment type string.
	 * @param location the location of the equipment.
	 * @param temp is this equipment only temporary?
	 * @return the equipment instance.
	 * @throws Exception if error creating equipment instance.
	 */
	public static Equipment getEquipment(String type, Coordinates location, boolean temp) {
		if (temp) {
			if (equipmentTypeCache.containsKey(type)) return equipmentTypeCache.get(type);
			else {
				Equipment equipment = getEquipment(type, location, false);
				equipmentTypeCache.put(type, equipment);
				return equipment;
			}
		}
		if (Bag.TYPE.equalsIgnoreCase(type)) return new Bag(location);
		else if (Barrel.TYPE.equalsIgnoreCase(type)) return new Barrel(location);
		else if (BuildingKit.TYPE.equalsIgnoreCase(type)) return new BuildingKit(location);
		else if (EVASuit.TYPE.equalsIgnoreCase(type)) return new EVASuit(location);
		else if (GasCanister.TYPE.equalsIgnoreCase(type)) return new GasCanister(location);
        else if (LargeBag.TYPE.equalsIgnoreCase(type)) return new LargeBag(location);
		else if (Robot.TYPE.equalsIgnoreCase(type)) return new Robot(null, null, null, null, location);
		else if (SpecimenContainer.TYPE.equalsIgnoreCase(type)) return new SpecimenContainer(location);
		else throw new IllegalStateException("Equipment: " + type + " could not be constructed.");
	}

	/**
	 * Gets an equipment instance from an equipment class.
	 * @param equipmentClass the equipment class to use.
	 * @param location the location of the equipment.
	 * @param temp is this equipment only temporary?
	 * @return the equipment instance.
	 * @throws Exception if error creating equipment instance.
	 */
	public static Equipment getEquipment(
		Class<? extends Equipment> equipmentClass,
		Coordinates location,
		boolean temp
	) {
		if (temp) {
			if (equipmentClassCache.containsKey(equipmentClass)) return equipmentClassCache.get(equipmentClass);
			else {
				Equipment equipment = getEquipment(equipmentClass, location, false);
				equipmentClassCache.put(equipmentClass, equipment);
				return equipment;
			}
		}
		if (Bag.class.equals(equipmentClass)) return new Bag(location);
		else if (Barrel.class.equals(equipmentClass)) return new Barrel(location);
		else if (EVASuit.class.equals(equipmentClass)) return new EVASuit(location);
		else if (GasCanister.class.equals(equipmentClass)) return new GasCanister(location);
        else if (LargeBag.class.equals(equipmentClass)) return new LargeBag(location);
		else if (SpecimenContainer.class.equals(equipmentClass)) return new SpecimenContainer(location);
		else if (BuildingKit.class.equals(equipmentClass)) return new BuildingKit(location);
		else if (Robot.class.equals(equipmentClass)) return new Robot(null, null, null, null, location);
		else throw new IllegalStateException("Equipment: " + equipmentClass + " could not be constructed.");
	}

	/**
	 * Gets the class of equipment.
	 * @param type the equipment type string.
	 * @return the equipment class.
	 * @throws Exception if equipment class could not be found.
	 */
	public static Class<? extends Equipment> getEquipmentClass(String type) {
		if (Bag.TYPE.equalsIgnoreCase(type)) return Bag.class;
		else if (Barrel.TYPE.equalsIgnoreCase(type)) return Barrel.class;
		else if (EVASuit.TYPE.equalsIgnoreCase(type)) return EVASuit.class;
		else if (GasCanister.TYPE.equalsIgnoreCase(type)) return GasCanister.class;
        else if (LargeBag.TYPE.equalsIgnoreCase(type)) return LargeBag.class;
		else if (SpecimenContainer.TYPE.equalsIgnoreCase(type)) return SpecimenContainer.class;
		else if (BuildingKit.TYPE.equalsIgnoreCase(type)) return BuildingKit.class;
		else if (Robot.TYPE.equalsIgnoreCase(type)) return Robot.class;
		else throw new IllegalStateException("Class for equipment: " + type + " could not be found.");
	}

    /**
     * Gets the empty mass of the equipment.
     * @param type the equipment type string.
     * @return empty mass (kg).
     * @throws Exception if equipment mass could not be determined.
     */
    public static double getEquipmentMass(String type) {
        if (Bag.TYPE.equalsIgnoreCase(type)) return Bag.EMPTY_MASS;
        else if (Barrel.TYPE.equalsIgnoreCase(type)) return Barrel.EMPTY_MASS;
        else if (EVASuit.TYPE.equalsIgnoreCase(type)) return EVASuit.EMPTY_MASS;
        else if (GasCanister.TYPE.equalsIgnoreCase(type)) return GasCanister.EMPTY_MASS;
        else if (LargeBag.TYPE.equalsIgnoreCase(type)) return LargeBag.EMPTY_MASS;
        else if (SpecimenContainer.TYPE.equalsIgnoreCase(type)) return SpecimenContainer.EMPTY_MASS;
        else if (BuildingKit.TYPE.equalsIgnoreCase(type)) return BuildingKit.EMPTY_MASS;
        else if (Robot.TYPE.equalsIgnoreCase(type)) return Robot.EMPTY_MASS;
        else throw new IllegalStateException("Class for equipment: " + type + " could not be found.");
    }
}