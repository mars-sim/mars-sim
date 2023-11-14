/*
 * Mars Simulation Project
 * Relation.java
 * @date 2023-05-24
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.social;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.UnitManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The Relation class models the relational connection of a person toward others.
 */
public class Relation implements Serializable {

	/**
	 * Details of a specific relationship to a Person
	 */
	public record Opinion(double respect, double care, double trust) implements Serializable {
		public double getAverage() {
			return (trust + care + respect)/3D;
		}
	};

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final Opinion EMPTY_OPINION = new Opinion(50, 50, 50);
	
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
	 * Gets the opinion regarding a person.
	 * 
	 * @param p Person to get an opinion on
	 * @return
	 */
	public Opinion getOpinion(Person p) {
		if (opinionMap.containsKey(p.getIdentifier())) {
			return opinionMap.get(p.getIdentifier());
		}
		return null;
//		return opinionMap.getOrDefault(p.getIdentifier(), EMPTY_OPINION);
	}
	
	/**
	 * Sets a random opinion regarding a person.
	 * 
	 * @param p
	 * @param opinion
	 */
	void setRandomOpinion(Person p, double opinion) {
		double score = opinion;
		if (score < 0)
			score = 10;
		if (score > 100)
			score = 100;
		
		double care = 0;
		double trust = 0;
		double respect = 0;
		
		int personID = p.getIdentifier();
		Opinion found = opinionMap.get(personID);
		
		if (found == null) {
			respect = RandomUtil.getRandomDouble(score/1.5, score * 1.5);
			care = RandomUtil.getRandomDouble(respect/1.5, respect * 1.5);			
			trust = RandomUtil.getRandomDouble(care/1.5, care * 1.5);

			// Gauge the difference between respect and trust
			double d = respect - trust;
			if (d >= 20 || d >= -20) {
				respect = respect - d/4;
				trust = trust + d/4;
			}

			// Gauge the difference between care and trust
			d = care - trust;
			if (d >= 15 || d >= -15) {
				care = care - d/4;
				trust = trust + d/4;
			}
			
			// Gauge the difference between care and respect
			d = care - respect;
			if (d >= 10 || d >= -10) {
				care = care - d/4;
				respect = respect + d/4;
			}
			
			if (respect < 0)
				respect = 0;
			if (respect > 100)
				respect = 100;
			
			if (care < 0)
				care = 0;
			if (care > 100)
				care = 100;
			
			if (trust < 0)
				trust = 0;
			if (trust > 100)
				trust = 100;
		}

		found = new Opinion(respect, care, trust);
		opinionMap.put(personID, found);
	}
	
	
	/**
	 * Changes the opinion regarding a person.
	 * 
	 * @param p
	 * @param mod
	 */
	void changeOpinion(Person p, double mod) {
		
		int personID = p.getIdentifier();
		Opinion found = opinionMap.get(personID);
		
		double care = found.care;
		double trust = found.trust;
		double respect = found.respect;
		int rand = RandomUtil.getRandomInt(6);
		if (rand == 0) {
			// Less likely to change the trust than care and respect
			trust += mod;
		}
		else if (rand == 1 || rand == 2) {
			care += mod;
		}
		else { // 3, 4, 5, 6
			// Most likely to change the respect than care and trust
			respect += mod;
		}
		
		if (respect < 0)
			respect = 0;
		if (respect > 100)
			respect = 100;
		
		if (care < 0)
			care = 0;
		if (care > 100)
			care = 100;
		
		if (trust < 0)
			trust = 0;
		if (trust > 100)
			trust = 100;
		
		found = new Opinion(respect, care, trust);
		opinionMap.put(personID, found);
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
