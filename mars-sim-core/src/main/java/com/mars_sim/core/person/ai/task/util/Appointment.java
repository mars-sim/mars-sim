/*
 * Mars Simulation Project
 * Appointment.java
 * @date 2023-07-03
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.task.util;

import java.io.Serializable;
import java.util.Set;

import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.building.Building;

public class Appointment implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Set<Person> group = new UnitSet<>();
	
	private int sol = 0;
	
	private int millisolInt = 0;
	
	private int duration = 0;
	
	private Person person = null;
	
	private Building building = null;
	
	private String taskName = null;
	
	public Appointment(Person person, int sol, int millisolInt, int duration, Building building, String taskName, Set<Person> group) {
		this.person = person;
		this.sol = sol;	
		this.millisolInt = millisolInt;
		this.duration = duration;
		this.building = building;
		this.taskName = taskName;
		this.group = group;
	}
	
	public int getSol() {
		return sol;
	}
	
	public int getMillisolInt() {
		return millisolInt;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public String getTaskName() {
		return taskName;
	}
}
