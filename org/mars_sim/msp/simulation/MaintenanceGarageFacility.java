/**
 * Mars Simulation Project
 * MaintenanceGarageFacility.java
 * @version 2.72 2001-06-24
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.Vector;

/**
 * The MaintenanceGarageFacility class represents the pressurized maintenance garage in a settlement.
 * Vehicles can be taken to a maintenance garage for repair and maintenance.
 * Note: Any number or size of vehicles can always be parked outside a settlement.  The garage's
 * capacity only reflects those vehicles in the garage itself.
 */
public class MaintenanceGarageFacility extends Facility {

    // Data members
    private int maxVehicleSize; // The maximum size of vehicle the garage can accomidate.
    private int maxSizeCapacity; // The total size point sum of vehicles the garage can accomidate at any given time.
    private int currentSizeSum; // The current sum of vehicle size points in the garage.
    private Vector vehicles; // A list of vehicles currently in the garage.

    /** Constructor for random creation. 
     *  @param manager the garage's facility manager
     */
    MaintenanceGarageFacility(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, "Maintenance Garage");

        // Initialize data members
        vehicles = new Vector();

        // Initialize random maxVehicleSize from 2 to 5.
        maxVehicleSize = 2 + RandomUtil.getRandomInt(3);

        // Initialize random maxSizeCapacity from maxVehicleSize to 5x maxVehicleSize.
        maxSizeCapacity = maxVehicleSize + (RandomUtil.getRandomInt(4 * maxVehicleSize));
    }

    /** Constructor for set values (used later when facilities can be built or upgraded.) 
     *  @param manager the garage's facility manager
     *  @param maxVehicleSize maximum size of vehicle the garage can accomidate
     *  @param maxSiceCapacity total size point sum of vehicles the garage can accomidate at any given time
     */
    MaintenanceGarageFacility(FacilityManager manager, int maxVehicleSize, int maxSizeCapacity) {

        // Use Facility's constructor.
        super(manager, "Maintenance Garage");

        // Initialize data members.
        vehicles = new Vector();
        this.maxVehicleSize = maxVehicleSize;
        this.maxSizeCapacity = maxSizeCapacity;
    }

    /** Returns the maximum vehicle size the garage can accomidate. 
     *  @return the maximum vehicle size the garage can accomidate
     */
    public int getMaxVehicleSize() {
        return maxVehicleSize;
    }

    /** Returns the total size point sum of vehicles the garage can accomidate at any given time. 
     *  @return total size point sum of vehicles the garage can accomidate at any given time
     */
    public int getMaxSizeCapacity() {
        return maxSizeCapacity;
    }

    /** Returns the sum of vehicle sizes currently in the garage. 
     *  @return sum of vehicle sizes currently in the garage
     */
    public int getTotalSize() {
        int result = 0;
        for (int x = 0; x < vehicles.size(); x++)
            result += ((Vehicle) vehicles.elementAt(x)).getSize();

        return result;
    }

    /** Add vehicle to garage if there's room.
      * @return true if vehicle has been added successfully.
      * False if vehicle could not be added.
      */
    public boolean addVehicle(Vehicle vehicle) {
        int vehicleSize = vehicle.getSize();

        // If vehicle is within the size limitations of the garage, add it.
        if (vehicleSize <= maxVehicleSize) {
            if ((vehicleSize + currentSizeSum) <= maxSizeCapacity) {
                vehicles.addElement(vehicle);
                currentSizeSum += vehicleSize;
                return true;
            }
        }

        return false;
    }

    /** Removes a vehicle from the garage.
     *  If the vehicle is not in the garage, does nothing.
     *  @param vehicle vehicle to be removed
     */
    public void removeVehicle(Vehicle vehicle) {
        if (vehicleInGarage(vehicle)) {
            vehicles.removeElement(vehicle);
            currentSizeSum -= vehicle.getSize();
        }
    }

    /** Returns true if vehicle is currently in the garage.
     *  Returns false otherwise.
     *  @return true if vehicle is currently in the garage
     */
    public boolean vehicleInGarage(Vehicle vehicle) {
        boolean result = false;

        for (int x = 0; x < vehicles.size(); x++) {
            if (vehicle == vehicles.elementAt(x))
                result = true;
        }

        return result;
    }

    /** Returns an array of vehicle names of the vehicles currently in the garage. 
     *  @return array of vehicle names for vehicles currently in the garage
     */
    public String[] getVehicleNames() {
        String[] result = new String[vehicles.size()];

        for (int x = 0; x < vehicles.size(); x++)
            result[x] = ((Vehicle) vehicles.elementAt(x)).getName();

        return result;
    }

    /** Returns an array of vehicles currently in the garage. 
     *  @return array of vehicles currently in the garage
     */
    public Vehicle[] getVehicles() {
        Vehicle[] result = new Vehicle[vehicles.size()];

        for (int x = 0; x < vehicles.size(); x++)
            result[x] = (Vehicle) vehicles.elementAt(x);

        return result;
    }
}
