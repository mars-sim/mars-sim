/**
 * Mars Simulation Project
 * VirtualMars.java
 * @version 2.70 2000-08-31
 * @author Scott Davis
 */

import java.util.*;

/** The VirtualMars class represents virtual Mars in the simulation.
 *  It contains all the units, a master clock, and access to the
 *  topography data.
 */
public class VirtualMars {

    private TerrainElevation elevationMap; // Terrain elevation of Mars
    private UnitManager units;             // Unit controller
    private MasterClock masterClock;       // Master clock for virtual world
    
    public VirtualMars() {

	// initialize terrain
	elevationMap =
	    new TerrainElevation("TopoMarsMap.dat", "TopoMarsMap.index", "TopoMarsMap.sum");
		
	// initialize all units
	units = new UnitManager(this);
		
	// initialize and start master clock
	masterClock = new MasterClock(this);
	masterClock.start();
    }
	
    /** Clock pulse from master clock */
    public void clockPulse(int seconds) {
	units.takeAction(seconds);
    }
	
    /** Returns terrain elevation object */
    public TerrainElevation getElevationMap() {
	return elevationMap;
    }
	
    //**************************** UI Accessor Methods ****************************
	
    /** returns an array of unit info for all moving vehicles sorted by name */
    public UnitInfo[] getMovingVehicleInfo() {
	return units.getMovingVehicleInfo();
    }
	
    /** returns an array of unit info for all vehicles sorted by name */
    public UnitInfo[] getVehicleInfo() {
	return units.getVehicleInfo();
    }
	
    /** returns an array of unit info for all settlements sorted by name */
    public UnitInfo[] getSettlementInfo() {
	return units.getSettlementInfo();
    }
	
    /** Returns an array of unit info for all people sorted by name */
    public UnitInfo[] getPeopleInfo() {
	return units.getPeopleInfo();
    }
	
    /** Returns a unit dialog for a given unit ID */
    public UnitDialog getDetailWindow(int unitID, MainDesktopPane desktop) {
	return units.getDetailWindow(unitID, desktop);
    };
}
