/*
 * Mars Simulation Project
 * BuildingPanelAstronomicalObservation.java
 * @date 2022-07-09
 * @author Sebastien Venot
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.ui.astroarts.OrbitViewer;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * A panel for the astronomical observation building function.
 */
@SuppressWarnings("serial")
public class BuildingPanelAstronomicalObservation
extends BuildingFunctionPanel {
	
	private static final String TELESCOPE_ICON = "astro";

	// Data members
	private int currentObserversAmount;

	private JLabel observersLabel;

	private AstronomicalObservation function;

	/**
	 * Constructor.
	 * 
	 * @param observatory the astronomical observatory building function.
	 * @param desktop the main desktop.
	 */
	public BuildingPanelAstronomicalObservation(
			AstronomicalObservation observatory, 
			MainDesktopPane desktop) {
		
		// User BuildingFunctionPanel constructor.
		super(
			Msg.getString("BuildingPanelAstronomicalObservation.title"), 
			ImageLoader.getIconByName(TELESCOPE_ICON), 
			observatory.getBuilding(), 
			desktop
		);

		function = observatory;
		currentObserversAmount = function.getObserverNum();
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
		
		// Prepare label panelAstronomicalObservation
		AttributePanel labelPanel = new AttributePanel(2);
		center.add(labelPanel, BorderLayout.NORTH);

		// Observer number label
		observersLabel = labelPanel.addTextField( Msg.getString("BuildingPanelAstronomicalObservation.numberOfObservers"),
									  Integer.toString(currentObserversAmount), null);

		// Observer capacityLabel
		labelPanel.addTextField(Msg.getString("BuildingPanelAstronomicalObservation.observerCapacity"),
					 					Integer.toString(function.getObservatoryCapacity()), null);
		
      	// Create the button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		// Create the orbit viewer button.
		JButton starMap = new JButton();
		starMap.setIcon(ImageLoader.getIconByName(OrbitViewer.ICON));
		starMap.setToolTipText("Open the Orbit Viewer");

		starMap.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getDesktop().openToolWindow(OrbitViewer.NAME);
				}
			});
		buttonPane.add(starMap);
		center.add(buttonPane, BorderLayout.CENTER);
	}

	@Override
	public void update() {
		if (currentObserversAmount != function.getObserverNum()) {
			currentObserversAmount = function.getObserverNum();
			observersLabel.setText(Integer.toString(currentObserversAmount));
		}
	}
}
