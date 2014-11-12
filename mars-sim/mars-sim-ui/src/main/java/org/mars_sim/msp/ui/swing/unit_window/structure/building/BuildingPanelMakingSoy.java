/**
 * Mars Simulation Project
 * BuildingPanelMakingSoy.java
 * @version 3.07 2014-11-11
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.MakingSoy;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.*;

import java.awt.*;

/**
 * This class is a building function panel representing 
 * the soy product prep of a settlement building.
 */
public class BuildingPanelMakingSoy
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Domain members
	private MakingSoy kitchen;
	/** The number of cooks label. */
	private JLabel numCooksLabel;
	/** The number of soymilk. */
	private JLabel servingsSoymilkLabel;
	/** The quality of the soymilk. */
	private JLabel soyQualityLabel;

	// Cache
	private int numCooksCache;
	private int servingsSoymilkCache;
	private int soyQualityCache;

	/**
	 * Constructor.
	 * @param kitchen the cooking building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelMakingSoy(MakingSoy kitchen, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(kitchen.getBuilding(), desktop);

		// Initialize data members
		this.kitchen = kitchen;

		// Set panel layout
		setLayout(new BorderLayout());

		// Prepare label panel
		JPanel labelPanel = new JPanel(new GridLayout(5, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);

		// Prepare cooking label
		JLabel cookingLabel = new JLabel(Msg.getString("BuildingPanelMakingSoy.makingSoy"), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(cookingLabel);

		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = new JLabel(Msg.getString("BuildingPanelMakingSoy.numberOfCooks", numCooksCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(numCooksLabel);

		// Prepare cook capacity label
		JLabel cookCapacityLabel = new JLabel(Msg.getString("BuildingPanelMakingSoy.cookCapacity", kitchen.getCookCapacity()), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(cookCapacityLabel);

		// Prepare meal number label
		servingsSoymilkCache = kitchen.getNumServingsFreshSoymilk();
		servingsSoymilkLabel = new JLabel(Msg.getString("BuildingPanelMakingSoy.servingsOfSoymilk", servingsSoymilkCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(servingsSoymilkLabel);

		// Prepare meal quality label
		soyQualityCache = kitchen.getBestSoymilkQuality();
		soyQualityLabel = new JLabel(Msg.getString("BuildingPanelMakingSoy.qualityOfSoymilk", soyQualityCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(soyQualityLabel);
	}	

	/**
	 * Update this panel
	 */
	public void update() {
		// Update cook number
		if (numCooksCache != kitchen.getNumCooks()) {
			numCooksCache = kitchen.getNumCooks();
			numCooksLabel.setText(Msg.getString("BuildingPanelMakingSoy.numberOfCooks", numCooksCache)); //$NON-NLS-1$
		}

		// Update meal number
		if (servingsSoymilkCache != kitchen.getNumServingsFreshSoymilk()) {
			servingsSoymilkCache = kitchen.getNumServingsFreshSoymilk();
			servingsSoymilkLabel.setText(Msg.getString("BuildingPanelMakingSoy.servingsOfSoymilk", servingsSoymilkCache)); //$NON-NLS-1$
		}

		// Update meal quality
		if (soyQualityCache != kitchen.getBestSoymilkQuality()) {
			soyQualityCache = kitchen.getBestSoymilkQuality();
			soyQualityLabel.setText(Msg.getString("BuildingPanelMakingSoy.qualityOfSoymilk", soyQualityCache)); //$NON-NLS-1$
		}
	}
}