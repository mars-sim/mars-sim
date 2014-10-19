/**
 * Mars Simulation Project
 * BuildingPanelThermalStorage.java
 * @version 3.07 2014-10-17
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.structure.building.function.ThermalStorage;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * The BuildingPanelThermalStorage class is a building function panel representing 
 * the heat storage of a settlement building.
 */
public class BuildingPanelThermalStorage
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private ThermalStorage storage;
	private JLabel capacityLabel;
	private double capacityCache;
	private JLabel storedLabel;
	private double storedCache;

	/**
	 * Constructor.
	 * @param storage The power storage building function.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelThermalStorage(ThermalStorage storage, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(storage.getBuilding(), desktop);

		this.storage = storage;

		// Set the layout
		setLayout(new GridLayout(2, 1, 0, 0));

		DecimalFormat formatter = new DecimalFormat("0.0");

		// Create capacity label.
		capacityCache = storage.getThermalStorageCapacity();
		capacityLabel = new JLabel("Thermal Capacity: " + formatter.format(capacityCache) + 
				" kJ", JLabel.CENTER);
		add(capacityLabel);

		// Create stored label.
		storedCache = storage.getHeatStored();
		storedLabel = new JLabel("Energy Stored: " + formatter.format(storedCache) + 
				" kJ", JLabel.CENTER);
		add(storedLabel);
	}

	@Override
	public void update() {

		DecimalFormat formatter = new DecimalFormat("0.0");

		// Update capacity label if necessary.
		double newCapacity = storage.getThermalStorageCapacity();
		if (capacityCache != newCapacity) {
			capacityCache = newCapacity;
			capacityLabel.setText("Thermal Capacity: " + formatter.format(capacityCache) + 
					" kJ");
		}

		// Update stored label if necessary.
		double newStored = storage.getHeatStored();
		if (storedCache != newStored) {
			storedCache = newStored;
			storedLabel.setText("Energy Stored: " + formatter.format(storedCache) + " kJ");
		}    
	}
}