/**
 * Mars Simulation Project
 * UnitManager.java
 * @version 2.70 2000-08-31
 * @author Scott Davis
 */

import java.util.*;

/** The UnitManager class contains and manages all units in virtual
 *  Mars. It has methods for getting information about units. It is
 *  also responsible for creating all units on its construction.
 *  There should be only one instance of this class and it should be
 *  constructed and owned by the virtual Mars object.
 */
public class UnitManager {

    private VirtualMars mars;          // Virtual Mars
    // should use collections here rather than Vector... (gregwhelan)
    private Vector unitVector;         // Master list of all units
    private Vector settlementsVector;  // List of settlement units
    private Vector vehiclesVector;     // List of vehicle units
    private Vector peopleVector;       // List of people units
	
    public UnitManager(VirtualMars mars) {

	// Initialize virtual mars to parameter
	this.mars = mars;

	// Initialize all unit vectors
	unitVector = new Vector();
	settlementsVector = new Vector();
	vehiclesVector = new Vector();
	peopleVector = new Vector();
		
	createSettlements();
	createVehicles(20);            // create 20 rovers
	createPeople();
    }
	
    /** Creates initial settlements with random locations */
    private void createSettlements() {
		
	// Initial settlement names
	String[] settlementNames = { "Port Zubrin", "Lowell Station", "Sagan Station",
				     "Goddard Settlement", "Port Braun", "Burroughstown", "McKay Base",
				     "Asimov Base", "Clarketown", "Sojourner Station", "Viking Base",
				     "Mariner Settlement", "Camp Bradbury", "Heinlein Station",
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
	    createSettlement(settlementNames[x], settlementCoords);
	}
    }

    private void createSettlement(String name, Coordinates coords) {
	Settlement tempSettlement = new Settlement(name, coords, mars, this);
	// Add settlement to master unit and settlements vectors
	unitVector.addElement(tempSettlement);
	settlementsVector.addElement(tempSettlement);
    }

	
    /** Creates initial vehicles at random settlements */
    private void createVehicles(int numRovers) {
	for (int x=0; x < numRovers; x++) { 
	    // Create a rover with the name: "Mars Rover X", where X is the rover number
	    // Place rover initially at random settlement
	    int randSettlement = RandomUtil.getRandomInteger(settlementsVector.size() - 1);
	    createVehicle("Mars Rover " + (x+1),
			  new Coordinates(0D, 0D),
			  (Settlement) settlementsVector.elementAt(randSettlement));
	}
    }
    
    public void createVehicle(String name, Coordinates coords, Settlement seti) {
	Rover tempVehicle = new Rover(name, coords, mars, this);
	// Add rover to master unit and vehicles vectors
	unitVector.addElement(tempVehicle);
	vehiclesVector.addElement(tempVehicle);
	tempVehicle.setSettlement(seti);
    }
	
    /** Creates initial people at random settlements */
    private void createPeople() {
		
	// Initial people names
	// Note: The following names are those of people I know and are used
	// with their permission in accordance to the GNU Public License
	// (see included file: "GPL_License" for details) 
	String[] knownPeopleNames = { "Scott Davis", "Mike Jones", "Steve Marley", "Oscar Carrol",
				      "Connie Carrol", "Michael Zummo", "Tom Zanoni", "Joseph Wagner",
				      "Troy Lane", "Anson Nichols", "Heather Nichols", "Charles Kwiatkowski",
				      "Libby Parker", "Kerrie Vaughan", "Shawn Malmarowski", "Peter Kokh",
				      "Matthew Giovanelli", "Karen Neder", "Doug Armstrong", "Bill Hensley",
				      "Heidi Hensley", "Greg Whelan" };
	
	// Note: The following names are fictional and any resemblance with
	// the names of real people is purely coincidental.
	String[] fictionalPeopleNames = { "Jennifer Hare", "David Kern", "Gregory Brown", "Ray Kerchoff",
					  "Victor Plantico", "Mark Chiappetta", "Michael Sobecke",
					  "Dan Folker", "Donna Hanson", "Jeri Axberg", "Jill Babbitz",
					  "Enrique Deorbeta", "Tracy Hauck", "Bill Krushas",
					  "Alexandria Luciano", "Douglas McMartin", "Marina Plavnick",
					  "Salvatorie Schifano", "Kerrie Stolpman", "Scott Walsh", 
					  "Rory Wright", "Jay Bondowzewski", "Frank Alioto", "Mary Carlson",
					  "Adrian Chan", "Miathong Chang", "Carla Deprey", 
					  "John Derkson", "Ramiro Ferrucci", "Daniel Frankowiak",
					  "Anna Harmon", "Tim Kaplan", "David Ksiciski", "Shizhe Li" };
		
	// Create a person for each initial name
	int totalPeople = knownPeopleNames.length + fictionalPeopleNames.length;
	for (int x=0; x < totalPeople; x++) {
			
	    // Get person's name from one of the name lists
	    String name = null;
	    if (x < knownPeopleNames.length) {
		name = knownPeopleNames[x];
	    } else {
		name = fictionalPeopleNames[x - knownPeopleNames.length];
	    }

	    // Place person initially in random settlement
	    int randSettlement = RandomUtil.getRandomInteger(settlementsVector.size() - 1);
	    createPerson(name, new Coordinates(0D, 0D),
			 (Settlement)settlementsVector.elementAt(randSettlement));
	}
    }

    private void createPerson(String name, Coordinates coord, Settlement seti) {
	// Create a person with that name
	Person tempPerson = new Person(name, coord, mars, this);
	// Add person to master unit and people vectors
	unitVector.addElement(tempPerson);
	peopleVector.addElement(tempPerson);
	tempPerson.setSettlement(seti);
    }
	    

    /** Notify all the units that time has passed. Times they are a
     *  changing.  */
    public void takeAction(int seconds) {
	for (int x=0; x < unitVector.size(); x++) {
	    ((Unit)unitVector.elementAt(x)).timePasses(seconds);
	}
    }

    /** Get number of settlements */
    public int getSettlementNum() {
	return settlementsVector.size();
    }

    /** Get number of vehicles */
    public int getVehicleNum() {
	return vehiclesVector.size();
    }

    /** Get population */
    public int getPeopleNum() {
	return peopleVector.size();
    }

    /** Get a random settlement */
    public Settlement getRandomSettlement() {
	int r = RandomUtil.getRandomInteger(settlementsVector.size() - 1);
	return (Settlement) settlementsVector.elementAt(r);
    }

    /** Get a random settlement other than given current one. */
    public Settlement getRandomSettlement(Settlement current) {
	Settlement newSettlement;
	do {
	    newSettlement = getRandomSettlement();
	} while (newSettlement == current);

	return newSettlement;
    }
	
    /** Get a random settlement among the closest three settlements to
     *   the given location.
     */
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
	
    /** Get a random settlement among the closest three settlements to
     *  the given settlement
     */
    public Settlement getRandomOfThreeClosestSettlements(Settlement current) {
	return getRandomOfThreeClosestSettlements(current.getCoordinates());
    }
	
    /** Get a unit based on its unit ID */
    public Unit getUnit(int unitID) {
	Unit result = null;
	for (int i=0; i < unitVector.size(); i++) {
	    if (((Unit) unitVector.elementAt(i)).getID() == unitID) {
		result = (Unit) unitVector.elementAt(i);
	    }
	}
	return result;
    }
	
    /** Returns an array of unit info sorted by unit name from a vector of units. */
    private UnitInfo[] sortUnitInfo(Vector unsortedUnits) {
		
	Vector tempVector = new Vector();
		
	for (int x=0; x < unsortedUnits.size(); x++) {
	    tempVector.addElement(unsortedUnits.elementAt(x));
	}
		
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
	
    /** Returns an array of unit info for all settlements sorted by
     *  unit name.  Used with the UI.
     */
    public UnitInfo[] getSettlementInfo() {
	return sortUnitInfo(settlementsVector);
    }
	
    /** Returns an array of unit info for all people sorted by unit
     *  name.  Used with the UI.
     */
    public UnitInfo[] getPeopleInfo() {
	return sortUnitInfo(peopleVector);
    }
	
    /** Returns an array of unit info for all vehicles sorted by unit
     *  name.  Used with the UI.
     */
    public UnitInfo[] getVehicleInfo() {
	return sortUnitInfo(vehiclesVector);
    }
		
    /** Returns an array of unit info for all vehicles not parked at a
     *  settlement. Used with the UI.
     */
    public UnitInfo[] getMovingVehicleInfo() {
	Vector movingVehicleInfo = new Vector();
	
	for (int x=0; x < vehiclesVector.size(); x++) {
	    Vehicle tempVehicle = (Vehicle) vehiclesVector.elementAt(x);
	    if (tempVehicle.getSettlement() == null) {
		movingVehicleInfo.addElement(tempVehicle);
	    }
	}
	return sortUnitInfo(movingVehicleInfo);
    }
	
    /** Returns a unit dialog for a given unit ID. Used with the UI. */
    public UnitDialog getDetailWindow(int unitID, MainDesktopPane desktop) {
	return getUnit(unitID).getDetailWindow(desktop);
    }
}
