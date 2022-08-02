/*
 * Mars Simulation Project
 * TabPanelMalfunction.java
 * @date 2022-08-02
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.MalfunctionPanel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.panel.WebPanel;

@SuppressWarnings("serial")
public class TabPanelMalfunction extends TabPanel {

	private static final String WARN_ICON = Msg.getString("icon.warn"); //$NON-NLS-1$

	/** The Settlement instance. */
	private Settlement settlement;
	
	private WebPanel malfunctionsListPanel;

	private List<Malfunction> malfunctionsList;

	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelMalfunction(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super("Malfunction", 
			ImageLoader.getNewIcon(WARN_ICON), 
			"Malfunction", 
			unit, desktop);

		settlement = (Settlement) unit;
		malfunctionsList = new ArrayList<>();
	}

	@Override
	protected void buildUI(JPanel content) {
		
		// Create malfunctions panel.
		WebPanel malfunctionsPanel = new WebPanel(new BorderLayout());
		content.add(malfunctionsPanel, BorderLayout.CENTER);
		
		// Create titled border panel
		addBorder(content, "Active Building Malfunctions");

		// Prepare malfunctions outer list panel.
		WebPanel malfunctionListMainPanel = new WebPanel(new BorderLayout(0, 0));

		// Prepare malfunctions list panel.
		malfunctionsListPanel = new WebPanel();
		malfunctionsListPanel.setPadding(5);
		malfunctionsPanel.add(malfunctionsListPanel);
		malfunctionsListPanel.setLayout(new BoxLayout(malfunctionsListPanel, BoxLayout.Y_AXIS));
		malfunctionListMainPanel.add(malfunctionsListPanel, BorderLayout.NORTH);

		populateMalfunctionsList();
	}

	/**
	 * Populates the malfunctions list.
	 */
	private void populateMalfunctionsList() {
		// Clear the list.
		malfunctionsListPanel.removeAll();

		// Populate the list.
		malfunctionsList.clear();
		Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Iterator<Malfunction> j = building.getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) {
				Malfunction malfunction = j.next();
				if (!malfunctionsList.contains(malfunction)) {
					malfunctionsList.add(malfunction);
					WebPanel panel = new MalfunctionPanel(malfunction, building);
					malfunctionsListPanel.add(panel);
				}
			}
		}
	}

	/**
	 * Updates the tab panel.
	 */
	@Override
	public void update() {
		// Create temporary malfunctions list.
		List<Malfunction> tempMalfunctions = new ArrayList<>();
		Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
		while (i.hasNext()) {
			Iterator<Malfunction> j = i.next().getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) {
				tempMalfunctions.add(j.next());
			}
		}

		// Check if malfunctions list has changed.
		if (!CollectionUtils.isEqualCollection(malfunctionsList, tempMalfunctions)) {		
			// Populate malfunctions list.
			populateMalfunctionsList();
		} 
	
		else {
			// Update all building malfunction panels.
			Component[] components = malfunctionsListPanel.getComponents();
			for (Component component : components) {
				((MalfunctionPanel) component).updateMalfunctionPanel();
			}
		}
	}

	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		settlement = null;
		malfunctionsList = null;
		malfunctionsListPanel = null;
	}
}
