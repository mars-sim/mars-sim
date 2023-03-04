/*
 * Mars Simulation Project
 * BuildingPanelCooking.java
 * @date 2022-07-11
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building.food;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingFunctionPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * This class is a building function panel representing
 * the cooking and food prepation info of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelCooking
extends BuildingFunctionPanel {

	private static final String COOKING_ICON = "cooking";
	
	// Domain members
	private Cooking kitchen;
	private JLabel numCooksLabel;
	private JLabel numMealsLabel;
	private JLabel numMealsTodayLabel;
	private JLabel mealGradeLabel;

	// Cache
	private int numCooksCache;
	private int numMealsCache;
	private String gradeCache = "";
	
	private int numMealsTodayCache;

	/**
	 * Constructor.
	 * 
	 * @param kitchen the cooking building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelCooking(Cooking kitchen, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelCooking.title"),  //$NON-NLS-1$
			ImageLoader.getIconByName(COOKING_ICON),
			kitchen.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.kitchen = kitchen;
	}
	
	@Override
	protected void buildUI(JPanel center) {
		// Prepare label panel
		AttributePanel labelPanel = new AttributePanel(5);
		center.add(labelPanel, BorderLayout.NORTH);

		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = labelPanel.addTextField( Msg.getString("BuildingPanelCooking.numberOfCooks"), 
									Integer.toString(numCooksCache), null); //$NON-NLS-1$

		// Prepare cook capacity label
		labelPanel.addTextField( Msg.getString("BuildingPanelCooking.cookCapacity"), 
									Integer.toString(kitchen.getCookCapacity()), null);

		// Prepare # of available meal label
		numMealsCache = kitchen.getNumberOfAvailableCookedMeals();
		numMealsLabel = labelPanel.addTextField(Msg.getString("BuildingPanelCooking.availableMeals"), 
									Integer.toString(numMealsCache), null); //$NON-NLS-1$

		// Prepare # of today cooked meal label
		numMealsTodayCache = kitchen.getTotalNumberOfCookedMealsToday();
		numMealsTodayLabel = labelPanel.addTextField(Msg.getString("BuildingPanelCooking.mealsToday"),
									Integer.toString(numMealsTodayCache), null); //$NON-NLS-1$

		// Prepare meal grade label
		String grade = computeGrade(kitchen.getBestMealQualityCache());
		mealGradeLabel = labelPanel.addTextField(Msg.getString("BuildingPanelCooking.bestQualityOfMeals"),
									grade, null); //$NON-NLS-1$
	}

	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {

		int numCooks = 0;
		numCooks = kitchen.getNumCooks();
		// Update cook number
		if (numCooksCache != numCooks) {
			numCooksCache = numCooks;
			numCooksLabel.setText(Integer.toString(numCooks));
		}

		int numMeals = 0;
		numMeals = kitchen.getNumberOfAvailableCookedMeals();
		// Update # of available meals
		if (numMealsCache != numMeals) {
			numMealsCache = numMeals;
			numMealsLabel.setText(Integer.toString(numMeals));
		}

		int numMealsToday = 0;
		numMealsToday = kitchen.getTotalNumberOfCookedMealsToday();
		// Update # of meals cooked today
		if (numMealsTodayCache != numMealsToday) {
			numMealsTodayCache = numMealsToday;
			numMealsTodayLabel.setText(Integer.toString(numMealsToday));
		}

		double mealQuality = kitchen.getBestMealQualityCache();
		String grade = computeGrade(mealQuality);
		// Update meal grade
		if (!gradeCache.equals(grade)) {
			gradeCache = grade;
			mealGradeLabel.setText(grade); 
		}
	}
	
	/**
	 * Converts a numeral quality to letter grade for a meal.
	 * 
	 * @param quality 
	 * @return grade
	 */
	static String computeGrade(double quality) {
		String grade = "";
				
		if (quality < -4)
			grade = "C-";
		else if (quality < -3)
			grade = "C";
		else if (quality < -2)
			grade = "C+";
		else if (quality < -1)
			grade = "B-";
		else if (quality < 0)
			grade = "B";
		else if (quality < 1)
			grade = "B+";
		else if (quality < 2)
			grade = "A-";
		else if (quality < 3)
			grade = "A";
		else
			grade = "A+";
				
		return grade;
	}
}
