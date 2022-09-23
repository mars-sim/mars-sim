/*
 * Mars Simulation Project
 * TabPanelCitizen.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;

import com.alee.laf.panel.WebPanel;

/**
 * The TabPanelAssociatedPeople is a tab panel for information on all people
 * associated with a settlement.
 */
@SuppressWarnings("serial")
public class TabPanelCitizen extends TabPanel{

	private static final String CITIZEN_ICON = Msg.getString("icon.citizen"); //$NON-NLS-1$
	
	private int populationCitizensCache;
	private int populationCapacityCache;
	private int populationIndoorCache;

	private Settlement settlement;

	private JTextField populationCitizensLabel;
	private JTextField populationCapacityLabel;
	private JTextField populationIndoorLabel;

	private UnitListPanel<Person> populationList;

	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelCitizen(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCitizen.title"), //$NON-NLS-1$
			ImageLoader.getNewIcon(CITIZEN_ICON),
			Msg.getString("TabPanelCitizen.title"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		// Prepare count spring layout panel.
		WebPanel countPanel = new WebPanel(new SpringLayout());
		content.add(countPanel, BorderLayout.NORTH);

		// Create associate label
		populationCitizensCache = settlement.getNumCitizens();
		populationCitizensLabel = addTextField(countPanel, Msg.getString("TabPanelCitizen.associated"),
											   populationCitizensCache, 4, null);

		// Create population indoor label
		populationIndoorCache = settlement.getIndoorPeopleCount();
		populationIndoorLabel = addTextField(countPanel, Msg.getString("TabPanelCitizen.indoor"),
											 populationIndoorCache, 4, null);

		// Create population capacity label
		populationCapacityCache = settlement.getPopulationCapacity();
		populationCapacityLabel = addTextField(countPanel, Msg.getString("TabPanelCitizen.capacity"),
											   populationCapacityCache, 4, null);

		// Set up the spring layout.
		SpringUtilities.makeCompactGrid(countPanel, 3, 2, // rows, cols
				INITX_DEFAULT_1, INITY_DEFAULT, // initX, initY
				XPAD_DEFAULT, YPAD_DEFAULT); // xPad, yPad

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
