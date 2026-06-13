/*
 * Mars Simulation Project
 * TabPanelMission.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityLabel;
import com.mars_sim.ui.swing.utils.model.BaseWorkerModel;

/**
 * Tab panel displaying vehicle mission info.
 */
@SuppressWarnings("serial")
class TabPanelMission extends EntityTableTabPanel<Vehicle> 
		implements EntityListener {

	private static final String FLAG_MISSION ="mission";
	
	// Cache
	private String phaseCache = null;
	private EntityLabel missionLabel;
	private JLabel missionPhase;
	private MembersModel model;

	private Mission trackedMission;

	/**
	 * Constructor.
	 * 
	 * @param vehicle the vehicle.
	 * @param context the main desktop.
	 */
	public TabPanelMission(Vehicle vehicle, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("mission.singular"), //-NLS-1$
			ImageLoader.getIconByName(FLAG_MISSION),
			Msg.getString("mission.singular"), //-NLS-1$
			vehicle, context
		);

		setTableTitle(Msg.getString("mission.members"));
	}

	@Override
	protected JPanel createInfoPanel() {

		// Prepare mission top panel
		var missionTopPanel = new AttributePanel();

		// Prepare mission panel
		missionLabel = new EntityLabel(getContext());
		missionTopPanel.addLabelledItem(Msg.getString("entity.name"), missionLabel);
		
		missionPhase = missionTopPanel.addRow(Msg.getString("mission.phase"), "");

		// Prepare mission bottom panel
		JPanel missionBottomPanel = new JPanel(new BorderLayout(0, 0));
		missionBottomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

		assignMission();
		
		return missionTopPanel;
	}

	protected TableModel createModel() {
		model = new MembersModel();
		if (trackedMission != null) {
			model.update(trackedMission);
		}
		return model;
	}

	/**
	 * Updates the info on this panel.
	 */
	private void updateMission() {
		Mission mission = getEntity().getMission();

		String newPhase = null;
		if (mission != null) {
		    newPhase = mission.getPhaseDescription();
			if (newPhase.equals(phaseCache)) {
				newPhase = null;
			}
		}
		else if (phaseCache != null) {
		    newPhase = "";
		}

		if (newPhase != null) {
			phaseCache = newPhase;
			missionPhase.setText(Conversion.trim(phaseCache, 24));
		}
	}

	/**
	 * Remove listener on the mission
	 */
	@Override
	public void destroy() {
		if (trackedMission != null) {
			trackedMission.removeEntityListener(this);
		}
		super.destroy();
	}

	/**
	 * Mission assigned to a vehicle has changed.
	 */
	private void assignMission() {
		var mission = getEntity().getMission();

		missionLabel.setEntity(mission);

		// Swap over the Mission tracked
		if (trackedMission != null) {
			trackedMission.removeEntityListener(this);
		}
		if (mission != null) {
			mission.addEntityListener(this);
		}
		trackedMission = mission;

		updateMission();
	}

	/**
	 * Track changes in the associated Mission
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		switch(event.getType()) {
			case Vehicle.MISSION_EVENT -> assignMission();
			case Mission.PHASE_EVENT -> updateMission();
			case Mission.ADD_MEMBER_EVENT, Mission.REMOVE_MEMBER_EVENT -> model.update((Mission) event.getSource());
			default -> {
						// Do nothing as other event types are not tracked
						}
		}
	}

	private static class MembersModel extends BaseWorkerModel {
		public MembersModel() {
			super(NAME, TASK);

		}

		public void update(Mission mission) {
			setEntities(mission.getMembers());
		}
	}
}