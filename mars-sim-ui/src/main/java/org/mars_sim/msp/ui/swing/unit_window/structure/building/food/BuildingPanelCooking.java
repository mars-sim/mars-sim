/*
 * Mars Simulation Project
 * BuildingPanelCooking.java
 * @date 2022-07-11
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building.food;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingFunctionPanel;

import javax.swing.*;

import java.awt.*;

/**
 * This class is a building function panel representing
 * the cooking and food prepation info of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelCooking
extends BuildingFunctionPanel {

	private static final String COOKING_ICON = Msg.getString("icon.cooking"); //$NON-NLS-1$
	
	// Domain members
	private Cooking kitchen;
	/** The number of cooks label. */
	private JTextField numCooksLabel;
	/** The number of available meals. */
	private JTextField numMealsLabel;
	/** The number of meals cooked today. */
	private JTextField numMealsTodayLabel;
	/** The quality of the meals. */
	private JTextField mealGradeLabel;

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
			ImageLoader.getNewIcon(COOKING_ICON),
			kitchen.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.kitchen = kitchen;
	}
	
	@Override
	protected void buildUI(JPanel center) {
		// Prepare label panel
		JPanel labelPanel = new JPanel(new GridLayout(5, 2, 1, 1));
		center.add(labelPanel, BorderLayout.NORTH);

		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = addTextField(labelPanel, Msg.getString("BuildingPanelCooking.numberOfCooks"), 
				numCooksCache, 5, null); //$NON-NLS-1$

		// Prepare cook capacity label
		addTextField(labelPanel, Msg.getString("BuildingPanelCooking.cookCapacity"), 
				kitchen.getCookCapacity(), 5, null);

		// Prepare # of available meal label
		numMealsCache = kitchen.getNumberOfAvailableCookedMeals();
		numMealsLabel = addTextField(labelPanel, Msg.getString("BuildingPanelCooking.availableMeals"), 
				numMealsCache, 5, null); //$NON-NLS-1$

		// Prepare # of today cooked meal label
		numMealsTodayCache = kitchen.getTotalNumberOfCookedMealsToday();
		numMealsTodayLabel = addTextField(labelPanel, Msg.getString("BuildingPanelCooking.mealsToday"), numMealsTodayCache, 5, null); //$NON-NLS-1$

		// Prepare meal grade label
		String grade = computeGrade(kitchen.getBestMealQualityCache());
		mealGradeLabel = addTextField(labelPanel, Msg.getString("BuildingPanelCooking.bestQualityOfMeals"), grade, 5, null); //$NON-NLS-1$
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
