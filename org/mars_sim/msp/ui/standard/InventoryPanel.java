/**
 * Mars Simulation Project
 * InventoryPanel.java
 * @version 2.74 2002-02-21
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.equipment.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** 
 * The InventoryPanel class displays a general inventory panel 
 *  for a unit dialog.  It displays resources and equipment in
 *  the unit's inventory.
 */
public class InventoryPanel extends JPanel {

    // Data members
    private Inventory inventory; // The unit's inventory object.
    private JLabel oxygenLabel;  // A label for oxygen resource.
    private JLabel waterLabel;   // A label for water resource.
    private JLabel foodLabel;    // A label for food resource.
    private JLabel fuelLabel;    // A label for fuel resource.
    private JLabel rockSamplesLabel; // A Label for rock samples resource.
    private EquipmentCollection cachedEquipment;
    private JPanel equipmentListingPane;

    /** 
     * Constructs an InventoryPanel object.
     * @param inventory the unit's inventory
     */
    public InventoryPanel(Inventory inventory) {
        // Call JPanel constructor.
	super();

	// Initialize data members.
	this.inventory = inventory;

	// Set default font
	setFont(new Font("Helvetica", Font.BOLD, 12));

	// Set a border around the panel.
	setBorder(new CompoundBorder(new EtchedBorder(),
	        new EmptyBorder(5, 5, 5, 5)));
	
	// Set layout
	setLayout(new BorderLayout(0, 5));

	// Prepare name label.
	JLabel nameLabel = new JLabel("Inventory", JLabel.CENTER);
	nameLabel.setForeground(Color.black);
	add(nameLabel, "North");

	// Prepare content pane.
	JPanel contentPane = new JPanel(new GridLayout(2, 1));
	add(contentPane, "Center");

        // Prepare resource pane.
	contentPane.add(prepareResourcePane());
	
	// Prepare equipment pane.
	contentPane.add(prepareEquipmentPane());
    }

    /**
     * Prepares the resource pane.
     * @return resource pane
     */
    private JPanel prepareResourcePane() {

        // Prepare resource pane.
	JPanel resourcePane = new JPanel(new BorderLayout());
	resourcePane.setBorder(new CompoundBorder(new EtchedBorder(),
                new EmptyBorder(5, 5, 5, 5)));
	
        // Prepare resource label.
	JLabel resourceLabel = new JLabel("Resources:", JLabel.CENTER);
	resourceLabel.setForeground(Color.black);
	resourcePane.add(resourceLabel, "North");
	   
        // Prepare resource amounts pane.
	JPanel resourceAmountOuterPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
	resourcePane.add(resourceAmountOuterPane, "Center");
	JPanel resourceAmountPane = new JPanel(new GridLayout(5, 2, 5, 3));
	resourceAmountOuterPane.add(resourceAmountPane);

        // Prepare oxygen name label.
	JLabel oxygenNameLabel = new JLabel("Oxygen:");
	oxygenNameLabel.setForeground(Color.black);
	resourceAmountPane.add(oxygenNameLabel);

	// Prepare oxygen label.
	double oxygen = inventory.getResourceMass(Inventory.OXYGEN);
	oxygenLabel = new JLabel("" + roundOneDecimal(oxygen) + " kg", JLabel.RIGHT);
	oxygenLabel.setForeground(Color.black);
	resourceAmountPane.add(oxygenLabel);

        // Prepare water name label.
	JLabel waterNameLabel = new JLabel("Water:");
	waterNameLabel.setForeground(Color.black);
	resourceAmountPane.add(waterNameLabel);

	// Prepare water label.
	double water = inventory.getResourceMass(Inventory.WATER);
	waterLabel = new JLabel("" + roundOneDecimal(water) + " kg", JLabel.RIGHT);
	waterLabel.setForeground(Color.black);
	resourceAmountPane.add(waterLabel);

        // Prepare food name label.
	JLabel foodNameLabel = new JLabel("Food:");
	foodNameLabel.setForeground(Color.black);
	resourceAmountPane.add(foodNameLabel);
	
	// Prepare food label.
	double food = inventory.getResourceMass(Inventory.FOOD);
	foodLabel = new JLabel("" + roundOneDecimal(food) + " kg", JLabel.RIGHT);
	foodLabel.setForeground(Color.black);
	resourceAmountPane.add(foodLabel);

	// Prepare fuel name label.
	JLabel fuelNameLabel = new JLabel("Fuel:");
	fuelNameLabel.setForeground(Color.black);
	resourceAmountPane.add(fuelNameLabel);

	// Prepare fuel label.
	double fuel = inventory.getResourceMass(Inventory.FUEL);
	fuelLabel = new JLabel("" + roundOneDecimal(fuel) + " kg", JLabel.RIGHT);
	fuelLabel.setForeground(Color.black);
	resourceAmountPane.add(fuelLabel);

	// Prepare rock samples name label.
	JLabel rockSamplesNameLabel = new JLabel("Rock Samples:");
	rockSamplesNameLabel.setForeground(Color.black);
	resourceAmountPane.add(rockSamplesNameLabel);

	// Prepare rock samples label.
	double rockSamples = inventory.getResourceMass(Inventory.ROCK_SAMPLES);
	rockSamplesLabel = new JLabel("" + roundOneDecimal(rockSamples) + " kg", JLabel.RIGHT);
	rockSamplesLabel.setForeground(Color.black);
	resourceAmountPane.add(rockSamplesLabel);

	// Return resource pane.
	return resourcePane;
    }

    /**
     * Prepares the equipment pane.
     * @return equipment pane
     */
    private JScrollPane prepareEquipmentPane() {

        // Prepare equipment pane.
	JPanel equipmentPane = new JPanel(new BorderLayout());
	JScrollPane scroller = new JScrollPane(equipmentPane);
	scroller.setBorder(new CompoundBorder(new EtchedBorder(),
                new EmptyBorder(5, 5, 5, 5)));

	// Prepare equipment label.
	JLabel equipmentLabel = new JLabel("Equipment:", JLabel.CENTER);
	equipmentLabel.setForeground(Color.black);
	equipmentPane.add(equipmentLabel, "North");

        // Prepare equipment listing pane.
	JPanel equipmentListingOuterPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
	equipmentPane.add(equipmentListingOuterPane, "Center");
	equipmentListingPane = new JPanel(new GridLayout(0, 2, 5, 3));
	equipmentListingOuterPane.add(equipmentListingPane);
	
        EquipmentCollection equipment = inventory.getContainedUnits().getEquipment();
	cachedEquipment = equipment.sortByName();

        // Refresh equipment listing pane.
	refreshEquipmentListing();
	
	// Return equipment pane.
	return scroller;
    }

    /** 
     * Refreshes the equipment listing pane with the cached euipment collection.
     */
    private void refreshEquipmentListing() {

        // Remove old items from equipment listing pane.
	equipmentListingPane.removeAll();

	// Add cached equipment collection.
	Equipment last = null;
	int count = 0;
	EquipmentIterator i = cachedEquipment.iterator();
	while (i.hasNext()) {
	    Equipment temp = i.next();
	    if (last != null) {
	        if (temp.getName().equals(last.getName())) {
	            count++;
	        }
	        else {
	            addEquipmentListing(last, count);
	            count = 1;
	        }
	    }
	    else count++;

            last = temp;

            if (!i.hasNext()) addEquipmentListing(last, count);
        }

	// Validate equipment listing pane.
	equipmentListingPane.validate();
    }
    
    /** 
     * Adds a piece of equipment and its quantity to the equipment listing pane.
     * @param equipment the piece of equipment
     * @param quantity the quantity of the equipment
     */
    private void addEquipmentListing(Equipment equipment, int quantity) {
        
        // Prepare equipment name label.
	JLabel equipmentNameLabel = new JLabel(equipment.getName() + ":");
	equipmentNameLabel.setForeground(Color.black);
	equipmentListingPane.add(equipmentNameLabel);
	                                                             
	// Prepare equipment quantity label.
	JLabel equipmentQuantityLabel = new JLabel("" + quantity, JLabel.RIGHT);
	equipmentQuantityLabel.setForeground(Color.black);
	equipmentListingPane.add(equipmentQuantityLabel);
    }
    
    /**
     * Updates the inventory panel's information.
     */
    public void updateInfo() {
	// Update the resources.
        updateResources();

	// Update the equipment.
	updateEquipment();
    }

    /**
     * Updates the resource info.
     */
    private void updateResources() {
  
        double oxygen = inventory.getResourceMass(Inventory.OXYGEN);
	oxygenLabel.setText("" + roundOneDecimal(oxygen) + " kg");

	double water = inventory.getResourceMass(Inventory.WATER);
	waterLabel.setText("" + roundOneDecimal(water) + " kg");

	double food = inventory.getResourceMass(Inventory.FOOD);
	foodLabel.setText("" + roundOneDecimal(food) + " kg");

	double fuel = inventory.getResourceMass(Inventory.FUEL);
	fuelLabel.setText("" + roundOneDecimal(fuel) + " kg");

	double rockSamples = inventory.getResourceMass(Inventory.ROCK_SAMPLES);
	rockSamplesLabel.setText("" + roundOneDecimal(rockSamples) + " kg");
    }

    /**
     * Updates the equipment info.
     */
    private void updateEquipment() {
 
        EquipmentCollection equipment = inventory.getContainedUnits().getEquipment();
	EquipmentCollection sortedEquipment = equipment.sortByName();

	// Check if sorted equipment list equals cached equipment list.
	boolean match = true;
	if (sortedEquipment.size() != cachedEquipment.size()) match = false;
	else {
            EquipmentIterator i1 = sortedEquipment.iterator();
	    EquipmentIterator i2 = cachedEquipment.iterator();
            while (i1.hasNext()) {
	        if (i1.next() != i2.next()) match = false;
	    }
	}

	if (!match) {
	    cachedEquipment = sortedEquipment;
	    refreshEquipmentListing();
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
