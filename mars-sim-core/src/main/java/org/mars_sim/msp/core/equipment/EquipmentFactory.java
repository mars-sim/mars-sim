/*
 * Mars Simulation Project
 * EquipmentFactory.java
 * @date 2022-10-04
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.manufacture.ManufactureConfig;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {
	
	private static final String wheel = "Make wheelbarrow";
	private static final String bottle = "maufacture thermal bottle";
	private static final String largeBag = "maufacture large bag";
	private static final String bag = "maufacture bag";
	private static final String box = "Make plastic specimen box";
	private static final String barrel = "Make plastic barrel";
	private static final String canister = "Make gas canister";
	private static final String suit = "Assemble EVA suit";
	
	private static Map<String, Double> weights = new HashMap<>();
	
	private static UnitManager unitManager;
	private static SimulationConfig simulationConfig;
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
			// Store a pressure suit inside the EVA suit
//			((EVASuit)newEqm).storeItemResource(ItemResourceUtil.pressureSuitID, 1);
			break;

		case BAG:
		case BARREL:
		case GAS_CANISTER:
		case LARGE_BAG:
			newEqm = new GenericContainer(newName, type, false, settlement);
			break;
			
		case SPECIMEN_BOX:
		case THERMAL_BOTTLE:
		case WHEELBARROW:			
			// Reusable Containers
			newEqm = new GenericContainer(newName, type, true, settlement);
			break;
		default:
			throw new IllegalStateException("Equipment: " + type + " could not be constructed.");
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
	 * @param type the equipment type string.
	 * @return empty mass (kg).
	 * @throws Exception if equipment mass could not be determined.
	 */
	public static double getEquipmentMass(EquipmentType type) {
		switch (type) {
				
		case BAG:
			return calculateMass(bag);
		case BARREL:
			return calculateMass(barrel);
		case EVA_SUIT:
			return calculateMass(suit); //EVASuit.emptyMass;
		case GAS_CANISTER:
			return calculateMass(canister);
		case LARGE_BAG:
			return calculateMass(largeBag);
		case SPECIMEN_BOX:
			return calculateMass(box);
		case THERMAL_BOTTLE:
			return calculateMass(bottle);
		case WHEELBARROW:
			return calculateMass(wheel);	
		default:
			throw new IllegalStateException("Class for equipment: " + type + " could not be found.");
		}
	}

	/**
	 * Calculates the mass of the output of a process.
	 * 
	 * @param processName
	 * @return
	 */
    public static double calculateMass(String processName) {	
		if (!weights.isEmpty() || !weights.containsKey(processName)) {
			double mass = 0;
	    	ManufactureProcessInfo manufactureProcessInfo = null;
	    	
	    	for (ManufactureProcessInfo info : manufactureConfig.getManufactureProcessList()) {
	    		if (info.getName().equalsIgnoreCase(processName)) {
	    			manufactureProcessInfo = info;
	    			break;
		        }
	    	}
	
			// Calculate total mass as the summation of the multiplication of the quantity and mass of each part 
			mass = manufactureProcessInfo.calculateTotalInputMass();
			weights.put(processName, mass);
			
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
