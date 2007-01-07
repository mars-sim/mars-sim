/**
 * Mars Simulation Project
 * CrewTabPanel.java
 * @version 2.79 2006-07-01
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.ui.standard.ImageLoader;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.standard.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

/**
 * Tab panel displaying vehicle mission info.
 */
public class MissionTabPanel extends TabPanel {

	private JTextArea missionTextArea;
    private JTextArea missionPhaseTextArea;
    private DefaultListModel memberListModel;
    private JList memberList;
    private JButton missionButton;
    private JButton monitorButton;
    
	// Cache
	private String missionCache = "";
    private String missionPhaseCache = "";
    private PersonCollection memberCache;
	
    /**
     * Constructor
     * @param vehicle the vehicle.
     * @param desktop the main desktop.
     */
    public MissionTabPanel(Vehicle vehicle, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Mission", null, "Vehicle's Mission", vehicle, desktop);
        
        Mission mission = Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
        
        // Prepare mission top panel
        JPanel missionTopPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        missionTopPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(missionTopPanel);
        
        // Prepare mission panel
        JPanel missionPanel = new JPanel(new BorderLayout(0, 0));
        missionTopPanel.add(missionPanel);
        
        // Prepare mission title label.
        JLabel missionTitleLabel = new JLabel("Mission", JLabel.CENTER);
        missionPanel.add(missionTitleLabel, BorderLayout.NORTH);
        
        // Prepare mission text area
        if (mission != null) missionCache = mission.getDescription();
        missionTextArea = new JTextArea(2, 20);
        if (missionCache != null) missionTextArea.setText(missionCache);
        missionTextArea.setLineWrap(true);
        missionTextArea.setEditable(false);
        missionPanel.add(new JScrollPane(missionTextArea), BorderLayout.CENTER);
        
        // Prepare mission phase panel
        JPanel missionPhasePanel = new JPanel(new BorderLayout(0, 0));
        missionTopPanel.add(missionPhasePanel);
        
        // Prepare mission phase label
        JLabel missionPhaseLabel = new JLabel("Mission Phase", JLabel.CENTER);
        missionPhasePanel.add(missionPhaseLabel, BorderLayout.NORTH);
        
        // Prepare mission phase text area
        if (mission != null) missionPhaseCache = mission.getPhaseDescription();
        missionPhaseTextArea = new JTextArea(2, 20);
        if (missionPhaseCache != null) missionPhaseTextArea.setText(missionPhaseCache);
        missionPhaseTextArea.setLineWrap(true);
        missionPhaseTextArea.setEditable(false);
        missionPhasePanel.add(new JScrollPane(missionPhaseTextArea), BorderLayout.CENTER);
        
        // Prepare mission bottom panel
        JPanel missionBottomPanel = new JPanel(new BorderLayout(0, 0));
        missionBottomPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(missionBottomPanel);
        
        // Prepare member label
        JLabel memberLabel = new JLabel("Members", JLabel.CENTER);
        missionBottomPanel.add(memberLabel, BorderLayout.NORTH);
        
        // Prepare member list panel
        JPanel memberListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        memberListPanel.setBorder(new MarsPanelBorder());
        missionBottomPanel.add(memberListPanel, BorderLayout.SOUTH);
        
        // Create scroll panel for member list.
        JScrollPane memberScrollPanel = new JScrollPane();
        memberScrollPanel.setPreferredSize(new Dimension(175, 100));
        memberListPanel.add(memberScrollPanel);
        
        // Create member list model
        memberListModel = new DefaultListModel();
        if (mission != null) memberCache = new PersonCollection(mission.getPeople());
        else memberCache = new PersonCollection();
        PersonIterator i = memberCache.iterator();
        while (i.hasNext()) memberListModel.addElement(i.next());
        
        // Create member list
        memberList = new JList(memberListModel);
        // memberList.addMouseListener(this);
        memberList.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent arg0) {
        		// If double-click, open person dialog.
                if (arg0.getClickCount() >= 2) 
                    getDesktop().openUnitWindow((Person) memberList.getSelectedValue());
        	}
        });
        memberScrollPanel.setViewportView(memberList);
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        buttonPanel.setBorder(new MarsPanelBorder());
        memberListPanel.add(buttonPanel);
        
        // Create mission tool button
        JButton missionButton = new JButton(ImageLoader.getIcon("Mission"));
        missionButton.setMargin(new Insets(1, 1, 1, 1));
        missionButton.setToolTipText("Open mission in mission tool.");
        missionButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
                Vehicle vehicle = (Vehicle) unit;
                Mission mission = Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
                if (mission != null) {
                	((MissionWindow) getDesktop().getToolWindow(MissionWindow.NAME)).selectMission(mission);
                	getDesktop().openToolWindow(MissionWindow.NAME);
                }
        	}
        });
        missionButton.setEnabled(mission != null);
        buttonPanel.add(missionButton);
        
        // Create member monitor button
        JButton monitorButton = new JButton(ImageLoader.getIcon("Monitor"));
        monitorButton.setMargin(new Insets(1, 1, 1, 1));
        monitorButton.setToolTipText("Open tab in monitor tool for this mission.");
        monitorButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
                Vehicle vehicle = (Vehicle) unit;
                Mission mission = Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
                if (mission != null) getDesktop().addModel(new PersonTableModel(mission));
        	}
        });
        monitorButton.setEnabled(mission != null);
        buttonPanel.add(monitorButton);
    }
	
    /**
     * Updates the info on this panel.
     */
	public void update() {
		
		Vehicle vehicle = (Vehicle) unit;
		Mission mission = Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
		
		if (mission != null) missionCache = mission.getDescription();
		else missionCache = "";
		if (!missionCache.equals(missionTextArea.getText())) 
            missionTextArea.setText(missionCache);
		
		if (mission != null) missionPhaseCache = mission.getPhaseDescription();
		else missionPhaseCache = "";
		if (!missionPhaseCache.equals(missionPhaseTextArea.getText())) 
            missionPhaseTextArea.setText(missionPhaseCache);
		
        // Update member list
		PersonCollection tempCollection = null;
		if (mission != null) tempCollection = mission.getPeople();
		else tempCollection = new PersonCollection();
        if (!memberCache.equals(tempCollection)) {
            memberCache = new PersonCollection(tempCollection);
            memberListModel.clear();
            PersonIterator i = memberCache.iterator();
            while (i.hasNext()) memberListModel.addElement(i.next());
        }
        
        // Update mission and monitor buttons.
        missionButton.setEnabled(mission != null);
        monitorButton.setEnabled(mission != null);
	}
}