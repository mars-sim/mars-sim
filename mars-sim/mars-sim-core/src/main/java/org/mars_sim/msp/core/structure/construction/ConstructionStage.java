/**
 * Mars Simulation Project
 * ConstructionStage.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.construction;

import java.io.Serializable;

/**
 * A construction stage of a construction site.
 * TODO externalize strings
 */
public class ConstructionStage implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Construction site events.
	public static final String ADD_CONSTRUCTION_WORK_EVENT = "adding construction work";
	public static final String ADD_SALVAGE_WORK_EVENT = "adding salvage work";

	/** Work time modifier for salvaging a construction stage. */
	private static final double SALVAGE_WORK_TIME_MODIFIER = .25D;

	// Data members
	private ConstructionStageInfo info;
	private ConstructionSite site;
	private double completedWorkTime;
	private boolean isSalvaging;

	/**
	 * Constructor.
	 * @param info the stage information.
	 */
	public ConstructionStage(ConstructionStageInfo info, ConstructionSite site) {
		this.info = info;
		this.site = site;
		completedWorkTime = 0D;
		isSalvaging = false;
	}

	/**
	 * Get the construction stage information.
	 * @return stage information.
	 */
	public ConstructionStageInfo getInfo() {
		return info;
	}

	/**
	 * Gets the completed work time on the stage.
	 * @return work time (in millisols).
	 */
	public double getCompletedWorkTime() {
		return completedWorkTime;
	}

	/**
	 * Sets the completed work time on the stage.
	 * @param completedWorkTime work time (in millisols).
	 */
	public void setCompletedWorkTime(double completedWorkTime) {
		this.completedWorkTime = completedWorkTime;
	}

	/**
	 * Gets the required work time for the stage.
	 * @return work time (in millisols).
	 */
	public double getRequiredWorkTime() {
		double requiredWorkTime = info.getWorkTime();
		if (isSalvaging) {
			requiredWorkTime *= SALVAGE_WORK_TIME_MODIFIER;
		}
		return requiredWorkTime;
	}

	/**
	 * Adds work time to the construction stage.
	 * @param workTime the work time (in millisols) to add.
	 */
	public void addWorkTime(double workTime) {
		completedWorkTime += workTime;

		if (completedWorkTime > getRequiredWorkTime()) {
			completedWorkTime = getRequiredWorkTime();
		}

		// Fire construction event
		if (isSalvaging) {
			site.fireConstructionUpdate(ADD_SALVAGE_WORK_EVENT, this);
		}
		else {
			site.fireConstructionUpdate(ADD_CONSTRUCTION_WORK_EVENT, this);
		}
	}

	/**
	 * Checks if the stage is complete.
	 * @return true if stage is complete.
	 */
	public boolean isComplete() {
		return (completedWorkTime >= getRequiredWorkTime());
	}

	/**
	 * Checks if the stage is salvaging.
	 * @return true if stage is salvaging.
	 */
	public boolean isSalvaging() {
		return isSalvaging;
	}

	/**
	 * Sets if the stage is salvaging.
	 * @param isSalvaging true if staging is salvaging.
	 */
	public void setSalvaging(boolean isSalvaging) {
		this.isSalvaging = isSalvaging;
	}

	@Override
	public String toString() {
		String result = "";
		if (isSalvaging) result = "salvaging " + info.getName();
		else result = "constructing " + info.getName();
		return result;
	}
}