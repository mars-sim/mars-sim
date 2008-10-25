/**
 * Mars Simulation Project
 * PowerGeneration.java
 * @version 2.85 2008-08-22
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.structure.goods.GoodsUtil;
 
/**
 * The PowerGeneration class is a building function for generating power.
 */
public class PowerGeneration extends Function implements Serializable {
    
    public static final String NAME = "Power Generation";
    
    private List<PowerSource> powerSources;
    
    /**
     * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public PowerGeneration(Building building) throws BuildingException {
    	// Call Function constructor.
    	super(NAME, building);
    	
    	// Determine power sources.
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
    		
    	try {
    		powerSources = config.getPowerSources(building.getName());
    	}
    	catch (Exception e) {
    		throw new BuildingException("PowerGeneration.constructor: " + e.getMessage());
    	}
    }
    
    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static final double getFunctionValue(String buildingName, boolean newBuilding, 
            Settlement settlement) throws Exception {
        
        double demand = settlement.getPowerGrid().getRequiredPower();
        
        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(NAME).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                PowerGeneration powerFunction = (PowerGeneration) building.getFunction(NAME);
                supply += getPowerSourceSupply(powerFunction.powerSources, settlement);
            }
        }
        
        double existingPowerValue = demand / (supply + 1D);
        
        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double powerSupply = getPowerSourceSupply(config.getPowerSources(buildingName), settlement);
        
        return powerSupply * existingPowerValue;
    }
    
    /**
     * Gets the supply value of a list of power sources.
     * @param powerSources list of power sources.
     * @param settlement the settlement.
     * @return supply value.
     * @throws Exception if error determining supply value.
     */
    private static double getPowerSourceSupply(List<PowerSource> powerSources, Settlement settlement) 
            throws Exception {
        double result = 0D;
        
        Iterator<PowerSource> j = powerSources.iterator();
        while (j.hasNext()) {
            PowerSource source = j.next();
            if (source instanceof StandardPowerSource) result += source.getMaxPower();
            else if (source instanceof FuelPowerSource) {
                FuelPowerSource fuelSource = (FuelPowerSource) source;
                double fuelPower = source.getMaxPower();
                AmountResource fuelResource = fuelSource.getFuelResource();
                Good fuelGood = GoodsUtil.getResourceGood(fuelResource);
                double fuelValue = settlement.getGoodsManager().getGoodValuePerMass(fuelGood);
                fuelValue *= fuelSource.getFuelConsumptionRate();
                fuelPower -= fuelValue;
                if (fuelPower < 0D) fuelPower = 0D;
                result += fuelPower;
            }
            else if (source instanceof SolarPowerSource) {
                result += source.getMaxPower() / 2D;
            }
            else if (source instanceof SolarThermalPowerSource) {
                result += source.getMaxPower() / 2.5D;
            }
            else if (source instanceof WindPowerSource) {
                // TODO: Base on current wind speed at settlement.
                result += source.getMaxPower() / 3D;
            }
        }
        
        return result;
    }
    
    /**
     * Gets the amount of electrical power generated.
     * @return power generated in kW
     */
    public double getGeneratedPower() {
    	double result = 0D;
    	Iterator i = powerSources.iterator();
    	while (i.hasNext()) 
    		result += ((PowerSource) i.next()).getCurrentPower(getBuilding());
    	return result;
    }
    
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
    public void timePassing(double time) throws BuildingException {
        for(PowerSource source : powerSources ) {
            if (source instanceof FuelPowerSource) {
                FuelPowerSource fuelSource = (FuelPowerSource)source;
                if (fuelSource.isToggleON()) {
                    fuelSource.consumeFuel(time,getBuilding().getInventory());
                }
            }
        }
    }
	
	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}
	
	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
		return 0D;
	}
    
    @Override
    public String[] getMalfunctionScopeStrings() {
        String[] result = new String[powerSources.size() + 1];
        
        result[0] = getName();
        
        for (int x = 0; x < powerSources.size(); x++)
            result[x + 1] = powerSources.get(x).getType();
        
        return result;
    }
    
    /**
     * Gets the power sources for the building.
     * @return list of power sources.
     */
    public List<PowerSource> getPowerSources() {
        return new ArrayList<PowerSource>(powerSources);
    }
}