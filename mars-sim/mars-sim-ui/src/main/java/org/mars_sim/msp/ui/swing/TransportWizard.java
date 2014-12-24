/**
 * Mars Simulation Project
 * TransportWizard.java
 * @version 3.07 2014-12-23

 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.interplanetary.transport.TransportEvent;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** 
 * The TransportWizard class is an internal frame for building transport event.
 * 
 */
public class TransportWizard
extends JInternalFrame {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private JLabel announcementLabel;
	private BuildingManager mgr;
	private Building building;
	private MainDesktopPane desktop;
	/** 
	 * Constructor .
	 * @param desktop the main desktop pane.
	 */
	public TransportWizard(final MainDesktopPane desktop) {

		// Use JDialog constructor
		super("Transport Wizard", false, false, false, false); //$NON-NLS-1$

		this.desktop = desktop;
		
		Simulation.instance().getMasterClock().setPaused(false);
		
		//setTitle();
		// Create the main panel
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.setBorder(new EmptyBorder(10, 20, 10, 20));
		setContentPane(mainPane);

		announcementLabel = new JLabel("", JLabel.CENTER); //$NON-NLS-1$
		announcementLabel.setText("Do you like this location for the new building " + building + " ?");
		announcementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.add(announcementLabel, BorderLayout.NORTH);
		//mainPane.setCursor(new Cursor(java.awt.Cursor.WAIT_CURSOR));
		
		JPanel btnPane = new JPanel();
		mainPane.add(btnPane, BorderLayout.CENTER);
		
		JButton b1 = new JButton("Accept");
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mgr.getResupply().setUserAcceptance(true);
				desktop.getMainWindow().unpauseSimulation();
			}
		});
		btnPane.add(b1);
		
		/*
		JButton b2 = new JButton("More Time");
		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//BuildingManager manager = mgr;
				mgr.setPauseLonger(true);
			}
		});
		btnPane.add(b1);
		*/
		
		JButton b3 = new JButton("Next Location");
		b3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mgr.getResupply().setUserAcceptance(false);				
				desktop.getMainWindow().unpauseSimulation();
			}
		});
		btnPane.add(b3);
	}

	/**
	 * Sets the announcement text for the window.
	 * @param announcement the announcement text.
	 */
	//public void setAnnouncement(String announcement) {
	//	announcementLabel.setText(announcement);
	//}
	public void setup(BuildingManager mgr, Building building) {
		this.mgr = mgr;
		this.building = building;
	}
}