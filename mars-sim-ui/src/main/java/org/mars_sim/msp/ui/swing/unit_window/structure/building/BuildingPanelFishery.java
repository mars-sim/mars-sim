/*
 * Mars Simulation Project
 * BuildingPanelFishery.java
 * @date 2022-07-10
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.farming.Fishery;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The BuildingPanelFishery class is a building function panel for
 * the fish farm of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelFishery extends BuildingFunctionPanel {

	private static final String FISH_ICON = "fish";

	// Caches
	private int numFish;
	private double weedMass;
	
	private Fishery tank;
	private JLabel numFishLabel;
	private JLabel weedLabel;
	
	/**
	 * Constructor.
	 * 
	 * @param The panel for the Fishery
	 * @param The main desktop
	 */
	public BuildingPanelFishery(Fishery tank, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelFishery.title"), 
			ImageLoader.getIconByName(FISH_ICON), 
			tank.getBuilding(), 
			desktop
		);
		
		this.tank = tank;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
		AttributePanel labelPanel = new AttributePanel(3);
		center.add(labelPanel, BorderLayout.NORTH);
		
		labelPanel.addTextField(Msg.getString("BuildingPanelFishery.tankSize"), Integer.toString(tank.getTankSize()), null);
		
		numFish = tank.getNumFish();
		numFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numFish"),
									Integer.toString(numFish), null);
	
				
		weedMass = tank.getWeedMass();	
		weedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.weedMass"),
								 StyleManager.DECIMAL_KG.format(weedMass), null);
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
			weedLabel.setText(StyleManager.DECIMAL_PLACES1.format(weedMass));
		}
	}
}