/*
 * Mars Simulation Project
 * BuildingPanelLiving.java
 * @date 2022-07-10
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;


/**
 * The BuildingPanelLiving class is a building function panel representing
 * the living accommodation details of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelLiving extends BuildingFunctionPanel {

	private static final String BED_ICON = "bed";

	private int bedCapCache;
	private int bedAssignedCache;
	private int bedOccupiedCache;
	private int bedEmptyCache;

	private JLabel bedCapLabel;
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
		AttributePanel labelPanel = new AttributePanel(4);
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

