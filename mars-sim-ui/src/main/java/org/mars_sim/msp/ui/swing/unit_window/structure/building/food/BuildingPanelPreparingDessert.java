/**
 * Mars Simulation Project
 * BuildingPanelPreparingDessert.java
 * @version 3.07 2014-11-28
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building.food;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingFunctionPanel;

import javax.swing.*;

import java.awt.*;

/**
 * This class is a building function panel representing 
 * the dessert preparation of a settlement building.
 */
public class BuildingPanelPreparingDessert
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Domain members
	private PreparingDessert kitchen;
	/** The number of cooks label. */
	private JLabel numCooksLabel;
	/** The number of desserts. */
	private JLabel servingsDessertLabel;
	private JLabel servingsDessertTodayLabel;
	/** The quality of the desserts. */
	private JLabel dessertQualityLabel;

	// Cache
	private int numCooksCache;
	private int servingsDessertCache;
	private int servingsDessertTodayCache;
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
		JPanel labelPanel = new JPanel(new GridLayout(6, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));
		
		// Prepare cooking label
		// 2014-11-21 Changed font type, size and color and label text
		JLabel makingSoyLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.title"), JLabel.CENTER); //$NON-NLS-1$
		makingSoyLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//makingSoyLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(makingSoyLabel);

		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.numberOfCooks", numCooksCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(numCooksLabel);

		// Prepare cook capacity label
		JLabel cookCapacityLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.cookCapacity", kitchen.getCookCapacity()), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(cookCapacityLabel);

		// Prepare serving number label
		servingsDessertCache = kitchen.getAvailableServingsDesserts();
		servingsDessertLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.availableDesserts", servingsDessertCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(servingsDessertLabel);
		
		// 2015-01-06 Added servingsDessertTodayLabel
		// Prepare servings dessert today label
		servingsDessertTodayCache = kitchen.getTotalServingsOfDessertsToday();
		servingsDessertTodayLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.dessertsToday", servingsDessertTodayCache), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(servingsDessertTodayLabel);

		
		// 2014-11-30 Added dessertQualityStr
		String dessertQualityStr;
		dessertQualityCache = kitchen.getBestDessertQuality();
		// Update Dessert quality
		if (dessertQualityCache == 0) dessertQualityStr = "None";
		else dessertQualityStr = "" + dessertQualityCache;
		dessertQualityLabel = new JLabel(Msg.getString("BuildingPanelPreparingDessert.bestQualityOfDessert", dessertQualityStr), JLabel.CENTER); //$NON-NLS-1$
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

		// Update servings of dessert
		if (servingsDessertCache != kitchen.getAvailableServingsDesserts()) {
			servingsDessertCache = kitchen.getAvailableServingsDesserts();
			servingsDessertLabel.setText(Msg.getString("BuildingPanelPreparingDessert.availableDesserts", servingsDessertCache)); //$NON-NLS-1$
		}

		// Update servings of dessert today
		if (servingsDessertTodayCache != kitchen.getTotalServingsOfDessertsToday()) {
			servingsDessertTodayCache = kitchen.getTotalServingsOfDessertsToday();
			servingsDessertTodayLabel.setText(Msg.getString("BuildingPanelPreparingDessert.dessertsToday", servingsDessertTodayCache)); //$NON-NLS-1$
		}
				
		// 2014-11-30 Added dessertQualityStr
		String dessertQualityStr;
		int dessertQuality = 0;
		dessertQuality = kitchen.getBestDessertQuality();
		// Update Dessert quality
		if (dessertQualityCache != dessertQuality) {
			dessertQualityCache = dessertQuality;
			if (dessertQuality == 0) dessertQualityStr = "None";
			else dessertQualityStr = "" + dessertQuality;
			dessertQualityLabel.setText(Msg.getString("BuildingPanelPreparingDessert.bestQualityOfDessert", dessertQualityStr)); //$NON-NLS-1$
		}
	}
}