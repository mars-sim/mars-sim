/*
 * Mars Simulation Project
 * BuildingPanelFishery.java
 * @date 2022-07-10
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.farming.Fishery;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

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
	private JTextField numFishLabel;
	private JTextField weedLabel;
	
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
		JPanel labelPanel = new JPanel(new SpringLayout());
		center.add(labelPanel, BorderLayout.NORTH);
		
		addTextField(labelPanel, Msg.getString("BuildingPanelFishery.tankSize"), tank.getTankSize(), null);
		
		numFish = tank.getNumFish();
		numFishLabel = addTextField(labelPanel, Msg.getString("BuildingPanelFishery.numFish"),
									numFish, null);
	
				
		weedMass = tank.getWeedMass();	
		weedLabel = addTextField(labelPanel, Msg.getString("BuildingPanelFishery.weedMass"),
								 StyleManager.DECIMAL_PLACES1.format(weedMass), null);
		
		SpringUtilities.makeCompactGrid(labelPanel,
                3, 2, //rows, cols
                INITX_DEFAULT, INITY_DEFAULT,        //initX, initY
                XPAD_DEFAULT, YPAD_DEFAULT);       //xPad, yPad
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