/*
 * Mars Simulation Project
 * PowerGrid.java
 * @date 2024-06-28
 * @author Scott Davis
 */
package com.mars_sim.core.building.utility.power;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class is a settlement's building power grid.
 */
public class PowerGrid implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(PowerGrid.class.getName());

//	private static final double R_LOAD = 1000D; // assume constant load resistance

	private static final double ROLLING_FACTOR = 1.1D; 
	
	private static final double PERC_AVG_VOLT_DROP = 98D;

	public static final double HOURS_PER_MILLISOL = MarsTime.HOURS_PER_MILLISOL; 
	
	/** The flag for checking if the simulation has just started. */
	private boolean justLoaded = true;
	private boolean sufficientPower;
	
	private double degradationRatePerSol = .0004D;
	private double systemEfficiency = 1D;
	private double powerGenerated;
	private double totalEnergyStored;
	private double energyStorageCapacity;
	private double powerRequired;
	private double powerValue;

	private PowerMode powerMode;
	
	private Settlement settlement;
	private BuildingManager manager;


	/**
	 * Constructor.
	 */
	public PowerGrid(Settlement settlement) {
		this.settlement = settlement;
		manager = settlement.getBuildingManager();
		powerMode = PowerMode.FULL_POWER;
		powerGenerated = 0D;
		totalEnergyStored = 0D;
		energyStorageCapacity = 0D;
		powerRequired = 0D;
		sufficientPower = true;
	}

	/**
	 * Gets the power grid mode.
	 * 
	 * @return power grid mode
	 */
	public PowerMode getPowerMode() {
		return powerMode;
	}

	/**
	 * Sets the power grid power mode.
	 * 
	 * @param newPowerMode the new power grid power mode.
	 */
	public void setPowerMode(PowerMode newPowerMode) {
		if (powerMode != newPowerMode) {
			powerMode = newPowerMode;
			settlement.fireUnitUpdate(UnitEventType.POWER_MODE_EVENT);
		}
	}

	/**
	 * Gets the generated power in the grid.
	 * 
	 * @return power in kW
	 */
	public double getGeneratedPower() {
		return powerGenerated;
	}

	/**
	 * Sets the generated power in the grid.
	 * 
	 * @param newGeneratedPower the new generated power (kW).
	 */
	private void setGeneratedPower(double newGeneratedPower) {
		double p = Math.round(newGeneratedPower*1000.0)/1000.0;
		if (powerGenerated != p) {
			powerGenerated = p;
			settlement.fireUnitUpdate(UnitEventType.GENERATED_POWER_EVENT);
		}
	}

	/**
	 * Gets the stored energy in the grid.
	 * 
	 * @return stored energy in kWh.
	 */
	public double getStoredEnergy() {
		return totalEnergyStored;
	}

	/**
	 * Displays the stored energy in kWh and its percent capacity.
	 * 
	 * @return
	 */
	public String displayStoredEnergy() {
		double stored = totalEnergyStored;
		if (stored < 0D || Double.isNaN(stored) || Double.isInfinite(stored))
			return "";
		
		double percent = stored / energyStorageCapacity * 100;
		
		StringBuilder sb = new StringBuilder();
		sb.append(Math.round(stored *10.0)/10.0)
		.append(" (")
		.append(Math.round(percent *10.0)/10.0)
		.append(" %)");
		
		return sb.toString();
	}
	
	/**
	 * Sets the stored energy in the grid.
	 * 
	 * @param newEnergyStored the new stored energy (kWh).
	 */
	public void setStoredEnergy(double newEnergyStored) {
		if (totalEnergyStored != newEnergyStored) {
			totalEnergyStored = newEnergyStored;
			settlement.fireUnitUpdate(UnitEventType.STORED_ENERGY_EVENT);
		}
	}

	/**
	 * Gets the stored energy capacity in the grid.
	 * 
	 * @return stored energy capacity in kWh.
	 */
	public double getStoredEnergyCapacity() {
		return energyStorageCapacity;
	}

	/**
	 * Sets the total stored energy capacity in the grid.
	 * 
	 * @param newCap the new stored energy capacity (kWh).
	 */
	public void setStoredEnergyCapacity(double newCap) {
		if (energyStorageCapacity != newCap) {
			energyStorageCapacity = newCap;
			settlement.fireUnitUpdate(UnitEventType.STORED_ENERGY_CAPACITY_EVENT);
		}
	} 

	/**
	 * Gets the power required from the grid.
	 * 
	 * @return power in kW
	 */
	public double getRequiredPower() {
		return powerRequired;
	}

	/**
	 * Sets the required power in the grid.
	 * 
	 * @param newRequiredPower the new required power (kW).
	 */
	private void setRequiredPower(double newRequiredPower) {
		if (powerRequired != newRequiredPower) {
			powerRequired = newRequiredPower;
			settlement.fireUnitUpdate(UnitEventType.REQUIRED_POWER_EVENT);
		}
	}

	/**
	 * Checks if there is enough power in the grid for all buildings to be set to
	 * full power.
	 * 
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
	@Override
	public boolean timePassing(ClockPulse pulse) {

		logger.log(settlement, Level.FINEST, 0, Msg.getString("PowerGrid.log.settlementPowerSituation", settlement.getName()));

		// update the total power generated in the grid.
		double powerGen = updateTotalPowerGenerated();

		// Determine total power required in the grid.
		double powerReq = updateTotalRequiredPower();

		// Update overall grid efficiency.
		updateEfficiency(pulse.getElapsed());

		// Update the power flow.
		double powerDiff = powerReq * ROLLING_FACTOR - powerGen;
		sufficientPower = (powerDiff < 0);
			
		// Run at the start of the sim once only
		if (justLoaded				
//			&& pulse.getMarsTime().getMissionSol() == 1
				&& pulse.getMarsTime().getMillisolInt() >= 1) {
					// Reset justLoaded
					justLoaded = false;
		}

		if (!justLoaded) {			
			// May add back for debugging : logger.info(settlement, 0, "neededPower: " + Math.round(neededPower) + "  powerGenerated: " + Math.round(powerGen) + "  powerRequired: " + Math.round(powerReq))
		
			if (powerDiff < 0) {
				handleExcessPower(pulse.getElapsed(), powerDiff);
			}
			else {
				handleLackOfPower(pulse.getElapsed(), powerDiff);
			}
		}
		
		// Update the total power storage capacity in the grid.
		updateTotalEnergyStorageCapacity();

		// Update the total power stored in the grid.
		updateTotalStoredEnergy();

		// Update power value.
		determinePowerValue();
		
		return true;
	}

	/**
	 * Updates the system efficiency factor.
	 * 
	 * @param time
	 */
	private void updateEfficiency(double time) {
		double dFactor = degradationRatePerSol * time / 1000D;
		systemEfficiency = systemEfficiency * (1 - dFactor);
	}


	/**
	 * Calculates the amount of electrical power generated.
	 * 
	 * @param increaseLoad
	 * @param neededPower
	 * @return net power change in kW
	 */
	public double stepUpDownPower(boolean increaseLoad, double neededPower) {
		double netPower = 0D;

		for(Building b : manager.getBuildingSet(FunctionType.POWER_GENERATION)) {
			for( PowerSource powerSource : b.getPowerGeneration().getPowerSources()) {
				double previous = powerSource.getCurrentPower(b);
				if (powerSource instanceof AdjustablePowerSource fps) {
					if (increaseLoad) {
						fps.increaseLoadCapacity();
					}
					else {
						fps.decreaseLoadCapacity();
					}

					double net = powerSource.getCurrentPower(b) - previous;
					if (Double.isFinite(net)) {
						netPower += net;
						neededPower -= netPower;
						if (neededPower <= 0) {
							return netPower;
						}
					}
				}
			}
		}
		return netPower;
	}
	
		
	/**
	 * Handles excess power.
	 * 
	 * @param time 
	 * @param neededPower
	 */
	private void handleExcessPower(double time, double neededPower) {
		double excess = -neededPower;
//		logger.info(settlement, 10_000, "excess: " + Math.round(excess));
		sufficientPower = true;
		
		Set<Building> buildings = manager.getBuildingSet(FunctionType.POWER_GENERATION);

		// 1. Switch from no power to low power in inhabitable buildings
		// building until required power reduction is met.
		double netPower1 = adjustPowerLevel(false, excess, buildings, 
				true, PowerMode.NO_POWER, PowerMode.LOW_POWER);
		
		excess -= netPower1;
		// Update the total generated power
		setGeneratedPower(powerGenerated - netPower1);
		
		if (excess < 0) {
			sufficientPower = false;
			return;
		}
		

		// 2. Switch from low power to full power mode on inhabitable buildings
		
		// If power needs are still not met, turn on full power in each inhabitable
		// building until required power reduction is met.
		double netPower2 = adjustPowerLevel(false, neededPower, buildings, 
				true, PowerMode.LOW_POWER, PowerMode.FULL_POWER);
		
		excess -= netPower2;
		// Update the total generated power
		setGeneratedPower(powerGenerated - netPower2);
		
		if (excess < 0) {
			sufficientPower = false;
			return;
		}
		
		// 3. Turn off emergency power generators. Have excess power. No need of 
		//    using methane power generators to produce electricity
		
		// C1. Turn off methane power generators 
		double methanePower = adjustPowerLevelFunctionType(false, excess, buildings, 
				FunctionType.POWER_GENERATION, PowerSourceType.FUEL_POWER);
		
		excess -= methanePower;
		// Update the total generated power
		setGeneratedPower(powerGenerated - methanePower);
		
		if (excess < 0) {
			sufficientPower = false;
			return;
		}
		
		// 4. Turn on low power mode on non-inhabitable buildings
		
		// Switch from no power to low power in each non-inhabitable
		// building until required power reduction is met.
		double netPower3 = adjustPowerLevel(false, excess, buildings, 
				false, PowerMode.NO_POWER, PowerMode.LOW_POWER);
		
		excess -= netPower3;
		// Update the total generated power
		setGeneratedPower(powerGenerated - netPower3);
		
		if (excess < 0) {
			sufficientPower = false;
			return;
		}
		

		// 5. Turn on full power mode on non-inhabitable buildings
		
		// Switch from low power to full power in each non-inhabitable
		// building until required power reduction is met.
		double netPower4 = adjustPowerLevel(false, neededPower, buildings, 
				false, PowerMode.LOW_POWER, PowerMode.FULL_POWER);
		
		excess -= netPower4;
		// Update the total generated power
		setGeneratedPower(powerGenerated - netPower4);
		
		if (excess < 0) {
			sufficientPower = false;
			return;
		}
			
		// 6. Store excess power in power storage buildings.

		double timeHr = time * HOURS_PER_MILLISOL;
		double excessEnergy = excess * timeHr * systemEfficiency;
		double unableToStoreEnergy = storeExcessPower(excessEnergy, timeHr);
		double excessPower = unableToStoreEnergy / timeHr / systemEfficiency;
		// Update the total generated power
		setGeneratedPower(powerGenerated - excessPower);
		
		if (excess < 0) {
			sufficientPower = false;
			return;
		}
		
		// 7. Step down the capacity of the fission power plant by a small percent
		int rand = RandomUtil.getRandomInt(9);
		if (rand == 9) {
			double netPower02 = stepUpDownPower(false, excess);
			excess -= netPower02;
			// Update the total generated power
			setGeneratedPower(powerGenerated + netPower02);
			
			if (excess < 0) {
				sufficientPower = false;
				return;
			}
		}
		
		// 8. Step down the capacity of the fission power plant by a small percent
		double netPower02 = stepUpDownPower(false, excess);
		excess -= netPower02;
		// Update the total generated power
		setGeneratedPower(powerGenerated + netPower02);
		
		if (excess < 0) {
			sufficientPower = false;
			return;
		}
		
		// 9. Store excess power in power storage buildings.

		excessEnergy = excess * timeHr * systemEfficiency;
		unableToStoreEnergy = storeExcessPower(excessEnergy, timeHr);
		excessPower = unableToStoreEnergy / timeHr / systemEfficiency;
		// Update the total generated power
		setGeneratedPower(powerGenerated - excessPower);
		
		if (excess < 0) {
			sufficientPower = false;
			return;
		}
	}

	/**
	 * Handles the demand for power by ramping up the power generation.
	 * 
	 * @param time
	 * @param neededPower
	 */
	private void handleLackOfPower(double time, double neededPower) {
		// May add back for debugging: logger.info(settlement, "neededPower: " + Math.round(neededPower))
		// insufficient power produced, need to pull energy from batteries to meet the
		// demand
		sufficientPower = false;
		
		// 1. Increases the load capacity of fission reactors if available
		double fissionPower0 = stepUpDownPower(true, neededPower);

		neededPower -= fissionPower0;
		// Update the total generated power with contribution from increased power load capacity of fission reactors
		setGeneratedPower(powerGenerated + fissionPower0);
		
		// if the fission reactors produces more than enough
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}

		double timeInHour = time * HOURS_PER_MILLISOL; 
	
		// 2. Retrieve power from the grid battery for the first time
		neededPower = retrievePowerGridBattery(timeInHour, neededPower);

		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}
		
		Set<Building> buildings = manager.getBuildingSet(FunctionType.POWER_GENERATION);
		
		// 3. Turn on emergency power generators to supplement power production
		
		// If still not having sufficient power,
		// turn on methane generators to low power mode if available
		double methanePower0 = adjustPowerLevelFunctionType(true, neededPower, buildings, 
				FunctionType.POWER_GENERATION, PowerSourceType.FUEL_POWER);

		neededPower -= methanePower0;
		// Update the total generated power with contribution from methane generators
		setGeneratedPower(powerGenerated + methanePower0);
		
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}
		
		// 4. Increases the load capacity of fission reactors if available
		double fissionPower1 = stepUpDownPower(true, neededPower);

		neededPower -= fissionPower1;
		// Update the total generated power with contribution from increased power load capacity of fission reactors
		setGeneratedPower(powerGenerated + fissionPower1);
		
		// if the fission reactors produces more than enough
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}
		
//		// 5. Retrieve power from the grid battery for the second time
//		neededPower = retrievePowerGridBattery(timeInHour, neededPower);
//		
//		if (neededPower < 0) {
//			sufficientPower = true;
//			return;
//		}
		
		// 6. If still not having sufficient power, reduce power to some buildings

		// Reduce each non-inhabitable building's full power mode to low power until
		// required power reduction is met.
		double savedPower0 = adjustPowerLevel(true, neededPower, buildings, 
				false, PowerMode.FULL_POWER, PowerMode.LOW_POWER);
		
		neededPower -= savedPower0;
		// Update the total generated power
		setGeneratedPower(powerGenerated + savedPower0);
		
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}

		// 7. If still not having sufficient power,
		// turn on methane generators to full power mode if available
		double methanePower1 = adjustPowerLevelFunctionType(true, neededPower, buildings, 
				FunctionType.POWER_GENERATION, PowerSourceType.FUEL_POWER);

		neededPower -= methanePower1;	
		// Update the total generated power with contribution from methane generators
		setGeneratedPower(powerGenerated + methanePower1);
		
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}
	
		// 8. Increases the load capacity of fission reactors if available
		double fissionPower2 = stepUpDownPower(true, neededPower);

		neededPower -= fissionPower2;
		// Update the total generated power with contribution from increased power load capacity of fission reactors
		setGeneratedPower(powerGenerated + fissionPower2);
		
		// if the fission reactors produces more than enough
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}
		
//		// 9. Retrieve power from the grid battery for the third time
//		neededPower = retrievePowerGridBattery(timeInHour, neededPower);
//		
//		if (neededPower < 0) {
//			sufficientPower = true;
//			return;
//		}
		
		// 10. If power needs are still not met, turn off the power in each
		// non-inhabitable building until required power reduction is met.
		double savedPower1 = adjustPowerLevel(true, neededPower, buildings, 
				false, PowerMode.LOW_POWER, PowerMode.NO_POWER);
			
		neededPower -= savedPower1;
		// Update the total generated power
		setGeneratedPower(powerGenerated + savedPower1);
		
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}
		
//		// 11. Retrieve power from the grid battery for the fourth time
//		neededPower = retrievePowerGridBattery(timeInHour, neededPower);
//		
//		if (neededPower < 0) {
//			sufficientPower = true;
//			return;
//		}
		
		// 12. If power needs are still not met, turn on the low power in each inhabitable
		// building until required power reduction is met.
		double savedPower2 = adjustPowerLevel(true, neededPower, buildings, 
				true, PowerMode.FULL_POWER, PowerMode.LOW_POWER);
		
		neededPower -= savedPower2;
		// Update the total generated power
		setGeneratedPower(powerGenerated + savedPower2);
		
		if (neededPower < 0) {
			sufficientPower = true;
		}
		
		// 13. Retrieve power from the grid battery for the fifth time
//		neededPower = retrievePowerGridBattery(timeInHour, neededPower);
//		
//		if (neededPower < 0) {
//			sufficientPower = true;
//			return;
//		}
		
//		// 14. If power needs are still not met, turn off the power in each inhabitable
//		// building until required power reduction is met.
//		double savedPower3 = adjustPowerLevel(true, neededPower, buildings, 
//				true, PowerMode.LOW_POWER, PowerMode.NO_POWER);
//		
//		neededPower -= savedPower3;
//		// Update the total generated power
//		setGeneratedPower(powerGenerated + savedPower3);
//		
//		if (neededPower < 0) {
//			sufficientPower = true;
//		}
	}
		
	/**
	 * Retrieves power from the grid battery.
	 * 
	 * @param timeInHour
	 * @param neededPower
	 * @return
	 */
	private double retrievePowerGridBattery(double timeInHour, double neededPower) {
		double newNeededPower = neededPower;
		// Assume the gauge of the cable is uniformly low, as represented by percentAverageVoltageDrop
		// Future: account for the distance of the separation between endpoints
		double neededEnergy = neededPower * timeInHour / PERC_AVG_VOLT_DROP * 100D;

		// Assume the energy flow is instantaneous and
		// subtract powerHr from the battery reserve
		double retrieved = retrieveStoredEnergy(neededEnergy, timeInHour);

		double batteryPower = retrieved / timeInHour;
		
		newNeededPower -= batteryPower;
		
		// Note: battery delivered power can help meet up with the power demand
		// However, currently, it's NOT recommended mixing it with the power generated by
		// other means or else it's difficult to know if a settlement is producing enough power
		// since battery only stores energy and releases it at a later date and is not
		// considered a source of power.
		
		// Do NOT update the total generated power with contribution from batteries: setGeneratedPower(powerGenerated + batteryPower);
	
		return newNeededPower;
	}
	
	/**
	 * Adjust the power level in inhabitable and non-inhabitable buildings.
	 * 
	 * @param gridLackPower true if the power grid has insufficient power
	 * @param powerToHandle either the excess power or the needed power
	 * @param buildings
	 * @param lifeSupport
	 * @param oldPowerMode
	 * @param newPowerMode
	 * @return the power that can be saved or the power that can be supplied after switching to the new power mode
	 */
	private double adjustPowerLevel(boolean gridLackPower, double powerToHandle, Set<Building> buildings, 
			boolean lifeSupport, PowerMode oldPowerMode, PowerMode newPowerMode) {
		// Make netPower always positive 
		double netPower = 0;
	
		Iterator<Building> i = buildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();

			boolean life = building.hasFunction(FunctionType.LIFE_SUPPORT);
			
			boolean isGenerator = building.getCategory() == BuildingCategory.POWER;
			if (isGenerator && !life)
				continue;
			
			PowerMode thisOldPM = building.getPowerMode();
			boolean morePower = canGenMoreThanLoad(building, newPowerMode);
			double power = 0;
			
			if (lifeSupport == life // && (!gridLackPower && morePower)
					&& thisOldPM == oldPowerMode) {
			
				/////// IF LACK OF POWER /////// 
				// For stepping down power
				if (oldPowerMode == PowerMode.FULL_POWER
					&& newPowerMode == PowerMode.LOW_POWER) {
					power = building.getFullPowerRequired()
							- building.getLowPowerRequired();
				}
				// For stepping down power
				else if (oldPowerMode == PowerMode.LOW_POWER
					&& newPowerMode == PowerMode.NO_POWER) {
					power = building.getLowPowerRequired();
				}
				
				/////// IF HAVING EXCESS POWER /////// 
				// For stepping up power
				else if (oldPowerMode == PowerMode.LOW_POWER
					&& newPowerMode == PowerMode.FULL_POWER) {
					power = building.getFullPowerRequired()
							- building.getLowPowerRequired();
				}
				// For stepping up power
				else if (oldPowerMode == PowerMode.NO_POWER
					&& newPowerMode == PowerMode.LOW_POWER) {
					power = building.getLowPowerRequired();
				}
				else {
					continue;
				}
							
				powerToHandle -= power;
				if (powerToHandle > 0) {
					netPower += power;
					// In case of excess power, +ve powerToHandle means it can handle the stepping up of power 
					// In case of lacking power, +ve powerToHandle means it can handle the stepping up of power 					
					// Switch from one power mode to another
					building.setPowerMode(newPowerMode);
					logger.info(building, "1. Power Mode: " + oldPowerMode.getName()
							+ " -> " + newPowerMode.getName());
				}
				
				else {
					// When lacking power and needing to step up power level
					if (gridLackPower && !morePower) {
						netPower += power;
						// Switch from one power mode to another
						building.setPowerMode(newPowerMode);	
						logger.info(building, "2. Power Mode: " + oldPowerMode.getName()
								+ " -> " + newPowerMode.getName());
					}

					return netPower;
				}
			}
		}
		return netPower;
	}
	
	/**
	 * Adjust the power level in power generating buildings.
	 * Note: for now, use only in methane power
	 * 
	 * @param stepUp turning up power level
	 * @param originalPower
	 * @param buildings
	 * @param functionType
	 * @param powerSourceType
	 * @return
	 */
	private double adjustPowerLevelFunctionType(boolean stepUp, double originalPower, 
			Set<Building> buildings, FunctionType functionType,
			PowerSourceType powerSourceType) {

		double newPower = originalPower;
	
		Iterator<Building> i = buildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
		
			if (!building.hasFunction(functionType))
				continue;
			
			boolean isGenerator = building.getCategory() == BuildingCategory.POWER;
			if (isGenerator && !stepUp)
				continue;
			
			List<PowerSource> sources = building.getPowerGeneration().getPowerSources();
			if (sources.isEmpty())
				continue;
			
			Iterator<PowerSource> j = sources.iterator();
			while (j.hasNext()) {
				PowerSource source = j.next();			
				if (source.getType() != powerSourceType)
					continue;
				
				FuelPowerSource fps = (FuelPowerSource)source; 
				
				boolean isOn = fps.isToggleON();
				
				double power = 0;
				
				if (stepUp && !isOn) {
								
					fps.toggleON();
					
					power = fps.getCurrentPower(building);					
				}
				
				else {
					
					if (isOn)
						power = fps.measurePower(100);
					
					fps.toggleOFF();	
				}
		
				newPower -= power;
				
				if (newPower < 0)
					return newPower;
			}
		}
		return newPower;
	}
	
	/**
	 * Updates the total power generated in the grid.
	 * 
	 * @return
	 * @throws BuildingException if error determining total power generated.
	 */
	private double updateTotalPowerGenerated() {
		// Add the power generated by all power generation buildings.
		double power = manager.getBuildingSet(FunctionType.POWER_GENERATION).stream()
								.mapToDouble(b -> b.getPowerGeneration().getGeneratedPower())
								.sum();
		setGeneratedPower(power);
		return power;
	}

	/**
	 * Updates the total energy stored in the grid.
	 * 
	 * @throws BuildingException if error determining total energy stored.
	 */
	private void updateTotalStoredEnergy() {
		double store = manager.getBuildingSet(FunctionType.POWER_STORAGE).stream()
								.mapToDouble(b -> b.getPowerStorage().getBattery().getCurrentStoredEnergy())
								.sum();
		setStoredEnergy(store);
	}

	/**
	 * Updates the total power required in the grid.
	 * 
	 * @return
	 * @throws BuildingException if error determining total power required.
	 */
	private double updateTotalRequiredPower() {
		double power = manager.getBuildingSet(FunctionType.POWER_GENERATION).stream()
				.mapToDouble(b -> b.getFullPowerRequired())
				.sum();
		// Note: need to check the required power in full power mode 
		// so that the return value will show how much power needed
		setRequiredPower(power);
		
		return power;
	}

	/**
	 * Updates the total energy storage capacity in the grid.
	 * 
	 * @throws BuildingException if error determining total energy storage capacity.
	 */
	private void updateTotalEnergyStorageCapacity() {
		double capacity = manager.getBuildingSet(FunctionType.POWER_STORAGE).stream()
									.mapToDouble(b -> b.getPowerStorage().getBattery().getEnergyStorageCapacity())
									.sum();
		setStoredEnergyCapacity(capacity);
	}

	/**
	 * Checks if building can generate more power than it uses in a given power mode.
	 *
	 * @param building the building
	 * @param mode     {@link PowerMode} the building's power mode to check.
	 * @return true if building supplies more power than it uses. throws
	 *         BuildingException if error in power generation.
	 */
	private boolean canGenMoreThanLoad(Building b, PowerMode mode) {
		double generated = 0D;
		
		PowerGeneration pg = b.getFunction(FunctionType.POWER_GENERATION);
		if (pg != null) {
			// The power that it can generate at this moment
			// e.g. Solar power is dependent upon the sunlight
			// e.g. Wind power is dependent upon the wind speed
			generated = b.getPowerGeneration().getGeneratedPower();
		}

		double powerLoad = 0D;
		if (mode == PowerMode.FULL_POWER)
			powerLoad = b.getFullPowerRequired();
		else if (mode == PowerMode.LOW_POWER)
			powerLoad = b.getLowPowerRequired();

		return generated > powerLoad;
	}

	/**
	 * Stores any excess energy into the power grid via battery storage systems in buildings if possible.
	 * 
	 * @param excessEnergy excess grid energy (in kW hr).
	 * @param timeHr
	 * @return excess energy that cannot be stored
	 */
	private double storeExcessPower(double excessEnergy, double timeHr) {
		double excess = excessEnergy;
		Iterator<Building> i = manager.getBuildingSet(FunctionType.POWER_STORAGE).iterator();
		while (i.hasNext()) {
			PowerStorage storage = i.next().getPowerStorage();
			double stored = storage.getBattery().getCurrentStoredEnergy();
			double max = storage.getBattery().getEnergyStorageCapacity();
			double gap = max - stored;
			double onePercent = max * .01D;

			if (gap > onePercent && excess > 0) {
				// Future: build a better battery model with charge capacity
				// parameters from
				// https://www.mathworks.com/help/physmod/elec/ref/genericbattery.html?requestedDomain=www.mathworks.com

				// Note: Tesla runs its batteries up to 4C charging rate
				// see https://teslamotorsclub.com/tmc/threads/limits-of-model-s-charging.36185/
		
				double kWhAccepted = storage.getBattery().chargeBattery(excess, timeHr);
				
				excess = excess - kWhAccepted;
			}
		}
		
		return excess;
	}

	/**
	 * Retrieves stored energy from grid-connected batteries.
	 * 
	 * @param needed the energy needed (kWh)
	 * @param timeHr the hours
	 * @return energy that can be retrieved (kWh)
	 */
	public double retrieveStoredEnergy(double totalEnergyNeeded, double timeHr) {

		double remainingNeed = totalEnergyNeeded;
//		double totalAvailable = 0;
		
		Set<Building> storages = manager.getBuildingSet(FunctionType.POWER_STORAGE);
		if (!storages.isEmpty()) {
			
//			for (Building b : storages) {
//				PowerStorage storage = b.getPowerStorage();
//				totalAvailable += storage.getBattery().estimateEnergyToDeliver(
//						remainingNeed - totalAvailable, R_LOAD, timeHr);
//			}
//		
//			double neededPerStorage = totalAvailable / storages.size();
			
			// Note: In near future  
			// First, retrieve energy from the battery where the building possess
			// Next, retrieve energy evenly across all other batteries and not just one battery  
			
			for (Building b : storages) {
				
				if (remainingNeed <= 0) {
					break;
				}
				
				double kWhDelivered = b.getPowerStorage().getBattery().requestEnergy(remainingNeed, timeHr);
				
				remainingNeed = remainingNeed - kWhDelivered;

//				double available = storage.getBattery().estimateEnergyToDeliver(
//						RandomUtil.getRandomDouble(neededPerStorage, neededPerStorage * 2), R_LOAD, time);
//				double stored = storage.getBattery().getCurrentStoredEnergy();
//
//				if (available > 0) {
//					// update the resultant energy stored in battery
//					stored = stored - available;
//					// update energy needed
//					remainingNeed = remainingNeed - available;
//					// update the energy stored in this battery
//					storage.getBattery().reconditionBattery(stored);
//					// update the total retrieved energy
//					retrieved = retrieved + available;
//				}
			}
		}
		
		return totalEnergyNeeded - remainingNeed;
	}



	/**
	 * Gets the value of electrical power at the settlement.
	 * 
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
		double supply = powerGenerated + (totalEnergyStored / 2D);

		double newPowerValue = demand / (supply + 1.0D);

		if (newPowerValue != powerValue) {
			powerValue = newPowerValue;
			settlement.fireUnitUpdate(UnitEventType.POWER_VALUE_EVENT);
		}
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		powerMode = null;
		settlement = null;
		manager = null;
	}
}
