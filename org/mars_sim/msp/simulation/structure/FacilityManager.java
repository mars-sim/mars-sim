/**
 * Mars Simulation Project
 * FacilityManager.java
 * @version 2.74 2002-03-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import java.util.Vector;
import java.io.Serializable;

/** The FacilityManager class manages a settlement's facilities.
 *  There is only one facility manager for each settlement.
 */
public class FacilityManager implements Serializable {

    // Data members
    private Mars mars; // The virtual Mars instance
    private Settlement settlement; // The settlement the facility manager belongs to.
    private Vector facilityList; // Unordered List of the settlement's facilities.

    /** Constructs a FacilityManager object
     *  @param settlement parent settlement
     */
    FacilityManager(Settlement settlement, Mars mars) {

        // Initialize data members
        this.settlement = settlement;
        this.mars = mars;

        // Initialize facilities for the settlement.
        facilityList = new Vector();

        // Add manditory facilities to manager.
        facilityList.addElement(new Greenhouse(this));
        facilityList.addElement(new MaintenanceGarage(this));
        facilityList.addElement(new Laboratory(this));
        facilityList.addElement(new InsituResourceProcessor(this));
        facilityList.addElement(new Infirmary(this));
    }

    /** Returns the settlement the owns this facility manager.
     *  @return parent settlement
     */
    public Settlement getSettlement() {
        return settlement;
    }

    /** Returns the virtual Mars instance.
     *  @return virtual Mars instance
     */
    public Mars getMars() {
        return mars;
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

    /** Time passing for all the facilities
     *  @param time the amount of time passing (in millisols)
     */
    void timePassing(double time) {
        for (int x = 0; x < facilityList.size(); x++)
            ((Facility) facilityList.elementAt(x)).timePassing(time);
    }
}
