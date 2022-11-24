/*
 * Mars Simulation Project
 * BuildingPanelLiving.java
 * @date 2022-07-10
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.panel.WebPanel;


/**
 * The BuildingPanelLiving class is a building function panel representing
 * the living accommodation details of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelLiving extends BuildingFunctionPanel {

	private static final String BED_ICON = Msg.getString("icon.bed"); //$NON-NLS-1$

	private int bedCapCache;
	private int bedAssignedCache;
	private int bedOccupiedCache;
	private int bedEmptyCache;

	private JTextField bedCapLabel;
	private JTextField bedAssignsLabel;
	private JTextField bedOccupiedLabel;
	private JTextField bedEmptyLabel;

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
			ImageLoader.getNewIcon(BED_ICON),
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
		WebPanel labelPanel = new WebPanel(new GridLayout(5, 2, 0, 0));
		center.add(labelPanel, BorderLayout.NORTH);

		// Create bed capacity label
		bedCapLabel = addTextField(labelPanel, Msg.getString("BuildingPanelLiving.beds.capacity"),
								   living.getBedCap(), 5, "Max number of beds available");

		// Create # of assigned bed label
		bedAssignsLabel = addTextField(labelPanel, Msg.getString("BuildingPanelLiving.beds.assigned"),
									   living.getNumAssignedBeds(), 5, "Number of beds already assigned");

		// Create bedOccupiedLabel
		bedOccupiedLabel = addTextField(labelPanel, Msg.getString("BuildingPanelLiving.beds.occupied"),
										living.getNumOccupiedActivitySpots(), 5, "Number of beds already occupied");

		// Create bedEmptyLabel
		bedEmptyLabel = addTextField(labelPanel, Msg.getString("BuildingPanelLiving.beds.empty"),
									 living.getNumEmptyActivitySpots(), 5, "Number of empty beds available");
	}

	@Override
	public void update() {
		// Update bedCapLabel
		if (bedCapCache != living.getBedCap()) {
			bedCapCache = living.getBedCap();
			bedCapLabel.setText(Integer.toString(bedCapCache));
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

