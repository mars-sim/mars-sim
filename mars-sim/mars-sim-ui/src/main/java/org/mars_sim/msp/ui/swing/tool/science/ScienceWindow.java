/**
 * Mars Simulation Project
 * ScienceWindow.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.science;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Window for the science tool.
 */
public class ScienceWindow extends ToolWindow {

    // Tool name
    public static final String NAME = "Science Tool";
    
    // Data members
    private OngoingStudyListPanel ongoingStudyListPane;
    private FinishedStudyListPanel finishedStudyListPane;
    private StudyDetailPanel studyDetailPane;
    private ScientificStudy selectedStudy;
    
    /**
     * Constructor
     * @param desktop the main desktop panel.
     */
    public ScienceWindow(MainDesktopPane desktop) {
        
        // Use ToolWindow constructor
        super(NAME, desktop);
        
        selectedStudy = null;
        
        // Create content panel.
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(MainDesktopPane.newEmptyBorder());
        setContentPane(mainPane);
        
        // Create lists panel.
        JPanel listsPane = new JPanel(new GridLayout(2, 1));
        mainPane.add(listsPane, BorderLayout.WEST);
        
        // Create ongoing study list panel.
        ongoingStudyListPane = new OngoingStudyListPanel(this);
        listsPane.add(ongoingStudyListPane);
        
        // Create finished study list panel.
        finishedStudyListPane = new FinishedStudyListPanel(this);
        listsPane.add(finishedStudyListPane);
        
        // Create study detail panel.
        studyDetailPane = new StudyDetailPanel(this);
        mainPane.add(studyDetailPane, BorderLayout.CENTER);
        
        // Pack window.
        pack();
    }
    
    /**
     * Sets the scientific study to display in the science window.
     * @param study the scientific study to display.
     */
    public void setScientificStudy(ScientificStudy study) {
        selectedStudy = study;
        studyDetailPane.displayScientificStudy(study);
        ongoingStudyListPane.selectScientificStudy(study, true);
        finishedStudyListPane.selectScientificStudy(study, true);
    }
    
    /**
     * Gets the displayed scientific study.
     * @return study or null if none displayed.
     */
    public ScientificStudy getScientificStudy() {
        return selectedStudy;
    }
    
    /**
     * Update the window.
     */
    public void update() {
        // Update all of the panels.
        ongoingStudyListPane.update();
        finishedStudyListPane.update();
        studyDetailPane.update();
    }
    
    /**
     * Opens an info window for researcher.
     * @param researcher the researcher.
     */
    void openResearcherWindow(Person researcher) {
        desktop.openUnitWindow(researcher, false);
    }
}