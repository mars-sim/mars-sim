/*
 * Mars Simulation Project
 * BuildingPanelPreparingDessert.java
 * @date 2022-07-11
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building.food;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingFunctionPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;


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
	private JLabel numCooksLabel;
	private JLabel servingsDessertLabel;
	private JLabel servingsDessertTodayLabel;
	private JLabel dessertQualityLabel;

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
		AttributePanel labelPanel = new AttributePanel(5);
		center.add(labelPanel, BorderLayout.NORTH);

		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = labelPanel.addTextField(Msg.getString("BuildingPanelPreparingDessert.numberOfCooks"),
										Integer.toString(numCooksCache), null); //$NON-NLS-1$

		// Prepare cook capacity label
		labelPanel.addTextField(Msg.getString("BuildingPanelPreparingDessert.cookCapacity"), 
										Integer.toString(kitchen.getCookCapacity()), null); //$NON-NLS-1$

		// Prepare serving number label
		servingsDessertCache = kitchen.getAvailableServingsDesserts();
		servingsDessertLabel = labelPanel.addTextField(
										Msg.getString("BuildingPanelPreparingDessert.availableDesserts"),
										Integer.toString(servingsDessertCache), null); //$NON-NLS-1$

		// Prepare servings dessert today label
		servingsDessertTodayCache = kitchen.getTotalServingsOfDessertsToday();
		servingsDessertTodayLabel = labelPanel.addTextField(
										Msg.getString("BuildingPanelPreparingDessert.dessertsToday"),
										Integer.toString(servingsDessertTodayCache), null); //$NON-NLS-1$

		dessertGradeCache = computeGrade(kitchen.getBestDessertQualityCache());
		// Update Dessert grade
		dessertQualityLabel = labelPanel.addTextField(
										Msg.getString("BuildingPanelPreparingDessert.bestQualityOfDessert"),
						 				dessertGradeCache, null); //$NON-NLS-1$
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
