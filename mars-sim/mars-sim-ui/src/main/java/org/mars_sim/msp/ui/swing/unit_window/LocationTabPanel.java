/**
 * Mars Simulation Project
 * LocationTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** 
 * The LocationTabPanel is a tab panel for location information.
 */
public class LocationTabPanel extends TabPanel implements ActionListener {
    
    private JLabel latitudeLabel;
    private JLabel longitudeLabel;
    private Coordinates locationCache;
    private JButton centerMapButton;
    private JPanel locationLabelPanel;
    private JButton locationButton;
    private JLabel locationTextLabel;
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public LocationTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Location", null, "Location", unit, desktop);
        
        // Create location panel
        JPanel locationPanel = new JPanel(new BorderLayout());
        locationPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(locationPanel);
        
        // Create location label panel
        locationLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPanel.add(locationLabelPanel, BorderLayout.NORTH);
        
        // Create center map button
        centerMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        centerMapButton.setToolTipText("Locate in Mars Navigator");
        locationLabelPanel.add(centerMapButton);
        
        // Create location label
        JLabel locationLabel = new JLabel("Location: ", JLabel.CENTER);
        locationLabelPanel.add(locationLabel);
        
        // Create location button
        locationButton = new JButton();
        locationButton.addActionListener(this);
        
        // Create location text label
        locationTextLabel = new JLabel("", JLabel.LEFT);
        
        // Add the location button or location text label depending on the situation.
        Unit container = unit.getContainerUnit();
        if (container != null) {
            locationButton.setText(container.getName());
            addLocationButton();
        }
        else {
            locationTextLabel.setText("Outside");
            if (unit instanceof Person) {
                Person person = (Person) unit;
                if (person.getLocationSituation().equals(Person.BURIED)) 
                    locationTextLabel.setText("Buried Outside");
            }
            addLocationTextLabel();
        }   
        
        // Prepare location coordinates panel
        JPanel locationCoordsPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        locationPanel.add(locationCoordsPanel, "Center");

        // Initialize location cache
        locationCache = new Coordinates(unit.getCoordinates());
        
        // Prepare latitude label
        latitudeLabel = new JLabel("Latitude: " + locationCache.getFormattedLatitudeString(), JLabel.LEFT);
        locationCoordsPanel.add(latitudeLabel);

        // Prepare longitude label
        longitudeLabel = new JLabel("Longitude: " + locationCache.getFormattedLongitudeString(), JLabel.LEFT);
        locationCoordsPanel.add(longitudeLabel);
    }
    
    /** 
     * Action event occurs.
     *
     * @param event the action event
     */
    public void actionPerformed(ActionEvent event) {
        JComponent source = (JComponent) event.getSource();
        
        // If the center map button was pressed, update navigator tool.
        if (source == centerMapButton)
            desktop.centerMapGlobe(unit.getCoordinates());
            
        // If the location button was pressed, open the unit window.
        if (source == locationButton) 
            desktop.openUnitWindow(unit.getContainerUnit(), false);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        
        // If unit's location has changed, update location display.
        if (!locationCache.equals(unit.getCoordinates())) {
            locationCache.setCoords(unit.getCoordinates());
            latitudeLabel.setText("Latitude: " + locationCache.getFormattedLatitudeString());
            longitudeLabel.setText("Longitude: " + locationCache.getFormattedLongitudeString());
        }
        
        // Update location button or location text label as necessary.
        Unit container = unit.getContainerUnit();
        if (container != null) {
            locationButton.setText(container.getName());
            addLocationButton();
        }
        else {
            locationTextLabel.setText("Outside");
            if (unit instanceof Person) {
                Person person = (Person) unit;
                if (person.getLocationSituation().equals(Person.BURIED)) 
                    locationTextLabel.setText("Buried Outside");
            }
            addLocationTextLabel();
        }   
    }
    
    /**
     * Adds the location button to the location label panel if it isn't already on
     * there and removes the location text label if it's there.
     */
    private void addLocationButton() {
        try {
            Component lastComponent = locationLabelPanel.getComponent(2);
            if (lastComponent == locationTextLabel) {
                locationLabelPanel.remove(locationTextLabel);
                locationLabelPanel.add(locationButton);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            locationLabelPanel.add(locationButton);
        }
    }
    
    /**
     * Adds the location text label to the location label panel if it isn't already on
     * there and removes the location button if it's there.
     */
    private void addLocationTextLabel() {
        try {
            Component lastComponent = locationLabelPanel.getComponent(2); 
            if (lastComponent == locationButton) {
                locationLabelPanel.remove(locationButton);
                locationLabelPanel.add(locationTextLabel);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            locationLabelPanel.add(locationTextLabel);
        }
    }
}   
