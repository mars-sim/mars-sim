/**
 * Mars Simulation Project
 * MalfunctionManager.java
 * @version 2.75 2004-02-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.malfunction;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.events.HistoricalEvent;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.medical.*;

/**
 * The MalfunctionManager class manages the current malfunctions in a unit.
 */
public class MalfunctionManager implements Serializable {

    private static double DEFAULT_MAINTENANCE_WORK_TIME = 1000D;
    
    // Data members
    private Malfunctionable entity;          // The owning entity.
    private double timeSinceLastMaintenance; // Time passing (in millisols) since
                                             // last maintenance on entity.
    private double effectiveTimeSinceLastMaintenance; // Time (millisols) that entity has been 
                                                      // actively used since last maintenance.
    private double maintenanceWorkTime;      // The required work time for maintenance on entity.
    private double maintenanceTimeCompleted; // The completed
    private Collection scope;                // The scope strings of the unit.
    private Collection malfunctions;         // The current malfunctions in the unit.
    private Mars mars;                       // The virtual Mars.

    // Life support modifiers.
    private double oxygenFlowModifier = 100D;
    private double waterFlowModifier = 100D;
    private double airPressureModifier = 100D;
    private double temperatureModifier = 100D;

    /**
     * Constructs a MalfunctionManager object.
     */
    public MalfunctionManager(Malfunctionable entity, Mars mars) {

        // Initialize data members
        this.entity = entity;
        timeSinceLastMaintenance = 0D;
        effectiveTimeSinceLastMaintenance = 0D;
        this.mars = mars;
        scope = new ArrayList();
        malfunctions = new ArrayList();
        maintenanceWorkTime = DEFAULT_MAINTENANCE_WORK_TIME;
    }

    /**
     * Add a unit scope string to the manager.
     * @param scopeString a unit scope string
     */
    public void addScopeString(String scopeString) {
        if ((scopeString != null) && !scope.contains(scopeString))
        scope.add(scopeString);
    }

    /**
     * Checks if entity has a malfunction.
     * @return true if malfunction
     */
    public boolean hasMalfunction() {
        return (malfunctions.size() > 0);
    }

    /**
     * Checks if the entity has a given malfunction.
     * @return true if entity has malfunction
     */
    public boolean hasMalfunction(Malfunction malfunction) {
        return malfunctions.contains(malfunction);
    }

    /**
     * Checks if entity has any emergency malfunctions.
     * @return true if emergency malfunction
     */
    public boolean hasEmergencyMalfunction() {
        boolean result = false;

        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if ((malfunction.getEmergencyWorkTime() -
                    malfunction.getCompletedEmergencyWorkTime()) > 0D) result = true;
            }
        }

        return result;
    }

    /**
     * Checks if entity has any normal malfunctions.
     * @return true if normal malfunction
     */
    public boolean hasNormalMalfunction() {
        boolean result = false;

        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if ((malfunction.getWorkTime() -
                    malfunction.getCompletedWorkTime()) > 0D) result = true;
            }
        }

        return result;
    }

    /**
     * Checks if entity has any EVA malfunctions.
     * @return true if EVA malfunction
     */
    public boolean hasEVAMalfunction() {
        boolean result = false;

        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if ((malfunction.getEVAWorkTime() -
                    malfunction.getCompletedEVAWorkTime()) > 0D) result = true;
            }
        }

        return result;
    }

    /**
     * Gets a collection of the unit's current malfunctions.
     * @return malfunction collection
     */
    public Collection getMalfunctions() {
        return new ArrayList(malfunctions);
    }

    /**
     * Gets the most serious malfunction the entity has.
     * @return malfunction
     */
    public Malfunction getMostSeriousMalfunction() {

        Malfunction result = null;
        double highestSeverity = 0;

        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if ((malfunction.getSeverity() > highestSeverity) && !malfunction.isFixed()) {
                    highestSeverity = malfunction.getSeverity();
                    result = malfunction;
                }
            }
        }

        return result;
    }

    /**
     * Gets the most serious emergency malfunction the entity has.
     * @return malfunction
     */
    public Malfunction getMostSeriousEmergencyMalfunction() {

        Malfunction result = null;
        double highestSeverity = 0;

        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if ((malfunction.getEmergencyWorkTime() - malfunction.getCompletedEmergencyWorkTime()) > 0D) {
                    if (malfunction.getSeverity() > highestSeverity) {
                        highestSeverity = malfunction.getSeverity();
                        result = malfunction;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the most serious normal malfunction the entity has.
     * @return malfunction
     */
    public Malfunction getMostSeriousNormalMalfunction() {

        Malfunction result = null;
        double highestSeverity = 0;

        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if ((malfunction.getWorkTime() - malfunction.getCompletedWorkTime()) > 0D) {
                    if (malfunction.getSeverity() > highestSeverity) {
                        highestSeverity = malfunction.getSeverity();
                        result = malfunction;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the most serious EVA malfunction the entity has.
     * @return malfunction
     */
    public Malfunction getMostSeriousEVAMalfunction() {

        Malfunction result = null;
        double highestSeverity = 0;

        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if ((malfunction.getEVAWorkTime() - malfunction.getCompletedEVAWorkTime()) > 0D) {
                    if (malfunction.getSeverity() > highestSeverity) {
                        highestSeverity = malfunction.getSeverity();
                        result = malfunction;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Adds a randomly selected malfunction to the unit (if possible).
     */
    private void addMalfunction() {
        MalfunctionFactory factory = mars.getMalfunctionFactory();
        Malfunction malfunction = factory.getMalfunction(scope);
        if (malfunction != null) {
            malfunctions.add(malfunction);
            HistoricalEvent newEvent = new MalfunctionEvent(entity, malfunction, false);
            mars.getEventManager().registerNewEvent(newEvent);

            issueMedicalComplaints(malfunction);
       }
    }

    /**
     * Time passing while the unit is being actively used.
     * @param time amount of time passing (in millisols)
     */
    public void activeTimePassing(double time) {

        effectiveTimeSinceLastMaintenance += time;

        // Check for malfunction due to lack of maintenance.
        double chance = time * .0000001D * effectiveTimeSinceLastMaintenance;

        if (RandomUtil.lessThanRandPercent(chance)) {
            // System.out.println(entity.getName() + " has maintenance-triggered malfunction: " + effectiveTimeSinceLastMaintenance);
            addMalfunction();
        }
    }

    /**
     * Time passing for unit.
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {

        Collection fixedMalfunctions = new ArrayList();

        // Check if any malfunctions are fixed.
        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if (malfunction.isFixed()) fixedMalfunctions.add(malfunction);
            }
        }

        if (fixedMalfunctions.size() > 0) {
            Iterator i = fixedMalfunctions.iterator();
            while (i.hasNext()) {
                Malfunction item = (Malfunction)i.next();
                malfunctions.remove(item);
                HistoricalEvent newEvent = new MalfunctionEvent(entity, item, true);
                mars.getEventManager().registerNewEvent(newEvent);
            }
        }

        // Determine life support modifiers.
        setLifeSupportModifiers(time);

        // Deplete resources.
        depleteResources(time);
        
        // Add time passing.
		timeSinceLastMaintenance += time;
    }

    /**
     * Determine life support modifiers for given time.
     * @param time amount of time passing (in millisols)
     */
    public void setLifeSupportModifiers(double time) {

        double tempOxygenFlowModifier = 0D;
        double tempWaterFlowModifier = 0D;
        double tempAirPressureModifier = 0D;
        double tempTemperatureModifier = 0D;

        // Make any life support modifications.
        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if (malfunction.getEmergencyWorkTime() > malfunction.getCompletedEmergencyWorkTime()) {
                    Map effects = malfunction.getLifeSupportEffects();
                    if (effects.get("Oxygen") != null)
                        tempOxygenFlowModifier += ((Double) effects.get("Oxygen")).doubleValue();
                    if (effects.get("Water") != null)
                        tempWaterFlowModifier += ((Double) effects.get("Water")).doubleValue();
                    if (effects.get("Air Pressure") != null)
                        tempAirPressureModifier += ((Double) effects.get("Air Pressure")).doubleValue();
                    if (effects.get("Temperature") != null)
                        tempTemperatureModifier += ((Double) effects.get("Temperature")).doubleValue();
                }
            }
        }

        if (tempOxygenFlowModifier < 0D) oxygenFlowModifier += tempOxygenFlowModifier * time;
        else oxygenFlowModifier = 100D;

        if (tempWaterFlowModifier < 0D) waterFlowModifier += tempWaterFlowModifier * time;
        else waterFlowModifier = 100D;

        if (tempAirPressureModifier < 0D) airPressureModifier += tempAirPressureModifier * time;
        else airPressureModifier = 100D;

        if (tempTemperatureModifier != 0D) temperatureModifier += tempTemperatureModifier * time;
        else temperatureModifier = 100D;
    }

    /**
     * Depletes resources due to malfunctions.
     * @param time amount of time passing (in millisols)
     */
    public void depleteResources(double time) {

        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if (malfunction.getEmergencyWorkTime() > malfunction.getCompletedEmergencyWorkTime()) {
                    Map effects = malfunction.getResourceEffects();
                    Iterator i2 = effects.keySet().iterator();
                    while (i2.hasNext()) {
                        String key = (String) i2.next();
                        double amount = ((Double) effects.get(key)).doubleValue();
                        entity.getInventory().removeResource(key, amount * time);
                    }
                }
            }
        }
    }

    /**
     * Called when the unit has an accident.
     */
    public void accident() {

        // System.out.println(entity.getName() + " accident()");

        // Multiple malfunctions may have occured.
        // 50% one malfunction, 25% two etc.
        boolean done = false;
        double chance = 100D;
        while (!done) {
            if (RandomUtil.lessThanRandPercent(chance)) {
                addMalfunction();
                chance /= 2D;
            }
            else done = true;
        }
    }

    /**
     * Gets the time since last maintenance on entity.
     * @return time (in millisols)
     */
    public double getTimeSinceLastMaintenance() {
        return timeSinceLastMaintenance;
    }
    
    /**
     * Gets the time the entity has been actively used
     * since its last maintenance.
     * @return time (in millisols)
     */
    public double getEffectiveTimeSinceLastMaintenance() {
    	return effectiveTimeSinceLastMaintenance;
    }

    /**
     * Gets the required work time for maintenance for the entity.
     * @return time (in millisols)
     */
    public double getMaintenanceWorkTime() {
        return maintenanceWorkTime;
    }

    /**
     * Sets the required work time for maintenance for the entity.
     * @param maintenanceWorkTime (in millisols)
     */
    public void setMaintenanceWorkTime(double maintenanceWorkTime) {
        this.maintenanceWorkTime = maintenanceWorkTime;
    }

    /**
     * Gets the work time completed on maintenance.
     * @return time (in millisols)
     */
    public double getMaintenanceWorkTimeCompleted() {
        return maintenanceTimeCompleted;
    }

    /**
     * Add work time to maintenance.
     * @param time (in millisols)
     */
    public void addMaintenanceWorkTime(double time) {
        maintenanceTimeCompleted += time;
        if (maintenanceTimeCompleted >= maintenanceWorkTime) {
            maintenanceTimeCompleted = 0D;
            timeSinceLastMaintenance = 0D;
            effectiveTimeSinceLastMaintenance = 0D;
        }
    }

    /**
     * Issues any necessary medical complaints.
     * @param malfunction the new malfunction
     */
    public void issueMedicalComplaints(Malfunction malfunction) {

        // Get people who can be affected by this malfunction.
        PersonCollection people = entity.getAffectedPeople();

        // Determine medical complaints for each malfunction.
        Iterator i1 = malfunction.getMedicalComplaints().keySet().iterator();
        while (i1.hasNext()) {
            String complaintName = (String) i1.next();
            double probability = ((Double) malfunction.getMedicalComplaints().get(complaintName)).doubleValue();
            MedicalManager medic = mars.getMedicalManager();
            Complaint complaint = medic.getComplaintByName(complaintName);
            if (complaint != null) {
                PersonIterator i2 = people.iterator();
                while (i2.hasNext()) {
                    Person person = i2.next();
                    if (RandomUtil.lessThanRandPercent(probability))
                    person.getPhysicalCondition().addMedicalComplaint(complaint);
                }
            }
        }
    }

    /**
     * Gets the oxygen flow modifier.
     * @return modifier
     */
    public double getOxygenFlowModifier() {
        return oxygenFlowModifier;
    }

    /**
     * Gets the water flow modifier.
     * @return modifier
     */
    public double getWaterFlowModifier() {
        return waterFlowModifier;
    }

    /**
     * Gets the air flow modifier.
     * @return modifier
     */
    public double getAirPressureModifier() {
        return airPressureModifier;
    }

    /**
     * Gets the temperature modifier.
     * @return modifier
     */
    public double getTemperatureModifier() {
        return temperatureModifier;
    }
}
