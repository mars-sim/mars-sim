/**
 * Mars Simulation Project
 * BuildingPanelCooking.java
 * @version 3.07 2014-11-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.Cooking;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.*;

import java.awt.*;

/**
 * This class is a building function panel representing 
 * the cooking and food prep info of a settlement building.
 */
public class BuildingPanelCooking
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Domain members
	private Cooking kitchen;
	/** The number of cooks label. */
	private JLabel numCooksLabel;
	/** The number of meals. */
	private JLabel numMealsLabel;
	/** The quality of the meals. */
	private JLabel mealQualityLabel;

	// Cache
	private int numCooksCache;
	private int numMealsCache;
	private int mealQualityCache;

	/**
	 * Constructor.
	 * @param kitchen the cooking building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelCooking(Cooking kitchen, MainDesktopPane desktop) {

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
		JLabel cookingLabel = new JLabel(Msg.getString("BuildingPanelCooking.cooking"), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(cookingLabel);

		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = new JLabel(Msg.getString("BuildingPanelCooking.numberOfCooks", numCooksCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(numCooksLabel);

		// Prepare cook capacity label
		JLabel cookCapacityLabel = new JLabel(Msg.getString("BuildingPanelCooking.cookCapacity", kitchen.getCookCapacity()), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(cookCapacityLabel);

		// Prepare meal number label
		numMealsCache = kitchen.getNumberOfCookedMeals();
		numMealsLabel = new JLabel(Msg.getString("BuildingPanelCooking.numberOfMeals", numMealsCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(numMealsLabel);

		// Prepare meal quality label
		mealQualityCache = kitchen.getBestMealQuality();
		mealQualityLabel = new JLabel(Msg.getString("BuildingPanelCooking.qualityOfMeals", mealQualityCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(mealQualityLabel);
	}	

	/**
	 * Update this panel
	 */
	public void update() {
		// Update cook number
		if (numCooksCache != kitchen.getNumCooks()) {
			numCooksCache = kitchen.getNumCooks();
			numCooksLabel.setText(Msg.getString("BuildingPanelCooking.numberOfCooks", numCooksCache)); //$NON-NLS-1$
		}

		// Update meal number
		if (numMealsCache != kitchen.getNumberOfCookedMeals()) {
			numMealsCache = kitchen.getNumberOfCookedMeals();
			numMealsLabel.setText(Msg.getString("BuildingPanelCooking.numberOfMeals", numMealsCache)); //$NON-NLS-1$
		}

		// Update meal quality
		if (mealQualityCache != kitchen.getBestMealQuality()) {
			mealQualityCache = kitchen.getBestMealQuality();
			mealQualityLabel.setText(Msg.getString("BuildingPanelCooking.qualityOfMeals", mealQualityCache)); //$NON-NLS-1$
		}
	}
}