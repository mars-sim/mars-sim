/**
 * Mars Simulation Project
 * ReturnLightUtilityVehicle.java
 * @version 3.03 2012-10-05
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/** 
 * A task for returning a light utility vehicle (LUV) to a rover or settlement
 * when a person finds themselves operating one.
 */
public class ReturnLightUtilityVehicle extends Task implements Serializable {

    private static Logger logger = Logger.getLogger(ReturnLightUtilityVehicle.class.getName());
    
    // Task phase
    private static final String RETURN_LUV = "Returning Light Utility Vehicle";

    // Static members
    private static final double STRESS_MODIFIER = .5D; // The stress modified per millisol.
    
    // Data members.
    LightUtilityVehicle luv = null;
    Unit returnContainer = null;
    
    /**
     * Constructor
     * @param person the person starting the task.
     */
    public ReturnLightUtilityVehicle(Person person) {
        super("Returning light utility vehicle", person, false, false, STRESS_MODIFIER, false, 0D);
        
        Vehicle personVehicle = person.getVehicle();
        if ((personVehicle != null) && (personVehicle instanceof LightUtilityVehicle)) {
            luv = (LightUtilityVehicle) personVehicle;
        }
        else {
            endTask();
            logger.severe(person.getName() + " is not in a light utility vehicle.");
        }
        
        // Return container may be settlement or rover.
        returnContainer = null;
        
        // Attempt to determine return container based on mission.
        Mission mission = person.getMind().getMission();
        if (mission != null) {
            if (mission instanceof RoverMission) {
                RoverMission roverMission = (RoverMission) mission;
                returnContainer = roverMission.getRover();
            }
            else {
                returnContainer = mission.getAssociatedSettlement();
            }
        }
        
        // If returnContainer hasn't been found, look for local settlement.
        if (returnContainer == null) {
            Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
            while (i.hasNext()) {
                Settlement settlement = i.next();
                if (person.getCoordinates().equals(settlement.getCoordinates())) {
                    returnContainer = settlement;
                    break;
                }
            }
        }
        
        // If returnContainer hasn't been found, look for local rover.
        if (returnContainer == null) {
            Iterator<Vehicle> i = Simulation.instance().getUnitManager().getVehicles().iterator();
            while (i.hasNext()) {
                Vehicle vehicle = i.next();
                if (vehicle instanceof Rover) {
                    returnContainer = vehicle;
                    break;
                }
            }
        }
        
        // Initialize task phase
        addPhase(RETURN_LUV);
        setPhase(RETURN_LUV);
        
        // If returnContainer still hasn't been found, end task.
        if (returnContainer == null) {
            endTask();
            logger.severe(person.getName() + " cannot find a settlement or rover to return light utility vehicle.");
        }
        else {
            setDescription("Returning " + luv.getName() + " to " + returnContainer.getName());
            logger.fine(person.getName() + " is starting to return light utility vehicle: " + luv.getName() + 
                    " to " + returnContainer.getName());
        }
    }
    
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.INVEHICLE)) {
            
            if (person.getVehicle() instanceof LightUtilityVehicle) {
                result = 500D;
                //System.out.println(person.getName() + " is in light utility vehicle!");
            }
        }

        return result;
    }
    
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        
        if (RETURN_LUV.equals(getPhase())) {
            return returnLUVPhase(time);
        }
        else {
            return time;
        }
    }
    
    /**
     * Perform the return LUV phase.
     * @param time the time to perform the task phase.
     * @return remaining time after performing the phase.
     */
    private double returnLUVPhase(double time) {
        
        // Remove person from light utility vehicle.
        luv.getInventory().retrieveUnit(person);
        luv.setOperator(null);
        
        // If not in a mission, return vehicle and unload attachment parts.
        Mission mission = person.getMind().getMission();
        if (mission == null) {
            // Put light utility vehicle in return container.
            if (returnContainer.getInventory().canStoreUnit(luv, false)) {
                returnContainer.getInventory().storeUnit(luv);
            }
            else {
                logger.severe("Light utility vehicle: " + luv.getName() + " could not be stored in " + returnContainer.getName());
            }
        
            // Unload any attachment parts or inventory from light utility vehicle.
            unloadLUVInventory();
        }
        
        endTask();
        
        return time;
    }
    
    /**
     * Unload all attachment parts and inventory from light utility vehicle.
     */
    private void unloadLUVInventory() {
        
        Inventory luvInv = luv.getInventory();
        Inventory rcInv = returnContainer.getInventory();
        
        // Unload all units.
        Iterator<Unit> j = luvInv.getContainedUnits().iterator();
        while (j.hasNext()) {
            Unit unit = j.next();
            if (rcInv.canStoreUnit(unit, false)) {
                luvInv.retrieveUnit(unit);
                rcInv.storeUnit(unit);
            }
            else {
                logger.severe(unit.getName() + " cannot be stored in " + returnContainer.getName());
            }
        }
        
        // Unload all parts.
        Iterator<ItemResource> i = luvInv.getAllItemResourcesStored().iterator();
        while (i.hasNext()) {
            ItemResource item = i.next();
            int num = luvInv.getItemResourceNum(item);
            double mass = item.getMassPerItem() * num;
            if (rcInv.getRemainingGeneralCapacity(false) >= mass) {
                luvInv.retrieveItemResources(item, num);
                rcInv.storeItemResources(item, num);
            }
            else {
                logger.severe(item.getName() + " numbered " + num + " cannot be stored in " + returnContainer.getName() + 
                        " due to insufficient remaining general capacity.");
            }
        }
        
        // Unload all amount resources.
        Iterator<AmountResource> k = luvInv.getAllAmountResourcesStored(false).iterator();
        while (k.hasNext()) {
            AmountResource resource = k.next();
            double amount = luvInv.getAmountResourceStored(resource, false);
            if (rcInv.hasAmountResourceCapacity(resource, amount, false)) {
                luvInv.retrieveAmountResource(resource, amount);
                rcInv.storeAmountResource(resource, amount, true);
            }
            else {
                logger.severe(resource.getName() + " of amount " + amount + " kg. cannot be stored in " + returnContainer.getName());
            }
        }
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<String> getAssociatedSkills() {
        return new ArrayList<String>(0);
    }

    @Override
    protected void addExperience(double time) {
        // Do nothing
    }
}