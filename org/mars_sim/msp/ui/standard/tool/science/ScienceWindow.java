/**
 * Mars Simulation Project
 * ScienceWindow.java
 * @version 2.87 2009-10-11
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.science;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;

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
    
    /**
     * Constructor
     * @param desktop the main desktop panel.
     */
    public ScienceWindow(MainDesktopPane desktop) {
        
        // Use ToolWindow constructor
        super(NAME, desktop);
        
        // Set window resizable to false.
        setResizable(false);
        
        // Create content panel.
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
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
     * Update the window.
     */
    public void update() {
        // Update all of the panels.
        ongoingStudyListPane.update();
        finishedStudyListPane.update();
        studyDetailPane.update();
    }
}