/*
 * Mars Simulation Project
 * OneActivity.java
 * @date 2023-11-19
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.io.Serializable;

/*
 * This class represents a record of a given activity (task or mission)
 * undertaken by a person or a robot
 */
public final class OneActivity implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private String taskName;
	private String missionName;
	private String description;
	private String phase;

	public OneActivity(String taskName, String description, String phase, String missionName) {
		this.taskName = taskName;
		this.description = description;
		this.phase = phase;
	}

	/**
	 * Gets the task name.
	 * 
	 * @return task name
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * Gets the description what the actor is doing.
	 * 
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the task phase.
	 * 
	 * @return task phase
	 */
	public String getPhase() {
		return phase;
	}

	public String getMission() {
		return missionName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
		result = prime * result + ((phase == null) ? 0 : phase.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OneActivity other = (OneActivity) obj;
		if (taskName == null) {
			if (other.taskName != null)
				return false;
		} else if (!taskName.equals(other.taskName))
			return false;
		if (missionName == null) {
			if (other.missionName != null)
				return false;
		} else if (!missionName.equals(other.missionName))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (phase == null) {
			if (other.phase != null)
				return false;
		} else if (!phase.equals(other.phase))
			return false;
		return true;
	}
}