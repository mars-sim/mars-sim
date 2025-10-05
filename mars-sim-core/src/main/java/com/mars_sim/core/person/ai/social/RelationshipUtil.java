/*
 * Mars Simulation Project
 * RelationshipUtil.java
 * @date 2023-05-24
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.social;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.MBTIPersonality;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The RelationshipUtil class computes the changes in social relationships between people.
 */
public class RelationshipUtil implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(RelationshipUtil.class.getName());

	/** The base % chance of a relationship change per millisol. */
	private static final double BASE_RELATIONSHIP_CHANGE_PROBABILITY = .1D;
	/** The base change amount per millisol. */
	private static final double BASE_RELATIONSHIP_CHANGE_AMOUNT = .1D;
	/** The base stress modifier per millisol for relationships. */
	private static final double BASE_STRESS_MODIFIER = .05D;
	/** The base opinion modifier per millisol for relationship change. */
	private static final double BASE_OPINION_MODIFIER = .2D;
	/** The base conversation modifier per millisol for relationship change. */
	private static final double BASE_CONVERSATION_MODIFIER = .2D;
	/** The base attractiveness modifier per millisol for relationship change. */
	private static final double BASE_ATTRACTIVENESS_MODIFIER = .1D;
	/** The base gender bonding modifier per millisol for relationship change. */
	private static final double BASE_GENDER_BONDING_MODIFIER = .02D;
	/** The base personality diff modifier per millisol for relationship change. */
	private static final double PERSONALITY_DIFF_MODIFIER = .1D;
	/**
	 * The base settler modifier per millisol as settlers are trained to get along
	 * with each other.
	 */
	private static final double SETTLER_MODIFIER = .02D;
	
	/**
	 * Adds a new relationship between two people.
	 * 
	 * @param person1          the first person (order isn't important)
	 * @param person2          the second person (order isn't important)
	 * @param relationshipType the type of relationship (see Relationship static
	 *                         members)
	 */
	private static void createRelationship(Person person1, Person person2, RelationshipType startingRelationship) {
		double opinion = switch(startingRelationship) {
			case FIRST_IMPRESSION -> getFirstImpression(person1, person2);
			case FACE_TO_FACE_COMMUNICATION -> getExistingRelationship(person1, person2);
			case REMOTE_COMMUNICATION -> getRemoteRelationship(person1, person2);
			default -> 1D;
		};
		
		if (opinion < 0)
			opinion = 10;
		if (opinion > 100)
			opinion = 100;
		
		person1.getRelation().setRandomOpinion(person2, opinion);
	}

	
	/**
	 * Changes the opinion of person1 toward person2.
	 * 
	 * @param person1
	 * @param person2
	 * @param opinion
	 */
	public static void changeOpinion(Person person1, Person person2, double mod) {
		person1.getRelation().changeOpinion(person2, mod);
	}
	
	/**
	 * Checks if a person has a relationship with another person.
	 * 
	 * @param person1 the first person (order isn't important)
	 * @param person2 the second person (order isn't important)
	 * @return true if the two people have a relationship
	 */
	private static boolean hasRelationship(Person person1, Person person2) {
		return (person1.getRelation().getOpinion(person2) != null);
	}

	/**
	 * Changes the opinion of person1 toward person2.
	 * 
	 * @param person1
	 * @param person2
	 * @param type
	 * @param mod
	 */
	public static void changeOpinion(Person person1, Person person2, RelationshipType type, double mod) {
		if (person1 == person2)
			return;
		
        // Check if existing relationship between person1 and person2.      
        if (!hasRelationship(person1, person2)) {
            // Create new relationship.
        	createRelationship(person1, person2, type);
        }
        changeOpinion(person1, person2, mod);
	}
	
	/**
	 * Gets all the people that a person knows (has met).
	 * 
	 * @param person the person
	 * @return a list of the people the person knows.
	 */
	public static Set<Person> getAllKnownPeople(Person person) {
		return person.getRelation().getAllKnownPeople();
	}
	
	/**
	 * Gets a map of my opinions over them.
	 * 
	 * @param person
	 * @return {@link Person} map
	 */
	public static Map<Person, Double> getMyOpinionsOfThem(Person person) {
		Map<Person, Double> friends = new HashMap<>();
		Collection<Person> list = getAllKnownPeople(person);
		double highestScore = 0;
		for (Person pp : list) {
			double score = getOpinionOfPerson(person, pp);
			if (highestScore <= score)
				highestScore = score;
			friends.put(pp, score);
		}

		return friends;
	}
	
	/**
	 * Gets the average opinion score over this person.
	 * 
	 * @param person
	 * @return {@link Person} map
	 */
	public static double getAverageOpinionOfMe(Person person) {
		Collection<Person> list = getAllKnownPeople(person);
		int size = list.size();
		double total = 0;
		if (!list.isEmpty()) {
			for (Person pp : list) {
				total += getOpinionOfPerson(pp, person);
			}
			
			return total/size;
		}
		
		return 50;
	}
	
	/**
	 * Gets the person's average opinion of them.
	 * 
	 * @param person
	 * @return {@link Person} map
	 */
	public static double getMyAverageOpinionOfThem(Person person) {
		Collection<Person> list = getAllKnownPeople(person);
		int size = list.size();
		double total = 0;
		if (!list.isEmpty()) {
			for (Person pp : list) {
				total += getOpinionOfPerson(person, pp);
			}
			
			return total/size;
		}
		
		return 50;
	}

	
	/**
	 * Gets the best friends, the ones having the highest relationship score.
	 * 
	 * @param person
	 * @return {@link Person} array
	 */
	public static Map<Person, Double> getBestFriends(Person person) {
		Map<Person, Double> bestFriends = getMyOpinionsOfThem(person);
		if (bestFriends.isEmpty())
			return bestFriends;
		int size = bestFriends.size();
		if (size == 1) {
			return bestFriends;
		}
		
		else if (size > 1) {
			Optional<Double> hScore = bestFriends.values().stream().max(Double::compareTo);
			if (hScore.isEmpty())
				return bestFriends;
			double highValue = hScore.get();
			bestFriends = bestFriends.entrySet().stream()
									.filter(a -> (a.getValue() >= highValue))
                       	 			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));			
		}
		return bestFriends;
	}

	/**
	 * Gets the opinion that a person has of another person. Note: If the people
	 * don't have a relationship, return default value of 50.
	 * 
	 * @param person1 the person holding the opinion.
	 * @param person2 the person who the opinion is of.
	 * @return opinion value from 0 (enemy) to 50 (indifferent) to 100 (close
	 *         friend).
	 */
	public static double getOpinionOfPerson(Person person1, Person person2) {
		if (person1.getRelation().getOpinion(person2) == null)
			return Relation.EMPTY_OPINION.getAverage();
		return person1.getRelation().getOpinion(person2).getAverage();
	}
	
	/**
	 * Gets the average opinion that a person has of a group of people. Note: If
	 * person1 doesn't have a relationship with any of the people, return default
	 * value of 50.
	 * 
	 * @param person1 the person holding the opinion.
	 * @param people  the collection of people who the opinion is of.
	 * @return opinion value from 0 (enemy) to 50 (indifferent) to 100 (bonded).
	 */
	public static double getAverageOpinionOfPeople(Person person1, Collection<Person> people) {

		if (people == null)
			throw new IllegalArgumentException("people is null");

		if (!people.isEmpty()) {
			double result = 0D;
			Iterator<Person> i = people.iterator();
			while (i.hasNext()) {
				Person person2 = i.next();
				result += getOpinionOfPerson(person1, person2);
			}

			result = result / people.size();
			return result;
		} else
			return 50D;
	}

	/**
	 * Time passing for a person's relationship.
	 * 
	 * @param person the person
	 * @param time   the time passing (millisols)
	 * @throws Exception if error.
	 */
	public static void timePassing(Person person, double time) {

		if (person.isRestingTask()) {
			// Update the person's relationships.
			updateRelationships(person, time);
	
			// Modify the person's stress based on relationships with local people.
			modifyStress(person, time);
		}
	}

	/**
	 * Change the local person's opinion.
	 * 
	 * @param localPerson
	 * @param person
	 * @param personStress
	 * @param localPersonStress
	 * @param time
	 */
	private static void changeOpinion(Person localPerson, Person person, double personStress, double localPersonStress, double time) {

		// Randomly determine change amount (negative or positive)
		double changeAmount = RandomUtil.getRandomDouble(BASE_RELATIONSHIP_CHANGE_AMOUNT) * time;
		if (RandomUtil.lessThanRandPercent(50))
			changeAmount = 0 - changeAmount;

		// Modify based on difference in other person's opinion.
		double otherOpinionModifier = (getOpinionOfPerson(localPerson, person)
				- getOpinionOfPerson(person, localPerson)) / 100D;
		otherOpinionModifier *= BASE_OPINION_MODIFIER * time;
		changeAmount += RandomUtil.getRandomDouble(otherOpinionModifier);

		// Modify based on the conversation attribute of other person.
		double conversation = localPerson.getNaturalAttributeManager()
				.getAttribute(NaturalAttributeType.CONVERSATION);
		double conversationModifier = (conversation - 50D) / 50D;
		conversationModifier *= BASE_CONVERSATION_MODIFIER * time;
		changeAmount += RandomUtil.getRandomDouble(conversationModifier);

		// Modify based on attractiveness attribute if people are of opposite genders.
		// Note: We may add sexual orientation later that will add further complexity to
		// this.
		double attractiveness = localPerson.getNaturalAttributeManager()
				.getAttribute(NaturalAttributeType.ATTRACTIVENESS);
		double attractivenessModifier = (attractiveness - 50D) / 50D;
		attractivenessModifier *= BASE_ATTRACTIVENESS_MODIFIER * time;
		boolean oppositeGenders = (person.getGender() != localPerson.getGender());
		if (oppositeGenders) {
			changeAmount += attractivenessModifier;
			RandomUtil.getRandomDouble(changeAmount);
		}
		// Modify based on same-gender bonding.
		double genderBondingModifier = BASE_GENDER_BONDING_MODIFIER * time;
		if (!oppositeGenders){
			changeAmount += genderBondingModifier;
			RandomUtil.getRandomDouble(changeAmount);
		}

		// Modify based on personality differences.
		MBTIPersonality personPersonality = person.getMind().getMBTI();
		MBTIPersonality localPersonality = localPerson.getMind().getMBTI();
		double personalityDiffModifier = (2D
				- personPersonality.getPersonalityDifference(localPersonality.getTypeString())) / 2D;
		personalityDiffModifier *= PERSONALITY_DIFF_MODIFIER * time;
		changeAmount += RandomUtil.getRandomDouble(personalityDiffModifier);

		// Modify based on settlers being trained to get along with each other.
		double settlerModifier = SETTLER_MODIFIER * time;
		changeAmount += RandomUtil.getRandomDouble(settlerModifier);

		// Modify magnitude based on the collective stress of the two people.
		double stressChangeModifier = 1 + ((personStress + localPersonStress) / 100D);
		changeAmount *= stressChangeModifier;

		// Change the person's opinion of the other person.
        changeOpinion(person, localPerson, changeAmount);
        
		logger.fine(person, "Changed the opinion of " + localPerson.getName() + " by "
					+ changeAmount);
	}
	
	/**
	 * Updates the person's relationship.
	 * 
	 * @param person the person to update
	 * @param time   the time passing (millisols)
	 * @throws Exception if error
	 */
	private static void updateRelationships(Person person, double time) {

		double personStress = person.getPhysicalCondition().getStress();

		// Get the person's local group of people.
		Collection<Person> localGroup = person.getLocalGroup();

		// Go through each person in local group.
		Iterator<Person> i = localGroup.iterator();
		while (i.hasNext()) {
			Person localPerson = i.next();
			if (!localPerson.equals(person)) {
				double localPersonStress = localPerson.getPhysicalCondition().getStress();
	
				// Check if new relationship.
				if (!hasRelationship(person, localPerson)) {
					createRelationship(person, localPerson, RelationshipType.FACE_TO_FACE_COMMUNICATION);
				}
		
				// Determine probability of relationship change per millisol.
				double changeProbability = BASE_RELATIONSHIP_CHANGE_PROBABILITY * time;
				double stressProbModifier = 1D + ((personStress + localPersonStress) / 100D);
				if (RandomUtil.lessThanRandPercent(changeProbability * stressProbModifier)) {
	
					changeOpinion(localPerson, person, personStress, localPersonStress, time);
				}
			}
		}
	}

	/**
	 * Modifies the person's stress based on relationships with local people.
	 * 
	 * @param person the person
	 * @param time   the time passing (millisols)
	 * @throws Exception if error
	 */
	private static void modifyStress(Person person, double time) {
		double stressModifier = 0D;

		Iterator<Person> i = person.getLocalGroup().iterator();
		while (i.hasNext()) {
			Person p = i.next();
			if (!p.equals(person)) {
				stressModifier -= ((getOpinionOfPerson(person, p) - 50D) / 50D);
			}
		}
		if (stressModifier != 0) {
			stressModifier = stressModifier * BASE_STRESS_MODIFIER * time;
//	        logger.info(person, 10_000, "Adding " + Math.round(stressModifier * 100.0)/100.0 + " to the stress.");
			person.getPhysicalCondition().addStress(stressModifier);
		}
	}

	/**
	 * Describes a relationship, given the opinion score.
	 * 
	 * @param opinion
	 * @return the description
	 */
	public static String describeRelationship(double opinion) {
		int score = ((int)opinion - 5)/10;

		return switch(score) {
			case 0 -> Msg.getString("TabPanelSocial.opinion.0"); //$NON-NLS-1$
			case 1 -> Msg.getString("TabPanelSocial.opinion.1"); //$NON-NLS-1$
			case 2 -> Msg.getString("TabPanelSocial.opinion.2"); //$NON-NLS-1$
			case 3 -> Msg.getString("TabPanelSocial.opinion.3"); //$NON-NLS-1$
			case 4 -> Msg.getString("TabPanelSocial.opinion.4"); //$NON-NLS-1$
			case 5 -> Msg.getString("TabPanelSocial.opinion.5"); //$NON-NLS-1$
			case 6 -> Msg.getString("TabPanelSocial.opinion.6"); //$NON-NLS-1$
			case 7 -> Msg.getString("TabPanelSocial.opinion.7"); //$NON-NLS-1$
			case 8 -> Msg.getString("TabPanelSocial.opinion.8"); //$NON-NLS-1$
			case 9 -> Msg.getString("TabPanelSocial.opinion.9"); //$NON-NLS-1$			
			default -> Msg.getString("TabPanelSocial.opinion.10"); //$NON-NLS-1$	
		};
	}
	
	/**
	 * Gets the first impression a person has of another person.
	 * 
	 * @param person the person getting the impression.
	 * @param target the person of the impression.
	 * @return the person's opinion of the target as a value from 0 to 100.
	 */
	private static double getFirstImpression(Person person, Person target) {
		double result = 10;

		// Random with bell curve around 50.
		int numberOfIterations = RandomUtil.getRandomInt(10);
		for (int x = 0; x < numberOfIterations; x++)
			result += RandomUtil.getGaussianPositive(50, 3);
		result /= numberOfIterations;

		if (result > 100) {
			result = 100;
		}
		else if (result < -100) {
			result = -100;
		}
		
		NaturalAttributeManager attributes = target.getNaturalAttributeManager();

		// Modify based on leadership attribute.
		double leaderModifier = attributes.getAttribute(NaturalAttributeType.LEADERSHIP) - 50D;
		result += RandomUtil.getRandomDouble(leaderModifier);
		
		// Modify based on conversation attribute.
		double conversationModifier = attributes.getAttribute(NaturalAttributeType.CONVERSATION) - 50D;
		result += RandomUtil.getRandomDouble(conversationModifier);

		// Modify based on attractiveness attribute if people are of opposite genders.
		double attractivenessModifier = attributes.getAttribute(NaturalAttributeType.ATTRACTIVENESS) - 50D;
		boolean oppositeGenders = (person.getGender() != target.getGender());
		if (oppositeGenders)
			result += RandomUtil.getRandomDouble(attractivenessModifier);
		
		// Modify based on total scientific achievement.
		ScienceType science0 = ScienceType.getJobScience(target.getMind().getJobType());	
		ScienceType science1 = ScienceType.getJobScience(person.getMind().getJobType());

		// If they are on the same professional field
		if (science0 == science1) {
			// Assuming being in the same field would increase affinity
			result += RandomUtil.getRandomDouble(5);
		}
		
		// Modify as settlers are trained to try to get along with each other.
		if (result < 50D)
			result += RandomUtil.getRandomDouble(50);

		if (result > 100)
			result = 100; 
			
		return result;
	}

	/**
	 * Gets the existing relationship between two people who have spent time
	 * together.
	 * 
	 * @param person the person who has a relationship with the target person.
	 * @param target the person who is the target of the relationship.
	 * @return the person's opinion of the target as a value from 0 to 100.
	 */
	private static double getExistingRelationship(Person person, Person target) {
		double result = 10D;

		// Modify based on person's conversation attribute.
		double conversationModifier0 = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.CONVERSATION) - 50D;
		// Modify based on target conversation attribute.
		double conversationModifier1 = target.getNaturalAttributeManager().getAttribute(NaturalAttributeType.CONVERSATION) - 50D;
		result += RandomUtil.getRandomDouble((conversationModifier0 + conversationModifier1)/8.0);
		
		// Modify based on attractiveness attribute if people are of opposite genders.
		double attractivenessModifier = target.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ATTRACTIVENESS) - 50D;
		boolean oppositeGenders = (person.getGender() != target.getGender());
		if (oppositeGenders)
			result += RandomUtil.getRandomDouble(attractivenessModifier);

		// Personality diff modifier
		MBTIPersonality personType = person.getMind().getMBTI();
		MBTIPersonality targetType = target.getMind().getMBTI();
		double personalityDiffModifier = (2D - personType.getPersonalityDifference(targetType.getTypeString()))
				* 50D;
		result += RandomUtil.getRandomDouble(personalityDiffModifier);

		// Modify based on total scientific achievement.
		result += target.getResearchStudy().getTotalScientificAchievement() / 10D;

		// If impressioner is a scientist, modify based on target's achievement in
		// scientific field.
		ScienceType science = ScienceType.getJobScience(target.getMind().getJobType());
		result += target.getResearchStudy().getScientificAchievement(science);

		// Modify as settlers are trained to try to get along with each other.
		if (result < 50D)
			result += RandomUtil.getRandomDouble(50 * SETTLER_MODIFIER);
		
		if (result > 100)
			result = 100; 
		
		return result;
	}

	/**
	 * Gets the relationship between two people who meet via remote
	 * communication.
	 * 
	 * @param person the person who has a relationship with the target person.
	 * @param target the person who is the target of the relationship.
	 * @return the person's opinion of the target as a value from 0 to 100.
	 */
	private static double getRemoteRelationship(Person person, Person target) {
		// Default to 50 for now.
		double result = 50D;

		// Modify based on person's conversation attribute.
		double conversationModifier0 = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.CONVERSATION) - 50D;
		// Modify based on target conversation attribute.
		double conversationModifier1 = target.getNaturalAttributeManager().getAttribute(NaturalAttributeType.CONVERSATION) - 50D;
		result += RandomUtil.getRandomDouble((conversationModifier0 + conversationModifier1)/4.0);
	
		// Modify based on total scientific achievement.
		result += target.getResearchStudy().getTotalScientificAchievement() / 10D;

		// If target is a scientist, modify based on target's achievement in
		// scientific field.
		ScienceType science = ScienceType.getJobScience(target.getMind().getJobType());
		result += target.getResearchStudy().getScientificAchievement(science);

		if (result > 100)
			result = 100; 
		
		return result;
	}
}
