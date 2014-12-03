/**
 * Mars Simulation Project
 * BuildingPanelCooking.java
 * @version 3.07 2014-11-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building.cooking;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingFunctionPanel;

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
		// 2014-11-21 Changed font type, size and color and label text
		JLabel cookingLabel = new JLabel(Msg.getString("BuildingPanelCooking.title"), JLabel.CENTER); //$NON-NLS-1$
		cookingLabel.setFont(new Font("Serif", Font.BOLD, 16));
		cookingLabel.setForeground(new Color(102, 51, 0)); // dark brown
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
		String mealQualityStr;
		mealQualityCache = kitchen.getBestMealQuality();
		// Update meal quality
		if (mealQualityCache == 0) mealQualityStr = "None";
		else mealQualityStr = "" + mealQualityCache;
		//System.out.println("BuildingPanelCooking.java : initial mealQualityCache : " + mealQualityCache);
		mealQualityLabel = new JLabel(Msg.getString("BuildingPanelCooking.bestQualityOfMeals", mealQualityStr), JLabel.CENTER); //$NON-NLS-1$
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
		numMeals = kitchen.getNumberOfCookedMeals();
		// Update meal number
		if (numMealsCache != numMeals) {
			numMealsCache = numMeals;
			numMealsLabel.setText(Msg.getString("BuildingPanelCooking.numberOfMeals", numMeals)); //$NON-NLS-1$
		}

		String mealQualityStr;
		int mealQuality = 0;
		mealQuality = kitchen.getBestMealQuality();
		// Update meal quality
		if (mealQualityCache != mealQuality) {
			mealQualityCache = mealQuality;
			if (mealQuality == 0) mealQualityStr = "None";
			else mealQualityStr = "" + mealQuality;
			//System.out.println("BuildingPanelCooking.java : updated mealQualityCache : "+ mealQuality);
			mealQualityLabel.setText(Msg.getString("BuildingPanelCooking.bestQualityOfMeals", mealQualityStr)); //$NON-NLS-1$
		}
	}
}