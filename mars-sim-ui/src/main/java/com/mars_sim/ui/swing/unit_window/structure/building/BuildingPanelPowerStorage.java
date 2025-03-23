/*
 * Mars Simulation Project
 * BuildingPanelPowerStorage.java
 * @date 2022-07-10
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.utility.power.PowerStorage;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The PowerStorageBuildingPanel class is a building function panel representing 
 * the power storage of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelPowerStorage
extends BuildingFunctionPanel {

	private static final String ENERGY_ICON = "energy";
	
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	private double capacityCache;
	private double storedCache;
	
	private JLabel storedTF;
	private JLabel capTF;

	private PowerStorage storage;

	/**
	 * Constructor.
	 * 
	 * @param storage The power storage building function.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPowerStorage(PowerStorage storage, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelPowerStorage.title"), 
			ImageLoader.getIconByName(ENERGY_ICON), 
			storage.getBuilding(), 
			desktop
		);

		this.storage = storage;
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {

		AttributePanel springPanel = new AttributePanel(2);
		center.add(springPanel, BorderLayout.NORTH);
		
		// Create capacity label.
		capacityCache = storage.getCurrentMaxCapacity();
		capTF = springPanel.addTextField(Msg.getString("BuildingPanelPowerStorage.cap"),
							 StyleManager.DECIMAL_KWH.format(capacityCache), null);
		
		// Create stored label.
		storedCache = storage.getkWattHourStored();
		storedTF = springPanel.addTextField(Msg.getString("BuildingPanelPowerStorage.stored"),
									StyleManager.DECIMAL_KWH.format(storedCache), null);
	}

	@Override
	public void update() {	
		if (!uiDone)
			initializeUI();
		

		// Update capacity label if necessary.
		double newCapacity = storage.getCurrentMaxCapacity();
		if (capacityCache != newCapacity) {
			capacityCache = newCapacity;
			capTF.setText(StyleManager.DECIMAL_KWH.format(capacityCache));
		}

		// Update stored label if necessary.
		double newStored = storage.getkWattHourStored();
		if (storedCache != newStored) {
			storedCache = newStored;
			storedTF.setText(StyleManager.DECIMAL_KWH.format(storedCache));
		}    
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		// take care to avoid null exceptions
		storedTF = null;
		capTF = null;
		storage = null;
	}
}
