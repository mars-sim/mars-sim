/*
 * Mars Simulation Project
 * SalvageProcess.java
 * @date 2024-08-10
 * @author Scott Davis
 */
package com.mars_sim.core.manufacture;

import java.io.Serializable;

import com.mars_sim.core.building.function.Manufacture;

/**
 * A process for salvaging a piece of equipment or vehicle.
 */
public class SalvageProcess implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Data members.
    private double workTimeRemaining;
    private double averageSkillLevel;
    
    private Salvagable salvagedUnit;
    
    private Manufacture workshop;
    private SalvageProcessInfo info;
    
    /**
     * Constructor.
     * 
     * @param info information about the salvage process.
     * @param workshop the manufacturing workshop where the salvage is taking place.
     */
    public SalvageProcess(SalvageProcessInfo info, Manufacture workshop, Salvagable salvagedUnit) {
        this.info = info;
        this.workshop = workshop;
        this.salvagedUnit = salvagedUnit;
        workTimeRemaining = info.getWorkTimeRequired();
        averageSkillLevel = 0D;
    }
    
    /**
     * Gets the information about the salvage process.
     * 
     * @return process information
     */
    public SalvageProcessInfo getInfo() {
        return info;
    }
    
    /**
     * Gets the remaining work time.
     * 
     * @return work time (millisols)
     */
    public double getWorkTimeRemaining() {
        return workTimeRemaining;
    }
    
    /**
     * Adds work time to the process.
     * 
     * @param workTime work time (millisols)
     * @param skill the material science skill used for the work.
     */
    public void addWorkTime(double workTime, int skill) {
        if (workTime > workTimeRemaining) workTime = workTimeRemaining;
        workTimeRemaining -= workTime;
        
        // Add skill for work time for average skill level.
        averageSkillLevel += workTime / info.getWorkTimeRequired() * skill;
    }
    
    @Override
    public String toString() {
        return info.toString();
    }

    /**
     * Gets the manufacture building function.
     * 
     * @return manufacture building function.
     */
    public Manufacture getWorkshop() {
        return workshop;
    }
    
    /**
     * Gets the salvaged unit.
     * 
     * @return salvaged unit.
     */
    public Salvagable getSalvagedUnit() {
        return salvagedUnit;
    }
    
    /**
     * Gets the average material science skill level
     * used during the salvage process.
     * 
     * @return skill level.
     */
    public double getAverageSkillLevel() {
        return averageSkillLevel;
    }

    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {
        workshop = null;
        info = null;
        salvagedUnit = null;
    }
}
