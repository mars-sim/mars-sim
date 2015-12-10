/**
 * Mars Simulation Project
 * LocationTabPanel.java
 * @version 3.07 2015-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
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

	// 2014-11-11 Added new panels and labels
	private JPanel tpPanel =  new JPanel();
	private JPanel outsideReadingPanel = new JPanel();
	private JPanel containerPanel = new JPanel();
	//private JLabel temperatureLabel;
	//private JLabel airPressureLabel;
	private JLabel locationLabel;
	private JLabel locLabel;
	//private Color THEME_COLOR = Color.ORANGE;
	//private double airPressureCache;
	//private int temperatureCache;
	private Unit containerCache, topContainerCache;

	private JPanel locationCoordsPanel;
	private JLabel latitudeLabel;
	private JLabel longitudeLabel;
	private JPanel locationLabelPanel;
	private JButton locationButton;

	private Coordinates locationCache;
	private JButton centerMapButton;

	DecimalFormat fmt = new DecimalFormat("##0");
	DecimalFormat fmt2 = new DecimalFormat("#0.00");
    /**
     * Constructor.
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public LocationTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(Msg.getString("LocationTabPanel.title"),
        		null,
        		Msg.getString("LocationTabPanel.tooltip"), unit, desktop);

        // Initialize location header.
		JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePane);

		JLabel titleLabel = new JLabel(Msg.getString("LocationTabPanel.title"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		titlePane.add(titleLabel);

        // Create location panel
        JPanel locationPanel = new JPanel(new BorderLayout(0,0));
        locationPanel.setBorder(new MarsPanelBorder());
       //locationPanel.setBorder(new EmptyBorder(5, 5, 5, 5) );

        topContentPanel.add(locationPanel);
        //topContentPanel.setBackground(THEME_COLOR);
        //locationPanel.setBackground(THEME_COLOR);

        // Create location label panel
        locationLabelPanel = new JPanel(new GridLayout(4,1,0,0)); // new JPanel(new FlowLayout(FlowLayout.CENTER));
        //locationLabelPanel.setBackground(THEME_COLOR);
        locationPanel.add(locationLabelPanel, BorderLayout.NORTH);

        // Prepare location coordinates panel
        locationCoordsPanel = new JPanel();
        //locationCoordsPanel.setBackground(THEME_COLOR);
        locationLabelPanel.add(locationCoordsPanel);
        //locationLabelPanel.setBackground(THEME_COLOR);
        locationCoordsPanel.setBorder(new EmptyBorder(5, 5, 5, 5) );
        locationCoordsPanel.setLayout(new BorderLayout(0, 0));

        // Initialize location cache
        locationCache = new Coordinates(unit.getCoordinates());

        // Prepare latitude label
        latitudeLabel = new JLabel("Latitude : " + getLatitudeString());
        latitudeLabel.setOpaque(false);
        latitudeLabel.setFont(new Font("Serif", Font.PLAIN, 13));
        latitudeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        locationCoordsPanel.add(latitudeLabel, BorderLayout.NORTH);

        // Prepare longitude label
        longitudeLabel = new JLabel("Longitude : " + getLongitudeString());
        longitudeLabel.setOpaque(false);
        longitudeLabel.setFont(new Font("Serif", Font.PLAIN, 13));
        longitudeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        locationCoordsPanel.add(longitudeLabel, BorderLayout.CENTER);

        JPanel gpsPane = new JPanel(new FlowLayout());
        locationLabelPanel.add(gpsPane);

        JLabel gpsLabel = new JLabel(Msg.getString("LocationTabPanel.gpslocator"), JLabel.CENTER); //$NON-NLS-1$
        gpsPane.add(gpsLabel);

        // Create center map button
        centerMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        centerMapButton.setOpaque(false);
        //centerMapButton.setBackground(THEME_COLOR);
        centerMapButton.setToolTipText("Locate a person/bot/object in a settlement/vehicle on Mars Navigator");

		JPanel locatorPane = new JPanel(new FlowLayout());
		locatorPane.add(centerMapButton);
		gpsPane.add(locatorPane);

        // 2015-12-09 Prepare loc label
        locLabel = new JLabel();
        //locLabel.setOpaque(false);
        locLabel.setFont(new Font("Serif", Font.PLAIN, 13));
        locLabel.setHorizontalAlignment(SwingConstants.CENTER);
		locationLabelPanel.add(locLabel);

/*

        // Create location button
        locationButton = new JButton();
        locationButton.setOpaque(false);
        //locationButton.setBackground(THEME_COLOR);
        locationButton.addActionListener(this);

        containerPanel.add(locationButton);



        // 2014-11-11 Set up tpPanel for outside temperature and pressure
        //tpPanel.setOpaque(false);
        //BorderLayout tpLayout = new BorderLayout(0, 0);
        //tpPanel.setLayout(tpLayout);
        //tpPanel.setBackground(THEME_COLOR);

        //TitledBorder tpTitle;
        //tpTitle = BorderFactory.createTitledBorder("Outside");
        //tpTitle.setTitleFont(new Font("Serif", Font.ITALIC, 9));
        //tpPanel.setBorder(tpTitle);
        //tpPanel.setBorder(BorderFactory.createEtchedBorder());

        // Prepare air pressure label
        airPressureLabel = new JLabel(getAirPressureString(getAirPressure()));
        airPressureLabel.setOpaque(false);
        airPressureLabel.setFont(new Font("Serif", Font.PLAIN, 13));
        airPressureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tpPanel.add(airPressureLabel, BorderLayout.CENTER);

        // Prepare temperature label
        temperatureLabel = new JLabel(getTemperatureString(getTemperature()));
        temperatureLabel.setOpaque(false);
        temperatureLabel.setFont(new Font("Serif", Font.PLAIN, 13));
        temperatureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tpPanel.add(temperatureLabel, BorderLayout.NORTH);

        locationLabelPanel.add(outsideReadingPanel);

        // Add the location button or outsideReadingPanel depending on the situation.
        Unit container = unit.getContainerUnit();
        if (container != null) {
            locationButton.setText(container.getName());
            //addContainerPanel();
        }
        else {
         	// 2014-10-22 Called new method checkOutsideReading()
        	//checkOutsideReading();
        	//addOutsideReadingPanel();
        }

        // initialize containerCache
        containerCache = unit.getContainerUnit();

*/
    }
/*
    // 2014-11-11 Modified temperature and pressure panel
    public String getTemperatureString(double value) {
    	// 2015-01-16 Used Msg.getString for the degree sign
    	// 2014-11-20 Changed from " °C" to " �C" for English Locale
    	return fmt.format(value) + " " + Msg.getString("temperature.sign.degreeCelsius"); //$NON-NLS-1$
    }

    public int getTemperature() {
		return (int) Simulation.instance().getMars().getWeather()
    			.getTemperature(unit.getCoordinates());
    }

    // 2014-11-07 Added temperature and pressure panel
    public String getAirPressureString(double value) {
    	return fmt2.format(value) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$
    }

    public double getAirPressure() {
    	return Math.round(Simulation.instance().getMars().getWeather()
	            .getAirPressure(unit.getCoordinates()) *100.0) / 100.0;
    }
*/
	private String getLatitudeString() {
		return locationCache.getFormattedLatitudeString();
	}

	private String getLongitudeString() {
		return locationCache.getFormattedLongitudeString();
	}


    /**
     * Check the type of unit and its location
     * Obtain temperature and pressure reading if the unit is outside the settlement
     *
     * @param calling setText to update the locationTextLabel

    // 2014-11-11 Overhauled checkOutsideReading()
	public void checkOutsideReading() {

	    // 2014-11-11 Added temperature and pressure panel
		double p = getAirPressure();
        if (airPressureCache != p) {
        	airPressureCache = p;
            airPressureLabel.setText(getAirPressureString(airPressureCache));
        }

        int t = getTemperature();
        if (temperatureCache != t) {
        	temperatureCache = t;
        	temperatureLabel.setText(getTemperatureString(temperatureCache));
        }

		//outsideReadingPanel.add(tpPanel);

		if (unit instanceof Settlement) {
			//Settlement settlement = (Settlement) unit;
			outsideReadingPanel.remove(tpPanel);
			outsideReadingPanel.add(tpPanel);
		}
	    if (unit instanceof Person) {

	        Person person = (Person) unit;
	        if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
				outsideReadingPanel.remove(tpPanel);
	        	outsideReadingPanel.add(tpPanel);
	        }
	        else if (person.getLocationSituation() == LocationSituation.BURIED) {
				outsideReadingPanel.remove(tpPanel);
				// 2014-11-29 Commented out remove(locationLabel) to avoid Exception
				//outsideReadingPanel.remove(locationLabel);
				outsideReadingPanel.add(locationLabel);
				// 2014-11-17 Fixed NullPointer Exception. setText after adding locationLabel;
				locationLabel.setText("Buried Outside");
			}
	        else { // the person is inside a settlement/vehicle
	        	outsideReadingPanel.remove(tpPanel);
	        	outsideReadingPanel.add(locationLabel);
	        }
	    }
	    if (unit instanceof Vehicle) {
        	Vehicle vehicle = (Vehicle) unit;
        	if (vehicle.getStatus() == "Moving" ||
        		vehicle.getStatus() == "Towed" ||
   	      		// TODO: what if vehicle is malfunction in the settlement, NOT during an excursion
        		vehicle.getStatus() == "Malfunction") {
    			outsideReadingPanel.remove(tpPanel);
				outsideReadingPanel.add(tpPanel);
        	}
        	else { // the vehicle should be in the settlement
            	outsideReadingPanel.remove(tpPanel);
	        	outsideReadingPanel.add(locationLabel);
        	}
	    }
	}
*/



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
    // 2014-11-11 Overhauled update()
    public void update() {

        // If unit's location has changed, update location display.
    	// TODO: if a person goes outside the settlement for servicing an equipment
    	// does the coordinate (down to how many decimal) change?
        if (!locationCache.equals(unit.getCoordinates())) {
            locationCache.setCoords(unit.getCoordinates());
            latitudeLabel.setText("Latitude : " + getLatitudeString());
            longitudeLabel.setText("Longitude : " + getLongitudeString());
        }


        // 2015-12-09 Prepare loc label
        // Update location button or location text label as necessary.
        Unit container = unit.getContainerUnit();
        //if (container == null && containerCache != null)
        //	containerCache = null;
        //else if (container != null) {
	        if (containerCache != container) {
	        	containerCache = container;
	        	updateLocation();
	        }
        //}

        Unit topContainer = unit.getTopContainerUnit();
        //if (topContainer == null && topContainerCache != null)
        //	topContainerCache = null;
        //else if (topContainer != null) {
	        if (topContainerCache != topContainer) {
	        	topContainerCache = topContainer;
	        	updateLocation();
	        }
        //}


        //else; // the unit's container has NOT changed

/*
		double p = getAirPressure();
        if (airPressureCache != p) {
        	airPressureCache = p;
            airPressureLabel.setText(getAirPressureString(airPressureCache));
        }

        int t = getTemperature();
        if (temperatureCache != t) {
        	temperatureCache = t;
        	temperatureLabel.setText(getTemperatureString(temperatureCache));
        }

   */

    }

    /**
     * Adds the location button to the location label panel if it isn't already on
     * there and removes the location text label if it's there.

    // 2014-11-11 Modified addContainerPanel()
    private void addContainerPanel() {
        try {
            Component lastComponent = locationLabelPanel.getComponent(3);
            if (lastComponent == outsideReadingPanel) {
                locationLabelPanel.remove(outsideReadingPanel);
                locationLabelPanel.add(containerPanel);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            locationLabelPanel.remove(outsideReadingPanel);
            locationLabelPanel.add(containerPanel);
        }
    }
*/
    /**
     * Adds the outsideReadingPanel if it isn't already on
     * there and removes the location button if it's there.

    // 2014-11-11 Modified addOutsideReadingPanel()
    private void addOutsideReadingPanel() {
        try {
            Component lastComponent = locationLabelPanel.getComponent(3);
            if (lastComponent == locationButton) {
                locationLabelPanel.remove(containerPanel);
                locationLabelPanel.add(outsideReadingPanel);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            locationLabelPanel.remove(containerPanel);
            locationLabelPanel.add(outsideReadingPanel);
        }
    }
    */

    /**
     * Tracks the location of a person/bot/vehicle/object
     */
    // 2015-12-09 Added updateLocation()
    public void updateLocation() {

    	String loc = null;

        // Case F or case G
        if (containerCache == null && topContainerCache == null) {
        	if (unit instanceof Person) {
        		Person p = (Person) unit;
        		if (p.getLocationSituation() == LocationSituation.OUTSIDE)
        			loc = " on a mission, stepped outside a vehicle at prescribed coordinates";
        		else if (p.getLocationSituation() == LocationSituation.BURIED)
    			loc = " buried outside " + p.getBuriedSettlement().getName();
        	}
        	else if (unit instanceof Robot) {
        		Robot r = (Robot) unit;
        		if (r.getLocationSituation() == LocationSituation.OUTSIDE)
        			loc = " on a mission, stepped outside a vehicle at prescribed coordinates";
        		else if (r.getLocationSituation() == LocationSituation.BURIED)
        			loc = " decommmissed ";// + r.getBuriedSettlement().getName();
        	}
        }

        // case B
        else if (containerCache == null && topContainerCache != null) {
        	loc = " near the premise of " + topContainerCache;
        }

        // case D
        //else if (topContainerCache == null && containerCache != null) {
        //	loc = " inside a vehicle on a mission outside";
        //}

        // Case A, Case C, Case D, or Case E
        else if (topContainerCache != null && containerCache != null) {
        	if (unit instanceof Person) {
        		Person p = (Person) unit;
        		if (p.getLocationSituation() == LocationSituation.IN_SETTLEMENT)
        			// case A
        			loc = " at " + p.getBuildingLocation().getNickName() + " in " + topContainerCache;
        		else if (p.getLocationSituation() == LocationSituation.IN_VEHICLE) {
         			if (p.getSettlement() != null)
        				// case C
               			loc = " in " + containerCache + " inside a garage";
        			else {
             			Vehicle vehicle = (Vehicle) unit.getContainerUnit();
            	     	// Note: a vehicle's container unit may be null if it's outside a settlement
            			Settlement settlement = (Settlement) vehicle.getContainerUnit();

        				if (settlement == null)
            				// case E
            				loc = " in " + containerCache + " on a mission outside";
        				else
        					// case D
        					//loc = " in a vehicle parked within the premise of a settlement";
        					loc = " in " + containerCache + " parked within the premise of " + settlement;
        			}
        		}
        	}
        	else if (unit instanceof Robot) {
        		Robot r = (Robot) unit;
        		if (r.getLocationSituation() == LocationSituation.IN_SETTLEMENT)
        			// case A
        			loc = " at " + r.getBuildingLocation().getNickName() + " in " + topContainerCache;
        		else if (r.getLocationSituation() == LocationSituation.IN_VEHICLE) {
         			if (r.getSettlement() != null)
        				// case C
               			loc = " in " + containerCache + " inside a garage";
        			else {
             			Vehicle vehicle = (Vehicle) unit.getContainerUnit();
            	     	// Note: a vehicle's container unit may be null if it's outside a settlement
            			Settlement settlement = (Settlement) vehicle.getContainerUnit();

        				if (settlement == null)
            				// case E
               				loc = " in " + containerCache + " on a mission outside";
        				else
        					// case D
        					//loc = " in a vehicle parked within the premise of a settlement";
        					loc = " in " + containerCache + " parked within the premise of " + settlement;
        			}
        		}
        	}
        }

        locLabel.setText("Last known :" + loc);
    }
}
