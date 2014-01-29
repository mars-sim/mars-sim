/**
 * Mars Simulation Project
 * StudyDetailPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.science;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

/**
 * A panel showing details of a selected scientific study.
 */
public class StudyDetailPanel extends JPanel {

    // Data members
    private JLabel scienceFieldLabel;
    private JLabel levelLabel;
    private JLabel statusLabel;
    private ResearcherPanel primaryResearcherPane;
    private ResearcherPanel[] collabResearcherPanes;
    private ScientificStudy study;
    
    /**
     * Constructor
     */
    StudyDetailPanel(ScienceWindow scienceWindow) {
        // Use JPanel constructor.
        super();
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(425, -1));
        
        JLabel titleLabel = new JLabel("Study Details", JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        Box mainPane = Box.createVerticalBox();
        mainPane.setBorder(new MarsPanelBorder());
        add(mainPane, BorderLayout.CENTER);
        
        JPanel infoPane = new JPanel(new GridLayout(3, 1, 0, 0));
        infoPane.setBorder(new MarsPanelBorder());
        infoPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPane.add(infoPane);
        
        scienceFieldLabel = new JLabel("Study Science: ", JLabel.LEFT);
        infoPane.add(scienceFieldLabel);
        
        levelLabel = new JLabel("Difficulty Level: ", JLabel.LEFT);
        infoPane.add(levelLabel);
        
        statusLabel = new JLabel("Status: ", JLabel.LEFT);
        infoPane.add(statusLabel);
        
        primaryResearcherPane = new ResearcherPanel(scienceWindow);
        primaryResearcherPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPane.add(primaryResearcherPane);
        
        collabResearcherPanes = new ResearcherPanel[3];
        for (int x = 0; x < 3; x++) {
            collabResearcherPanes[x] = new ResearcherPanel(scienceWindow);
            collabResearcherPanes[x].setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPane.add(collabResearcherPanes[x]);
        }
        
        // Add a vertical glue.
        mainPane.add(Box.createVerticalGlue());
    }
    
    /**
     * Updates the panel.
     */
    void update() {
        if (study != null) {
            // Update the status label.
            statusLabel.setText("Status: " + getStatusString(study));
            
            // Update any changes to the displayed collaborative researcher panels.
            Iterator<Person> i = study.getCollaborativeResearchers().keySet().iterator();
            int count = 0;
            while (i.hasNext()) {
                Person researcher = i.next();
                if (!researcher.equals(collabResearcherPanes[count].getStudyResearcher()))
                    collabResearcherPanes[count].setStudyResearcher(study, researcher);
                count++;
            }
            for (int x = count; x < collabResearcherPanes.length; x++) {
                if (collabResearcherPanes[x].getStudyResearcher() != null)
                    collabResearcherPanes[x].setStudyResearcher(null, null);
            }
            
            // Update all researcher panels.
            primaryResearcherPane.update();
            for (ResearcherPanel collabResearcherPane : collabResearcherPanes) collabResearcherPane.update();
        }
    }
    
    /**
     * Display information about a scientific study.
     * @param study the scientific study.
     */
    void displayScientificStudy(ScientificStudy study) {
        this.study = study;
        
        if (study != null) {
            scienceFieldLabel.setText("Study Science: " + study.getScience().getName());
            levelLabel.setText("Difficulty Level: " + study.getDifficultyLevel());
            statusLabel.setText("Status: " + getStatusString(study));
            
            primaryResearcherPane.setStudyResearcher(study, study.getPrimaryResearcher());
            Iterator<Person> i = study.getCollaborativeResearchers().keySet().iterator();
            int count = 0;
            while (i.hasNext()) {
                collabResearcherPanes[count].setStudyResearcher(study, i.next());
                count++;
            }
            for (int x = count; x < collabResearcherPanes.length; x++)
                collabResearcherPanes[x].setStudyResearcher(null, null);
        }
        else {
            clearLabels();
            clearResearcherPanels();
        }
    }
    
    /**
     * Clear all labels.
     */
    private void clearLabels() {
        scienceFieldLabel.setText("Study Science: ");
        levelLabel.setText("Difficulty Level: ");
        statusLabel.setText("Status: ");
    }
    
    /**
     * Clear all researcher panels.
     */
    private void clearResearcherPanels() {
        primaryResearcherPane.setStudyResearcher(null, null);
        for (ResearcherPanel collabResearcherPane : collabResearcherPanes)
            collabResearcherPane.setStudyResearcher(null, null);
    }
    
    /**
     * Get the status string for a scientific study.
     * @param study the scientific study.
     * @return the status string.
     */
    private String getStatusString(ScientificStudy study) {
        String result = "";
        
        if (study != null) {
            if (!study.isCompleted()) result = study.getPhase();
            else result = study.getCompletionState();
        }
        
        return result;
    }
}