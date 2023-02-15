/*
 * Mars Simulation Project
 * BuildingPanelMalfunctionable.java
 * @date 2022-08-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.MalfunctionPanel;


/**
 * The BuildingPanelMalfunctionable class is a building function panel
 * representing the malfunctions of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelMalfunctionable extends BuildingFunctionPanel {

	private static final String WARN_ICON = "warn";

	/** The malfunctionable building. */
	private Malfunctionable malfunctionable;
	
	/** A collection of malfunction panels. */
	private Collection<MalfunctionPanel> malfunctionPanels;
	/** A collection of malfunctions in building. */
	private Collection<Malfunction> malfunctions;
	/** Malfunction list panel. */
	private JPanel malfunctionListPanel;

	/**
	 * Constructor.
	 * 
	 * @param malfunctionable the malfunctionable building the panel is for.
	 * @param desktop         The main desktop.
	 */
	public BuildingPanelMalfunctionable(Malfunctionable malfunctionable, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelMalfunctionable.title"), 
			ImageLoader.getIconByName(WARN_ICON), 
			(Building) malfunctionable, 
			desktop
		);

		// Initialize data members.
		this.malfunctionable = malfunctionable;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create scroll panel for malfunction list
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setPreferredSize(new Dimension(170, 120));
		center.add(scrollPanel, BorderLayout.NORTH);
		// Create titled border panel
		addBorder(center, "Active Malfunctions");
		
		scrollPanel.setOpaque(false);
		scrollPanel.setBackground(new Color(0, 0, 0, 128));
		scrollPanel.getViewport().setOpaque(false);
		scrollPanel.getViewport().setBackground(new Color(0, 0, 0, 128));

		// Create malfunction list main panel.
		JPanel malfunctionListMainPanel = new JPanel(new BorderLayout(0, 0));
		scrollPanel.setViewportView(malfunctionListMainPanel);

		// Create malfunction list panel
		malfunctionListPanel = new JPanel();
		//malfunctionListPanel.setPadding(5);
		malfunctionListPanel.setLayout(new BoxLayout(malfunctionListPanel, BoxLayout.Y_AXIS));
		malfunctionListMainPanel.add(malfunctionListPanel, BorderLayout.NORTH);

		malfunctionPanels = new ArrayList<>();
		
		// Create malfunction panels
		malfunctions = new ArrayList<>(malfunctionable.getMalfunctionManager().getMalfunctions());
		Iterator<Malfunction> i = malfunctions.iterator();
		while (i.hasNext()) {
			MalfunctionPanel panel = new MalfunctionPanel(i.next(), null);
			malfunctionListPanel.add(panel);
			malfunctionPanels.add(panel);
		}
	}

	@Override
	public void update() {

		Collection<Malfunction> newMalfunctions = malfunctionable.getMalfunctionManager().getMalfunctions();

		// Update malfunction panels if necessary.
		if (!CollectionUtils.isEqualCollection(malfunctions, newMalfunctions)) {
			// Add malfunction panels for new malfunctions.
			Iterator<Malfunction> iter1 = newMalfunctions.iterator();
			while (iter1.hasNext()) {
				Malfunction malfunction = iter1.next();
				if (!malfunctions.contains(malfunction)) {
					MalfunctionPanel panel = new MalfunctionPanel(malfunction, null);
					malfunctionPanels.add(panel);
					malfunctionListPanel.add(panel);
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

			// Update malfunction cache.
			malfunctions = new ArrayList<>(newMalfunctions);
		}

		// Have each malfunction panel update.
		Iterator<MalfunctionPanel> i = malfunctionPanels.iterator();
		while (i.hasNext())
			i.next().updateMalfunctionPanel();
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
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		malfunctionable = null;
		malfunctionPanels = null;
		malfunctions = null;
		malfunctionListPanel = null;
	}
}
