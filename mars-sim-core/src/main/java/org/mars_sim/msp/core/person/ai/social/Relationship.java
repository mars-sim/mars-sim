/**
 * Mars Simulation Project
 * Relationship.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.social;

import java.io.Serializable;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.MBTIPersonality;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Relationship class represents a social relationship between two people.
 */
public class Relationship implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// TODO Types of starting relationships should be an enum.
	/** First impression if for meeting a new person. */
	public static final String FIRST_IMPRESSION = "First Impression";
	/** Existing relationship is for meeting a person who is already known. */
	public static final String EXISTING_RELATIONSHIP = "Existing Relationship";
	/** Communication meeting is for meeting a new person remotely (email, etc). */
	public static final String COMMUNICATION_MEETING = "Communication Meeting";

	/**
	 * Relationship modifier for settlers since they are trained to get along with
	 * each other.
	 */
	private static final double SETTLER_MODIFIER = 20D;

	// Data members
	private int person1;
	private double person1Opinion;
	private int person2;
	private double person2Opinion;

	/**
	 * Constructor.
	 * 
	 * @param person1              the first person in the relationship (order
	 *                             really isn't important)
	 * @param person2              the second person in the relationship
	 * @param startingRelationship the type of starting relationship (see static
	 *                             strings above)
	 * @throws IllegalArgumentException if invalid parameters
	 */
	Relationship(Person person1, Person person2, String startingRelationship) throws IllegalArgumentException {

		// Initialize data members
		this.person1 = person1.getIdentifier();
		this.person2 = person2.getIdentifier();

		if (FIRST_IMPRESSION.equals(startingRelationship)) {
			setPerson1Opinion(getFirstImpression(person1, person2));
			setPerson2Opinion(getFirstImpression(person2, person1));
		} else if (EXISTING_RELATIONSHIP.equals(startingRelationship)) {
			setPerson1Opinion(getExistingRelationship(person1, person2));
			setPerson2Opinion(getExistingRelationship(person2, person1));
		} else if (COMMUNICATION_MEETING.equals(startingRelationship)) {
			setPerson1Opinion(getCommunicationMeeting(person1, person2));
			setPerson2Opinion(getCommunicationMeeting(person2, person1));
		} else
			throw new IllegalArgumentException("Invalid starting relationship type: " + startingRelationship);
	}

	/**
	 * Gets the two people in relationship.
	 * 
	 * @return array of two people
	 */
	public int[] getPeople() {
		int[] result = { person1, person2 };
		return result;
	}

	/**
	 * Checks if a given person is in this relationship.
	 * 
	 * @param person the person to check
	 * @return true if person is in this relationship
	 */
	public boolean hasPerson(Person person) {
		boolean result = false;
		if (person1 == person.getIdentifier())
			result = true;
		else if (person2 == person.getIdentifier())
			result = true;
		return result;
	}

	/**
	 * Sets person 1's opinion of person 2
	 * 
	 * @param opinion person 1's opinion as a value from 0 to 100.
	 */
	private void setPerson1Opinion(double opinion) {
		person1Opinion = opinion;
		if (person1Opinion < 0D)
			person1Opinion = 0D;
		if (person1Opinion > 100D)
			person1Opinion = 100D;
	}

	/**
	 * Sets person 2's opinion of person 1
	 * 
	 * @param opinion person 2's opinion as a value from 0 to 100.
	 */
	private void setPerson2Opinion(double opinion) {
		person2Opinion = opinion;
		if (person2Opinion < 0D)
			person2Opinion = 0D;
		if (person2Opinion > 100D)
			person2Opinion = 100D;
	}

	/**
	 * Gets one of the two people's opinion of the other.
	 * 
	 * @param person the person to get an opinion from.
	 * @return the person's opinion of the other person as a value from 0 to 100.
	 * @throws IllegalArgumentException if person is not one of the two people in
	 *                                  the relationship.
	 */
	public double getPersonOpinion(Person person) throws IllegalArgumentException {
		if (person.getIdentifier() == person1)
			return person1Opinion;
		else if (person.getIdentifier() == person2)
			return person2Opinion;
		else
			throw new IllegalArgumentException("Invalid person: " + person);
	}

	/**
	 * Sets one of the two people's opinion of the other.
	 * 
	 * @param person  the person to set the opinion for.
	 * @param opinion the person's opinion of the other person as a value from 0 to
	 *                100.
	 * @throws IllegalArgumentException if person is not one of the two people in
	 *                                  the relationship.
	 */
	public void setPersonOpinion(Person person, double opinion) throws IllegalArgumentException {
		if (person.getIdentifier() == person1)
			setPerson1Opinion(opinion);
		else if (person.getIdentifier() == person2)
			setPerson2Opinion(opinion);
		else
			throw new IllegalArgumentException("Invalid person: " + person);
	}

	/**
	 * Gets the first impression a person has of another person.
	 * 
	 * @param impressioner the person getting the impression.
	 * @param impressionee the person who's the object of the impression.
	 * @return the opinion of the impressioner as a value from 0 to 100.
	 */
	private double getFirstImpression(Person impressioner, Person impressionee) {
		double result = 0D;

		// Random with bell curve around 50.
		int numberOfIterations = 3;
		for (int x = 0; x < numberOfIterations; x++)
			result += RandomUtil.getRandomDouble(100D);
		result /= numberOfIterations;

		NaturalAttributeManager attributes = impressionee.getNaturalAttributeManager();

		// Modify based on conversation attribute.
		double conversationModifier = (double) attributes.getAttribute(NaturalAttributeType.CONVERSATION) - 50D;
		result += RandomUtil.getRandomDouble(conversationModifier);

		// Modify based on attractiveness attribute if people are of opposite genders.
		// Note: We may add sexual orientation later that will add further complexity to
		// this.
		double attractivenessModifier = (double) attributes.getAttribute(NaturalAttributeType.ATTRACTIVENESS) - 50D;
		boolean oppositeGenders = (!impressioner.getGender().equals(impressionee.getGender()));
		if (oppositeGenders)
			result += RandomUtil.getRandomDouble(attractivenessModifier);

		// Modify based on total scientific achievement.
		result += impressionee.getTotalScientificAchievement() / 10D;

		// If impressioner is a scientist, modify based on impressionee's achievement in
		// scientific field.
		ScienceType science = ScienceType.getJobScience(impressioner.getMind().getJob());
		result += impressionee.getScientificAchievement(science);

		// Modify as settlers are trained to try to get along with each other.
		if (result < 50D)
			result += RandomUtil.getRandomDouble(SETTLER_MODIFIER);

		return result;
	}

	/**
	 * Gets an existing relationship between two people who have spent time
	 * together.
	 * 
	 * @param person the person who has a relationship with the target person.
	 * @param target the person who is the target of the relationship.
	 * @return the person's opinion of the target as a value from 0 to 100.
	 */
	private double getExistingRelationship(Person person, Person target) {
		double result = 0D;

		// Random with bell curve around 50.
		int numberOfIterations = 3;
		for (int x = 0; x < numberOfIterations; x++)
			result += RandomUtil.getRandomDouble(100D);
		result /= numberOfIterations;

		NaturalAttributeManager attributes = target.getNaturalAttributeManager();

		// Modify based on conversation attribute.
		double conversationModifier = (double) attributes.getAttribute(NaturalAttributeType.CONVERSATION) - 50D;
		result += RandomUtil.getRandomDouble(conversationModifier);

		// Modify based on attractiveness attribute if people are of opposite genders.
		// Note: We may add sexual orientation later that will add further complexity to
		// this.
		double attractivenessModifier = (double) attributes.getAttribute(NaturalAttributeType.ATTRACTIVENESS) - 50D;
		boolean oppositeGenders = (!person.getGender().equals(target.getGender()));
		if (oppositeGenders)
			result += RandomUtil.getRandomDouble(attractivenessModifier);

		// Personality diff modifier
		MBTIPersonality personType = person.getMind().getMBTI();
		MBTIPersonality targetType = target.getMind().getMBTI();
		double personalityDiffModifier = (2D - (double) personType.getPersonalityDifference(targetType.getTypeString()))
				* 50D;
		result += RandomUtil.getRandomDouble(personalityDiffModifier);

		// Modify based on total scientific achievement.
		result += target.getTotalScientificAchievement() / 10D;

		// If impressioner is a scientist, modify based on target's achievement in
		// scientific field.
		ScienceType science = ScienceType.getJobScience(target.getMind().getJob());
		result += target.getScientificAchievement(science);

		// Modify as settlers are trained to try to get along with each other.
		if (result < 50D)
			result += RandomUtil.getRandomDouble(SETTLER_MODIFIER);

		return result;
	}

	/**
	 * Gets an new relationship between two people who meet via remote
	 * communication.
	 * 
	 * @param person the person who has a relationship with the target person.
	 * @param target the person who is the target of the relationship.
	 * @return the person's opinion of the target as a value from 0 to 100.
	 */
	private double getCommunicationMeeting(Person person, Person target) {
		double result = 0D;

		// Default to 50 for now.
		result = 50D;

		// Modify based on total scientific achievement.
		result += target.getTotalScientificAchievement() / 10D;

		// If impressioner is a scientist, modify based on target's achievement in
		// scientific field.
		ScienceType science = ScienceType.getJobScience(target.getMind().getJob());
		result += target.getScientificAchievement(science);

		return result;
	}
}