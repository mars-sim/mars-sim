/**
 * Mars Simulation Project
 * Relationship.java
 * @version 2.77 2004-08-31
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.social;

import java.io.Serializable;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.*;

/**
 * The Relationship class represents a social relationship between
 * two people.
 */
public class Relationship implements Serializable {

	// Types of starting relationships.
	public static final String FIRST_IMPRESSION = "First Impression";
	public static final String EXISTING_RELATIONSHIP = "Existing Relationship";

	// Data members
	private Person person1;
	private double person1Opinion;
	private Person person2;
	private double person2Opinion;

	/**
	 * Constructor
	 * @param person1 the first person in the relationship (order really isn't important)
	 * @param person2 the second person in the relationship
	 * @param startingRelationship the type of starting relationship (see static strings above)
	 * @throws IllegalArgumentException if invalid parameters
	 */
	public Relationship(Person person1, Person person2, String startingRelationship) throws IllegalArgumentException {
		
		// Initialize data members
		this.person1 = person1;
		this.person2 = person2;
		
		if (FIRST_IMPRESSION.equals(startingRelationship)) {
			setPerson1Opinion(getFirstImpression(person1, person2));
			setPerson2Opinion(getFirstImpression(person2, person1));
		}
		else if (EXISTING_RELATIONSHIP.equals(startingRelationship)) {
			setPerson1Opinion(getExistingRelationship(person1, person2));
			setPerson2Opinion(getExistingRelationship(person2, person1));
		}
		else throw new IllegalArgumentException("Invalid starting relationship type: " + startingRelationship);
	}
	
	/**
	 * Gets person 1
	 * @return person 1
	 */
	public Person getPerson1() {
		return person1;
	}
	
	/**
	 * Gets person 1's opinion of person 2
	 * @return person 1's opinion as a value from 0 to 100.
	 */
	public double getPerson1Opinion() {
		return person1Opinion;
	}
	
	/**
	 * Sets person 1's opinion of person 2
	 * @param opinion person 1's opinion as a value from 0 to 100.
	 */
	private void setPerson1Opinion(double opinion) {
		person1Opinion = opinion;
		if (person1Opinion < 0D) person1Opinion = 0D;
		if (person1Opinion > 100D) person1Opinion = 100D;
	}
	
	/**
	 * Gets person 2
	 * @return person 2
	 */
	public Person getPerson2() {
		return person2;
	}
	
	/**
	 * Gets person 2's opinion of person 1
	 * @return person 2's opinion as a value from 0 to 100.
	 */
	public double getPerson2Opinion() {
		return person2Opinion;
	}
	
	/**
	 * Sets person 2's opinion of person 1
	 * @param opinion person 2's opinion as a value from 0 to 100.
	 */
	private void setPerson2Opinion(double opinion) {
		person2Opinion = opinion;
		if (person2Opinion < 0D) person2Opinion = 0D;
		if (person2Opinion > 100D) person2Opinion = 100D;
	}
	
	/**
	 * Get's one of the two people's opinion of the other.
	 * @param person the person to get an opinion from.
	 * @return the person's opinion of the other person as a value from 0 to 100.
	 * @throws IllegalArgumentException if person is not one the two people in the relationship.
	 */
	public double getPersonOpinion(Person person) throws IllegalArgumentException {
		if (person == person1) return getPerson1Opinion();
		if (person == person2) return getPerson2Opinion();
		else throw new IllegalArgumentException("Invalid person: " + person);
	}
	
	/**
	 * Gets the first impression a person has of another person.
	 * @param impressioner the person getting the impression.
	 * @param impressionee the person who's the object of the impression.
	 * @return the opinion of the impressioner as a value from 0 to 100.
	 */
	private double getFirstImpression(Person impressioner, Person impressionee) {
		double result = 0D;
		
		// Random with bell curve around 50.
		int numberOfIterations = 3;
		for (int x = 0; x < numberOfIterations; x++) result+= RandomUtil.getRandomDouble(100D);
		result /= numberOfIterations;
		
		NaturalAttributeManager attributes = impressionee.getNaturalAttributeManager();
		
		// Modify based on conversation attribute.
		double conversationModifier = (double) attributes.getAttribute(NaturalAttributeManager.CONVERSATION) - 50D;
		result+= RandomUtil.getRandomDouble(conversationModifier);
		
		// Modify based on attractiveness attribute if people are of opposite genders.
		// Note: We may add sexual orientation later that will add further complexity to this.
		double attractivenessModifier = (double) attributes.getAttribute(NaturalAttributeManager.ATTRACTIVENESS) - 50D;
		boolean oppositeGenders = (!impressioner.getGender().equals(impressionee.getGender()));
		if (oppositeGenders) result+= RandomUtil.getRandomDouble(attractivenessModifier);
		
		return result;
	}
	
	/**
	 * Gets an existing relationship between two people who have spent time together.
	 * @param person the person who has a relationship with the target person.
	 * @param target the person who is the target of the relationship.
	 * @return the person's opinion of the target as a value from 0 to 100.
	 */
	private double getExistingRelationship(Person person, Person target) {
		double result = 0D;
		
		// Random with bell curve around 50.
		int numberOfIterations = 3;
		for (int x = 0; x < numberOfIterations; x++) result+= RandomUtil.getRandomDouble(100D);
		result /= numberOfIterations;
		
		// TODO: Add more modifiers here.
		
		return result;
	}
}