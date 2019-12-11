/**
 * Mars Simulation Project
 * NavigationTabPanel.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.TravelMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/**
 * The NavigationTabPanel is a tab panel for a vehicle's navigation information.
 */
@SuppressWarnings("serial")
public class NavigationTabPanel extends TabPanel implements ActionListener {

    private static Logger logger = Logger.getLogger(NavigationTabPanel.class.getName());

    private static DecimalFormat formatter = new DecimalFormat("0.0");
    
    private WebButton driverButton;
    private WebButton centerMapButton;
    private WebButton destinationButton;
    
    private WebLabel statusLabel;
    private WebLabel beaconLabel;
    private WebLabel speedLabel;
    private WebLabel elevationLabel;
    private WebLabel destinationLatitudeLabel;
    private WebLabel destinationLongitudeLabel;
    private WebLabel distanceLabel;
    private WebLabel etaLabel;
    private WebLabel destinationTextLabel;
    
    private JPanel destinationLabelPanel;
    
    private DirectionDisplayPanel directionDisplay;
    private TerrainDisplayPanel terrainDisplay;

    // Data cache
	/** Is UI constructed. */
	private boolean uiDone = false;
    private boolean beaconCache;
    
    private double speedCache;
    private double elevationCache;
    private double distanceCache;
    
    private String destinationTextCache;
    private String etaCache;
    
    private VehicleOperator driverCache;
    private List<StatusType> statusCache;

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
    public NavigationTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super("Navigation", null, "Navigation", unit, desktop);
	
        vehicle = (Vehicle) unit;

	}

	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;

		// Create tht title label.
		WebPanel titlePanel = new WebPanel(new FlowLayout());
		WebLabel titleLabel = new WebLabel(Msg.getString("NavigationTabPanel.title"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		titlePanel.add(titleLabel);
		topContentPanel.add(titlePanel);
		
        // Prepare graphic display panel
        WebPanel graphicDisplayPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
        graphicDisplayPanel.setBorder(new MarsPanelBorder());
        graphicDisplayPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        topContentPanel.add(graphicDisplayPanel);

        // Prepare direction display panel
        WebPanel directionDisplayPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
        directionDisplayPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        graphicDisplayPanel.add(directionDisplayPanel);

        // Prepare direction display
        directionDisplay = new DirectionDisplayPanel(vehicle);
        directionDisplay.setToolTipText("Compass for showing the direction of travel");
        directionDisplayPanel.add(directionDisplay);

        // If vehicle is a ground vehicle, prepare terrain display.
        if (vehicle instanceof GroundVehicle) {
            WebPanel terrainDisplayPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
            terrainDisplayPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
            graphicDisplayPanel.add(terrainDisplayPanel);
            terrainDisplay = new TerrainDisplayPanel((GroundVehicle) vehicle);
            terrainDisplay.setToolTipText("Terrain indicator for showing elevation changes");
            terrainDisplayPanel.add(terrainDisplay);
        }
        
   
		// Prepare the main panel for housing the driving  spring layout.
		WebPanel mainPanel = new WebPanel(new BorderLayout());
		topContentPanel.add(mainPanel);	
		
		// Prepare the destination panel for housing the center map button, the destination header label, and the coordinates
		WebPanel destinationPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
//		Border border = new MarsPanelBorder();
//		Border margin = new EmptyBorder(5,5,5,5);
//		destinationPanel.setBorder(new CompoundBorder(border, margin));
		mainPanel.add(destinationPanel, BorderLayout.NORTH);

        // Prepare destination left panel
        WebPanel leftPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
        destinationPanel.add(leftPanel);
        
        // Prepare destination label panel
        destinationLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationLabelPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        destinationPanel.add(destinationLabelPanel, BorderLayout.NORTH);

        // Prepare center map button
        centerMapButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.centerMap")));//ImageLoader.getIcon("CenterMap")); 
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        centerMapButton.setToolTipText("Locate the vehicle in Navigator Tool");
        leftPanel.add(centerMapButton);
        
        // Prepare destination label
        WebLabel destinationLabel = new WebLabel("Destination :", WebLabel.RIGHT);
        leftPanel.add(destinationLabel);
        
        // Prepare destination button
        destinationButton = new WebButton();
        destinationButton.addActionListener(this);

        // Prepare destination text label
        destinationTextLabel = new WebLabel("", WebLabel.LEFT);
        
        boolean hasDestination = false;

        Mission mission = missionManager.getMissionForVehicle(vehicle);
        if ((mission != null) && (mission instanceof VehicleMission)) {

            VehicleMission vehicleMission = (VehicleMission) mission;
            if (vehicleMission != null
            		&& vehicleMission.getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT)) {
                hasDestination = true;
                destinationLocationCache = vehicleMission.getNextNavpoint().getLocation();
                NavPoint destinationPoint = vehicleMission.getNextNavpoint();
                if (destinationPoint.isSettlementAtNavpoint()) {
                    // If destination is settlement, add destination button.
                    destinationSettlementCache = destinationPoint.getSettlement();
                    destinationButton.setText(destinationSettlementCache.getName());
                    destinationLabelPanel.add(destinationButton);
                }
                else {
                    // If destination is coordinates, add destination text label.
                    destinationTextCache = Conversion.capitalize(destinationPoint.getDescription());//"A Navpoint";
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
		WebPanel locPanel = new WebPanel(new BorderLayout());
		mainPanel.add(locPanel, BorderLayout.CENTER);
		
		// Prepare the top panel using spring layout.
		WebPanel destinationSpringPanel = new WebPanel(new SpringLayout());
		destinationSpringPanel.setBorder(new EmptyBorder(15, 5, 15, 5));
		locPanel.add(destinationSpringPanel, BorderLayout.NORTH);
		
        // Prepare latitude header label.
        WebLabel latitudeHeaderLabel = new WebLabel("     Destination Latitude :", WebLabel.RIGHT);
        destinationSpringPanel.add(latitudeHeaderLabel);
        
        // Prepare destination latitude label.
        String latitudeString = "";
        if (destinationLocationCache != null) latitudeString =
            destinationLocationCache.getFormattedLatitudeString();
        destinationLatitudeLabel = new WebLabel("" + latitudeString, WebLabel.LEFT);
        destinationSpringPanel.add(destinationLatitudeLabel);
        
        // Prepare longitude header label.
        WebLabel longitudeHeaderLabel = new WebLabel("Destination Longitude :", WebLabel.RIGHT);
        destinationSpringPanel.add(longitudeHeaderLabel);
        
        // Prepare destination longitude label.
        String longitudeString = "";
        if (destinationLocationCache != null) longitudeString =
            destinationLocationCache.getFormattedLongitudeString();
        destinationLongitudeLabel = new WebLabel("" + longitudeString, WebLabel.LEFT);
        destinationSpringPanel.add(destinationLongitudeLabel);

        // Prepare distance header label.
        WebLabel distanceHeaderLabel = new WebLabel("Remaining Distance :", WebLabel.RIGHT);
        destinationSpringPanel.add(distanceHeaderLabel);
        
        // Prepare distance label.
        if ((mission != null) && (mission instanceof VehicleMission) &&
                ((VehicleMission) mission).getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT)) {
        	try {
        		distanceCache = ((VehicleMission) mission).getCurrentLegRemainingDistance();
        	}
        	catch (Exception e) {
        		logger.log(Level.SEVERE,"Error getting current leg remaining distance.");
    			e.printStackTrace(System.err);
        	}
        	distanceLabel = new WebLabel(formatter.format(distanceCache) + " km", WebLabel.LEFT);
        }
        else {
        	distanceCache = 0D;
        	distanceLabel = new WebLabel("", WebLabel.LEFT);
        }
        destinationSpringPanel.add(distanceLabel);

        // Prepare ETA header label.
        WebLabel etaHeaderLabel = new WebLabel("ETA :", WebLabel.RIGHT);
        destinationSpringPanel.add(etaHeaderLabel);
        
        // Prepare ETA label.
        if ((mission != null) && (mission instanceof VehicleMission) &&
                (((VehicleMission) mission).getLegETA() != null)) {
            etaCache = ((VehicleMission) mission).getLegETA().toString();
        }
        else 
        	etaCache = "";
        
        etaLabel = new WebLabel("" + etaCache, WebLabel.LEFT);
        destinationSpringPanel.add(etaLabel);

        // Lay out the spring panel.
     	SpringUtilities.makeCompactGrid(destinationSpringPanel,
     		                                4, 2, //rows, cols
     		                               30, 10,        //initX, initY
    		                               10, 4);       //xPad, yPad
     	
    	// Prepare the driving spring layout.
		WebPanel drivingSpringPanel = new WebPanel(new SpringLayout());
//		drivingSpringPanel.setBorder(new MarsPanelBorder());
		drivingSpringPanel.setBorder(new EmptyBorder(15, 5, 15, 5));
		mainPanel.add(drivingSpringPanel, BorderLayout.SOUTH);  
        
        // Prepare status header label
        WebLabel statusHeaderLabel = new WebLabel("Status :", WebLabel.RIGHT);
        drivingSpringPanel.add(statusHeaderLabel);
        
        // Prepare status label
        statusCache = vehicle.getStatusTypes();
        statusLabel = new WebLabel("" + vehicle.printStatusTypes(), WebLabel.LEFT);
        drivingSpringPanel.add(statusLabel);
           
        // Prepare beacon header label
        WebLabel beaconHeaderLabel = new WebLabel("        Emergency Beacon :", WebLabel.RIGHT);
        drivingSpringPanel.add(beaconHeaderLabel);
        
        // Prepare beacon label
        beaconCache = vehicle.isBeaconOn();
        String beaconString;
        if (beaconCache) beaconString = "On";
        else beaconString = "Off";
        beaconLabel = new WebLabel("" + beaconString, WebLabel.LEFT);
        drivingSpringPanel.add(beaconLabel);

        // Prepare speed header label
        WebLabel speedHeaderLabel = new WebLabel("Speed :", WebLabel.RIGHT);
        drivingSpringPanel.add(speedHeaderLabel);
        
        // Prepare speed label
        speedCache = vehicle.getSpeed();
        speedLabel = new WebLabel(formatter.format(speedCache) + " km/h", WebLabel.LEFT);
        drivingSpringPanel.add(speedLabel);

        // Prepare elevation header label for ground vehicle
        WebLabel elevationHeaderLabel = new WebLabel("Elevation :", WebLabel.RIGHT);
        drivingSpringPanel.add(elevationHeaderLabel);
        
        // Prepare elevation label for ground vehicle
//        if (vehicle instanceof GroundVehicle) {
            GroundVehicle gVehicle = (GroundVehicle) vehicle;
            elevationCache = gVehicle.getElevation();
            elevationLabel = new WebLabel("" + formatter.format(elevationCache) +
                " km", WebLabel.LEFT);
            drivingSpringPanel.add(elevationLabel, BorderLayout.SOUTH);
//        }

        // Prepare driver label
        WebLabel driverLabel = new WebLabel("Driver :", WebLabel.RIGHT);
//            driverLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        drivingSpringPanel.add(driverLabel);
        
        // Prepare driver button and add it if vehicle has driver.
        driverCache = vehicle.getOperator();
        driverButton = new WebButton();
        driverButton.addActionListener(this);
        driverButton.setVisible(false);
        if (driverCache != null) {
            driverButton.setText(driverCache.getOperatorName());
            driverButton.setVisible(true);
        }
        
        // Prepare driver panel
        WebPanel driverPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        driverPanel.add(driverButton);
        drivingSpringPanel.add(driverPanel);
            
        // Lay out the spring panel.
     	SpringUtilities.makeCompactGrid(drivingSpringPanel,
     		                                5, 2, //rows, cols
     		                                30, 10,        //initX, initY
     		                                10, 4);       //xPad, yPad
    }

    /**
     * Updates the info on this panel.
     */
    public void update() {
		if (!uiDone)
			initializeUI();
		
        Vehicle vehicle = (Vehicle) unit;

        // Update driver button if necessary.
        boolean driverChange = false;
        if (driverCache == null) {
            if (vehicle.getOperator() != null) driverChange = true;
        }
        else if (!driverCache.equals(vehicle.getOperator())) driverChange = true;
        if (driverChange) {
            driverCache = vehicle.getOperator();
            if (driverCache == null) {
                driverButton.setVisible(false);
            }
            else {
                driverButton.setVisible(true);
                driverButton.setText(driverCache.getOperatorName());
            }
        }

        // Update status label
        if (!vehicle.sameStatusTypes(statusCache, vehicle.getStatusTypes())) {
            statusCache = vehicle.getStatusTypes();
            statusLabel.setText(vehicle.printStatusTypes());
        }

        // Update beacon label
        if (beaconCache != vehicle.isBeaconOn()) {
        	beaconCache = vehicle.isBeaconOn();
        	if (beaconCache) beaconLabel.setText("On");
        	else beaconLabel.setText("Off");
        }

        // Update speed label
        if (speedCache != vehicle.getSpeed()) {
            speedCache = vehicle.getSpeed();
            speedLabel.setText("" + formatter.format(speedCache) + " km/h");
        }

        // Update elevation label if ground vehicle.
        if (vehicle instanceof GroundVehicle) {
            GroundVehicle gVehicle = (GroundVehicle) vehicle;
            double currentElevation = gVehicle.getElevation();
            if (elevationCache != currentElevation) {
                elevationCache = currentElevation;
                elevationLabel.setText(formatter.format(elevationCache) + " km");
            }
        }

        Mission mission = missionManager.getMissionForVehicle(vehicle);
        
        boolean hasDestination = false;
        		
        if ((mission != null) && (mission instanceof VehicleMission)
                && ((VehicleMission) mission).getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT)) {
        	NavPoint destinationPoint = ((VehicleMission) mission).getNextNavpoint();
        	
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
//        		if (destinationTextCache != "") {
        			// If destination is coordinates, update destination text label.
        			destinationTextCache = Conversion.capitalize(destinationPoint.getDescription());//"A Navpoint";
        			destinationTextLabel.setText(destinationTextCache);
        			addDestinationTextLabel();
                    destinationSettlementCache = null;
//        		}
        	}
        }
        
        if (!hasDestination) {
          	// If destination is none, update destination text label.
        	if (destinationTextCache != "") {
        		destinationTextCache = "";
        		destinationTextLabel.setText(destinationTextCache);
        		addDestinationTextLabel();
        		destinationSettlementCache = null;
        	}
        }
        

        // Update latitude and longitude panels if necessary.
        if ((mission != null) && (mission instanceof VehicleMission)
                && ((VehicleMission) mission).getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT)) {
            VehicleMission vehicleMission = (VehicleMission) mission;
        	if (destinationLocationCache == null)
        		destinationLocationCache = new Coordinates(vehicleMission.getNextNavpoint().getLocation());
        	else 
        		destinationLocationCache.setCoords(vehicleMission.getNextNavpoint().getLocation());
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
        if ((mission != null) && (mission instanceof VehicleMission)) {
            VehicleMission vehicleMission = (VehicleMission) mission;
        	try {
        		if (distanceCache != vehicleMission.getCurrentLegRemainingDistance()) {
        			distanceCache = vehicleMission.getCurrentLegRemainingDistance();
        			distanceLabel.setText("" + formatter.format(distanceCache) + " km");
        		}
        	}
        	catch (Exception e) {
        		logger.log(Level.SEVERE,"Error getting current leg remaining distance.");
    			e.printStackTrace(System.err);
        	}
        }
        else {
        	distanceCache = 0D;
        	distanceLabel.setText("");
        }

        // Update ETA if necessary
        if ((mission != null) && (mission instanceof VehicleMission)) {
            VehicleMission vehicleMission = (VehicleMission) mission;
            if (vehicleMission.getLegETA() != null) {
                if (!etaCache.equals(vehicleMission.getLegETA().toString())) {
                    etaCache = vehicleMission.getLegETA().toString();
                    etaLabel.setText("" + etaCache);
                }
            }
        }
        else {
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

        // If center map button is pressed, center navigator tool
        // at destination location.
        if (source == centerMapButton) {
        	if (destinationLocationCache != null)
        		desktop.centerMapGlobe(destinationLocationCache);
        }

        // If destination settlement button is pressed, open window for settlement.
        if (source == destinationButton) desktop.openUnitWindow(destinationSettlementCache, false);

        // If driver button is pressed, open window for driver.
        if (source == driverButton) desktop.openUnitWindow((Unit) driverCache, false);
    }
    
	public void destroy() {
		formatter = null; 
	    driverButton = null; 
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
	    distanceLabel = null; 
	    etaLabel = null; 
	    directionDisplay = null; 
	    terrainDisplay = null; 
	    driverCache = null; 
	    statusCache = null; 
	    destinationLocationCache = null; 
	    destinationSettlementCache = null; 
		missionManager = null; 
	}
	
    
}
