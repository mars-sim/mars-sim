/**
 * Mars Simulation Project
 * Manufacture.java
 * @version 3.07 2014-10-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.manufacture.PartSalvage;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageProcess;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Type;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A building function for manufacturing.
 */
public class Manufacture
extends Function
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(Manufacture.class.getName());

    private static final BuildingFunction FUNCTION = BuildingFunction.MANUFACTURE;

    private static final double PROCESS_MAX_VALUE = 100D;

    // Data members.
    private int techLevel;
    private int concurrentProcesses;
    private List<ManufactureProcess> processes;
    private List<SalvageProcess> salvages;

    /**
     * Constructor.
     * @param building the building the function is for.
     * @throws BuildingException if error constructing function.
     */
    public Manufacture(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        techLevel = config.getManufactureTechLevel(building.getName());
        concurrentProcesses = config.getManufactureConcurrentProcesses(building.getName());

        // Load activity spots
        loadActivitySpots(config.getManufactureActivitySpots(building.getName()));

        processes = new ArrayList<ManufactureProcess>();
        salvages = new ArrayList<SalvageProcess>();
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        double result = 0D;

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        int buildingTech = config.getManufactureTechLevel(buildingName);

        double demand = 0D;
        Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
        while (i.hasNext()) {
            demand += i.next().getMind().getSkillManager().getSkillLevel(SkillType.MATERIALS_SCIENCE);
        }

        double supply = 0D;
        int highestExistingTechLevel = 0;
        boolean removedBuilding = false;
        Iterator<Building> j = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (j.hasNext()) {
            Building building = j.next();
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                Manufacture manFunction = (Manufacture) building.getFunction(FUNCTION);
                int tech = manFunction.techLevel;
                double processes = manFunction.concurrentProcesses;
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += (tech * tech) * processes * wearModifier;

                if (tech > highestExistingTechLevel) {
                    highestExistingTechLevel = tech;
                }
            }
        }

        double baseManufactureValue = demand / (supply + 1D);

        double processes = config.getManufactureConcurrentProcesses(buildingName);
        double manufactureValue = (buildingTech * buildingTech) * processes;

        result = manufactureValue * baseManufactureValue;

        // If building has higher tech level than other buildings at settlement, 
        // add difference between best manufacturing processes.
        if (buildingTech > highestExistingTechLevel) {
            double bestExistingProcessValue = 0D;
            if (highestExistingTechLevel > 0D) {
                bestExistingProcessValue = getBestManufacturingProcessValue(highestExistingTechLevel, settlement);
            }
            double bestBuildingProcessValue = getBestManufacturingProcessValue(buildingTech, settlement);
            double processValueDiff = bestBuildingProcessValue - bestExistingProcessValue;

            if (processValueDiff < 0D) {
                processValueDiff = 0D;
            }

            if (processValueDiff > PROCESS_MAX_VALUE) {
                processValueDiff = PROCESS_MAX_VALUE;
            }

            result += processValueDiff;
        }

        return result;
    }

    /**
     * Gets the best manufacturing process value for a given manufacturing tech level at a settlement.
     * @param techLevel the manufacturing tech level.
     * @param settlement the settlement
     * @return best manufacturing process value.
     */
    private static double getBestManufacturingProcessValue(int techLevel, Settlement settlement) {

        double result = 0D;

        Iterator<ManufactureProcessInfo> i = ManufactureUtil.getAllManufactureProcesses().iterator();
        while (i.hasNext()) {
            ManufactureProcessInfo process = i.next();
            if (process.getTechLevelRequired() <= techLevel) {
                double value = ManufactureUtil.getManufactureProcessValue(process, settlement);
                if (value > result) {
                    result = value;
                }
            }
        }

        return result;
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
     * Gets the total manufacturing and salvage processes currently in this building.
     * @return total process number.
     */
    public int getTotalProcessNumber() {
        return processes.size() + salvages.size();
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
    public void addProcess(ManufactureProcess process) {
        if (process == null) {
            throw new IllegalArgumentException("process is null");
        }
        if (getTotalProcessNumber() >= concurrentProcesses) {
            throw new IllegalStateException("No space to add new manufacturing process.");
        }
        processes.add(process);

        // Consume inputs.
        Inventory inv = getBuilding().getInventory();
        for (ManufactureProcessItem item : process.getInfo().getInputList()) {
            if (Type.AMOUNT_RESOURCE.equals(item.getType())) {
                AmountResource resource = AmountResource.findAmountResource(item.getName());
                inv.retrieveAmountResource(resource, item.getAmount());
            }
            else if (Type.PART.equals(item.getType())) {
                Part part = (Part) ItemResource.findItemResource(item.getName());
                inv.retrieveItemResources(part, (int) item.getAmount());
            }
            else throw new IllegalStateException(
                    "Manufacture process input: " +
                            item.getType() +
                            " not a valid type."
                    );

            // Recalculate settlement good value for input item.
            GoodsManager goodsManager = getBuilding().getBuildingManager().getSettlement().getGoodsManager();
            goodsManager.updateGoodValue(ManufactureUtil.getGood(item), false);
        }


        // Log manufacturing process starting.
        if (logger.isLoggable(Level.FINEST)) {
            Settlement settlement = getBuilding().getBuildingManager().getSettlement();
            logger.finest(
                    getBuilding() + " at " 
                            + settlement
                            + " starting manufacturing process: " 
                            + process.getInfo().getName()
                    );
        }
    }

    /**
     * Gets a list of the current salvage processes.
     * @return unmodifiable list of salvage processes.
     */
    public List<SalvageProcess> getSalvageProcesses() {
        return Collections.unmodifiableList(salvages);
    }

    /**
     * Adds a new salvage process to the building.
     * @param process the new salvage process.
     * @throws BuildingException if error adding process.
     */
    public void addSalvageProcess(SalvageProcess process) {
        if (process == null) throw new IllegalArgumentException("process is null");

        if (getTotalProcessNumber() >= concurrentProcesses) 
            throw new IllegalStateException("No space to add new salvage process.");

        salvages.add(process);


        // Retrieve salvaged unit from inventory and remove from unit manager.
        Inventory inv = getBuilding().getInventory();
        Unit salvagedUnit = process.getSalvagedUnit();
        if (salvagedUnit != null) {
            inv.retrieveUnit(salvagedUnit);
        }
        else throw new IllegalStateException("Salvaged unit is null");

        // Set the salvage process info for the salvaged unit.
        Settlement settlement = getBuilding().getBuildingManager().getSettlement();
        ((Salvagable) salvagedUnit).startSalvage(process.getInfo(), settlement);

        // Recalculate settlement good value for salvaged unit.
        GoodsManager goodsManager = settlement.getGoodsManager();
        Good salvagedGood = null;
        if (salvagedUnit instanceof Equipment) {
            salvagedGood = GoodsUtil.getEquipmentGood(salvagedUnit.getClass());
        }
        else if (salvagedUnit instanceof Vehicle) {
            salvagedGood = GoodsUtil.getVehicleGood(salvagedUnit.getDescription());
        }
        if (salvagedGood != null) goodsManager.updateGoodValue(salvagedGood, false);
        else throw new IllegalStateException("Salvaged good is null");

        // Log salvage process starting.
        if (logger.isLoggable(Level.FINEST)) {
            Settlement stl = getBuilding().getBuildingManager().getSettlement();
            logger.finest(getBuilding() + " at " 
                    + stl
                    + " starting salvage process: " 
                    + process.toString());
        }
    }

    @Override
    public double getFullPowerRequired() {
        double result = 0D;
        Iterator<ManufactureProcess> i = processes.iterator();
        while (i.hasNext()) {
            ManufactureProcess process = i.next();
            if (process.getProcessTimeRemaining() > 0D)
                result += process.getInfo().getPowerRequired();
        }
        return result;
    }

    @Override
    public double getPowerDownPowerRequired() {
        double result = 0D;
        Iterator<ManufactureProcess> i = processes.iterator();
        while (i.hasNext()) {
            ManufactureProcess process = i.next();
            if (process.getProcessTimeRemaining() > 0D)
                result += process.getInfo().getPowerRequired();
        }
        return result;
    }

    @Override
    public void timePassing(double time) {

        List<ManufactureProcess> finishedProcesses = new ArrayList<ManufactureProcess>();

        Iterator<ManufactureProcess> i = processes.iterator();
        while (i.hasNext()) {
            ManufactureProcess process = i.next();
            process.addProcessTime(time);

            if ((process.getProcessTimeRemaining() == 0D) && 
                    (process.getWorkTimeRemaining() == 0D)) {
                finishedProcesses.add(process);
            }
        }

        // End all processes that are done.
        Iterator<ManufactureProcess> j = finishedProcesses.iterator();
        while (j.hasNext()) {
            endManufacturingProcess(j.next(), false);
        }
    }

    /**
     * Checks if manufacturing function currently requires manufacturing work.
     * @param skill the person's materials science skill level.
     * @return true if manufacturing work.
     */
    public boolean requiresManufacturingWork(int skill) {
        boolean result = false;

        if (concurrentProcesses > getTotalProcessNumber()) result = true;
        else {
            Iterator<ManufactureProcess> i = processes.iterator();
            while (i.hasNext()) {
                ManufactureProcess process = i.next();
                boolean workRequired = (process.getWorkTimeRemaining() > 0D);
                boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
                if (workRequired && skillRequired) result = true;
            }
        }

        return result;
    }

    /**
     * Checks if manufacturing function currently requires salvaging work.
     * @param skill the person's materials science skill level.
     * @return true if manufacturing work.
     */
    public boolean requiresSalvagingWork(int skill) {
        boolean result = false;

        if (concurrentProcesses > getTotalProcessNumber()) result = true;
        else {
            Iterator<SalvageProcess> i = salvages.iterator();
            while (i.hasNext()) {
                SalvageProcess process = i.next();
                boolean workRequired = (process.getWorkTimeRemaining() > 0D);
                boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
                if (workRequired && skillRequired) result = true;
            }
        }

        return result;
    }

    /**
     * Ends a manufacturing process.
     * @param process the process to end.
     * @param premature true if the process has ended prematurely.
     * @throws BuildingException if error ending process.
     */
    public void endManufacturingProcess(ManufactureProcess process, boolean premature) {

        if (!premature) {
            // Produce outputs.
            Settlement settlement = getBuilding().getBuildingManager().getSettlement();
            UnitManager manager = Simulation.instance().getUnitManager();
            Inventory inv = getBuilding().getInventory();

            Iterator<ManufactureProcessItem> j = process.getInfo().getOutputList().iterator();
            while (j.hasNext()) {
                ManufactureProcessItem item = j.next();
                if (ManufactureUtil.getManufactureProcessItemValue(item, settlement, true) > 0D) {
                    if (Type.AMOUNT_RESOURCE.equals(item.getType())) {
                        // Produce amount resources.
                        AmountResource resource = AmountResource.findAmountResource(item.getName());
                        double amount = item.getAmount();
                        double capacity = inv.getAmountResourceRemainingCapacity(resource, true, false);
                        if (item.getAmount() > capacity) {
                            double overAmount = item.getAmount() - capacity;
                            logger.fine("Not enough storage capacity to store " + overAmount + " of " + 
                                    item.getName() + " from " + process.getInfo().getName() + " at " + 
                                    settlement.getName());
                            amount = capacity;
                        }
                        inv.storeAmountResource(resource, amount, true);
                    }
                    else if (Type.PART.equals(item.getType())) {
                        // Produce parts.
                        Part part = (Part) ItemResource.findItemResource(item.getName());
                        double mass = item.getAmount() * part.getMassPerItem();
                        double capacity = inv.getGeneralCapacity();
                        if (mass <= capacity) {
                            inv.storeItemResources(part, (int) item.getAmount());
                        }
                    }
                    else if (Type.EQUIPMENT.equals(item.getType())) {
                        // Produce equipment.
                        String equipmentType = item.getName();
                        int number = (int) item.getAmount();
                        for (int x = 0; x < number; x++) {
                            Equipment equipment = EquipmentFactory.getEquipment(equipmentType, settlement.getCoordinates(), false);
                            equipment.setName(manager.getNewName(UnitType.EQUIPMENT, equipmentType, null));
                            inv.storeUnit(equipment);
                        }
                    }
                    else if (Type.VEHICLE.equals(item.getType())) {
                        // Produce vehicles.
                        String vehicleType = item.getName();
                        int number = (int) item.getAmount();
                        for (int x = 0; x < number; x++) {
                            if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
                                String name = manager.getNewName(UnitType.VEHICLE, "LUV", null);
                                manager.addUnit(new LightUtilityVehicle(name, vehicleType, settlement));
                            }
                            else {
                                String name = manager.getNewName(UnitType.VEHICLE, null, null);
                                manager.addUnit(new Rover(name, vehicleType, settlement));
                            }
                        }
                    }
                    else throw new IllegalStateException("Manufacture.addProcess(): output: " +
                            item.getType() + " not a valid type.");

                    // Recalculate settlement good value for output item.
                    GoodsManager goodsManager = getBuilding().getBuildingManager().getSettlement().getGoodsManager();
                    goodsManager.updateGoodValue(ManufactureUtil.getGood(item), false);
                }
            }
        }
        else {

            // Premature end of process.  Return all input materials.
            Settlement settlement = getBuilding().getBuildingManager().getSettlement();
            UnitManager manager = Simulation.instance().getUnitManager();
            Inventory inv = getBuilding().getInventory();

            Iterator<ManufactureProcessItem> j = process.getInfo().getInputList().iterator();
            while (j.hasNext()) {
                ManufactureProcessItem item = j.next();
                if (ManufactureUtil.getManufactureProcessItemValue(item, settlement, false) > 0D) {
                    if (Type.AMOUNT_RESOURCE.equals(item.getType())) {
                        // Produce amount resources.
                        AmountResource resource = AmountResource.findAmountResource(item.getName());
                        double amount = item.getAmount();
                        double capacity = inv.getAmountResourceRemainingCapacity(resource, true, false);
                        if (item.getAmount() > capacity) {
                            double overAmount = item.getAmount() - capacity;
                            logger.severe("Not enough storage capacity to store " + overAmount + " of " + 
                                    item.getName() + " from " + process.getInfo().getName() + " at " + 
                                    settlement.getName());
                            amount = capacity;
                        }
                        inv.storeAmountResource(resource, amount, true);
                    }
                    else if (Type.PART.equals(item.getType())) {
                        // Produce parts.
                        Part part = (Part) ItemResource.findItemResource(item.getName());
                        double mass = item.getAmount() * part.getMassPerItem();
                        double capacity = inv.getGeneralCapacity();
                        if (mass <= capacity) {
                            inv.storeItemResources(part, (int) item.getAmount());
                        }
                    }
                    else if (Type.EQUIPMENT.equals(item.getType())) {
                        // Produce equipment.
                        String equipmentType = item.getName();
                        int number = (int) item.getAmount();
                        for (int x = 0; x < number; x++) {
                            Equipment equipment = EquipmentFactory.getEquipment(equipmentType, settlement.getCoordinates(), false);
                            equipment.setName(manager.getNewName(UnitType.EQUIPMENT, equipmentType, null));
                            inv.storeUnit(equipment);
                        }
                    }
                    else if (Type.VEHICLE.equals(item.getType())) {
                        // Produce vehicles.
                        String vehicleType = item.getName();
                        int number = (int) item.getAmount();
                        for (int x = 0; x < number; x++) {
                            if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
                                String name = manager.getNewName(UnitType.VEHICLE, "LUV", null);
                                manager.addUnit(new LightUtilityVehicle(name, vehicleType, settlement));
                            }
                            else {
                                String name = manager.getNewName(UnitType.VEHICLE, null, null);
                                manager.addUnit(new Rover(name, vehicleType, settlement));
                            }
                        }
                    }
                    else throw new IllegalStateException("Manufacture.addProcess(): output: " +
                            item.getType() + " not a valid type.");

                    // Recalculate settlement good value for output item.
                    GoodsManager goodsManager = getBuilding().getBuildingManager().getSettlement().getGoodsManager();
                    goodsManager.updateGoodValue(ManufactureUtil.getGood(item), false);
                }
            }
        }

        processes.remove(process);

        // Log process ending.
        if (logger.isLoggable(Level.FINEST)) { 
            Settlement settlement = getBuilding().getBuildingManager().getSettlement();
            logger.finest(getBuilding() + " at " + settlement + " ending manufacturing process: " + 
                    process.getInfo().getName());
        }
    }

    /**
     * Ends a salvage process.
     * @param process the process to end.
     * @param premature true if process is ended prematurely.
     * @throws BuildingException if error ending process.
     */
    public void endSalvageProcess(SalvageProcess process, boolean premature) {

        Map<Part, Integer> partsSalvaged = new HashMap<Part, Integer>(0);

        if (!premature) {    
            // Produce salvaged parts.
            Settlement settlement = getBuilding().getBuildingManager().getSettlement();
            GoodsManager goodsManager = settlement.getGoodsManager();
            Inventory inv = getBuilding().getInventory();

            // Determine the salvage chance based on the wear condition of the item.
            double salvageChance = 50D;
            Unit salvagedUnit = process.getSalvagedUnit();
            if (salvagedUnit instanceof Malfunctionable) {
                Malfunctionable malfunctionable = (Malfunctionable) salvagedUnit;
                double wearCondition = malfunctionable.getMalfunctionManager().getWearCondition();
                salvageChance = (wearCondition * .25D) + 25D;
            }

            // Add the average material science skill of the salvagers.
            salvageChance += process.getAverageSkillLevel() * 5D;

            // Salvage parts.
            List<PartSalvage> partsToSalvage = process.getInfo().getPartSalvageList();
            Iterator<PartSalvage> i = partsToSalvage.iterator();
            while (i.hasNext()) {
                PartSalvage partSalvage = i.next();
                Part part = (Part) ItemResource.findItemResource(partSalvage.getName());

                int totalNumber = 0;
                for (int x = 0; x < partSalvage.getNumber(); x++) {
                    if (RandomUtil.lessThanRandPercent(salvageChance)) totalNumber++;
                }

                if (totalNumber > 0) {
                    partsSalvaged.put(part, totalNumber);

                    double mass = totalNumber * part.getMassPerItem();
                    double capacity = inv.getGeneralCapacity();
                    if (mass <= capacity) inv.storeItemResources(part, totalNumber);

                    // Recalculate settlement good value for salvaged part.
                    goodsManager.updateGoodValue(GoodsUtil.getResourceGood(part), false);
                }
            }
        }

        // Finish the salvage.
        ((Salvagable) process.getSalvagedUnit()).getSalvageInfo().finishSalvage(partsSalvaged);

        salvages.remove(process);

        // Log salvage process ending.
        if (logger.isLoggable(Level.FINEST)) { 
            Settlement settlement = getBuilding().getBuildingManager().getSettlement();
            logger.finest(getBuilding() + " at " + settlement + " ending salvage process: " + 
                    process.toString());
        }
    }

    @Override
    public double getMaintenanceTime() {
        double result = 0D;

        // Add maintenance for tech level.
        result += techLevel * 10D;

        // Add maintenance for concurrect process capacity.
        result += concurrentProcesses * 10D;

        return result;
    }

    @Override
    public void destroy() {
        super.destroy();

        Iterator<ManufactureProcess> i = processes.iterator();
        while (i.hasNext()) {
            i.next().destroy();
        }

        Iterator<SalvageProcess> j = salvages.iterator();
        while (j.hasNext()) {
            j.next().destroy();
        }
    }
}