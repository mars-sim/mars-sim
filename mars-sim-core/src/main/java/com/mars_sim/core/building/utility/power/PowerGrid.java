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

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class is a settlement's building power grid.
 */
public class PowerGrid implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	// May add back : private static final SimLogger logger = SimLogger.getLogger(PowerGrid.class.getName())

	private static final double ROLLING_FACTOR = 1.05; 
	
	private static final double PERC_AVG_VOLT_DROP = 98D;

	public static final double HOURS_PER_MILLISOL = MarsTime.HOURS_PER_MILLISOL; 
	
    public static final String POWER_VALUE_EVENT = "power value";
    public static final String POWER_LOAD_EVENT = "power load";
    public static final String STORED_ENERGY_CAPACITY_EVENT = "stored power capacity";
    public static final String STORED_ENERGY_EVENT = "stored power";
    public static final String GENERATED_POWER_EVENT = "generated power";

	/** The flag for checking if the simulation has just started. */
	private boolean justLoaded = true;

	private double degradationRatePerSol = .0004D;
	private double systemEfficiency = 1D;
	private double powerGenerated;
	private double totalEnergyStored;
	private double energyStorageCapacity;
	private double powerLoad;
	private double powerValue;

	private Settlement settlement;
	private BuildingManager manager;


	/**
	 * Constructor.
	 */
	public PowerGrid(Settlement settlement) {
		this.settlement = settlement;
		manager = settlement.getBuildingManager();
		powerGenerated = 0D;
		totalEnergyStored = 0D;
		energyStorageCapacity = 0D;
		powerLoad = 0D;
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
		
		if (newGeneratedPower > 0D && !Double.isNaN(newGeneratedPower) && !Double.isInfinite(newGeneratedPower)
				&& powerGenerated != p) {
			powerGenerated = p;
			settlement.fireUnitUpdate(PowerGrid.GENERATED_POWER_EVENT);
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
			settlement.fireUnitUpdate(PowerGrid.STORED_ENERGY_EVENT);
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
			settlement.fireUnitUpdate(PowerGrid.STORED_ENERGY_CAPACITY_EVENT);
		}
	} 

	/**
	 * Gets the power load from the grid.
	 * 
	 * @return power load in kW
	 */
	public double getPowerLoad() {
		return powerLoad;
	}

	/**
	 * Sets the power load in the grid.
	 * 
	 * @param new power load in kW
	 */
	private void setLoadPower(double newPower) {
		if (powerLoad != newPower) {
			powerLoad = newPower;
			settlement.fireUnitUpdate(PowerGrid.POWER_LOAD_EVENT);
		}
	}

	/**
	 * Time passing for power grid.
	 * 
	 * @param pulse clock pulse
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// Get the amount of time in millisols
		double time = pulse.getElapsed();
		
		// For debugging: logger.log(settlement, Level.FINEST, 0, Msg.getString("PowerGrid.log.settlementPowerSituation", settlement.getName()))
		
		// update the total power generated in the grid.
		double powerGen = updateTotalPowerGenerated();

		// Determine total power load in the grid.
		double powerLoad = updateTotalLoadPower();

		double adjUsageRatio = computeAdjustablePowerSourceUsage();
					
		int rand = RandomUtil.getRandomInt((int)(50.0 * adjUsageRatio));
		
		if (rand == 0) {
			// Note: this is just a temporary measure to force the adjustable power source to keep up production by default
			for (Building b : manager.getBuildingSet(FunctionType.POWER_GENERATION)) {
				for (PowerSource powerSource : b.getPowerGeneration().getPowerSources()) {
					if (powerSource instanceof AdjustablePowerSource fps) {
						if (fps.getUsageRatio() < 1) {
							fps.increaseLoadCapacity();
						}
					}
				}
			}
		}
				
		// Update overall grid efficiency.
		updateEfficiency(time);
		
		// Run at the start of the sim once only
		if (justLoaded				
//			&& pulse.getMarsTime().getMissionSol() == 1
				&& pulse.getMarsTime().getMillisolInt() >= 1) {
					// Reset justLoaded
					justLoaded = false;
		}
		
		if (!justLoaded) {
			
	 		// Update the power flow.
			double powerDiff = powerGen - powerLoad; 
			
			if (powerGen > 0) {
			
				double powerRatio = powerDiff / powerGen;
				
				if (powerRatio > 0.05) {
					handleExcessPower(time, powerDiff);
				}
				else if (powerDiff < 0) {
					handleLackOfPower(time, powerDiff);
				}
			}
			
			else {
				handleLackOfPower(time, powerDiff);
			}
		}

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
		// For debugging : logger.info("Sys eff: " + Math.round(systemEfficiency * 100.0)/100.0)
	}


	/**
	 * Calculates the amount of electrical power generated.
	 * 
	 * @param increaseLoad
	 * @param powerToReconcile
	 * @return net power change in kW
	 */
	public double stepUpDownPower(boolean increaseLoad, double powerToReconcile) {
		double netPower = 0D;

		for (Building b : manager.getBuildingSet(FunctionType.POWER_GENERATION)) {
			for (PowerSource powerSource : b.getPowerGeneration().getPowerSources()) {
				double previous = powerSource.getCurrentPower(b);
				if (powerSource instanceof AdjustablePowerSource fps) {
					if (increaseLoad) {
						fps.increaseLoadCapacity();
						double net = powerSource.getCurrentPower(b) - previous;
						if (net > 0 && Double.isFinite(net)) {
							netPower += net;
							powerToReconcile -= net;
							if (powerToReconcile <= 0) {
								return netPower;
							}
						}
					}
					else {
						fps.decreaseLoadCapacity();
						double net = powerSource.getCurrentPower(b) - previous;
						if (net < 0 && Double.isFinite(net)) {
							netPower -= net;
							powerToReconcile += net;
							if (powerToReconcile <= 0) {
								return netPower;
							}
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
	 * @param time in millisols
	 * @param excessP (Note: excessP is +ve)
	 */
	private void handleExcessPower(double time, double excessP) {

		double timeInHour = time * HOURS_PER_MILLISOL;
		
		double excess = excessP;
		
//		May need for future debugging: 	logger.info("excess0: " + Math.round(excess * 10.0)/10.0);
		
		Set<Building> buildings = manager.getBuildingSet(FunctionType.POWER_GENERATION);

		// 1. Switch from no power to low power in inhabitable buildings
		// building until required power reduction is met.
		double netPower1 = adjustBuildingPowerLevel(false, excess, buildings, 
				true, PowerMode.NO_POWER, PowerMode.LOW_POWER);
		
		excess -= netPower1;

		if (excess <= 0) {
			return;
		}
		
//		May need for future debugging: 	logger.info("excess1: " + Math.round(excess * 10.0)/10.0);
		
		// 2. Switch from low power to full power mode on inhabitable buildings
		
		// If power needs are still not met, turn on full power in each inhabitable
		// building until required power reduction is met.
		double netPower2 = adjustBuildingPowerLevel(false, excess, buildings, 
				true, PowerMode.LOW_POWER, PowerMode.FULL_POWER);
		
		excess -= netPower2;
	
		if (excess <= 0) {
			return;
		}
		
//		May need for future debugging: 	logger.info("excess2: " + Math.round(excess * 10.0)/10.0);
		
		// 3. Turn on low power mode on non-inhabitable buildings
		
		// Switch from no power to low power in each non-inhabitable
		// building until required power reduction is met.
		double netPower3 = adjustBuildingPowerLevel(false, excess, buildings, 
				false, PowerMode.NO_POWER, PowerMode.LOW_POWER);
		
		excess -= netPower3;

		if (excess <= 0) {
			return;
		}
		
//		May need for future debugging: logger.info("excess3: " + Math.round(excess * 10.0)/10.0);
		
		// 4. Turn off emergency power generators. Have excess power. No need of 
		//    using methane power generators to produce electricity
		
		// C1. Turn off methane power generators 
		double methanePower = adjustPowerGenerator(false, excess, buildings, 
				FunctionType.POWER_GENERATION, PowerSourceType.FUEL_POWER);
		
		excess = methanePower;

		if (excess <= 0) {
			return;
		}
		
//		May need for future debugging: 	logger.info("excess4: " + Math.round(excess * 10.0)/10.0);
		
		// 5. Turn on full power mode on non-inhabitable buildings
		
		// Switch from low power to full power in each non-inhabitable
		// building until required power reduction is met.
		double netPower5 = adjustBuildingPowerLevel(false, excess, buildings, 
				false, PowerMode.LOW_POWER, PowerMode.FULL_POWER);
		
		excess -= netPower5;

		if (excess <= 0) {
			return;
		}			
		
//		May need for future debugging: logger.info("excess5: " + Math.round(excess * 10.0)/10.0);

		// 6. Store the excess power in batteries
		
		copeWithPowerSurplus(100, excess, timeInHour);
			
		
		// 7. Step down the capacity of the fission power plant by a small percent
		double netPower7 = stepUpDownPower(false, excess);
		excess -= netPower7;

		if (excess <= 0) {
			return;
		}
		
//		May need for future debugging: if (excess > 0) logger.info("excess7: " + Math.round(excess * 10.0)/10.0);
	}

	/**
	 * Copes with the power surplus.
	 * 
	 * @param cycles
	 * @param excessPower
	 * @param timeInHour
	 */
	private void copeWithPowerSurplus(int cycles, double excessPower, double timeInHour) {

		// Update the total power storage capacity in the grid.
		double energyCap = updateTotalEnergyStorageCapacity();

		// Update the total power stored in the grid.
		double energyStored = updateTotalStoredEnergy();
		
		// Get the gap due to the stored energy/power.
		// from 0 (full/best) to 1.0 (empty/worst)
		double gap = (energyCap - energyStored) / energyCap;
		
		double limit = MathUtils.between(cycles * gap, 0, cycles);
		
		int count = 0;
		
		while (excessPower > 0 && count <= limit) {
			
			count ++;
			
			// 1. Store excess power in power storage buildings.
	
			double excessEnergy = excessPower * timeInHour * systemEfficiency;
			
//			May need for future debugging: 	logger.info("excessEnergy: " + Math.round(excessEnergy * 1000.0)/1000.0);
			
			double unableToStoreEnergy = storeExcessPower(excessEnergy, timeInHour);
			
			double excessPower0 = unableToStoreEnergy / timeInHour;
			
			excessPower = excessPower0 / systemEfficiency;
	
			if (excessPower <= 0) {
				return;
			}
			
//			May need for future debugging: 	logger.info("excess6: " + Math.round(excess * 10.0)/10.0);
			
			// 2. Step down the capacity of the fission power plant by a small percent
			
			double usageRatio = computeAdjustablePowerSourceUsage(); 
	
			int rand = RandomUtil.getRandomInt((int)(limit * 6 / usageRatio));
			if (rand == 0) {
				double netPower02 = stepUpDownPower(false, excessPower);
				excessPower -= netPower02;
	
				// Update the total generated power with contribution from fission generators
				setGeneratedPower(powerGenerated - netPower02);
				
				if (excessPower <= 0) {
					return;
				}
			}
				
//			May need for future debugging: 	logger.info("excess7: " + Math.round(excess * 10.0)/10.0);	
		}
	}
	
	/**
	 * Handles the demand for power by ramping up the power generation.
	 * 
	 * @param time in millisols
	 * @param neededP (Note: neededP is -ve)
	 */
	private void handleLackOfPower(double time, double neededP) {
		// May add back for debugging: logger.info(settlement, "neededPower0: " + Math.round(neededPower))

		// Convert neededP (-ve) to neededPower (+ve)
		double neededPower = -neededP;
		
//		May need for future debugging: logger.info("neededPower0: " + Math.round(neededPower * 10.0)/10.0)
	
		double timeInHour = time * HOURS_PER_MILLISOL; 

		// 1. Retrieve the needed power from batteries

		// Note: Insufficient power produced, need to pull energy from batteries to meet the
		// demand
		
		copeWithPowerDeficit(120, neededPower, timeInHour);

//		May need for future debugging: logger.info("neededPower1: " + Math.round(neededPower * 10.0)/10.0)
		
		Set<Building> buildings = manager.getBuildingSet(FunctionType.POWER_GENERATION);
		
		// 2. Turn on emergency power generators to supplement power production
		
		// If still not having sufficient power,
		// turn on methane generators to low power mode if available
		double methanePower0 = adjustPowerGenerator(true, neededPower, buildings, 
				FunctionType.POWER_GENERATION, PowerSourceType.FUEL_POWER);

		neededPower = methanePower0;
		// Update the total generated power with contribution from methane generators
		setGeneratedPower(powerGenerated + methanePower0);
		
		if (neededPower < 0) {
			return;
		}
		
//		May need for future debugging: logger.info("neededPower2: " + Math.round(neededPower * 10.0)/10.0);
		
		// 3. Retrieve the needed power from batteries

		copeWithPowerDeficit(80, neededPower, timeInHour);
		
	
		// 4. If still not having sufficient power, reduce power to some buildings

		// Reduce each non-inhabitable building's full power mode to low power until
		// required power reduction is met.
		double savedPower0 = adjustBuildingPowerLevel(true, neededPower, buildings, 
				false, PowerMode.FULL_POWER, PowerMode.LOW_POWER);
		
		neededPower -= savedPower0;
		// Update the total generated power
		setGeneratedPower(powerGenerated + savedPower0);
		
		if (neededPower < 0) {
			return;
		}
		
//		May need for future debugging: logger.info("neededPower4: " + Math.round(neededPower * 10.0)/10.0);

		// 5. Retrieve the needed power from batteries

		copeWithPowerDeficit(80, neededPower, timeInHour);
		

		// 6. If still not having sufficient power,
		// turn on methane generators to full power mode if available
		double methanePower1 = adjustPowerGenerator(true, neededPower, buildings, 
				FunctionType.POWER_GENERATION, PowerSourceType.FUEL_POWER);

		neededPower = methanePower1;	
		// Update the total generated power with contribution from methane generators
		setGeneratedPower(powerGenerated + methanePower1);
		
		if (neededPower < 0) {
			return;
		}
	
//		May need for future debugging: logger.info("neededPower6: " + Math.round(neededPower * 10.0)/10.0);
		
		// 7. Retrieve the needed power from batteries

		copeWithPowerDeficit(80, neededPower, timeInHour);
		
//		May need for future debugging: logger.info("neededPower9: " + Math.round(neededPower * 10.0)/10.0);
		
		// 8. If power needs are still not met, turn off the power in each
		// non-inhabitable building until required power reduction is met.
		double savedPower1 = adjustBuildingPowerLevel(true, neededPower, buildings, 
				false, PowerMode.LOW_POWER, PowerMode.NO_POWER);
			
		neededPower -= savedPower1;
		// Update the total generated power
		setGeneratedPower(powerGenerated + savedPower1);
		
		if (neededPower < 0) {
			return;
		}
		
//		May need for future debugging: logger.info("neededPower8: " + Math.round(neededPower * 10.0)/10.0);
		
		// 9. Retrieve the needed power from batteries

		copeWithPowerDeficit(80, neededPower, timeInHour);
		
//		May need for future debugging: logger.info("neededPower11: " + Math.round(neededPower * 10.0)/10.0);
		
		// 10. If power needs are still not met, turn on the low power in each inhabitable
		// building until required power reduction is met.
		double savedPower2 = adjustBuildingPowerLevel(true, neededPower, buildings, 
				true, PowerMode.FULL_POWER, PowerMode.LOW_POWER);
		
		neededPower -= savedPower2;
		// Update the total generated power
		setGeneratedPower(powerGenerated + savedPower2);
		
		if (neededPower < 0) {
			return;
		}
		
//		May need for future debugging: 	logger.info("neededPower10: " + Math.round(neededPower * 10.0)/10.0);
		
		// 11. Retrieve the needed power from batteries

		copeWithPowerDeficit(160, neededPower, timeInHour);	
		
		
//		Note: for now, avoid shutting down inhabitable buildings since they do serve to produce life support resources
//		
//		// 12. If power needs are still not met, turn off the power in each inhabitable
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
		
//		May need for future debugging: 	logger.info("neededPower12: " + Math.round(neededPower * 100.0)/100.0);
		
	}
		
	/**
	 * Copes with the power deficit.
	 * 
	 * @param cycles
	 * @param neededPower
	 * @param timeInHour
	 */
	private void copeWithPowerDeficit(int cycles, double neededPower, double timeInHour) {
		int count = 0;
		
		// Update the total power storage capacity in the grid.
		double energyCap = updateTotalEnergyStorageCapacity();

		// Update the total power stored in the grid.
		double energyStored = updateTotalStoredEnergy();
				
		// Get the gap due to the stored energy/power.
		// from 0.0 (full) to 1.0 (empty)
		double gap = (energyCap - energyStored) / energyCap;
		
		double limit = MathUtils.between(cycles / gap, cycles, 200);
		
		while (neededPower > 0 && count < limit) { 
			
			count++;
		
			// 1. Retrieve power from the grid battery for the first time
			double retrievedPower = retrievePowerGridBattery(timeInHour, neededPower);
			
			neededPower -= retrievedPower;
			
			// Update the total generated power with contribution from batteries
			//setGeneratedPower(powerGenerated + retrievedPower);
			
			if (neededPower < 0) {
				return;
			}
			
	//		May need for future debugging: llogger.info("neededPower1: " + Math.round(neededPower * 10.0)/10.0)
				
			double usageRatio = computeAdjustablePowerSourceUsage(); 
			
			int rand = RandomUtil.getRandomInt((int)(limit / 40 * usageRatio));
			if (rand == 0) {
						
				// 2. Increases the load capacity of fission reactors if available
				double fissionPower0 = stepUpDownPower(true, neededPower);
		
				neededPower -= fissionPower0;
				// Update the total generated power with contribution from increased power load capacity of fission reactors
				setGeneratedPower(powerGenerated + fissionPower0);
				
				// if the fission reactors produces more than enough
				if (neededPower < 0) {
					return;
				}
			}
		}
	}
	
	/**
	 * Computes the adjustable power source usage ratio.
	 * 
	 * @return
	 */
	private double computeAdjustablePowerSourceUsage() {
		double num = 0;
		double result = 0;
		
		for (Building b : manager.getBuildingSet(FunctionType.POWER_GENERATION)) {
			for (PowerSource powerSource : b.getPowerGeneration().getPowerSources()) {
				if (powerSource instanceof AdjustablePowerSource fps) {
					num++;
					double ratio = fps.getUsageRatio();
					result += ratio;
				}
			}
		}
		
		return result / num;
	}
	
	/**
	 * Adjust the power level in inhabitable and non-inhabitable buildings.
	 * 
	 * @param lackingPower true if the power grid has insufficient power
	 * @param powerToHandle (always +ve) either the excess power or the needed power
	 * @param buildings
	 * @param checkLifeSupport true if only looking at building having life support
	 * @param oldPowerMode
	 * @param newPowerMode
	 * @return the delta power to be saved or used after switching to the new power mode
	 */
	private double adjustBuildingPowerLevel(boolean lackingPower, double powerToHandle, Set<Building> buildings, 
			boolean checkLifeSupport, PowerMode oldPowerMode, PowerMode newPowerMode) {
		// Make netPower always positive 
		double netPower = 0;
	
		Iterator<Building> i = buildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();

			// Check if this building has life support function
			boolean hasLife = building.hasFunction(FunctionType.LIFE_SUPPORT);
			
			// Check if this building matches this one criterion of calling this method 
			if (hasLife == checkLifeSupport) {
	
				PowerMode thisOldPM = building.getPowerMode();
	
				double power = 0;
				
				if (thisOldPM == oldPowerMode) {
					
					boolean morePower = canGenMoreThanLoad(building, newPowerMode);
					
					if (lackingPower && !morePower) {
						/////// IF LACK OF POWER /////// 
					 				
						// For stepping down power
						if (oldPowerMode == PowerMode.FULL_POWER
							&& newPowerMode == PowerMode.LOW_POWER) {
							// power will be +ve
							power = building.getFullPowerLoad() - building.getLowPowerLoad();
														
							powerToHandle -= power;

							
							if (powerToHandle > 0) {
								// Case 1 : Can handle power
								
								netPower += power;
								// In case of lacking power, +ve powerToHandle means it can handle the stepping up of power 					
								// Switch from one power mode to another
								building.setPowerMode(newPowerMode);
							}
					
							else {
								// Case 2 : Can't handle power
								// When lacking power and needing to step up power level
							}
						}
						// For stepping down power
						else if (oldPowerMode == PowerMode.LOW_POWER
							&& newPowerMode == PowerMode.NO_POWER) {
							
							boolean isGenerator = building.getCategory() == BuildingCategory.POWER;
		
							if (!isGenerator) {
													
								// power will be +ve
								power = building.getLowPowerLoad();
								
								powerToHandle -= power;
				
								if (powerToHandle > 0) {
									// Case 1 : Can handle power
									
									netPower += power;
									// In case of lacking power, +ve powerToHandle means it can handle the stepping up of power 					
									// Switch from one power mode to another
									building.setPowerMode(newPowerMode);
								}
						
								else {
									// Case 2 : Can't handle power
									// When lacking power and needing to step up power level
								}
							}
						}
					}
					
					else {
						/////// IF EXCESS OF POWER /////// 
		
						// For stepping up power
						if (oldPowerMode == PowerMode.LOW_POWER
							&& newPowerMode == PowerMode.FULL_POWER) {
							// power will be +ve
							power = building.getFullPowerLoad()	- building.getLowPowerLoad();
							
							powerToHandle -= power;
		
							if (powerToHandle > 0) {
								// Case 1 : Can handle power
								
								netPower += power;
								// In case of excess power, +ve powerToHandle means it can handle the stepping up of power 					
								// Switch from one power mode to another
								building.setPowerMode(newPowerMode);
							}
							
							else {
								// Case 2 : Can't handle power
								// When lacking power and needing to step up power level
							}
						}
						// For stepping up power
						else if (oldPowerMode == PowerMode.NO_POWER
							&& newPowerMode == PowerMode.LOW_POWER) {
							// power will be +ve
							power = building.getLowPowerLoad();
							
							powerToHandle -= power;
		
							if (powerToHandle > 0) {
								// Case 1 : Can handle power
								
								netPower += power;
								// In case of lacking power, +ve powerToHandle means it can handle the stepping up of power 					
								// Switch from one power mode to another
								building.setPowerMode(newPowerMode);
							}
					
							else {
								// Case 2 : Can't handle power
								// When lacking power and needing to step up power level
							}
						}
					}
				}
			}
		}
		return netPower;
	}
	
	/**
	 * Adjusts the power level in power generating buildings.
	 * 
	 * @Note: for now, use only in methane power
	 * 
	 * @param stepUp turning up power level
	 * @param originalPower
	 * @param buildings
	 * @param functionType
	 * @param powerSourceType
	 * @return the power to be saved or used after switching to the new power mode
	 */
	private double adjustPowerGenerator(boolean stepUp, double originalPower, 
			Set<Building> buildings, FunctionType functionType,
			PowerSourceType powerSourceType) {

		double newPower = originalPower;
	
		Iterator<Building> i = buildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
		
			boolean hasFunction = building.hasFunction(functionType);
			
			boolean isGenerator = building.getCategory() == BuildingCategory.POWER;
		
			List<PowerSource> sources = building.getPowerGeneration().getPowerSources();
			
			if (hasFunction && isGenerator && !sources.isEmpty()) {
				
				Iterator<PowerSource> j = sources.iterator();
				while (j.hasNext()) {
					PowerSource source = j.next();			
					if (source.getType() != powerSourceType)
						continue;
					
					FuelPowerSource fps = (FuelPowerSource)source; 
					
					boolean isOn = fps.isToggleON();
					
					double cPower = 0;
					
					if (stepUp && !isOn) {
									
						fps.toggleON();
						
						cPower = fps.getCurrentPower(building);					
					}
					
					else {
						
						if (isOn)
							cPower = fps.measurePower(100);
						
						fps.toggleOFF();	
					}
			
					newPower -= cPower;
					
					if (newPower < 0)
						return newPower;
				}
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
	 * @return
	 * @throws BuildingException if error determining total energy stored.
	 */
	private double updateTotalStoredEnergy() {
		double store = manager.getBuildingSet(FunctionType.POWER_STORAGE).stream()
								.mapToDouble(b -> b.getPowerStorage().getBattery().getCurrentStoredEnergy())
								.sum();
		setStoredEnergy(store);
		
		return store;
	}

	/**
	 * Updates the total power load in the grid.
	 * 
	 * @return
	 * @throws BuildingException if error determining total power load.
	 */
	private double updateTotalLoadPower() {
		double power = manager.getBuildingSet(FunctionType.POWER_GENERATION).stream()
				.mapToDouble(b -> b.getCurrentPowerLoad()) //getFullPowerLoad())
				.sum();
		// Note: need to check the required power in full power mode 
		// so that the return value will show how much power needed
		setLoadPower(power);
		
		return power;
	}

	/**
	 * Updates the total energy storage capacity in the grid.
	 * 
	 * @return
	 * @throws BuildingException if error determining total energy storage capacity.
	 */
	private double updateTotalEnergyStorageCapacity() {
		double capacity = manager.getBuildingSet(FunctionType.POWER_STORAGE).stream()
									.mapToDouble(b -> b.getPowerStorage().getBattery().getEnergyStorageCapacity())
									.sum();
		setStoredEnergyCapacity(capacity);
		
		return capacity;
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
			powerLoad = b.getFullPowerLoad();
		else if (mode == PowerMode.LOW_POWER)
			powerLoad = b.getLowPowerLoad();

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
		double excessE = excessEnergy;
		List<Building> batteries = manager.getBuildings(FunctionType.POWER_STORAGE);
		int size = batteries.size();
		double portionE = excessE / size;
		
		for (int i = 0; i < size; i++) {
			Building b = batteries.get(i);
	
			if (excessE <= 0)
				return excessE;
			
			PowerStorage storage = b.getPowerStorage();
			double stored = storage.getBattery().getCurrentStoredEnergy();
			double cap = storage.getBattery().getEnergyStorageCapacity();
			double diff = cap - stored;
			double kWhAccepted = 0;
			
			if (diff < portionE) {
				kWhAccepted = storage.getBattery().chargeBattery(diff, timeHr);
				// Take back the unused excess
				double used = (portionE - diff)/(size - i - 1);
				portionE += used;
			}
			
			else {
				kWhAccepted = storage.getBattery().chargeBattery(portionE, timeHr);
			}
			
			excessE -= kWhAccepted;
			
			// Future: build a better battery model with charge capacity
			// parameters from
			// https://www.mathworks.com/help/physmod/elec/ref/genericbattery.html?requestedDomain=www.mathworks.com

			// Note: Tesla runs its batteries up to 4C charging rate
			// see https://teslamotorsclub.com/tmc/threads/limits-of-model-s-charging.36185/
		}
		
		return excessE;
	}

	/**
	 * Retrieves power from the grid battery.
	 * 
	 * @param timeInHour
	 * @param neededPower
	 * @return retrievedPower
	 */
	private double retrievePowerGridBattery(double timeInHour, double neededPower) {
		double newNeededPower = neededPower;
		
		// Assume the gauge of the cable is uniformly low, as represented by percentAverageVoltageDrop
		// Future: account for the distance of the separation between endpoints
		double neededEnergy = newNeededPower * timeInHour / PERC_AVG_VOLT_DROP * 100D;

		// Assume the energy flow is instantaneous and
		// subtract powerHr from the battery reserve
		double retrieved = retrieveStoredEnergy(neededEnergy, timeInHour);

		double batteryPower = retrieved / timeInHour;
		
	
		// Note: battery delivered power can help meet up with the power demand
		// However, currently, it's NOT recommended mixing it with the power generated by
		// other means or else it's difficult to know if a settlement is producing enough power
		// since battery only stores energy and releases it at a later date and is not
		// considered a source of power.
			
		return batteryPower;
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
				
				double kWhDelivered = b.getPowerStorage().getBattery().consumeEnergy(remainingNeed, timeHr);
				
//				logger.info(b.getName() + " -  kWhDelivered: " + Math.round(kWhDelivered * 1000.)/1000.0 
//						+ "  remainingNeed: " + Math.round(remainingNeed * 1000.)/1000.0
//						+ "  totalEnergyNeeded: " + Math.round(totalEnergyNeeded * 1000.)/1000.0);
				
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
		double demand = powerLoad;
		double supply = powerGenerated + (totalEnergyStored / 2D);

		double newPowerValue = demand / (supply + 1.0D);

		if (newPowerValue != powerValue) {
			powerValue = newPowerValue;
			settlement.fireUnitUpdate(PowerGrid.POWER_VALUE_EVENT);
		}
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		settlement = null;
		manager = null;
	}
}
