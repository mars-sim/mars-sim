/**
 * Mars Simulation Project
 * TabPanelMissions.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.vehicle.TabPanelMission;

/**
 * Tab panel displaying a list of settlement missions.<br>
 * Renamed to plural form to be distinguishable from
 * {@link TabPanelMission}, which displays a vehicle's
 * single mission's details.
 */
@SuppressWarnings("serial")
public class TabPanelMissions
extends TabPanel {

	// Data members
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private List<Mission> missionsCache;
	private DefaultListModel<Mission> missionListModel;
	private JList<Mission> missionList;
	private JButton missionButton;
	private JButton monitorButton;
	private JCheckBox overrideCheckbox;

	private static MissionManager missionManager;

	/**
	 * Constructor.
	 * @param settlement {@link Settlement} the settlement this tab panel is for.
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public TabPanelMissions(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelMissions.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelMissions.tooltip"), //$NON-NLS-1$
			settlement, desktop
		);

		// Initialize data members.
		this.settlement = settlement;
	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		missionManager = Simulation.instance().getMissionManager();
		
		// Create label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Create settlement missions label.
		JLabel label = new JLabel(Msg.getString("TabPanelMissions.label"), JLabel.CENTER); //$NON-NLS-1$
		label.setFont(new Font("Serif", Font.BOLD, 16));
		//label.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(label);

		// Create center panel.
		JPanel centerPanel = new JPanel(new BorderLayout());
//		centerPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(centerPanel, BorderLayout.CENTER);

		// Create mission list panel.
		JPanel missionListPanel = new JPanel();
		centerPanel.add(missionListPanel, BorderLayout.CENTER);

		// Create mission scroll panel.
		JScrollPane missionScrollPanel = new JScrollPane();
		missionScrollPanel.setPreferredSize(new Dimension(190, 220));
		missionListPanel.add(missionScrollPanel);

		// Create mission list model.
		missionListModel = new DefaultListModel<Mission>();
		//MissionManager manager = Simulation.instance().getMissionManager();
		missionsCache = missionManager.getMissionsForSettlement(settlement);
		Iterator<Mission> i = missionsCache.iterator();
		while (i.hasNext()) missionListModel.addElement(i.next());

		// Create mission list.
		missionList = new JList<Mission>(missionListModel);
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		missionList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				boolean missionSelected = !missionList.isSelectionEmpty();
				missionButton.setEnabled(missionSelected);
				monitorButton.setEnabled(missionSelected);
			}
		});
		missionScrollPanel.setViewportView(missionList);

		// Create button panel.
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		centerPanel.add(buttonPanel, BorderLayout.EAST);

		// Create inner button panel.
		JPanel innerButtonPanel = new JPanel(new GridLayout(2, 1, 0, 2));
		buttonPanel.add(innerButtonPanel, BorderLayout.NORTH);

		// Create mission button.
		missionButton = new JButton(ImageLoader.getIcon(Msg.getString("img.mission"))); //$NON-NLS-1$
		missionButton.setMargin(new Insets(1, 1, 1, 1));
		missionButton.setToolTipText(Msg.getString("TabPanelMissions.tooltip.mission")); //$NON-NLS-1$
		missionButton.setEnabled(false);
		missionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openMissionTool();
			}
		});
		innerButtonPanel.add(missionButton);

		// Create monitor button.
		monitorButton = new JButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.setToolTipText(Msg.getString("TabPanelMissions.tooltip.monitor")); //$NON-NLS-1$
		monitorButton.setEnabled(false);
		monitorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openMonitorTool();
			}
		});
		innerButtonPanel.add(monitorButton);

		// Create bottom panel.
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		bottomPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(bottomPanel, BorderLayout.SOUTH);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelMissions.checkbox.overrideMissionCreation")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelMissions.tooltip.overrideMissionCreation")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setMissionCreationOverride(overrideCheckbox.isSelected());
			}
		});
		overrideCheckbox.setSelected(settlement.getMissionCreationOverride());
		bottomPanel.add(overrideCheckbox);
	}

	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		// Get all missions for the settlement.
		//MissionManager manager = Simulation.instance().getMissionManager();
		List<Mission> missions = missionManager.getMissionsForSettlement(settlement);

		// Update mission list if necessary.
		if (!missions.equals(missionsCache)) {
			Mission selectedMission = (Mission) missionList.getSelectedValue();

			missionsCache = missions;
			missionListModel.clear();
			Iterator<Mission> i = missionsCache.iterator();
			while (i.hasNext()) missionListModel.addElement(i.next());

			if ((selectedMission != null) && missionListModel.contains(selectedMission))
				missionList.setSelectedValue(selectedMission, true);
		}

		// Update mission override check box if necessary.
		if (settlement.getMissionCreationOverride() != overrideCheckbox.isSelected()) 
			overrideCheckbox.setSelected(settlement.getMissionCreationOverride());
	}

	/**
	 * Opens the mission tool to the selected mission in the mission list.
	 */
	private void openMissionTool() {
		Mission mission = (Mission) missionList.getSelectedValue();
		if (mission != null) {
			((MissionWindow) getDesktop().getToolWindow(MissionWindow.NAME)).selectMission(mission);
			getDesktop().openToolWindow(MissionWindow.NAME);
		}
	}

	/**
	 * Opens the monitor tool with a mission tab for the selected mission 
	 * in the mission list.
	 */
	private void openMonitorTool() {
		Mission mission = (Mission) missionList.getSelectedValue();
		if (mission != null) getDesktop().addModel(new PersonTableModel(mission));
	}

	/**
	 * Sets the settlement mission creation override flag.
	 * @param override the mission creation override flag.
	 */
	private void setMissionCreationOverride(boolean override) {
		settlement.setMissionCreationOverride(override);
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		settlement = null;
		missionsCache = null;
		missionListModel = null;
		missionList = null;
		missionButton = null;
		monitorButton = null;
		overrideCheckbox = null;
		missionManager = null;
	}
}