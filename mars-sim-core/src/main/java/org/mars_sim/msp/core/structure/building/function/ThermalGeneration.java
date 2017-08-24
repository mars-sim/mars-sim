/**
 * Mars Simulation Project
 * ThermalGeneration.java
 * @version 3.1.0 2017-08-17
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.function.HeatMode;

/**
 * The ThermalGeneration class handles how the buildings of a settlement
 * generate and control temperature by heating 
 */
public class ThermalGeneration
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ThermalGeneration.class.getName());

	DecimalFormat fmt = new DecimalFormat("#.#######");

	/** TODO Name of the building function needs to be internationalized. */
	private static final BuildingFunction FUNCTION = BuildingFunction.THERMAL_GENERATION;

	// Data members.
	private double heatGenerated;
	private double heatGeneratedCache;
	private double powerGenerated;
	private double powerGeneratedCache;
	
	private boolean sufficientHeat;

	private Heating heating;
	private Building building;
	private HeatSource heatSource;
	
	private List<HeatSource> heatSources;
	
	/**
	 * Constructor
	 */
	public ThermalGeneration(Building building) {
		// Call Function constructor.
		super(FUNCTION, building);
		heating = new Heating(building);

		// Determine heat sources.
		BuildingConfig config = SimulationConfig.instance()
				.getBuildingConfiguration();
		heatSources = config.getHeatSources(building.getBuildingType());
		this.building = building;

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
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(
				FUNCTION).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!newBuilding
					&& building.getBuildingType().equalsIgnoreCase(buildingName)
					&& !removedBuilding) {
				removedBuilding = true;
			} else {
				ThermalGeneration heatFunction = (ThermalGeneration) building.getFunction(FUNCTION);
				double wearModifier = (building.getMalfunctionManager()
						.getWearCondition() / 100D) * .75D + .25D;
				supply += getHeatSourceSupply(heatFunction.heatSources,
						settlement) * wearModifier;
			}
		}

		double existingHeatValue = demand / (supply + 1D);

		BuildingConfig config = SimulationConfig.instance()
				.getBuildingConfiguration();
		double heatSupply = getHeatSourceSupply(config
				.getHeatSources(buildingName), settlement);

		return heatSupply * existingHeatValue;
	}

	/**
	 * Gets the supply value of a list of heat sources.
	 * @param heatSources list of heat sources.
	 * @param settlement the settlement.
	 * @return supply value.
	 * @throws Exception if error determining supply value.
	 */
	private static double getHeatSourceSupply(List<HeatSource> heatSources,
			Settlement settlement) {
		double result = 0D;

		Iterator<HeatSource> j = heatSources.iterator();
		while (j.hasNext()) {
			HeatSource source = j.next();
/*
			if (source instanceof ElectricHeatSource) {
				result += source.getMaxHeat();
			}
			else if (source instanceof SolarHeatSource) {
				result += source.getMaxHeat();
			}
			else if (source instanceof FuelHeatSource) {
				result += source.getMaxHeat();
			}
			else
*/				
				result += source.getMaxHeat();//.getAverageHeat(settlement);
		}

		return result;
	}
	
	/**
	 * Checks if there is enough heat in the grid for all
	 * buildings to be set to full heat.
	 * @return true if sufficient heat
	 */
	public boolean isSufficientHeat() {
		return sufficientHeat;
	}


	/**
	 * Gets the total amount of heat that this building is capable of producing (regardless malfunctions).
	 * @return heat generated in kJ/s (heat flow rate)
	 */
	// get heat from HeatSource.java
	//2014-10-24 Added getHeatGenerationCapacity()
	// Note: NOT affected by HeatMode.POWER_DOWN
	public double getHeatGenerationCapacity() {
		double result = 0D;
		Iterator<HeatSource> i = heatSources.iterator();
		while (i.hasNext()) {
			result += i.next().getMaxHeat();//.getCurrentHeat(getBuilding());
		}
		//logger.info("getGeneratedHeat() : total heat gain is " + fmt.format(result) );
		return result;
	}

	/**
	 * Gets the total amount of heat that this building is CURRENTLY producing
	 * @return heat generated in kJ/s (heat flow rate)
	 */
	// get heat from HeatSource.java
	// 2014-10-24 Modified getGeneratedHeat()
	public double getGeneratedHeat() {
		if (heatGeneratedCache != heatGenerated) {
			// if heatGeneratedCache is different from the its last value
			heatGeneratedCache = heatGenerated;
		}
		return heatGenerated; // = 0.0 if heatMode == HeatMode.POWER_DOWN
	}

	public double getGeneratedPower() {
		if (powerGeneratedCache != powerGenerated) {
			powerGeneratedCache = powerGenerated;
		}
		return powerGenerated; 
	}
	
	/**
	 * Calculate the total amount of heat that this building is CURRENTLY producing
	 * @return heat generated in kW
	 */
	// 2014-11-02 Created calculateGeneratedHeat()
	public double calculateGeneratedHeat() {

		double result = 0D;
		HeatMode heatMode = building.getHeatMode();

		// Building should only produce heat if it has no current malfunctions.
		//if (!getBuilding().getMalfunctionManager().hasMalfunction()) {
			
			if (heatMode == HeatMode.ONLINE) {
				Iterator<HeatSource> i = heatSources.iterator();
				while (i.hasNext()) {
					while (i.hasNext()) {
						HeatSource heatSource = i.next();
					    if (heatSource.getType().equals(HeatSourceType.SOLAR_HEATING)) {
					    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
					    	result += heatSource.getCurrentHeat(getBuilding());
					    }
					    else if (heatSource.getType().equals(HeatSourceType.ELECTRIC_HEATING)) {
					    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
					    	result += heatSource.getCurrentHeat(getBuilding());
					    }
					    else if (heatSource.getType().equals(HeatSourceType.FUEL_HEATING)) {
					    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
					    	result += heatSource.getCurrentHeat(getBuilding());
					    }
					}
				}
			}
			else if (heatMode == HeatMode.HALF_HEAT) {
				Iterator<HeatSource> i = heatSources.iterator();
				while (i.hasNext()) {
					HeatSource heatSource = i.next();
				    if (heatSource.getType().equals(HeatSourceType.SOLAR_HEATING)) {
				    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
				    	result = result + heatSource.getCurrentHeat(getBuilding())/2D;
				    }
				    else if (heatSource.getType().equals(HeatSourceType.ELECTRIC_HEATING)) {
				    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
				    	result = result + heatSource.getCurrentPower(getBuilding())/2D;
				    }
				    else if (heatSource.getType().equals(HeatSourceType.FUEL_HEATING)) {
				    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
				    	result = result + heatSource.getCurrentHeat(getBuilding())/2D;
				    }
				}
			}
		//}

		return result;
	}


	/**
	 * Calculate the total amount of heat that this building is CURRENTLY producing
	 * @return heat generated in kW
	 */
	// 2015-05-04 Created calculateGeneratedPower()
	public double calculateGeneratedPower() {

		double result = 0D;
		HeatMode heatMode = building.getHeatMode();

		//if (!getBuilding().getMalfunctionManager().hasMalfunction()) {
			if (heatMode == HeatMode.HEAT_OFF) {
				// at HEAT_OFF, the solar heat engine will be set to output electricity instead of heat
				Iterator<HeatSource> i = heatSources.iterator();
				while (i.hasNext()) {
					HeatSource heatSource = i.next();
				    if (heatSource.getType().equals(HeatSourceType.SOLAR_HEATING)) {
				    	//System.out.println(building.getNickName() + "'s power is " + heatSource.getCurrentPower(getBuilding()));
				    	result += heatSource.getCurrentPower(getBuilding());
				    }
				    //else if (heatSource.getType().equals(HeatSourceType.ELECTRIC_HEATING)) {
				    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
				    //	result += heatSource.getCurrentPower(getBuilding());
				    //}
				    //else if (heatSource.getType().equals(HeatSourceType.FUEL_HEATING)) {
				    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
				    //	result += heatSource.getCurrentPower(getBuilding());
				    //}
				}
			}
			else if (heatMode == HeatMode.HALF_HEAT) {
				// at HEAT_OFF, the solar heat engine will be set to output electricity instead of heat
				Iterator<HeatSource> i = heatSources.iterator();
				while (i.hasNext()) {
					HeatSource heatSource = i.next();
				    if (heatSource.getType().equals(HeatSourceType.SOLAR_HEATING)) {
				    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
				    	result = result + heatSource.getCurrentPower(getBuilding())/2D;
				    }
				    //else if (heatSource.getType().equals(HeatSourceType.ELECTRIC_HEATING)) {
				    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
				    //	result += heatSource.getCurrentPower(getBuilding())/2D;
				    //}
				    //else if (heatSource.getType().equals(HeatSourceType.FUEL_HEATING)) {
				    	//System.out.println(heatSource.toString() + " at building "+ building.getNickName() + " is HEAT_OFF");
				    //	result = result + heatSource.getCurrentPower(getBuilding())/2D;
				    //}
				}
			}
		//}

		return result;
	}

	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {

		// 2014-11-02 Added calculateGeneratedHeat()
		// Set heatGenerated at the building the furnace belongs
		heatGenerated = calculateGeneratedHeat();

		if ( heatGeneratedCache != heatGenerated) {
			// if heatGeneratedCache is different from the its last value
			heatGeneratedCache = heatGenerated;
			building.setHeatGenerated(heatGenerated);
		}

		powerGenerated = calculateGeneratedPower();

		if ( powerGeneratedCache != powerGenerated) {
			// if heatGeneratedCache is different from the its last value
			powerGeneratedCache = powerGenerated;
			//building.setPowerGenerated(powerGenerated);
		}
		
		// set new efficiency 
/*		
		for (HeatSource source : heatSources)
			if (source instanceof SolarHeatSource) {
				SolarHeatSource solarHeatSource = (SolarHeatSource) source;
				//System.out.println("solarHeatSource.getMaxHeat() is "+ solarHeatSource.getMaxHeat());
				double factor = solarHeatSource.getCurrentHeat(getBuilding()) / solarHeatSource.getMaxHeat();
				// TODO : use HeatMode.FULL_POWER ?
				double d_factor = SolarHeatSource.DEGRADATION_RATE_PER_SOL * time/1000D;
				double eff = solarHeatSource.getEfficiency() ;
				double new_eff = eff - eff * d_factor * factor;
				solarHeatSource.setEfficiency(new_eff);
				//System.out.println("new_eff is " + new_eff);
			}
			else if (source instanceof ElectricHeatSource) {
				//ElectricHeatSource electricHeatSource = (ElectricHeatSource) source;
				//System.out.println("solarHeatSource.getMaxHeat() is "+ solarHeatSource.getMaxHeat());
				//double factor = electricHeatSource.getCurrentHeat(getBuilding()) / electricHeatSource.getMaxHeat();
				//double d_factor = ElectricHeatSource.DEGRADATION_RATE_PER_SOL * time/1000D;
				//double eff = electricHeatSource.getEfficiency() ;
				//double new_eff = eff - eff * d_factor * factor;
				//electricHeatSource.setEfficiency(new_eff); // at 70% flat
				//System.out.println("new_eff is " + new_eff);
			}
			else if (source instanceof FuelHeatSource) {
				//FuelHeatSource fuelHeatSource = (FuelHeatSource) source;
				//System.out.println("solarHeatSource.getMaxHeat() is "+ solarHeatSource.getMaxHeat());
				//double factor = fuelHeatSource.getCurrentHeat(getBuilding()) / fuelHeatSource.getMaxHeat();
				//double d_factor = FuelHeatSource.DEGRADATION_RATE_PER_SOL * time/1000D;
				//double eff = fuelHeatSource.getEfficiency() ;
				//double new_eff = eff - eff * d_factor * factor;
				//fuelHeatSource.setEfficiency(5new_eff);
				//System.out.println("new_eff is " + new_eff);
			}
*/
		
		heating.timePassing(time);
	}


	public Heating getHeating() {
		return heating;
	}


	/**
	 * Gets the amount of heat required when function is at power down level.
	 * @return heat (J)
	 */
	public double getPoweredDownHeatRequired() {
		return 0D;
	}

	@Override
	public String[] getMalfunctionScopeStrings() {
		String[] result = new String[heatSources.size() + 1];
		// TODO take care to properly internationalize malfunction scope "strings"
		result[0] = getFunction().getName();

		for (int x = 0; x < heatSources.size(); x++) {
			result[x + 1] = heatSources.get(x).getType().getString();
		}

		return result;
	}

	/**
	 * Gets the heat sources for the building.
	 * @return list of heat sources.
	 */
	public HeatSource getHeatSource() {
		return heatSource;
	}


	/**
	 * Gets the heat sources for the building.
	 * @return list of heat sources.
	 */
	public List<HeatSource> getHeatSources() {
		return new ArrayList<HeatSource>(heatSources);
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
	 * Switch on or off the heating system
	 * @param true if it's on

	public void setHeating(boolean isOn) {
		heating = isOn;
	}
 */
	/**
	 * get the state of the heating system
	 * @param true if it's on

	public boolean isHeating() {
		return heating;
	}
	 */
	/**
	 * Gets the capacity of the furnace.
	 * @return heat in J

	public double getCapacity() {
		return capacity;
	}
*/
	/**
	 * Gets the efficiency
	 * @return between 0 to 1

	public double getEfficiency() {
		return efficiency;
	}
 */

	/**
	 * Gets the heat required from the grid.
	 * @return heat in J

	public double getRequiredHeat() {
		return heatRequired;
	}
*/

	public double getFullPowerRequired() {
		return getElectricPowerRequired();
	}

	public double getElectricPowerRequired() {
		HeatMode heatMode = building.getHeatMode();
		
		if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
			return 0;

		// add the need of electric heat
		double result = 0;
		Iterator<HeatSource> i = heatSources.iterator();
		while (i.hasNext()) {
			HeatSource source = i.next();
			if (source instanceof ElectricHeatSource) {
				// if it needs to be ON, use getMaxHeat() since it's the max power needed before counting in the heater efficiency 
				result = result + source.getMaxHeat()/source.getEfficiency(); 
			}
		}
		
		if (heatMode == HeatMode.HALF_HEAT)
			return result/2D;
			
		return result;
	}

	@Override
	public double getPoweredDownPowerRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Gets the amount of heat required when function is at full power.
	 * @return heat (J)
	 */
	// 2014-11-02: temporarily set getFullHeatRequired() = heatGenerated
	// thus getGeneratedHeat() = getFullHeatRequired()
	// will consolidate them into one in near future
	//public double getFullHeatRequired() {
	//	return 0;//heatGenerated;
	//}

	
	@Override
	public void destroy() {
		super.destroy();

		Iterator<HeatSource> i = heatSources.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}
		heatSources.clear();
	}

}