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
	
	private static final double THRESHOLD = .1;
	
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
	 * @param pulse
	 * @throws Exception if error during action.
	 */
	private void moderateTime(ClockPulse pulse) {
		double remaining = pulse.getElapsed();
		double pTime = Task.getStandardPulseTime();
		if (pTime == 0.0) {
			pTime = remaining;
		}
		while (remaining > 0) {
			if (remaining > pTime) {
				// Consume the pulse time.
				transferHeat(pulse, pTime);
				// Reduce the total time by the pulse time
				remaining -= pTime;
			}
			else {
				// Consume the pulse time.
				transferHeat(pulse, remaining);
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
	private void transferHeat(ClockPulse pulse, double millisols) {
		
		if (pulse.getMarsTime().getMillisol() < .2)
			return;
		
		// Call heating's timePassing
		heating.timePassing(millisols);
		
		double nowT = building.getCurrentTemperature();
		
		// Note: Since devT = tPreset - nowT
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
		
		// waterheatsink in kW. If +ve, it traps heat in a room
		// Trapped heat as a form of air heat sink is okay
		double waterHeatSink = heating.getWaterHeatSink();
	
		double devT = building.getDevTemp();
		if (devT < -25)
			devT = -25;
		else if (devT > 25)
			devT = 25;
		// Note: devT = tPreset - newT;

		// if devT is -ve, too hot. Lower heatGen
		// if devT is +ve, too cold. Raise heatGen

		double mHeatReq = 0;
		double mod = devT / 10 + 1;
		double f = 1;
		if (devT < 0) {
			// too hot, lower heatGen
			// mod from 0.0 to 1.0
		
			if (heatReq < 0) {	
				// too hot
				// heatReq: -ve
				// mod: +ve/-ve
				// mod: +ve/-ve
				mHeatReq = heatReq * mod * mod * 0.2;
				f = mod;
				// mHeatReq is -ve
			}
			else if (heatReq == 0.0) {
				mHeatReq = 0;
				// mHeatReq is -ve
			}
			else if (heatReq > 0) {
				// hot but need more heat
				// heatReq: +ve
				// mod: +ve/-ve
				mHeatReq = 0; //heatReq * mod * 0.5;
				// mHeatReq is -ve
			}
			
			waterHeatSink = 0;
			airHeatSink = 0;
		}
		else if (devT >= 0) {
			// too cold. Raise heatGen
			// mod from 1.0 to 2.0 to ...
	
			if (heatReq < 0) {	
				// cold but doesn't need more heat
				// heatReq: -ve
				// mod: +ve
				mHeatReq = heatReq * mod * -1.5;
				// mHeatReq is -ve
			}
			else if (heatReq == 0.0) {
				// heatReq: 0
				// mod: +ve
				mHeatReq = mod * mod * 2.5;
				// new heatReq is +ve, do NOT make it -ve
			}
			else if (heatReq > 0) {
				// too cold
				// heatReq: +ve
				// mod: +ve
				// mod: +ve
				mHeatReq = heatReq * mod * mod * 2.5;
				// new heatReq is +ve
			}
			
			// if airHeatSink is +ve, it's okay to reduce it. 
			// Trapped heat as a form of air heat sink is okay
			double mAir = devT / 2 * airHeatSink * .1;
			if (mAir > 3)
				airHeatSink = 3;
			else if (airHeatSink <= 0)
				airHeatSink = -1.5 + airHeatSink * .1;			
			
			// if waterHeatSink is +ve, it's okay to reduce it. 
			// Trapped heat as a form of water heat sink is okay
			double mWater = devT / 2 * waterHeatSink * .1;
			if (mWater > 3)
				waterHeatSink = 3;
			else if (waterHeatSink <= 0)
				waterHeatSink = -1.5 + waterHeatSink * .1;
		}

		double heatGen = 0;
		double remainHeatReq = 0;

		double finalHeatReq = mHeatReq - f * (.1 * preNetHeat + .15 * postNetHeat + airHeatSink + waterHeatSink);
		
		if (finalHeatReq <= THRESHOLD) {
			// input zero heat req in order to reset all heat sources to zero
			double heat[] = calculateHeatGen(0, millisols);
			heatGen = heat[0];
			remainHeatReq = heat[1];
			// Update heat generated in Heating
			heating.insertHeatGenerated(heatGen);
			// Update heat generated in ThermalGeneration
			setGeneratedHeat(heatGen);
			
			double heatSurplus = -remainHeatReq;
			// Update heat surplus in ThermalGeneration
			setHeatSurplus(heatSurplus);
			
//			logger.warning(building, 0, "3. heatGen: " + Math.round(heatGen * 1000.0)/1000.0		
//					+ "  finalHeatReq: " + Math.round(finalHeatReq * 1000.0)/1000.0
//					+ "  T: " + Math.round(nowT * 10.0)/10.0
//					+ "  devT: " + Math.round(devT * 10.0)/10.0
//					+ "  mod: " + Math.round(mod * 1000.0)/1000.0
//					+ "  mHeatReq: " + Math.round(mHeatReq * 100.0)/100.0
//					+ "  heatReq: " + Math.round(heatReq * 100.0)/100.0
//					+ "  remainHeatReq: " + Math.round(remainHeatReq * 100.0)/100.0
//					+ "  preNetHeat: " + Math.round(preNetHeat * 100.0)/100.0
//					+ "  postNetHeat: " + Math.round(postNetHeat * 100.0)/100.0
//					+ "  millisols: " + Math.round(millisols * 1000.0)/1000.0
//					);
			return;
		}
		
		// Find out how much heat can be generated to match this requirement
		double heat[] = calculateHeatGen(finalHeatReq, millisols);
		heatGen = heat[0];
		remainHeatReq = heat[1];
		
		if (remainHeatReq > 0.5) {
			logger.warning(building, 10_000L , "2. Unmet remaining heat req: " 
					+ Math.round(remainHeatReq * 100.0)/100.0 + " kW.");
		}
			
		// Update heat generated in Heating
		heating.insertHeatGenerated(heatGen);
		// Update heat generated in ThermalGeneration
		setGeneratedHeat(heatGen);
		
		double heatSurplus = -remainHeatReq;
		// Update heat surplus in ThermalGeneration
		setHeatSurplus(heatSurplus);
		
		if (devT >= 20 || devT <= -20 
			|| nowT >= 40 || nowT <= 10 
			|| heatGen >= 40
			|| heatReq >= 40 || heatReq <= -40)
//			building.getBuildingType().contains("Large Greenhouse"))
			logger.warning(building, 0, "3. heatGen: " + Math.round(heatGen * 100.0)/100.0		
					+ "  finalHeatReq: " + Math.round(finalHeatReq * 100.0)/100.0
					+ "  T: " + Math.round(nowT * 10.0)/10.0
					+ "  devT: " + Math.round(devT * 10.0)/10.0
					+ "  mod: " + Math.round(mod * 1000.0)/1000.0
					+ "  mHeatReq: " + Math.round(mHeatReq * 100.0)/100.0
					+ "  heatReq: " + Math.round(heatReq * 100.0)/100.0
					+ "  remainHeatReq: " + Math.round(remainHeatReq * 100.0)/100.0
					+ "  preNetHeat: " + Math.round(preNetHeat * 100.0)/100.0
					+ "  postNetHeat: " + Math.round(postNetHeat * 100.0)/100.0
					+ "  millisols: " + Math.round(millisols * 1000.0)/1000.0
					);
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
			moderateTime(pulse);
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
