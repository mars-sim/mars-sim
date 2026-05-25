/*
 * Mars Simulation Project
 * TabPanelVehicles.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;


import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.model.GenericVehicleModel;

/** 
 * The TabPanelVehicles is a tab panel for parked vehicles and vehicles on mission.
 */
@SuppressWarnings("serial")
class TabPanelVehicles extends EntityTabPanel<Settlement>
 implements EntityListener {
	
	private static final String SUV_ICON ="vehicle";
	
	private ParkedVehicleModel parkedVehicles;
	private MissionVehicleModel missionVehicles;
	
	/**
	 * Constructor.
	 * @param unit the unit to display
	 * @param context the UI context.
	 */
	public TabPanelVehicles(Settlement unit, UIContext context) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("vehicle.plural"), //$NON-NLS-1$
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
		var dim = new Dimension(-1, 100);
		parkedVehicles = new ParkedVehicleModel(settlement);
		parkedVehicles.update();
		vehiclePanel.add(SwingHelper.createScrolledTable(parkedVehicles, getContext(),
					Msg.getString("TabPanelVehicles.parked.vehicles"), dim));

		// Mission vehicles
		missionVehicles = new MissionVehicleModel(settlement);
		missionVehicles.update();
		vehiclePanel.add(SwingHelper.createScrolledTable(missionVehicles, getContext(),
			Msg.getString("TabPanelVehicles.mission.vehicles"), dim));
	}

	@Override
	public void entityUpdate(EntityEvent event) {
		parkedVehicles.update();
		missionVehicles.update();
	}

	@Override
	public void destroy() {
		if (missionVehicles != null) {
			parkedVehicles.cleanUp();
			missionVehicles.cleanUp();
		}
		super.destroy();
	}

	/**
	 * The MissionVehicleModel is a model for mission vehicles.
	 */
	private static class MissionVehicleModel extends GenericVehicleModel {
		private Settlement settlement;

		public MissionVehicleModel(Settlement settlement) {
			super(NAME, MISSION);
			this.settlement = settlement;
		}

		public void update() {
			setEntities(settlement.getMissionVehicles());
		}
	}

	/**
	 * The ParkedVehicleModel is a model for parked vehicles.
	 */
	private static class ParkedVehicleModel extends GenericVehicleModel {
		private Settlement settlement;
		
		public ParkedVehicleModel(Settlement settlement) {
			super(NAME, TYPE);
			this.settlement = settlement;
		}

		public void update() {
			setEntities(settlement.getParkedGaragedVehicles());
		}
	}
}