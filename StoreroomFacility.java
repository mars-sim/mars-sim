//************************** Storeroom Facility **************************
// Last Modified: 5/14/00

// The StoreroomFacility class represents the collective storerooms in a settlement.
// It defines the settlement's storage of food, oxygen, water, fuel, parts and other 
// various materials.

// Every settlement should have storerooms.

public class StoreroomFacility extends Facility {

	// Data members
	
	private double foodStores;    // The settlement's stores of food.
	private double oxygenStores;  // The settlement's stores of oxygen.
	private double waterStores;   // The settlement's stores of water.
	private double fuelStores;    // The settlement's stores of fuel (methane and other fuel).
	private double partsStores;   // The settlement's stores of mechanical and electrical parts.

	// Constructor for random creation.

	public StoreroomFacility(FacilityManager manager) {
	
		// Use Facility's constructor.
		
		super(manager, "Storerooms");
	
		// Initialize random capacity for each good from 10 to 100.
		
		foodStores = 10 + RandomUtil.getRandomInteger(90);
		oxygenStores = 10 + RandomUtil.getRandomInteger(90);
		waterStores = 10 + RandomUtil.getRandomInteger(90);
		fuelStores = 10 + RandomUtil.getRandomInteger(90);
		partsStores = 10 + RandomUtil.getRandomInteger(90);
	}
	
	// Constructor for set storage values (used later when facilities can be built or upgraded.)
	
	public StoreroomFacility(FacilityManager manager, double food, double oxygen, double water, double fuel, double parts) {
	
		// Use Facility's constructor.
		
		super(manager, "Storerooms");
		
		// Initialize data members.
		
		foodStores = food;
		oxygenStores = oxygen;
		waterStores = water;
		fuelStores = fuel;
		partsStores = parts;
	}
	
	// Returns the amount of food stored at the settlement.
	
	public double getFoodStores() { return foodStores; }
	
	// Removes food from storage.
	
	public double removeFood(double amount) { 
	
		double result = amount;
		if (amount > foodStores) result = amount - foodStores;
		foodStores -= amount;
		if (foodStores < 0) foodStores = 0;
		
		return result;
	}
	
	// Adds food to storage.
	
	public void addFood(double amount) { foodStores += Math.abs(amount); }
	
	// Returns the amount of oxygen stored at the settlement.
	
	public double getOxygenStores() { return oxygenStores; }
	
	// Removes oxygen from storage.
	
	public double removeOxygen(double amount) { 
	
		double result = amount;
		if (amount > oxygenStores) result = amount - oxygenStores;
		oxygenStores -= amount;
		if (oxygenStores < 0) oxygenStores = 0;
		
		return result;
	}
	
	// Adds oxygen to storage.
	
	public void addOxygen(double amount) { oxygenStores += Math.abs(amount); }
		
	// Returns the amount of water stored at the settlement.
	
	public double getWaterStores() { return waterStores; }
	
	// Removes water from storage.
	
	public double removeWater(double amount) { 
	
		double result = amount;
		if (amount > waterStores) result = amount - waterStores;
		waterStores -= amount;
		if (waterStores < 0) waterStores = 0;
		
		return result;
	}
	
	// Adds water to storage.
	
	public void addWater(double amount) { waterStores += Math.abs(amount); }		
		
	// Returns the amount of fuel stored at the settlement.
	
	public double getFuelStores() { return fuelStores; }
	
	// Removes fuel from storage.
	
	public double removeFuel(double amount) { 
	
		double result = amount;
		if (amount > fuelStores) result = amount - fuelStores;
		fuelStores -= amount;
		if (fuelStores < 0) fuelStores = 0;
		
		return result;
	}
	
	// Adds fuel to storage.
	
	public void addFuel(double amount) { fuelStores += Math.abs(amount); }	
	
	// Returns the amount of parts stored at the settlement.
	
	public double getPartsStores() { return partsStores; }
	
	// Removes parts from storage.
	
	public double removeParts(double amount) { 
	
		double result = amount;
		if (amount > partsStores) result = amount - partsStores;
		partsStores -= amount;
		if (partsStores < 0) partsStores = 0;
		
		return result;
	}
	
	// Adds parts to storage.
	
	public void addParts(double amount) { partsStores += Math.abs(amount); }
		
	// Returns the UI panel for this facility.
	
	public FacilityPanel getUIPanel(MainDesktopPane desktop) { return new StoreroomFacilityPanel(this, desktop); }
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