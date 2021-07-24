/**
 * Mars Simulation Project
 * Crew.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Crew implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    //private static final Logger logger = Logger.getLogger(Crew.class.getName());

	private String name;
	private String destination;
	
	private List<Member> team = new ArrayList<>();
	
	public Crew(String name) {
		this.name = name;	
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

	public void setDestination(String value) {
		destination = value;
	}
	
	public String getDestination() {
		return destination;
	}

}
