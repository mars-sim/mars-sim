/**
 * Mars Simulation Project
 * CookingBuildingPanel.java
 * @version 2.78 2004-11-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.structure.building.function.Cooking;
import org.mars_sim.msp.ui.standard.*;

/**
 * The CookingBuildingPanel class is a building function panel representing 
 * the cooking and food prep info of a settlement building.
 */
public class CookingBuildingPanel extends BuildingFunctionPanel {

	// Domain members
	private Cooking kitchen;
	private JLabel numCooksLabel; // The number of cooks label.
	private JLabel numMealsLabel; // The number of meals.
	private JLabel mealQualityLabel; // The quality of the meals.
	
	// Cache
	private int numCooksCache;
	private int numMealsCache;
	private int mealQualityCache;

	/**
	 * Constructor
	 *
	 * @param kitchen the cooking building this panel is for.
	 * @param desktop The main desktop.
	 */
	public CookingBuildingPanel(Cooking kitchen, MainDesktopPane desktop) {
        
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
		JLabel cookingLabel = new JLabel("Cooking", JLabel.CENTER);
		labelPanel.add(cookingLabel);
        
		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = new JLabel("Number of Cooks: " + numCooksCache, JLabel.CENTER);
		labelPanel.add(numCooksLabel);
        
		// Prepare cook capacity label
		JLabel cookCapacityLabel = new JLabel("Cook Capacity: " + kitchen.getCookCapacity(), JLabel.CENTER);
		labelPanel.add(cookCapacityLabel);
        
		// Prepare meal number label
		numMealsCache = kitchen.getNumberOfCookedMeals();
		numMealsLabel = new JLabel("Number of Meals: " + numMealsCache, JLabel.CENTER);
		labelPanel.add(numMealsLabel);
		
		// Prepare meal quality label
		mealQualityCache = kitchen.getBestMealQuality();
		mealQualityLabel = new JLabel("Quality of Meals: " + mealQualityCache, JLabel.CENTER);
		labelPanel.add(mealQualityLabel);
	}	

	/**
	 * Update this panel
	 */
	public void update() {
		// Update cook number
		if (numCooksCache != kitchen.getNumCooks()) {
			numCooksCache = kitchen.getNumCooks();
			numCooksLabel.setText("Number of Cooks: " + numCooksCache);
		}
		
		// Update meal number
		if (numMealsCache != kitchen.getNumberOfCookedMeals()) {
			numMealsCache = kitchen.getNumberOfCookedMeals();
			numMealsLabel.setText("Number of Meals: " + numMealsCache);
		}
		
		// Update meal quality
		if (mealQualityCache != kitchen.getBestMealQuality()) {
			mealQualityCache = kitchen.getBestMealQuality();
			mealQualityLabel.setText("Quality of Meals: " + mealQualityCache);
		}
	}
}