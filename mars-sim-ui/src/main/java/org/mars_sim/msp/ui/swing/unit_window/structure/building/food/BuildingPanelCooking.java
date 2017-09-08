/**
 * Mars Simulation Project
 * BuildingPanelCooking.java
 * @version 3.1.0 2017-04-26
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building.food;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingFunctionPanel;

import javax.swing.*;

import java.awt.*;

/**
 * This class is a building function panel representing
 * the cooking and food prepation info of a settlement building.
 */
public class BuildingPanelCooking
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Domain members
	private Cooking kitchen;
	/** The number of cooks label. */
	private JLabel numCooksLabel;
	/** The number of available meals. */
	private JLabel numMealsLabel;
	/** The number of meals cooked today. */
	private JLabel numMealsTodayLabel;
	/** The quality of the meals. */
	private JLabel mealGradeLabel;

	// Cache
	private int numCooksCache;
	private int numMealsCache;
	private String gradeCache = "";
	
	private int numMealsTodayCache;

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
		JPanel labelPanel = new JPanel(new GridLayout(6, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Prepare cooking label
		// 2014-11-21 Changed font type, size and color and label text
		JLabel cookingLabel = new JLabel(Msg.getString("BuildingPanelCooking.title"), JLabel.CENTER); //$NON-NLS-1$
		cookingLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//cookingLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(cookingLabel);

		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = new JLabel(Msg.getString("BuildingPanelCooking.numberOfCooks", numCooksCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(numCooksLabel);

		// Prepare cook capacity label
		JLabel cookCapacityLabel = new JLabel(Msg.getString("BuildingPanelCooking.cookCapacity", kitchen.getCookCapacity()), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(cookCapacityLabel);

		// Prepare # of available meal label
		numMealsCache = kitchen.getNumberOfAvailableCookedMeals();
		numMealsLabel = new JLabel(Msg.getString("BuildingPanelCooking.availableMeals", numMealsCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(numMealsLabel);

		// 2015-01-06 Added numMealsTodayLabel
		// Prepare # of today cooked meal label
		numMealsTodayCache = kitchen.getTotalNumberOfCookedMealsToday();
		numMealsTodayLabel = new JLabel(Msg.getString("BuildingPanelCooking.mealsToday", numMealsTodayCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(numMealsTodayLabel);

		// Prepare meal grade label
		String grade = computeGrade(kitchen.getBestMealQualityCache());
		mealGradeLabel = new JLabel(Msg.getString("BuildingPanelCooking.bestQualityOfMeals", grade), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(mealGradeLabel);
	}

	/**
	 * Update this panel
	 */
	public void update() {

		int numCooks = 0;
		numCooks = kitchen.getNumCooks();
		// Update cook number
		if (numCooksCache != numCooks) {
			numCooksCache = numCooks;
			numCooksLabel.setText(Msg.getString("BuildingPanelCooking.numberOfCooks", numCooks)); //$NON-NLS-1$
		}

		int numMeals = 0;
		numMeals = kitchen.getNumberOfAvailableCookedMeals();
		// Update # of available meals
		if (numMealsCache != numMeals) {
			numMealsCache = numMeals;
			numMealsLabel.setText(Msg.getString("BuildingPanelCooking.availableMeals", numMeals)); //$NON-NLS-1$
		}

		// 2015-01-06 Added numMealsTodayLabel
		int numMealsToday = 0;
		numMealsToday = kitchen.getTotalNumberOfCookedMealsToday();
		// Update # of meals cooked today
		if (numMealsTodayCache != numMealsToday) {
			numMealsTodayCache = numMealsToday;
			numMealsTodayLabel.setText(Msg.getString("BuildingPanelCooking.mealsToday", numMealsToday)); //$NON-NLS-1$
		}

		double mealQuality = kitchen.getBestMealQualityCache();
		String grade = computeGrade(mealQuality);
		// Update meal grade
		if (!gradeCache.equals(grade)) {
			gradeCache = grade;
			mealGradeLabel.setText(Msg.getString("BuildingPanelCooking.bestQualityOfMeals", grade)); //$NON-NLS-1$
		}
	}
	
	/***
	 * Converts a numeral quality to letter grade for a meal
	 * @param quality 
	 * @return grade
	 */
	public String computeGrade(double quality) {
		String grade = "";
				
		if (quality < -3)
			grade = "C-";
		else if (quality < -2)
			grade = "C+";
		else if (quality < -1)
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
		else //if (quality < 4)
			grade = "A+";
				
		return grade;
	}
	
	
	
}