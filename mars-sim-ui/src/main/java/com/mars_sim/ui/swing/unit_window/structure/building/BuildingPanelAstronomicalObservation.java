/*
 * Mars Simulation Project
 * BuildingPanelAstronomicalObservation.java
 * @date 2022-07-09
 * @author Sebastien Venot
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.AstronomicalObservation;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.astroarts.OrbitViewer;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * A panel for the astronomical observation building function.
 */
@SuppressWarnings("serial")
class BuildingPanelAstronomicalObservation extends EntityTabPanel<Building> {
	
	private static final String TELESCOPE_ICON = "astro";
	
	private int currentObserversAmount;

	private JLabel observersLabel;

	private AstronomicalObservation function;

	/**
	 * Constructor.
	 * 
	 * @param observatory the astronomical observatory building function.
	 * @param context the UI context
	 */
	public BuildingPanelAstronomicalObservation(
			AstronomicalObservation observatory, 
			UIContext context) {
		
		// User BuildingFunctionPanel constructor.
		super(
			Msg.getString("BuildingPanelAstronomicalObservation.title"), 
			ImageLoader.getIconByName(TELESCOPE_ICON), null,
			context, observatory.getBuilding()
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

		starMap.addActionListener(e -> getContext().openToolWindow(OrbitViewer.NAME));
		buttonPane.add(starMap);
		center.add(buttonPane, BorderLayout.CENTER);
	}

	@Override
	public void refreshUI() {
		if (currentObserversAmount != function.getObserverNum()) {
			currentObserversAmount = function.getObserverNum();
			observersLabel.setText(Integer.toString(currentObserversAmount));
		}
	}
}
