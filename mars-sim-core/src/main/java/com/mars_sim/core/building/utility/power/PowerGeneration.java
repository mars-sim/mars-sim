/*
 * Mars Simulation Project
 * PowerGeneration.java
 * @date 2024-08-03
 * @author Scott Davis
 */
package com.mars_sim.core.building.utility.power;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.building.config.SourceSpec;
import com.mars_sim.core.building.config.GenerationSpec;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;

/**
 * The PowerGeneration class is a building function for generating power.
 */
public class PowerGeneration extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
//	may add back private static final SimLogger logger = SimLogger.getLogger(PowerGeneration.class.getName())

	private double powerGeneratedCache;

	private List<PowerSource> powerSources;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public PowerGeneration(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.POWER_GENERATION, spec, building);

		powerGeneratedCache = 0D;
		
		// Determine power sources.
		powerSources = new ArrayList<>();
		
		if (spec instanceof GenerationSpec ss) {
			for (SourceSpec sourceSpec : ss.getSources()) {
				String type = sourceSpec.getType();
				double power = sourceSpec.getCapacity();
				int numModules = sourceSpec.getNumModules();
				double conversion = sourceSpec.getConversionEfficiency();
				double percentLoadCapacity = sourceSpec.getpercentLoadCapacity();
				
				PowerSource powerSource = null;
				PowerSourceType powerType = PowerSourceType.getType(type);
				switch (powerType) {
					case FISSION_POWER:
						powerSource = new FissionPowerSource(numModules, power, conversion, percentLoadCapacity);				
						break;
						
					case THERMIONIC_NUCLEAR_POWER:
						powerSource = new ThermionicNuclearPowerSource(numModules, power, conversion, percentLoadCapacity);				
						break;
						
					case SOLAR_POWER:
						powerSource = new SolarPowerSource(building, power);
						break;
						
					case SOLAR_THERMAL:
						powerSource = new SolarThermalPowerSource(building, power);
						break;
						
					case FUEL_POWER:
						boolean toggle = Boolean.parseBoolean(sourceSpec.getAttribute(SourceSpec.TOGGLE));
						String fuelType = sourceSpec.getAttribute(SourceSpec.FUEL_TYPE);
						powerSource = new FuelPowerSource(building, power, toggle, fuelType);
						break;
						
					case WIND_POWER:
						powerSource = new WindPowerSource(building, power);				
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
		else {
			throw new IllegalArgumentException("FunctionSpec is of wrong type");
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
		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.POWER_GENERATION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += getPowerSourceSupply(building.getPowerGeneration().powerSources, settlement) * wearModifier;
			}
		}

		double existingPowerValue = demand / (supply + 1D);
		var spec = buildingConfig.getFunctionSpec(buildingName, FunctionType.POWER_GENERATION);
		if (spec instanceof GenerationSpec ss) {
			double powerSupply = ss.getSources().stream()
									.mapToDouble(SourceSpec::getCapacity).sum();

			return powerSupply * existingPowerValue;
		}
		return 0;
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
			result += j.next().getAveragePower(settlement);
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

		// Future: Building should only produce power if it has no current malfunctions.
		// Need to check for !getBuilding().getMalfunctionManager().hasMalfunction()

		Iterator<PowerSource> i = powerSources.iterator();
		while (i.hasNext()) {
			PowerSource powerSource = i.next();
			if (powerSource instanceof FuelPowerSource source) {
				
				// Future: Should look for a better way of inserting the time param to FuelPowerSource
				
				// Note: do NOT toggle it on and off here. // source.toggleON()
				// Toggling on and off should be driven by needs in PowerGrid
				
				PowerMode powerMode = building.getPowerMode();
				if (powerMode == PowerMode.FULL_POWER) {

					source.setTime(time);
				}
				else if (powerMode == PowerMode.LOW_POWER) {
				
					// Future: Will need a standardized way to tune how a power source
					// will respond to low power mode
					source.setTime(time / 10);
				}
				else {

					source.setTime(0);
				}	
			}
			
			double p = powerSource.getCurrentPower(getBuilding());
			
			if (!Double.isNaN(p) && !Double.isInfinite(p)) {
				result += p;
			}
		}
	
		/**
		 * Note: Please do retain the block of code below for future feature
		 */
		
//		if (thermalGeneration == null)
//			thermalGeneration = building.getThermalGeneration();
//
//		// Note: Check to see if this building has thermal generation function
//		// that may convert thermal energy to electrical power
//		if (thermalGeneration != null) {
//			double p = thermalGeneration.getGeneratedPower();
//			result += p;
//		}
	
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
	
	/**
	 * Gets a set of malfunction scopes.
	 */
	@Override
	public Set<String> getMalfunctionScopeStrings() {
		Set<String> set = new HashSet<>();
		String n = getFunctionType().getName();
		set.add(n);
		
		for (int x = 0; x < powerSources.size(); x++) {
			set.add(powerSources.get(x).getType().getName());
		}

		return set;
	}

	/**
	 * Gets the power sources for the building.
	 * 
	 * @return list of power sources.
	 */
	public List<PowerSource> getPowerSources() {
		return new ArrayList<>(powerSources);
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
	@Override
	public void removeFromSettlement() {
		powerSources.forEach(i -> i.removeFromSettlement());
	}

	public double getGeneratedPower() {
		return powerGeneratedCache;
	}
}
