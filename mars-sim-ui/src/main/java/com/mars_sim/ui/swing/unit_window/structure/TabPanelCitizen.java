/*
 * Mars Simulation Project
 * TabPanelCitizen.java
 * @date 2025-07-02
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.PopulationStats;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.unit_window.UnitListPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The TabPanelCitizen is a tab panel for information on all people
 * associated with a settlement.
 */
@SuppressWarnings("serial")
public class TabPanelCitizen extends TabPanel{

	private static final String CITIZEN_ICON = "people";
	
	private int populationCitizensCache = -1;
	private int populationCapacityCache = -1;
	private int populationIndoorCache = -1;

	private double populationAgeCache =-1;

	private String genderRatioCache = "";
	
	private Settlement settlement;

	private JLabel populationAgeLabel;
	private JLabel populationCitizensLabel;
	private JLabel populationCapacityLabel;
	private JLabel populationIndoorLabel;
	private JLabel genderRatioLabel;
	
	private UnitListPanel<Person> populationList;

	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelCitizen(Settlement unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCitizen.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(CITIZEN_ICON),
			Msg.getString("TabPanelCitizen.title"), //$NON-NLS-1$
			desktop
		);

		settlement = unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		// Prepare count spring layout panel.
		AttributePanel countPanel = new AttributePanel(5);
		content.add(countPanel, BorderLayout.NORTH);

		// Create associate label
		populationCitizensLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.citizen"),
											   		"", null);

		// Create population indoor label
		populationIndoorLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.indoor"),
											 		"", null);

		// Create population capacity label
		populationCapacityLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.capacity"),
											   		"", null);

		genderRatioLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.gender"),
		   								"", null);

		populationAgeLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.age"),
							"", null);
		
		populationList = new UnitListPanel<>(getDesktop(), new Dimension(175, 250)) {
			@Override
			protected Collection<Person> getData() {
				return settlement.getAllAssociatedPeople();
			}
		};
		addBorder(populationList, Msg.getString("TabPanelCitizen.titledBorder"));
		content.add(populationList, BorderLayout.CENTER);

		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		int num0 = settlement.getNumCitizens();
		// Update citizen num
		if (populationCitizensCache != num0) {
			populationCitizensCache = num0;
			populationCitizensLabel.setText(Integer.toString(populationCitizensCache));
		}

		int num = settlement.getIndoorPeopleCount();
		// Update indoor num
		if (populationIndoorCache != num) {
			populationIndoorCache = num;
			populationIndoorLabel.setText(Integer.toString(populationIndoorCache));
		}

		int cap = settlement.getPopulationCapacity();
		// Update capacity
		if (populationCapacityCache != cap) {
			populationCapacityCache = cap;
			populationCapacityLabel.setText(Integer.toString(populationCapacityCache));
		}
		
		var pop = settlement.getAllAssociatedPeople();
		var ratio = PopulationStats.getGenderRatioAsString(pop);
		// Update gender ratio
		if (!genderRatioCache.equals(ratio)) {
			genderRatioCache = ratio;
			genderRatioLabel.setText(genderRatioCache);
		}

		double age = PopulationStats.getAverageAge(pop);
		// Update capacity
		if (populationAgeCache != age) {
			populationAgeCache = age;
			populationAgeLabel.setText(StyleManager.DECIMAL_PLACES1.format(populationAgeCache));
		}
		
		// Update population list
		populationList.update();
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		populationList = null;
	}
}
