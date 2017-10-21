/**
 * Mars Simulation Project
 * Crew.java
 * @version 3.1.0 2017-01-24
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Crew implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    //private static Logger logger = Logger.getLogger(Crew.class.getName());

	private String name;
	private String destination;
	
	//Set<Member> team = new HashSet<>();
	List<Member> team = new ArrayList<>();
	
	
	public Crew(String name) {
		this.name = name;	
	}

	public void add(Member m) {
		team.add(m);
	}

	//public void add(Set<Member> members) {
	//	team = members;
	//}
	
	//public void add(List<Member> members) {
	//	members = members;
	//}
	
	//public Set<Member> getTeam() {
	//	return team;
	//}

	public List<Member> getTeam() {
		return team;
	}

	public String getName() {
		return name;
	}

	public void setDestination(String value) {
		destination = value;
		// TODO: set destination for all members 
	}
	
	public String getDestination() {
		return destination;
	}
}
