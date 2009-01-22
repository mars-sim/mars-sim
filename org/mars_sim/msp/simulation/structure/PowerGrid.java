/**
 * Mars Simulation Project
 * PowerGrid.java
 * @version 2.85 2008-11-07
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure;
 
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.time.MarsClock;
 
/**
 * The PowerGrid class is a settlement's building power grid.
 */
public class PowerGrid implements Serializable {
    
    private static String CLASS_NAME = 
        "org.mars_sim.msp.simulation.structure.PowerGrid";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);
        
	// Unit update events.
	public static final String POWER_MODE_EVENT = "power mode";
	public static final String GENERATED_POWER_EVENT = "generated power";
    public static final String STORED_POWER_EVENT = "stored power";
    public static final String STORED_POWER_CAPACITY_EVENT= "stored power capacity";
	public static final String REQUIRED_POWER_EVENT = "required power";
	public static final String POWER_VALUE_EVENT = "power value";
	
    // Statc data members
    public static final String POWER_UP_MODE = "Power up";
    public static final String POWER_DOWN_MODE = "Power down";
        
    // Data members
    private String powerMode;
    private double powerGenerated;
    private double powerStored;
    private double powerStorageCapacity;
    private double powerRequired;
    private boolean sufficientPower;
    private Settlement settlement;
    private double powerValue;
    
    /**
     * Constructor
     */
    public PowerGrid(Settlement settlement) {
        this.settlement = settlement;
        powerMode = POWER_UP_MODE;
        powerGenerated = 0D;
        powerStored = 0D;
        powerStorageCapacity = 0D;
        powerRequired = 0D;
        sufficientPower = true;
    }
    
    /**
     * Gets the power grid mode.
     * @return power grid mode string.
     */
    public String getPowerMode() {
    	return powerMode;
    }
    
    /**
     * Sets the power grid mode.
     * @param newPowerMode the new power grid mode.
     */
    public void setPowerMode(String newPowerMode) {
    	if (!powerMode.equals(newPowerMode)) {
    		if (POWER_UP_MODE.equals(newPowerMode)) powerMode = POWER_UP_MODE;
    		else if (POWER_DOWN_MODE.equals(newPowerMode)) powerMode = POWER_DOWN_MODE;
    		settlement.fireUnitUpdate(POWER_MODE_EVENT);
    	}
    }
    
    /**
     * Gets the generated power in the grid.
     * @return power in kW
     */
    public double getGeneratedPower() {
        return powerGenerated;
    }
    
    /**
     * Sets the generated power in the grid.
     * @param newGeneratedPower the new generated power (kW).
     */
    private void setGeneratedPower(double newGeneratedPower) {
    	if (powerGenerated != newGeneratedPower) {
    		powerGenerated = newGeneratedPower;
    		settlement.fireUnitUpdate(GENERATED_POWER_EVENT);
    	}
    }
    
    /**
     * Gets the stored power in the grid.
     * @return stored power in kW hr.
     */
    public double getStoredPower() {
        return powerStored;
    }
    
    /**
     * Sets the stored power in the grid.
     * @param newPowerStored the new stored power (kW hr).
     */
    public void setStoredPower(double newPowerStored) {
        if (powerStored != newPowerStored) {
            powerStored = newPowerStored;
            settlement.fireUnitUpdate(STORED_POWER_EVENT);
        }
    }
    
    /**
     * Gets the stored power capacity in the grid.
     * @return stored power capacity in kW hr.
     */
    public double getStoredPowerCapacity() {
        return powerStorageCapacity;
    }
    
    /**
     * Sets the stored power capacity in the grid.
     * @param newPowerStorageCapacity the new stored power capacity (kW hr).
     */
    public void setStoredPowerCapacity(double newPowerStorageCapacity) {
        if (powerStorageCapacity != newPowerStorageCapacity) {
            powerStorageCapacity = newPowerStorageCapacity;
            settlement.fireUnitUpdate(STORED_POWER_CAPACITY_EVENT);
        }
    }
    
    /**
     * Gets the power required from the grid.
     * @return power in kW
     */
    public double getRequiredPower() {
        return powerRequired;
    }
    
    /**
     * Sets the required power in the grid.
     * @param newRequiredPower the new required power (kW).
     */
    private void setRequiredPower(double newRequiredPower) {
    	if (powerRequired != newRequiredPower) {
    		powerRequired = newRequiredPower;
    		settlement.fireUnitUpdate(REQUIRED_POWER_EVENT);
    	}
    }
    
    /**
     * Checks if there is enough power in the grid for all 
     * buildings to be set to full power.
     * @return true if sufficient power
     */
    public boolean isSufficientPower() {
        return sufficientPower;
    }
    
    /**
     * Time passing for power grid.
     *
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) throws BuildingException {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine(settlement.getName() + " power situation: ");
        }
        
        // update the total power generated in the grid.
        updateTotalPowerGenerated();
        
        // Update the total power stored in the grid.
        updateTotalStoredPower();
        
        // Update the total power storage capacity in the grid.
        updateTotalPowerStorageCapacity();
        
        // Determine total power required in the grid.
        updateTotalRequiredPower();
        
        // Check if there is enough power generated to fully supply each building.
        if (getRequiredPower() <= getGeneratedPower()) {
            sufficientPower = true;
            
            // Store excess power in power storage buildings.
            double timeHr = MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
            double excessPower = (getGeneratedPower() - getRequiredPower()) * timeHr;
            storeExcessPower(excessPower);
        }
        else {
            sufficientPower = false;
            double neededPower = getRequiredPower() - getGeneratedPower();
            
            // Retrieve power from power storage buildings.
            double timeHr = MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
            double neededPowerHr = neededPower * timeHr;
            neededPowerHr = retrieveStoredPower(neededPowerHr);
            neededPower = neededPowerHr / timeHr;
            
            BuildingManager manager = settlement.getBuildingManager();
            List<Building> buildings = manager.getBuildings();
            
            // Reduce each building's power mode to low power until 
            // required power reduction is met.
            if (!powerMode.equals(POWER_DOWN_MODE)) {
            	Iterator<Building> iLowPower = buildings.iterator();
            	while (iLowPower.hasNext() && (neededPower > 0D)) {
                	Building building = iLowPower.next();
                	if (!powerSurplus(building, Building.FULL_POWER)) {
                		building.setPowerMode(Building.POWER_DOWN);
                		neededPower -= building.getFullPowerRequired() - 
                        	building.getPoweredDownPowerRequired();
                	}
            	}
            }
            
            // If power needs are still not met, turn off the power to each 
            // uninhabitable building until required power reduction is met.
            if (neededPower > 0D) {
                Iterator<Building> iNoPower = buildings.iterator();
                while (iNoPower.hasNext() && (neededPower > 0D)) {
                    Building building = iNoPower.next();
                    if (!powerSurplus(building, Building.POWER_DOWN) && 
                    		!(building.hasFunction(LifeSupport.NAME))) {
                        building.setPowerMode(Building.NO_POWER);
                        neededPower -= building.getPoweredDownPowerRequired();
                    }
                }
            }
            
            // If power needs are still not met, turn off the power to each inhabitable building 
            // until required power reduction is met.
            if (neededPower > 0D) {
                Iterator<Building> iNoPower = buildings.iterator();
                while (iNoPower.hasNext() && (neededPower > 0D)) {
                    Building building = iNoPower.next();
                    if (!powerSurplus(building, Building.POWER_DOWN) && 
                        	building.hasFunction(LifeSupport.NAME)) {
                        building.setPowerMode(Building.NO_POWER);
                        neededPower -= building.getPoweredDownPowerRequired();
                    }
                }
            }
        }
        
        // Update power value.
        determinePowerValue();
    }
    
    /**
     * Updates the total power generated in the grid.
     * @throws BuildingException if error determining total power generated.
     */
    private void updateTotalPowerGenerated() throws BuildingException {
        double tempPowerGenerated = 0D;
        BuildingManager manager = settlement.getBuildingManager();
        Iterator<Building> iPow = manager.getBuildings(PowerGeneration.NAME).iterator();
        while (iPow.hasNext()) {
            Building building = iPow.next();
            PowerGeneration gen = (PowerGeneration) building.getFunction(PowerGeneration.NAME);
            tempPowerGenerated += gen.getGeneratedPower();
            // logger.info(((Building) gen).getName() + " generated: " + gen.getGeneratedPower());
        }
        setGeneratedPower(tempPowerGenerated);
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Total power generated: " + powerGenerated);
        }
    }
    
    /**
     * Updates the total power stored in the grid.
     * @throws BuildingException if error determining total power stored.
     */
    private void updateTotalStoredPower() throws BuildingException {
        double tempPowerStored = 0D;
        BuildingManager manager = settlement.getBuildingManager();
        Iterator<Building> iStore = manager.getBuildings(PowerStorage.NAME).iterator();
        while (iStore.hasNext()) {
            Building building = iStore.next();
            PowerStorage store = (PowerStorage) building.getFunction(PowerStorage.NAME);
            tempPowerStored += store.getPowerStored();
        }
        setStoredPower(tempPowerStored);
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Total power stored: " + powerStored);
        }
    }
    
    /**
     * Updates the toal power required in the grid.
     * @throws BuildingException if error determining total power required.
     */
    private void updateTotalRequiredPower() throws BuildingException {
        double tempPowerRequired = 0D;
        boolean powerUp = powerMode.equals(POWER_UP_MODE);
        BuildingManager manager = settlement.getBuildingManager();
        List<Building> buildings = manager.getBuildings();
        Iterator<Building> iUsed = buildings.iterator();
        while (iUsed.hasNext()) {
            Building building = iUsed.next();
            if (powerUp) {
                building.setPowerMode(Building.FULL_POWER);
                tempPowerRequired += building.getFullPowerRequired();
                if(logger.isLoggable(Level.FINE)) {
                    logger.fine(building.getName() + " full power used: " + 
                            building.getFullPowerRequired());
                }
            }
            else {
                building.setPowerMode(Building.POWER_DOWN);
                tempPowerRequired += building.getPoweredDownPowerRequired();
                
                if(logger.isLoggable(Level.FINE)) {
                logger.fine(building.getName() + " power down power used: " + 
                        building.getPoweredDownPowerRequired());
                }
            }
        }
        setRequiredPower(tempPowerRequired);
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Total power required: " + powerRequired);
        }
    }
    
    /**
     * Updates the total power storage capacity in the grid.
     * @throws BuildingException if error determining total power storage capacity.
     */
    private void updateTotalPowerStorageCapacity() throws BuildingException {
        double tempPowerStorageCapacity = 0D;
        BuildingManager manager = settlement.getBuildingManager();
        Iterator<Building> iStore = manager.getBuildings(PowerStorage.NAME).iterator();
        while (iStore.hasNext()) {
            Building building = iStore.next();
            PowerStorage store = (PowerStorage) building.getFunction(PowerStorage.NAME);
            tempPowerStorageCapacity += store.getPowerStorageCapacity();
        }
        setStoredPowerCapacity(tempPowerStorageCapacity);
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Total power storage capacity: " + powerStorageCapacity);
        }
    }
    
    /**
     * Checks if building generates more power 
     * than it uses in a given power mode.
     *
     * @param building the building
     * @param mode the building's power mode to check.
     * @return true if building supplies more power than it uses.
     * throws BuildingException if error in power generation.
     */
    private boolean powerSurplus(Building building, String mode) throws BuildingException {
        double generated = 0D;
        if (building.hasFunction(PowerGeneration.NAME)) {
        	PowerGeneration powerGeneration = 
        		(PowerGeneration) building.getFunction(PowerGeneration.NAME);
        	generated = powerGeneration.getGeneratedPower(); 
        }
            
        double used = 0D;
        if (mode.equals(Building.FULL_POWER)) used = building.getFullPowerRequired();
        else if (mode.equals(Building.POWER_DOWN)) used = building.getPoweredDownPowerRequired();
        
        if (generated > used) return true;
        else return false;
    }
    
    /**
     * Stores any excess grid power in power storage buildings if possible.
     * @param excessPower excess grid power (in kW hr).
     * @throws BuildingException if error storing excess power.
     */
    private void storeExcessPower(double excessPower) throws BuildingException {
        BuildingManager manager = settlement.getBuildingManager();
        Iterator<Building> i = manager.getBuildings(PowerStorage.NAME).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            PowerStorage storage = (PowerStorage) building.getFunction(PowerStorage.NAME);
            double remainingCapacity = storage.getPowerStorageCapacity() - storage.getPowerStored();
            if (remainingCapacity > 0D) {
                double powerToStore = excessPower;
                if (remainingCapacity < powerToStore) powerToStore = remainingCapacity;
                storage.setPowerStored(storage.getPowerStored() + powerToStore);
                excessPower -= powerToStore;
            }
        }
    }
    
    /**
     * Retrieves stored power for the grid.
     * @param neededPower the power needed (kW hr).
     * @return stored power retrieved (kW hr).
     * @throws BuildingException if error retrieving power.
     */
    private double retrieveStoredPower(double neededPower) throws BuildingException {
        BuildingManager manager = settlement.getBuildingManager();
        Iterator<Building> i = manager.getBuildings(PowerStorage.NAME).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            PowerStorage storage = (PowerStorage) building.getFunction(PowerStorage.NAME);
            if ((storage.getPowerStored() > 0D) && (neededPower > 0D)) {
                double retrievedPower = neededPower;
                if (storage.getPowerStored() < retrievedPower) retrievedPower = storage.getPowerStored();
                storage.setPowerStored(storage.getPowerStored() - retrievedPower);
                neededPower -= retrievedPower;
            }
        }
        return neededPower;
    }
    
    /**
     * Gets the value of electrical power at the settlement.
     * @return value of power (VP per kw h).
     */
    public double getPowerValue() {
    	return powerValue;
    }
    
    /**
     * Determines the value of electrical power at the settlement.
     */
    private void determinePowerValue() {
    	double demand = powerRequired;
    	double supply = powerGenerated + (powerStored / 2D);
    	
    	double newPowerValue = demand / (supply + 1.0D);
        
    	if (newPowerValue != powerValue) {
    		powerValue = newPowerValue;
    		settlement.fireUnitUpdate(POWER_VALUE_EVENT);
    	}
    }
}