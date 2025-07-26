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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.monitor.PersonTableModel;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityListLauncher;

/**
 * Tab panel displaying vehicle mission info.
 */
@SuppressWarnings("serial")
public class TabPanelMission
extends TabPanel {
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelMission.class.getName());

	private static final String FLAG_MISSION ="mission";
	
	private DefaultListModel<Worker> memberListModel;
	
	private JButton monitorButton;

	// Cache
	private String phaseCache = null;
	private Collection<Worker> memberCache;
	private EntityLabel missionLabel;
	private JLabel missionPhase;

	/**
	 * Constructor.
	 * 
	 * @param vehicle the vehicle.
	 * @param desktop the main desktop.
	 */
	public TabPanelMission(Vehicle vehicle, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelMission.title"), //-NLS-1$
			ImageLoader.getIconByName(FLAG_MISSION),
			Msg.getString("TabPanelMission.title"), //-NLS-1$
			vehicle, desktop
		);
	}

	@Override
	protected void buildUI(JPanel topContentPanel) {
  JList<Worker> memberList;

		// Prepare mission top panel
		var missionTopPanel = new AttributePanel();
		topContentPanel.add(missionTopPanel, BorderLayout.NORTH);

		// Prepare mission panel
		missionLabel = new EntityLabel(getDesktop());
		missionTopPanel.addLabelledItem("Name", missionLabel);
		
		missionPhase = missionTopPanel.addRow(Msg.getString("TabPanelMission.missionPhase"), "");

		// Prepare mission bottom panel
		JPanel missionBottomPanel = new JPanel(new BorderLayout(0, 0));
		missionBottomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		topContentPanel.add(missionBottomPanel, BorderLayout.CENTER);

		// Prepare member label
		JLabel memberLabel = new JLabel(Msg.getString("TabPanelMission.members"), SwingConstants.CENTER); //$NON-NLS-1$
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
		memberList.addMouseListener(new EntityListLauncher(getDesktop()));
		memberScrollPanel.setViewportView(memberList);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 5, 5));
		memberListPanel.add(buttonPanel);

		// Create member monitor button
		monitorButton = new JButton(ImageLoader.getIconByName(MonitorWindow.ICON)); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(2, 2, 2, 2));
		monitorButton.setToolTipText(Msg.getString("TabPanelMission.tooltip.monitor")); //$NON-NLS-1$
		monitorButton.addActionListener(e -> {
				Mission m = ((Vehicle) getUnit()).getMission();
				if (m != null) {
					try {
						getDesktop().addModel(new PersonTableModel(m));
					} catch (Exception ex) {
						logger.severe("PersonTableModel cannot be added.");
					}
				}
		});
		buttonPanel.add(monitorButton);

		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		Vehicle vehicle = (Vehicle) getUnit();
		Mission mission = vehicle.getMission();

		missionLabel.setEntity(mission);

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
		if (memberCache != null && !Arrays.equals(memberCache.toArray(), tempCollection.toArray())) {
			memberCache = tempCollection;
			memberListModel.clear();
			Iterator<Worker> i = memberCache.iterator();
			while (i.hasNext()) {
			    memberListModel.addElement(i.next());
			}
		}

		// Update mission and monitor buttons.
		monitorButton.setEnabled(mission != null);
	}
}
