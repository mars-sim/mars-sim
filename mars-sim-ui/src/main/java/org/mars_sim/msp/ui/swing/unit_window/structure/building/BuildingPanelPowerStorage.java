/*
 * Mars Simulation Project
 * BuildingPanelPowerStorage.java
 * @date 2021-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

import com.alee.laf.panel.WebPanel;

/**
 * The PowerStorageBuildingPanel class is a building function panel representing 
 * the power storage of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelPowerStorage
extends BuildingFunctionPanel {

	private static final String kWh = " kWh";
	
	private JTextField storedTF;
	private JTextField capTF;

	private double capacityCache;
	private double storedCache;
	
	private PowerStorage storage;
	
	private DecimalFormat formatter = new DecimalFormat("0.0");

	
	/**
	 * Constructor.
	 * @param storage The power storage building function.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPowerStorage(PowerStorage storage, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(Msg.getString("BuildingPanelPowerStorage.title"), storage.getBuilding(), desktop);

		this.storage = storage;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		WebPanel springPanel = new WebPanel(new SpringLayout());
		center.add(springPanel, BorderLayout.NORTH);
		
		// Create capacity label.
		capacityCache = Math.round(storage.getCurrentMaxCapacity() *10.0)/10.0;;
		capTF = addTextField(springPanel, Msg.getString("BuildingPanelPowerStorage.cap"),
							 formatter.format(capacityCache) + kWh, null);
		
		// Create stored label.
		storedCache = Math.round(storage.getkWattHourStored() *10.0)/10.0;;
		storedTF = addTextField(springPanel, Msg.getString("BuildingPanelPowerStorage.stored"),
								storedCache + kWh, null);
		
		SpringUtilities.makeCompactGrid(springPanel,
                2, 2, 			//rows, cols
                75, 10,        //initX, initY
                3, 1);       //xPad, yPad
	}

	@Override
	public void update() {

		// Update capacity label if necessary.
		double newCapacity = Math.round(storage.getCurrentMaxCapacity() *10.0)/10.0;
		if (capacityCache != newCapacity) {
			capacityCache = newCapacity;
			capTF.setText(capacityCache + kWh);
		}

		// Update stored label if necessary.
		double newStored = Math.round(storage.getkWattHourStored() *10.0)/10.0;
		if (storedCache != newStored) {
			storedCache = newStored;
			storedTF.setText(storedCache + kWh);
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
		formatter = null;
	}
}
