/*
 * Mars Simulation Project
 * ThermalGeneration.java
 * @date 2024-07-03
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.utility.heating;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.structure.building.FunctionSpec;
import com.mars_sim.core.structure.building.SourceSpec;
import com.mars_sim.core.structure.building.function.Function;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.time.ClockPulse;

/**
 * The ThermalGeneration class handles how the buildings of a settlement
 * generate and control temperature by heating .
 */
public class ThermalGeneration extends Function {

	/** default serial  id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ThermalGeneration.class.getName());
	
//	private static final double HEAT_MATCH_MOD = 1;
	
	// Data members.
	private double heatGeneratedCache;
	
	private double heatSurplusCache;

	private Heating heating;
	
	private List<HeatSource> heatSources;
	
	private HeatSource solarHeatSource;
	private HeatSource nuclearHeatSource;
	private HeatSource electricHeatSource;
	private HeatSource fuelHeatSource;
	
	private double sHeatCache;
	private double fHeatCache;
	private double eHeatCache;
	private double nHeatCache;
	
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

		double demand = settlement.getThermalSystem().getTotalHeatReq();
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
	 * Gets the total amount of generated heat that this building is CURRENTLY producing.
	 * 
	 * @return heat generated in kW (heat flow rate)
	 */
	public double getGeneratedHeat() {
		return heatGeneratedCache;
	}

	/**
	 * Sets the total amount of generated heat that this building is CURRENTLY producing.
	 * 
	 * @return heat generated in kW (heat flow rate)
	 */
	public void setGeneratedHeat(double heat) {
		heatGeneratedCache = heat;
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
	 * @return heat array {heat generated and heat required}
	 */
	private double[] calculateHeatGen(double heatLoad, double time) {
		// Assume heatLoad is positive to begin with
//		logger.info(building, 5_000 , "heatLoad: " + Math.round(heatLoad * 100.0)/100.0);
		double remainHeatReq = heatLoad;
		double heatGen = 0D;
		double heat[] = new double[2];
	
		double sHeat = 0;
		double eHeat = 0;
		double nHeat = 0;
		double fHeat = 0;
		
		HeatMode newHeatMode = null;
		
		List<HeatMode> ALL_HEAT_MODES = HeatMode.ALL_HEAT_MODES;
		HeatMode heatMode = ALL_HEAT_MODES.get(0);
		
		// Order of business: solar, nuclear, electric, and fuel

		int size = ALL_HEAT_MODES.size() - 1;
		
		if (solarHeatSource != null) {
				
			if (((SolarHeatingSource)solarHeatSource).getSunlight() > 0) {
				
				for (int i=0; i<size; i++) {
					heatMode = ALL_HEAT_MODES.get(i);
	
			    	sHeat = solarHeatSource.requestHeat(heatMode.getPercentage());
		    	
					if (Double.isNaN(sHeat) || Double.isInfinite(sHeat)) {
						logger.info(building, "SolarHeatSource has invalid heat value.");
						break;
					}
					
					double sheatReq = remainHeatReq - sHeat;
	
					if (sheatReq > 0) {
						// if the heatReq is not met, then need to go to the next percent level to raise sHeat
						// Go to the next heat source for more heat
					}
					else if (sHeat >= 0) {		
						// if the heatReq turn -ve and is therefore met, then use this percent level to generate heat
	
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
						
						heatGen += sHeat;
						remainHeatReq -= sHeat;
						
						sHeatCache = sHeat;
							
						heat[0] = heatGen;
						heat[1] = remainHeatReq;			
						return heat;
					}

				} // end of for loop	
			}
			
			if (sHeat >= 0) {
				// If the solar panel can generate electricity (i.e. not at night)
	
				heatGen += sHeat;
				remainHeatReq -= sHeat;
				
				sHeatCache = sHeat;

				// Set the new heat mode
				newHeatMode = heatMode;
				solarHeatSource.setHeatMode(newHeatMode, building);
				building.fireUnitUpdate(UnitEventType.SOLAR_HEAT_EVENT);
			}
			else {
				// If the solar panel cannot generate electricity (i.e. at night)
				solarHeatSource.setHeatMode(HeatMode.OFFLINE, building);
				building.fireUnitUpdate(UnitEventType.SOLAR_HEAT_EVENT);
			}
		}
		
		if (nuclearHeatSource != null) {
			
			for (int i=0; i<size; i++) {
				heatMode = ALL_HEAT_MODES.get(i);

		    	nHeat = nuclearHeatSource.requestHeat(heatMode.getPercentage());
				 	
				if (Double.isNaN(nHeat) || Double.isInfinite(nHeat)) {
					logger.info(building, "NuclearHeatSource has invalid heat value.");
					break;
				}
				
				double nheatReq = remainHeatReq - nHeat;

				if (nheatReq > 0) {
					// if the heatReq is not met, then need to go to the next percent level to raise sHeat

					// Go to the next heat source for more heat
				}
				else if (nHeat >= 0) {	
					// if the heatReq turn -ve and is therefore met, then use this percent level to generate heat

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
	
					heatGen += nHeat;
					remainHeatReq -= nHeat;
					
					nHeatCache = nHeat;
					
					heat[0] = heatGen;
					heat[1] = remainHeatReq;			
					return heat;
				}
			}
			
			if (nHeat >= 0) {
				// If this heat source can generate electricity
				
				heatGen += nHeat;
				remainHeatReq -= nHeat;

				nHeatCache = nHeat;
				
				// Set the new heat mode
				newHeatMode = heatMode;
				nuclearHeatSource.setHeatMode(newHeatMode, building);
				building.fireUnitUpdate(UnitEventType.NUCLEAR_HEAT_EVENT);
			}
			else {
				// If this heat source cannot generate electricity
				nuclearHeatSource.setHeatMode(HeatMode.OFFLINE, building);
				building.fireUnitUpdate(UnitEventType.NUCLEAR_HEAT_EVENT);
			}
		}
		
		if (electricHeatSource != null) {
	
			for (int i=0; i<size; i++) {
				heatMode = ALL_HEAT_MODES.get(i);
				
		    	eHeat = electricHeatSource.requestHeat(heatMode.getPercentage());
				
				if (Double.isNaN(eHeat) || Double.isInfinite(eHeat)) {
					logger.info(building, "ElectricHeatSource has invalid heat value.");
					break;
				}	
				
				double eheatReq = remainHeatReq - eHeat;
				
				if (eheatReq > 0) {
					// if the heatReq is not met, then need to go to the next percent level to raise sHeat

					// Go to the next heat source for more heat
				}
				else if (eHeat >= 0) {
					// if the heatReq turn -ve and is therefore met, then use this percent level to generate heat
	
					// Set the new heat mode
					newHeatMode = heatMode;
					
					electricHeatSource.setHeatMode(newHeatMode, building);
					building.fireUnitUpdate(UnitEventType.ELECTRIC_HEAT_EVENT);
					
					// Turn off fuel heat
					if (fuelHeatSource != null) {
						fuelHeatSource.setHeatMode(HeatMode.OFFLINE, building);
						building.fireUnitUpdate(UnitEventType.FUEL_HEAT_EVENT);
					}
				
					heatGen += eHeat;
					remainHeatReq -= eHeat;
					
					eHeatCache = eHeat;
					
					heat[0] = heatGen;
					heat[1] = remainHeatReq;		
					return heat;
				}
			}
			
			if (eHeat >= 0) {
				// If this heat source can generate electricity
				
				heatGen += eHeat;
				remainHeatReq -= eHeat;
				
				eHeatCache = eHeat;
				
				// Set the new heat mode
				newHeatMode = heatMode;
				electricHeatSource.setHeatMode(newHeatMode, building);
				building.fireUnitUpdate(UnitEventType.ELECTRIC_HEAT_EVENT);
			}
			else {
				// If this heat source cannot generate electricity
				electricHeatSource.setHeatMode(HeatMode.OFFLINE, building);
				building.fireUnitUpdate(UnitEventType.ELECTRIC_HEAT_EVENT);
			}
		}
		
		if (fuelHeatSource != null) {
	
			for (int i=0; i<size; i++) {
				heatMode = ALL_HEAT_MODES.get(i);
				
				fuelHeatSource.setTime(time);
		    	fHeat = fuelHeatSource.requestHeat(heatMode.getPercentage());
				
				if (Double.isNaN(fHeat) || Double.isInfinite(fHeat)) {
					logger.info(building, "FuelHeatSource has invalid heat value.");
					break;
				}	
				
				double fheatReq = remainHeatReq - fHeat;
				
				if (fheatReq > 0) {
					// if the heatReq is not met, then need to go to the next percent level to raise sHeat
					// Go to the next heat source for more heat
				}
				else if (fHeat >= 0) {
					// if the heatReq turn -ve and is therefore met, then use this percent level to generate heat
	
					// Set the new heat mode
					newHeatMode = heatMode;

					fuelHeatSource.setHeatMode(newHeatMode, building);
					building.fireUnitUpdate(UnitEventType.FUEL_HEAT_EVENT);
					
					heatGen += fHeat;
					remainHeatReq -= fHeat;
					
					fHeatCache = fHeat;
					
					heat[0] = heatGen;
					heat[1] = remainHeatReq;			
					return heat;
				}
			}
			
			if (fHeat >= 0) {
				
				heatGen += fHeat;
				remainHeatReq -= fHeat;
				
				fHeatCache = fHeat;
				
				// Set the new heat mode
				newHeatMode = heatMode;
				fuelHeatSource.setHeatMode(newHeatMode, building);
				building.fireUnitUpdate(UnitEventType.FUEL_HEAT_EVENT);
			}
			else {
				fuelHeatSource.setHeatMode(HeatMode.OFFLINE, building);
				building.fireUnitUpdate(UnitEventType.FUEL_HEAT_EVENT);
			}
		}

//		if (sHeat + nHeat + eHeat + fHeat > 0) {
			heat[0] = heatGen;
			heat[1] = remainHeatReq;			
//		}
		return heat;
	}


	/**
	 * Moderate the time for heating.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during action.
	 */
	private void moderateTime(double time) {
		double remaining = time;
		double pTime = Task.getStandardPulseTime();
		while (remaining > 0 && pTime > 0) {
			if (remaining > pTime) {
				// Consume the pulse time.
				transferHeat(pTime);
				// Reduce the total time by the pulse time
				remaining -= pTime;
			}
			else {
				// Consume the pulse time.
				transferHeat(remaining);
				// Reduce the total time by the pulse time
				remaining = 0;
			}
		}
	}
	
	/**
	 * Transfers the heat.
	 * 
	 * @param millisols time in millisols
	 * @throws Exception if error during action.
	 */
	private void transferHeat(double millisols) {
		// Call heating's timePassing
		heating.timePassing(millisols);
		
		// Note: Since devT = tPreset - currentT
		// and heatReq = convFactor * devT,
		
		// If heatReq is -ve, then devT is -ve, currentT is higher than normal
		// no need to turn on heating
		// If heatReq is +ve, then devT is +ve, currentT is lower than normal
		// need to turn on heating	
		double heatReq = heating.getHeatRequired();

		// postNetHeat is +ve, then gain is greater than loss
		double postNetHeat = heating.getPostNetHeat();
		
		// preNetHeat is +ve, then gain is greater than loss
		double preNetHeat = heating.getPreNetHeat();

		// airheatsink in kW. If +ve, it traps heat in a room
		double airHeatSink = heating.getAirHeatSink();
		
		// if airHeatSink is +ve, it's okay to reduce it. 
		// Trapped heat as a form of air heat sink is okay
		if (airHeatSink > 0)
			airHeatSink = 0.25 * airHeatSink;
		else if (airHeatSink <= 0)
			airHeatSink = -1.5 + airHeatSink;
		
		// waterheatsink in kW. If +ve, it traps heat in a room
		// Trapped heat as a form of air heat sink is okay
		double waterHeatSink = heating.getWaterHeatSink();
				
		// if waterHeatSink is +ve, it's okay to reduce it. 
		// Trapped heat as a form of water heat sink is okay
		if (waterHeatSink > 0)
			waterHeatSink = 0.25 * waterHeatSink;
		else if (waterHeatSink <= 0)
			waterHeatSink = -1.5 + waterHeatSink;
		
		double heatGen = 0;
		double remainHeatReq = 0;
		
//		if (heatReq + airHeatSink + waterHeatSink <= 0) {
//			// Still let it call calculateHeatGen in order to turn off heat sources
//			double heat[] = calculateHeatGen(0, millisols);	
//			heatGen = heat[0];
//			remainHeatReq = heat[1];
//		
//			if (heatGen > 40 || heatReq > 40 || heatReq < -40) {
//				logger.warning(building, 1_000L , "1. heatGen: " 
//						+ Math.round(heatGen * 1000.0)/1000.0
//						+ "  T: " + Math.round(building.getCurrentTemperature() * 10.0)/10.0
//						+ "  millisols: " + Math.round(millisols * 1000.0)/1000.0
//						+ "  heatReq: " + Math.round(heatReq * 1000.0)/1000.0
//						+ "  remainHeatReq: " + Math.round(remainHeatReq * 1000.0)/1000.0
//						+ "  preNetHeat: " + Math.round(preNetHeat * 1000.0)/1000.0
//						+ "  postNetHeat: " + Math.round(postNetHeat * 1000.0)/1000.0);
//			}
//		}
//		else {
			double finalHeatReq = heatReq - .5 * airHeatSink - .5 * postNetHeat - airHeatSink - waterHeatSink;		
			// Find out how much heat can be generated to match this requirement
			double heat[] = calculateHeatGen(finalHeatReq, millisols);
			heatGen = heat[0];
			remainHeatReq = heat[1];
			
			if (remainHeatReq > 0.5) {
				logger.warning(building, 10_000L , "2. Unmet remaining heat req: " 
						+ Math.round(remainHeatReq) + " kW.");
			}
			
			if (heatGen > 40 || heatReq > 40 || heatReq < -40) {
				logger.warning(building, 1_000L , "3. heatGen: " 
						+ Math.round(heatGen * 1000.0)/1000.0
						+ "  T: " + Math.round(building.getCurrentTemperature() * 10.0)/10.0
						+ "  time: " + Math.round(millisols * 1000.0)/1000.0
						+ "  heatReq: " + Math.round(heatReq * 1000.0)/1000.0
						+ "  remainHeatReq: " + Math.round(remainHeatReq * 1000.0)/1000.0
						+ "  preNetHeat: " + Math.round(preNetHeat * 1000.0)/1000.0
						+ "  postNetHeat: " + Math.round(postNetHeat * 1000.0)/1000.0
						+ "  finalHeatReq: " + Math.round(finalHeatReq * 1000.0)/1000.0);
			}
//		}		
		

		
		// Update heat generated continuously
		heating.insertHeatGenerated(heatGen);
		setGeneratedHeat(heatGen);
		
		// Note that the following can happen : 
		// Lander Hab 1 - heatGen: 39.655 > 50 kW.  heatReq: 5.02  remainHeatReq: -9.589  finalHeatReq: 30.066
		// Lander Hab 1 - diffHeatGainLoss: 38.797181908421216 > 20.
		// This will cause an over-abundance of heat gain.
		// Need to be cautious about remainHeatReq not exceeding a certain amount
		
		double heatSurplus = -remainHeatReq;

		setHeatSurplus(heatSurplus);
	}
	
	/**
	 * Sets the heat surplus (excess heat generated) and call unitUpdate.
	 * 
	 * @return heat in kW.
	 */
	public void setHeatSurplus(double heat)  {
		heatSurplusCache = heat;
		building.fireUnitUpdate(UnitEventType.HEAT_SURPLUS_EVENT);
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
			moderateTime(pulse.getElapsed());
		}
		return valid;
	}
	
	/**
	 * Gets the heat surplus (excess heat generated).
	 * 
	 * @return heat in kW.
	*/
	public double getHeatSurplus() {
		return heatSurplusCache;
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
	 * Gets the solar heat source.
	 * 
	 * @return
	 */
	public HeatSource getSolarHeatSource() {
		return solarHeatSource;
	}

	/**
	 * Gets the electric heat source.
	 * 
	 * @return
	 */
	public HeatSource getElectricHeatSource() {
		return electricHeatSource;
	}

	/**
	 * Gets the fuel heat source.
	 * 
	 * @return
	 */
	public HeatSource getFuelHeatSource() {
		return fuelHeatSource;
	}

	/**
	 * Gets the nuclear heat source.
	 * 
	 * @return
	 */
	public HeatSource getNuclearHeatSource() {
		return nuclearHeatSource;
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
		
		if (heatMode == HeatMode.OFFLINE)
			return 0;

		return electricHeatSource.getCurrentPower();
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
		
		if (heatMode == HeatMode.OFFLINE)
			return 0;

		return solarHeatSource.getCurrentPower();
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
		
		if (heatMode == HeatMode.OFFLINE)
			return 0;

		return nuclearHeatSource.getCurrentPower();
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
		
		if (heatMode == HeatMode.OFFLINE)
			return 0;

		return fuelHeatSource.getCurrentPower();
	}
	
	/**
	 * Gets the solar heat cache.
	 * 
	 * @return
	 */
	public double getSolarHeat() {
		return sHeatCache;
	}
	
	/**
	 * Gets the fuel heat cache.
	 * 
	 * @return
	 */
	public double getFuelHeat() {
		return fHeatCache;
	}
	
	/**
	 * Gets the electric heat cache.
	 * 
	 * @return
	 */
	public double getElectricHeat() {
		return eHeatCache;
	}
	
	/**
	 * Gets the nuclear heat cache.
	 * 
	 * @return
	 */
	public double getNuclearHeat() {
		return nHeatCache;
	}
	
	
	@Override
	public void destroy() {
		super.destroy();
		heating.destroy();
		heating = null;
		heatSources = null;
	}
}
