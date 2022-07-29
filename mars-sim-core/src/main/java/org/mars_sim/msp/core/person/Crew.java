/*
 * Mars Simulation Project
 * Crew.java
 * @date 2022-07-29
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.configuration.UserConfigurable;


/**
 * This class represents a pre-defiend Crew of people who can be assigned to Settlments.
 */
public class Crew implements UserConfigurable {

	private String name;
	private String description = "";
	
	private List<Member> team = new ArrayList<>();

	private boolean bundled;
	
	public Crew(String name, String description, boolean bundled) {
		this.name = name;
		this.description = description;
		this.bundled = bundled;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	public void addMember(Member m) {
		team.add(m);
	}

	public List<Member> getTeam() {
		return team;
	}

	@Override
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
	
	@Override
	public String toString() {
		return name;
	}

	/**
//	 * Is this crew bundled with the code base 
 * 
	 * @return
	 */
	@Override
	public boolean isBundled() {
		return bundled;
	}

}
