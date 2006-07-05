/**
 * Mars Simulation Project
 * ActivityTabPanel.java
 * @version 2.76 2004-06-14
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.person;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Mind;
import org.mars_sim.msp.simulation.person.ai.job.*;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.task.TaskManager;
import org.mars_sim.msp.simulation.person.medical.DeathInfo;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

/** 
 * The ActivityTabPanel is a tab panel for a person's current activities.
 */
public class ActivityTabPanel extends TabPanel implements ActionListener {
    
    private JTextArea taskTextArea;
    private JTextArea taskPhaseTextArea;
    private JTextArea missionTextArea;
    private JTextArea missionPhaseTextArea;
    private JLabel jobLabel;
    private JComboBox jobComboBox;
    private JButton monitorButton;
    
    // Data cache
    private String jobCache = "";
    private String taskCache = "";
    private String taskPhaseCache = "";
    private String missionCache = "";
    private String missionPhaseCache = "";
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public ActivityTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Activity", null, "Activity", unit, desktop);
        
        Person person = (Person) unit;
        Mind mind = person.getMind();
        boolean dead = person.getPhysicalCondition().isDead();
        DeathInfo deathInfo = person.getPhysicalCondition().getDeathDetails();
        
        // Prepare activity label panel
        JPanel activityLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(activityLabelPanel);
        
        // Prepare activity label
        JLabel activityLabel = new JLabel("Activity", JLabel.CENTER);
        activityLabelPanel.add(activityLabel);
        
        // Prepare job panel
        JPanel jobPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        jobPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(jobPanel);
        
		// Prepare job label
		jobLabel = new JLabel("Job: ", JLabel.CENTER);
		jobPanel.add(jobLabel);        
        
        // Prepare job combo box
		jobCache = mind.getJob().getName();
        JobManager jobManager = Simulation.instance().getJobManager();
        String[] jobNames = new String[jobManager.getJobs().size()];
        for (int x=0; x < jobManager.getJobs().size(); x++)
        	jobNames[x] = ((Job) jobManager.getJobs().get(x)).getName();
        jobComboBox = new JComboBox(jobNames);
        jobComboBox.setSelectedItem(jobCache);
        jobComboBox.addActionListener(this);
        jobPanel.add(jobComboBox);
        
        // Prepare activity panel
        JPanel activityPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        centerContentPanel.add(activityPanel);
        
        // Prepare task top panel
        JPanel taskTopPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        taskTopPanel.setBorder(new MarsPanelBorder());
        activityPanel.add(taskTopPanel);
        
        // Prepare task panel
        JPanel taskPanel = new JPanel(new BorderLayout(0, 0));
        taskTopPanel.add(taskPanel);
        
        // Prepare task label
        JLabel taskLabel = new JLabel("Task", JLabel.CENTER);
        taskPanel.add(taskLabel, BorderLayout.NORTH);
        
        // Prepare task text area
        if (dead) taskCache = deathInfo.getTask();
        else taskCache = mind.getTaskManager().getTaskDescription();
        taskTextArea = new JTextArea(2, 20);
        if (taskCache != null) taskTextArea.setText(taskCache);
        taskTextArea.setLineWrap(true);
        taskTextArea.setEditable(false);
        taskPanel.add(new JScrollPane(taskTextArea), BorderLayout.CENTER);
        
        // Prepare task phase panel
        JPanel taskPhasePanel = new JPanel(new BorderLayout(0, 0));
        taskTopPanel.add(taskPhasePanel);
        
        // Prepare task phase label
        JLabel taskPhaseLabel = new JLabel("Task Phase", JLabel.CENTER);
        taskPhasePanel.add(taskPhaseLabel, BorderLayout.NORTH);
        
        // Prepare task phase text area
        if (dead) taskPhaseCache = deathInfo.getTaskPhase();
        else taskPhaseCache = mind.getTaskManager().getPhase();
        taskPhaseTextArea = new JTextArea(2, 20);
        if (taskPhaseCache != null) taskPhaseTextArea.setText(taskPhaseCache);
        taskPhaseTextArea.setLineWrap(true);
        taskPhaseTextArea.setEditable(false);
        taskPhasePanel.add(new JScrollPane(taskPhaseTextArea), BorderLayout.CENTER);
        
        // Prepare mission top panel
        JPanel missionTopPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        missionTopPanel.setBorder(new MarsPanelBorder());
        activityPanel.add(missionTopPanel);
        
        // Prepare mission panel
        JPanel missionPanel = new JPanel(new BorderLayout(0, 0));
        missionTopPanel.add(missionPanel);
        
        // Prepare mission label
        JLabel missionLabel = new JLabel("Mission", JLabel.CENTER);
        missionPanel.add(missionLabel, BorderLayout.NORTH);
        
        // Prepare mission text area
        if (dead) missionCache = deathInfo.getMission();
        else if (mind.getMission() != null) missionCache = mind.getMission().getDescription();
        missionTextArea = new JTextArea(2, 20);
        if (missionCache != null) missionTextArea.setText(missionCache);
        missionTextArea.setLineWrap(true);
        missionTextArea.setEditable(false);
        missionPanel.add(new JScrollPane(missionTextArea), BorderLayout.CENTER);
        
        // Prepare mission monitor button
        monitorButton = new JButton(ImageLoader.getIcon("Monitor"));
        monitorButton.setMargin(new Insets(1, 1, 1, 1));
        monitorButton.setToolTipText("Open Monitor tab for this mission.");
        monitorButton.addActionListener(this);
        JPanel monitorButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        monitorButtonPanel.add(monitorButton);
        missionPanel.add(monitorButtonPanel, BorderLayout.EAST);
        
        // Prepare mission phase panel
        JPanel missionPhasePanel = new JPanel(new BorderLayout(0, 0));
        missionTopPanel.add(missionPhasePanel);
        
        // Prepare mission phase label
        JLabel missionPhaseLabel = new JLabel("Mission Phase", JLabel.CENTER);
        missionPhasePanel.add(missionPhaseLabel, BorderLayout.NORTH);
        
        // Prepare mission phase text area
        if (dead) missionPhaseCache = deathInfo.getMissionPhase();
        else if (mind.getMission() != null) missionPhaseCache = mind.getMission().getPhaseDescription();
        missionPhaseTextArea = new JTextArea(2, 20);
        if (missionPhaseCache != null) missionPhaseTextArea.setText(missionPhaseCache);
        missionPhaseTextArea.setLineWrap(true);
        missionPhaseTextArea.setEditable(false);
        missionPhasePanel.add(new JScrollPane(missionPhaseTextArea), BorderLayout.CENTER);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        
        Person person = (Person) unit;
        Mind mind = person.getMind();
        boolean dead = person.getPhysicalCondition().isDead();
        DeathInfo deathInfo = person.getPhysicalCondition().getDeathDetails();
        
        // Update job if necessary.
        if (dead) {
        	jobCache = deathInfo.getJob();
        	jobComboBox.setEnabled(false);
       	} 
        else jobCache = mind.getJob().getName();
        if (!jobCache.equals(jobComboBox.getSelectedItem())) jobComboBox.setSelectedItem(jobCache);
        
        TaskManager taskManager = null;
        Mission mission = null;
        if (!dead) {
            taskManager = mind.getTaskManager();
            if (mind.hasActiveMission()) mission = mind.getMission();
        }
        
        // Update task text area if necessary.
        if (dead) taskCache = deathInfo.getTask();
        else taskCache = taskManager.getTaskDescription();
        if (!taskCache.equals(taskTextArea.getText())) 
            taskTextArea.setText(taskCache);
        
        // Update task phase text area if necessary.
        if (dead) taskPhaseCache = deathInfo.getTaskPhase();
        else taskPhaseCache = taskManager.getPhase();
        if (!taskPhaseCache.equals(taskPhaseTextArea.getText())) 
            taskPhaseTextArea.setText(taskPhaseCache);
        
        // Update mission text area if necessary.
        if (dead) missionCache = deathInfo.getMission();
        else if (mission != null) missionCache = mission.getDescription();
        else missionCache = "";
        if (!missionCache.equals(missionTextArea.getText())) 
            missionTextArea.setText(missionCache);
        
        // Update mission phase text area if necessary.
        if (dead) missionPhaseCache = deathInfo.getMissionPhase();
        else if (mission != null) missionPhaseCache = mission.getPhaseDescription();
        else missionPhaseCache = "";
        if (!missionPhaseCache.equals(missionPhaseTextArea.getText())) 
            missionPhaseTextArea.setText(missionPhaseCache);
    }
    
    /** 
     * Action event occurs.
     *
     * @param event the action event
     */
    public void actionPerformed(ActionEvent event) {
    	Object source = event.getSource();
    	
    	if (source == monitorButton) {
        	Person person = (Person) unit;
        	if (!person.getPhysicalCondition().isDead()) {
            	Mind mind = person.getMind();
            	if (mind.hasActiveMission()) 
                	desktop.addModel(new PersonTableModel(mind.getMission()));
        	}
    	}
    	else if (source == jobComboBox) {
    		int jobIndex = jobComboBox.getSelectedIndex();
    		Job job = (Job) Simulation.instance().getJobManager().getJobs().get(jobIndex);
    		((Person) unit).getMind().setJob(job, true);
    	}
    }
}       