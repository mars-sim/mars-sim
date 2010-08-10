/**
 * Mars Simulation Project
 * AstronomicalObservationBuildingPanel.java
 * @version 3.00 2010-08-10
 * @author Sebastien Venot
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * A panel for the astronomical observation building function.
 */
public class AstronomicalObservationBuildingPanel extends BuildingFunctionPanel {

    // Data members
    private int currentObserversAmount;
    private AstronomicalObservation function;
    private JLabel observersLabel;

    /**
     * Constructor
     * @param observatory the astronomical observatory building function.
     * @param desktop the main desktop.
     */
    public AstronomicalObservationBuildingPanel(AstronomicalObservation observatory, 
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
        observersLabel = new JLabel("Number of Observers: " + currentObserversAmount, JLabel.CENTER);
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