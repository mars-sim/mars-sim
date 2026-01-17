/*
 * Mars Simulation Project
 * BuildingPanelPowerStorage.java
 * @date 2022-07-10
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.utility.power.PowerStorage;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The PowerStorageBuildingPanel class is a building function panel representing 
 * the power storage of a settlement building.
 */
@SuppressWarnings("serial")
class BuildingPanelPowerStorage extends EntityTabPanel<Building> implements TemporalComponent {

	private static final String ENERGY_ICON = "energy";
	
	private JDoubleLabel storedTF;
	private JDoubleLabel capTF;

	private PowerStorage storage;

	/**
	 * Constructor.
	 * 
	 * @param storage The power storage building function.
	 * @param context The UI context.
	 */
	public BuildingPanelPowerStorage(PowerStorage storage, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelPowerStorage.title"), 
			ImageLoader.getIconByName(ENERGY_ICON), null,
			context, storage.getBuilding() 
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
		var capacityCache = storage.getBattery().getEnergyStorageCapacity();
		capTF = new JDoubleLabel(StyleManager.DECIMAL_KWH, capacityCache);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelPowerStorage.cap"), capTF);
		
		// Create stored label.
		var storedCache = storage.getBattery().getCurrentStoredEnergy();
		storedTF = new JDoubleLabel(StyleManager.DECIMAL_KWH, storedCache);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelPowerStorage.stored"), storedTF);
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {
		capTF.setValue(storage.getBattery().getEnergyStorageCapacity());
		storedTF.setValue(storage.getBattery().getCurrentStoredEnergy());   
	}
}