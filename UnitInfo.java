/**
 * Mars Simulation Project
 * UnitInfo.java
 * @version 2.70 2000-02-29
 * @author Scott Davis
 */


/** The UnitInfo class represents basic information about a unit.  The
 *  UI commonly requests this information from the virtual Mars.
 */
public class UnitInfo {

    private Unit unit;                 // Particular Unit referenced by UnitInfo
    private int unitID;                // Unit's ID number
    private String unitName;           // Unit's name
	
    public UnitInfo(Unit unit, int unitID, String unitName) {
	
	// Initialize globals to arguments
	this.unit = unit;
	this.unitID = unitID;
	this.unitName = unitName;
    }
	
    /** Returns unit's ID number */
    public int getID() {
	return unitID;
    }
	
    /** Returns unit's name */
    public String getName() {
	return unitName;
    }
	
    /** Returns unit's location coordinates */
    public Coordinates getCoords() {
	return unit.getCoordinates();
    }
}
