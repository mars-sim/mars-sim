/**
 * Mars Simulation Project
 * Crew.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Crew {

	private String name;
	
	private List<Member> team = new ArrayList<>();

	private boolean bundled;
	
	public Crew(String name, boolean bundled) {
		this.name = name;
		this.bundled = bundled;
	}

	public void setName(String newName) {
		this.name = newName;	
	}
	
	public void addMember(Member m) {
		team.add(m);
	}

	public List<Member> getTeam() {
		return team;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * Gets the number of people configured for the simulation.
	 * 
	 * @param crewID the type of crew (Alpha or Beta)
	 * @return number of people.
	 * @throws Exception if error in XML parsing.
	 */
	public int getNumberOfConfiguredPeople() {
		return team.size();
	}
	

}
