/*
 * Mars Simulation Project
 * BuildingPanelLiving.java
 * @date 2023-11-24
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.structure.building.function.LivingAccommodations;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.utils.AttributePanel;


/**
 * The BuildingPanelLiving class is a building function panel representing
 * the living accommodation details of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelLiving extends BuildingFunctionPanel {

	private static final String BED_ICON = "bed";

	private int bedCapCache;
	private int guestBedCapCache;
	private int bedAssignedCache;
	private int bedOccupiedCache;
	private int bedEmptyCache;

	private JLabel bedCapLabel;
	private JLabel guestBedCapLabel;
	private JLabel bedAssignsLabel;
	private JLabel bedOccupiedLabel;
	private JLabel bedEmptyLabel;

	private LivingAccommodations living;

	/**
	 * Constructor.
	 * 
	 * @param living the building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelLiving(LivingAccommodations living, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelLiving.title"), 
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

		// Create label panel
		AttributePanel labelPanel = new AttributePanel(5);
		center.add(labelPanel, BorderLayout.NORTH);

		// Create bed capacity label
		bedCapLabel = labelPanel.addTextField(Msg.getString("BuildingPanelLiving.beds.capacity"),
									Integer.toString(living.getBedCap()), "Max number of beds available");

		// Create # of assigned bed label
		bedAssignsLabel = labelPanel.addTextField(Msg.getString("BuildingPanelLiving.beds.assigned"),
									Integer.toString(living.getNumAssignedBeds()), "Number of beds already assigned");

		// Create bedOccupiedLabel
		bedOccupiedLabel = labelPanel.addTextField(Msg.getString("BuildingPanelLiving.beds.occupied"),
									Integer.toString(living.getNumOccupiedActivitySpots()), "Number of beds already occupied");

		// Create bedEmptyLabel
		bedEmptyLabel = labelPanel.addTextField(Msg.getString("BuildingPanelLiving.beds.empty"),
									 Integer.toString(living.getNumEmptyActivitySpots()), "Number of empty beds available");
	
		// Create guest bed capacity label
		guestBedCapLabel = labelPanel.addTextField(Msg.getString("BuildingPanelLiving.guestBeds.capacity"),
									Integer.toString(living.getMaxGuestBeds()), "Max number of guest beds available");

	}

	@Override
	public void update() {
		// Update bedCapLabel
		if (bedCapCache != living.getBedCap()) {
			bedCapCache = living.getBedCap();
			bedCapLabel.setText(Integer.toString(bedCapCache));
		}

		// Update guestBedCapLabel
		if (guestBedCapCache != living.getMaxGuestBeds()) {
			guestBedCapCache = living.getMaxGuestBeds();
			guestBedCapLabel.setText(Integer.toString(guestBedCapCache));
		}
			
		// Update bedAssignsLabel
		if (bedAssignedCache != living.getNumAssignedBeds()) {
			bedAssignedCache = living.getNumAssignedBeds();
			bedAssignsLabel.setText(Integer.toString(bedAssignedCache));
		}

		// Update bedOccupiedLabel
		if (bedOccupiedCache != living.getNumOccupiedActivitySpots()) {
			bedOccupiedCache = living.getNumOccupiedActivitySpots();
			bedOccupiedLabel.setText(Integer.toString(bedOccupiedCache));
		}

		// Update bedEmptyLabel
		if (bedEmptyCache != living.getNumEmptyActivitySpots()) {
			bedEmptyCache = living.getNumEmptyActivitySpots();
			bedEmptyLabel.setText(Integer.toString(bedEmptyCache));
		}
	}
}

