//************************** Living Quarters Facility **************************
// Last Modified: 5/6/00

// The LivingQuartersFacility class represents the living quarters in a settlement.
// It defines the settlement's capacity for inhabitants in both normal and emergency
// situations.

// Every settlement should have living quarters.

public class LivingQuartersFacility extends Facility {

	// Data members
	
	private int normalCapacity;   // Inhabitant capacity of the settlement under normal conditions.
	private int maximumCapacity;  // Inhabitant capacity of the settlement under emergency conditions.

	// Constructor for random creation.

	public LivingQuartersFacility(FacilityManager manager) {
	
		// Use Facility's constructor.
		
		super(manager, "Living Quarters", "Quarters");
	
		// Initialize random normal capacity from 10 to 30.
		
		normalCapacity = 10 + RandomUtil.getRandomInteger(20);
		
		// Initialize maximumCapacity as twice normal capacity.
		
		maximumCapacity = 2 * normalCapacity;
	}
	
	// Constructor for set capacity value (used later when facilities can be built or upgraded.)
	
	public LivingQuartersFacility(FacilityManager manager, int normalCapacity) {
	
		// Use Facility's constructor.
		
		super(manager, "Living Quarters", "Quarters");
		
		// Initialize data members.
		
		this.normalCapacity = normalCapacity;
		maximumCapacity = 2 * normalCapacity;
	}
	
	// Returns the normal capacity of the settlement.
	
	public int getNormalCapacity() { return normalCapacity; }
	
	// Returns the maximum capacity of the settlement.
	
	public int getMaximumCapacity() { return maximumCapacity; }
}	

// Mars Simulation Project
// Copyright (C) 1999 Scott Davis
//
// For questions or comments on this project, contact:
//
// Scott Davis
// 1725 W. Timber Ridge Ln. #6206
// Oak Creek, WI  53154
// scud1@execpc.com
// http://www.execpc.com/~scud1/
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