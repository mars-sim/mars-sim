/**
 * Mars Simulation Project
 * LocationTabPanel.java
 * @version 3.07 2014-10-23
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;


import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/** 
 * The LocationTabPanel is a tab panel for location information.
 */
public class LocationTabPanel
extends TabPanel
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	 /** default logger.   */
	//private static Logger logger = Logger.getLogger(LocationTabPanel.class.getName());
	   
	private JPanel locationCoordsPanel;
	private JLabel latitudeLabel;
	private JLabel longitudeLabel;
	private JPanel locationLabelPanel;
	private JButton locationButton;
	
	private JLabel locationLabel;
	private JLabel locationTextLabel;
	
	private Coordinates locationCache;
	private JButton centerMapButton;

	DecimalFormat fmt = new DecimalFormat("###.#"); 
    /**
     * Constructor.
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public LocationTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Location", null, "Location", unit, desktop);
        
        // Create location panel
        JPanel locationPanel = new JPanel(new BorderLayout(0,0));
        locationPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(locationPanel);
        
        // Create location label panel
        locationLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        locationPanel.add(locationLabelPanel, BorderLayout.NORTH);
         
        // Prepare location coordinates panel
        locationCoordsPanel = new JPanel();
        locationLabelPanel.add(locationCoordsPanel);
        locationCoordsPanel.setLayout(new BorderLayout(0, 0));
        
        // Create center map button
        centerMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        centerMapButton.setToolTipText("Locate in Mars Navigator (center map on location)");
        locationLabelPanel.add(centerMapButton);

        locationLabel = new JLabel("@", JLabel.CENTER); 
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
         	// 2014-10-23 mkung: Called new method checkOutsideReading()
        	checkOutsideReading();

            addLocationTextLabel();
        }   

        // Initialize location cache
        locationCache = new Coordinates(unit.getCoordinates());
        
        // Prepare latitude label
        latitudeLabel = new JLabel(getLatitudeString());
        latitudeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        locationCoordsPanel.add(latitudeLabel, BorderLayout.NORTH);
        
        // Prepare longitude label
        longitudeLabel = new JLabel(getLongitudeString());
        longitudeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        locationCoordsPanel.add(longitudeLabel, BorderLayout.CENTER);
    }

    /** 
     * Check the type of unit and its location 
     * Obtain temperature and pressure reading if the unit is outside the settlement
     * 
     * @param calling setText to update the locationTextLabel
     */
 	// 2014-10-23 mkung: Created new method checkOutsideReading()
	public void checkOutsideReading() {   
		locationTextLabel.setText("Outside");
 
		if (unit instanceof Settlement) {
			Settlement settlement = (Settlement) unit;
			String outsideReading = getOutsideReading();	
				locationTextLabel.setText(outsideReading);
		}
        
        if (unit instanceof Person) {
            Person person = (Person) unit;           
			if (person.getLocationSituation() == LocationSituation.OUTSIDE) {			
				String outsideReading = getOutsideReading();	
				locationTextLabel.setText(outsideReading);
			}				
			if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
				locationTextLabel = new JLabel("In Vehicle", JLabel.LEFT);
			}
	        
			if (person.getLocationSituation() == LocationSituation.BURIED) 
				locationTextLabel.setText("Buried Outside");
        }
        
        if (unit instanceof Vehicle) {
        	Vehicle vehicle = (Vehicle) unit;
      		// TODO: is a vehicle in malfunction always outside or during an excursion
        	if (vehicle.getStatus() == "Moving" ||
        			vehicle.getStatus() == "Towed" ||
        			vehicle.getStatus() == "Malfunction") {      
				String outsideReading = getOutsideReading();
				locationTextLabel.setText(outsideReading);
        	}
        	
        }
	}

    /** 
     * Obtain the temperature and pressure reading if the unit is outside the settlement
     * 
     * @param return a string showing outside Temperature and Air Pressure
     */
 	// 2014-10-23 mkung: Created getOutsideReading() method
	public String getOutsideReading() {
	
		double outsideTemp = Simulation.instance().getMars().getWeather().getTemperature(unit.getCoordinates());
	    double outsideAirPressure = Simulation.instance().getMars().getWeather()
		            .getAirPressure(unit.getCoordinates());
		return fmt.format(outsideAirPressure) + " mbars    "
					+ fmt.format(outsideTemp) + " °C";
	
	}
	
	
	private String getLatitudeString() {
		return locationCache.getFormattedLatitudeString();
	}

	private String getLongitudeString() {
		return locationCache.getFormattedLongitudeString();
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
            latitudeLabel.setText(getLatitudeString());
            longitudeLabel.setText(getLongitudeString());
        }
        
        // Update location button or location text label as necessary.
        Unit container = unit.getContainerUnit();
        if (container != null) {
            locationButton.setText(container.getName());
            addLocationButton();
			locationLabel.setText("@");
        }
        else {
         	// 2014-10-23 mkung: Called new method checkOutsideReading()
        	checkOutsideReading();
			locationLabel.setText("");
            addLocationTextLabel();
        }   
    }
    
    /**
     * Adds the location button to the location label panel if it isn't already on
     * there and removes the location text label if it's there.
     */
    private void addLocationButton() {
        try {
            Component lastComponent = locationLabelPanel.getComponent(3);
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
            Component lastComponent = locationLabelPanel.getComponent(3); 
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
