/**
 * Mars Simulation Project
 * LocationTabPanel.java
 * @version 2.75 2003-05-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.ui.standard.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** 
 * The LocationTabPanel is a tab panel for location information.
 */
public class LocationTabPanel extends TabPanel implements ActionListener {
    
    private JLabel latitudeLabel;
    private JLabel longitudeLabel;
    private Coordinates locationCache;
    
    /**
     * Constructor
     *
     * @param proxy the UI proxy for the unit.
     * @param desktop the main desktop.
     */
    public LocationTabPanel(UnitUIProxy proxy, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Location", null, "Location of the settlement", proxy, desktop);
        
        // Create location panel
        JPanel locationPanel = new JPanel(new BorderLayout());
        locationPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(locationPanel);
        
        // Create location label panel
        JPanel locationLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPanel.add(locationLabelPanel, BorderLayout.NORTH);
        
        // Create center map button
        JButton centerMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        locationLabelPanel.add(centerMapButton);
        
        // Create location label
        JLabel locationLabel = new JLabel("Location: ", JLabel.CENTER);
        locationLabelPanel.add(locationLabel);
        
        // Prepare location coordinates panel
        JPanel locationCoordsPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        locationPanel.add(locationCoordsPanel, "Center");

        // Initialize location cache
        locationCache = new Coordinates(proxy.getUnit().getCoordinates());
        
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
        // If the center map button was pressed, update navigator tool.
        desktop.centerMapGlobe(proxy.getUnit().getCoordinates());
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        // If unit's location has changed, update location display.
        if (!locationCache.equals(proxy.getUnit().getCoordinates())) {
            locationCache.setCoords(proxy.getUnit().getCoordinates());
            latitudeLabel.setText("Latitude: " + locationCache.getFormattedLatitudeString());
            longitudeLabel.setText("Longitude: " + locationCache.getFormattedLongitudeString());
        }
    }
}   
