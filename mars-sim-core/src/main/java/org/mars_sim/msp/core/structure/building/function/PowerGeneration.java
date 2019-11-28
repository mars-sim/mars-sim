/**
 * Mars Simulation Project
 * PowerGeneration.java
 * @version 3.1.0 2018-08-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

/**
 * The PowerGeneration class is a building function for generating power.
 */
public class PowerGeneration extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(PowerGeneration.class.getName());
	
	/** TODO Name of the building function needs to be internationalized. */
	private static final FunctionType FUNCTION = FunctionType.POWER_GENERATION;

	// Data members.
	private double time;

	private double powerGeneratedCache;

	private List<PowerSource> powerSources;

	private ThermalGeneration thermalGeneration;
	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public PowerGeneration(Building building) {
		// Call Function constructor.
		super(FUNCTION, building);
		this.building = building;

		// Determine power sources.
		powerSources = buildingConfig.getPowerSources(building.getBuildingType());

	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		double demand = settlement.getPowerGrid().getRequiredPower();

		double supply = 0D;
		boolean removedBuilding = false;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				PowerGeneration powerFunction = building.getPowerGeneration();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += getPowerSourceSupply(powerFunction.powerSources, settlement) * wearModifier;
			}
		}

		double existingPowerValue = demand / (supply + 1D);

		double powerSupply = getPowerSourceSupply(buildingConfig.getPowerSources(buildingName), settlement);

		return powerSupply * existingPowerValue;
	}

	/**
	 * Gets the supply value of a list of power sources.
	 * 
	 * @param powerSources list of power sources.
	 * @param settlement   the settlement.
	 * @return supply value.
	 * @throws Exception if error determining supply value.
	 */
	private static double getPowerSourceSupply(List<PowerSource> powerSources, Settlement settlement) {
		double result = 0D;

		Iterator<PowerSource> j = powerSources.iterator();
		while (j.hasNext()) {
			PowerSource source = j.next();
			result += source.getAveragePower(settlement);
			if (source instanceof StandardPowerSource)
				result += source.getMaxPower();
			else if (source instanceof FuelPowerSource) {
				FuelPowerSource fuelSource = (FuelPowerSource) source;
				double fuelPower = source.getMaxPower();
				// AmountResource fuelResource = fuelSource.getFuelResource();
				int id = fuelSource.getFuelResourceID();
				Good fuelGood = GoodsUtil.getResourceGood(id);
				double fuelValue = settlement.getGoodsManager().getGoodValuePerItem(fuelGood);
				fuelValue *= fuelSource.getFuelConsumptionRate();
				fuelPower -= fuelValue;
				if (fuelPower < 0D)
					fuelPower = 0D;
				result += fuelPower;
			} else if (source instanceof SolarPowerSource) {
				result += source.getMaxPower() * .707;
			} else if (source instanceof SolarThermalPowerSource) {
				result += source.getMaxPower() * .707;
			} else if (source instanceof WindPowerSource) {
				result += source.getMaxPower() * .707;
			} else if (source instanceof AreothermalPowerSource) {
				double areothermalHeat = surface.getAreothermalPotential(settlement.getCoordinates());
				// TODO: why divided by 100D
				result += source.getMaxPower()  * .707 * areothermalHeat / 100D;
			}
		}

		return result;
	}

	/**
	 * Calculates the amount of electrical power generated.
	 * 
	 * @return power generated in kW
	 */
	public double calculateGeneratedPower() {
		double result = 0D;

		// Building should only produce power if it has no current malfunctions.
		// if (!getBuilding().getMalfunctionManager().hasMalfunction()) {

		Iterator<PowerSource> i = powerSources.iterator();
		while (i.hasNext()) {
			PowerSource powerSource = i.next();
			if (powerSource.getType().equals(PowerSourceType.FUEL_POWER)) {
				// System.out.println(heatSource.toString() + " at building "+
				// building.getNickName() + " is HEAT_OFF");
				powerSource.setTime(time);
			}
			
			double p = powerSource.getCurrentPower(getBuilding());
			
			if (!Double.isNaN(p) && !Double.isInfinite(p)) {
				result += p;
			}
			
		}

//		logger.info(building.getNickName() + " generated power : " + Math.round(result * 100.0)/100.0 + " kW");
		
		if (thermalGeneration == null)
			thermalGeneration = building.getThermalGeneration();

		// Note: some buildings don't have thermal generation function
		if (thermalGeneration != null) {
			double p = thermalGeneration.getGeneratedPower();
//			logger.info(building.getNickName() + " thermal power : " + Math.round(p * 100.0)/100.0 + " kW");
			result += p;// calculateGeneratedPower();
		}
	
		return result;
	}

//	/**
//	 * Gets the amount of electrical power generated.
//	 * @return power generated in kW
//	 
//	public double getGeneratedPower() {
//	double result = 0D;
//		// Building should only produce power if it has no current malfunctions.
//		if (!getBuilding().getMalfunctionManager().hasMalfunction()) {
//			
//			Iterator<PowerSource> i = powerSources.iterator();
//			while (i.hasNext()) {
//					PowerSource powerSource = i.next();
//				    if (powerSource.getType().equals(PowerSourceType.FUEL_POWER)) {
//				    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
//				    	powerSource.setTime(time);
//				    	result += powerSource.getCurrentPower(getBuilding());
//				    }
//				    else
//						result += powerSource.getCurrentPower(getBuilding());
//			}			
//			
//			//Iterator<PowerSource> i = powerSources.iterator();
//			//while (i.hasNext()) {
//			//	result += i.next().getCurrentPower(getBuilding());
//			//}
//		}
//
//
//		return powerGeneratedCache;
//
//	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {
		this.time = time;

		double powerGenerated = calculateGeneratedPower();

		if (powerGeneratedCache != powerGenerated) {
			powerGeneratedCache = powerGenerated;
		}

//		for (PowerSource source : powerSources) {
//			if (source instanceof SolarPowerSource) {
//				SolarPowerSource solarPowerSource = (SolarPowerSource) source;
//				//System.out.println("solarPowerSource.getMaxPower() is "+ solarPowerSource.getMaxPower());
//				double factor = solarPowerSource.getCurrentPower(getBuilding()) / solarPowerSource.getMaxPower();
//				// TODO : use PowerMode.FULL_POWER ?
//				double d_factor = SolarPowerSource.DEGRADATION_RATE_PER_SOL * time/1000D;
//				double eff = solarPowerSource.getEfficiency() ;
//				double new_eff = eff - eff * d_factor * factor;
//				solarPowerSource.setEfficiency(new_eff);
//				//System.out.println("new_eff is " + new_eff);
//			}
//			
//			else if (source instanceof FuelPowerSource) {
//				FuelPowerSource fuelSource = (FuelPowerSource) source;
//				if (fuelSource.isToggleON()) {
//					//fuelSource.consumeFuel(time, getBuilding().getSettlementInventory());
//				}
//			}
//
//		}

	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0D;
	}

	@Override
	public String[] getMalfunctionScopeStrings() {
		String[] result = new String[powerSources.size() + 1];
		// TODO take care to properly internationalize malfunction scope "strings"
		result[0] = getFunctionType().getName();

		for (int x = 0; x < powerSources.size(); x++) {
			result[x + 1] = powerSources.get(x).getType().getName();
		}

		return result;
	}

	/**
	 * Gets the power sources for the building.
	 * 
	 * @return list of power sources.
	 */
	public List<PowerSource> getPowerSources() {
		return new ArrayList<PowerSource>(powerSources);
	}

	@Override
	public double getMaintenanceTime() {

		double result = 0D;

		Iterator<PowerSource> i = powerSources.iterator();
		while (i.hasNext()) {
			result += i.next().getMaintenanceTime();
		}

		return result;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	/**
	 * Return the fuel cell stacks to the inventory
	 */
	public void removeFromSettlement() {
		Iterator<PowerSource> i = powerSources.iterator();
		while (i.hasNext()) {
			i.next().removeFromSettlement();
		}
	}

	public double getGeneratedPower() {
		return powerGeneratedCache;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		powerSources = null;
		thermalGeneration = null;
		building = null;

//		Iterator<PowerSource> i = powerSources.iterator();
//		while (i.hasNext()) {
//			i.next().destroy();
//		}
//		powerSources.clear();

	}

}