/**
 * Mars Simulation Project
 * GroundVehicleDialog.java
 * @version 2.74 2002-03-15
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 
 
import org.mars_sim.msp.simulation.*; 
import org.mars_sim.msp.simulation.vehicle.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * The GroundVehicleDialog class is the detail window for a ground vehicle.
 */
public abstract class GroundVehicleDialog extends VehicleDialog {
    
    // Data members
    protected GroundVehicle groundVehicle;               // Ground vehicle related to this detail window
    protected VehicleTerrainDisplay terrainDisplay;      // Terrain average grade display
    protected VehicleDirectionDisplay directionDisplay;  // Direction display
    protected JLabel elevationLabel;                     // Elevation label
	
    // Update data cache
    protected double elevation;                          // Cached elevation data
    protected double terrainGrade;                       // Cached terrain grade data
    protected Direction direction;                       // Cached direction data
	
    /** Constructs a GroundVehicleDialog object 
     *  @param parentDesktop desktop pane
     *  @param groundVehicleUIProxy the ground vehicle's UI proxy
     */
    public GroundVehicleDialog(MainDesktopPane parentDesktop, GroundVehicleUIProxy groundVehicleUIProxy) {
	// Use VehicleDialog constructor	
	super(parentDesktop, groundVehicleUIProxy);
    }

    /** Initialize cached data members */
    protected void initCachedData() {
	super.initCachedData();
		
	elevation = 0D;
	terrainGrade = 0D;
	direction = new Direction(0D);
		
    }

    /** Override setupComponents */
    protected void setupComponents() {
		
	// Initialize ground vehicle
	groundVehicle = (GroundVehicle) parentUnit;
	
	super.setupComponents();
    }

    /** Override setupNavigationPane 
     *  @return the navigation pane
     */
    protected JPanel setupNavigationPane() {
		
	// Prepare navigation pane
	JPanel navigationPane = super.setupNavigationPane();
		
	// Prepare elevation label
	double tempElevation = Math.round(groundVehicle.getElevation() * 100D) / 100D;
	elevationLabel = new JLabel("Elevation: " + tempElevation + " km.");
	JPanel elevationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	elevationLabelPane.add(elevationLabel);
	navigationInfoPane.add(elevationLabelPane);

	// Prepare ground display pane
	JPanel groundDisplayPane = new JPanel();
	groundDisplayPane.setLayout(new BoxLayout(groundDisplayPane, BoxLayout.X_AXIS));
	groundDisplayPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
	navigationPane.add(groundDisplayPane);
		
	// Prepare terrain display
	terrainDisplay = new VehicleTerrainDisplay();
	JPanel terrainDisplayPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	terrainDisplayPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), 
               new LineBorder(Color.green)));
	terrainDisplayPane.setMaximumSize(new Dimension(106, 56));
	terrainDisplayPane.add(terrainDisplay);
	groundDisplayPane.add(terrainDisplayPane);
		
	// Add glue spacer
	groundDisplayPane.add(Box.createHorizontalGlue());
		
	// Prepare direction display
	directionDisplay = new VehicleDirectionDisplay(groundVehicle.getDirection(), 
               (vehicle.getStatus() == Vehicle.MOVING));
	JPanel directionDisplayPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	directionDisplayPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), 
               new LineBorder(Color.green)));
	directionDisplayPane.setMaximumSize(new Dimension(56, 56));
	directionDisplayPane.add(directionDisplay);
	groundDisplayPane.add(directionDisplayPane);
		
	// Return navigation pane
	return navigationPane;
    }
	
    /** Override generalUpdate */
    protected void generalUpdate() {
	super.generalUpdate();
	updateAveGrade();
	updateDirection();
	updateElevation();
    }

    /** Update terrain display */
    protected void updateAveGrade() {
	if (vehicle.getStatus() != Vehicle.MOVING) {
		if (terrainGrade != 0D) {
			terrainDisplay.updateTerrainAngle(0D);
			terrainGrade = 0D;
		}
	}
	else {
            double tempGrade = groundVehicle.getTerrainGrade();
	    if (terrainGrade != tempGrade) {
		terrainDisplay.updateTerrainAngle(tempGrade);
		terrainGrade = tempGrade;
	    }
	}
    }

    /** Update direction display */
    protected void updateDirection() {
	Direction tempDirection = groundVehicle.getDirection();
	if (!direction.equals(tempDirection)) {
            directionDisplay.updateDirection(tempDirection, (groundVehicle.getStatus().equals("Parked") || 
                   groundVehicle.getStatus().equals("Periodic Maintenance")));
	    direction = tempDirection;
	}
    }

    /** Update elevation label */
    protected void updateElevation() {
	double tempElevation = Math.round(groundVehicle.getElevation() * 100D) / 100D;
	if (elevation != tempElevation) {
            elevationLabel.setText("Elevation: " + tempElevation + " km.");
	    elevation = tempElevation;
	}
    }
   
    /** Set window size 
     *  @return the window's size
     */
    protected Dimension setWindowSize() { return new Dimension(310, 435); }
}
