//************************ Unit Manager ************************
// Last Modified: 5/8/00

import java.util.*;

// The UnitManager class contains and manages all units in virtual Mars.
// It has methods for getting information about units.
// It is also responsible for creating all units on its construction.
// There should be only one instance of this class and it should be constructed
// and owned by the virtual Mars object.

public class UnitManager {

	// Data members

	private VirtualMars mars;          // Virtual Mars
	private Vector unitVector;         // Master list of all units
	private Vector settlementsVector;  // List of settlement units
	private Vector vehiclesVector;     // List of vehicle units
	private Vector peopleVector;       // List of people units
	

	// Constructor

	public UnitManager(VirtualMars mars) {

		// Initialize virtual mars to parameter
		
		this.mars = mars;

		// Initialize all unit vectors
		
		unitVector = new Vector();
		settlementsVector = new Vector();
		vehiclesVector = new Vector();
		peopleVector = new Vector();
		
		// Initialize unit ID counter
		
		int unitIDCounter = 0;
		
		// Initialize settlements
		
		unitIDCounter = createSettlements(unitIDCounter);
		
		// Initialize vehicles
		
		unitIDCounter = createVehicles(unitIDCounter);
		
		// Initialize people
		
		unitIDCounter = createPeople(unitIDCounter);
	}
	
	// Creates initial settlements with random locations
	
	private int createSettlements(int IDCounter) {
		
		// Initial settlement names
		
		String[] settlementNames = { "Port Zubrin", "Lowell Station", "Sagan Station", "Goddard Settlement", "Port Braun", "Burroughstown", "McKay Base",
		                             "Asimov Base", "Clarketown", "Sojourner Station", "Viking Base", "Mariner Settlement", "Camp Bradbury", "Heinlein Station", 
		                             "Schiaparelli Settlement" };

		// Set base random value
		
		Random baseRand = new Random();  
		
		// Create a settlement for each initial settlement name
		
		for (int x=0; x < settlementNames.length; x++) {
			
			// Determine random location of settlement, adjust so it will be less likely to be near the poles
			
			double settlementPhi = (baseRand.nextGaussian() * (Math.PI / 7D)) + (Math.PI / 2D);
			if (settlementPhi > Math.PI) settlementPhi = Math.PI;
			if (settlementPhi < 0D) settlementPhi = 0D;
			double settlementTheta = (double) (Math.random() * (2D * Math.PI));
			Coordinates settlementCoords = new Coordinates(settlementPhi, settlementTheta);
			
			// Create settlement at that location
			
			Settlement tempSettlement = new Settlement(IDCounter, settlementNames[x], settlementCoords, mars, this);
			
			// Add settlement to master unit and settlements vectors
			
			unitVector.addElement(tempSettlement);
			settlementsVector.addElement(tempSettlement);
			
			// Iterate unit ID number
			
			IDCounter++;
		}
		
		// Return updated unit ID number
		
		return IDCounter;
	}
	
	// Creates initial vehicles at random settlements
	
	private int createVehicles(int IDCounter) {
		
		// Create 20 rovers
		
		for (int x=0; x < 20; x++) { 
			
			// Create a rover with the name: "Mars RoverX", where X is the rover number
			
			Rover tempVehicle = new Rover(IDCounter, "Mars Rover " + (x+1), new Coordinates(0D, 0D), mars, this);
			
			// Add rover to master unit and vehicles vectors
			
			unitVector.addElement(tempVehicle);
			vehiclesVector.addElement(tempVehicle);
			
			// Place rover initially at random settlement
			
			int randSettlement = RandomUtil.getRandomInteger(settlementsVector.size() - 1);
			tempVehicle.setSettlement((Settlement) settlementsVector.elementAt(randSettlement));
			
			// Iterate unit ID number
			
			IDCounter++;
		}
		
		// Return updated unit ID number
		
		return IDCounter;
	}
	
	// Creates initial people at random settlements
	
	private int createPeople(int IDCounter) {
		
		// Initial people names
		
		// Note: The following names are those of people I know and are used with their permission in accordance to the GNU Public License
		// (see included file: "GPL_License" for details) 
		
		String[] knownPeopleNames = { "Scott Davis", "Mike Jones", "Steve Marley", "Oscar Carrol", "Connie Carrol", "Michael Zummo", "Tom Zanoni", 
		                              "Joseph Wagner", "Troy Lane", "Anson Nichols", "Heather Nichols", "Charles Kwiatkowski", "Libby Parker", "Kerrie Vaughan", 
		                              "Shawn Malmarowski", "Peter Kokh", "Matthew Giovanelli", "Karen Neder", "Doug Armstrong", "Bill Hensley", "Heidi Hensley" };
	
		// Note: The following names are fictional and any resemblance with the names of real people is purely coincidental.
	
		String[] fictionalPeopleNames = { "Jennifer Hare", "David Kern", "Gregory Brown", "Ray Kerchoff", "Victor Plantico", "Mark Chiappetta", "Michael Sobecke",
		                                  "Dan Folker", "Donna Hanson", "Jeri Axberg", "Jill Babbitz", "Enrique Deorbeta", "Tracy Hauck", "Bill Krushas",
		                                  "Alexandria Luciano", "Douglas McMartin", "Marina Plavnick", "Salvatorie Schifano", "Kerrie Stolpman", "Scott Walsh", 
		                                  "Rory Wright", "Jay Bondowzewski", "Frank Alioto", "Mary Carlson", "Adrian Chan", "Miathong Chang", "Carla Deprey", 
		                                  "John Derkson", "Ramiro Ferrucci", "Daniel Frankowiak", "Anna Harmon", "Tim Kaplan", "David Ksiciski", "Shizhe Li" };
		
		// Create a person for each initial name
		
		int totalPeople = knownPeopleNames.length + fictionalPeopleNames.length;
		for (int x=0; x < totalPeople; x++) {
			
			// Get person's name from one of the name lists
			
			String name = null;
			if (x < knownPeopleNames.length) name = knownPeopleNames[x];
			else name = fictionalPeopleNames[x - knownPeopleNames.length];
			
			// Create a person with that name
			
			Person tempPerson = new Person(IDCounter, name, new Coordinates(0D, 0D), mars, this);
			
			// Add person to master unit and people vectors
			
			unitVector.addElement(tempPerson);
			peopleVector.addElement(tempPerson);
			
			// Place person initially in random settlement
			
			int randSettlement = RandomUtil.getRandomInteger(settlementsVector.size() - 1);
			tempPerson.setSettlement((Settlement) settlementsVector.elementAt(randSettlement));
			
			// Iterate unit ID number
			
			IDCounter++;
		}
		
		// Return updated unit ID number
		
		return IDCounter;
	}
	
	// Make each person take action
	// Notify each settlement that time passes for time-related processes
	// (Note: Later automated processes may be added to vehicles)
	
	public void takeAction(int seconds) {
		for (int x=0; x < peopleVector.size(); x++) ((Person) peopleVector.elementAt(x)).takeAction(seconds); 
		for (int x=0; x < settlementsVector.size(); x++) ((Settlement) settlementsVector.elementAt(x)).timePasses(seconds);
	}

	// Get population number of a particular type of unit

	public int getSettlementNum() { return settlementsVector.size(); }
	public int getVehicleNum() { return vehiclesVector.size(); }
	public int getPeopleNum() { return peopleVector.size(); }

	// Get a random settlement
	
	public Settlement getRandomSettlement() {
		int r = RandomUtil.getRandomInteger(settlementsVector.size() - 1);
		return (Settlement) settlementsVector.elementAt(r);
	}

	// Get a random settlement other than given current one.

	public Settlement getRandomSettlement(Settlement current) {
		Settlement newSettlement = getRandomSettlement();
		while (newSettlement == current) newSettlement = getRandomSettlement();
		return newSettlement;
	}
	
	// Get a random settlement among the closest three settlements to the given location.
	
	public Settlement getRandomOfThreeClosestSettlements(Coordinates location) {
		Vector tempVector = new Vector();
		Vector resultVector = new Vector();
		
		for (int x=0; x < settlementsVector.size(); x++) {
			Settlement tempSettlement = (Settlement) settlementsVector.elementAt(x);
			if (!tempSettlement.getCoordinates().equals(location)) tempVector.addElement(tempSettlement);
		}
		
		for (int x=0; x < 3; x++) {
			Settlement nearestSettlement = null;
			double smallestDistance = 100000D;
			for (int y=0; y < tempVector.size(); y++) {
				Settlement tempSettlement = (Settlement) tempVector.elementAt(y);
				double tempDistance = location.getDistance(tempSettlement.getCoordinates());
				if ((tempDistance < smallestDistance) && (tempDistance != 0D)) {
					smallestDistance = tempDistance;
					nearestSettlement = tempSettlement;
				}
			}
			resultVector.addElement(nearestSettlement);
			tempVector.removeElement(nearestSettlement);
		}
		
		int r = RandomUtil.getRandomInteger(2);
		return (Settlement) resultVector.elementAt(r);
	}
	
	// Get a random settlement among the closest three settlements to the given settlement
	
	public Settlement getRandomOfThreeClosestSettlements(Settlement current) {
		return getRandomOfThreeClosestSettlements(current.getCoordinates());
	}
	
	// Get a unit based on its unit ID
	
	public Unit getUnit(int unitID) {
		Unit result = null;
		for (int i=0; i < unitVector.size(); i++)
			if (((Unit) unitVector.elementAt(i)).getID() == unitID) result = (Unit) unitVector.elementAt(i);
		return result;
	}
	
	// Returns an array of unit info sorted by unit name from a vector of units.
	
	private UnitInfo[] sortUnitInfo(Vector unsortedUnits) {
		
		Vector tempVector = new Vector();
		
		for (int x=0; x < unsortedUnits.size(); x++) tempVector.addElement(unsortedUnits.elementAt(x)); 
		
		Unit sorterUnit = null;
		UnitInfo[] sortedInfo = new UnitInfo[tempVector.size()];
		
		for (int x=0; x < sortedInfo.length; x++) {
			sorterUnit = (Unit) tempVector.elementAt(0);
			for (int y=0; y < tempVector.size(); y++) {
				Unit tempUnit = (Unit) tempVector.elementAt(y);
				if (tempUnit.getName().compareTo(sorterUnit.getName()) <= 0) {
					sorterUnit = tempUnit;
				}
			}
			sortedInfo[x] = sorterUnit.getUnitInfo();
			tempVector.removeElement(sorterUnit);
		}
		
		return sortedInfo;
	}
	
	// Returns an array of unit info for all settlements sorted by unit name.
	// Used with the UI.
	
	public UnitInfo[] getSettlementInfo() { return sortUnitInfo(settlementsVector);	}
	
	// Returns an array of unit info for all people sorted by unit name.
	// Used with the UI.
	
	public UnitInfo[] getPeopleInfo() { return sortUnitInfo(peopleVector); }
	
	// Returns an array of unit info for all vehicles sorted by unit name.
	// Used with the UI.
	
	public UnitInfo[] getVehicleInfo() { return sortUnitInfo(vehiclesVector); }
		
	// Returns an array of unit info for all vehicles not parked at a settlement
	// Used with the UI.
	
	public UnitInfo[] getMovingVehicleInfo() {
		
		Vector movingVehicleInfo = new Vector();
		
		for (int x=0; x < vehiclesVector.size(); x++) {
			Vehicle tempVehicle = (Vehicle) vehiclesVector.elementAt(x);
			if (tempVehicle.getSettlement() == null) 
				movingVehicleInfo.addElement(tempVehicle);
		}
		
		return sortUnitInfo(movingVehicleInfo);
	}
	
	// Returns a unit dialog for a given unit ID.
	// Used with the UI.
	
	public UnitDialog getDetailWindow(int unitID, MainDesktopPane desktop) { return getUnit(unitID).getDetailWindow(desktop); }
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