/**
 * Mars Simulation Project
 * ThermalSystem.java
 * @version 3.07 2014-10-17
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.text.DecimalFormat;
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
import org.mars_sim.msp.core.structure.building.function.HeatMode;
import org.mars_sim.msp.core.structure.building.function.ThermalStorage;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The ThermalSystem class is the settlement's Heating Distribution and Storage Subsystem.
 */
public class ThermalSystem
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ThermalSystem.class.getName());

	DecimalFormat fmt = new DecimalFormat("#.####"); 
	
	// Data members
	private HeatMode heatMode;
	private double heatGenerated;
	private double heatStored;
	private double thermalStorageCapacity;
	private double heatRequired;
	private boolean sufficientHeat;
	private Settlement settlement;
	private double heatValue;

	private int count=0;
	/**
	 * Constructor.
	 */
	public ThermalSystem(Settlement settlement) {
		count= count+1;
			//logger.info("constructor : count is " + count);
		this.settlement = settlement;
		heatMode = HeatMode.POWER_UP;
		heatGenerated = 0D;
		heatStored = 0D;
		thermalStorageCapacity = 0D;
		heatRequired = 0D;
		sufficientHeat = true;
	}

	/**
	 * Gets the heat grid mode.
	 * @return heat grid mode
	 */
	public HeatMode getHeatMode() {
		return heatMode;
	}

	/**
	 * Sets the heat grid mode.
	 * @param newHeatMode the new heat grid mode.
	 */
	public void setHeatMode(HeatMode newHeatMode) {
		if (heatMode != newHeatMode) {
			if (HeatMode.POWER_UP == newHeatMode) heatMode = HeatMode.POWER_UP;
			else if (HeatMode.POWER_DOWN == newHeatMode) heatMode = HeatMode.POWER_DOWN;
			settlement.fireUnitUpdate(UnitEventType.HEAT_MODE_EVENT);
		}
	}

	/**
	 * Gets the generated heat in the grid.
	 * @return heat [in Joules]
	 */
	public double getGeneratedHeat() {
		//logger.info("getGeneratedHeat() : heatGenerated is " + fmt.format(heatGenerated) ); 
		return heatGenerated;
	}

	/**
	 * Sets the generated heat in the grid.
	 * @param newGeneratedHeat the new generated heat (kW).
	 */
	private void setGeneratedHeat(double newGeneratedHeat) {
		if (heatGenerated != newGeneratedHeat) {
			heatGenerated = newGeneratedHeat;
			settlement.fireUnitUpdate(UnitEventType.GENERATED_HEAT_EVENT);
		}
	}

	/**
	 * Gets the stored heat in the grid.
	 * @return stored heat in kW hr.
	 */
	public double getStoredHeat() {
		return heatStored;
	}

	/**
	 * Sets the stored heat in the grid.
	 * @param newHeatStored the new stored heat (kW hr).
	 */
	public void setStoredHeat(double newHeatStored) {
		if (heatStored != newHeatStored) {
			heatStored = newHeatStored;
			settlement.fireUnitUpdate(UnitEventType.STORED_HEAT_EVENT);
		}
	}

	/**
	 * Gets the stored heat capacity in the grid.
	 * @return stored heat capacity in kW hr.
	 */
	public double getStoredHeatCapacity() {
		return thermalStorageCapacity;
	}

	/**
	 * Sets the stored heat capacity in the grid.
	 * @param newThermalStorageCapacity the new stored heat capacity (kW hr).
	 */
	public void setStoredHeatCapacity(double newThermalStorageCapacity) {
		if (thermalStorageCapacity != newThermalStorageCapacity) {
			thermalStorageCapacity = newThermalStorageCapacity;
			settlement.fireUnitUpdate(UnitEventType.STORED_HEAT_CAPACITY_EVENT);
		}
	}

	/**
	 * Gets the heat required from the grid.
	 * @return heat in kW
	 */
	public double getRequiredHeat() {
		return heatRequired;
	}

	/**
	 * Sets the required heat in the grid.
	 * @param newRequiredHeat the new required heat (kW).
	 */
	private void setRequiredHeat(double newRequiredHeat) {
		if (heatRequired != newRequiredHeat) {
			heatRequired = newRequiredHeat;
			settlement.fireUnitUpdate(UnitEventType.REQUIRED_HEAT_EVENT);
			
		}
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
	 * Time passing for heat grid.
	 * @param time amount of time passing (in millisols)
	 */
	public void timePassing(double time) {

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"ThermalSystem.log.settlementHeatSituation",
					settlement.getName()
				)
			);
		}

		// update the total heat generated in the grid.
		updateTotalHeatGenerated();

		// Update the total heat stored in the grid.
		updateTotalStoredHeat();

		// Update the total heat storage capacity in the grid.
		updateTotalThermalStorageCapacity();

		// Determine total heat required in the grid.
		updateTotalRequiredHeat();

		// Check if there is enough heat generated to fully supply each building.
		if (heatRequired <= heatGenerated) {
			sufficientHeat = true;

			// Store excess heat in heat storage buildings.
			double timeHr = MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
			double excessHeat = (heatGenerated - heatRequired) * timeHr;
			storeExcessHeat(excessHeat);
		}
		else {
			sufficientHeat = false;
			double neededHeat = heatRequired - heatGenerated;

			// Retrieve heat from heat storage buildings.
			double timeHr = MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
			double neededHeatHr = neededHeat * timeHr;
			neededHeatHr = retrieveStoredHeat(neededHeatHr);
			neededHeat = neededHeatHr / timeHr;

			BuildingManager manager = settlement.getBuildingManager();
			List<Building> buildings = manager.getBuildings();

			// Reduce each building's heat mode to low heat until 
			// required heat reduction is met.
			if (heatMode != HeatMode.POWER_DOWN) {
				Iterator<Building> iLowHeat = buildings.iterator();
				while (iLowHeat.hasNext() && (neededHeat > 0D)) {
					Building building = iLowHeat.next();
					if (!heatSurplus(building, HeatMode.FULL_POWER)) {
						building.setHeatMode(HeatMode.POWER_DOWN);
						neededHeat -= building.getFullHeatRequired() - 
								building.getPoweredDownHeatRequired();
					}
				}
			}

			// If heat needs are still not met, turn off the heat to each 
			// uninhabitable building until required heat reduction is met.
			if (neededHeat > 0D) {
				Iterator<Building> iNoHeat = buildings.iterator();
				while (iNoHeat.hasNext() && (neededHeat > 0D)) {
					Building building = iNoHeat.next();
					if (!heatSurplus(building, HeatMode.POWER_DOWN) && 
							!(building.hasFunction(BuildingFunction.LIFE_SUPPORT))) {
						building.setHeatMode(HeatMode.NO_POWER);
						neededHeat -= building.getPoweredDownHeatRequired();
					}
				}
			}

			// If heat needs are still not met, turn off the heat to each inhabitable building 
			// until required heat reduction is met.
			if (neededHeat > 0D) {
				Iterator<Building> iNoHeat = buildings.iterator();
				while (iNoHeat.hasNext() && (neededHeat > 0D)) {
					Building building = iNoHeat.next();
					if (!heatSurplus(building, HeatMode.POWER_DOWN) && 
							building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
						building.setHeatMode(HeatMode.NO_POWER);
						neededHeat -= building.getPoweredDownHeatRequired();
					}
				}
			}
		}

		// Update heat value.
		determineHeatValue();
	}

	/**
	 * Updates the total heat generated in the grid.
	 * @throws BuildingException if error determining total heat generated.
	 */
	private void updateTotalHeatGenerated() {
		double tempHeatGenerated = 0D;

		// Add the heat generated by all heat generation buildings.
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iHeat = manager.getBuildings(BuildingFunction.THERMAL_GENERATION).iterator();
		while (iHeat.hasNext()) {
			Building building = iHeat.next();
			ThermalGeneration gen = (ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
			tempHeatGenerated += gen.getGeneratedHeat();
			// logger.info(((Building) gen).getName() + " generated: " + gen.getGeneratedHeat());
		}
		setGeneratedHeat(tempHeatGenerated);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"ThermalSystem.log.totalHeatGenerated", //$NON-NLS-1$
					Double.toString(heatGenerated)
				)
			);
		}
	}

	/**
	 * Updates the total heat stored in the grid.
	 * @throws BuildingException if error determining total heat stored.
	 */
	private void updateTotalStoredHeat() {
		double tempHeatStored = 0D;
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iStore = manager.getBuildings(BuildingFunction.THERMAL_STORAGE).iterator();
		while (iStore.hasNext()) {
			Building building = iStore.next();
			ThermalStorage store = (ThermalStorage) building.getFunction(BuildingFunction.THERMAL_STORAGE);
			tempHeatStored += store.getHeatStored();
		}
		setStoredHeat(tempHeatStored);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"ThermalSystem.log.totalHeatStored", //$NON-NLS-1$
					Double.toString(heatStored)
				)
			);
		}
	}

	/**
	 * Updates the total heat required in the grid.
	 * @throws BuildingException if error determining total heat required.
	 */
	private void updateTotalRequiredHeat() {
		double tempHeatRequired = 0D;
		boolean heatUp = heatMode == HeatMode.POWER_UP;
		BuildingManager manager = settlement.getBuildingManager();
		List<Building> buildings = manager.getBuildings();
		Iterator<Building> iUsed = buildings.iterator();
		while (iUsed.hasNext()) {
			Building building = iUsed.next();
			if (heatUp) {
				building.setHeatMode(HeatMode.FULL_POWER);
				tempHeatRequired += building.getFullHeatRequired();
				if(logger.isLoggable(Level.FINE)) {
					logger.fine(
						Msg.getString(
							"ThermalSystem.log.buildingFullHeatUsed", //$NON-NLS-1$
							building.getName(),
							Double.toString(building.getFullHeatRequired())
						)
					);
				}
			}
			else {
				building.setHeatMode(HeatMode.POWER_DOWN);
				tempHeatRequired += building.getPoweredDownHeatRequired();

				if(logger.isLoggable(Level.FINE)) {
					logger.fine(
						Msg.getString(
							"ThermalSystem.log.buildingHeatDownHeatUsed", //$NON-NLS-1$
							building.getName(),
							Double.toString(building.getPoweredDownHeatRequired())
						)
					);
				}
			}
		}
		setRequiredHeat(tempHeatRequired);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"ThermalSystem.log.totalHeatRequired", //$NON-NLS-1$
					Double.toString(heatRequired)
				)
			);
		}
	}

	/**
	 * Updates the total heat storage capacity in the grid.
	 * @throws BuildingException if error determining total heat storage capacity.
	 */
	private void updateTotalThermalStorageCapacity() {
		double tempThermalStorageCapacity = 0D;
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iStore = manager.getBuildings(BuildingFunction.THERMAL_STORAGE).iterator();
		while (iStore.hasNext()) {
			Building building = iStore.next();
			ThermalStorage store = (ThermalStorage) building.getFunction(BuildingFunction.THERMAL_STORAGE);
			tempThermalStorageCapacity += store.getThermalStorageCapacity();
		}
		setStoredHeatCapacity(tempThermalStorageCapacity);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"ThermalSystem.log.totalThermalStorageCapacity", //$NON-NLS-1$
					Double.toString(thermalStorageCapacity)
				)
			);
		}
	}

	/**
	 * Checks if building generates more heat 
	 * than it uses in a given heat mode.
	 *
	 * @param building the building
	 * @param mode {@link HeatMode} the building's heat mode to check.
	 * @return true if building supplies more heat than it uses.
	 * throws BuildingException if error in heat generation.
	 */
	private boolean heatSurplus(Building building, HeatMode mode) {
		double generated = 0D;
		if (building.hasFunction(BuildingFunction.THERMAL_GENERATION)) {
			ThermalGeneration heatGeneration = 
					(ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
			generated = heatGeneration.getGeneratedHeat(); 
		}

		double used = 0D;
		if (mode == HeatMode.FULL_POWER) used = building.getFullHeatRequired();
		else if (mode == HeatMode.POWER_DOWN) used = building.getPoweredDownHeatRequired();

		return generated > used;
	}

	/**
	 * Stores any excess grid heat in heat storage buildings if possible.
	 * @param excessHeat excess grid heat (in kW hr).
	 * @throws BuildingException if error storing excess heat.
	 */
	private void storeExcessHeat(double excessHeat) {
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> i = manager.getBuildings(BuildingFunction.THERMAL_STORAGE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			ThermalStorage storage = (ThermalStorage) building.getFunction(BuildingFunction.THERMAL_STORAGE);
			double remainingCapacity = storage.getThermalStorageCapacity() - storage.getHeatStored();
			if (remainingCapacity > 0D) {
				double heatToStore = excessHeat;
				if (remainingCapacity < heatToStore) heatToStore = remainingCapacity;
				storage.setHeatStored(storage.getHeatStored() + heatToStore);
				excessHeat -= heatToStore;
			}
		}
	}

	/**
	 * Retrieves stored heat for the grid.
	 * @param neededHeat the heat needed (kW hr).
	 * @return stored heat retrieved (kW hr).
	 * @throws BuildingException if error retrieving heat.
	 */
	private double retrieveStoredHeat(double neededHeat) {
		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> i = manager.getBuildings(BuildingFunction.THERMAL_STORAGE).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			ThermalStorage storage = (ThermalStorage) building.getFunction(BuildingFunction.THERMAL_STORAGE);
			if ((storage.getHeatStored() > 0D) && (neededHeat > 0D)) {
				double retrievedHeat = neededHeat;
				if (storage.getHeatStored() < retrievedHeat) retrievedHeat = storage.getHeatStored();
				storage.setHeatStored(storage.getHeatStored() - retrievedHeat);
				neededHeat -= retrievedHeat;
			}
		}
		return neededHeat;
	}

	/**
	 * Gets the value of electrical heat at the settlement.
	 * @return value of heat (VP per kw h).
	 */
	public double getHeatValue() {
		return heatValue;
	}

	/**
	 * Determines the value of electrical heat at the settlement.
	 */
	private void determineHeatValue() {
		double demand = heatRequired;
		double supply = heatGenerated + (heatStored / 2D);

		double newHeatValue = demand / (supply + 1.0D);

		if (newHeatValue != heatValue) {
			heatValue = newHeatValue;
			settlement.fireUnitUpdate(UnitEventType.HEAT_VALUE_EVENT);
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		heatMode = null;
		settlement = null;
	}
}