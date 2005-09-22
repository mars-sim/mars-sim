/**
 * Mars Simulation Project
 * AnnouncementWindow.java
 * @version 2.78 2005-09-21
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** 
 * The AnnouncementWindow class is an internal frame for displaying popup announcements
 * in the main desktop pane.
 */
public class AnnouncementWindow extends JInternalFrame {

	JLabel announcementLabel;
	
    /** 
     * Constructor 
     * @param desktop the main desktop pane.
     */
    public AnnouncementWindow(MainDesktopPane desktop) {

        // Use JDialog constructor
        super("", false, false, false, false);

        // Create the main panel
        JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(10, 20, 10, 20));
        setContentPane(mainPane);

		announcementLabel = new JLabel("", JLabel.CENTER);
        announcementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.add(announcementLabel, BorderLayout.CENTER);
    }
    
    /**
     * Sets the announcement text for the window.
     * @param announcement the announcement text.
     */
    public void setAnnouncement(String announcement) {
    	announcementLabel.setText(announcement);
    }
}