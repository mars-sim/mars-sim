/**
 * Mars Simulation Project
 * ThermalGeneration.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.SourceSpec;
import org.mars_sim.msp.core.time.ClockPulse;

/**
 * The ThermalGeneration class handles how the buildings of a settlement
 * generate and control temperature by heating 
 */
public class ThermalGeneration
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private double heatGeneratedCache;
	
	private double powerGeneratedCache;

	private Heating heating;
	
	private List<HeatSource> heatSources;
	
	/**
	 * Constructor
	 */
	public ThermalGeneration(Building building) {
		// Call Function constructor.
		super(FunctionType.THERMAL_GENERATION, building);
		
		heating = new Heating(building);

		// Determine heat sources.
		heatSources = new ArrayList<>();
		
		for (SourceSpec spec : buildingConfig.getHeatSources(building.getBuildingType())) {
			double heat = spec.getCapacity();
			HeatSource heatSource = null;
			HeatSourceType sourceType = HeatSourceType.valueOf(spec.getType().toUpperCase().replace(" ", "_"));
			
			switch (sourceType) {
			case ELECTRIC_HEATING:
				heatSource = new ElectricHeatSource(heat);				
				break;

			case SOLAR_HEATING:
				heatSource = new SolarHeatSource(building, heat);
				break;
				
			case FUEL_HEATING:
				boolean toggleStafe = Boolean.parseBoolean(spec.getAttribute(SourceSpec.TOGGLE));
				String fuelType = spec.getAttribute(SourceSpec.FUEL_TYPE);
				double consumptionSpeed = Double.parseDouble(spec.getAttribute(SourceSpec.CONSUMPTION_RATE));
				heatSource = new FuelHeatSource(building, heat, toggleStafe, fuelType, consumptionSpeed);
				break;
				
			default:
				throw new IllegalArgumentException("Do not know heat source type :" + spec.getType());
			}
			heatSources.add(heatSource);
		}
	}

	/**
	 * Gets the value of the function for a named building.
	 * @param buildingName the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value) (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName,
			boolean newBuilding, Settlement settlement) {

		double demand = settlement.getThermalSystem().getRequiredHeat();
		double supply = 0D;
		boolean removedBuilding = false;

		for (Building building : settlement.getBuildingManager().getBuildings(FunctionType.THERMAL_GENERATION)) {
			if (!newBuilding
					&& building.getBuildingType().equalsIgnoreCase(buildingName)
					&& !removedBuilding) {
				removedBuilding = true;
			} else {
				ThermalGeneration heatFunction = building.getThermalGeneration();
				double wearModifier = (building.getMalfunctionManager()
						.getWearCondition() / 100D) * .75D + .25D;
				supply += getHeatSourceSupply(heatFunction.heatSources) * wearModifier;
			}
		}

		double existingHeatValue = demand / (supply + 1D);

		double heatSupply = buildingConfig.getHeatSources(buildingName).stream()
								.mapToDouble(SourceSpec::getCapacity).sum();

		return heatSupply * existingHeatValue;
	}

	/**
	 * Gets the supply value of a list of heat sources.
	 * @param heatSources list of heat sources.
	 * @param settlement the settlement.
	 * @return supply value.
	 * @throws Exception if error determining supply value.
	 */
	private static double getHeatSourceSupply(List<HeatSource> heatSources) {
		double result = 0D;

		for (HeatSource source : heatSources) {				
			result += source.getMaxHeat();
		}

		return result;
	}

	/**
	 * Gets the total amount of heat that this building is capable of producing (regardless malfunctions).
	 * @return heat generated in kW (heat flow rate)
	 */
	public double getHeatGenerationCapacity() {
		double result = 0D;
		
		for (HeatSource source : heatSources) {
			result += source.getMaxHeat();
		}
		return result;
	}

	/**
	 * Gets the total amount of heat that this building is CURRENTLY producing
	 * @return heat generated in kW (heat flow rate)
	 */
	public double getGeneratedHeat() {
		return heatGeneratedCache; // = 0.0 if heatMode == HeatMode.POWER_DOWN
	}

	/**
	 * Gets the total amount of power that this building is CURRENTLY producing
	 * @return power generated in kW ()
	 */
	public double getGeneratedPower() {
		return powerGeneratedCache; 
	}

	/**
	 * Calculate the total amount of heat that this building is CURRENTLY producing
	 * and also the power required to generate the heat
	 * 
	 * @return heat generated in kW
	 */
	private double calculateGeneratedHeat(double time) {

		double result = 0D;
		
		double elecRequired = 0;
		Building build = getBuilding();
		int percentageHeat = building.getHeatMode().getPercentage();
		for (HeatSource heatSource : heatSources) {
			heatSource.setPercentagePower(percentageHeat);
	    	heatSource.setTime(time);

	    	double heatCreated = heatSource.getCurrentHeat(build);

	    	if (heatSource.getType() == HeatSourceType.ELECTRIC_HEATING) {
	    		// Electric heating needs electric
	    		elecRequired += heatCreated;
	    	}
	
			result += heatCreated;
		}
			
		building.setPowerRequiredForHeating(elecRequired);

		return result;
	}


	/**
	 * Calculate the total amount of power that this building is CURRENTLY producing from heat sources
	 * 
	 * @return power generated in kW
	 */
	private double calculateGeneratedPower() {

		double result = 0D;
		HeatMode heatMode = building.getHeatMode();
		if (heatMode != HeatMode.FULL_HEAT) {
			boolean sufficientPower = building.getSettlement().getPowerGrid().isSufficientPower();
			
			// Calculate the unused
			int sparePercentage = 100 - heatMode.getPercentage();
			for (HeatSource heatSource : heatSources) {
			    if (heatSource.getType().equals(HeatSourceType.SOLAR_HEATING)) {
			    	heatSource.setPercentagePower(sparePercentage);
			    	result += heatSource.getCurrentPower(getBuilding());
			    }
			    // only if there's not enough power
			    else if (heatSource.getType().equals(HeatSourceType.FUEL_HEATING)) {
				    if (!sufficientPower) {
				    	heatSource.setPercentagePower(sparePercentage);
				    	result += heatSource.getCurrentPower(getBuilding());
				    }
			    }	
			}
		}

		return result;
	}

	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			heating.timePassing(pulse);
			
			double heatGenerated = 0;
			double powerGenerated = 0;
			
			// Set heatGenerated at the building the furnace belongs
			if (building.getPowerMode() == PowerMode.FULL_POWER) {
				heatGenerated = calculateGeneratedHeat(pulse.getElapsed());		
				powerGenerated = calculateGeneratedPower();
			}
			
			if (heatGeneratedCache != heatGenerated) {
				heatGeneratedCache = heatGenerated;
				building.setHeatGenerated(heatGenerated);
			}
	
			if (powerGeneratedCache != powerGenerated) {
				powerGeneratedCache = powerGenerated;
			}
			
			// set new efficiency. Needs a new method in HeatSource updateEffeciency 
			
	//		for (HeatSource source : heatSources)
	//			if (source instanceof SolarHeatSource) {
	//				SolarHeatSource solarHeatSource = (SolarHeatSource) source;
	//				//System.out.println("solarHeatSource.getMaxHeat() is "+ solarHeatSource.getMaxHeat());
	//				double factor = solarHeatSource.getCurrentHeat(getBuilding()) / solarHeatSource.getMaxHeat();
	//				// TODO : use HeatMode.FULL_POWER ?
	//				double d_factor = SolarHeatSource.DEGRADATION_RATE_PER_SOL * time/1000D;
	//				double eff = solarHeatSource.getEfficiency() ;
	//				double new_eff = eff - eff * d_factor * factor;
	//				solarHeatSource.setEfficiency(new_eff);
	//				//System.out.println("new_eff is " + new_eff);
	//			}
		}
		return valid;
	}


	public Heating getHeating() {
		return heating;
	}

	@Override
	public String[] getMalfunctionScopeStrings() {
		String[] result = new String[heatSources.size() + 1];
		// TODO take care to properly internationalize malfunction scope "strings"
		result[0] = getFunctionType().getName();

		for (int x = 0; x < heatSources.size(); x++) {
			result[x + 1] = heatSources.get(x).getType().toString();
		}

		return result;
	}

	/**
	 * Gets the heat sources for the building.
	 * @return list of heat sources.
	 */
	public List<HeatSource> getHeatSources() {
		return new ArrayList<>(heatSources);
	}

    @Override
    public double getMaintenanceTime() {

        double result = 0D;

        Iterator<HeatSource> i = heatSources.iterator();
        while (i.hasNext()) {
            result += i.next().getMaintenanceTime();
        }

        return result;
    }

	public double getFullPowerRequired() {
		return getElectricPowerRequired();
	}

	public double getElectricPowerRequired() {
		HeatMode heatMode = building.getHeatMode();
		
		if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
			return 0;

		// add the need of electric heat
		double result = 0;

		for (HeatSource source : heatSources) {
			if (source instanceof ElectricHeatSource) {
				// if it needs to be ON, use getMaxHeat() since it's the max power needed before counting in the heater efficiency 
				result = result + ((ElectricHeatSource)source).getCurrentHeat(building);
			}
		}
		
		return result;
	}

	
	@Override
	public void destroy() {
		super.destroy();

		heating = null;
		heatSources = null;
	}

}
