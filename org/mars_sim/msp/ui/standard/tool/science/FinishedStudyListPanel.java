/**
 * Mars Simulation Project
 * FinishedStudyListPanel.java
 * @version 2.87 2009-10-12
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.science;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel showing a selectable list of finished scientific studies.
 */
public class FinishedStudyListPanel extends JPanel {

    // Data members
    private ScienceWindow scienceWindow;
    
    /**
     * Constructor
     * @param scienceWindow the science window.
     */
    FinishedStudyListPanel(ScienceWindow scienceWindow) {
        // Use JPanel constructor.
        super();
        
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Finished Scientific Studies", JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel listPane = new JPanel(new BorderLayout());
        add(listPane, BorderLayout.CENTER);
    }
}