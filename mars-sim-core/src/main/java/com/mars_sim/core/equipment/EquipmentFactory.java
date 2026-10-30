/*
 * Mars Simulation Project
 * EquipmentFactory.java
 * @date 2024-09-09
 * @author Scott Davis
 */

package com.mars_sim.core.equipment;

import java.util.EnumMap;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureConfig;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.unit.AbstractMobileUnit;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {
	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(EquipmentFactory.class.getName());
	// Default mass for uncalculated types
	protected static final double DEFAULT_MASS = 0.0001;
	
	// Base masses (kg) aligned with current manufacturing input-mass/output-quantity calculations.
	private static final EnumMap<EquipmentType, Double> massMap = new EnumMap<>(EquipmentType.class);
	
//	static {
//		massMap.put(EquipmentType.DATA_RECORDER, 0.699);
//		massMap.put(EquipmentType.EVA_SUIT, 15.285);
//		massMap.put(EquipmentType.BAG, 0.10);
//		massMap.put(EquipmentType.BARREL, 1.30);
//		massMap.put(EquipmentType.GAS_CANISTER, 4.60);
//		massMap.put(EquipmentType.LARGE_BAG, 0.20);
//		massMap.put(EquipmentType.SPECIMEN_BOX, 0.65);
//		massMap.put(EquipmentType.THERMAL_BOTTLE, 0.50);
//		massMap.put(EquipmentType.WHEELBARROW, 12.16);
//	}

	private static ManufactureConfig manufactureConfig;
	
	private static UnitManager unitManager;

	/**
	 * Private constructor for static factory class.
	 */
	private EquipmentFactory() {
	}

	/**
	 * Creates a new piece of Equipment. This may be temporary to be shared.
	 * 
	 * @param type
	 * @param settlement
	 * @param temp
	 * @return
	 */
	public static synchronized Equipment createEquipment(EquipmentType type, Settlement settlement) {
		// Create the name upfront
		String newName = Equipment.generateName(type.getName());

		Equipment newEqm = null;
		switch (type) {
		
		case DATA_RECORDER:
			newEqm = new DataRecorder(newName, settlement);
			break;
			
		case EVA_SUIT:
			newEqm = new EVASuit(newName, settlement);
			break;

		case BAG, BARREL, GAS_CANISTER, LARGE_BAG:
			newEqm = new GenericContainer(newName, type, false, settlement);
			break;
			
		case SPECIMEN_BOX, THERMAL_BOTTLE, WHEELBARROW:			
			// Reusable Containers
			newEqm = new GenericContainer(newName, type, true, settlement);
			break;
		}

    	double usualMass = 0; 
    	
    	if ((massMap.isEmpty() || !massMap.containsKey(type))) {
    		
    		if (type == EquipmentType.EVA_SUIT) {
    			usualMass = EVASuit.getUsualMass();
    		}
    		else {
	    		usualMass = calculateUsualMass(type, "Manufacture " + type.getName(), type.getName());
    		}
    		
    		massMap.put(type, usualMass);
    	}
    	else {
    		usualMass = massMap.get(type);
    	}
    	
    	if (newEqm instanceof AbstractMobileUnit amu) {
    		amu.setBaseMass(usualMass);
    	}
    	
		unitManager.addUnit(newEqm);

		// Add this equipment as being placed in this settlement
		settlement.getEquipmentInventory().addEquipment(newEqm);
		
		return newEqm;
	}

	/**
	 * Gets an equipment instance from an equipment type string.
	 *
	 * @param type     the equipment type string.
	 * @param location the location of the equipment.
	 * @return {@link Equipment}
	 * @throws Exception if error creating equipment instance.
	 */
	public static Equipment createEquipment(String type, Settlement settlement) {
		// Create a new instance of the equipment
		return createEquipment(EquipmentType.convertName2Enum(type), settlement);
	}

	/**
	 * Calculates the mass of the output of a process.
	 * 
	 * @param type
	 * @param processName
	 * @param productName
	 * @return
	 */
    public static double calculateUsualMass(EquipmentType type, String processName, String productName) {	
		double massPerItem = 0;
		
		if (massMap.isEmpty() || !massMap.containsKey(type)) {

			double mass = 0;
			double quantity = 0;
	    	ManufactureProcessInfo manufactureProcessInfo = null;

	    	if (manufactureConfig == null) {
	    		manufactureConfig = SimulationConfig.instance().getManufactureConfiguration();
	    	}
	    			
	    	for (ManufactureProcessInfo info : manufactureConfig.getManufactureProcessList()) {
	    		if (info.getName().equalsIgnoreCase(processName)) {
	    			manufactureProcessInfo = info;
	    			break;
		        }
	    	}
			
	    	if (manufactureProcessInfo != null) {
				// Calculate total mass as the summation of the multiplication of the quantity and mass of each part 
				mass = manufactureProcessInfo.calculateTotalInputMass();
				// Calculate output quantity
				quantity = manufactureProcessInfo.calculateOutputQuantity(productName);		
				
				// mass/quantity is mass per unit e.g. need 7.8 kg of polyethylene in order to make 12 baskets 
				massPerItem = mass/quantity;
				// Save the key value pair onto the weights Map
				massMap.put(type, massPerItem);
	    	}
	    	else {
	    		logger.severe("manufactureProcessInfo is not ready.");
	    	}
		}

		return massPerItem;
    }
    
	/**
	 * Gets the empty mass of the equipment.
	 *
	 * @param type the equipment type.
	 * @return empty mass (kg).
	 * @throws Exception if equipment mass could not be determined.
	 */
	public static double getUsualMass(EquipmentType type) {
		if (massMap.containsKey(type))
			return massMap.get(type);
		return DEFAULT_MASS;
	}
	
	/**
	 * Sets up the default Unit Manager to use.
	 * 
	 * @param mgr
	 */
	public static void initialise(UnitManager mgr) {
		unitManager = mgr;
	}
}
