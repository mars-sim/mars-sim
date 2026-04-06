/*
 * Mars Simulation Project
 * BuildingPanelAccommodation.java
 * @date 2023-11-24
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.entitywindow.building;

import javax.swing.JPanel;
import javax.swing.table.TableModel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.LivingAccommodation;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.ActivitySpotModel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The BuildingPanelLiving class is a building function panel representing
 * the living accommodation details of a settlement building.
 */
@SuppressWarnings("serial")
class BuildingPanelAccommodation extends EntityTableTabPanel<Building> 
		implements TemporalComponent{

	private static final String BED_ICON = "bed";
	
	private JIntegerLabel bedOccupiedLabel;

	private LivingAccommodation living;

	private ActivitySpotModel bedTableModel;

	/**
	 * Constructor.
	 * 
	 * @param living the building this panel is for.
	 * @param context the UI context
	 */
	public BuildingPanelAccommodation(LivingAccommodation living, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelAccommodation.title"), 
			ImageLoader.getIconByName(BED_ICON), null,
			living.getBuilding(), context
		);

		// Initialize data members
		this.living = living;
		setTableTitle("Beds");
	}
	
	/**
	 * Builds the info panel
	 */
	@Override
	protected JPanel createInfoPanel() {
		// Create label panel
		var labelPanel = new AttributePanel();

		// Create bed capacity label
		labelPanel.addTextField(Msg.getString("BuildingPanelAccommodation.beds.capacity"),
									Integer.toString(living.getBedCap()), "Max number of beds available");

		// Create bedOccupiedLabel
		bedOccupiedLabel = new JIntegerLabel(living.getNumOccupiedActivitySpots());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelAccommodation.beds.assigned"), bedOccupiedLabel, "Number of beds already occupied");
		// Create guest bed capacity label
		labelPanel.addTextField(Msg.getString("BuildingPanelAccommodation.guesthouse"),
									Boolean.toString(living.isGuestHouse()), "Max number of guest beds available");
		return labelPanel;
	}
		
	@Override
	protected TableModel createModel() {
		bedTableModel = new ActivitySpotModel(living.getActivitySpots(),
											  getContext().getSimulation().getUnitManager());

		return bedTableModel;
	}

	/**
	 * Updates this panel on clock pulse but should probably be event driven instead.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {

		// Update bedOccupiedLabel
		bedOccupiedLabel.setValue(living.getNumOccupiedActivitySpots());
		bedTableModel.refresh();
	}
}