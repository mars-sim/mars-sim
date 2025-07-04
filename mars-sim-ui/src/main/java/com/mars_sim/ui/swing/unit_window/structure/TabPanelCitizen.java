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
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
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
	
	private int populationCitizensCache;
	private int populationCapacityCache;
	private int populationIndoorCache;

	private double populationAgeCache;

	
	private String genderRatioCache;
	
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
		populationCitizensCache = settlement.getNumCitizens();
		populationCitizensLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.citizen"),
											   		Integer.toString(populationCitizensCache), null);

		// Create population indoor label
		populationIndoorCache = settlement.getIndoorPeopleCount();
		populationIndoorLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.indoor"),
											 		Integer.toString(populationIndoorCache), null);

		// Create population capacity label
		populationCapacityCache = settlement.getPopulationCapacity();
		populationCapacityLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.capacity"),
											   		Integer.toString(populationCapacityCache), null);

		genderRatioCache = settlement.getGenderRatioString();
		genderRatioLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.gender"),
		   		genderRatioCache, null);

		populationAgeCache = settlement.computePopulationAverageAge();
		populationAgeLabel = countPanel.addTextField(Msg.getString("TabPanelCitizen.age"),
				Double.toString(Math.round(populationAgeCache * 10.0)/10.0), null);
		
		
		populationList = new UnitListPanel<>(getDesktop(), new Dimension(175, 250)) {
			@Override
			protected Collection<Person> getData() {
				return settlement.getAllAssociatedPeople();
			}
		};
		addBorder(populationList, Msg.getString("TabPanelCitizen.titledBorder"));
		content.add(populationList, BorderLayout.CENTER);
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
			populationCitizensLabel.setText(populationCitizensCache + "");
		}

		int num = settlement.getIndoorPeopleCount();
		// Update indoor num
		if (populationIndoorCache != num) {
			populationIndoorCache = num;
			populationIndoorLabel.setText(populationIndoorCache + "");
		}

		int cap = settlement.getPopulationCapacity();
		// Update capacity
		if (populationCapacityCache != cap) {
			populationCapacityCache = cap;
			populationCapacityLabel.setText(populationCapacityCache + "");
		}
		
		String ratio = genderRatioCache = settlement.getGenderRatioString();
		// Update gender ratio
		if (!genderRatioCache.equals(ratio)) {
			genderRatioCache = ratio;
			genderRatioLabel.setText(genderRatioCache);
		}

		double age = Math.round(settlement.computePopulationAverageAge() * 10.0)/10.0;
		// Update capacity
		if (populationAgeCache != age) {
			populationAgeCache = age;
			populationAgeLabel.setText(populationAgeCache + "");
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
