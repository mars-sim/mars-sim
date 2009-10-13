/**
 * Mars Simulation Project
 * OngoingStudyListPanel.java
 * @version 2.87 2009-10-12
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.science;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel showing a selectable list of ongoing scientific studies.
 */
public class OngoingStudyListPanel extends JPanel {

    // Data members
    private ScienceWindow scienceWindow;
    
    /**
     * Constructor
     * @param scienceWindow the science window.
     */
    OngoingStudyListPanel(ScienceWindow scienceWindow) {
        // Use JPanel constructor.
        super();
        
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Ongoing Scientific Studies", JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel listPane = new JPanel(new BorderLayout());
        add(listPane, BorderLayout.CENTER);
    }
}