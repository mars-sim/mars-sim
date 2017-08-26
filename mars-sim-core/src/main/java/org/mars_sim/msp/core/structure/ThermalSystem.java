/**
 * Mars Simulation Project
 * ThermalSystem.java
 * @version 3.07 2015-02-18
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
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;

/**
 * The ThermalSystem class is the settlement's Thermal Control, Distribution and Storage Subsystem.
 * This class will only have one and only one instance
 */
public class ThermalSystem
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ThermalSystem.class.getName());

	DecimalFormat fmt = new DecimalFormat("#.####");

	// Data members
	private double powerGenerated;
	private double heatGenerated;
	private double heatGenerationCapacity;
	private double heatStored;
	private double thermalStorageCapacity;
	private double heatRequired;
	private double heatValue;

	private boolean sufficientHeat;

	private HeatMode heatMode;
	private HeatMode heatModeCache;

	private Settlement settlement;
	private PowerGrid powerGrid;
	private BuildingManager manager;
	
	//private ThermalGeneration heater;

	/**
	 * Constructor.
	 */
	public ThermalSystem(Settlement settlement) {

		this.settlement = settlement;
		this.manager = settlement.getBuildingManager();
		
		heatMode = HeatMode.QUARTER_HEAT;
		heatGenerated = 0D;
		heatStored = 0D;
		thermalStorageCapacity = 0D;
		heatRequired = 0D;
		sufficientHeat = true;

		powerGrid = settlement.getPowerGrid();
	}

	/**
	 * Gets the heat mode.
	 * @return heat mode
	 */
	public HeatMode getHeatMode() {
		return heatMode;
	}

	/**
	 * Sets the heat mode.
	 * @param newHeatMode the new heat mode.
	 */
	public void setHeatMode(HeatMode newHeatMode) {
		if (heatMode != newHeatMode) {
			//if (HeatMode.POWER_UP == newHeatMode) heatMode = HeatMode.POWER_UP;
			//else if (HeatMode.HALF_POWER == newHeatMode) heatMode = HeatMode.HALF_POWER;
			//else if (HeatMode.HEAT_OFF == newHeatMode) heatMode = HeatMode.HEAT_OFF;
			if (powerGrid == null)
				powerGrid = settlement.getPowerGrid();
			if (powerGrid.getPowerMode() == PowerMode.POWER_UP)
				heatMode = HeatMode.QUARTER_HEAT;
			//else if (HeatMode.HALF_POWER == newHeatMode) heatMode = HeatMode.HALF_POWER;
			else if (powerGrid.getPowerMode() == PowerMode.POWER_DOWN)
				heatMode = HeatMode.OFFLINE;
			settlement.fireUnitUpdate(UnitEventType.HEAT_MODE_EVENT);
		}
	}

	/**
	 * Gets the total max possible generated heat in the heating system.
	 * @return heat in kW
	 */
	public double getGeneratedCapacity() {
		//logger.info("getGeneratedCapacity() : heatGenerated is " + fmt.format(heatGenerationCapacity) );
		return heatGenerationCapacity;
	}

	/**
	 * Gets the total max possible generated heat in the heating system.
	 * @return heat in kW
	 */
	public double getGeneratedHeat() {
		//logger.info("getGeneratedHeat() : heatGenerated is " + fmt.format(heatGenerated) );
		return heatGenerated;
	}

	/**
	 * Gets the total max possible generated heat in the heating system.
	 * @return heat in kW
	 */
	public double getGeneratedPower() {
		return powerGenerated;
	}


	/**
	 * Sets the new amount of generated heat in the heating system.
	 * @param newGeneratedHeat the new generated heat kW
	 */
	private void setGeneratedHeat(double newGeneratedHeat) {
		if (heatGenerated != newGeneratedHeat) {
			heatGenerated = newGeneratedHeat;
			settlement.fireUnitUpdate(UnitEventType.GENERATED_HEAT_EVENT);
		}
	}

	/**
	 * Sets the new amount of generated power in the heating system.
	 * @param newGeneratedHeat the new generated power kW
	 */
	private void setGeneratedPower(double newGeneratedPower) {
		if (powerGenerated != newGeneratedPower) {
			powerGenerated = newGeneratedPower;
			settlement.fireUnitUpdate(UnitEventType.GENERATED_POWER_EVENT);
		}
	}

	/**
	 * Gets the stored heat in the heating system.
	 * @return stored heat in kW
	 */
	public double getStoredHeat() {
		return heatStored;
	}

	/**
	 * Sets the stored heat in the
	 * @param newHeatStored the new stored heat (kJ).
	 */
	public void setStoredHeat(double newHeatStored) {
		if (heatStored != newHeatStored) {
			heatStored = newHeatStored;
			settlement.fireUnitUpdate(UnitEventType.STORED_HEAT_EVENT);
		}
	}

	/**
	 * Gets the stored thermal capacity in the heating system.
	 * @return stored thermal capacity in kJ.
	 */
	public double getStoredHeatCapacity() {
		return thermalStorageCapacity;
	}

	/**
	 * Sets the stored thermal capacity in the heating system.
	 * @param newThermalStorageCapacity the new stored thermal capacity (kJ).
	 */
	public void setStoredHeatCapacity(double newThermalStorageCapacity) {
		if (thermalStorageCapacity != newThermalStorageCapacity) {
			thermalStorageCapacity = newThermalStorageCapacity;
			settlement.fireUnitUpdate(UnitEventType.STORED_HEAT_CAPACITY_EVENT);
		}
	}

	/**
	 * Gets the heat required from the heating system.
	 * @return heat in kJ/s
	 */
	// NOT USED FOR THE TIME BEING. always return ZERO
	public double getRequiredHeat() {
		return heatRequired;
	}

	/**
	 * Sets the required heat in the heating system.
	 * @param newRequiredHeat the new required heat (kJ/s).
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
	 * Time passing for heating system.
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

		// update the total heat generated in the heating system.
		updateTotalHeatGenerated();

		updateTotalPowerGenerated();

		// Update the total heat stored in the heating system.
		//updateTotalStoredHeat();

		// Update the total heat storage capacity in the heating system.
		//updateTotalThermalStorageCapacity();

		// Determine total heat required in the heating system.
		updateTotalRequiredHeat();
/*
		// Check if there is enough heat generated to fully supply each building.
		if (heatRequired <= heatGenerated) {
			sufficientHeat = true;

			// Store excess heat in heat storage buildings.
			//double timeHr = MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
			//double excessHeat = (heatGenerated - heatRequired) * timeHr;
			//storeExcessHeat(excessHeat);
		}
		else {
			sufficientHeat = false;

			// TODO: the neededHeat assumes the heat is transmitted across the settlement. Check if this is compatible with
			// heating calculation currently set for each individual building.
			double neededHeat = heatRequired - heatGenerated;

			// Retrieve heat from heat storage buildings.
			double timeHr = MarsClock.convertMillisolsToSeconds(time) / 60D / 60D;
			double neededHeatHr = neededHeat * timeHr;
			//neededHeatHr = retrieveStoredHeat(neededHeatHr);
			neededHeat = neededHeatHr / timeHr;

			BuildingManager manager = settlement.getBuildingManager();
			List<Building> buildings = manager.getBuildings();

			// Reduce each building's heat mode to low heat until
			// required heat reduction is met.
			if (heatMode != HeatMode.HEAT_OFF) {
				Iterator<Building> iLowHeat = buildings.iterator();
				while (iLowHeat.hasNext() && (neededHeat > 0D)) {
					Building building = iLowHeat.next();
					if (!heatSurplus(building, HeatMode.ONLINE)) {
						building.setHeatMode(HeatMode.HEAT_OFF);
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
					if (!heatSurplus(building, HeatMode.HEAT_OFF) &&
							!(building.hasFunction(BuildingFunction.LIFE_SUPPORT))) {
						building.setHeatMode(HeatMode.OFFLINE);
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
					if (!heatSurplus(building, HeatMode.HEAT_OFF) &&
							building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
						building.setHeatMode(HeatMode.OFFLINE);
						neededHeat -= building.getPoweredDownHeatRequired();
					}
				}
			}
		}

*/

		// Update heat value.
		determineHeatValue();

	}

	/**
	 * Updates the total heat generated in the heating system.
	 * @throws BuildingException if error determining total heat generated.
	 */
	private void updateTotalHeatGenerated() {
		double tempHeatGenerated = 0D;

		// Add the heat generated by all heat generation buildings.
		//BuildingManager manager = settlement.getBuildingManager();
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
	 * Updates the total power generated by the solar heat engine system.
	 * @throws BuildingException if error determining total heat generated.
	 */
	private void updateTotalPowerGenerated() {
		double power = 0D;

		// Add the heat generated by all heat generation buildings.
		//BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> iHeat = manager.getBuildings(BuildingFunction.THERMAL_GENERATION).iterator();
		while (iHeat.hasNext()) {
			Building building = iHeat.next();
			ThermalGeneration gen = (ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
			power += gen.getGeneratedPower();
			//System.out.println(building.getNickName() + "'s power : " + power);
			// logger.info(((Building) gen).getName() + " generated: " + gen.getGeneratedHeat());
		}
		setGeneratedPower(power);
		//System.out.println("total power : " + power);

		if(logger.isLoggable(Level.FINE)) {
			logger.fine(
				Msg.getString(
					"ThermalSystem.log.totalPowerGenerated", //$NON-NLS-1$
					Double.toString(power)
				)
			);
		}
	}

	/**
	 * Updates the total heat stored in the heating system.
	 * @throws BuildingException if error determining total heat stored.

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
 */
	/**
	 * Updates the total heat required in the heating system.
	 * @throws BuildingException if error determining total heat required.
	 */
	private void updateTotalRequiredHeat() {
		double tempHeatRequired = 0D;
		//boolean heatUp = heatMode == HeatMode.POWER_UP;
		//BuildingManager manager = settlement.getBuildingManager();
		List<Building> buildings = manager.getBuildings();
		Iterator<Building> iUsed = buildings.iterator();
		while (iUsed.hasNext()) {
			Building building = iUsed.next();
			if (heatMode == HeatMode.FULL_HEAT) {
				tempHeatRequired += building.getFullHeatRequired();
				if(logger.isLoggable(Level.FINE)) {
					logger.fine(
						Msg.getString(
							"ThermalSystem.log.buildingFullHeatUsed", //$NON-NLS-1$
							building.getBuildingType(),
							Double.toString(building.getFullHeatRequired())
						)
					);
				}
			}
			else if (heatMode == HeatMode.HALF_HEAT) {
				tempHeatRequired = tempHeatRequired + building.getFullHeatRequired()/2D;
				if(logger.isLoggable(Level.FINE)) {
					logger.fine(
						Msg.getString(
							"ThermalSystem.log.buildingHalfHeatUsed", //$NON-NLS-1$
							building.getBuildingType(),
							Double.toString(building.getFullHeatRequired()/2D)
						)
					);
				}
			}
			else if (heatMode == HeatMode.QUARTER_HEAT) {
				tempHeatRequired += building.getFullHeatRequired()/4D;
				if(logger.isLoggable(Level.FINE)) {
					logger.fine(
						Msg.getString(
							"ThermalSystem.log.buildingQuarterHeatUsed", //$NON-NLS-1$
							building.getBuildingType(),
							Double.toString(building.getFullHeatRequired()/4D)
						)
					);
				}
			}
			else {
				heatMode = HeatMode.HEAT_OFF;
				//logger.info("setHeatMode() : heatMode was " + heatModeCache);
				if ( heatModeCache != heatMode) {
					// if heatModeCache is different from the its last value
					heatModeCache = heatMode;
					building.setHeatMode(heatMode);
					//logger.info("setHeatMode() : heatMode is now " + heatMode);
				}

				tempHeatRequired += building.getPoweredDownHeatRequired();

				if(logger.isLoggable(Level.FINE)) {
					logger.fine(
						Msg.getString(
							"ThermalSystem.log.buildingHeatDownHeatUsed", //$NON-NLS-1$
							building.getBuildingType(),
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
	 * Updates the total heat storage capacity in the heating system.
	 * @throws BuildingException if error determining total thermal storage capacity.

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
*/
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
		if (mode == HeatMode.FULL_HEAT) used = building.getFullHeatRequired();
		if (mode == HeatMode.HALF_HEAT) used = building.getFullHeatRequired()/2D;
		if (mode == HeatMode.QUARTER_HEAT) used = building.getFullHeatRequired()/4D;
		else if (mode == HeatMode.HEAT_OFF) used = building.getPoweredDownHeatRequired();

		return generated > used;
	}

	/**
	 * Stores any excess heat in heat storage buildings if possible.
	 * @param excessHeat excess heat (in kJ/s).
	 * @throws BuildingException if error storing excess heat.

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
*/
	/**
	 * Retrieves stored heat for the heating system..
	 * @param neededHeat the heat needed (kJ/s).
	 * @return stored heat retrieved (kJ/s).
	 * @throws BuildingException if error retrieving heat.

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
 */
	/**
	 * Gets the value of heat at the settlement.
	 * @return value of heat (VP per kJ/s).
	public double getHeatValue() {
		return heatValue;
	}

	/**
	 * Determines the value of heat energy at the settlement.
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