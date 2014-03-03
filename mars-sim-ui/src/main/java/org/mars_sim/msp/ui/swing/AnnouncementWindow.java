/**
 * Mars Simulation Project
 * AnnouncementWindow.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** 
 * The AnnouncementWindow class is an internal frame for displaying popup announcements
 * in the main desktop pane.
 */
public class AnnouncementWindow
extends JInternalFrame {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private JLabel announcementLabel;

	/** 
	 * Constructor .
	 * @param desktop the main desktop pane.
	 */
	public AnnouncementWindow(MainDesktopPane desktop) {

		// Use JDialog constructor
		super("", false, false, false, false); //$NON-NLS-1$

		// Create the main panel
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.setBorder(new EmptyBorder(10, 20, 10, 20));
		setContentPane(mainPane);

		announcementLabel = new JLabel("", JLabel.CENTER); //$NON-NLS-1$
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