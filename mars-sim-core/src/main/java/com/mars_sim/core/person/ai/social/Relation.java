/*
 * Mars Simulation Project
 * Relation.java
 * @date 2024-02-03
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
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The Relation class models the relationship between two units.
 */
public class Relation implements Serializable {

	/**
	 * Details of a specific relationship between two units
	 */
	public record Opinion(double d0, double d1, double d2) implements Serializable {
		public double getAverage() {
			return (d2 + d1 + d0)/3D;
		}
	};

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Note: For Person:
	// d0 : e.g. respect
	// d1 : e.g. care
	// d2 : e.g. trust

	// Note: For Settlement:
	// d0 : diplomatic
	// d1 : economic
	// d2 : socio-cultural

	public static final Opinion EMPTY_OPINION = new Opinion(50, 50, 50);

	public static final double MAX_OPINION = 100D;
	
	/** A unit's opinion of another unit. */
	private Map<Integer, Opinion> opinionMap = new HashMap<>();
	
	/** The Unit Manager instance. */
	private static UnitManager unitManager;

	/**
	 * Constructor.
	 * 
	 * @param appraiser
	 */
	public Relation(Appraiser appraiser)  {
	}
	
	/**
	 * Gets the opinion regarding a unit.
	 * 
	 * @param appraiser
	 * @return
	 */
	public Opinion getOpinion(Appraiser appraised) {
		if (opinionMap.containsKey(appraised.getIdentifier())) {
			return opinionMap.get(appraised.getIdentifier());
		}
		return null;
//		Future: Need to determine how best to handle null opinion 
		// return opinionMap.getOrDefault(p.getIdentifier(), EMPTY_OPINION);
	}
	
	/**
	 * Sets a random opinion regarding a unit.
	 * 
	 * @param appraised
	 * @param opinion
	 */
	void setRandomOpinion(Appraiser appraised, double opinion) {
		double score = opinion;

		int id = appraised.getIdentifier();
		
		Opinion found = opinionMap.get(id);
		
		if (found == null) {
			double d0 = RandomUtil.getRandomDouble(score/1.5, score * 1.5);
			double d1 = RandomUtil.getRandomDouble(d0/1.5, d0 * 1.5);			
			double d2 = RandomUtil.getRandomDouble(d1/1.5, d1 * 1.5);

			// Gauge the difference between d0 and d2
			double d = d0 - d2;
			if (d >= 20 || d >= -20) {
				d0 = d0 - d/4;
				d2 = d2 + d/4;
			}

			// Gauge the difference between d1 and d2
			d = d1 - d2;
			if (d >= 15 || d >= -15) {
				d1 = d1 - d/4;
				d2 = d2 + d/4;
			}
			
			// Gauge the difference between d1 and d0
			d = d1 - d0;
			if (d >= 10 || d >= -10) {
				d1 = d1 - d/4;
				d0 = d0 + d/4;
			}
			
			d0 = MathUtils.between(d0, 0, MAX_OPINION);
			d1 = MathUtils.between(d1, 0, MAX_OPINION);
			d2 = MathUtils.between(d2, 0, MAX_OPINION);
			
			found = new Opinion(d0, d1, d2);
			opinionMap.put(id, found);
		}
	}
	
	/**
	 * Changes the opinion regarding a unit.
	 * 
	 * @param u
	 * @param mod
	 */
	void changeOpinion(Appraiser appraised, double mod) {
		int id = appraised.getIdentifier();
		
		Opinion found = opinionMap.get(id);
		
		if (found == null) {
			// Randomly set the opinion map
			setRandomOpinion(appraised, 0);
			
			found = opinionMap.get(id);
		}

		double d1 = found.d1;
		double d2 = found.d2;
		double d0 = found.d0;
		int rand = RandomUtil.getRandomInt(6);
		if (rand == 0) {
			// Less likely to change the d2 than d1 and d0
			d2 += mod;
		}
		else if (rand == 1 || rand == 2) {
			d1 += mod;
		}
		else { // 3, 4, 5, 6
			// Most likely to change the d0 than d1 and d2
			d0 += mod;
		}
		
		found = new Opinion(d0, d1, d2);
		opinionMap.put(id, found);
	}
	
	/**
	 * Gets all people known.
	 * 
	 * @param person the person
	 * @return a list of people
	 */
	Set<Person> getAllKnownPeople(Person person) {
		return opinionMap.keySet().stream()
				.map(id -> unitManager.getPersonByID(id))
				.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * Gets all settlements known.
	 * 
	 * @param settlement the settlement
	 * @return a list of settlement
	 */
	Set<Settlement> getAllKnownSettlement(Settlement settlement) {
		return opinionMap.keySet().stream()
				.map(id -> unitManager.getSettlementByID(id))
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
