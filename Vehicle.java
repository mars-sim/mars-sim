//************************** Abstract Basic Vehicle Unit **************************
// Last Modified: 8/29/00

// The Vehicle class represents a generic vehicle.  It keeps track of generic information about the vehicle.
// This class needs to be subclassed to represent a specific type of vehicle.

import java.awt.*;
import java.util.*;
import javax.swing.*;

public abstract class Vehicle extends Unit {

	// Data members

	protected double direction;            // Direction vehicle is traveling in
	protected double speed;                // Current speed of vehicle in kph
	protected double baseSpeed;            // Base speed of vehicle in kph (can be set in child class)
	protected String status;               // Current status of vehicle ("Moving", "Parked") (other child-specific status allowed)
	protected Settlement settlement;       // The settlement which the vehicle is parked at
	protected Vector passengers;           // List of people who are passengers in vehicle
	protected Person driver;               // Driver of the vehicle
	protected double distanceTraveled;     // Total distance traveled by vehicle
	protected double distanceMaint;        // Distance traveled by vehicle since last maintenance
	
	protected Coordinates destinationCoords;    // Coordinates of the destination
	protected Settlement destinationSettlement; // Destination settlement (it applicable)
	protected String destinationType;           // Type of destination ("None", "Settlement" or "Coordinates")
	protected double distanceToDestination;     // Distance in meters to the destination
	protected boolean isReserved;               // True if vehicle is currently reserved for a driver and cannot be taken by another
	protected int vehicleSize;                  // Size of vehicle in arbitrary units.(Value of size units will be established later.)
	protected int maintenanceWork;              // Work done for vehicle maintenance.
	protected int totalMaintenanceWork;         // Total amount of work necessary for vehicle maintenance.
	
	protected HashMap potentialFailures;           // A table of potential failures in the vehicle. (populated by child classes)
	protected MechanicalFailure mechanicalFailure; // A list of current failures in the vehicle.
	
	protected boolean distanceMark;

	// Constructor

	public Vehicle(int unitID, String name, Coordinates location, VirtualMars mars, UnitManager manager) {

		// Use Unit constructor

		super(unitID, name, location, mars, manager);
		
		// Initialize globals

		direction = 0D;
		speed = 0D;
		baseSpeed = 30D;  // Child vehicles should change this as appropriate
		status = new String("Parked");
		distanceTraveled = 0D;
		distanceMaint = 0D;

		settlement = null;
		passengers = new Vector();
		driver = null;
		
		destinationCoords = null;
		destinationSettlement = null;
		destinationType = new String("None");
		distanceToDestination = 0;
		
		isReserved = false;
		vehicleSize = 1;
		
		potentialFailures = new HashMap();
		mechanicalFailure = null;
		maintenanceWork = 0;
		totalMaintenanceWork = 12 * 60 * 60; // (12 hours)
		
		distanceMark = false;
	}

	// Returns vehicle's current status
	
	public String getStatus() { return new String(status); }
	
	// Sets vehicle's current status
	
	public void setStatus(String status) { this.status = new String(status); }
	
	// Returns true if vehicle is reserved by someone
	
	public boolean isReserved() { return isReserved; }
	
	// Reserves a vehicle or cancels a reservation
	
	public void setReserved(boolean status) { isReserved = status; }

	// Returns speed of vehicle

	public double getSpeed() { return speed; }
	
	// Sets the vehicle's current speed
	
	public void setSpeed(double speed) { this.speed = speed; }
	
	// Returns base speed of vehicle
	
	public double getBaseSpeed() { return baseSpeed; }
	
	// Returns total distance traveled by vehicle (in km.)
	
	public double getTotalDistanceTraveled() { return distanceTraveled; }
	
	// Adds a distance (in km.) to the vehicle's total distance traveled
	
	public void addTotalDistanceTraveled(double distance) { distanceTraveled += distance; }
	
	// Returns distance traveled by vehicle since last maintenance (in km.)
	
	public double getDistanceLastMaintenance() { return distanceMaint; }

	// Adds a distance (in km.) to the vehicle's distance since last maintenance
	
	public void addDistanceLastMaintenance(double distance) { 
		distanceMaint += distance; 
		if ((distanceMaint > 5000D) && !distanceMark) distanceMark = true;
	}
	
	// Sets vehicle's distance since last maintenance to zero
	
	public void clearDistanceLastMaintenance() { distanceMaint = 0; }

	// Returns direction of vehicle (0 = north, clockwise in radians)

	public double getDirection() { return direction; }

	// Sets the vehicle's facing direction (0 = north, clockwise in radians)
	
	public void setDirection(double direction) { this.direction = direction; }

	// Returns number of passengers in vehicle

	public int getPassengerNum() { return passengers.size(); }

	// Returns a particular passenger by vector index number

	public Person getPassenger(int index) { 
		Person result = null;
		if (index < passengers.size()) result = (Person) passengers.elementAt(index); 
		return result;
	}
	
	// Returns true if a given person is currently in the vehicle
	
	public boolean isPassenger(Person person) {
		boolean found = false;
		
		for (int x=0; x < passengers.size(); x++)
			if (person == (Person) passengers.elementAt(x)) found = true;
			
		return found;
	} 

	// Add a new passenger to the vehicle

	public void addPassenger(Person passenger) { 
		if (!isPassenger(passenger)) passengers.addElement(passenger); 
	}
	
	// Removes a passenger from a vehicle
	
	public void removePassenger(Person passenger) {
		if (isPassenger(passenger)) {
			passengers.removeElement(passenger);
			if (passenger == driver) driver = null;
		}
	}

	// Returns driver of the vehicle
	
	public Person getDriver() { return driver; }
	
	// Sets the driver of the vehicle
	
	public void setDriver(Person driver) { this.driver = driver; }

	// Returns the current settlement vehicle is parked at.
	// Returns null if vehicle is not currently parked at a settlement.

	public Settlement getSettlement() { 
		if ((status.equals("Parked") || status.equals("Periodic Maintenance")) && (settlement != null)) return settlement; 
		else return null;
	}
	
	// Sets the settlement which the vehicle is parked at

	public void setSettlement(Settlement settlement) {
		this.settlement = settlement;
		if (settlement != null) {
			location.setCoords(settlement.getCoordinates());
			settlement.addVehicle(this);
		}
	}
	
	// Returns distance to destination in kilometers
	// Returns 0 if vehicle is not currently moving toward a destination

	public double getDistanceToDestination() { return distanceToDestination; }
	
	// Sets the vehicle's distance to its destination
	
	public void setDistanceToDestination(double distanceToDestination) { this.distanceToDestination = distanceToDestination; }

	// Gets the type of destination for the vehicle
	
	public String getDestinationType() { return new String(destinationType); }

	// Sets the type of destination for the vehicle ("Coordinates", "Settlement" or "None")

	public void setDestinationType(String destinationType) { this.destinationType = new String(destinationType); }

	// Sets the destination coordinates
	
	public void setDestination(Coordinates destinationCoords) {
		this.destinationCoords = new Coordinates(destinationCoords);
		destinationType = new String("Coordinates");
	}
	
	// Returns the destination coordinates.
	// Returns null if there is no destination.
	
	public Coordinates getDestination() { 
		if (destinationCoords != null) return new Coordinates(destinationCoords);
		else return null;
	}
	
	// Sets the destination settlement
	
	public void setDestinationSettlement(Settlement destinationSettlement) {
		this.destinationSettlement = destinationSettlement;
		if (destinationSettlement != null) {
			setDestination(destinationSettlement.getCoordinates());
			destinationType = new String("Settlement");
		}
	}

	// Returns the destination settlement.
	// Returns null if there is no destination settlement.
	
	public Settlement getDestinationSettlement() {
		if (destinationSettlement != null) return destinationSettlement;
		else return null;
	}
	
	// Returns the vehicle's size.
	
	public int getSize() { return vehicleSize; }
	
	// Returns a vector of the vehicle's current failures.
	
	public MechanicalFailure getMechanicalFailure() { return mechanicalFailure; }
	
	// Creates a new mechanical failure for the vehicle from its list of potential failures.
	
	public void newMechanicalFailure() {
		
		Object keys[] = potentialFailures.keySet().toArray();
		
		// Sum weights
		
		int totalWeight = 0;
		
		for (int x=0; x < keys.length; x++) totalWeight += ((Integer) potentialFailures.get((String) keys[x])).intValue();
		
		// Get a random number from 0 to the total weight	
			
		int r = (int) Math.round(Math.random() * (double) totalWeight);
		
		// Determine which failure is selected  
		  
		int tempWeight = ((Integer) potentialFailures.get((String) keys[0])).intValue();
		int failureNum = 0;
		while (tempWeight < r) {
			failureNum++;
			tempWeight += ((Integer) potentialFailures.get((String) keys[failureNum])).intValue();
		}
		String failureName = (String) keys[failureNum];
		
		mechanicalFailure = new MechanicalFailure(failureName);
		// System.out.println(name + " has mechanical failure: " + mechanicalFailure.getName());
	}
	
	// Add work to periodic vehicle maintenance.
	
	public void addWorkToMaintenance(int seconds) {
		
		// If vehicle has already been maintained, return.
		
		if (distanceMaint == 0D) return;
		
		// Add work to maintenance work done.
		
		maintenanceWork += seconds;
		
		// If maintenance work is complete, vehicle good for 5,000 km.
		
		if (maintenanceWork >= totalMaintenanceWork) {
			maintenanceWork = 0;
			distanceMaint = 0D;
		}
	}
	
	// Returns the current amount of work towards maintenance.
	
	public int getCurrentMaintenanceWork() { return maintenanceWork; }
	
	// Returns the total amount of work needed for maintenance.
	
	public int getTotalMaintenanceWork() { return totalMaintenanceWork; }
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