/*
 * Mars Simulation Project
 * TabPanelMission.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.monitor.PersonTableModel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityListLauncher;

/**
 * Tab panel displaying vehicle mission info.
 */
@SuppressWarnings("serial")
class TabPanelMission extends EntityTabPanel<Vehicle> 
		implements EntityListener {

	private static final String FLAG_MISSION ="mission";
	
	private DefaultListModel<Worker> memberListModel;
	
	private JButton monitorButton;

	// Cache
	private String phaseCache = null;
	private Collection<Worker> memberCache;
	private EntityLabel missionLabel;
	private JLabel missionPhase;

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
			Msg.getString("Mission.singular"), //-NLS-1$
			ImageLoader.getIconByName(FLAG_MISSION),
			Msg.getString("Mission.singular"), //-NLS-1$
			context, vehicle
		);
	}

	@Override
	protected void buildUI(JPanel topContentPanel) {
  		JList<Worker> memberList;

		// Prepare mission top panel
		var missionTopPanel = new AttributePanel();
		topContentPanel.add(missionTopPanel, BorderLayout.NORTH);

		// Prepare mission panel
		missionLabel = new EntityLabel(getContext());
		missionTopPanel.addLabelledItem(Msg.getString("Entity.name"), missionLabel);
		
		missionPhase = missionTopPanel.addRow(Msg.getString("Mission.phase"), "");

		// Prepare mission bottom panel
		JPanel missionBottomPanel = new JPanel(new BorderLayout(0, 0));
		missionBottomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		topContentPanel.add(missionBottomPanel, BorderLayout.CENTER);

		// Prepare member label
		JLabel memberLabel = new JLabel(Msg.getString("Mission.members"), SwingConstants.CENTER); //-NLS-1$
		StyleManager.applySubHeading(memberLabel);
		missionBottomPanel.add(memberLabel, BorderLayout.NORTH);

		// Prepare member list panel
		JPanel memberListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		missionBottomPanel.add(memberListPanel, BorderLayout.CENTER);

		// Create scroll panel for member list.
		JScrollPane memberScrollPanel = new JScrollPane();
		memberScrollPanel.setPreferredSize(new Dimension(225, 100));
		memberListPanel.add(memberScrollPanel);

		// Create member list model
		memberListModel = new DefaultListModel<>();

		// Create member list
		memberList = new JList<>(memberListModel);
		memberList.addMouseListener(new EntityListLauncher(getContext()));
		memberScrollPanel.setViewportView(memberList);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 5, 5));
		memberListPanel.add(buttonPanel);

		// Create member monitor button
		monitorButton = new JButton(ImageLoader.getIconByName(MonitorWindow.ICON)); //-NLS-1$
		monitorButton.setMargin(new Insets(2, 2, 2, 2));
		monitorButton.setToolTipText(Msg.getString("TabPanelMission.tooltip.monitor")); //-NLS-1$
		monitorButton.addActionListener(e -> {
			Mission m = getEntity().getMission();
			if (m != null) {
				showModel(new PersonTableModel(m));
			}
		});
		buttonPanel.add(monitorButton);

		assignMission();
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

		// Update member list
		Collection<Worker> tempCollection = null;
		if (mission != null) {
		    tempCollection = mission.getMembers();
		}
		else {
		    tempCollection = new ConcurrentLinkedQueue<>();
		}
		if (memberCache == null || !memberCache.equals(tempCollection)) {
			memberCache = tempCollection;
			memberListModel.clear();
			memberCache.forEach(i ->  memberListModel.addElement(i));
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
		monitorButton.setEnabled(mission != null);

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
			case Mission.PHASE_EVENT, Mission.ADD_MEMBER_EVENT,
				Mission.REMOVE_MEMBER_EVENT -> updateMission();
			default -> { // Do nothing
						}
		}
	}
}