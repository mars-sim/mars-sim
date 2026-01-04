/*
 * Mars Simulation Project
 * TabPanelVehicles.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;


import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitListPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/** 
 * The TabPanelVehicles is a tab panel for parked vehicles and vehicles on mission.
 */
@SuppressWarnings("serial")
class TabPanelVehicles extends EntityTabPanel<Settlement>
 implements EntityListener {
	
	private static final String SUV_ICON ="vehicle";
	
	private UnitListPanel<Vehicle> parkedVehicles;
	private UnitListPanel<Vehicle> missionVehicles;
	
	/**
	 * Constructor.
	 * @param unit the unit to display
	 * @param context the UI context.
	 */
	public TabPanelVehicles(Settlement unit, UIContext context) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("Vehicle.plural"), //$NON-NLS-1$
			ImageLoader.getIconByName(SUV_ICON), null,
			context, unit
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {
		var settlement = getEntity();
		JPanel vehiclePanel = new JPanel();
		vehiclePanel.setLayout(new BoxLayout(vehiclePanel, BoxLayout.Y_AXIS));
		content.add(vehiclePanel);

		// Parked Vehicles
		parkedVehicles = new UnitListPanel<>(getContext()) {
			@Override
			protected Collection<Vehicle> getData() {
				return settlement.getParkedGaragedVehicles();
			}
		};
		parkedVehicles.setBorder(SwingHelper.createLabelBorder(Msg.getString("TabPanelVehicles.parked.vehicles")));
		vehiclePanel.add(parkedVehicles);

		// Mission vehicles
		missionVehicles = new UnitListPanel<>(getContext()) {
			@Override
			protected Collection<Vehicle> getData() {
				return settlement.getMissionVehicles();
			}
		};
		missionVehicles.setBorder(SwingHelper.createLabelBorder(Msg.getString("TabPanelVehicles.mission.vehicles")));
		vehiclePanel.add(missionVehicles);
	}

	@Override
	public void entityUpdate(EntityEvent event) {
		parkedVehicles.update();
		missionVehicles.update();
	}
}