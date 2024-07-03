/*
 * Mars Simulation Project
 * ThermalGeneration.java
 * @date 2024-07-03
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.function;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.structure.building.FunctionSpec;
import com.mars_sim.core.structure.building.SourceSpec;
import com.mars_sim.core.time.ClockPulse;

/**
 * The ThermalGeneration class handles how the buildings of a settlement
 * generate and control temperature by heating .
 */
public class ThermalGeneration extends Function {

	/** default serial  id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// May add back private static final SimLogger logger = SimLogger.getLogger(ThermalGeneration.class.getName())
	
//	private static final double HEAT_MATCH_MOD = 1;
	
	// Data members.
	private double heatGeneratedCache;
	
	private double heatDev;

	private Heating heating;
	
	private List<HeatSource> heatSources;
	
	private HeatSource solarHeatSource;
	
	private HeatSource nuclearHeatSource;
	
	private HeatSource electricHeatSource;
	
	private HeatSource fuelHeatSource;
	
	/**
	 * Constructor
	 */
	public ThermalGeneration(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.THERMAL_GENERATION, spec, building);
		
		heating = new Heating(building, spec);

		// Determine heat sources.
		heatSources = new ArrayList<>();
		
		for (SourceSpec sourceSpec : buildingConfig.getHeatSources(building.getBuildingType())) {
			double heat = sourceSpec.getCapacity();
			HeatSource heatSource = null;
			HeatSourceType sourceType = HeatSourceType.valueOf(sourceSpec.getType().toUpperCase().replace(" ", "_"));
			
			switch (sourceType) {
			case ELECTRIC_HEATING:
				heatSource = new ElectricHeatSource(building, heat);	
				electricHeatSource = heatSource;
				break;

			case SOLAR_HEATING:
				heatSource = new SolarHeatingSource(building, heat);
				solarHeatSource = heatSource;
				break;
				
			case FUEL_HEATING:
				boolean toggle = Boolean.parseBoolean(sourceSpec.getAttribute(SourceSpec.TOGGLE));
				String fuelType = sourceSpec.getAttribute(SourceSpec.FUEL_TYPE);
				heatSource = new FuelHeatSource(building, heat, toggle, fuelType);
				fuelHeatSource = heatSource;
				break;
				
			case THERMAL_NUCLEAR:
				heatSource = new ThermalNuclearSource(building, heat);
				nuclearHeatSource = heatSource;
				break;
				
			default:
				throw new IllegalArgumentException("Do not know heat source type :" + sourceSpec.getType());
			}
			
			// Add this heat source into the list
			heatSources.add(heatSource);
		}
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
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

		for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.THERMAL_GENERATION)) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
				removedBuilding = true;
			} else {
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += getHeatSourceSupply(building.getThermalGeneration().heatSources) * wearModifier;
			}
		}

		double existingHeatValue = demand / (supply + 1D);

		double heatSupply = buildingConfig.getHeatSources(buildingName).stream()
								.mapToDouble(SourceSpec::getCapacity).sum();

		return heatSupply * existingHeatValue;
	}

	/**
	 * Gets the supply value of a list of heat sources.
	 * 
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
	 * 
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
	 * Gets the total amount of heat that this building is CURRENTLY producing.
	 * 
	 * @return heat generated in kW (heat flow rate)
	 */
	public double getGeneratedHeat() {
		return heatGeneratedCache;
	}

//	/**
//	 * Gets the total amount of power that this building is CURRENTLY producing.
//	 * 
//	 * @return power generated in kW ()
//	 */
//	public double getGeneratedPower() {
//		return powerGeneratedCache; 
//	}

	/**
	 * Calculates the amount of heat that this building is generating to cover the heat load.
	 * 
	 * @param heatLoad
	 * @param time
	 * @return heat generated in kW.
	 */
	private double calculateHeatGen(double heatLoad, double time) {

		double heatReq = heatLoad * 1.25;
		double heatGen = 0D;
	
		HeatMode newHeatMode = null;
		HeatMode heatMode = null;
		
		// Order of business: solar, nuclear, electric, and fuel
		
		List<HeatMode> ALL_HEAT_MODES = HeatMode.ALL_HEAT_MODES;
		int size = ALL_HEAT_MODES.size() - 1;
		
		if (solarHeatSource != null) {	
			for (int i=1; i<size; i++) {
				heatMode = ALL_HEAT_MODES.get(i);

		    	double h = solarHeatSource.requestHeat(heatMode.getPercentage());

				if (!Double.isNaN(h) && !Double.isInfinite(h)) {
					heatGen += h;
					heatReq -= h;		
					if (heatReq > 0) {
						// Go to the next heat source for more heat
					}
					else {				
					
						// Set the new heat mode
						newHeatMode = heatMode;
						
						solarHeatSource.setHeatMode(newHeatMode, building);
						building.fireUnitUpdate(UnitEventType.SOLAR_HEAT_EVENT);	
						
						// Convert all thermal nuclear heat to electricity
						if (nuclearHeatSource != null) {
							nuclearHeatSource.setHeatMode(HeatMode.HEAT_OFF, building);
							building.fireUnitUpdate(UnitEventType.NUCLEAR_HEAT_EVENT);
						}
						
						// Turn off electric heat
						if (electricHeatSource != null) {
							electricHeatSource.setHeatMode(HeatMode.OFFLINE, building);
							building.fireUnitUpdate(UnitEventType.ELECTRIC_HEAT_EVENT);
						}
						
						// Turn off fuel heat
						if (fuelHeatSource != null) {
							fuelHeatSource.setHeatMode(HeatMode.OFFLINE, building);
							building.fireUnitUpdate(UnitEventType.FUEL_HEAT_EVENT);
						}
						
						return heatGen;
					}
				}
			}
			
			// Set the new heat mode
			newHeatMode = heatMode;
			solarHeatSource.setHeatMode(newHeatMode, building);
			building.fireUnitUpdate(UnitEventType.SOLAR_HEAT_EVENT);
		}
		
		if (nuclearHeatSource != null) {
			for (int i=1; i<size; i++) {
				heatMode = ALL_HEAT_MODES.get(i);

		    	double h = nuclearHeatSource.requestHeat(heatMode.getPercentage());
				
				if (!Double.isNaN(h) && !Double.isInfinite(h)) {
					heatGen += h;
					heatReq -= h;			
					if (heatReq > 0) {
						// Go to the next heat source for more heat
					}
					else {		
						// Set the new heat mode
						newHeatMode = heatMode;
						
						// Will automatically convert rest of thermal nuclear heat to electricity					
						nuclearHeatSource.setHeatMode(newHeatMode, building);
						building.fireUnitUpdate(UnitEventType.NUCLEAR_HEAT_EVENT);
						
						// Turn off electric heat
						if (electricHeatSource != null) {
							electricHeatSource.setHeatMode(HeatMode.OFFLINE, building);		
							building.fireUnitUpdate(UnitEventType.ELECTRIC_HEAT_EVENT);
						}
						
						// Turn off fuel heat
						if (fuelHeatSource != null) {
							fuelHeatSource.setHeatMode(HeatMode.OFFLINE, building);
							building.fireUnitUpdate(UnitEventType.FUEL_HEAT_EVENT);
						}
		
						return heatGen;
					}
				}
			}
			
			// Set the new heat mode
			newHeatMode = heatMode;
			nuclearHeatSource.setHeatMode(newHeatMode, building);
			building.fireUnitUpdate(UnitEventType.NUCLEAR_HEAT_EVENT);
		}
		
		if (electricHeatSource != null) {
			for (int i=1; i<size; i++) {
				heatMode = ALL_HEAT_MODES.get(i);
				
		    	double h = electricHeatSource.requestHeat(heatMode.getPercentage());
				
				if (!Double.isNaN(h) && !Double.isInfinite(h)) {
					heatGen += h;
					heatReq -= h;			
					if (heatReq > 0) {
						// Go to the next heat source for more heat
					}
					else {
						// Set the new heat mode
						newHeatMode = heatMode;
						
						electricHeatSource.setHeatMode(newHeatMode, building);
						building.fireUnitUpdate(UnitEventType.ELECTRIC_HEAT_EVENT);
						
						// Turn off fuel heat
						if (fuelHeatSource != null) {
							fuelHeatSource.setHeatMode(HeatMode.OFFLINE, building);
							building.fireUnitUpdate(UnitEventType.FUEL_HEAT_EVENT);
						}
					
						return heatGen;
					}
				}
			}
			
			// Set the new heat mode
			newHeatMode = heatMode;
			electricHeatSource.setHeatMode(newHeatMode, building);
			building.fireUnitUpdate(UnitEventType.ELECTRIC_HEAT_EVENT);
		}
		
		if (fuelHeatSource != null) {
			for (int i=1; i<size; i++) {
				heatMode = ALL_HEAT_MODES.get(i);
				
				fuelHeatSource.setTime(time);
		    	double h = fuelHeatSource.requestHeat(heatMode.getPercentage());
				
				if (!Double.isNaN(h) && !Double.isInfinite(h)) {
					heatGen += h;
					heatReq -= h;			
					if (heatReq > 0) {
						// Go to the next heat source for more heat
					}
					else {
						// Set the new heat mode
						newHeatMode = heatMode;

						fuelHeatSource.setHeatMode(newHeatMode, building);
						building.fireUnitUpdate(UnitEventType.FUEL_HEAT_EVENT);
						
						return heatGen;
					}
				}
			}
			
			// Set the new heat mode
			newHeatMode = heatMode;
			fuelHeatSource.setHeatMode(newHeatMode, building);
			building.fireUnitUpdate(UnitEventType.FUEL_HEAT_EVENT);
		}

		return heatGen;
	}

//	/**
//	 * Calculates the total amount of power that this building is CURRENTLY producing from heat sources.
//	 * 
//	 * @return power generated in kW
//	 */
//	private double calculateTotGenPower() {
//
//		double result = 0D;
//		HeatMode heatMode = building.getHeatMode();
//
//		boolean sufficientPower = building.getSettlement().getPowerGrid().isSufficientPower();
//		
//		// Calculate the unused
//		double sparePercentage = 100 - heatMode.getPercentage();
//		for (HeatSource heatSource : heatSources) {
//
//			if (heatSource.getType() == HeatSourceType.FUEL_HEATING) {
//		    	 // if there's not enough electrical power
//			    if (!sufficientPower) {
//			    	heatSource.setPercentagePower(sparePercentage);
//			    	// Note: could be cheating if the mechanism of conversion 
//			    	// is NOT properly defined
//			    	// Convert heat to electricity
//			    	result += heatSource.getCurrentPower(getBuilding());
//			    }
//		    }	
//		    
//		    else if (heatSource.getType() == HeatSourceType.THERMAL_NUCLEAR) {
//		    	heatSource.setPercentagePower(sparePercentage);
//		    	result += heatSource.getCurrentPower(getBuilding());
//		    }
//		    
//		    else if (heatSource.getType() == HeatSourceType.SOLAR_HEATING) {
//		    	heatSource.setPercentagePower(sparePercentage);
//		    	result += heatSource.getCurrentPower(getBuilding());
//		    }
//		}
//
//		return result;
//	}

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
			// Call heating's timePassing
			heating.timePassing(pulse);
			// Remove the required heat from Heating class
			double heatReq = heating.getHeatRequired();
			
//			double heatMatch = heating.getHeatMatch() * HEAT_MATCH_MOD;
			
			double heatGen = 0;
			
			// Find out how much heat can be generated to match this requirement
			if (heatReq > 0)
				heatGen = calculateHeatGen(heatReq, pulse.getElapsed());		
			// Need to update this cache value in Heating continuously
//			heatGeneratedCache = heatGen;
		
			double dev = (heatReq - heatGen);
			// A. If diff is negative, the heat load has been completely covered.
			// B. If diff is positive, the heat load has NOT been fully matched.	
//			if (dev > 0 && building.getBuildingType().contains("Greenhouse"))
//				logger.info(building, "heatDev: " + Math.round(dev * 1000.0)/1000.0);
			
			// Q: how to inform about the heat deviation ?
			
			// Update the heat deviation for this building
			setHeatDev(dev);
			
			// Update the heat generated for this building
			heating.insertHeatGenerated(heatGen);
			
			// Note: could be cheating if the mechanism of conversion is NOT properly defined
		    // Convert heat to electricity to help out
//			double powerGenerated = calculateTotGenPower();

			// Future: set new efficiency. Needs a new method in HeatSource updateEffeciency 
		}
		return valid;
	}

	/**
	 * Sets the heat deviation and call unitUpdate.
	 * 
	 * @return heat in kW.
	 */
	public void setHeatDev(double heat)  {
		heatDev = heat;
		building.fireUnitUpdate(UnitEventType.HEAT_DEV_EVENT);
	}
	
	/**
	 * Gets the heat deviation.
	 * 
	 * @return heat in kW.
	*/
	public double getHeatDev() {
		return heatDev;
	}
	
	
	public Heating getHeating() {
		return heating;
	}

	/**
	 * Gets a set of malfunction scopes.
	 */
	@Override
	public Set<String> getMalfunctionScopeStrings() {
		Set<String> set = new HashSet<>();
		String n = getFunctionType().getName();
		set.add(n);
		
		for (int x = 0; x < heatSources.size(); x++) {
			set.add(heatSources.get(x).getType().getName());
		}

		return set;
	}

	/**
	 * Gets the heat sources for the building.
	 * 
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

    /**
     * Gets the power required for heating.
     */
	public double getHeatRequired() {
		return heating.getHeatRequired();	
	}

	/**
	 * Gets the power required for generating electric heat.
	 * 
	 * @return
	 */
	public double getElectricPowerGen() {
		if (electricHeatSource == null)
			return 0;
		
		HeatMode heatMode = electricHeatSource.getHeatMode();
		
		if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
			return 0;

		return electricHeatSource.getCurrentHeat();
	}

	/**
	 * Gets the power required for generating solar heat.
	 * 
	 * @return
	 */
	public double getSolarPowerGen() {
		if (solarHeatSource == null)
			return 0;
		
		HeatMode heatMode = solarHeatSource.getHeatMode();
		
		if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
			return 0;

		return solarHeatSource.getCurrentHeat();
	}
	
	/**
	 * Gets the power required for generating thermal nuclear heat.
	 * 
	 * @return
	 */
	public double getNuclearPowerGen() {
		if (nuclearHeatSource == null)
			return 0;
		
		HeatMode heatMode = nuclearHeatSource.getHeatMode();
		
		if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
			return 0;

		return nuclearHeatSource.getCurrentHeat();
	}
	
	/**
	 * Gets the power required for generating fuel heat.
	 * 
	 * @return
	 */
	public double getFuelPowerGen() {
		if (fuelHeatSource == null)
			return 0;
		
		HeatMode heatMode = fuelHeatSource.getHeatMode();
		
		if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
			return 0;

		return fuelHeatSource.getCurrentHeat();
	}
	
	
	@Override
	public void destroy() {
		super.destroy();
		heating.destroy();
		heating = null;
		heatSources = null;
	}
}
