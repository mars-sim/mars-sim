/**
 * Mars Simulation Project
 * PowerGrid.java
 * @version 3.1.0 2016-10-19
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
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The PowerGrid class is a settlement's building power grid.
 */
public class PowerGrid
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(PowerGrid.class.getName());
	public static double PSEUDO_PERCENT_VOLTAGE_DROP = .95D;
	//private static double PSUEDO_CHARGING_RATE = 5D;
	public static double HOURS_PER_MILLISOL = 0.0247 ; //MarsClock.SECONDS_IN_MILLISOL / 3600D;
	
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
	 * @return power grid mode
	 */
	public PowerMode getPowerMode() {
		return powerMode;
	}

	/**
	 * Sets the power grid mode.
	 * @param newPowerMode the new power grid mode.
	 */
	public void setPowerMode(PowerMode newPowerMode) {
		if (powerMode != newPowerMode) {
			if (PowerMode.POWER_UP == newPowerMode) powerMode = PowerMode.POWER_UP;
			else if (PowerMode.POWER_DOWN == newPowerMode) powerMode = PowerMode.POWER_DOWN;
			settlement.fireUnitUpdate(UnitEventType.POWER_MODE_EVENT);
		}
	}

	/**
	 * Gets the generated power in the grid.
	 * @return power in kW
	 */
	public double getGeneratedPower() {
		return powerGenerated;
	}

	/**
	 * Sets the generated power in the grid.
	 * @param newGeneratedPower the new generated power (kW).
	 */
	private void setGeneratedPower(double newGeneratedPower) {
		if (powerGenerated != newGeneratedPower) {
			powerGenerated = newGeneratedPower;
			settlement.fireUnitUpdate(UnitEventType.GENERATED_POWER_EVENT);
		}
	}

	/**
	 * Gets the stored power in the grid.
	 * @return stored power in kW hr.
	 */
	public double getStoredEnergy() {
		return totalEnergyStored;
	}

	/**
	 * Sets the stored power in the grid.
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
	 * @return stored energy capacity in kW hr.
	 */
	public double getStoredEnergyCapacity() {
		return energyStorageCapacity;
	}

	/**
	 * Sets the stored power capacity in the grid.
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
	 * @return power in kW
	 */
	public double getRequiredPower() {
		return powerRequired;
	}

	/**
	 * Sets the required power in the grid.
	 * @param newRequiredPower the new required power (kW).
	 */
	private void setRequiredPower(double newRequiredPower) {
		if (powerRequired != newRequiredPower) {
			powerRequired = newRequiredPower;
			settlement.fireUnitUpdate(UnitEventType.REQUIRED_POWER_EVENT);
		}
	}

	/**
	 * Checks if there is enough power in the grid for all
	 * buildings to be set to full power.
	 * @return true if sufficient power
	 */
	public boolean isSufficientPower() {
		return sufficientPower;
	}

	/**
	 * Time passing for power grid.
	 * @param time amount of time passing (in millisols)
	 */
	public void timePassing(double time) {

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"PowerGrid.log.settlementPowerSituation",
					settlement.getName()
				)
			);
		}

		// update the total power generated in the grid.
		updateTotalPowerGenerated();

		// Determine total power required in the grid.
		updateTotalRequiredPower();

		// 2016-10-18 Update the power flow
		updatePowerFlow(time);
		
		// Update the total power storage capacity in the grid.
		updateTotalEnergyStorageCapacity();

		// Update the total power stored in the grid.
		updateTotalStoredEnergy();

	
		// Update power value.
		determinePowerValue();
	}

	/**
	 * Calculate the flow of power/energy taking place due to the supply and demand of power
	 * @param time
	 */
	public void updatePowerFlow(double time) {		
		// Check if there is enough power generated to fully supply each building.
		if (powerRequired < powerGenerated) {
			
			sufficientPower = true;
			// Store excess power in power storage buildings.
			double timeHr = time * HOURS_PER_MILLISOL; //MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
			double excessEnergy = (powerGenerated - powerRequired) * timeHr;
			storeExcessPower(excessEnergy, time);
		}
		
		else {
			
			sufficientPower = false;
			double neededPower = powerRequired - powerGenerated;
			double timeHr = time * HOURS_PER_MILLISOL; //MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
			double neededEnergy = neededPower * timeHr;		
			// Find available power from power storage buildings.			
			double availablePowerHr = computeStoredEnergy();
			// NOTE : assume the energy flow is instantaneous and 
			// the gauge of the cable is very low and 
			if (availablePowerHr * PSEUDO_PERCENT_VOLTAGE_DROP >= neededEnergy) {
	
				double actualNeeded = neededEnergy / PSEUDO_PERCENT_VOLTAGE_DROP;
				// subtract powerHr from the battery reserve		
				double retrieved = retrieveStoredEnergy(actualNeeded);
				
				if (retrieved >= actualNeeded)
					sufficientPower = true;
				else
					sufficientPower = true;
				
			}
			
			else {
				sufficientPower = false;
			}
			
			if (!sufficientPower) {
				
				neededEnergy = neededEnergy - availablePowerHr;				
				neededPower = neededEnergy / timeHr;
				
				//BuildingManager manager = settlement.getBuildingManager();
				List<Building> buildings = manager.getBuildings();//getACopyOfBuildings();
		
				// Reduce each building's power mode to low power until
				// required power reduction is met.
				if (powerMode != PowerMode.POWER_DOWN) {
					Iterator<Building> iLowPower = buildings.iterator();
					while (iLowPower.hasNext() && (neededPower > 0D)) {
						Building building = iLowPower.next();
						if (!powerSurplus(building, PowerMode.FULL_POWER)) {
							building.setPowerMode(PowerMode.POWER_DOWN);
							neededPower -= building.getFullPowerRequired() -
									building.getPoweredDownPowerRequired();
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
								!(building.hasFunction(BuildingFunction.LIFE_SUPPORT))) {
							building.setPowerMode(PowerMode.NO_POWER);
							neededPower -= building.getPoweredDownPowerRequired();
						}
					}
				}
		
				// If power needs are still not met, turn off the power to each inhabitable building
				// until required power reduction is met.
				if (neededPower > 0D) {
					Iterator<Building> iNoPower = buildings.iterator();
					while (iNoPower.hasNext() && (neededPower > 0D)) {
						Building building = iNoPower.next();
						if (!powerSurplus(building, PowerMode.POWER_DOWN) &&
								// turn off the power to each inhabitable building
								building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
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
	 * @throws BuildingException if error determining total power generated.
	 */
	private void updateTotalPowerGenerated() {
		double tempPowerGenerated = 0D;

		// Add the power generated by all power generation buildings.
		//BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iPow = manager.getBuildings(BuildingFunction.POWER_GENERATION).iterator();
		while (iPow.hasNext()) {
			Building building = iPow.next();
			PowerGeneration gen = (PowerGeneration) building.getFunction(BuildingFunction.POWER_GENERATION);
			tempPowerGenerated += gen.getGeneratedPower();
			// logger.info(((Building) gen).getName() + " generated: " + gen.getGeneratedPower());
		}
		setGeneratedPower(tempPowerGenerated);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"PowerGrid.log.totalPowerGenerated", //$NON-NLS-1$
					Double.toString(powerGenerated)
				)
			);
		}
	}

	/**
	 * Updates the total energy stored in the grid.
	 * @throws BuildingException if error determining total energy stored.
	 */
	private void updateTotalStoredEnergy() {
		double tempPowerStored = 0D;
		//BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iStore = manager.getBuildings(BuildingFunction.POWER_STORAGE).iterator();
		while (iStore.hasNext()) {
			Building building = iStore.next();
			PowerStorage store = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
			tempPowerStored += store.getkWattHourStored();
		}
		setStoredEnergy(tempPowerStored);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"PowerGrid.log.totalPowerStored", //$NON-NLS-1$
					Double.toString(totalEnergyStored)
				)
			);
		}
	}

	/**
	 * Updates the total power required in the grid.
	 * @throws BuildingException if error determining total power required.
	 */
	private void updateTotalRequiredPower() {
		double tempPowerRequired = 0D;
		boolean powerUp = powerMode == PowerMode.POWER_UP;
		//BuildingManager manager = settlement.getBuildingManager();
		List<Building> buildings = manager.getBuildings();//getACopyOfBuildings();
		Iterator<Building> iUsed = buildings.iterator();
		while (iUsed.hasNext()) {
			Building building = iUsed.next();
			if (powerUp) {
				building.setPowerMode(PowerMode.FULL_POWER);
				tempPowerRequired += building.getFullPowerRequired();
				if(logger.isLoggable(Level.FINE)) {
					logger.fine(
						Msg.getString(
							"PowerGrid.log.buildingFullPowerUsed", //$NON-NLS-1$
							building.getNickName(),
							Double.toString(building.getFullPowerRequired())
						)
					);
				}
			}
			else {
				building.setPowerMode(PowerMode.POWER_DOWN);
				tempPowerRequired += building.getPoweredDownPowerRequired();

				if(logger.isLoggable(Level.FINE)) {
					logger.fine(
						Msg.getString(
							"PowerGrid.log.buildingPowerDownPowerUsed", //$NON-NLS-1$
							building.getNickName(),
							Double.toString(building.getPoweredDownPowerRequired())
						)
					);
				}
			}
		}
		setRequiredPower(tempPowerRequired);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"PowerGrid.log.totalPowerRequired", //$NON-NLS-1$
					Double.toString(powerRequired)
				)
			);
		}
	}

	/**
	 * Updates the total energy storage capacity in the grid.
	 * @throws BuildingException if error determining total energy storage capacity.
	 */
	private void updateTotalEnergyStorageCapacity() {
		double tempPowerStorageCapacity = 0D;
		//BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iStore = manager.getBuildings(BuildingFunction.POWER_STORAGE).iterator();
		while (iStore.hasNext()) {
			Building building = iStore.next();
			PowerStorage store = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
			tempPowerStorageCapacity += store.getEnergyStorageCapacity();
		}
		setStoredPowerCapacity(tempPowerStorageCapacity);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"PowerGrid.log.totalPowerStorageCapacity", //$NON-NLS-1$
					Double.toString(energyStorageCapacity)
				)
			);
		}
	}

	/**
	 * Checks if building generates more power
	 * than it uses in a given power mode.
	 *
	 * @param building the building
	 * @param mode {@link PowerMode} the building's power mode to check.
	 * @return true if building supplies more power than it uses.
	 * throws BuildingException if error in power generation.
	 */
	private boolean powerSurplus(Building building, PowerMode mode) {
		double generated = 0D;
		if (building.hasFunction(BuildingFunction.POWER_GENERATION)) {
			PowerGeneration powerGeneration =
					(PowerGeneration) building.getFunction(BuildingFunction.POWER_GENERATION);
			generated = powerGeneration.getGeneratedPower();
		}

		double used = 0D;
		if (mode == PowerMode.FULL_POWER) used = building.getFullPowerRequired();
		else if (mode == PowerMode.POWER_DOWN) used = building.getPoweredDownPowerRequired();

		return generated > used;
	}

	/**
	 * Stores any excess power grid in energy storage buildings if possible.
	 * @param excessEnergy excess grid energy (in kW hr).
	 * @throws BuildingException if error storing excess energy.
	 */
	private void storeExcessPower(double excessEnergy, double time) {
		//System.out.println("time : "+ time);
		//BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> i = manager.getBuildings(BuildingFunction.POWER_STORAGE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			PowerStorage storage = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
			double kWhStored = storage.getkWattHourStored();
			double remainingCapacity = storage.getEnergyStorageCapacity() - kWhStored;
			double voltage = storage.getBatteryVoltage();
			//if (kWhStored == 0) {
			//	logger.info("The grid battery is depleted at " + building.getNickName() + " in " + settlement.getName());					
			//}

			if (remainingCapacity > 0D) {
				// TODO: need to come up with a better battery model with charge capacity parameters from https://www.mathworks.com/help/physmod/elec/ref/genericbattery.html?requestedDomain=www.mathworks.com
				double Ah = storage.getAmpHourRating();
				double hr = time * HOURS_PER_MILLISOL;
				// Note: Set max charging rate as 3C as Tesla runs its batteries up to 4C charging rate
				// see https://teslamotorsclub.com/tmc/threads/limits-of-model-s-charging.36185/
				double chargeRate = 3D - 2.9D * voltage / PowerStorage.BATTERY_MAX_VOLTAGE ;
				// make minimum charging rate as 0.1C;
				double ampere = chargeRate * Ah * hr * storage.getBatteryHealth();
				double energyToStore = ampere /1000D * (PowerStorage.SECONDARY_LINE_VOLTAGE - voltage);
				//logger.info("Ah : " + Math.round(Ah * 100D)/100D
				//		+ "    hr : " + Math.round(hr * 10000D)/10000D
				//		+ "    ampere : " + Math.round(ampere * 100D)/100D
				//		+ "    energyToStore : " + Math.round(energyToStore * 100D)/100D
				//		+ " in " + building.getNickName());
				
				//double amp = excessEnergy/PowerStorage.SECONDARY_LINE_VOLTAGE;
				//double percent_charged = Ah * PSUEDO_CHARGING_RATE * time;
				//double energyToStore = excessEnergy * percent_charged;
	
				if (remainingCapacity < energyToStore) {
					// if the max cap of this building's battery has been reached, look for another building's battery to store the excess energy					
					energyToStore = remainingCapacity;
					logger.info("The grid battery is now fully charged at " + Math.round(kWhStored * 100D)/100D + " kWh at " + building.getNickName() + " in " + settlement.getName());
				}
				else { // remainingCapacity >= energyToStore
					;
				}				
				// the excessEnergy will be split into many chunks and stored at an array of batteries across the settlement
				excessEnergy = excessEnergy - energyToStore;
				// TODO: calculate how much power can be rejected via radiators 
				// raise settlement temperature (or capture the excess power as heat)
				// or turn down some modules in the power plant to conserve resources
				storage.setEnergyStored(kWhStored + energyToStore);
/*		
				if (totalEnergyStored + energyToStore < energyStorageCapacity)
					totalEnergyStored = totalEnergyStored + energyToStore;
				else
					totalEnergyStored = energyStorageCapacity;
*/		
			}

			else {
				// if there is no more capacity to store on this building's battery
				// iterate to the next building
			}
		}
	}

	/**
	 * Retrieves stored energy for the grid.
	 * @return stored energy retrieved (kW hr).
	 */
	private double computeStoredEnergy() {
		double available = 0;
		//BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> i = manager.getBuildings(BuildingFunction.POWER_STORAGE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			PowerStorage storage = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
			if (storage.getkWattHourStored() > 0)
				available = available + storage.getkWattHourStored();
		}
		
		return available;
	}

	/**
	 * Retrieves stored energy for the grid.
	 * @param needed the energy needed (kW hr).
	 * @return energy retrieved (kW hr).
	 */
	// 2016-11-13 Added retrieveStoredEnergy()
	private double retrieveStoredEnergy(double needed) {
		double retrieved = 0;
		List<Building> list = manager.getBuildings(BuildingFunction.POWER_STORAGE);
		for (Building b : list) {
			PowerStorage storage = (PowerStorage) b.getFunction(BuildingFunction.POWER_STORAGE);		
/*		
			if ((storage.getPowerStored() > 0D) && (neededPower > 0D)) {
				double retrievedPower = neededPower;
				if (storage.getPowerStored() < retrievedPower) 
					retrievedPower = storage.getPowerStored();
				storage.setPowerStored(storage.getPowerStored() - retrievedPower);
				neededPower -= retrievedPower;
			}
		}
*/
			if (needed <= 0) {
				return retrieved;
			}

			else if (storage.getkWattHourStored() > 0) {
				// Check how much energy available
				double available = storage.getkWattHourStored();				
				double newAmount = 0;

				if (needed <= available) {
					newAmount = available - needed;
					needed = 0;
				}

				else { // if (needed > available)
					newAmount = 0;
					needed = needed - available;
				}

				retrieved = retrieved + newAmount;
				storage.setEnergyStored(newAmount);
			}
		}

		return retrieved;
	}
	
	/**
	 * Gets the value of electrical power at the settlement.
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