/*
 * Mars Simulation Project
 * Relation.java
 * @date 2023-05-24
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.social;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;

/**
 * The Relation class models the relational connection of a person toward others.
 */
public class Relation implements Serializable {

	/**
	 * Details of a specific relationship to a Person
	 */
	public record Opinion(double trust, double care, double respect) implements Serializable {
		public double getAverage() {
			return (trust + care + respect)/3D;
		}
	};

	/** default serial id. */
	private static final long serialVersionUID = 1L;


	private static final Opinion EMPTY_OPINION = new Opinion(0, 0, 0);

	
	/** The person's opinion of another person. */
	private Map<Integer, Opinion> opinionMap = new HashMap<>();
	
	/** The Unit Manager instance. */
	private static UnitManager unitManager;

	/**
	 * Constructor.
	 * 
	 * @param person
	 */
	public Relation(Person person)  {
	}
	
	/**
	 * Gets the Opinion regarding a person.
	 * 
	 * @param p Person to get an opnion on
	 * @return
	 */
	public Opinion getOpinion(Person p) {
		return opinionMap.getOrDefault(p.getIdentifier(), EMPTY_OPINION);
	}
	
	/**
	 * Sets the opinion regarding a person.
	 * 
	 * @param p
	 * @param opinion
	 */
	void setOpinion(Person p, double opinion) {
		if (opinion < 1)
			opinion = 1;
		if (opinion > 100)
			opinion = 100;
		double care, trust, respect;
		int personID = p.getIdentifier();
		Opinion found = opinionMap.get(personID);
		if (found == null) {
			trust = 50.0 + RandomUtil.getRandomDouble(-10, 10);
			care = 50.0 + RandomUtil.getRandomDouble(-10, 10);
			respect = 50.0 + RandomUtil.getRandomDouble(-10, 10);
		}
		else {
			care = found.care;
			trust = found.trust;
			respect = found.respect;
			int rand = RandomUtil.getRandomInt(2);
			if (rand == 0) {
				trust = opinion;
			}
			else if (rand == 1) {
				care = opinion;
			}
			else {
				respect = opinion;
			}
		}

		opinionMap.put(personID, new Opinion(trust, care, respect));
	}
	
	
	/**
	 * Changes the opinion regarding a person.
	 * 
	 * @param p
	 * @param mod
	 */
	void changeOpinion(Person p, double mod) {
		double result = getOpinion(p).getAverage() + mod;
		if (result < 1)
			result = 1;
		if (result > 100)
			result = 100;
		setOpinion(p, result);
	}
	
	/**
	 * Gets all the people that a person knows (has met).
	 * 
	 * @param person the person
	 * @return a list of the people the person knows.
	 */
	Set<Person> getAllKnownPeople(Person person) {
		return opinionMap.keySet().stream()
				.map(id -> unitManager.getPersonByID(id))
				.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * Initializes instances.
	 * 
	 * @param um the unitManager instance
	 */
	public static void initializeInstances(UnitManager um) {
		unitManager = um;		
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		opinionMap.clear();
		opinionMap = null;
	}
}
