/**
 * Mars Simulation Project
 * EmbarkFromSettlement.java
 * @version 2.72 2001-07-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import java.util.Vector;
import org.mars_sim.msp.simulation.*;

/** The EmbarkFromSettlement class is a task for procuring and preparing a vehicle 
 *  from a settlement for travel. 
 */
class EmbarkFromSettlement extends Task {

    // Data members
    private Settlement destinationSettlement;
    private Coordinates destination;
    private Vehicle vehicle;
    private Vector passengers;

    /** Constructs a EmbarkFromSettlement object with a given destination settlement
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param destinationSettlement the destination settlement
     */
    public EmbarkFromSettlement(Person person, VirtualMars mars, Settlement destinationSettlement) {
        super("Embarking from " + person.getSettlement().getName(), person, mars);

        this.destinationSettlement = destinationSettlement;
        destination = destinationSettlement.getCoordinates();
        vehicle = null;
        passengers = new Vector();
    }

    /** Constructs a EmbarkFromSettlement object with given destination coordinates.
     *  
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param destination the destination coordinates 
     */
    public EmbarkFromSettlement(Person person, VirtualMars mars, Coordinates destination) {
        super("Embarking from " + person.getSettlement().getName(), person, mars);

        this.destination = destination;
        destinationSettlement = null;
        vehicle = null;
        passengers = new Vector();
    }

    /** Performs this task for a given period of time 
     *  @param time amount of time to perform task (in millisols) 
     */
    double doTask(double time) {
        double timeLeft = super.doTask(time);
        if (subTask != null) return timeLeft;

        if (phase.equals("")) {
            phase = "Reserve Vehicle";
            phaseTimeCompleted = 0D;
        }

        if (phase.equals("Reserve Vehicle")) {
            timeLeft = reserveVehicle(timeLeft);
            if ((timeLeft > 0D) && !isDone) {
                phase = "Load Vehicle";
                phaseTimeCompleted = 0D;
            }
        }

        if (phase.equals("Load Vehicle")) {
            timeLeft = loadVehicle(timeLeft);
            if ((timeLeft > 0D) && !isDone) {
                phase = "Invite Passengers";
                phaseTimeCompleted = 0D;
            }
        }

        if (phase.equals("Invite Passengers")) {
            timeLeft = invitePassengers(timeLeft);
            if ((timeLeft > 0D) && !isDone) {
                phase = "Enter Vehicle";
                phaseTimeCompleted = 0D;
            }
        }
   
        if (phase.equals("Enter Vehicle")) {
            timeLeft = enterVehicle(timeLeft);
            if (timeLeft > 0D) isDone = true;
        } 

        return timeLeft; 
    }

    /** Finds a suitable vehicle from the settlement and reserves it
     *  so no one else will take it.
     *  If suitable vehicle cannot be found, the task is stopped.
     *
     *  @param time the amount of time to perform the phase
     *  @return the amount of time remaining after performing phase
     */
    private double reserveVehicle(double time) {
        phaseTimeRequired = 20D; // (20 microsols)

        phaseTimeCompleted += time;
        if (phaseTimeCompleted >= phaseTimeRequired) {

            Settlement embarkingSettlement = person.getSettlement();
            FacilityManager facilityManager = embarkingSettlement.getFacilityManager();
            MaintenanceGarageFacility garage = (MaintenanceGarageFacility) facilityManager.getFacility("Maintenance Garage"); 

            for (int x=0; x < embarkingSettlement.getVehicleNum(); x++) {
                Vehicle tempVehicle = embarkingSettlement.getVehicle(x);
                if (!tempVehicle.isReserved() && !garage.vehicleInGarage(tempVehicle)) {
                    if (tempVehicle.getRange() > person.getCoordinates().getDistance(destination)) {
                        vehicle = tempVehicle;
                        tempVehicle.setReserved(true);
                    }
                }
            }

            if (vehicle == null) isDone = true; 

            return phaseTimeCompleted - phaseTimeRequired;
        }
        else return 0D;
    }

    /** Loads vehicle with fuel and supplies.
     *  If the vehicle cannot be properly supplied, the task is stopped.
     *  @param time the amount of time to perform the phase
     *  @return the amount of time remaining after performing phase
     */
    private double loadVehicle(double time) {
        phaseTimeRequired = 10D; // (10 microsols)

        phaseTimeCompleted += time;
        if (phaseTimeCompleted >= phaseTimeRequired) {
            
            Settlement embarkingSettlement = person.getSettlement();
            FacilityManager facilityManager = embarkingSettlement.getFacilityManager();
            StoreroomFacility storage = (StoreroomFacility) facilityManager.getFacility("Storerooms"); 
            boolean resourcesAvailable = true;        

            // Fill vehicle with fuel.
            double neededFuel = vehicle.getFuelCapacity() - vehicle.getFuel();
            if (neededFuel < storage.getFuelStores() - 50D) {
                storage.removeFuel(neededFuel);
                vehicle.addFuel(neededFuel);
            }
            else resourcesAvailable = false;
            
            // Fill vehicle with oxygen.
            double neededOxygen = vehicle.getOxygenCapacity() - vehicle.getOxygen();
            if (neededOxygen < storage.getOxygenStores() - 50D) {
                storage.removeOxygen(neededOxygen);
                vehicle.addOxygen(neededOxygen);
            }
            else resourcesAvailable = false;

            // Fill vehicle with water.
            double neededWater = vehicle.getWaterCapacity() - vehicle.getWater();
            if (neededWater < storage.getWaterStores() - 50D) {
                storage.removeWater(neededWater);
                vehicle.addWater(neededWater);
            }
            else resourcesAvailable = false;
            
            // Fill vehicle with food.
            double neededFood = vehicle.getFoodCapacity() - vehicle.getFood();
            if (neededFood < storage.getFoodStores() - 50D) {
                storage.removeFood(neededFood);
                vehicle.addFood(neededFood);
            }
            else resourcesAvailable = false;

            if (!resourcesAvailable) { 
                vehicle.setReserved(false);
                isDone = true;
            }

            return phaseTimeCompleted - phaseTimeRequired;
        }
        else return 0;
    }

    /** Invite other people along for the trip. 
     *
     *  @param time the amount of time to perform the phase
     *  @return the amount of time remaining after performing phase
     */
    private double invitePassengers(double time) {
        phaseTimeRequired = 25; // (25 microsols)

        phaseTimeCompleted += time;
        if (phaseTimeCompleted >= phaseTimeRequired) {

            // Determine destination settlement capacity (if destination is a settlement)
            int destinationSettlementCapacity = 0;
            if (destinationSettlement != null) {
                // Determine current capacity of settlement
                FacilityManager manager = destinationSettlement.getFacilityManager();
                LivingQuartersFacility quarters = (LivingQuartersFacility) manager.getFacility("Living Quarters");
                destinationSettlementCapacity = quarters.getMaximumCapacity() - quarters.getCurrentPopulation();
          
                // Subtract number of people currently traveling to settlement
                Vehicle[] vehicles = person.getUnitManager().getVehicles();
                for (int x=0; x < vehicles.length; x++) {
                    Settlement tempSettlement = vehicles[x].getDestinationSettlement();
                    if ((tempSettlement != null) && (tempSettlement == destinationSettlement)) 
                        destinationSettlementCapacity -= vehicles[x].getPassengerNum();
                }
            }

            // Add new passengers to passenger list.
            Settlement embarkingSettlement = person.getSettlement();
            for (int x=0; x < embarkingSettlement.getPeopleNum(); x++) {
                Person tempPerson = embarkingSettlement.getPerson(x);
                if (tempPerson != person) {
                    boolean goingWith = true;

                    Task tempTask = tempPerson.getTaskManager().getCurrentTask();
                    if ((tempTask != null) && !tempTask.getName().equals("Relaxing")) goingWith = false;
 
                    if (vehicle.getPassengerNum() == vehicle.getMaxPassengers()) goingWith = false; 

                    if (destinationSettlement != null) {
                        if (destinationSettlementCapacity < vehicle.getPassengerNum() + 1) goingWith = false;
                    }
         
                    if (RandomUtil.lessThanRandPercent(50)) goingWith = false;

                    if (goingWith) passengers.add(tempPerson);
                }
            }  

            return phaseTimeCompleted - phaseTimeRequired;
        }
        else return 0;
    }

    /** Driver and passengers enter vehicle and strap themselves in. 
     *
     *  @param time the amount of time to perform the phase
     *  @return the amount of time remaining after performing phase
     */
    private double enterVehicle(double time) {
        phaseTimeRequired = 10; // (10 microsols)

        phaseTimeCompleted += time;
        if (phaseTimeCompleted >= phaseTimeRequired) {

            Settlement embarkingSettlement = person.getSettlement();

            // Prepare driver
            person.setVehicle(vehicle);  // Should already be set
            person.setLocationSituation("In Vehicle");
            vehicle.addPassenger(person);
            embarkingSettlement.personLeave(person);

            // Prepare passengers
            for (int x=0; x < passengers.size(); x++) {
                Person passenger = (Person) passengers.get(x);
                if (passenger.getVehicle() == null) {
                    passenger.getTaskManager().clearCurrentTask();
                    passenger.setVehicle(vehicle);
                    passenger.setLocationSituation("In Vehicle");
                    embarkingSettlement.personLeave(person);
                    vehicle.addPassenger(passenger);
                }
            }

            // Prepare vehicle
            vehicle.setDriver(person);
            embarkingSettlement.vehicleLeave(vehicle);        
            vehicle.setStatus("Moving");
            vehicle.setSettlement(null);
            vehicle.setReserved(false);
            vehicle.setDestination(destination);
            vehicle.setDestinationSettlement(destinationSettlement);
            if (destinationSettlement != null) vehicle.setDestinationType("Settlement");
            else vehicle.setDestinationType("Coordinates");

            return phaseTimeCompleted - phaseTimeRequired;
        }
        else return 0;
    }
}
