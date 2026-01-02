/*
 * Mars Simulation Project
 * NavigationTabPanel.java
 * @date 2024-07-29
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.MapSelector;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The NavigationTabPanel is a tab panel for a vehicle's navigation information.
 */
@SuppressWarnings("serial")
public class NavigationTabPanel extends EntityTabPanel<Vehicle>
        implements ActionListener, EntityListener {
    
	private static final String NAV_ICON = "navigation";

    private JButton centerMapButton;
    private JButton destinationButton;
    
    private JPanel destinationLabelPanel;
    
    private JLabel statusLabel;
    private JLabel beaconLabel;
    private JLabel speedLabel;
    private JLabel elevationLabel;
    private JLabel destinationCoord;
    private JLabel remainingDistanceLabel;
    private JLabel etaLabel;
    private EntityLabel pilotLabel;
    private JLabel destinationTextLabel;
    private JLabel hoveringHeightLabel;
    
    private DirectionDisplayPanel directionDisplay;
    private TerrainDisplayPanel terrainDisplay;

    // Data cache
	/** Is UI constructed. */
    private boolean beaconCache = true;
    
    private double hoveringHeightCache = -1;
    private double speedCache = -1;
    private double elevationCache = -1;
    private double remainingDistanceCache = -1;
    
    private String destinationTextCache = "";
    private String etaCache = "";
	
    private Coordinates destinationLocationCache;
    private Settlement destinationSettlementCache;

    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param context the UI context.
     */
    public NavigationTabPanel(Vehicle unit, UIContext context) {
        // Use the TabPanel constructor
        super(
        	Msg.getString("NavigationTabPanel.title"), 
        	ImageLoader.getIconByName(NAV_ICON),
        	Msg.getString("NavigationTabPanel.title"), 
        	context, unit
        );	
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
        var vehicle = getEntity();
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
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPanel.add(topPanel, BorderLayout.NORTH);

        // Prepare destination left panel
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(leftPanel);
        
        // Prepare destination label panel
        destinationLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationLabelPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        topPanel.add(destinationLabelPanel, BorderLayout.NORTH);

        // Prepare center map button
		final Icon centerIcon = ImageLoader.getIconByName(NavigatorWindow.ICON);
		centerMapButton = new JButton(centerIcon); 
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        centerMapButton.setToolTipText("Locate the vehicle in Navigator Tool");
        topPanel.add(centerMapButton, BorderLayout.CENTER);
        
        // Prepare destination label
        JLabel destinationLabel = new JLabel("Destination :", SwingConstants.RIGHT);
        leftPanel.add(destinationLabel);
        
        // Prepare destination button
        destinationButton = new JButton();
        destinationButton.addActionListener(this);

        // Prepare destination text label
        destinationTextLabel = new JLabel("", SwingConstants.LEFT);
        
        boolean hasDestination = false;

        Mission mission = vehicle.getMission();
        if (mission instanceof VehicleMission vm && vm.isTravelling()) {
            hasDestination = true;
            NavPoint destinationPoint = vm.getCurrentDestination();
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
		AttributePanel destinationSpringPanel = new AttributePanel();
		locPanel.add(destinationSpringPanel, BorderLayout.NORTH);

        destinationCoord = destinationSpringPanel.addRow("Destination Coordinates", "");
        remainingDistanceLabel = destinationSpringPanel.addRow("Remaining Distance", "");
        etaLabel = destinationSpringPanel.addRow("ETA", "");
        statusLabel = destinationSpringPanel.addRow(Msg.getString("Vehicle.status"), "");
        beaconLabel = destinationSpringPanel.addRow("Emergency Beacon", "");
        speedLabel = destinationSpringPanel.addRow(Msg.getString("Vehicle.speed"), "");        
        elevationLabel = destinationSpringPanel.addRow("Ground Elevation", "");
    
        if (vehicle instanceof Drone) {
	        // Update hovering height label.
	        hoveringHeightLabel = destinationSpringPanel.addRow("Hovering Height", "");
        }
        
        // Prepare driver button and add it if vehicle has driver.
        pilotLabel = new EntityLabel(getContext());
        destinationSpringPanel.addLabelledItem(Msg.getString("Vehicle.operator"), pilotLabel);

        updateDisplay();
    }

    /**
     * Update the display based on clock pulse.
     */
    private void updateDisplay() {

        var vehicle = getEntity();

        // Update status label
        statusLabel.setText(vehicle.printStatusTypes());
      
        // Update beacon label
        if (beaconCache != vehicle.isBeaconOn()) {
        	beaconCache = vehicle.isBeaconOn();
        	beaconLabel.setText(beaconCache ? "On" : "Off");
        }

        // Update speed label
        if (speedCache != vehicle.getSpeed()) {
            speedCache = vehicle.getSpeed();
            speedLabel.setText(StyleManager.DECIMAL_KPH.format(speedCache));
        }

        // Update elevation label.
        double currentElevation = vehicle.getElevation();
        if (elevationCache != currentElevation) {
            elevationCache = currentElevation;
            elevationLabel.setText(StyleManager.DECIMAL_KM.format(elevationCache));
        }

        if (vehicle instanceof Drone d) {
	        // Update hovering height label.
	        double currentHoveringHeight = d.getHoveringHeight();
	        if (hoveringHeightCache != currentHoveringHeight) {
	        	hoveringHeightCache = currentHoveringHeight;
	        	hoveringHeightLabel.setText(StyleManager.DECIMAL_M.format(currentHoveringHeight));
	        }
        }
        
        pilotLabel.setEntity(vehicle.getOperator());

        Mission mission = vehicle.getMission();
        
        boolean hasDestination = false;
        		
        if (mission instanceof VehicleMission vm
                && vm.isTravelling()) {
        	NavPoint destinationPoint = vm.getCurrentDestination();
        	
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
        
        if (!hasDestination && destinationTextCache != null && !destinationTextCache.equals("")) {
            destinationTextCache = "";
            destinationTextLabel.setText(destinationTextCache);
            addDestinationTextLabel();
            destinationSettlementCache = null;
        }

        // Update latitude and longitude panels if necessary.
        if (mission instanceof VehicleMission vm
                && vm.isTravelling()) {
        	destinationLocationCache = vm.getCurrentDestination().getLocation();
            destinationCoord.setText(destinationLocationCache.getFormattedString());
        }
        else {
        	if (destinationLocationCache != null) {
        		destinationLocationCache = null;
                destinationCoord.setText("");
        	}
        }

        // Update distance to destination if necessary.
        if (mission instanceof VehicleMission vm) {
            double remaining = vm.getTotalDistanceRemaining();
            if (remainingDistanceCache != remaining) {
                remainingDistanceCache = remaining;
                remainingDistanceLabel.setText(StyleManager.DECIMAL_KM.format(remainingDistanceCache));
            }

            MarsTime newETA = vm.getLegETA();
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
        
        // If center map button is pressed, center navigator tool
        // at destination location.
        if (source.equals(centerMapButton) && destinationLocationCache != null)
        	MapSelector.displayCoords(getContext(), destinationLocationCache);
        

        // If destination settlement button is pressed, open window for settlement.
        if (source.equals(destinationButton)) 
        	getContext().showDetails(destinationSettlementCache);
    }

    @Override
    public void entityUpdate(EntityEvent event) {
        if (EntityEventType.COORDINATE_EVENT.equals(event.getType())
                || EntityEventType.STATUS_EVENT.equals(event.getType())) {
            updateDisplay();
        }
    }
}
