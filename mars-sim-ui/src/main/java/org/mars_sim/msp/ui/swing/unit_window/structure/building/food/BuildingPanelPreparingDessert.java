/*
 * Mars Simulation Project
 * BuildingPanelPreparingDessert.java
 * @date 2022-07-11
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building.food;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingFunctionPanel;

import javax.swing.*;

import java.awt.*;

/**
 * This class is a building function panel representing
 * the dessert preparation of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelPreparingDessert
extends BuildingFunctionPanel {

	private static final String DESSERT_ICON = "dessert";
	
	// Domain members
	private PreparingDessert kitchen;
	/** The number of cooks label. */
	private JTextField numCooksLabel;
	/** The number of desserts. */
	private JTextField servingsDessertLabel;
	private JTextField servingsDessertTodayLabel;
	/** The quality of the desserts. */
	private JTextField dessertQualityLabel;

	// Cache
	private int numCooksCache;
	private int servingsDessertCache;
	private int servingsDessertTodayCache;
	
	private String dessertGradeCache = "";
	
	/**
	 * Constructor.
	 * @param kitchen the cooking building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelPreparingDessert(PreparingDessert kitchen, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor.
		super(
			Msg.getString("BuildingPanelPreparingDessert.title"), 
			ImageLoader.getIconByName(DESSERT_ICON),
			kitchen.getBuilding(), 
			desktop
		);
		
		// Initialize data members
		this.kitchen = kitchen;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
			
		// Prepare label panel
		JPanel labelPanel = new JPanel(new GridLayout(5, 2, 1, 1));
		center.add(labelPanel, BorderLayout.NORTH);

		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = addTextField(labelPanel, Msg.getString("BuildingPanelPreparingDessert.numberOfCooks")
				, numCooksCache, 5, null); //$NON-NLS-1$

		// Prepare cook capacity label
		addTextField(labelPanel, Msg.getString("BuildingPanelPreparingDessert.cookCapacity"), 
				kitchen.getCookCapacity(), 5, null); //$NON-NLS-1$

		// Prepare serving number label
		servingsDessertCache = kitchen.getAvailableServingsDesserts();
		servingsDessertLabel = addTextField(labelPanel, 
				Msg.getString("BuildingPanelPreparingDessert.availableDesserts"),
					servingsDessertCache, 5, null); //$NON-NLS-1$

		// Prepare servings dessert today label
		servingsDessertTodayCache = kitchen.getTotalServingsOfDessertsToday();
		servingsDessertTodayLabel = addTextField(labelPanel, 
				Msg.getString("BuildingPanelPreparingDessert.dessertsToday"),
					servingsDessertTodayCache, 5, null); //$NON-NLS-1$

		dessertGradeCache = computeGrade(kitchen.getBestDessertQualityCache());
		// Update Dessert grade
		dessertQualityLabel = addTextField(labelPanel, 
				Msg.getString("BuildingPanelPreparingDessert.bestQualityOfDessert"),
						 dessertGradeCache, 5, null); //$NON-NLS-1$
	}

	/**
	 * Converts a numeral quality to letter grade for a dessert.
	 * 
	 * @param quality 
	 * @return grade
	 */
	private static String computeGrade(double quality) {
		return BuildingPanelCooking.computeGrade(quality);
	}
	
	/**
	 * Update this panel
	 */
	@Override
	public void update() {
		// Update cook number
		if (numCooksCache != kitchen.getNumCooks()) {
			numCooksCache = kitchen.getNumCooks();
			numCooksLabel.setText(Integer.toString(numCooksCache)); //$NON-NLS-1$
		}

		// Update servings of dessert
		if (servingsDessertCache != kitchen.getAvailableServingsDesserts()) {
			servingsDessertCache = kitchen.getAvailableServingsDesserts();
			servingsDessertLabel.setText(Integer.toString(servingsDessertCache)); //$NON-NLS-1$
		}

		// Update servings of dessert today
		if (servingsDessertTodayCache != kitchen.getTotalServingsOfDessertsToday()) {
			servingsDessertTodayCache = kitchen.getTotalServingsOfDessertsToday();
			servingsDessertTodayLabel.setText(Integer.toString(servingsDessertTodayCache)); //$NON-NLS-1$
		}

		String dessertGrade = computeGrade(kitchen.getBestDessertQualityCache());
		// Update Dessert grade
		if (!dessertGradeCache.equals(dessertGrade)) {
			dessertGradeCache = dessertGrade;
			dessertQualityLabel.setText(dessertGrade); //$NON-NLS-1$
		}
	}
}
