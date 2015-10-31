/**
 * Mars Simulation Project
 * AstronomicalObservationBuildingPanel.java
 * @version 3.07 2014-11-21
 * @author Sebastien Venot
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
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
		// 2014-11-21 Changed font type, size and color and label text
		JLabel astronomyLabel = new JLabel(Msg.getString("BuildingPanelAstronomicalObservation.title"), JLabel.CENTER); //$NON-NLS-1$
		astronomyLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//astronomyLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(astronomyLabel);

		// Observer number label
		// 2014-11-21 Fixed currentObserversAmount
		observersLabel = new JLabel(Msg.getString("BuildingPanelAstronomicalObservation.numberOfObservers", currentObserversAmount), JLabel.CENTER); //$NON-NLS-1$
		observersLabel.setHorizontalAlignment(JLabel.CENTER);
		update();
		labelPanel.add(observersLabel);

		// Observer capacityLabel
		JLabel observerCapacityLabel = new JLabel(
			Msg.getString(
				"BuildingPanelAstronomicalObservation.observerCapacity", //$NON-NLS-1$
				function.getObservatoryCapacity()
			),JLabel.CENTER
		);
		labelPanel.add(observerCapacityLabel);
		
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));
		astronomyLabel.setOpaque(false);
		astronomyLabel.setBackground(new Color(0,0,0,128));
		observersLabel.setOpaque(false);
		observersLabel.setBackground(new Color(0,0,0,128));
	}

	@Override
	public void update() {
		if (currentObserversAmount != function.getObserverNum()) {
			currentObserversAmount = function.getObserverNum();
			observersLabel.setText(
				Msg.getString(
					"BuildingPanelAstronomicalObservation.numberOfObservers", //$NON-NLS-1$
					currentObserversAmount
				)
			);
		}
	}
}