/*
 * Mars Simulation Project
 * BuildingPanelPowerStorage.java
 * @date 2022-07-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

/**
 * The PowerStorageBuildingPanel class is a building function panel representing 
 * the power storage of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelPowerStorage
extends BuildingFunctionPanel {

	private static final String ENERGY_ICON = Msg.getString("icon.energy"); //$NON-NLS-1$
	
	private JTextField storedTF;
	private JTextField capTF;

	private double capacityCache;
	private double storedCache;
	
	private PowerStorage storage;

	/**
	 * Constructor.
	 * @param storage The power storage building function.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPowerStorage(PowerStorage storage, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelPowerStorage.title"), 
			ImageLoader.getNewIcon(ENERGY_ICON), 
			storage.getBuilding(), 
			desktop
		);

		this.storage = storage;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		JPanel springPanel = new JPanel(new SpringLayout());
		center.add(springPanel, BorderLayout.NORTH);
		
		// Create capacity label.
		capacityCache = storage.getCurrentMaxCapacity();
		capTF = addTextField(springPanel, Msg.getString("BuildingPanelPowerStorage.cap"),
							 StyleManager.DECIMAL_KWH.format(capacityCache), null);
		
		// Create stored label.
		storedCache = storage.getkWattHourStored();
		storedTF = addTextField(springPanel, Msg.getString("BuildingPanelPowerStorage.stored"),
									StyleManager.DECIMAL_KWH.format(storedCache), null);
		
		SpringUtilities.makeCompactGrid(springPanel,
                2, 2, 			//rows, cols
                INITX_DEFAULT, INITY_DEFAULT,        //initX, initY
                XPAD_DEFAULT, YPAD_DEFAULT);       //xPad, yPad
	}

	@Override
	public void update() {

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
	 * Prepare object for garbage collection.
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
