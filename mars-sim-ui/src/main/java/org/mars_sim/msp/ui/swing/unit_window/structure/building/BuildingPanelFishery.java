/*
 * Mars Simulation Project
 * BuildingPanelFishery.java
 * @date 2021-10-07
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.farming.Fishery;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The BuildingPanelFishery class is a building function panel for
 * the fish farm of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelFishery
extends BuildingFunctionPanel {

	/** Decimal formatter. */
	private DecimalFormat formatter = new DecimalFormat(Msg.getString("BuildingPanelFishery.decimalFormat")); //$NON-NLS-1$

	// Caches
	private int numFish;
	private double weedMass;
	
	private Fishery tank;
	private JTextField numFishLabel;
	private JTextField weedLabel;
	
	/**
	 * Constructor.
	 * @param The panel for the Fishery
	 * @param The main desktop
	 */
	public BuildingPanelFishery(Fishery tank, MainDesktopPane desktop) {
		super(Msg.getString("BuildingPanelFishery.title"), tank.getBuilding(), desktop);
		
		this.tank = tank;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
		JPanel labelPanel = new JPanel(new GridLayout(3, 2, 3, 1));
		center.add(labelPanel, BorderLayout.NORTH);
		
		addTextField(labelPanel, Msg.getString("BuildingPanelFishery.tankSize"), tank.getTankSize(), null);
		
		numFish = tank.getNumFish();
		numFishLabel = addTextField(labelPanel, Msg.getString("BuildingPanelFishery.numFish"),
									numFish, null);
	
				
		weedMass = tank.getWeedMass();	
		weedLabel = addTextField(labelPanel, Msg.getString("BuildingPanelFishery.weedMass"),
								 formatter.format(weedMass), null);
	}

	/**
	 * Update this panel with latest values
	 */
	@Override
	public void update() {	

		if (numFish != tank.getNumFish()) {
			numFish = tank.getNumFish();
			numFishLabel.setText(Integer.toString(numFish));
		}

		double newWeedMass = tank.getWeedMass();
		if (weedMass != newWeedMass) {
			weedMass = newWeedMass;
			weedLabel.setText(formatter.format(weedMass));
		}
	}
}