//************************** Storeroom Facility Panel **************************
// Last Modified: 5/30/00

// The StoreroomFacilityPanel class displays information about a settlement's storeroom facility in the user interface.

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class StoreroomFacilityPanel extends FacilityPanel {

	// Data members
	
	private StoreroomFacility storeroom;  // The storeroom facility this panel displays.
	private JLabel foodValueLabel;        // A label displaying the stores of food.
	private JLabel oxygenValueLabel;      // A label displaying the stores of oxygen.
	private JLabel waterValueLabel;       // A label displaying the stores of water.
	private JLabel fuelValueLabel;        // A label displaying the stores of fuel.
	private JLabel partsValueLabel;       // A label displaying the stores of parts.
	
	// Update data cache
	
	private double food;                  // Food supplies
	private double oxygen;                // Oxygen supplies
	private double water;                 // Water supplies
	private double fuel;                  // Fuel supplies
	private double parts;                 // Parts supplies
	
	// Constructor
	
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
		
		JPanel labelPane = new JPanel(new GridLayout(5, 2, 5, 3));
		storagePane.add(labelPane);
		
		// Prepare food label
		
		JLabel foodLabel = new JLabel("Food:");
		foodLabel.setForeground(Color.black);
		labelPane.add(foodLabel);
		
		// Prepare food value label
		
		food = storeroom.getFoodStores();
		foodValueLabel = new JLabel("" + roundOneDecimal(food), JLabel.RIGHT);
		foodValueLabel.setForeground(Color.black);
		labelPane.add(foodValueLabel);
		
		// Prepare oxygen label
		
		JLabel oxygenLabel = new JLabel("Oxygen:");
		oxygenLabel.setForeground(Color.black);
		labelPane.add(oxygenLabel);
		
		// Prepare oxygen value label
		
		oxygen = storeroom.getOxygenStores();
		oxygenValueLabel = new JLabel("" + roundOneDecimal(oxygen), JLabel.RIGHT);
		oxygenValueLabel.setForeground(Color.black);
		labelPane.add(oxygenValueLabel);
		
		// Prepare water label
		
		JLabel waterLabel = new JLabel("Water:");
		waterLabel.setForeground(Color.black);
		labelPane.add(waterLabel);
		
		// Prepare water value label
		
		water = storeroom.getWaterStores();
		waterValueLabel = new JLabel("" + roundOneDecimal(water), JLabel.RIGHT);
		waterValueLabel.setForeground(Color.black);
		labelPane.add(waterValueLabel);
		
		// Prepare fuel label
		
		JLabel fuelLabel = new JLabel("Fuel:");
		fuelLabel.setForeground(Color.black);
		labelPane.add(fuelLabel);
		
		// Prepare fuel value label
		
		fuel = storeroom.getFuelStores();
		fuelValueLabel = new JLabel("" + roundOneDecimal(fuel), JLabel.RIGHT);
		fuelValueLabel.setForeground(Color.black);
		labelPane.add(fuelValueLabel);
		
		// Prepare parts label
		
		JLabel partsLabel = new JLabel("Parts:");
		partsLabel.setForeground(Color.black);
		labelPane.add(partsLabel);
		
		// Prepare parts value label
		
		parts = storeroom.getPartsStores();
		partsValueLabel = new JLabel("" + roundOneDecimal(parts), JLabel.RIGHT);
		partsValueLabel.setForeground(Color.black);
		labelPane.add(partsValueLabel);
	}
	
	// Updates the facility panel's information
	
	public void updateInfo() { 
	
		if (food != storeroom.getFoodStores()) foodValueLabel.setText("" + roundOneDecimal(food));
		if (oxygen != storeroom.getOxygenStores()) oxygenValueLabel.setText("" + roundOneDecimal(oxygen));
		if (water != storeroom.getWaterStores()) waterValueLabel.setText("" + roundOneDecimal(water));
		if (fuel != storeroom.getFuelStores()) fuelValueLabel.setText("" + roundOneDecimal(fuel));
		if (parts != storeroom.getPartsStores()) partsValueLabel.setText("" + roundOneDecimal(parts));
	}
	
	// Returns a double value rounded to one decimal point
	
	public double roundOneDecimal(double initial) {
		return (double) (Math.round(initial * 1000D) / 1000D);
	}
}	

// Mars Simulation Project
// Copyright (C) 2000 Scott Davis
//
// For questions or comments on this project, email:
// mars-sim-users@lists.sourceforge.net
//
// or visit the project's Web site at:
// http://mars-sim@sourceforge.net
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA