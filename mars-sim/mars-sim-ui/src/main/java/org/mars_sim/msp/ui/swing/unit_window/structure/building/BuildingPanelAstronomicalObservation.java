/**
 * Mars Simulation Project
 * AstronomicalObservationBuildingPanel.java
 * @version 3.06 2014-01-29
 * @author Sebastien Venot
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for the astronomical observation building function.
 */
public class BuildingPanelAstronomicalObservation
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private int currentObserversAmount;
	private AstronomicalObservation function;
	private JLabel observersLabel;

	/**
	 * Constructor.
	 * @param observatory the astronomical observatory building function.
	 * @param desktop the main desktop.
	 */
	public BuildingPanelAstronomicalObservation(AstronomicalObservation observatory, 
			MainDesktopPane desktop) {
		// User BuildingFunctionPanel constructor.
		super(observatory.getBuilding(), desktop);

		// Set panel layout
		setLayout(new BorderLayout());

		function = observatory;

		currentObserversAmount = function.getObserverNum();

		// Prepare label panelAstronomicalObservation
		JPanel labelPanel = new JPanel(new GridLayout(4, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);

		// Astronomy top label
		JLabel astronomyLabel = new JLabel("Astronomy Observation", JLabel.CENTER);
		labelPanel.add(astronomyLabel);

		// Observer number label
		observersLabel = new JLabel();
		observersLabel.setHorizontalAlignment(JLabel.CENTER);
		update();
		labelPanel.add(observersLabel);

		// Observer capacityLabel
		JLabel observerCapacityLabel = new JLabel("Observer Capacity: " + 
				function.getObservatoryCapacity(), JLabel.CENTER);
		labelPanel.add(observerCapacityLabel);
	}

	@Override
	public void update() {
		if (currentObserversAmount != function.getObserverNum()) {
			currentObserversAmount = function.getObserverNum();
			observersLabel.setText("Number of Observers: " + currentObserversAmount);
		}
	}
}