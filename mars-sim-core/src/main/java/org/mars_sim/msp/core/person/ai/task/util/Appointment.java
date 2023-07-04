/*
 * Mars Simulation Project
 * Appointment.java
 * @date 2023-07-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.task.util;

import java.io.Serializable;
import java.util.Set;

import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.building.Building;

public class Appointment implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Set<Person> group = new UnitSet<>();
	
	private int sol = 0;
	
	private int millisolInt = 0;
	
	private Person person = null;
	
	private Building building = null;
	
	private Task task = null;
	
	public Appointment(Person person, int sol, int millisolInt, Building building, Task task, Set<Person> group) {
		this.person = person;
		this.sol = sol;	
		this.millisolInt = millisolInt;
		this.building = building;
		this.task = task;
		this.group = group;
		
	}
	
}
