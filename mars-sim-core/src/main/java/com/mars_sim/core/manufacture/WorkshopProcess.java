/*
 * Mars Simulation Project
 * WorkshopProcess.java
 * @date 2025-04-17
 * @author Barry Evans
 */
package com.mars_sim.core.manufacture;

import java.io.Serializable;

import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.process.ProcessInfo;

/**
 * A process that is executed in a Workshop.
 */
public abstract class WorkshopProcess implements Serializable {
	private double workTimeRemaining;
	private double processTimeRemaining;
    private double averageSkillLevel = 0D;

	private ProcessInfo info;

    private Manufacture workshop;

	/**
	 * Process to run on a Workshop
	 * @param workshop
	 * @param info
	 */
    protected WorkshopProcess(Manufacture workshop, ProcessInfo info) {
        this.workshop = workshop;
        this.workTimeRemaining = info.getWorkTimeRequired();
		this.processTimeRemaining = info.getProcessTimeRequired();
		this.info = info;
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
	 */
	public void addWorkTime(double workTime, int skill) {
		workTimeRemaining -= workTime;
		if (workTimeRemaining < 0D)
			workTimeRemaining = 0D;
        
        // Add skill for work time for average skill level.
        averageSkillLevel += workTime / info.getWorkTimeRequired() * skill;
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
	 * Gets the remaining process time.
	 * 
	 * @return process time (millisols)
	 */
	public double getProcessTimeRemaining() {
		return processTimeRemaining;
	}

	/**
	 * Adds process time to the process.
	 * 
	 * @param processTime process time (millisols)
	 */
	public void addProcessTime(double processTime) {
		processTimeRemaining -= processTime;
		if (processTimeRemaining < 0D)
			processTimeRemaining = 0D;
	}

    /**
	 * Gets the workshop running htis process
	 * 
	 * @return workshop building function.
	 */
	public Manufacture getWorkshop() {
		return workshop;
	}

	/**
	 * Gets the process information.
	 * 
	 * @return process information.
	 */
	public ProcessInfo getInfo() {
		return info;
	}
    	
	/**
	 * Stops the process.
	 * @param premature Was it stopped prematurely?
	 */
	public abstract void stopProcess(boolean premature);

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		workshop = null;
	}
}
