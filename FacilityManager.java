/**
 * Mars Simulation Project
 * FacilityManager.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

import java.util.*;

/** The FacilityManager class manages a settlement's facilities.
 *  There is only one facility manager for each settlement.
 */
public class FacilityManager {

    private Settlement settlement; // The settlement the facility manager belongs to.
    private Vector facilityList; // Unordered List of the settlement's facilities.

    public FacilityManager(Settlement settlement) {

        // Initialize settlement
        this.settlement = settlement;

        // Initialize facilities for the settlement.
        facilityList = new Vector();

        // Add manditory facilities to manager.
        facilityList.addElement(new LivingQuartersFacility(this));
        facilityList.addElement(new GreenhouseFacility(this));
        facilityList.addElement(new StoreroomFacility(this));
        facilityList.addElement(new MaintenanceGarageFacility(this));
        facilityList.addElement(new LaboratoryFacility(this));
    }

    /** Returns the settlement the owns this facility manager. */
    public Settlement getSettlement() {
        return settlement;
    }

    /** Returns the number of facilities in the manager. */
    public int getFacilityNum() {
        return facilityList.size();
    }

    /** Returns a facility given an index number. If there is no
       *  facility at that index number, return null. */
    public Facility getFacility(int index) {
        if ((index >= 0) && (index < facilityList.size())) {
            return (Facility) facilityList.elementAt(index);
        } else {
            return null;
        }
    }

    /** Returns a facility given its name. If there is no facility of
       *  the given name, return null.
       */
    public Facility getFacility(String name) {
        for (int x = 0; x < facilityList.size(); x++) {
            Facility tempFacility = getFacility(x);
            if (tempFacility.getName().equals(name)) {
                return tempFacility;
            }
        }
        return null;
    }

    /** Sends facilities time pulse. */
    public void timePasses(int seconds) {
        for (int x = 0; x < facilityList.size(); x++) {
            ((Facility) facilityList.elementAt(x)).timePasses(seconds);
        }
    }
}
