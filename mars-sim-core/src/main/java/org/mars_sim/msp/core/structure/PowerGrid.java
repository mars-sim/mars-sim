/**
 * Mars Simulation Project
 * PowerGrid.java
 * @version 3.07 2014-12-06

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

	// Data members
	private PowerMode powerMode;
	private double powerGenerated;
	private double powerStored;
	private double powerStorageCapacity;
	private double powerRequired;
	private boolean sufficientPower;
	private Settlement settlement;
	private double powerValue;

	/**
	 * Constructor.
	 */
	public PowerGrid(Settlement settlement) {
		this.settlement = settlement;
		powerMode = PowerMode.POWER_UP;
		powerGenerated = 0D;
		powerStored = 0D;
		powerStorageCapacity = 0D;
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
	public double getStoredPower() {
		return powerStored;
	}

	/**
	 * Sets the stored power in the grid.
	 * @param newPowerStored the new stored power (kW hr).
	 */
	public void setStoredPower(double newPowerStored) {
		if (powerStored != newPowerStored) {
			powerStored = newPowerStored;
			settlement.fireUnitUpdate(UnitEventType.STORED_POWER_EVENT);
		}
	}

	/**
	 * Gets the stored power capacity in the grid.
	 * @return stored power capacity in kW hr.
	 */
	public double getStoredPowerCapacity() {
		return powerStorageCapacity;
	}

	/**
	 * Sets the stored power capacity in the grid.
	 * @param newPowerStorageCapacity the new stored power capacity (kW hr).
	 */
	public void setStoredPowerCapacity(double newPowerStorageCapacity) {
		if (powerStorageCapacity != newPowerStorageCapacity) {
			powerStorageCapacity = newPowerStorageCapacity;
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

		// Update the total power stored in the grid.
		updateTotalStoredPower();

		// Update the total power storage capacity in the grid.
		updateTotalPowerStorageCapacity();

		// Determine total power required in the grid.
		updateTotalRequiredPower();

		// Check if there is enough power generated to fully supply each building.
		if (powerRequired <= powerGenerated) {
			sufficientPower = true;

			// Store excess power in power storage buildings.
			double timeHr = MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
			double excessPower = (powerGenerated - powerRequired) * timeHr;
			storeExcessPower(excessPower);
		}
		else {
			sufficientPower = false;
			double neededPower = powerRequired - powerGenerated;

			// Retrieve power from power storage buildings.
			double timeHr = MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
			double neededPowerHr = neededPower * timeHr;
			neededPowerHr = retrieveStoredPower(neededPowerHr);
			neededPower = neededPowerHr / timeHr;

			BuildingManager manager = settlement.getBuildingManager();
			List<Building> buildings = manager.getACopyOfBuildings();

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
							building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
						building.setPowerMode(PowerMode.NO_POWER);
						neededPower -= building.getPoweredDownPowerRequired();
					}
				}
			}
		}

		// Update power value.
		determinePowerValue();
	}

	/**
	 * Updates the total power generated in the grid.
	 * @throws BuildingException if error determining total power generated.
	 */
	private void updateTotalPowerGenerated() {
		double tempPowerGenerated = 0D;

		// Add the power generated by all power generation buildings.
		BuildingManager manager = settlement.getBuildingManager();
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
	 * Updates the total power stored in the grid.
	 * @throws BuildingException if error determining total power stored.
	 */
	private void updateTotalStoredPower() {
		double tempPowerStored = 0D;
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iStore = manager.getBuildings(BuildingFunction.POWER_STORAGE).iterator();
		while (iStore.hasNext()) {
			Building building = iStore.next();
			PowerStorage store = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
			tempPowerStored += store.getPowerStored();
		}
		setStoredPower(tempPowerStored);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"PowerGrid.log.totalPowerStored", //$NON-NLS-1$
					Double.toString(powerStored)
				)
			);
		}
	}

	/**
	 * Updates the toal power required in the grid.
	 * @throws BuildingException if error determining total power required.
	 */
	private void updateTotalRequiredPower() {
		double tempPowerRequired = 0D;
		boolean powerUp = powerMode == PowerMode.POWER_UP;
		BuildingManager manager = settlement.getBuildingManager();
		List<Building> buildings = manager.getACopyOfBuildings();
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
	 * Updates the total power storage capacity in the grid.
	 * @throws BuildingException if error determining total power storage capacity.
	 */
	private void updateTotalPowerStorageCapacity() {
		double tempPowerStorageCapacity = 0D;
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iStore = manager.getBuildings(BuildingFunction.POWER_STORAGE).iterator();
		while (iStore.hasNext()) {
			Building building = iStore.next();
			PowerStorage store = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
			tempPowerStorageCapacity += store.getPowerStorageCapacity();
		}
		setStoredPowerCapacity(tempPowerStorageCapacity);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"PowerGrid.log.totalPowerStorageCapacity", //$NON-NLS-1$
					Double.toString(powerStorageCapacity)
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
	 * Stores any excess grid power in power storage buildings if possible.
	 * @param excessPower excess grid power (in kW hr).
	 * @throws BuildingException if error storing excess power.
	 */
	private void storeExcessPower(double excessPower) {
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> i = manager.getBuildings(BuildingFunction.POWER_STORAGE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			PowerStorage storage = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
			double remainingCapacity = storage.getPowerStorageCapacity() - storage.getPowerStored();
			if (remainingCapacity > 0D) {
				double powerToStore = excessPower;
				if (remainingCapacity < powerToStore) powerToStore = remainingCapacity;
				storage.setPowerStored(storage.getPowerStored() + powerToStore);
				excessPower -= powerToStore;
			}
		}
	}

	/**
	 * Retrieves stored power for the grid.
	 * @param neededPower the power needed (kW hr).
	 * @return stored power retrieved (kW hr).
	 * @throws BuildingException if error retrieving power.
	 */
	private double retrieveStoredPower(double neededPower) {
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> i = manager.getBuildings(BuildingFunction.POWER_STORAGE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			PowerStorage storage = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
			if ((storage.getPowerStored() > 0D) && (neededPower > 0D)) {
				double retrievedPower = neededPower;
				if (storage.getPowerStored() < retrievedPower) retrievedPower = storage.getPowerStored();
				storage.setPowerStored(storage.getPowerStored() - retrievedPower);
				neededPower -= retrievedPower;
			}
		}
		return neededPower;
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
		double supply = powerGenerated + (powerStored / 2D);

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
	}
}