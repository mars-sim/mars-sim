/**
 * Mars Simulation Project
 * ManufactureUtil.java
 * @version 2.85 2008-11-28
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.manufacture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.equipment.EquipmentFactory;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.function.Manufacture;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.structure.goods.GoodsManager;
import org.mars_sim.msp.simulation.structure.goods.GoodsUtil;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.VehicleConfig;

/**
 * Utility class for getting manufacturing processes.
 */
public final class ManufactureUtil {
	
	/**
	 * Private constructor.
	 */
	private ManufactureUtil() {}
	
	/**
	 * Gets all manufacturing processes.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static final List<ManufactureProcessInfo> getAllManufactureProcesses() 
			throws Exception {
		ManufactureConfig config = SimulationConfig.instance().getManufactureConfiguration();
		return new ArrayList<ManufactureProcessInfo>(config.getManufactureProcessList());
	}
	
	/**
	 * Gets manufacturing processes within the capability of a tech level.
	 * @param techLevel the tech level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static final List<ManufactureProcessInfo> getManufactureProcessesForTechLevel(
			int techLevel) throws Exception {
		List<ManufactureProcessInfo> result = new ArrayList<ManufactureProcessInfo>();
		
		ManufactureConfig config = SimulationConfig.instance().getManufactureConfiguration();
		Iterator<ManufactureProcessInfo> i = config.getManufactureProcessList().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			if (process.getTechLevelRequired() <= techLevel) result.add(process);
		}
		
		return result;
	}
	
	/**
	 * Gets manufacturing processes within the capability of a tech level and a skill level.
	 * @param techLevel the tech level.
	 * @param skillLevel the skill level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static final List<ManufactureProcessInfo> getManufactureProcessesForTechSkillLevel(
			int techLevel, int skillLevel) throws Exception {
		List<ManufactureProcessInfo> result = new ArrayList<ManufactureProcessInfo>();
		
		ManufactureConfig config = SimulationConfig.instance().getManufactureConfiguration();
		Iterator<ManufactureProcessInfo> i = config.getManufactureProcessList().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			if ((process.getTechLevelRequired() <= techLevel) && 
					(process.getSkillLevelRequired() <= skillLevel)) result.add(process);
		}
		
		return result;
	}
	
	/**
	 * Gets the goods value of a manufacturing process at a settlement.
	 * @param process the manufacturing process.
	 * @param settlement the settlement.
	 * @return goods value of output goods minus input goods.
	 * @throws Exception if error determining good values.
	 */
	public static final double getManufactureProcessValue(ManufactureProcessInfo process, 
			Settlement settlement) throws Exception {
		
		double inputsValue = 0D;
		Iterator<ManufactureProcessItem> i = process.getInputList().iterator();
		while (i.hasNext()) inputsValue += getManufactureProcessItemValue(i.next(), settlement);
		
		double outputsValue = 0D;
		Iterator<ManufactureProcessItem> j = process.getOutputList().iterator();
		while (j.hasNext()) outputsValue += getManufactureProcessItemValue(j.next(), settlement);
		
        // Subtract power value.
        double hoursInMillisol = MarsClock.convertMillisolsToSeconds(1D) / 60D / 60D;
        double powerHrsRequiredPerMillisol = process.getPowerRequired() * hoursInMillisol;
        double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();
        
		return outputsValue - inputsValue - powerValue;
	}
	
	/**
	 * Gets the good value of a manufacturing process item for a settlement.
	 * @param item the manufacturing process item.
	 * @param settlement the settlement.
	 * @return good value.
	 * @throws Exception if error getting good value.
	 */
	public static final double getManufactureProcessItemValue(ManufactureProcessItem item, 
			Settlement settlement) throws Exception {
		double result = 0D;
		
		GoodsManager manager = settlement.getGoodsManager();
		
		if (item.getType().equals(ManufactureProcessItem.AMOUNT_RESOURCE)) {
			AmountResource resource = AmountResource.findAmountResource(item.getName());
			Good good = GoodsUtil.getResourceGood(resource);
			result = manager.getGoodValuePerMass(good) * item.getAmount();
		}
		else if (item.getType().equals(ManufactureProcessItem.PART)) {
			ItemResource resource = ItemResource.findItemResource(item.getName());
			Good good = GoodsUtil.getResourceGood(resource);
			result = manager.getGoodValuePerItem(good) * item.getAmount();
		}
		else if (item.getType().equals(ManufactureProcessItem.EQUIPMENT)) {
			Class equipmentClass = EquipmentFactory.getEquipmentClass(item.getName());
			Good good = GoodsUtil.getEquipmentGood(equipmentClass);
			result = manager.getGoodValuePerItem(good) * item.getAmount();
		}
		else if (item.getType().equals(ManufactureProcessItem.VEHICLE)) {
			Good good = GoodsUtil.getVehicleGood(item.getName());
			result = manager.getGoodValuePerItem(good) * item.getAmount();
		}
		else throw new Exception("Item type: " + item.getType() + " not valid.");
		
		return result;
	}

	/**
	 * Checks to see if a manufacturing process can be started at a given manufacturing building.
	 * @param process the manufacturing process to start.
	 * @param workshop the manufacturing building.
	 * @return true if process can be started.
	 * @throws Exception if error determining if process can be started.
	 */
	public static final boolean canProcessBeStarted(ManufactureProcessInfo process, 
			Manufacture workshop) throws Exception {
		boolean result = true;
		
		// Check to see if workshop is full of processes.
		if (workshop.getProcesses().size() >= workshop.getConcurrentProcesses()) result = false;
		
		// Check to see if process tech level is above workshop tech level.
		if (workshop.getTechLevel() < process.getTechLevelRequired()) result = false;
		
		Inventory inv = workshop.getBuilding().getInventory();
		
		// Check to see if process input items are available at settlement.
		if (!areProcessInputsAvailable(process, inv)) result = false;
		
		// Check to see if room for process output items at settlement.
		// if (!canProcessOutputsBeStored(process, inv)) result = false;
		
		return result;
	}
	
	/**
	 * Checks if process inputs are available in an inventory.
	 * @param process the manufacturing process.
	 * @param inv the inventory.
	 * @return true if process inputs are available.
	 * @throws Exception if error determining if process inputs are available.
	 */
	private static final boolean areProcessInputsAvailable(ManufactureProcessInfo process, Inventory inv) 
			throws Exception {
		boolean result = true;
		
		Iterator<ManufactureProcessItem> i = process.getInputList().iterator();
		while (i.hasNext()) {
			ManufactureProcessItem item = i.next();
			if (ManufactureProcessItem.AMOUNT_RESOURCE.equalsIgnoreCase(item.getType())) {
				AmountResource resource = AmountResource.findAmountResource(item.getName());
				if (inv.getAmountResourceStored(resource) < item.getAmount()) result = false;
			}
			else if (ManufactureProcessItem.PART.equalsIgnoreCase(item.getType())) {
				Part part = (Part) ItemResource.findItemResource(item.getName());
				if (inv.getItemResourceNum(part) < (int) item.getAmount()) result = false;
			}
			else throw new BuildingException("Manufacture process input: " + 
					item.getType() + " not a valid type.");
		}
		
		return result;
	}
	
	/**
	 * Checks if enough storage room for process outputs in an inventory.
	 * @param process the manufacturing process.
	 * @param inv the inventory.
	 * @return true if storage room.
	 * @throws Exception if error determining storage room for outputs.
	 */
	/*
	private static final boolean canProcessOutputsBeStored(ManufactureProcessInfo process, Inventory inv)
			throws Exception {
		boolean result = true;
		
		Iterator<ManufactureProcessItem> j = process.getOutputList().iterator();
		while (j.hasNext()) {
			ManufactureProcessItem item = j.next();
			if (ManufactureProcessItem.AMOUNT_RESOURCE.equalsIgnoreCase(item.getType())) {
				AmountResource resource = AmountResource.findAmountResource(item.getName());
				double capacity = inv.getAmountResourceRemainingCapacity(resource, true);
				if (item.getAmount() > capacity) result = false; 
			}
			else if (ManufactureProcessItem.PART.equalsIgnoreCase(item.getType())) {
				Part part = (Part) ItemResource.findItemResource(item.getName());
				double mass = item.getAmount() * part.getMassPerItem();
				double capacity = inv.getGeneralCapacity();
				if (mass > capacity) result = false;
			}
			else if (ManufactureProcessItem.EQUIPMENT.equalsIgnoreCase(item.getType())) {
				String equipmentType = item.getName();
				int number = (int) item.getAmount();
				Equipment equipment = EquipmentFactory.getEquipment(equipmentType, 
						new Coordinates(0D, 0D), true);
				double mass = equipment.getBaseMass() * number;
				double capacity = inv.getGeneralCapacity();
				if (mass > capacity) result = false;
			}
			else if (ManufactureProcessItem.VEHICLE.equalsIgnoreCase(item.getType())) {
				// Vehicles are stored outside a settlement.
			}
			else throw new BuildingException("Manufacture.addProcess(): output: " + 
					item.getType() + " not a valid type.");
		}
		
		return result;
	}
	*/
	
	/**
	 * Checks if settlement has buildings with manufacture function.
	 * @param settlement the settlement.
	 * @return true if buildings with manufacture function.
	 * @throws BuildingException if error checking for manufacturing buildings.
	 */
	public static final boolean doesSettlementHaveManufacturing(Settlement settlement) 
			throws BuildingException {
		BuildingManager manager = settlement.getBuildingManager();
        return (manager.getBuildings(Manufacture.NAME).size() > 0);
	}
	
	/**
	 * Gets the highest manufacturing tech level in a settlement.
	 * @param settlement the settlement.
	 * @return highest manufacturing tech level.
	 * @throws BuildingException if error determining highest tech level.
	 */
	public static final int getHighestManufacturingTechLevel(Settlement settlement) 
			throws BuildingException {
		int highestTechLevel = 0;
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> i = manager.getBuildings(Manufacture.NAME).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Manufacture manufacturingFunction = (Manufacture) building.getFunction(Manufacture.NAME);
    		if (manufacturingFunction.getTechLevel() > highestTechLevel) 
    			highestTechLevel = manufacturingFunction.getTechLevel();
		}
		
		return highestTechLevel;
	}

	/**
	 * Gets a good for a manufacture process item.
	 * @param item the manufacture process item.
	 * @return good
	 * @throws Exception if error determining good.
	 */
	public static Good getGood(ManufactureProcessItem item) throws Exception {
		Good result = null;
		if (ManufactureProcessItem.AMOUNT_RESOURCE.equalsIgnoreCase(item.getType())) {
			AmountResource resource = AmountResource.findAmountResource(item.getName());
			result = GoodsUtil.getResourceGood(resource);
		}
		else if (ManufactureProcessItem.PART.equalsIgnoreCase(item.getType())) {
			Part part = (Part) ItemResource.findItemResource(item.getName());
			result = GoodsUtil.getResourceGood(part);
		}
		else if (ManufactureProcessItem.EQUIPMENT.equalsIgnoreCase(item.getType())) {
			Class equipmentClass = EquipmentFactory.getEquipmentClass(item.getName());
			result = GoodsUtil.getEquipmentGood(equipmentClass);
		}
		else if (ManufactureProcessItem.VEHICLE.equalsIgnoreCase(item.getType())) {
			result = GoodsUtil.getVehicleGood(item.getName());
		}
		
		return result;
	}
    
    /**
     * Gets the mass for a manufacturing process item.
     * @param item the manufacturing process item.
     * @return mass (kg).
     * @throws Exception if error determining the mass.
     */
    public static double getMass(ManufactureProcessItem item) throws Exception {
        double mass = 0D;
        
        if (ManufactureProcessItem.AMOUNT_RESOURCE.equalsIgnoreCase(item.getType())) {
            mass = item.getAmount();
        }
        else if (ManufactureProcessItem.PART.equalsIgnoreCase(item.getType())) {
            Part part = (Part) ItemResource.findItemResource(item.getName());
            mass = item.getAmount() * part.getMassPerItem();
        }
        else if (ManufactureProcessItem.EQUIPMENT.equalsIgnoreCase(item.getType())) {
            double equipmentMass = EquipmentFactory.getEquipmentMass(item.getName());
            mass = item.getAmount() * equipmentMass;
        }
        else if (ManufactureProcessItem.VEHICLE.equalsIgnoreCase(item.getType())) {
            VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
            mass = item.getAmount() * config.getEmptyMass(item.getName());
        }
        
        return mass;
    }
}