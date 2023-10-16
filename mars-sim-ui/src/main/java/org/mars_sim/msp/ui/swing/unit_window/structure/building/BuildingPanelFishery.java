/*
 * Mars Simulation Project
 * BuildingPanelFishery.java
 * @date 2023-09-19
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.structure.building.function.farming.Fishery;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;
import org.mars_sim.tools.Msg;

/**
 * The BuildingPanelFishery class is a building function panel for
 * the fish farm of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelFishery extends BuildingFunctionPanel {

	private static final String FISH_ICON = "fish";

	// Caches
	private int numFish;
	private int numIdealFish; 
	private int maxFish;

	private double weedMass;
	private double weedDemand;
	
	private double powerReq;
	
	private JLabel numFishLabel;
	private JLabel numIdealFishLabel;
	private JLabel maxFishLabel;
	
	private JLabel weedMassLabel;
	private JLabel weedDemandLabel;

	private JLabel powerReqLabel;

	private Fishery tank;

	
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
		AttributePanel labelPanel = new AttributePanel(7);
		center.add(labelPanel, BorderLayout.NORTH);
		
		labelPanel.addTextField(Msg.getString("BuildingPanelFishery.tankSize"), Integer.toString(tank.getTankSize()), null);
		
		numFish = tank.getNumFish();
		numFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numFish"),
									Integer.toString(numFish), null);
				
		numIdealFish = tank.getIdealFish();
		numIdealFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.numIdealFish"),
									Integer.toString(numIdealFish), null);
		
		maxFish = tank.getMaxFish();
		maxFishLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.maxFish"),
									Integer.toString(maxFish), null);

		weedMass = tank.getWeedMass();	
		weedMassLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.weedMass"),
								 StyleManager.DECIMAL_KG.format(weedMass), null);
		
		weedDemand = tank.getWeedDemand();	
		weedDemandLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.weedDemand"),
								 StyleManager.DECIMAL_PLACES2.format(weedDemand), null);
		
		powerReq = tank.getFullPowerRequired();	
		powerReqLabel = labelPanel.addTextField(Msg.getString("BuildingPanelFishery.powerReq"),
								 StyleManager.DECIMAL_KW.format(powerReq), null);
	}

	/**
	 * Updates this panel with latest values.
	 */
	@Override
	public void update() {	

		int newNumFish = tank.getNumFish();
		if (numFish != newNumFish) {
			numFish = newNumFish;
			numFishLabel.setText(Integer.toString(newNumFish));
		}
		
		int newIdealFish = tank.getIdealFish();
		if (numIdealFish != newIdealFish) {
			numIdealFish = newIdealFish;
			numIdealFishLabel.setText(Integer.toString(newIdealFish));
		}
		
		int newMaxFish = tank.getMaxFish();
		if (maxFish != newMaxFish) {
			maxFish = newMaxFish;
			maxFishLabel.setText(Integer.toString(newMaxFish));
		}

		double newWeedMass = tank.getWeedMass();
		if (weedMass != newWeedMass) {
			weedMass = newWeedMass;
			weedMassLabel.setText(StyleManager.DECIMAL_KG.format(newWeedMass));
		}
		
		double newWeedDemand = tank.getWeedDemand();
		if (weedDemand != newWeedDemand) {
			weedDemand = newWeedDemand;
			weedDemandLabel.setText(StyleManager.DECIMAL_PLACES1.format(newWeedDemand));
		}
		
		double newPowerReq = tank.getFullPowerRequired();	
		if (powerReq != newPowerReq) {
			powerReq = newPowerReq;
			powerReqLabel.setText(StyleManager.DECIMAL_KW.format(newPowerReq));
		}
	}
}