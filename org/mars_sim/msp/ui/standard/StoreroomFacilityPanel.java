/**
 * Mars Simulation Project
 * StoreroomFacilityPanel.java
 * @version 2.73 2001-12-07
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import org.mars_sim.msp.simulation.*; 
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *  The StoreroomFacilityPanel class displays information about a settlement's 
 *  storeroom facility in the user interface.
 */
public class StoreroomFacilityPanel extends FacilityPanel {

    // Data members
    private StoreroomFacility storeroom;  // The storeroom facility this panel displays.
    private JLabel foodValueLabel;        // A label displaying the stores of food.
    private JLabel oxygenValueLabel;      // A label displaying the stores of oxygen.
    private JLabel waterValueLabel;       // A label displaying the stores of water.
    private JLabel fuelValueLabel;        // A label displaying the stores of fuel.
	
    // Update data cache
    private double food;                  // Food supplies
    private double oxygen;                // Oxygen supplies
    private double water;                 // Water supplies
    private double fuel;                  // Fuel supplies
	
    /** Constructs a StoreroomFacilityPanel object 
     *  @param storeroom the storeroom facility
     *  @param desktop the desktop pane
     */
    public StoreroomFacilityPanel(StoreroomFacility storeroom, MainDesktopPane desktop) {
	
	// Use FacilityPanel's constructor	
	super(desktop);
		
	// Initialize data members
	this.storeroom = storeroom;
	tabName = "Storage";
		
	// Set up components
	setLayout(new BorderLayout());
		
	// Prepare content pane
	JPanel contentPane = new JPanel(new BorderLayout(0, 5));
	add(contentPane, "North");
		
	// Prepare name label
	JLabel nameLabel = new JLabel("Storeroom", JLabel.CENTER);
	nameLabel.setForeground(Color.black);
	contentPane.add(nameLabel, "North");
		
	// Prepare info pane
	JPanel infoPane = new JPanel(new BorderLayout(0, 5));
	infoPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
	contentPane.add(infoPane, "Center");
		
	// Prepare supplies label
	JLabel suppliesLabel = new JLabel("Supplies:", JLabel.CENTER);
	suppliesLabel.setForeground(Color.black);
	infoPane.add(suppliesLabel, "North");
		
	// Prepare storage pane
	JPanel storagePane = new JPanel();
	infoPane.add(storagePane, "Center");
		
	// Prepare label pane
	JPanel labelPane = new JPanel(new GridLayout(4, 2, 5, 3));
	storagePane.add(labelPane);
		
	// Prepare food label
	JLabel foodLabel = new JLabel("Food:");
	foodLabel.setForeground(Color.black);
	labelPane.add(foodLabel);
		
	// Prepare food value label
	food = storeroom.getFoodStores();
	foodValueLabel = new JLabel("" + roundOneDecimal(food) + " kg", JLabel.RIGHT);
	foodValueLabel.setForeground(Color.black);
	labelPane.add(foodValueLabel);
		
	// Prepare oxygen label		
	JLabel oxygenLabel = new JLabel("Oxygen:");
	oxygenLabel.setForeground(Color.black);
	labelPane.add(oxygenLabel);
		
	// Prepare oxygen value label
	oxygen = storeroom.getOxygenStores();
	oxygenValueLabel = new JLabel("" + roundOneDecimal(oxygen) + " kg", JLabel.RIGHT);
	oxygenValueLabel.setForeground(Color.black);
	labelPane.add(oxygenValueLabel);
		
	// Prepare water label
	JLabel waterLabel = new JLabel("Water:");
	waterLabel.setForeground(Color.black);
	labelPane.add(waterLabel);
		
	// Prepare water value label
	water = storeroom.getWaterStores();
	waterValueLabel = new JLabel("" + roundOneDecimal(water) + " kg", JLabel.RIGHT);
	waterValueLabel.setForeground(Color.black);
	labelPane.add(waterValueLabel);
		
	// Prepare fuel label
	JLabel fuelLabel = new JLabel("Fuel:");
	fuelLabel.setForeground(Color.black);
	labelPane.add(fuelLabel);
		
	// Prepare fuel value label
	fuel = storeroom.getFuelStores();
	fuelValueLabel = new JLabel("" + roundOneDecimal(fuel) + "kg", JLabel.RIGHT);
	fuelValueLabel.setForeground(Color.black);
	labelPane.add(fuelValueLabel);
    }
	
    /** Updates the facility panel's information */
    public void updateInfo() { 
	
	if (food != storeroom.getFoodStores()) {
	    food = storeroom.getFoodStores();
	    foodValueLabel.setText("" + roundOneDecimal(food) + " kg");
	}
	if (oxygen != storeroom.getOxygenStores()) {
	    oxygen = storeroom.getOxygenStores();
	    oxygenValueLabel.setText("" + roundOneDecimal(oxygen) + " kg");
	}
	if (water != storeroom.getWaterStores()) {
	    water = storeroom.getWaterStores();
	    waterValueLabel.setText("" + roundOneDecimal(water) + " kg");
	}
	if (fuel != storeroom.getFuelStores()) {
	    fuel = storeroom.getFuelStores();
	    fuelValueLabel.setText("" + roundOneDecimal(fuel) + " kg");
	}
    }
	
    /** Returns a double value rounded to one decimal point 
     *  @param initial the initial double value
     *  @return the rounded value
     */
    public double roundOneDecimal(double initial) {
        return (double) (Math.round(initial * 10D) / 10D);
    }
}
