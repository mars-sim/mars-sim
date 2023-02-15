/*
 * Mars Simulation Project
 * TabPanelMalfunction.java
 * @date 2022-08-02
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.MalfunctionPanel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

@SuppressWarnings("serial")
public class TabPanelMalfunction extends TabPanel {

	private static final String WARN_ICON = "warn";

	/** The Settlement instance. */
	private Settlement settlement;
	
	/** A collection of malfunction panels. */
	private Collection<MalfunctionPanel> malfunctionPanels;
	/** A collection of malfunctions. */
	private Collection<Malfunction> malfunctions;
	/** Malfunction list panel. */
	private JPanel malfunctionListPanel;

	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelMalfunction(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super("Malfunction", 
			ImageLoader.getIconByName(WARN_ICON), 
			"Malfunction", 
			unit, desktop);

		settlement = (Settlement) unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		
		// Create malfunctions panel.
		JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
		content.add(mainPanel, BorderLayout.NORTH);
		// Create titled border panel
		addBorder(content, "Active Building Malfunctions");

		// Prepare malfunctions outer list panel.
		JPanel malfunctionListMainPanel = new JPanel(new BorderLayout(0, 0));

		// Prepare malfunctions list panel.
		malfunctionListPanel = new JPanel();
		//malfunctionListPanel.setPadding(2);
		malfunctionListPanel.setLayout(new BoxLayout(malfunctionListPanel, BoxLayout.Y_AXIS));
		malfunctionListMainPanel.add(malfunctionListPanel, BorderLayout.NORTH);

		mainPanel.add(malfunctionListPanel);
		malfunctionPanels = new ArrayList<>();
		malfunctions = new ArrayList<>();
					
		Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Iterator<Malfunction> j = building.getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) {
				Malfunction malfunction = j.next();
				if (!malfunctions.contains(malfunction)) {
					malfunctions.add(malfunction);
					MalfunctionPanel panel = new MalfunctionPanel(malfunction, building);
					panel.setBorder(new MarsPanelBorder());
					//panel.setPadding(5);
					malfunctionListPanel.add(panel);
					malfunctionPanels.add(panel);
				}
			}
		}
	}

	/**
	 * Populates the malfunctions list.
	 * 
	 * @param newMalfunctions
	 */
	private void populateMalfunctionsList(List<Malfunction> newMalfunctions) {

		Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Iterator<Malfunction> j = building.getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) {
				Malfunction malfunction = j.next();
				if (!malfunctions.contains(malfunction)) {
					MalfunctionPanel panel = new MalfunctionPanel(malfunction, building);
					panel.setBorder(new MarsPanelBorder());
					//panel.setPadding(5);
					malfunctionListPanel.add(panel);
					malfunctionPanels.add(panel);
				}
			}
	
			// Remove malfunction panels for repaired malfunctions.
			Iterator<Malfunction> iter2 = malfunctions.iterator();
			while (iter2.hasNext()) {
				Malfunction malfunction = iter2.next();
				if (!newMalfunctions.contains(malfunction)) {
					MalfunctionPanel panel = getMalfunctionPanel(malfunction);
					if (panel != null) {
						malfunctionPanels.remove(panel);
						malfunctionListPanel.remove(panel);
					}
				}
			}
		}
		
		// Update malfunction cache.
		malfunctions = new ArrayList<>(newMalfunctions);
	}

	/**
	 * Gets an existing malfunction panel for a given malfunction.
	 * 
	 * @param malfunction the given malfunction
	 * @return malfunction panel or null if none.
	 */
	private MalfunctionPanel getMalfunctionPanel(Malfunction malfunction) {
		MalfunctionPanel result = null;

		Iterator<MalfunctionPanel> i = malfunctionPanels.iterator();
		while (i.hasNext()) {
			MalfunctionPanel panel = i.next();
			if (panel.getMalfunction() == malfunction)
				result = panel;
		}

		return result;
	}
	
	/**
	 * Updates the tab panel.
	 */
	@Override
	public void update() {
		// Create a new malfunctions list.
		List<Malfunction> newMalfunctions = new ArrayList<>();
		Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
		while (i.hasNext()) {
			Iterator<Malfunction> j = i.next().getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) {
				newMalfunctions.add(j.next());
			}
		}

		// Check if malfunctions list has changed.
		if (!CollectionUtils.isEqualCollection(malfunctions, newMalfunctions)) {		
			// Populate malfunctions list.
			populateMalfunctionsList(newMalfunctions);
		} 
		
		// Have each malfunction panel update.
		Iterator<MalfunctionPanel> ii = malfunctionPanels.iterator();
		while (ii.hasNext())
			ii.next().updateMalfunctionPanel();
	}

	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		settlement = null;
		malfunctions = null;
		malfunctionListPanel = null;
	}
}
