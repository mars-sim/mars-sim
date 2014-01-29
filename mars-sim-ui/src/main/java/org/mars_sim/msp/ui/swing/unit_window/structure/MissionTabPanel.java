/**
 * Mars Simulation Project
 * MissionTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

/**
 * Tab panel displaying a list of settlement missions.
 */
public class MissionTabPanel extends TabPanel {

	// Data members
	private Settlement settlement;
	private List<Mission> missionsCache;
	private DefaultListModel missionListModel;
	private JList missionList;
	private JButton missionButton;
	private JButton monitorButton;
	private JCheckBox overrideCheckbox;
	
	/**
	 * Constructor
	 * @param settlement the settlement this tab panel is for.
	 * @param desktop the main desktop panel.
	 */
	public MissionTabPanel(Settlement settlement, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super("Mission", null, "Settlement Missions", settlement, desktop);
        
        // Initialize data members.
        this.settlement = settlement;
        
        // Create label panel.
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(labelPanel);
        
        // Create settlement missions label.
        JLabel settlementMissionsLabel = new JLabel("Settlement Missions", JLabel.CENTER);
        labelPanel.add(settlementMissionsLabel);
        
        // Create center panel.
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Create mission list panel.
        JPanel missionListPanel = new JPanel();
        centerPanel.add(missionListPanel, BorderLayout.CENTER);
        
        // Create mission scroll panel.
        JScrollPane missionScrollPanel = new JScrollPane();
		missionScrollPanel.setPreferredSize(new Dimension(190, 220));
		missionListPanel.add(missionScrollPanel);
		
		// Create mission list model.
		missionListModel = new DefaultListModel();
		MissionManager manager = Simulation.instance().getMissionManager();
        missionsCache = manager.getMissionsForSettlement(settlement);
		Iterator<Mission> i = missionsCache.iterator();
		while (i.hasNext()) missionListModel.addElement(i.next());
		
		// Create mission list.
		missionList = new JList(missionListModel);
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
        missionButton = new JButton(ImageLoader.getIcon("Mission"));
        missionButton.setMargin(new Insets(1, 1, 1, 1));
        missionButton.setToolTipText("Open mission in mission tool.");
        missionButton.setEnabled(false);
        missionButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		openMissionTool();
        	}
        });
        innerButtonPanel.add(missionButton);
        
        // Create monitor button.
        monitorButton = new JButton(ImageLoader.getIcon("Monitor"));
        monitorButton.setMargin(new Insets(1, 1, 1, 1));
        monitorButton.setToolTipText("Open tab in monitor tool for this mission.");
        monitorButton.setEnabled(false);
        monitorButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		openMonitorTool();
        	}
        });
        innerButtonPanel.add(monitorButton);
        
        // Create bottom panel.
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Create override check box.
        overrideCheckbox = new JCheckBox("Override new mission creation");
        overrideCheckbox.setToolTipText("Prevents settlement inhabitants from starting new missions.");
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
		
		// Get all missions for the settlement.
		MissionManager manager = Simulation.instance().getMissionManager();
        List<Mission> missions = manager.getMissionsForSettlement(settlement);
        
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
}