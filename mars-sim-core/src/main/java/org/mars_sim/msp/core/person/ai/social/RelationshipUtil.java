/*
 * Mars Simulation Project
 * RelationshipUtil.java
 * @date 2023-05-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.social;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.MBTIPersonality;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;

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
	private static final double BASE_STRESS_MODIFIER = .1D;
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

	/** The Unit Manager instance. */
	private static UnitManager unitManager;
	
	/**
	 * Adds a new relationship between two people.
	 * 
	 * @param person1          the first person (order isn't important)
	 * @param person2          the second person (order isn't important)
	 * @param relationshipType the type of relationship (see Relationship static
	 *                         members)
	 */
	public static void createRelationship(Person person1, Person person2, RelationshipType startingRelationship) {
		if (RelationshipType.FIRST_IMPRESSION == startingRelationship) {
			setOpinion(person1, person2, getFirstImpression(person1, person2));
			setOpinion(person2, person1, getFirstImpression(person2, person1));
		} else if (RelationshipType.FACE_TO_FACE_COMMUNICATION == startingRelationship) {
			setOpinion(person1, person2, getExistingRelationship(person1, person2));
			setOpinion(person2, person1, getExistingRelationship(person2, person1));
		} else if (RelationshipType.REMOTE_COMMUNICATION == startingRelationship) {
			setOpinion(person1, person2, getRemoteRelationship(person1, person2));
			setOpinion(person2, person1, getRemoteRelationship(person2, person1));
		}
	}

	/**
	 * Sets the opinion of person1 toward person2.
	 * 
	 * @param person1
	 * @param person2
	 * @param opinion
	 */
	public static void setOpinion(Person person1, Person person2, double opinion) {
		person1.getRelation().setOpinion(person2.getIdentifier(), opinion);
	}
	
	/**
	 * Changes the opinion of person1 toward person2.
	 * 
	 * @param person1
	 * @param person2
	 * @param opinion
	 */
	public static void changeOpinion(Person person1, Person person2, double mod) {
		person1.getRelation().changeOpinion(person2.getIdentifier(), mod);
	}
	
	/**
	 * Gets the opinion of person1 toward person2.
	 * 
	 * @param person1
	 * @param person2
	 * return opinion
	 */
	public static double getOpinion(Person person1, Person person2) {
		return person1.getRelation().getOpinion(person2.getIdentifier());
	}
	
	/**
	 * Gets the opinion of person1 toward person2.
	 * 
	 * @param person1ID
	 * @param person2ID
	 * return opinion
	 */
	public static double getOpinion(int person1ID, int person2ID) {
		return unitManager.getPersonByID(person1ID).getRelation().getOpinion(person2ID);
	}
	
	/**
	 * Gets the opinion of person1 toward person2.
	 * 
	 * @param person1
	 * @param person2ID
	 * return opinion
	 */
	public static double getOpinion(Person person1, int person2ID) {
		return person1.getRelation().getOpinion(person2ID);
	}
	
	/**
	 * Gets the opinion of person1 toward person2.
	 * 
	 * @param person1
	 * @param person2
	 * return opinion
	 */
	public static double[] getOpinions(Person person1, Person person2) {
		return person1.getRelation().getOpinions(person2.getIdentifier());
	}
	
	/**
	 * Gets the opinion of person1 toward person2.
	 * 
	 * @param person1ID
	 * @param person2ID
	 * return opinion
	 */
	public static double[] getOpinions(int person1ID, int person2ID) {
		return unitManager.getPersonByID(person1ID).getRelation().getOpinions(person2ID);
	}
	
	/**
	 * Gets the opinion of person1 toward person2.
	 * 
	 * @param person1
	 * @param person2ID
	 * return opinion
	 */
	public static double[] getOpinions(Person person1, int person2ID) {
		return person1.getRelation().getOpinions(person2ID);
	}
	
	/**
	 * Checks if a person has a relationship with another person.
	 * 
	 * @param person1 the first person (order isn't important)
	 * @param person2 the second person (order isn't important)
	 * @return true if the two people have a relationship
	 */
	public static boolean hasRelationship(Person person1, Person person2) {
		return (person1.getRelation().getOpinion(person2.getIdentifier()) != -1
				&& person2.getRelation().getOpinion(person1.getIdentifier()) != -1);
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
		return person.getRelation().getAllKnownPeople(person);
	}
	
	/**
	 * Gets a map of my opinions over them.
	 * 
	 * @param person
	 * @return {@link Person} map
	 */
	public static Map<Person, Double> getMyOpinionsOfThem(Person person) {
		Map<Person, Double> friends = new ConcurrentHashMap<>();
		Collection<Person> list = getAllKnownPeople(person);
		double highestScore = 0;
		if (!list.isEmpty()) {
			for (Person pp : list) {
				double score = getOpinionOfPerson(person, pp);
				if (highestScore <= score)
					highestScore = score;
				friends.put(pp, score);
			}
		}

		return sortByValue(friends);
	}
	
	/**
	 * Gets a map of their opinions over a person.
	 * 
	 * @param person
	 * @return {@link Person} map
	 */
	public static Map<Person, Double> getTheirOpinionsOfMe(Person person) {
		Map<Person, Double> friends = new ConcurrentHashMap<>();
		Collection<Person> list = getAllKnownPeople(person);
		double highestScore = 0;
		if (!list.isEmpty()) {
			for (Person pp : list) {
				double score = getOpinionOfPerson(pp, person);
				if (highestScore <= score)
					highestScore = score;
				friends.put(pp, score);
			}
		}

		return sortByValue(friends);
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
	 * Sorts the map according to the value of each entry.
	 * 
	 * @param map
	 * @return a map
	 */
	private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
	    List<Entry<K, V>> list = new CopyOnWriteArrayList<>(map.entrySet());
	    Collections.sort(list, new Comparator<Object>() {
	        @SuppressWarnings("unchecked")
	        public int compare(Object o1, Object o2) {
	            return ((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
	        }
	    });

	    Map<K, V> result = new ConcurrentHashMap<>();
	    for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext();) {
	        Map.Entry<K, V> entry = it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }

	    return result;
	}
	
	/**
	 * Gets the best friends, the ones having the highest relationship score.
	 * 
	 * @param person
	 * @return {@link Person} array
	 */
	public static Map<Person, Double> getBestFriends(Person person) {
		Map<Person, Double> bestFriends = getMyOpinionsOfThem(person);
		int size = bestFriends.size();
		if (size == 1) {
			return bestFriends;
		}
		
		else if (size > 1) {
			double hScore = 0;
			for (Entry<Person, Double> entry : bestFriends.entrySet()) {
				Person p = entry.getKey();
				double score = bestFriends.get(p);
				if (hScore < score) {
					hScore = score;
				}
			}	
			Map<Person, Double> list = new ConcurrentHashMap<>();
			for (Entry<Person, Double> entry : bestFriends.entrySet()) {
				Person p = entry.getKey();
				double score = bestFriends.get(p);
				if (score >= hScore) {
					// in case if more than one person has the same score
					list.put(p, score);
				}
			}
			return list;
			
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
		double result = 50D;

		if (hasRelationship(person1, person2)) {
			result = getOpinion(person1, person2);
		}

		return result;
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
	public static double[] getOpinionsOfPerson(Person person1, Person person2) {
		double[] result = {50D, 50D};

		if (hasRelationship(person1, person2)) {
			result = getOpinions(person1, person2);
		}

		return result;
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

		// Update the person's relationships.
		updateRelationships(person, time);

		// Modify the person's stress based on relationships with local people.
		modifyStress(person, time);
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
					boolean oppositeGenders = (!person.getGender().equals(localPerson.getGender()));
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
		
		stressModifier = stressModifier * BASE_STRESS_MODIFIER * time;
		person.getPhysicalCondition().addStress(stressModifier);
	}

	/**
	 * Describes a relationship, given the opinion score.
	 * 
	 * @param opinion
	 * @return the description
	 */
	public static String describeRelationship(double opinion) {
		String result = null;
		if (opinion < 5) result = Msg.getString("TabPanelSocial.opinion.0"); //$NON-NLS-1$
		else if (opinion < 15) result = Msg.getString("TabPanelSocial.opinion.1"); //$NON-NLS-1$
		else if (opinion < 25) result = Msg.getString("TabPanelSocial.opinion.2"); //$NON-NLS-1$
		else if (opinion < 35) result = Msg.getString("TabPanelSocial.opinion.3"); //$NON-NLS-1$
		else if (opinion < 45) result = Msg.getString("TabPanelSocial.opinion.4"); //$NON-NLS-1$
		else if (opinion < 55) result = Msg.getString("TabPanelSocial.opinion.5"); //$NON-NLS-1$
		else if (opinion < 65) result = Msg.getString("TabPanelSocial.opinion.6"); //$NON-NLS-1$
		else if (opinion < 75) result = Msg.getString("TabPanelSocial.opinion.7"); //$NON-NLS-1$
		else if (opinion < 85) result = Msg.getString("TabPanelSocial.opinion.8"); //$NON-NLS-1$
		else if (opinion < 95) result = Msg.getString("TabPanelSocial.opinion.9"); //$NON-NLS-1$			
		else result = Msg.getString("TabPanelSocial.opinion.10"); //$NON-NLS-1$	
		return result.toLowerCase();
	}
	
	/**
	 * Computes the overall relationship score of a settlement.
	 * 
	 * @param s Settlement
	 * @return the score
	 */
	public static double getRelationshipScore(Settlement s) {
		double score = 0;

		int count = 0;
		for (Person pp : s.getAllAssociatedPeople()) {
			Map<Person, Double> friends = getTheirOpinionsOfMe(pp);
			if (!friends.isEmpty()) {	
				for (Entry<Person, Double> entry : friends.entrySet()) {
					Person p = entry.getKey();
					score += friends.get(p);
					count++;
				}
			}
		}
		
		if (count > 0) {
			score = Math.round(score/count *100.0)/100.0;
		}
		
		return score;
	}
	
	/**
	 * Gets the first impression a person has of another person.
	 * 
	 * @param person the person getting the impression.
	 * @param target the person of the impression.
	 * @return the person's opinion of the target as a value from 0 to 100.
	 */
	private static double getFirstImpression(Person person, Person target) {
		double result = 0;

		// Random with bell curve around 50.
		int numberOfIterations = 3;
		for (int x = 0; x < numberOfIterations; x++)
			result += RandomUtil.getRandomDouble(100D);
		result /= numberOfIterations;

		NaturalAttributeManager attributes = target.getNaturalAttributeManager();

		// Modify based on leadership attribute.
		double leaderModifier = attributes.getAttribute(NaturalAttributeType.LEADERSHIP) - 50D;
		result += RandomUtil.getRandomDouble(leaderModifier);
		
		// Modify based on conversation attribute.
		double conversationModifier = attributes.getAttribute(NaturalAttributeType.CONVERSATION) - 50D;
		result += RandomUtil.getRandomDouble(conversationModifier);

		// Modify based on attractiveness attribute if people are of opposite genders.
		// Note: We may add sexual orientation later that will add further complexity to
		// this.
		double attractivenessModifier = attributes.getAttribute(NaturalAttributeType.ATTRACTIVENESS) - 50D;
		boolean oppositeGenders = (!person.getGender().equals(target.getGender()));
		if (oppositeGenders)
			result += RandomUtil.getRandomDouble(attractivenessModifier);
		
		// Modify based on total scientific achievement.
		ScienceType science0 = ScienceType.getJobScience(target.getMind().getJob());	
		ScienceType science1 = ScienceType.getJobScience(person.getMind().getJob());

		// If they are on the same professional field
		if (science0 == science1) {
			// Assuming being in the same field would increase affinity
			result += RandomUtil.getRandomDouble(5);
		}
		
		// Modify as settlers are trained to try to get along with each other.
		if (result < 50D)
			result += RandomUtil.getRandomDouble(SETTLER_MODIFIER);

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
		double result = 0D;

		// Random with bell curve around 50.
		int numberOfIterations = 3;
		for (int x = 0; x < numberOfIterations; x++)
			result += RandomUtil.getRandomDouble(100D);
		result /= numberOfIterations;

		// Modify based on person's conversation attribute.
		double conversationModifier0 = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.CONVERSATION) - 50D;
		// Modify based on target conversation attribute.
		double conversationModifier1 = target.getNaturalAttributeManager().getAttribute(NaturalAttributeType.CONVERSATION) - 50D;
		result += RandomUtil.getRandomDouble((conversationModifier0 + conversationModifier1)/8.0);
		
		// Modify based on attractiveness attribute if people are of opposite genders.
		// Note: We may add sexual orientation later that will add further complexity to
		// this.
		double attractivenessModifier = target.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ATTRACTIVENESS) - 50D;
		boolean oppositeGenders = (!person.getGender().equals(target.getGender()));
		if (oppositeGenders)
			result += RandomUtil.getRandomDouble(attractivenessModifier);

		// Personality diff modifier
		MBTIPersonality personType = person.getMind().getMBTI();
		MBTIPersonality targetType = target.getMind().getMBTI();
		double personalityDiffModifier = (2D - personType.getPersonalityDifference(targetType.getTypeString()))
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
		result += target.getTotalScientificAchievement() / 10D;

		// If target is a scientist, modify based on target's achievement in
		// scientific field.
		ScienceType science = ScienceType.getJobScience(target.getMind().getJob());
		result += target.getScientificAchievement(science);

		return result;
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
		logger = null;
	}

}
