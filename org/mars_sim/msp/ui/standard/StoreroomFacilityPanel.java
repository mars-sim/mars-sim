/**
 * Mars Simulation Project
 * StoreroomFacilityPanel.java
 * @version 2.74 2002-01-30
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *  The StoreroomFacilityPanel class displays information about a settlement's 
 *  storeroom in the user interface.
 */
public class StoreroomFacilityPanel extends FacilityPanel {

    // Data members
    private Settlement settlement;        // The settlement
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
     *  @param settlement the settlement 
     *  @param desktop the desktop pane
     */
    public StoreroomFacilityPanel(Settlement settlement, MainDesktopPane desktop) {
	
	// Use FacilityPanel's constructor	
	super(desktop);
		
	// Initialize data members
	this.settlement = settlement;
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
	food = settlement.getInventory().getResourceMass(Inventory.FOOD); 
	foodValueLabel = new JLabel("" + roundOneDecimal(food) + " kg", JLabel.RIGHT);
	foodValueLabel.setForeground(Color.black);
	labelPane.add(foodValueLabel);
		
	// Prepare oxygen label		
	JLabel oxygenLabel = new JLabel("Oxygen:");
	oxygenLabel.setForeground(Color.black);
	labelPane.add(oxygenLabel);
		
	// Prepare oxygen value label
	oxygen = settlement.getInventory().getResourceMass(Inventory.OXYGEN); 
	oxygenValueLabel = new JLabel("" + roundOneDecimal(oxygen) + " kg", JLabel.RIGHT);
	oxygenValueLabel.setForeground(Color.black);
	labelPane.add(oxygenValueLabel);
		
	// Prepare water label
	JLabel waterLabel = new JLabel("Water:");
	waterLabel.setForeground(Color.black);
	labelPane.add(waterLabel);
		
	// Prepare water value label
	water = settlement.getInventory().getResourceMass(Inventory.WATER);
	waterValueLabel = new JLabel("" + roundOneDecimal(water) + " kg", JLabel.RIGHT);
	waterValueLabel.setForeground(Color.black);
	labelPane.add(waterValueLabel);
		
	// Prepare fuel label
	JLabel fuelLabel = new JLabel("Fuel:");
	fuelLabel.setForeground(Color.black);
	labelPane.add(fuelLabel);
		
	// Prepare fuel value label
	fuel = settlement.getInventory().getResourceMass(Inventory.FUEL); 
	fuelValueLabel = new JLabel("" + roundOneDecimal(fuel) + "kg", JLabel.RIGHT);
	fuelValueLabel.setForeground(Color.black);
	labelPane.add(fuelValueLabel);
    }
	
    /** Updates the facility panel's information */
    public void updateInfo() { 

	double newFood = settlement.getInventory().getResourceMass(Inventory.FOOD);
	if (food != newFood) {
	    food = newFood;
	    foodValueLabel.setText("" + roundOneDecimal(food) + " kg");
	}
	
	double newOxygen = settlement.getInventory().getResourceMass(Inventory.OXYGEN);
	if (oxygen != newOxygen) {
	    oxygen = newOxygen;
	    oxygenValueLabel.setText("" + roundOneDecimal(oxygen) + " kg");
	}
	
	double newWater = settlement.getInventory().getResourceMass(Inventory.WATER);
	if (water != newWater) {
	    water = newWater;
	    waterValueLabel.setText("" + roundOneDecimal(water) + " kg");
	}
	
	double newFuel = settlement.getInventory().getResourceMass(Inventory.FUEL);
	if (fuel != newFuel) {
	    fuel = newFuel;
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
