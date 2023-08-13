/*
 * Mars Simulation Project
 * PowerGrid.java
 * @date 2023-06-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.AdjustablePowerSource;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.PowerSource;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.Temporal;

/**
 * The PowerGrid class is a settlement's building power grid.
 */
public class PowerGrid implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(PowerGrid.class.getName());

	private static final double R_LOAD = 1000D; // assume constant load resistance

	private static final double ROLLING_FACTOR = 1.1D; 
	
	private static final double PERC_AVG_VOLT_DROP = 98D;

	public static final double HOURS_PER_MILLISOL = MarsTime.HOURS_PER_MILLISOL; 

	private double degradationRatePerSol = .0004D;
	private double systemEfficiency = 1D;
	private double powerGenerated;
	private double totalEnergyStored;
	private double energyStorageCapacity;
	private double powerRequired;
	private boolean sufficientPower;
	private double powerValue;

	private Settlement settlement;
	private BuildingManager manager;
	private PowerMode powerMode;

	/**
	 * Constructor.
	 */
	public PowerGrid(Settlement settlement) {
		this.settlement = settlement;
		manager = settlement.getBuildingManager();
		powerMode = PowerMode.POWER_UP;
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
	 * Sets the power grid mode.
	 * 
	 * @param newPowerMode the new power grid mode.
	 */
	public void setPowerMode(PowerMode newPowerMode) {
		if (powerMode != newPowerMode) {
			if (PowerMode.POWER_UP == newPowerMode)
				powerMode = PowerMode.POWER_UP;
			else if (PowerMode.POWER_DOWN == newPowerMode)
				powerMode = PowerMode.POWER_DOWN;
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

	public String getDisplayStoredEnergy() {
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
		updateTotalPowerGenerated();

		// Determine total power required in the grid.
		updateTotalRequiredPower();

		// Update overal grid efficiency.
		updateEfficiency(pulse.getElapsed());

		// Update the power flow.
		double neededPower = powerRequired * ROLLING_FACTOR - powerGenerated;
		sufficientPower = (neededPower < 0);
		if (neededPower < 0) {
			handleExcessPower(pulse.getElapsed());
		}
		else {
			generateMorePower(pulse.getElapsed());
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
		systemEfficiency = systemEfficiency - systemEfficiency * dFactor;
	}


	/**
	 * Calculates the amount of electrical power generated.
	 * 
	 * @return power generated in kW
	 */
	public double stepUpDownPower(boolean increaseLoad) {
		double power = 0D;

		for(Building b : manager.getBuildingSet(FunctionType.POWER_GENERATION)) {
			for(PowerSource powerSource : b.getPowerGeneration().getPowerSources()) {
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
						power += net;
					}
				}
			}
		}
		return power;
	}
	
		
	/**
	 * Handle an excess of power
	 * 
	 * @param time
	 */
	private void handleExcessPower(double time) {
		// Store excess power in power storage buildings.
		double timeHr = time * HOURS_PER_MILLISOL;
		double excessEnergy = (powerGenerated - powerRequired) * timeHr * systemEfficiency;
		storeExcessPower(excessEnergy, time);
		
		int rand = RandomUtil.getRandomInt(10);
		if (rand == 10) {
			// Step down the capacity of the fission power plant by a small percent
			stepUpDownPower(false);
		}
	}

	/**
	 * Handle an excess of power
	 * 
	 * @param time
	 */
	private void generateMorePower(double time) {
		double neededPower = powerRequired * ROLLING_FACTOR - powerGenerated;

		// insufficient power produced, need to pull energy from batteries to meet the
		// demand
		sufficientPower = false;
		
		// increases the load capacity of fission reactors if available
		double newPower0 = stepUpDownPower(true);

		// Update the total generated power with contribution from increased power load capacity of fission reactors
		setGeneratedPower(powerGenerated + newPower0);
		neededPower -= newPower0;
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}

		double timeInHour = time * HOURS_PER_MILLISOL; 
		
		// Assume the gauge of the cable is uniformly low, as represented by percentAverageVoltageDrop
		// TODO: account for the distance of the separation between endpoints
		double neededEnergy = neededPower * timeInHour / PERC_AVG_VOLT_DROP * 100D;

		// Assume the energy flow is instantaneous and
		// subtract powerHr from the battery reserve
		double retrieved = retrieveStoredEnergy(neededEnergy, time);

		double newPower1 = retrieved / timeInHour;
		
		// Update the total generated power with contribution from batteries
		setGeneratedPower(powerGenerated + newPower1);
		neededPower -= newPower1;
		if (neededPower < 0) { 
			// if the grid batteries has more than enough
			sufficientPower = true;
			return;
		}

		// If still not having sufficient power,
		// increases the load capacity of fission reactors if available
		double newPower2 = stepUpDownPower(true);

		// Update the total generated power with contribution from increased power load capacity of fission reactors
		setGeneratedPower(powerGenerated + newPower2);
		neededPower -= newPower2;		
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}
		
		// If still not having sufficient power, reduce power to some buildings
		
		Set<Building> buildings = manager.getBuildingSet();

		// Reduce each building's power mode to low power until
		// required power reduction is met.
		double newPower3 = turnOnLowPower(neededPower, buildings);
		
		// Update the total generated power
		setGeneratedPower(powerGenerated + newPower3);
		neededPower -= newPower3;
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}
		
		// If power needs are still not met, turn off the power to each
		// uninhabitable building until required power reduction is met.
		double newPower4 = turnOffNoninhabitable(neededPower, buildings);
		
		// Update the total generated power
		setGeneratedPower(powerGenerated + newPower4);	
		neededPower -= newPower4;
		if (neededPower < 0) {
			sufficientPower = true;
			return;
		}

		// If power needs are still not met, turn off the power to each inhabitable
		// building until required power reduction is met.
		double newPower5 = turnOffInhabitable(neededPower, buildings);
		
		// Update the total generated power
		setGeneratedPower(powerGenerated + newPower5);
		neededPower -= newPower5;
		sufficientPower = (neededPower <= 0);
	}

	/**
	 * Turns on low power mode in non-inhabitable buildings.
	 * 
	 * @param neededPower
	 * @param buildings
	 */
	private double turnOnLowPower(double neededPower, Set<Building> buildings) {
		double newPower = 0;
		if (powerMode != PowerMode.POWER_DOWN) {
			Iterator<Building> iLowPower = buildings.iterator();
			while (iLowPower.hasNext()) {
				Building building = iLowPower.next();
				// Future : should have a prioritized list of power usage
				if (!powerSurplus(building, PowerMode.FULL_POWER) &&
					!building.hasFunction(FunctionType.LIFE_SUPPORT)) {
					building.setPowerMode(PowerMode.POWER_DOWN);
					newPower += building.getFullPowerRequired() - building.getPoweredDownPowerRequired();
					neededPower -= newPower;
					if (neededPower <= 0) {
						return newPower;
					}
				}
			}
		}
		return newPower;
	}
	
	/**
	 * Turns off the power in non-inhabitable buildings.
	 * 
	 * @param neededPower
	 * @param buildings
	 */
	private double turnOffNoninhabitable(double neededPower, Set<Building> buildings) {
		double newPower = 0;
		Iterator<Building> iNoPower = buildings.iterator();
		while (iNoPower.hasNext()) {
			Building building = iNoPower.next();
			// Future : should have a prioritized list of power usage
			if (!powerSurplus(building, PowerMode.POWER_DOWN) &&
				!building.hasFunction(FunctionType.LIFE_SUPPORT)) {
				building.setPowerMode(PowerMode.NO_POWER);
				newPower += building.getPoweredDownPowerRequired();
				neededPower -= newPower;
				if (neededPower <= 0) {
					return newPower;
				}
			}
		}
		return newPower;
	}
		
	/**
	 * Turns off the power in inhabitable buildings.
	 * 
	 * @param neededPower
	 * @param buildings
	 */
	private double turnOffInhabitable(double neededPower, Set<Building> buildings) {
		double newPower = 0;
		Iterator<Building> iNoPower = buildings.iterator();
		while (iNoPower.hasNext()) {
			Building building = iNoPower.next();
			// Future : should a prioritized list of power usage
			if (!powerSurplus(building, PowerMode.POWER_DOWN) &&
				building.hasFunction(FunctionType.LIFE_SUPPORT)) {
				building.setPowerMode(PowerMode.NO_POWER);
				newPower += building.getPoweredDownPowerRequired();
				neededPower -= newPower;
				if (neededPower <= 0) {
					return newPower;
				}
			}
		}
		return newPower;
	}
	
	/**
	 * Updates the total power generated in the grid.
	 * 
	 * @throws BuildingException if error determining total power generated.
	 */
	private void updateTotalPowerGenerated() {
		// Add the power generated by all power generation buildings.
		double power = manager.getBuildingSet(FunctionType.POWER_GENERATION).stream()
								.mapToDouble(b -> b.getPowerGeneration().getGeneratedPower())
								.sum();
		setGeneratedPower(power);

		logger.log(settlement, Level.FINEST, 0, Msg.getString("PowerGrid.log.totalPowerGenerated", //$NON-NLS-1$
					Double.toString(power)));
	}

	/**
	 * Updates the total energy stored in the grid.
	 * 
	 * @throws BuildingException if error determining total energy stored.
	 */
	private void updateTotalStoredEnergy() {
		double store = manager.getBuildingSet(FunctionType.POWER_STORAGE).stream()
								.mapToDouble(b -> b.getPowerStorage().getkWattHourStored())
								.sum();
		setStoredEnergy(store);

		logger.log(settlement, Level.FINEST, 0, Msg.getString("PowerGrid.log.totalPowerStored", //$NON-NLS-1$
					Double.toString(totalEnergyStored)));
	}

	/**
	 * Updates the total power required in the grid.
	 * 
	 * @throws BuildingException if error determining total power required.
	 */
	private void updateTotalRequiredPower() {
		double power = 0D;
		boolean powerUp = powerMode == PowerMode.POWER_UP;
		Iterator<Building> iUsed = manager.getBuildingSet().iterator();
		while (iUsed.hasNext()) {
			Building building = iUsed.next();
			if (powerUp) {
				building.setPowerMode(PowerMode.FULL_POWER);
				power += building.getFullPowerRequired();
				logger.log(settlement, Level.FINEST, 0, Msg.getString("PowerGrid.log.buildingFullPowerUsed", //$NON-NLS-1$
							building.getNickName(), 
							Double.toString(
									Math.round(building.getFullPowerRequired()*100.00)/100.00
									)));
			}

			power += building.getPoweredDownPowerRequired();

			logger.log(settlement, Level.FINEST, 0, Msg.getString("PowerGrid.log.buildingPowerDownPowerUsed", //$NON-NLS-1$
						building.getNickName(), 
						Double.toString(
								Math.round(building.getPoweredDownPowerRequired()*100.00)/100.00
								)));

		}

		setRequiredPower(power);

		logger.log(settlement, Level.FINEST, 0, Msg.getString("PowerGrid.log.totalPowerRequired", //$NON-NLS-1$
					Double.toString(powerRequired)));
	}

	/**
	 * Updates the total energy storage capacity in the grid.
	 * 
	 * @throws BuildingException if error determining total energy storage capacity.
	 */
	private void updateTotalEnergyStorageCapacity() {
		double capacity = manager.getBuildingSet(FunctionType.POWER_STORAGE).stream()
									.mapToDouble(b -> b.getPowerStorage().getCurrentMaxCapacity())
									.sum();
		setStoredEnergyCapacity(capacity);

		logger.log(settlement, Level.FINEST, 0, Msg.getString("PowerGrid.log.totalPowerStorageCapacity", //$NON-NLS-1$
					Double.toString(energyStorageCapacity)));
	}

	/**
	 * Checks if building generates more power than it uses in a given power mode.
	 *
	 * @param building the building
	 * @param mode     {@link PowerMode} the building's power mode to check.
	 * @return true if building supplies more power than it uses. throws
	 *         BuildingException if error in power generation.
	 */
	private boolean powerSurplus(Building b, PowerMode mode) {
		double generated = 0D;
		if (b.hasFunction(FunctionType.POWER_GENERATION)) {
			generated = b.getPowerGeneration().getGeneratedPower();
		}

		double used = 0D;
		if (mode == PowerMode.FULL_POWER)
			used = b.getFullPowerRequired();
		else if (mode == PowerMode.POWER_DOWN)
			used = b.getPoweredDownPowerRequired();

		return generated > used;
	}

	/**
	 * Stores any excess energy into the power grid via battery storage systems in buildings if possible.
	 * 
	 * @param excessEnergy excess grid energy (in kW hr).
	 * @throws BuildingException if error storing excess energy.
	 */
	private void storeExcessPower(double excessEnergy, double time) {
		double excess = excessEnergy;
		Iterator<Building> i = manager.getBuildingSet(FunctionType.POWER_STORAGE).iterator();
		while (i.hasNext()) {
			PowerStorage storage = i.next().getPowerStorage();
			double stored = storage.getkWattHourStored();
			double max = storage.getCurrentMaxCapacity();
			double gap = max - stored;
			double onePercent = max * .01D;

			if (gap > onePercent && excess > 0) {
				// TODO: need to come up with a better battery model with charge capacity
				// parameters from
				// https://www.mathworks.com/help/physmod/elec/ref/genericbattery.html?requestedDomain=www.mathworks.com

				// Note: Tesla runs its batteries up to 4C charging rate
				// see https://teslamotorsclub.com/tmc/threads/limits-of-model-s-charging.36185/

				double accept = chargeBattery(storage, excess, time);

				if (accept > 0 && accept <= excess) {

					// update the resultant energy stored in battery
					stored = stored + accept;
					// update excess energy
					excess = excess - accept;

					// update the energy stored in this battery
					storage.setEnergyStored(stored);
				}
			}
		}
	}

	/**
	 * Receives energy from the grid to charge up a single battery storage system.
	 * 
	 * @param storage PowerStorage
	 * @param excess  energy
	 * @param time    in millisols
	 * @return energy to be delivered
	 */
	public double chargeBattery(PowerStorage storage, double excess, double time) {
		double stored = storage.getkWattHourStored();
		double needed = storage.getCurrentMaxCapacity() - stored;
		double smallest = 0;
		if (needed <= 0 || excess <= 0) {
			return 0;
		}

		else {
			double voltage = storage.getTerminalVoltage();
			// assume the internal resistance of the battery is constant
			double r_int = storage.getResistance();
			double max = storage.getCurrentMaxCapacity();
			double state_of_charge = stored / max;
			// use fudge_factor to dampen the power delivery when the battery is getting
			// depleted
			double fudge_factor = 4 * (1 - state_of_charge);
			double V_out = voltage * R_LOAD / (R_LOAD + r_int);

			if (V_out > 0) {

				double Ah = storage.getAmpHourRating();
				double hr = time * HOURS_PER_MILLISOL;

				// Note: Tesla runs its batteries up to 4C charging rate
				// see https://teslamotorsclub.com/tmc/threads/limits-of-model-s-charging.36185/

				double cRating = storage.geCRating();
				double ampere = cRating * Ah;
				double possible = ampere / 1000D * V_out * hr * fudge_factor;

				smallest = Math.min(excess, Math.min(possible, needed));
			}

			else {
				return 0;
			}

		}

		return smallest;
	}

	/**
	 * Retrieves stored energy from grid-connected batteries.
	 * 
	 * @param needed the energy needed (kWh)
	 * @param time the hours
	 * @return energy to be retrieved (kWh)
	 */
	public double retrieveStoredEnergy(double totalEnergyNeeded, double time) {
		double retrieved = 0;
		double remainingNeed = totalEnergyNeeded;
		double totalAvailable = 0;
		
		Set<Building> storages = manager.getBuildingSet(FunctionType.POWER_STORAGE);
		if (!storages.isEmpty()) {
			
			for (Building b : storages) {
				PowerStorage storage = b.getPowerStorage();
				totalAvailable += computeAvailableEnergyForDischarge(storage, remainingNeed - totalAvailable, time);
			}
		
			double neededPerStorage = totalAvailable / storages.size();
			
			for (Building b : storages) {
				PowerStorage storage = b.getPowerStorage();
				
				if (remainingNeed <= 0) {
					break;
				}

				double available = computeAvailableEnergyForDischarge(storage, RandomUtil.getRandomDouble(neededPerStorage, neededPerStorage * 2), time);
				double stored = storage.getkWattHourStored();

				if (available > 0) {

					// update the resultant energy stored in battery
					stored = stored - available;

					// update energy needed
					remainingNeed = remainingNeed - available;

					// update the energy stored in this battery
					storage.setEnergyStored(stored);

					// update the total retrieved energy
					retrieved = retrieved + available;
				}
			}
		}
		return retrieved;
	}

	/**
	 * Computes the available stored energy to be discharged (from a battery storage system to the grid).
	 * Called by retrieveStoredEnergy
	 * 
	 * @param storage PowerStorage
	 * @param needed  energy
	 * @param time    in millisols
	 * @return energy to be delivered
	 */
	public double computeAvailableEnergyForDischarge(PowerStorage storage, double needed, double time) {
		double possible = 0;
		double stored = storage.getkWattHourStored();

		if (stored <= 0 || needed <= 0)
			return 0;

		double voltage = storage.getTerminalVoltage();
		// assume the internal resistance of the battery is constant
		double resistence = storage.getResistance();
		double max = storage.getCurrentMaxCapacity();
		double stateOfCharge = stored / max;
		// use fudge_factor to dampen the power delivery when the battery is getting
		// depleted
		double fudgeFactor = 3 * stateOfCharge;
		double outputVoltage = voltage * R_LOAD / (R_LOAD + resistence);

		if (outputVoltage <= 0)
			return 0;

		double ampPerHr = storage.getAmpHourRating();
		double hr = time * HOURS_PER_MILLISOL;
		// Note: Set max charging rate as 3C as Tesla runs its batteries up to 4C
		// charging rate
		// see https://teslamotorsclub.com/tmc/threads/limits-of-model-s-charging.36185/

		double cRating = storage.geCRating();
		double ampere = cRating * ampPerHr;
		possible = ampere / 1000D * outputVoltage * hr * fudgeFactor;

		return Math.min(stored, Math.min(possible, needed));
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
