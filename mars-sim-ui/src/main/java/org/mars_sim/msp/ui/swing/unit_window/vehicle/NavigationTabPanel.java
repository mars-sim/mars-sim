/*
 * Mars Simulation Project
 * NavigationTabPanel.java
 * @date 2022-08-24
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The NavigationTabPanel is a tab panel for a vehicle's navigation information.
 */
@SuppressWarnings("serial")
public class NavigationTabPanel extends TabPanel implements ActionListener {

    private static final Logger logger = Logger.getLogger(NavigationTabPanel.class.getName());
    
	private static final String NAV_ICON = "navigation";

    private JButton centerMapButton;
    private JButton destinationButton;
    
    private JLabel statusLabel;
    private JLabel beaconLabel;
    private JLabel speedLabel;
    private JLabel elevationLabel;
    private JLabel destinationLatitudeLabel;
    private JLabel destinationLongitudeLabel;
    private JLabel remainingDistanceLabel;
    private JLabel etaLabel;
    private JLabel pilotLabel;
    
    private JLabel destinationTextLabel;
    
    private JPanel destinationLabelPanel;
    
    private DirectionDisplayPanel directionDisplay;
    private TerrainDisplayPanel terrainDisplay;

    // Data cache
	/** Is UI constructed. */
    private boolean beaconCache;
    
    private double speedCache;
    private double elevationCache;
    private double remainingDistanceCache;
    
    private String destinationTextCache;
    private String etaCache;
    
    private String pilotCache;

	/** The Vehicle instance. */
	private Vehicle vehicle;
	
    private Coordinates destinationLocationCache;
    private Settlement destinationSettlementCache;

    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public NavigationTabPanel(Vehicle unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(
        	Msg.getString("NavigationTabPanel.title"), 
        	ImageLoader.getIconByName(NAV_ICON),
        	Msg.getString("NavigationTabPanel.title"), 
        	desktop
        );
	
        vehicle = unit;
	}

    @Override
    protected void buildUI(JPanel content) {
		
        // Prepare graphic display panel
        JPanel graphicDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        graphicDisplayPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        content.add(graphicDisplayPanel, BorderLayout.NORTH);

        // Prepare direction display panel
        JPanel directionDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
        directionDisplayPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        graphicDisplayPanel.add(directionDisplayPanel);

        // Prepare direction display
        directionDisplay = new DirectionDisplayPanel(vehicle);
        directionDisplay.setToolTipText("Compass for showing the direction of travel");
        directionDisplayPanel.add(directionDisplay);

        // If vehicle is a vehicle, prepare terrain display.
        JPanel terrainDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
        terrainDisplayPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        graphicDisplayPanel.add(terrainDisplayPanel);
        terrainDisplay = new TerrainDisplayPanel(vehicle);
        terrainDisplay.setToolTipText("Terrain indicator for showing elevation changes");
        terrainDisplayPanel.add(terrainDisplay);
   
		// Prepare the main panel for housing the driving  spring layout.
		JPanel mainPanel = new JPanel(new BorderLayout());
		content.add(mainPanel, BorderLayout.CENTER);	
		
		// Prepare the destination panel for housing the center map button, the destination header label, and the coordinates
		JPanel destinationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPanel.add(destinationPanel, BorderLayout.NORTH);

        // Prepare destination left panel
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        destinationPanel.add(leftPanel);
        
        // Prepare destination label panel
        destinationLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationLabelPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        destinationPanel.add(destinationLabelPanel, BorderLayout.NORTH);

        // Prepare center map button
		final Icon centerIcon = ImageLoader.getIconByName(NavigatorWindow.ICON);
		centerMapButton = new JButton(centerIcon); 
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        centerMapButton.setToolTipText("Locate the vehicle in Navigator Tool");
        leftPanel.add(centerMapButton);
        
        // Prepare destination label
        JLabel destinationLabel = new JLabel("Destination :", JLabel.RIGHT);
        leftPanel.add(destinationLabel);
        
        // Prepare destination button
        destinationButton = new JButton();
        destinationButton.addActionListener(this);

        // Prepare destination text label
        destinationTextLabel = new JLabel("", JLabel.LEFT);
        
        boolean hasDestination = false;

        Mission mission = vehicle.getMission();
        if (mission instanceof VehicleMission) {

            VehicleMission vehicleMission = (VehicleMission) mission;
            if (vehicleMission.isTravelling()) {
                hasDestination = true;
                NavPoint destinationPoint = vehicleMission.getCurrentDestination();
                destinationLocationCache = destinationPoint.getLocation();
                if (destinationPoint.isSettlementAtNavpoint()) {
                    // If destination is settlement, add destination button.
                    destinationSettlementCache = destinationPoint.getSettlement();
                    destinationButton.setText(destinationSettlementCache.getName());
                    destinationLabelPanel.add(destinationButton);
                }
                else {
                    // If destination is coordinates, add destination text label.
                    destinationTextCache = destinationPoint.getDescription();
                    destinationTextLabel.setText(destinationTextCache);
                    destinationLabelPanel.add(destinationTextLabel);
                }
            }
        }
        
        if (!hasDestination) {
            // If destination is none, add destination text label.
            destinationTextCache = "";
            destinationTextLabel.setText(destinationTextCache);
            destinationLabelPanel.add(destinationTextLabel);
        }

		// Prepare the top panel for housing the driving  spring layout.
		JPanel locPanel = new JPanel(new BorderLayout());
		mainPanel.add(locPanel, BorderLayout.CENTER);
		
		// Prepare the top panel using spring layout.
		AttributePanel destinationSpringPanel = new AttributePanel(9);
		locPanel.add(destinationSpringPanel, BorderLayout.NORTH);
        
        // Prepare destination latitude label.
        String latitudeString = "";
        if (destinationLocationCache != null) {
        	latitudeString = destinationLocationCache.getFormattedLatitudeString();
        }
        destinationLatitudeLabel = destinationSpringPanel.addTextField("Destination Latitude", latitudeString, null);

        // Prepare destination longitude label.
        String longitudeString = "";
        if (destinationLocationCache != null) longitudeString =
            destinationLocationCache.getFormattedLongitudeString();
        destinationLongitudeLabel = destinationSpringPanel.addTextField("Destination Longitude", longitudeString, null);

        // Prepare distance label.
        String distanceText;
		if ((mission instanceof VehicleMission) &&
                ((VehicleMission) mission).isTravelling()) {
        	try {
        		remainingDistanceCache = ((VehicleMission) mission).getTotalDistanceRemaining();
        	}
        	catch (Exception e) {
        		logger.log(Level.SEVERE,"Error getting estimated total remaining distance.");
        	}
        	distanceText = StyleManager.DECIMAL_KM.format(remainingDistanceCache);
        }
        else {
        	remainingDistanceCache = 0D;
        	distanceText = "";
        }
        remainingDistanceLabel = destinationSpringPanel.addTextField("Remaining Distance", distanceText, null);
 
        // Prepare ETA label.
        etaCache = "";
        if (mission instanceof VehicleMission vm) {
            MarsClock due = vm.getLegETA();
            if (due != null) {
                etaCache = due.toString();
            }
        }
        etaLabel = destinationSpringPanel.addTextField("ETA", etaCache, null);
        
        // Prepare status label
        statusLabel = destinationSpringPanel.addTextField("Status", vehicle.printStatusTypes(), null);
           
        // Prepare beacon label
        beaconCache = vehicle.isBeaconOn();
        String beaconString;
        if (beaconCache) beaconString = "On";
        else beaconString = "Off";
        beaconLabel = destinationSpringPanel.addTextField("Emergency Beacon", beaconString, null);

        // Prepare speed label
        speedCache = vehicle.getSpeed();
        speedLabel = destinationSpringPanel.addTextField("Speed", StyleManager.DECIMAL_KMH.format(speedCache), null);
        
        // Prepare elevation label for vehicle       	     
        elevationCache = vehicle.getElevation();
        elevationLabel = destinationSpringPanel.addTextField("Elevation", StyleManager.DECIMAL_KM.format(elevationCache), null);
    
        // Prepare driver button and add it if vehicle has driver.
        if (vehicle.getOperator() != null)
        	pilotCache = vehicle.getOperator().getName();
        else
        	pilotCache = "";
        
        pilotLabel = destinationSpringPanel.addTextField("Pilot", pilotCache, null);

    }

    /**
     * Updates the info on this panel.
     */
    @Override
    public void update() {

        // Update status label
        statusLabel.setText(vehicle.printStatusTypes());
      
        // Update beacon label
        if (beaconCache != vehicle.isBeaconOn()) {
        	beaconCache = vehicle.isBeaconOn();
        	if (beaconCache) beaconLabel.setText("On");
        	else beaconLabel.setText("Off");
        }

        // Update speed label
        if (speedCache != vehicle.getSpeed()) {
            speedCache = vehicle.getSpeed();
            speedLabel.setText(StyleManager.DECIMAL_KMH.format(speedCache));
        }

        // Update elevation label.
        double currentElevation = vehicle.getElevation();
        if (elevationCache != currentElevation) {
            elevationCache = currentElevation;
            elevationLabel.setText(StyleManager.DECIMAL_KM.format(elevationCache));
        }


        // Update pilot label.
        String pilot = "";
        if (vehicle.getOperator() != null)
        	pilot = vehicle.getOperator().getName();
 
        if (!pilotCache.equals(pilot)) {
        	pilotCache = pilot;
            pilotLabel.setText(pilot);
        }
        
        Mission mission = vehicle.getMission();
        
        boolean hasDestination = false;
        		
        if ((mission instanceof VehicleMission)
                && ((VehicleMission) mission).isTravelling()) {
        	NavPoint destinationPoint = ((VehicleMission) mission).getCurrentDestination();
        	
        	hasDestination = true;
        	
        	if (destinationPoint.isSettlementAtNavpoint()) {
        		// If destination is settlement, update destination button.
        		if (destinationSettlementCache != destinationPoint.getSettlement()) {
        			destinationSettlementCache = destinationPoint.getSettlement();
        			destinationButton.setText(destinationSettlementCache.getName());
        			addDestinationButton();
        			destinationTextCache = "";
        		}
        	}
        	else {
        			// If destination is coordinates, update destination text label.
        			destinationTextCache = destinationPoint.getDescription();
        			destinationTextLabel.setText(destinationTextCache);
        			addDestinationTextLabel();
                    destinationSettlementCache = null;
        	}
        }
        
        if (!hasDestination) {
          	// If destination is none, update destination text label.
        	if (!destinationTextCache.equals("")) {
        		destinationTextCache = "";
        		destinationTextLabel.setText(destinationTextCache);
        		addDestinationTextLabel();
        		destinationSettlementCache = null;
        	}
        }
        

        // Update latitude and longitude panels if necessary.
        if ((mission instanceof VehicleMission)
                && ((VehicleMission) mission).isTravelling()) {
            VehicleMission vehicleMission = (VehicleMission) mission;
        	destinationLocationCache = vehicleMission.getCurrentDestination().getLocation();
            destinationLatitudeLabel.setText("" +
                    destinationLocationCache.getFormattedLatitudeString());
            destinationLongitudeLabel.setText("" +
                    destinationLocationCache.getFormattedLongitudeString());
        }
        else {
        	if (destinationLocationCache != null) {
        		destinationLocationCache = null;
                destinationLatitudeLabel.setText("");
                destinationLongitudeLabel.setText("");
        	}
        }

        // Update distance to destination if necessary.
        if (mission instanceof VehicleMission vehicleMission) {
            double remaining = vehicleMission.getTotalDistanceRemaining();
            if (remainingDistanceCache != remaining) {
                remainingDistanceCache = remaining;
                remainingDistanceLabel.setText(StyleManager.DECIMAL_KM.format(remainingDistanceCache));
            }

            MarsClock newETA = vehicleMission.getLegETA();
            if (newETA != null) {
                String newText = newETA.toString();
                if (!etaCache.equals(newText)) {
                    etaCache = newText;
                    etaLabel.setText(etaCache);
                }
            }
        }
        else {
        	remainingDistanceCache = 0D;
        	remainingDistanceLabel.setText("");
            etaCache = "";
        	etaLabel.setText("");
        }

        // Update direction display
        directionDisplay.update();

        // Update terrain display
        terrainDisplay.update();
    }

    /**
     * Adds a destination button if it isn't there and removes the destination text label.
     */
    private void addDestinationButton() {
        try {
            Component lastComponent = destinationLabelPanel.getComponent(2);
            if (lastComponent == destinationTextLabel) {
                destinationLabelPanel.remove(destinationTextLabel);
                destinationLabelPanel.add(destinationButton);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            destinationLabelPanel.add(destinationButton);
        }
    }

    /**
     * Adds a destination text label if it isn't there and removes the destination button.
     */
    private void addDestinationTextLabel() {
        try {
            Component lastComponent = destinationLabelPanel.getComponent(2);
            if (lastComponent == destinationButton) {
                destinationLabelPanel.remove(destinationButton);
                destinationLabelPanel.add(destinationTextLabel);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            destinationLabelPanel.add(destinationTextLabel);
        }
    }

    /**
     * Action event occurs.
     *
     * @param event the action event
     */
    public void actionPerformed(ActionEvent event) {
        JComponent source = (JComponent) event.getSource();
        MainDesktopPane desktop = getDesktop();
        
        // If center map button is pressed, center navigator tool
        // at destination location.
        if (source == centerMapButton) {
        	if (destinationLocationCache != null)
        		desktop.centerMapGlobe(destinationLocationCache);
        }

        // If destination settlement button is pressed, open window for settlement.
        if (source == destinationButton) 
        	desktop.showDetails(destinationSettlementCache);
    }
    
    @Override
	public void destroy() {
    	super.destroy();
 
	    statusLabel = null; 
	    beaconLabel = null; 
	    speedLabel = null; 
	    elevationLabel = null; 
	    centerMapButton = null; 
	    destinationButton = null; 
	    destinationTextLabel = null; 
	    destinationLabelPanel = null; 
	    destinationLatitudeLabel = null; 
	    destinationLongitudeLabel = null; 
	    remainingDistanceLabel = null; 
	    etaLabel = null; 
	    pilotLabel = null;
	    directionDisplay = null; 
	    terrainDisplay = null; 
	    destinationLocationCache = null; 
	    destinationSettlementCache = null; 
	}
	
    
}
