/**
 * Mars Simulation Project
 * BuildingPanelFishery.java
 * @version 3.1.2 2020-09-02
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.farming.Fishery;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.label.WebLabel;

/**
 * The BuildingPanelThermal class is a building function panel representing 
 * the heat production of a settlement building.
 */
//TODO: list individual power source in building tab and the power generated from that source
public class BuildingPanelFishery
extends BuildingFunctionPanel {

	/** Decimal formatter. */
	private DecimalFormat formatter = new DecimalFormat(Msg.getString("BuildingPanelFishery.decimalFormat")); //$NON-NLS-1$

	// Caches
	private int numFish;
	private double weedMass;
	
	private Fishery tank;
	private WebLabel numFishLabel;
	private WebLabel weedLabel;
	
	/**
	 * Constructor.
	 * @param The panel for the Fishery
	 * @param The main desktop
	 */
	public BuildingPanelFishery(Fishery tank, MainDesktopPane desktop) {
		super(tank.getBuilding(), desktop);

		
		this.tank = tank;
		this.building = tank.getBuilding();
	
		setLayout(new GridLayout(4, 1, 0, 0));
			
		WebLabel titleLabel = new WebLabel(
					Msg.getString("BuildingPanelFishery.title"), //$NON-NLS-1$
					WebLabel.CENTER);		
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		add(titleLabel);

		WebLabel sizeLabel = new WebLabel(
				Msg.getString("BuildingPanelFishery.tankSize", tank.getTankSize()), //$NON-NLS-1$
				WebLabel.CENTER
			);
		add(sizeLabel);	
		
		numFish = tank.getNumFish();
		numFishLabel = new WebLabel(
				Msg.getString("BuildingPanelFishery.numFish", numFish), //$NON-NLS-1$
				WebLabel.CENTER
			);
		add(numFishLabel);	
				
		weedMass = tank.getWeedMass();	
		weedLabel = new WebLabel(	
				Msg.getString("BuildingPanelFishery.weedMass", formatter.format(weedMass)), //$NON-NLS-1$
				WebLabel.CENTER
			);
		add(weedLabel);
	}

	/**
	 * Update this panel with latest values
	 */
	public void update() {	

		if (numFish != tank.getNumFish()) {
			numFish = tank.getNumFish();
			numFishLabel.setText(Msg.getString("BuildingPanelFishery.numFish", numFish)); //$NON-NLS-1$
		}

		double newWeedMass = tank.getWeedMass();
		if (weedMass != newWeedMass) {
			weedMass = newWeedMass;
			weedLabel.setText(Msg.getString("BuildingPanelFishery.weedMass", formatter.format(weedMass))); //$NON-NLS-1$
		}
	}
}
