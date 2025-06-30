/*
 * Mars Simulation Project
 * WorkshopProcess.java
 * @date 2025-04-17
 * @author Barry Evans
 */
package com.mars_sim.core.manufacture;

import java.io.Serializable;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.structure.Settlement;

/**
 * A process that is executed in a Workshop.
 */
public abstract class WorkshopProcess implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private double workTimeRemaining;
	private double processTimeRemaining;
    private double averageSkillLevel = 0D;

	private WorkshopProcessInfo info;
	private boolean active;
    private Manufacture workshop;

	/**
	 * Process to run on a Workshop
	 * @param workshop
	 * @param info
	 */
    protected WorkshopProcess(String name, Manufacture workshop, WorkshopProcessInfo info) {
        this.workshop = workshop;
        this.workTimeRemaining = info.getWorkTimeRequired();
		this.processTimeRemaining = info.getProcessTimeRequired();
		this.info = info;
		this.name = name;
		this.active = true;
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
	 * Is the process active?
	 * @return
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Adds work time to the process.
	 * 
	 * @param workTime work time (millisols)
	 * @rteturn Is the process still active
	 */
	public boolean addWorkTime(double workTime, int skill) {
		workTimeRemaining -= workTime;
		if (workTimeRemaining < 0D)
			workTimeRemaining = 0D;
        
        // Add skill for work time for average skill level.
        averageSkillLevel += workTime / info.getWorkTimeRequired() * skill;

		checkStillActive();
		return active;
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
	 * Does this process need a tool
	 * @return
	 */
	public Tooling getTooling() {
		return info.getTooling();
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
	 * @return True iof the process is still active
	 */
	public boolean addProcessTime(double processTime) {
		processTimeRemaining -= processTime;
		if (processTimeRemaining < 0D)
			processTimeRemaining = 0D;

		checkStillActive();
		return active;
	}

	private void checkStillActive() {
		if (!active)
			return;

		active = (processTimeRemaining > 0D) || (workTimeRemaining > 0D);
		if (!active) {
			stopProcess(false);
		}
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
	 * Get the descriptive name of the process
	 */
	public String getName() {
		return name;
	}

	/**
	 * Where is this process running?
	 * @return
	 */
	protected Building getBuilding() {
		return workshop.getBuilding();
	}

	/**
	 * Deposits the outputs.
	 * 
	 * @param process
	 */
	protected void depositOutputs() {
		var host = getWorkshop().getBuilding();
		Settlement settlement = host.getAssociatedSettlement();

		info.depositOutputs(settlement, true);
	}

	/**
	 * Returns the used inputs.
	 * 
	 * @param process
	 */
	protected void returnInputs() {
		var host = getWorkshop().getBuilding();
		Settlement settlement = host.getAssociatedSettlement();

		info.returnInputs(settlement);
	}


	/**
	 * Start the process running
	 */
	public abstract boolean startProcess();

	/**
	 * Stops the process.
	 * @param premature Was it stopped prematurely?
	 */
	public void stopProcess(boolean premature) {
		workshop.removeProcess(this);
		active = false;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		workshop = null;
	}
}
