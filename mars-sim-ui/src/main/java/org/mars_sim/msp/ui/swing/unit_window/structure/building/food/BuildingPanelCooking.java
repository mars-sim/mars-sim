/**
 * Mars Simulation Project
 * BuildingPanelCooking.java
 * @version 3.07 2015-01-06
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
	private JLabel mealQualityLabel;

	// Cache
	private int numCooksCache;
	private int numMealsCache;
	private int mealQualityCache;
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


		// Prepare meal quality label
		//String mealQualityStr;
		mealQualityCache = kitchen.getBestMealQualityCache();
		// Update meal quality
		//if (mealQualityCache == 0) mealQualityStr = "None";
		//else mealQualityStr = "" + mealQualityCache;
		//System.out.println("BuildingPanelCooking.java : initial mealQualityCache : " + mealQualityCache);
		mealQualityLabel = new JLabel(Msg.getString("BuildingPanelCooking.bestQualityOfMeals", mealQualityCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(mealQualityLabel);
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

		//String mealQualityStr;
		int mealQuality = 0;
		mealQuality = kitchen.getBestMealQualityCache();
		// Update meal quality
		if (mealQualityCache != mealQuality) {
			mealQualityCache = mealQuality;
			//if (mealQuality == 0) mealQualityStr = "None";
			//else mealQualityStr = "" + mealQuality;
			//System.out.println("BuildingPanelCooking.java : updated mealQualityCache : "+ mealQuality);
			mealQualityLabel.setText(Msg.getString("BuildingPanelCooking.bestQualityOfMeals", mealQuality)); //$NON-NLS-1$
		}
	}
}