/**
 * Mars Simulation Project
 * LivingQuartersFacility.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation; 
 
/**
 * The LivingQuartersFacility class represents the living quarters in a settlement.
 * It defines the settlement's capacity for inhabitants in both normal and emergency
 * situations.
 */
 
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
	
	public Unit[] getPopulationUnits() {
		
		int populationNum = manager.getSettlement().getPeopleNum();
		Unit[] personUnits = new Unit[populationNum];
		
		for (int x=0; x < populationNum; x++) personUnits[x] = manager.getSettlement().getPerson(x);
		
		return personUnits;
	}
}
