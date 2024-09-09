/*
 * Mars Simulation Project
 * EquipmentFactory.java
 * @date 2024-09-09
 * @author Scott Davis
 */

package com.mars_sim.core.equipment;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureConfig;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.structure.Settlement;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {

	/** Default logger. */
	private static final SimLogger logger = SimLogger.getLogger(EquipmentFactory.class.getName());
	
	// Note: it's haphazard to match the string of the manu process since it can change.
	// Will need to implement a better way in matching and looking for the manu process that assemblies the item of interest.
	private static final String wheel = "Make wheelbarrow";
	private static final String bottle = "Manufacture thermal bottle";
	private static final String largeBag = "Manufacture large bag";
	private static final String bag = "Manufacture bag";
	private static final String box = "Make plastic specimen box";
	private static final String barrel = "Make plastic barrel";
	private static final String canister = "Make gas canister";
	private static final String suit = "Assemble EVA suit";
	
	private static Map<String, Double> weights = new HashMap<>();
	
	private static UnitManager unitManager;
//	private static SimulationConfig simulationConfig;
	private static ManufactureConfig manufactureConfig;

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
		case EVA_SUIT:
			newEqm = new EVASuit(newName, settlement);
			// Set description for this robot
			newEqm.setDescription("A standard EVA suit for Mars surface operation.");
			// Store a pressure suit inside the EVA suit
//			((EVASuit)newEqm).storeItemResource(ItemResourceUtil.pressureSuitID, 1);
			break;

		case BAG:
		case BARREL:
		case GAS_CANISTER:
		case LARGE_BAG:
			newEqm = new GenericContainer(newName, type, false, settlement);
			newEqm.setDescription("A standard " + type.getName().toLowerCase() + ".");
			break;
			
		case SPECIMEN_BOX:
		case THERMAL_BOTTLE:
		case WHEELBARROW:			
			// Reusable Containers
			newEqm = new GenericContainer(newName, type, true, settlement);
			newEqm.setDescription("A standard " + type.getName().toLowerCase() + ".");
			break;
		default:
			throw new IllegalStateException("Equipment type '" + type + "' could not be constructed.");
		}

		unitManager.addUnit(newEqm);
		// Add this equipment as being owned by this settlement
		settlement.addEquipment(newEqm);
		// Set the container unit
		newEqm.setContainerUnit(settlement);

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
	 * Gets the empty mass of the equipment.
	 *
	 * @param type the equipment type.
	 * @return empty mass (kg).
	 * @throws Exception if equipment mass could not be determined.
	 */
	public static double getEquipmentMass(EquipmentType type) {
		String productName = type.getName();
		
		switch (type) {				
			case BAG:
				return calculateMass(bag, productName);
			case BARREL:
				return calculateMass(barrel, productName);
			case EVA_SUIT:
				return calculateMass(suit, productName);
			case GAS_CANISTER:
				return calculateMass(canister, productName);
			case LARGE_BAG:
				return calculateMass(largeBag, productName);
			case SPECIMEN_BOX:
				return calculateMass(box, productName);
			case THERMAL_BOTTLE:
				return calculateMass(bottle, productName);
			case WHEELBARROW:
				return calculateMass(wheel, productName);	
			default:
				throw new IllegalStateException("Class for equipment type '" + type + "' could not be found.");
		}
	}

	/**
	 * Calculates the mass of the output of a process.
	 * 
	 * @param processName
	 * @return
	 */
    public static double calculateMass(String processName, String productName) {	
		if (weights.isEmpty() || !weights.containsKey(processName)) {
			double mass = 0;
			double quantity = 1;
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
				// Save the key value pair onto the weights Map
				weights.put(processName, mass/quantity);
	    	}

	    	if (mass == 0)
	    		logger.severe("The process '" + processName + "' cannot be found.");
	    		
			return mass;
		}

		return weights.get(processName);
    }
    
	/**
	 * Sets up the default Unit Manager to use.
	 * 
	 * @param mgr
	 */
	public static void initialise(UnitManager mgr, ManufactureConfig mConfig) {
		unitManager = mgr;
		manufactureConfig = mConfig;
	}
}
