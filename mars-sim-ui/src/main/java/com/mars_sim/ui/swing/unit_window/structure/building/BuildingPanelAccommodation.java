/*
 * Mars Simulation Project
 * BuildingPanelAccommodation.java
 * @date 2023-11-24
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.mars_sim.core.structure.building.function.LivingAccommodation;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityLauncher;


/**
 * The BuildingPanelLiving class is a building function panel representing
 * the living accommodation details of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelAccommodation extends BuildingFunctionPanel {

	private static final String BED_ICON = "bed";

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	private int bedCapCache;
	private int bedOccupiedCache;

	private JLabel bedCapLabel;
	private JLabel bedOccupiedLabel;

	private LivingAccommodation living;

	

	/**
	 * Constructor.
	 * 
	 * @param living the building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelAccommodation(LivingAccommodation living, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelAccommodation.title"), 
			ImageLoader.getIconByName(BED_ICON),
			living.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.living = living;
	}
	
	/**
	 * Builds the UI elements.
	 * 
	 * @param center the panel to be built
	 */
	@Override
	protected void buildUI(JPanel center) {
  		ActivitySpotModel bedTableModel;

		// Create label panel
		AttributePanel labelPanel = new AttributePanel(3);
		center.add(labelPanel, BorderLayout.NORTH);

		// Create bed capacity label
		bedCapLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAccommodation.beds.capacity"),
									Integer.toString(living.getBedCap()), "Max number of beds available");

		// Create bedOccupiedLabel
		bedOccupiedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelAccommodation.beds.assigned"),
									Integer.toString(living.getNumOccupiedActivitySpots()), "Number of beds already occupied");

		// Create guest bed capacity label
		labelPanel.addTextField(Msg.getString("BuildingPanelAccommodation.guesthouse"),
									Boolean.toString(living.isGuestHouse()), "Max number of guest beds available");
		
		// Create scroll panel for beds
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setPreferredSize(new Dimension(160, 120));

		center.add(scrollPanel, BorderLayout.CENTER);
	    scrollPanel.getViewport().setOpaque(false);
	    scrollPanel.setOpaque(false);
		scrollPanel.setBorder(StyleManager.createLabelBorder("Beds"));

		// Prepare medical table model
		bedTableModel = new ActivitySpotModel(living.getActivitySpots(),
											  getDesktop().getSimulation().getUnitManager());

		// Prepare medical table
		JTable table = new JTable(bedTableModel);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		EntityLauncher.attach(table, getDesktop());

		scrollPanel.setViewportView(table);
	}

	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		// Update bedCapLabel
		if (bedCapCache != living.getBedCap()) {
			bedCapCache = living.getBedCap();
			bedCapLabel.setText(Integer.toString(bedCapCache));
		}

		// Update bedOccupiedLabel
		if (bedOccupiedCache != living.getNumOccupiedActivitySpots()) {
			bedOccupiedCache = living.getNumOccupiedActivitySpots();
			bedOccupiedLabel.setText(Integer.toString(bedOccupiedCache));
		}
	}
}

