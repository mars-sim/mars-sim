/*
 * Mars Simulation Project
 * TabPanelCrew.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.entitywindow.mission.MemberTableModel;

/**
 * The TabPanelCrew is a tab panel for a vehicle's crew information.
 */
@SuppressWarnings("serial")
public class TabPanelCrew extends EntityTableTabPanel<Vehicle> implements EntityListener {

	private static final String CREW_ICON = "people"; //$NON-NLS-1$

	private MemberTableModel memberTableModel;

	private JIntegerLabel crewNumTF;

	/**
	 * Constructor.
	 * 
	 * @param vehicle the vehicle.
	 * @param context the UI context.
	 */
	public TabPanelCrew(Vehicle vehicle, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCrew.title"),
			ImageLoader.getIconByName(CREW_ICON),
			Msg.getString("TabPanelCrew.tooltip"),
			vehicle, context
		);
	}

	@Override
	protected JPanel createInfoPanel() {
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        
		// Create crew count panel
		AttributePanel crewCountPanel = new AttributePanel(2);
		northPanel.add(crewCountPanel, BorderLayout.CENTER);

		Crewable vehicle = (Crewable) getEntity();

		// Create crew num header label
		crewNumTF = new JIntegerLabel(vehicle.getCrewNum());
		crewCountPanel.addLabelledItem(Msg.getString("TabPanelCrew.crewNum"), crewNumTF, Msg.getString("TabPanelCrew.crew.tooltip"));

		// Create crew cap header label
		int crewCapacityCache = vehicle.getCrewCapacity();
		crewCountPanel.addTextField(Msg.getString("TabPanelCrew.crewCapacity"),
								Integer.toString(crewCapacityCache),
					 			Msg.getString("TabPanelCrew.crewCapacity.tooltip"));

		return northPanel;
	}

	@Override
	protected TableModel createModel() {
		memberTableModel = new MemberTableModel((Crewable) getEntity());
		assignMission();
		return memberTableModel;
	}

	@Override
	public void destroy() {
		if (memberTableModel != null) {
			if (memberTableModel.getMission() != null) {
				memberTableModel.getMission().removeEntityListener(this);
			}
		}
		super.destroy();
	}

    @Override
    public void entityUpdate(EntityEvent event) {
		switch(event.getType()) {
			case Mission.ADD_MEMBER_EVENT, Mission.REMOVE_MEMBER_EVENT -> {
				if (memberTableModel != null) {
					memberTableModel.updateOccupantList();
				}
			}

			case Vehicle.MISSION_EVENT -> assignMission();

			default -> {
				// Do nothing
			}
		}
    }

	private void assignMission() {
		if (memberTableModel != null) {
			if (memberTableModel.getMission() != null) {
				memberTableModel.getMission().removeEntityListener(this);
			}

			memberTableModel.setMission(getEntity().getMission());
			if (memberTableModel.getMission() != null) {
				memberTableModel.getMission().addEntityListener(this);
			}
		}
	}
}