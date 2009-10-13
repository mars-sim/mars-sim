/**
 * Mars Simulation Project
 * StudyDetailPanel.java
 * @version 2.87 2009-10-12
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.science;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel showing details of a selected scientific study.
 */
public class StudyDetailPanel extends JPanel {

    // Data members
    private ScienceWindow scienceWindow;
    
    /**
     * Constructor
     * @param scienceWindow the science window.
     */
    StudyDetailPanel(ScienceWindow scienceWindow) {
        // Use JPanel constructor.
        super();
        
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Study Details", JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel mainPane = new JPanel(new BorderLayout());
        add(mainPane, BorderLayout.CENTER);
    }
}