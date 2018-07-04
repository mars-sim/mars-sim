/**
 * Mars Simulation Project
 * BuildingPanelPowerStorage.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.SpringLayout;

/**
 * The PowerStorageBuildingPanel class is a building function panel representing 
 * the power storage of a settlement building.
 */
public class BuildingPanelPowerStorage
extends BuildingFunctionPanel {

	private static final String kWh = " kWh";
	
	// Data members
	private WebLabel storedLabel;
	private WebLabel capacityLabel;
	
	private WebTextField storedTF;
	private WebTextField capTF;

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
		super(storage.getBuilding(), desktop);

		this.storage = storage;

		// Set the layout
		setLayout(new BorderLayout());

		// 2014-11-21 Changed font type, size and color and label text
		WebLabel titleLabel = new WebLabel(
				Msg.getString("BuildingPanelPowerStorage.title"), //$NON-NLS-1$
				WebLabel.CENTER);		
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		add(titleLabel, BorderLayout.NORTH);
		
		WebPanel springPanel = new WebPanel(new SpringLayout());
		add(springPanel, BorderLayout.CENTER);
		
		// Create capacity label.
		capacityCache = Math.round(storage.getCurrentMaxCapacity() *10.0)/10.0;;
		capacityLabel = new WebLabel(
				Msg.getString("BuildingPanelPowerStorage.cap"), //$NON-NLS-1$
				WebLabel.CENTER);		
		springPanel.add(capacityLabel);
		
		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		capTF = new WebTextField(formatter.format(capacityCache) + kWh);
		capTF.setEditable(false);
		capTF.setColumns(7);
		capTF.setPreferredSize(new Dimension(120, 25));
		wrapper1.add(capTF);
		springPanel.add(wrapper1);
		
		// Create stored label.
		storedCache = Math.round(storage.getkWattHourStored() *10.0)/10.0;;
		storedLabel = new WebLabel(
				Msg.getString("BuildingPanelPowerStorage.stored"), //$NON-NLS-1$
				WebLabel.CENTER);
		springPanel.add(storedLabel);
		
		WebPanel wrapper2 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		storedTF = new WebTextField(storedCache + kWh);
		storedTF.setEditable(false);
		storedTF.setColumns(7);
		storedTF.setPreferredSize(new Dimension(120, 25));
		wrapper2.add(storedTF);
		springPanel.add(wrapper2);
		
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
	public void destroy() {
		// take care to avoid null exceptions
		storedLabel = null;
		capacityLabel = null;
		storedTF = null;
		capTF = null;
		storage = null;
		formatter = null;
	}
}