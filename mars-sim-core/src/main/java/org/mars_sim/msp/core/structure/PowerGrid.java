/**
 * Mars Simulation Project
 * PowerGrid.java
 * @version 3.1.0 2017-11-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;

/**
 * The PowerGrid class is a settlement's building power grid.
 */
public class PowerGrid implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(PowerGrid.class.getName());

	private static String sourceName = logger.getName();

	public static double R_LOAD = 1000; // assume constant load resistance

	public static double percentAverageVoltageDrop = 98D;

	public static double HOURS_PER_MILLISOL = 0.0247; // MarsClock.SECONDS_IN_MILLISOL / 3600D;

	public double degradationRatePerSol = .0004;

	public double systemEfficiency = 1D;

	// Data members
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

		sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());

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
//		if (!Double.isNaN(p) && !Double.isInfinite(p) 
		if (powerGenerated != p) {
			powerGenerated = p;
			settlement.fireUnitUpdate(UnitEventType.GENERATED_POWER_EVENT);
		}
	}

	/**
	 * Gets the stored power in the grid.
	 * 
	 * @return stored power in kW hr.
	 */
	public double getStoredEnergy() {
		return totalEnergyStored;
	}

	/**
	 * Sets the stored power in the grid.
	 * 
	 * @param newEnergyStored the new stored power (kW hr).
	 */
	public void setStoredEnergy(double newEnergyStored) {
		if (totalEnergyStored != newEnergyStored) {
			totalEnergyStored = newEnergyStored;
			settlement.fireUnitUpdate(UnitEventType.STORED_POWER_EVENT);
		}
	}

	/**
	 * Gets the stored energy capacity in the grid.
	 * 
	 * @return stored energy capacity in kW hr.
	 */
	public double getStoredEnergyCapacity() {
		return energyStorageCapacity;
	}

	/**
	 * Sets the stored power capacity in the grid.
	 * 
	 * @param newPowerStorageCapacity the new stored power capacity (kW hr).
	 */
	public void setStoredPowerCapacity(double newPowerStorageCapacity) {
		if (energyStorageCapacity != newPowerStorageCapacity) {
			energyStorageCapacity = newPowerStorageCapacity;
			settlement.fireUnitUpdate(UnitEventType.STORED_POWER_CAPACITY_EVENT);
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
	public void timePassing(double time) {

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest(Msg.getString("PowerGrid.log.settlementPowerSituation", settlement.getName()));
		}

		// update the total power generated in the grid.
		updateTotalPowerGenerated();

		// Determine total power required in the grid.
		updateTotalRequiredPower();

		// Update overal grid efficiency.
		updateEfficiency(time);

		// Update the power flow.
		updatePowerFlow(time);

		// Update the total power storage capacity in the grid.
		updateTotalEnergyStorageCapacity();

		// Update the total power stored in the grid.
		updateTotalStoredEnergy();

		// Update power value.
		determinePowerValue();
	}

	public void updateEfficiency(double time) {
		double d_factor = degradationRatePerSol * time / 1000D;
		systemEfficiency = systemEfficiency - systemEfficiency * d_factor;
	}

	/**
	 * Calculate the flow of power/energy taking place due to the supply and demand
	 * of power
	 * 
	 * @param time
	 */
	public void updatePowerFlow(double time) {
		// Check if there is enough power generated to fully supply each building.
		if (powerRequired < powerGenerated) { // excess energy to charge grid batteries

			sufficientPower = true;
			// Store excess power in power storage buildings.
			double timeHr = time * HOURS_PER_MILLISOL; // MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
			double excessEnergy = (powerGenerated - powerRequired) * timeHr * systemEfficiency;
			storeExcessPower(excessEnergy, time);
		}

		else { // insufficient power produced, need to pull energy from batteries to meet the
				// demand

			sufficientPower = false;
			double neededPower = powerRequired - powerGenerated;
			double timeInHour = time * HOURS_PER_MILLISOL; // MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
			double neededEnergy = neededPower * timeInHour / percentAverageVoltageDrop * 100D;
			// Find available power from power storage buildings.
			// double totalAvailablekWHr = computeTotalStoredEnergy();

			// NOTE : assume the energy flow is instantaneous and
			// the gauge of the cable is very low

			// subtract powerHr from the battery reserve
			double retrieved = retrieveStoredEnergy(neededEnergy, time);

			double delta_energy = neededEnergy - retrieved;

			if (retrieved >= 0 && delta_energy == 0) { // if the grid batteries has more than enough
				sufficientPower = true;
			}

			if (!sufficientPower) { // if still not having sufficient power

				// compute power needed
				neededPower = delta_energy / timeInHour;

				// BuildingManager manager = settlement.getBuildingManager();
				List<Building> buildings = manager.getBuildings();// getACopyOfBuildings();

				// Reduce each building's power mode to low power until
				// required power reduction is met.
				if (powerMode != PowerMode.POWER_DOWN) {
					Iterator<Building> iLowPower = buildings.iterator();
					while (iLowPower.hasNext() && (neededPower > 0D)) {
						Building building = iLowPower.next();
						if (!powerSurplus(building, PowerMode.FULL_POWER)) {
							building.setPowerMode(PowerMode.POWER_DOWN);
							neededPower -= building.getFullPowerRequired() - building.getPoweredDownPowerRequired();
						}
					}
				}

				// If power needs are still not met, turn off the power to each
				// uninhabitable building until required power reduction is met.
				if (neededPower > 0D) {
					Iterator<Building> iNoPower = buildings.iterator();
					while (iNoPower.hasNext() && (neededPower > 0D)) {
						Building building = iNoPower.next();
						if (!powerSurplus(building, PowerMode.POWER_DOWN) &&
						// turn off the power to each uninhabitable building
								!(building.hasFunction(FunctionType.LIFE_SUPPORT))) {
							building.setPowerMode(PowerMode.NO_POWER);
							neededPower -= building.getPoweredDownPowerRequired();
						}
					}
				}

				// If power needs are still not met, turn off the power to each inhabitable
				// building
				// until required power reduction is met.
				if (neededPower > 0D) {
					Iterator<Building> iNoPower = buildings.iterator();
					while (iNoPower.hasNext() && (neededPower > 0D)) {
						Building building = iNoPower.next();
						if (!powerSurplus(building, PowerMode.POWER_DOWN) &&
						// turn off the power to each inhabitable building
								building.hasFunction(FunctionType.LIFE_SUPPORT)) {
							building.setPowerMode(PowerMode.NO_POWER);
							neededPower -= building.getPoweredDownPowerRequired();
						}
					}
				}
			}
		}
	}

	/**
	 * Updates the total power generated in the grid.
	 * 
	 * @throws BuildingException if error determining total power generated.
	 */
	private void updateTotalPowerGenerated() {
		double power = 0D;

		// Add the power generated by all power generation buildings.
		// BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iPow = manager.getBuildings(FunctionType.POWER_GENERATION).iterator();
		while (iPow.hasNext()) {
			power += iPow.next().getPowerGeneration().getGeneratedPower();
		}
		setGeneratedPower(power);

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest(Msg.getString("PowerGrid.log.totalPowerGenerated", //$NON-NLS-1$
					Double.toString(power)));
		}
	}

	/**
	 * Updates the total energy stored in the grid.
	 * 
	 * @throws BuildingException if error determining total energy stored.
	 */
	private void updateTotalStoredEnergy() {
		double store = 0D;
		// BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iStore = manager.getBuildings(FunctionType.POWER_STORAGE).iterator();
		while (iStore.hasNext()) {
			Building b = iStore.next();
			// PowerStorage store = (PowerStorage)
			// building.getFunction(BuildingFunction.POWER_STORAGE);
			store += b.getPowerStorage().getkWattHourStored();
		}
		setStoredEnergy(store);

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest(Msg.getString("PowerGrid.log.totalPowerStored", //$NON-NLS-1$
					Double.toString(totalEnergyStored)));
		}
	}

	/**
	 * Updates the total power required in the grid.
	 * 
	 * @throws BuildingException if error determining total power required.
	 */
	private void updateTotalRequiredPower() {
		double power = 0D;
		boolean powerUp = powerMode == PowerMode.POWER_UP;
		List<Building> buildings = manager.getBuildings();
		Iterator<Building> iUsed = buildings.iterator();
		while (iUsed.hasNext()) {
			Building building = iUsed.next();
			if (powerUp) {
				building.setPowerMode(PowerMode.FULL_POWER);
				power += building.getFullPowerRequired();
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest(Msg.getString("PowerGrid.log.buildingFullPowerUsed", //$NON-NLS-1$
							building.getNickName(), 
							Double.toString(
									Math.round(building.getFullPowerRequired()*100.00)/100.00
									)));
				}
			}

			power += building.getPoweredDownPowerRequired();

			if (logger.isLoggable(Level.FINEST)) {
				logger.finest(Msg.getString("PowerGrid.log.buildingPowerDownPowerUsed", //$NON-NLS-1$
						building.getNickName(), 
						Double.toString(
								Math.round(building.getPoweredDownPowerRequired()*100.00)/100.00
								)));
			}

		}

		setRequiredPower(power);

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest(Msg.getString("PowerGrid.log.totalPowerRequired", //$NON-NLS-1$
					Double.toString(powerRequired)));
		}
	}

	/**
	 * Updates the total energy storage capacity in the grid.
	 * 
	 * @throws BuildingException if error determining total energy storage capacity.
	 */
	private void updateTotalEnergyStorageCapacity() {
		double store = 0D;
		// BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iStore = manager.getBuildings(FunctionType.POWER_STORAGE).iterator();
		while (iStore.hasNext()) {
			Building b = iStore.next();
			store += b.getPowerStorage().getCurrentMaxCapacity();
		}

		setStoredPowerCapacity(store);

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest(Msg.getString("PowerGrid.log.totalPowerStorageCapacity", //$NON-NLS-1$
					Double.toString(energyStorageCapacity)));
		}
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
	 * Stores any excess power grid in energy storage buildings if possible.
	 * 
	 * @param excessEnergy excess grid energy (in kW hr).
	 * @throws BuildingException if error storing excess energy.
	 */
	private void storeExcessPower(double excessEnergy, double time) {
		// double totalDelivered = 0;
		double excess = excessEnergy;
		Iterator<Building> i = manager.getBuildings(FunctionType.POWER_STORAGE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			PowerStorage storage = (PowerStorage) building.getFunction(FunctionType.POWER_STORAGE);
			double stored = storage.getkWattHourStored();
			double max = storage.getCurrentMaxCapacity();
			double gap = max - stored;
			double one_percent = max * .01;

			// logger.info("The grid battery at " + building.getNickName() + " in " +
			// settlement.getName() + " is currently at " + Math.round(kWhStored *
			// 100D)/100D + " kWh");

			if (gap > one_percent && excess > 0) {
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

//					gap = max - stored;
//					one_percent = max * .01;
//					
//					if (gap <= one_percent)
//						LogConsolidated.log(logger, Level.INFO, 10000, sourceName, 
//								"The grid battery at " + building.getNickName() + " in " + settlement.getName() 
//								+ " has been charged to 99% (at " + Math.round(stored * 100D)/100D + " kWh)", null);

				}

			}

		}

	}

	/**
	 * Deliver stored energy from a battery
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

				double c_Rating = storage.geCRating();

				// double chargeRate = c_Rating - c_Rating *.99 * voltage /
				// PowerStorage.BATTERY_MAX_VOLTAGE ;
				// logger.info("chargeRate is " + Math.round(chargeRate*100D)/100D);
				// double ampere = chargeRate * Ah * hr * storage.getBatteryHealth();

				double ampere = c_Rating * Ah;
				double possible = ampere / 1000D * V_out * hr * fudge_factor;

				smallest = Math.min(excess, Math.min(possible, needed));

//				logger.info("Charging of "
//						storage.getBuilding().getNickName() 
//						+ "\t    excess : " + Math.round(excess*100D)/100D
//						+ "	   Ah : " + Math.round(Ah * 100D)/100D
//						+ "    hr : " + Math.round(hr * 10000D)/10000D
//						+ "    ampere : " + Math.round(ampere * 100D)/100D
//						+ "    delta : " + Math.round(delta * 100D)/100D
//						+ "    possible : " + Math.round(possible * 100D)/100D
//						+ "    needed : " + Math.round(needed*100D)/100D
//						+ "    stored : " + Math.round(stored*100D)/100D
//						+ "    smallest : " + Math.round(smallest*100D)/100D);

			}

			else {
				return 0;
			}

		}

		return smallest;
	}

//	/**
//	 * Compute total stored energy in the grid.
//	 * @return stored energy retrieved (kW hr).
//	 
//	private double computeTotalStoredEnergy() {
//		double available = 0;
//		//BuildingManager manager = settlement.getBuildingManager();
//		Iterator<Building> i = manager.getBuildings(BuildingFunction.POWER_STORAGE).iterator();
//		while (i.hasNext()) {
//			Building building = i.next();
//			PowerStorage storage = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
//			if (storage.getkWattHourStored() > 0)
//				available = available + storage.getkWattHourStored();
//		}
//		
//		return available;
//	}

	/**
	 * Retrieves stored energy from the grid.
	 * 
	 * @param needed the energy needed (kW hr).
	 * @return energy to be retrieved (kW hr).
	 */
	// 2016-11-13 Added retrieveStoredEnergy()
	private double retrieveStoredEnergy(double energyNeeded, double time) {
		double retrieved = 0;
		double needed = energyNeeded;
		List<Building> list = manager.getBuildings(FunctionType.POWER_STORAGE);
		for (Building b : list) {
			PowerStorage storage = (PowerStorage) b.getFunction(FunctionType.POWER_STORAGE);

//			if ((storage.getPowerStored() > 0D) && (neededPower > 0D)) {
//				double retrievedPower = neededPower;
//				if (storage.getPowerStored() < retrievedPower) 
//					retrievedPower = storage.getPowerStored();
//				storage.setPowerStored(storage.getPowerStored() - retrievedPower);
//				neededPower -= retrievedPower;
//			}
//		}

			if (needed <= 0) {
				return 0;
			}

			else {

				double available = dischargeBattery(storage, needed, time);
				double stored = storage.getkWattHourStored();

				if (available > 0 && available <= needed) {

					// update the resultant energy stored in battery
					stored = stored - available;

					// update energy needed
					needed = needed - available;

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
	 * Deliver stored energy from a battery
	 * 
	 * @param storage PowerStorage
	 * @param needed  energy
	 * @param time    in millisols
	 * @return energy to be delivered
	 */
	public double dischargeBattery(PowerStorage storage, double needed, double time) {
		double smallest = 0;
		double possible = 0;
		double stored = storage.getkWattHourStored();

		if (stored <= 0 || needed <= 0) {
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
			double fudge_factor = 3 * state_of_charge;
			double V_out = voltage * R_LOAD / (R_LOAD + r_int);

			if (V_out > 0) {

				double Ah = storage.getAmpHourRating();
				double hr = time * HOURS_PER_MILLISOL;
				// Note: Set max charging rate as 3C as Tesla runs its batteries up to 4C
				// charging rate
				// see https://teslamotorsclub.com/tmc/threads/limits-of-model-s-charging.36185/

				double c_Rating = storage.geCRating();

				// double chargeRate = c_Rating - c_Rating *.99 * voltage /
				// PowerStorage.BATTERY_MAX_VOLTAGE ;
				// logger.info("chargeRate is " + Math.round(chargeRate*100D)/100D);
				// double ampere = chargeRate * Ah * hr * storage.getBatteryHealth();

				double ampere = c_Rating * Ah;
				possible = ampere / 1000D * V_out * hr * fudge_factor;

				smallest = Math.min(stored, Math.min(possible, needed));

//				logger.info("Discharging of " 
//						+ storage.getBuilding().getNickName() 
//						+ "\t    needed : " + Math.round(needed*100D)/100D
//						+ "    stored : " + Math.round(stored*100D)/100D
//						+ "	   Ah : " + Math.round(Ah * 100D)/100D
//						+ "    hr : " + Math.round(hr * 10000D)/10000D
//						+ "    ampere : " + Math.round(ampere * 100D)/100D
//						+ "    V_out : " + Math.round(V_out * 100D)/100D
//						+ "    possible : " + Math.round(possible * 100D)/100D
//						+ "    smallest : " + Math.round(smallest*100D)/100D);

			}

			else {
				return 0;
			}
		}

		// logger.info(energyToDeliver + " / " + totalStored);

		return smallest;
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
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		powerMode = null;
		settlement = null;
		manager = null;
	}
}