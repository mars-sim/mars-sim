/**
 * Mars Simulation Project
 * DeathTabPanel.java
 * @version 2.75 2003-07-16
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.person;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.medical.*;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

/** 
 * The DeathTabPanel is a tab panel with info about a person's death.
 */
public class DeathTabPanel extends TabPanel implements ActionListener {
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public DeathTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Death", null, "Death Info", unit, desktop);
        
        Person person = (Person) unit;
        PhysicalCondition condition = person.getPhysicalCondition();
        DeathInfo death = condition.getDeathDetails();
        
        // Create death info label panel.
        JPanel deathInfoLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(deathInfoLabelPanel);
        
        // Prepare death info label
        JLabel deathInfoLabel = new JLabel("Death Info", JLabel.CENTER);
        deathInfoLabelPanel.add(deathInfoLabel);
        
        // Prepare death label panel
        JPanel deathLabelPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        deathLabelPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(deathLabelPanel, BorderLayout.NORTH);
        
        // Prepare cause label
        JLabel causeLabel = new JLabel("Cause: " + death.getIllness(), JLabel.LEFT);
        deathLabelPanel.add(causeLabel);
        
        // Prepare time label
        JLabel timeLabel = new JLabel("Time: " + death.getTimeOfDeath(), JLabel.LEFT);
        deathLabelPanel.add(timeLabel);
        
        // Prepare malfunction label
        JLabel malfunctionLabel = new JLabel("Malfunction (if any): " + death.getMalfunction(), JLabel.LEFT);
        deathLabelPanel.add(malfunctionLabel);
        
        // Prepare bottom content panel
        JPanel bottomContentPanel = new JPanel(new BorderLayout(0, 0));
        centerContentPanel.add(bottomContentPanel, BorderLayout.CENTER);
        
        // Prepare location panel
        JPanel locationPanel = new JPanel(new BorderLayout());
        locationPanel.setBorder(new MarsPanelBorder());
        bottomContentPanel.add(locationPanel, BorderLayout.NORTH);
        
        // Prepare location label panel
        JPanel locationLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPanel.add(locationLabelPanel, BorderLayout.NORTH);
        
        // Prepare center map button
        JButton centerMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        locationLabelPanel.add(centerMapButton);
        
        // Prepare location label
        JLabel locationLabel = new JLabel("Location: " + death.getLocationOfDeath(), JLabel.CENTER);
        locationLabelPanel.add(locationLabel);
        
        // Prepare location coordinates panel
        JPanel locationCoordsPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        locationPanel.add(locationCoordsPanel, "Center");

        // Initialize location cache
        Coordinates deathLocation = death.getLocationOfDeath();
        
        // Prepare latitude label
        JLabel latitudeLabel = new JLabel("Latitude: " + deathLocation.getFormattedLatitudeString(), JLabel.LEFT);
        locationCoordsPanel.add(latitudeLabel);

        // Prepare longitude label
        JLabel longitudeLabel = new JLabel("Longitude: " + deathLocation.getFormattedLongitudeString(), JLabel.LEFT);
        locationCoordsPanel.add(longitudeLabel);
        
        // Add empty panel
        bottomContentPanel.add(new JPanel(), BorderLayout.CENTER);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {}
    
    /** 
     * Action event occurs.
     *
     * @param event the action event
     */
    public void actionPerformed(ActionEvent event) {
        
        // Update navigator tool.
        desktop.centerMapGlobe(unit.getCoordinates());
    }
}
       
