/**
 * Mars Simulation Project
 * AnnouncementWindow.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.ui.swing.tool.ResizedFontLabel;

import java.awt.*;

/**
 * The AnnouncementWindow class is an internal frame for displaying popup
 * announcements in the main desktop pane.
 */
public class AnnouncementWindow extends JInternalFrame {

	private static final long serialVersionUID = 7803343954911356522L;

	private ResizedFontLabel announcementLabel;

	private MainDesktopPane desktop;

	/**
	 * Constructor .
	 * 
	 * @param desktop
	 *            the main desktop pane.
	 */
	public AnnouncementWindow(MainDesktopPane desktop) {

		// Use JDialog constructor
		super("", false, false, false, false); //$NON-NLS-1$

		this.desktop = desktop;
		// Create the main panel
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(mainPane);

		mainPane.setSize(new Dimension(200, 80));
		announcementLabel = new ResizedFontLabel(" "); //$NON-NLS-1$
		announcementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		mainPane.add(announcementLabel, BorderLayout.CENTER);
		mainPane.setCursor(new Cursor(java.awt.Cursor.WAIT_CURSOR));

	}

	/**
	 * Sets the announcement text for the window.
	 * 
	 * @param announcement
	 *            the announcement text.
	 */
	public void setAnnouncement(String announcement) {

		announcementLabel.setText(announcement);

	}
}