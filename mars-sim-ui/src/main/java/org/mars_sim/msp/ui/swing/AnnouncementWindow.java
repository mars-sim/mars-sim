/**
 * Mars Simulation Project
 * AnnouncementWindow.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.ui.swing.tool.ResizedFontLabel;
import org.mars_sim.msp.ui.swing.tool.WaitLayerUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** 
 * The AnnouncementWindow class is an internal frame for displaying popup announcements
 * in the main desktop pane.
 */
public class AnnouncementWindow
extends JInternalFrame {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private ResizedFontLabel announcementLabel;

	private MainDesktopPane desktop;
	/** 
	 * Constructor .
	 * @param desktop the main desktop pane.
	 */
	public AnnouncementWindow(MainDesktopPane desktop) {

		// Use JDialog constructor
		super("", false, false, false, false); //$NON-NLS-1$

		this.desktop = desktop;
		// Create the main panel
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.setBorder(new EmptyBorder(0,0,0,0));//10, 20, 10, 20));
		setContentPane(mainPane);

		mainPane.setSize(100, 40);
		announcementLabel = new ResizedFontLabel(" ");// JLabel.CENTER); //$NON-NLS-1$
		announcementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		mainPane.add(announcementLabel, BorderLayout.CENTER);
		mainPane.setCursor(new Cursor(java.awt.Cursor.WAIT_CURSOR));	
	    
	}

	
	/**
	 * Sets the announcement text for the window.
	 * @param announcement the announcement text.
	 */
	public void setAnnouncement(String announcement) {

		if (desktop.getMainScene() != null) {	
			if (announcement.contains("Saving") || announcement.contains("Autosaving")) {
				//desktop.getMainScene().showSavingStage();
			//else if (announcement.contains("Paused"))
			//	desktop.getMainScene().showPausedStage();
			} else if (announcement.contains("Loading"))
				//desktop.getMainScene().showLoadingStage();
				;
		}
		else {
			announcementLabel.setText(announcement);			
		}
		
/*		
		JPanel p = new JPanel();
		
	    WaitLayerUI waitLayerUI = new WaitLayerUI();
	    JLayer<JPanel> jlayer = new JLayer<JPanel>(desktop.getMainScene().get, waitLayerUI);
	    
	    
	    final Timer stopper = new Timer(4000, new ActionListener() {
	      public void actionPerformed(ActionEvent ae) {
	    	  waitLayerUI.stop();  	  
	      }
	    });
	    stopper.setRepeats(false);
	        
	    JFrame f = new JFrame("");	    
	
	    f.add (jlayer);
	    f.setSize(300, 200);
	    //f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
	    f.setLocationRelativeTo (null);
	    f.setVisible (true);
	    f.requestFocus();
	    f.toFront();
*/
	}
}