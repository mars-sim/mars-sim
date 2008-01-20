/**
 * Mars Simulation Project
 * Manufacture.java
 * @version 2.83 2008-01-19
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.simulation.equipment.Equipment;
import org.mars_sim.msp.simulation.equipment.EquipmentFactory;
import org.mars_sim.msp.simulation.manufacture.ManufactureProcess;
import org.mars_sim.msp.simulation.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingConfig;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.vehicle.Rover;

/**
 * A building function for manufacturing.
 */
public class Manufacture extends Function implements Serializable {

	private static final String NAME = "Manufacture";
	
	// Data members.
	private int techLevel;
	private int concurrentProcesses;
	private List<ManufactureProcess> processes;
	
	/**
	 * Constructor
	 * @param building the building the function is for.
	 * @throws BuildingException if error constructing function.
	 */
	public Manufacture(Building building) throws BuildingException {
		// Use Function constructor.
		super(NAME, building);
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
		try {
			techLevel = config.getManufactureTechLevel(building.getName());
			concurrentProcesses = config.getManufactureConcurrentProcesses(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("Manufacture.constructor: " + e.getMessage());
		}
		
		processes = new ArrayList<ManufactureProcess>();
	}
	
	/**
	 * Gets the manufacturing tech level of the building.
	 * @return tech level.
	 */
	public int getTechLevel() {
		return techLevel;
	}
	
	/**
	 * Gets the maximum concurrent manufacturing processes supported by the building.
	 * @return maximum concurrent processes.
	 */
	public int getConcurrentProcesses() {
		return concurrentProcesses;
	}
	
	/**
	 * Gets a list of the current manufacturing processes.
	 * @return unmodifiable list of processes.
	 */
	public List<ManufactureProcess> getProcesses() {
		return Collections.unmodifiableList(processes);
	}
	
	/**
	 * Adds a new manufacturing process to the building.
	 * @param process the new manufacturing process.
	 * @throws BuildingException if error adding process.
	 */
	public void addProcess(ManufactureProcess process) throws BuildingException {
		if (process == null) throw new IllegalArgumentException("process is null");
		processes.add(process);
		
		// Consume inputs.
		try {
			Inventory inv = getBuilding().getInventory();
			Iterator<ManufactureProcessItem> i = process.getInfo().getInputList().iterator();
			while (i.hasNext()) {
				ManufactureProcessItem item = i.next();
				if (ManufactureProcessItem.AMOUNT_RESOURCE.equalsIgnoreCase(item.getType())) {
					AmountResource resource = AmountResource.findAmountResource(item.getName());
					inv.retrieveAmountResource(resource, item.getAmount());
				}
				else if (ManufactureProcessItem.PART.equalsIgnoreCase(item.getType())) {
					Part part = (Part) ItemResource.findItemResource(item.getName());
					inv.retrieveItemResources(part, (int) item.getAmount());
				}
				else throw new BuildingException("Manufacture process input: " + 
						item.getType() + " not a valid type.");
			}
		}
		catch (Exception e) {
			throw new BuildingException("Problem adding manufacturing process.", e);
		}
	}
	
	@Override
	public double getFullPowerRequired() {
		// TODO When we add power requirements for manufacturing processes, 
		// we can base power required on that.
		return 0;
	}

	@Override
	public double getPowerDownPowerRequired() {
		return 0;
	}

	@Override
	public void timePassing(double time) throws BuildingException {
		
		Iterator<ManufactureProcess> i = processes.iterator();
		while (i.hasNext()) {
			ManufactureProcess process = i.next();
			process.addProcessTime(time);
		
			// If process is done, produce outputs.
			if ((process.getProcessTimeRemaining() == 0D) && 
					(process.getWorkTimeRemaining() == 0D)) {
				
				// Produce outputs.
				try {
					Settlement settlement = getBuilding().getBuildingManager().getSettlement();
					UnitManager manager = Simulation.instance().getUnitManager();
					Inventory inv = getBuilding().getInventory();
					
					Iterator<ManufactureProcessItem> j = process.getInfo().getOutputList().iterator();
					while (i.hasNext()) {
						ManufactureProcessItem item = j.next();
						if (ManufactureProcessItem.AMOUNT_RESOURCE.equalsIgnoreCase(item.getType())) {
							// Produce amount resources.
							AmountResource resource = AmountResource.findAmountResource(item.getName());
							double capacity = inv.getAmountResourceRemainingCapacity(resource, true);
							if (item.getAmount() <= capacity) 
								inv.storeAmountResource(resource, item.getAmount(), true);
						}
						else if (ManufactureProcessItem.PART.equalsIgnoreCase(item.getType())) {
							// Produce parts.
							Part part = (Part) ItemResource.findItemResource(item.getName());
							double mass = item.getAmount() * part.getMassPerItem();
							double capacity = inv.getGeneralCapacity();
							if (mass <= capacity)
								inv.storeItemResources(part, (int) item.getAmount());
						}
						else if (ManufactureProcessItem.EQUIPMENT.equalsIgnoreCase(item.getType())) {
							// Produce equipment.
							String equipmentType = item.getName();
							int number = (int) item.getAmount();
							for (int x = 0; x < number; x++) {
								Equipment equipment = EquipmentFactory.getEquipment(equipmentType, settlement.getCoordinates(), false);
								equipment.setName(manager.getNewName(UnitManager.EQUIPMENT, equipmentType, null));
								inv.storeUnit(equipment);
							}
						}
						else if (ManufactureProcessItem.VEHICLE.equalsIgnoreCase(item.getType())) {
							// Produce vehicles.
							String vehicleType = item.getName();
							int number = (int) item.getAmount();
							for (int x = 0; x < number; x++) {
								String vehicleName = manager.getNewName(UnitManager.VEHICLE, null, null);
								Rover rover = new Rover(vehicleName, vehicleType, settlement);
								manager.addUnit(rover);
							}
						}
						else throw new BuildingException("Manufacture.addProcess(): input: " + 
								item.getType() + " not a valid type.");
					}
				}
				catch (Exception e) {
					throw new BuildingException("Problem completing manufacturing process.", e);
				}
				
				i.remove();
			}
		}
	}
}