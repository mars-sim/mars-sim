/**
 * Mars Simulation Project
 * ConstructionMissionCustomInfoPanel.java
 * @version 2.85 2008-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mars_sim.msp.simulation.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionEvent;
import org.mars_sim.msp.simulation.structure.construction.ConstructionEvent;
import org.mars_sim.msp.simulation.structure.construction.ConstructionListener;
import org.mars_sim.msp.simulation.structure.construction.ConstructionSite;
import org.mars_sim.msp.simulation.structure.construction.ConstructionStage;

/**
 * A panel for displaying construction custom mission information.
 */
public class ConstructionMissionCustomInfoPanel extends MissionCustomInfoPanel 
        implements ConstructionListener {

    // Data members.
    private BuildingConstructionMission mission;
    private ConstructionSite site;
    private JLabel stageLabel;
    private BoundedRangeModel progressBarModel;
    
    /**
     * Constructor
     */
    ConstructionMissionCustomInfoPanel() {
        // Use JPanel constructor.
        super();
        
        // Set layout.
        setLayout(new BorderLayout());
        
        JPanel contentsPanel = new JPanel(new GridLayout(3, 1));
        add(contentsPanel, BorderLayout.NORTH);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        contentsPanel.add(titlePanel);
        
        JLabel titleLabel = new JLabel("Construction Site");
        titlePanel.add(titleLabel);
        
        JPanel stagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentsPanel.add(stagePanel);
        
        stageLabel = new JLabel("Stage:");
        stagePanel.add(stageLabel);
        
        JPanel progressBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentsPanel.add(progressBarPanel);
        
        JProgressBar progressBar = new JProgressBar();
        progressBarModel = progressBar.getModel();
        progressBar.setStringPainted(true);
        progressBarPanel.add(progressBar);
    }
    
    @Override
    public void updateMission(Mission mission) {
        // Remove as construction listener if necessary.
        if (site != null) site.removeConstructionListener(this);
        
        if (mission instanceof BuildingConstructionMission) {
            this.mission = (BuildingConstructionMission) mission;
            site = this.mission.getConstructionSite();
            if (site != null) site.addConstructionListener(this);
            
            stageLabel.setText(getStageString());
            updateProgressBar();
        }
    }

    @Override
    public void updateMissionEvent(MissionEvent e) {
        stageLabel.setText(getStageString());
    }
    
    /**
     * Catch construction update event.
     * @param event the mission event.
     */
    public void constructionUpdate(ConstructionEvent event) {
        if (ConstructionStage.ADD_CONSTRUCTION_WORK_EVENT.equals(event.getType()))
            updateProgressBar();
    }
    
    /**
     * Gets the stage label string.
     * @return stage string.
     */
    private String getStageString() {
        StringBuffer stageString = new StringBuffer("Stage: ");
        if (mission != null) {
            ConstructionStage stage = mission.getConstructionStage();
            if (stage != null) stageString.append(stage.getInfo().getName());
        }
        
        return stageString.toString();
    }
    
    /**
     * Updates the progress bar.
     */
    private void updateProgressBar() {
        int workProgress = 0;
        if (mission != null) {
            ConstructionStage stage = mission.getConstructionStage();
            if (stage != null) {
                double completedWork = stage.getCompletedWorkTime();
                double requiredWork = stage.getInfo().getWorkTime();
                if (requiredWork > 0D) workProgress = (int) (100D * completedWork / requiredWork);
            }
        }
        progressBarModel.setValue(workProgress);
    }
}