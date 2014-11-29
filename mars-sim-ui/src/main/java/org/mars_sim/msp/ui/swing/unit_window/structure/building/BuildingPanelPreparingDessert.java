/**
 * Mars Simulation Project
 * BuildingPanelPreparingDessert.java
 * @version 3.07 2014-11-28
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.PreparingDessert;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.*;

import java.awt.*;

/**
 * This class is a building function panel representing 
 * the soy product prep of a settlement building.
 */
public class BuildingPanelPreparingDessert
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Domain members
	private PreparingDessert kitchen;
	/** The number of cooks label. */
	private JLabel numCooksLabel;
	/** The number of soymilk. */
	private JLabel servingsDessertLabel;
	/** The quality of the soymilk. */
	private JLabel dessertQualityLabel;

	// Cache
	private int numCooksCache;
	private int servingsDessertCache;
	private int dessertQualityCache;

	/**
	 * Constructor.
	 * @param kitchen the cooking building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPreparingDessert(PreparingDessert kitchen, MainDesktopPane desktop) {

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
		// 2014-11-21 Changed font type, size and color and label text
		JLabel makingSoyLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.title"), JLabel.CENTER); //$NON-NLS-1$
		makingSoyLabel.setFont(new Font("Serif", Font.BOLD, 16));
		makingSoyLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(makingSoyLabel);

		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.numberOfCooks", numCooksCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(numCooksLabel);

		// Prepare cook capacity label
		JLabel cookCapacityLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.cookCapacity", kitchen.getCookCapacity()), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(cookCapacityLabel);

		// Prepare meal number label
		servingsDessertCache = kitchen.getNumServingsFreshDessert();
		servingsDessertLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.servingsOfDessert", servingsDessertCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(servingsDessertLabel);

		// Prepare meal quality label
		dessertQualityCache = kitchen.getBestDessertQuality();
		dessertQualityLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.bestQualityOfDessert", dessertQualityCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(dessertQualityLabel);
	}	

	/**
	 * Update this panel
	 */
	public void update() {
		// Update cook number
		if (numCooksCache != kitchen.getNumCooks()) {
			numCooksCache = kitchen.getNumCooks();
			numCooksLabel.setText(Msg.getString("BuildingPanelPreparingDessert.numberOfCooks", numCooksCache)); //$NON-NLS-1$
		}

		// Update meal number
		if (servingsDessertCache != kitchen.getNumServingsFreshDessert()) {
			servingsDessertCache = kitchen.getNumServingsFreshDessert();
			servingsDessertLabel.setText(Msg.getString("BuildingPanelPreparingDessert.servingsOfDessert", servingsDessertCache)); //$NON-NLS-1$
		}

		// Update meal quality
		if (dessertQualityCache != kitchen.getBestDessertQuality()) {
			dessertQualityCache = kitchen.getBestDessertQuality();
			dessertQualityLabel.setText(Msg.getString("BuildingPanelPreparingDessert.bestQualityOfDessert", dessertQualityCache)); //$NON-NLS-1$
		}
	}
}