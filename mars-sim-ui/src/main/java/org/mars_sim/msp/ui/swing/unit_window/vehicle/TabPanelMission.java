/*
 * Mars Simulation Project
 * TabPanelMission.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.EntityListLauncher;

/**
 * Tab panel displaying vehicle mission info.
 */
@SuppressWarnings("serial")
public class TabPanelMission
extends TabPanel {
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelMission.class.getName());

	private static final String FLAG_MISSION ="mission";
	
	private JTextArea missionTextArea;
	private JTextArea missionPhaseTextArea;
	private DefaultListModel<Worker> memberListModel;
	private JList<Worker> memberList;
	private JButton missionButton;
	private JButton monitorButton;

	// Cache
	private String missionCache = null;
	private String missionPhaseCache = null;
	private Collection<Worker> memberCache;

	/** The Vehicle instance. */
	private Vehicle vehicle;

	/**
	 * Constructor.
	 * 
	 * @param vehicle the vehicle.
	 * @param desktop the main desktop.
	 */
	public TabPanelMission(Vehicle vehicle, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelMission.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(FLAG_MISSION),
			Msg.getString("TabPanelMission.title"), //$NON-NLS-1$
			vehicle, desktop
		);

      this.vehicle = vehicle;

	}

	@Override
	protected void buildUI(JPanel topContentPanel) {

		Mission mission = vehicle.getMission();

		// Prepare mission top panel
		JPanel missionTopPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		missionTopPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		topContentPanel.add(missionTopPanel, BorderLayout.NORTH);

		// Prepare mission panel
		JPanel missionPanel = new JPanel(new BorderLayout(0, 0));
		missionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		missionTopPanel.add(missionPanel);

		// Prepare mission text area
		if (mission != null) missionCache = mission.getName();
		missionTextArea = new JTextArea(2, 20);
		if (missionCache != null) missionTextArea.setText(missionCache);
		missionTextArea.setLineWrap(true);
		missionTextArea.setEditable(false);
		missionPanel.add(new JScrollPane(missionTextArea), BorderLayout.NORTH);

		// Prepare mission phase panel
		JPanel missionPhasePanel = new JPanel(new BorderLayout(0, 0));
		missionPhasePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		missionTopPanel.add(missionPhasePanel);

		// Prepare mission phase label
		JLabel missionPhaseLabel = new JLabel(Msg.getString("TabPanelMission.missionPhase"), SwingConstants.CENTER); //$NON-NLS-1$
		missionPhasePanel.add(missionPhaseLabel, BorderLayout.NORTH);
		missionTopPanel.add(missionPhasePanel);
		
		// Prepare mission phase text area
		if (mission != null) missionPhaseCache = mission.getPhaseDescription();
		missionPhaseTextArea = new JTextArea(2, 20);
		if (missionPhaseCache != null) missionPhaseTextArea.setText(missionPhaseCache);
		missionPhaseTextArea.setLineWrap(true);
		missionPhaseTextArea.setEditable(false);
		missionPhasePanel.add(new JScrollPane(missionPhaseTextArea), BorderLayout.CENTER);

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
		if (mission != null) memberCache = mission.getMembers();
		else memberCache = new ConcurrentLinkedQueue<>();
		Iterator<Worker> i = memberCache.iterator();
		while (i.hasNext()) memberListModel.addElement(i.next());

		// Create member list
		memberList = new JList<>(memberListModel);
		// memberList.addMouseListener(this);
		memberList.addMouseListener(new EntityListLauncher(getDesktop()));
		memberScrollPanel.setViewportView(memberList);

		JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		memberListPanel.add(buttonPanel);

		Unit unit = getUnit();
		
		// Create mission tool button
		missionButton = new JButton(ImageLoader.getIconByName(MissionWindow.ICON)); 
		missionButton.setMargin(new Insets(2, 2, 2, 2));
		missionButton.setToolTipText(Msg.getString("TabPanelMission.tooltip.mission")); //$NON-NLS-1$
		missionButton.addActionListener(e -> {
				Vehicle vehicle = (Vehicle) unit;
				Mission m = vehicle.getMission();
				if (m != null) {
					getDesktop().showDetails(m);
				}
		});
		missionButton.setEnabled(mission != null);
		buttonPanel.add(missionButton);

		// Create member monitor button
		monitorButton = new JButton(ImageLoader.getIconByName(MonitorWindow.ICON)); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(2, 2, 2, 2));
		monitorButton.setToolTipText(Msg.getString("TabPanelMission.tooltip.monitor")); //$NON-NLS-1$
		monitorButton.addActionListener(e -> {
				Vehicle vehicle = (Vehicle) unit;
				Mission m = vehicle.getMission();
				if (m != null) {
					try {
						getDesktop().addModel(new PersonTableModel(m));
					} catch (Exception ex) {
						logger.severe("PersonTableModel cannot be added.");
					}
				}
		});
		monitorButton.setEnabled(mission != null);
		buttonPanel.add(monitorButton);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		Vehicle vehicle = (Vehicle) getUnit();
		Mission mission = vehicle.getMission();

		if (mission != null) {
		    missionCache = mission.getName();
		}
		else {
		    missionCache = null;
		}
		if (!missionTextArea.getText().equals(missionCache)) {
			missionTextArea.setText(missionCache);
		}

		if (mission != null) {
		    missionPhaseCache = mission.getPhaseDescription();
		}
		else {
		    missionPhaseCache = null;
		}
		if (!missionPhaseTextArea.getText().equals(missionPhaseCache)) {
			missionPhaseTextArea.setText(missionPhaseCache);
		}

		// Update member list
		Collection<Worker> tempCollection = null;
		if (mission != null) {
		    tempCollection = mission.getMembers();
		}
		else {
		    tempCollection = new ConcurrentLinkedQueue<>();
		}
		if (!Arrays.equals(memberCache.toArray(), tempCollection.toArray())) {
			memberCache = tempCollection;
			memberListModel.clear();
			Iterator<Worker> i = memberCache.iterator();
			while (i.hasNext()) {
			    memberListModel.addElement(i.next());
			}
		}

		// Update mission and monitor buttons.
		missionButton.setEnabled(mission != null);
		monitorButton.setEnabled(mission != null);
	}

	@Override
	public void destroy() {
		super.destroy();
		
		missionTextArea = null;
		missionPhaseTextArea = null;
		memberListModel = null;
		memberList = null;
		missionButton = null;
		monitorButton = null;
		memberCache = null;
	}
}
