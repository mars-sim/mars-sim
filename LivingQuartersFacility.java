//************************** Living Quarters Facility **************************
// Last Modified: 5/14/00

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
		
		super(manager, "Living Quarters");
	
		// Initialize random normal capacity from 10 to 30.
		
		normalCapacity = 10 + RandomUtil.getRandomInteger(20);
		
		// Initialize maximumCapacity as twice normal capacity.
		
		maximumCapacity = 2 * normalCapacity;
	}
	
	// Constructor for set capacity value (used later when facilities can be built or upgraded.)
	
	public LivingQuartersFacility(FacilityManager manager, int normalCapacity) {
	
		// Use Facility's constructor.
		
		super(manager, "Living Quarters");
		
		// Initialize data members.
		
		this.normalCapacity = normalCapacity;
		maximumCapacity = 2 * normalCapacity;
	}
	
	// Returns the normal capacity of the settlement.
	
	public int getNormalCapacity() { return normalCapacity; }
	
	// Returns the maximum capacity of the settlement.
	
	public int getMaximumCapacity() { return maximumCapacity; }
	
	// Returns the current population of the settlement.
	
	public int getCurrentPopulation() { return manager.getSettlement().getPeopleNum(); }
	
	// Returns an array of UnitInfo about inhabitants of the settlement
	
	public UnitInfo[] getPopulationInfo() {
		
		int populationNum = manager.getSettlement().getPeopleNum();
		UnitInfo[] personInfo = new UnitInfo[populationNum];
		
		for (int x=0; x < populationNum; x++) personInfo[x] = manager.getSettlement().getPerson(x).getUnitInfo();
		
		return personInfo;
	}
	
	// Returns the UI panel for this facility.
	
	public FacilityPanel getUIPanel(MainDesktopPane desktop) { return new LivingQuartersFacilityPanel(this, desktop); }
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