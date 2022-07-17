/*
 * Mars Simulation Project
 * PowerGeneration.java
 * @date 2022-06-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.SourceSpec;
import org.mars_sim.msp.core.time.ClockPulse;

/**
 * The PowerGeneration class is a building function for generating power.
 */
public class PowerGeneration extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
//	private static final SimLogger logger = SimLogger.getLogger(PowerGeneration.class.getName());

	private double powerGeneratedCache;

	private List<PowerSource> powerSources;

	private ThermalGeneration thermalGeneration;
	
	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public PowerGeneration(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.POWER_GENERATION, spec, building);

		// Determine power sources.
		powerSources = new ArrayList<>();
		for (SourceSpec sourceSpec : buildingConfig.getPowerSources(building.getBuildingType())) {
			String type = sourceSpec.getType();
			double power = sourceSpec.getCapacity();
		
			PowerSource powerSource = null;
			PowerSourceType powerType = PowerSourceType.getType(type);
			switch (powerType) {
			case STANDARD_POWER:
				powerSource = new StandardPowerSource(power);				
				break;
				
			case SOLAR_POWER:
				powerSource = new SolarPowerSource(power);
				break;
				
			case SOLAR_THERMAL:
				powerSource = new SolarThermalPowerSource(power);
				break;
				
			case FUEL_POWER:
				boolean toggleStafe = Boolean.parseBoolean(sourceSpec.getAttribute(SourceSpec.TOGGLE));
				String fuelType = sourceSpec.getAttribute(SourceSpec.FUEL_TYPE);
				double consumptionSpeed = Double.parseDouble(sourceSpec.getAttribute(SourceSpec.CONSUMPTION_RATE));
				powerSource = new FuelPowerSource(power, toggleStafe, fuelType, consumptionSpeed);
				break;
				
			case WIND_POWER:
				powerSource = new WindPowerSource(power);				
				break;
				
			case AREOTHERMAL_POWER:
				powerSource = new AreothermalPowerSource(power);
				break;
			
			default:
				throw new IllegalArgumentException("Don't know how to build PowerSource : " + type);
			}
			powerSources.add(powerSource);
		}
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
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.POWER_GENERATION).iterator();
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


		double powerSupply = buildingConfig.getHeatSources(buildingName).stream()
								.mapToDouble(SourceSpec::getCapacity).sum();

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
//				Good fuelGood = GoodsUtil.getResourceGood(id);
				double fuelValue = settlement.getGoodsManager().getGoodValuePerItem(id);
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
	public double calculateGeneratedPower(double time) {
		double result = 0D;

		// Building should only produce power if it has no current malfunctions.
		// if (!getBuilding().getMalfunctionManager().hasMalfunction()) {

		Iterator<PowerSource> i = powerSources.iterator();
		while (i.hasNext()) {
			PowerSource powerSource = i.next();
			if (powerSource.getType() == PowerSourceType.FUEL_POWER) {
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

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			powerGeneratedCache = calculateGeneratedPower(pulse.getElapsed());
		}
		return valid;
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
	
	/**
	 * Returns the power source to the inventory.
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

		thermalGeneration = null;

		Iterator<PowerSource> i = powerSources.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}
		powerSources.clear();

		powerSources = null;
	}

}
