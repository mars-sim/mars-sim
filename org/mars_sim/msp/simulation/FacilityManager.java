/**
 * Mars Simulation Project
 * FacilityManager.java
 * @version 2.72 2001-04-25
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.Vector;

/** The FacilityManager class manages a settlement's facilities.
 *  There is only one facility manager for each settlement.
 */
public class FacilityManager {

    // Data members
    private Settlement settlement; // The settlement the facility manager belongs to.
    private Vector facilityList; // Unordered List of the settlement's facilities.

    /** Constructs a FacilityManager object
     *  @param settlement parent settlement
     */
    FacilityManager(Settlement settlement) {

        // Initialize settlement
        this.settlement = settlement;

        // Initialize facilities for the settlement.
        facilityList = new Vector();

        // Add manditory facilities to manager.
	LivingQuartersFacility quarters = new LivingQuartersFacility(this);
        facilityList.addElement(quarters);
        facilityList.addElement(new GreenhouseFacility(this, quarters));
        facilityList.addElement(new StoreroomFacility(this));
        facilityList.addElement(new MaintenanceGarageFacility(this));
        facilityList.addElement(new LaboratoryFacility(this));
        facilityList.addElement(new InsituResourceProcessorFacility(this));
    }

    /** Returns the settlement the owns this facility manager. 
     *  @return parent settlement
     */
    public Settlement getSettlement() {
        return settlement;
    }

    /** Returns the number of facilities in the manager. 
     *  @return number of facilities the manager controls
     */
    public int getFacilityNum() {
        return facilityList.size();
    }

    /** Returns a facility given an index number. If there is no
     *  facility at that index number, return null. 
     *  @param index index number of the requested facility
     *  @return requested facility
     */
    public Facility getFacility(int index) {
        if ((index >= 0) && (index < facilityList.size())) {
            return (Facility) facilityList.elementAt(index);
        } else {
            return null;
        }
    }

    /** Returns a facility given its name. If there is no facility of
     *  the given name, return null. 
     *  @param name name of the requested facility
     *  @return requested facility
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

    /** Sends facilities a time pulse. 
     *  @param seconds number of seconds of time passing
     */
    void timePasses(double seconds) {
        for (int x = 0; x < facilityList.size(); x++) {
            ((Facility) facilityList.elementAt(x)).timePasses(seconds);
        }
    }
}
