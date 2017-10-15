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

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * The PowerStorageBuildingPanel class is a building function panel representing 
 * the power storage of a settlement building.
 */
public class BuildingPanelPowerStorage
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String kWh = " kWh";
	
	// Data members
	private JLabel storedLabel;
	private JLabel capacityLabel;
	
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
		super(storage.getBuilding(), desktop);

		this.storage = storage;

		// Set the layout
		setLayout(new BorderLayout());

		// 2014-11-21 Changed font type, size and color and label text
		JLabel titleLabel = new JLabel(
				Msg.getString("BuildingPanelPowerStorage.title"), //$NON-NLS-1$
				JLabel.CENTER);		
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		add(titleLabel, BorderLayout.NORTH);
		
		JPanel springPanel = new JPanel(new SpringLayout());
		add(springPanel, BorderLayout.CENTER);
		
		// Create capacity label.
		capacityCache = storage.getCurrentMaxCapacity();
		capacityLabel = new JLabel(
				Msg.getString("BuildingPanelPowerStorage.cap"), //$NON-NLS-1$
				JLabel.CENTER);		
		springPanel.add(capacityLabel);
		
		JPanel wrapper1 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		storedTF = new JTextField(formatter.format(capacityCache) + kWh);
		storedTF.setEditable(false);
		storedTF.setColumns(7);
		storedTF.setPreferredSize(new Dimension(120, 25));
		wrapper1.add(storedTF);
		springPanel.add(wrapper1);
		
		// Create stored label.
		storedCache = storage.getkWattHourStored();
		storedLabel = new JLabel(
				Msg.getString("BuildingPanelPowerStorage.stored"), //$NON-NLS-1$
				JLabel.CENTER);
		springPanel.add(storedLabel);
		
		JPanel wrapper2 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		storedTF = new JTextField(formatter.format(storedCache) + kWh);
		storedTF.setEditable(false);
		storedTF.setColumns(7);
		storedTF.setPreferredSize(new Dimension(120, 25));
		wrapper2.add(storedTF);
		springPanel.add(wrapper2);
		
		SpringUtilities.makeCompactGrid(springPanel,
                2, 2, 			//rows, cols
                75, 25,        //initX, initY
                3, 1);       //xPad, yPad
	}

	@Override
	public void update() {

		// Update capacity label if necessary.
		double newCapacity = storage.getCurrentMaxCapacity();
		if (capacityCache != newCapacity) {
			capacityCache = newCapacity;
			capTF.setText(formatter.format(capacityCache) + kWh);
		}

		// Update stored label if necessary.
		double newStored = storage.getkWattHourStored();
		if (storedCache != newStored) {
			storedCache = newStored;
			storedTF.setText(formatter.format(storedCache) + kWh);
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