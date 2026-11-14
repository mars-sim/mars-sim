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

    	double usualMass = getUsualMass(type);
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
    private static double calculateUsualMass(EquipmentType type, String processName, String productName) {	
		double massPerItem = 0;
		
		if (!massMap.containsKey(type)) {

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

	    	}
	    	else {
	    		logger.severe("manufactureProcessInfo is not ready.");
	    	}

			// Save the key value pair onto the weights Map
			massMap.put(type, massPerItem);
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

		double newMass;
		if (type == EquipmentType.EVA_SUIT) {
			newMass = EVASuit.getUsualMass();
    		massMap.put(type, newMass);
    	}
		else {
			newMass = calculateUsualMass(type, "Manufacture " + type.getName(), type.getName());
		}
		return newMass;
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
