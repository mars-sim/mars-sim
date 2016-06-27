package org.mars_sim.msp.core.person;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.person.Person;

public class Crew {

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
