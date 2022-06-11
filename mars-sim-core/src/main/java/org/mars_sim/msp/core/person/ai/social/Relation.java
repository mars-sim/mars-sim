/*
 * Mars Simulation Project
 * Relation.java
 * @date 2022-06-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.social;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.person.Person;

/**
 * The Relation class represents the relational connection between two people.
 */
public class Relation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** The person's opinion toward another person. */
	private Map<Integer, Double> opinionMap = new HashMap<>();

	/**
	 * Constructor.
	 * 
	 * @param person
	 */
	public Relation(Person person)  {
	}
	
	/**
	 * Gets the opinion regarding a person
	 * 
	 * @param personID
	 * @return
	 */
	public double getOpinion(int personID) {
		if (opinionMap.containsKey(personID))
			return opinionMap.get(personID);
		else
			return 0;
	}
	
	/**
	 * Sets the opinion regarding a person
	 * 
	 * @param personID
	 * @param opinion
	 */
	public void setOpinion(int personID, double opinion) {
		if (opinion < 1)
			opinion = 1;
		if (opinion > 100)
			opinion = 100;
		opinionMap.put(personID, opinion);
	}
	
	/**
	 * Changes the opinion regarding a person
	 * 
	 * @param personID
	 * @param mod
	 */
	public void changeOpinion(int personID, double mod) {
		double result = getOpinion(personID) + mod;
		if (result < 1)
			result = 1;
		if (result > 100)
			result = 100;
		opinionMap.put(personID, result);
	}
	
	/**
	 * Gets a set of people's ids
	 * 
	 * @return a set of people's ids
	 */
	public Set<Integer> getPeopleIDs() {
		return opinionMap.keySet();
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		opinionMap.clear();
		opinionMap = null;
	}
}
