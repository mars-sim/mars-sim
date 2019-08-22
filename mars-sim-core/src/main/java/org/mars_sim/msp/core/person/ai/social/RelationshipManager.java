/**
 * Mars Simulation Project
 * RelationshipManager.java
 * @version 3.1.0 2017-09-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.social;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.MBTIPersonality;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

import com.phoenixst.plexus.DefaultGraph;
import com.phoenixst.plexus.EdgePredicate;
import com.phoenixst.plexus.EdgePredicateFactory;
import com.phoenixst.plexus.Graph;
import com.phoenixst.plexus.GraphUtils;
import com.phoenixst.plexus.NoSuchNodeException;
import com.phoenixst.plexus.Traverser;

/**
 * The RelationshipManager class keeps track of all the social relationships
 * between people.<br/>
 * <br/>
 * The simulation instance has only one relationship manager.
 */
public class RelationshipManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default serial id. */
	private static Logger logger = Logger.getLogger(RelationshipManager.class.getName());

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

	/** The relationship graph. */
	private Graph relationshipGraph;
	
	private static UnitManager unitManager;

	/**
	 * Constructor
	 */
	public RelationshipManager() {
		// Create new graph for relationships.
		if (relationshipGraph == null)
			relationshipGraph = new DefaultGraph();
	}

	/**
	 * Adds an initial settler who will have an existing relationship with all the
	 * other inhabitants if his/her settlement.
	 * 
	 * @param person     the person to add.
	 * @param settlement the settlement the person starts at.
	 */
	public void addInitialSettler(Person person, Settlement settlement) {
		addPerson(person, settlement.getIndoorPeople());
	}

	/**
	 * Adds a new resupply immigrant who will have an existing relationship with the
	 * other immigrants in his/her group.
	 * 
	 * @param person         the person to add.
	 * @param immigrantGroup the groups of immigrants this person belongs to.
	 */
	public void addNewImmigrant(Person person, Collection<Person> immigrantGroup) {
		addPerson(person, immigrantGroup);
	}

	/**
	 * Adds a new person for the relationship manager.
	 * 
	 * @param person       the new person
	 * @param initialGroup the group that this person has existing relationships
	 *                     with.
	 */
	private void addPerson(Person person, Collection<Person> initialGroup) {
		if ((person == null) || (initialGroup == null))
			throw new IllegalArgumentException("RelationshipManager.addPerson(): null parameter.");
	
		if (!relationshipGraph.containsNode(person.getIdentifier())) {
			relationshipGraph.addNode(person.getIdentifier());

			Iterator<Person> i = initialGroup.iterator();
			while (i.hasNext()) {
				Person person2 = i.next();
				if (person2 != person) {
					addRelationship(person, person2, Relationship.EXISTING_RELATIONSHIP);

					if (logger.isLoggable(Level.FINEST)) {
						logger.finest(person.getName() + " and " + person2.getName() + " have existing relationship.");
					}
				}
			}
		}
	}

	/**
	 * Adds a new relationship between two people.
	 * 
	 * @param person1          the first person (order isn't important)
	 * @param person2          the second person (order isn't important)
	 * @param relationshipType the type of relationship (see Relationship static
	 *                         members)
	 */
	public void addRelationship(Person person1, Person person2, String relationshipType) {
		try {
			Relationship relationship = new Relationship(person1, person2, relationshipType);
//			if (relationshipType.equals(Relationship.EXISTING_RELATIONSHIP))
//				;
//			else if (relationshipType.equals(Relationship.COMMUNICATION_MEETING))
//				;
//			else if (relationshipType.equals(Relationship.FIRST_IMPRESSION))
//				;
			relationshipGraph.addEdge(relationship, person1.getIdentifier(), person2.getIdentifier(), false);
		} catch (NoSuchNodeException e) {
		}
	}

	/**
	 * Checks if a person has a relationship with another person.
	 * 
	 * @param person1 the first person (order isn't important)
	 * @param person2 the second person (order isn't important)
	 * @return true if the two people have a relationship
	 */
	public boolean hasRelationship(Person person1, Person person2) {
		EdgePredicate edgePredicate = EdgePredicateFactory.createEqualsNodes(person1.getIdentifier(), person2.getIdentifier(),
				GraphUtils.UNDIRECTED_MASK);
		return (relationshipGraph.getEdge(edgePredicate) != null);
	}

	/**
	 * Gets the relationship between two people.
	 * 
	 * @param person1 the first person (order isn't important)
	 * @param person2 the second person (order isn't important)
	 * @return the relationship or null if none.
	 */
	public Relationship getRelationship(Person person1, Person person2) {
		Relationship result = null;
		if (hasRelationship(person1, person2)) {
			EdgePredicate edgePredicate = EdgePredicateFactory.createEqualsNodes(person1.getIdentifier(), person2.getIdentifier(),
					GraphUtils.UNDIRECTED_MASK);
			result = (Relationship) relationshipGraph.getEdge(edgePredicate).getUserObject();
		}
		return result;
	}

	/**
	 * Gets all of a person's relationships.
	 * 
	 * @param person the person
	 * @return a list of the person's Relationship objects.
	 */
	public List<Relationship> getAllRelationships(Person person) {
//		if (allRelationshipList == null) {
		 List<Relationship> allRelationshipList = new ArrayList<Relationship>();
			Traverser traverser = relationshipGraph.traverser(person.getIdentifier(), GraphUtils.UNDIRECTED_TRAVERSER_PREDICATE);
			while (traverser.hasNext()) {
				traverser.next();
				Relationship relationship = (Relationship) traverser.getEdge().getUserObject();
				allRelationshipList.add(relationship);
			}
//		}
		return allRelationshipList;
	}

	/**
	 * Gets all the people that a person knows (has met).
	 * 
	 * @param person the person
	 * @return a list of the people the person knows.
	 */
	public Collection<Person> getAllKnownPeople(Person person) {
		Collection<Person> result = new ConcurrentLinkedQueue<Person>();
		Traverser traverser = relationshipGraph.traverser(person.getIdentifier(), GraphUtils.UNDIRECTED_TRAVERSER_PREDICATE);
		while (traverser.hasNext()) {
			Person knownPerson = unitManager.getPersonByID((Integer)traverser.next());
			result.add(knownPerson);
		}
		return result;
	}

	/**
	 * Gets a map of my opinions over them
	 * 
	 * @param person
	 * @return {@link Person} map
	 */
	public Map<Person, Double> getMyOpinionsOfThem(Person person) {
		Map<Person, Double> friends = new HashMap<>();
		Collection<Person> list = getAllKnownPeople(person);
//		System.out.println("list : " + list);
		double highestScore = 0;
//		double nextScore = 0;	
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
	 * Gets a map of their opinions over me
	 * 
	 * @param person
	 * @return {@link Person} map
	 */
	public Map<Person, Double> getTheirOpinionsOfMe(Person person) {
		Map<Person, Double> friends = new HashMap<>();
		Collection<Person> list = getAllKnownPeople(person);
//		System.out.println("list : " + list);
		double highestScore = 0;
//		double nextScore = 0;	
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
	 * Sorts the map according to the value of each entry
	 * 
	 * @param map
	 * @return a map
	 */
	private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
	    List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
	    Collections.sort(list, new Comparator<Object>() {
	        @SuppressWarnings("unchecked")
	        public int compare(Object o1, Object o2) {
	            return ((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
	        }
	    });

	    Map<K, V> result = new LinkedHashMap<>();
	    for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext();) {
	        Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }

	    return result;
	}
	
	/**
	 * Gets the best friends, the ones having the highest relationship score
	 * 
	 * @param person
	 * @return {@link Person} array
	 */
	public Map<Person, Double> getBestFriends(Person person) {
		Map<Person, Double> bestFriends = getMyOpinionsOfThem(person);
		int size = bestFriends.size();
		if (size == 1) {
			return bestFriends;
		}
		
		else if (size > 1) {
			double hScore = 0;
			for (Person p : bestFriends.keySet()) {
				double score = bestFriends.get(p);
				if (hScore < score) {
					hScore = score;
				}
			}
//			System.out.println("best score : " + hScore);		
			Map<Person, Double> list = new HashMap<>();
			for (Person p : bestFriends.keySet()) {
				double score = bestFriends.get(p);
				if (score >= hScore) {
					// in case if more than one person has the same score
					list.put(p, score);
				}
			}
//			System.out.println("list : " + list);
			return list;
			
		}
//		System.out.println("bestFriends : " + bestFriends);
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
	public double getOpinionOfPerson(Person person1, Person person2) {
		double result = 50D;

		if (hasRelationship(person1, person2)) {
			Relationship relationship = getRelationship(person1, person2);
			result = relationship.getPersonOpinion(person1);
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
	 * @return opinion value from 0 (enemy) to 50 (indifferent) to 100 (close
	 *         friend).
	 */
	public double getAverageOpinionOfPeople(Person person1, Collection<Person> people) {

		if (people == null)
			throw new IllegalArgumentException("people is null");

		if (people.size() > 0) {
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
	 * Time passing for a person's relationships.
	 * 
	 * @param person the person
	 * @param time   the time passing (millisols)
	 * @throws Exception if error.
	 */
	public void timePassing(Person person, double time) {

		// Update the person's relationships.
		updateRelationships(person, time);

		// Modify the person's stress based on relationships with local people.
		modifyStress(person, time);
	}

	/**
	 * Updates the person's relationships
	 * 
	 * @param person the person to update
	 * @param time   the time passing (millisols)
	 * @throws Exception if error
	 */
	private void updateRelationships(Person person, double time) {

		double personStress = person.getPhysicalCondition().getStress();

		// Get the person's local group of people.
		Collection<Person> localGroup = person.getLocalGroup();

		// Go through each person in local group.
		Iterator<Person> i = localGroup.iterator();
//		int count2 = 0;
		while (i.hasNext()) {
			Person localPerson = i.next();
			double localPersonStress = localPerson.getPhysicalCondition().getStress();

			// Check if new relationship.
			if (!hasRelationship(person, localPerson)) {
				addRelationship(person, localPerson, Relationship.FIRST_IMPRESSION);

				if (logger.isLoggable(Level.FINEST)) {
					logger.finest(
							person.getName() + " and " + localPerson.getName() + " meet for the first time.");
				}
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
				if (oppositeGenders)
					RandomUtil.getRandomDouble(changeAmount += attractivenessModifier);

				// Modify based on same-gender bonding.
				double genderBondingModifier = BASE_GENDER_BONDING_MODIFIER * time;
				if (!oppositeGenders)
					RandomUtil.getRandomDouble(changeAmount += genderBondingModifier);

				// Modify based on personality differences.
				MBTIPersonality personPersonality = person.getMind().getMBTI();
				MBTIPersonality localPersonality = localPerson.getMind().getMBTI();
				double personalityDiffModifier = (2D
						- (double) personPersonality.getPersonalityDifference(localPersonality.getTypeString())) / 2D;
				personalityDiffModifier *= PERSONALITY_DIFF_MODIFIER * time;
				changeAmount += RandomUtil.getRandomDouble(personalityDiffModifier);

				// Modify based on settlers being trained to get along with each other.
				double settlerModifier = SETTLER_MODIFIER * time;
				changeAmount += RandomUtil.getRandomDouble(settlerModifier);

				// Modify magnitude based on the collective stress of the two people.
				double stressChangeModifier = 1 + ((personStress + localPersonStress) / 100D);
				changeAmount *= stressChangeModifier;

				// Change the person's opinion of the other person.
				Relationship relationship = getRelationship(person, localPerson);
				if (relationship != null)
					relationship.setPersonOpinion(person, relationship.getPersonOpinion(person) + changeAmount);
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest(person.getName() + " has changed opinion of " + localPerson.getName() + " by "
							+ changeAmount);
				}
			}
		}
//		count2++;
	}

	/**
	 * Modifies the person's stress based on relationships with local people.
	 * 
	 * @param person the person
	 * @param time   the time passing (millisols)
	 * @throws Exception if error
	 */
	private void modifyStress(Person person, double time) {
		double stressModifier = 0D;

		Iterator<Person> i = person.getLocalGroup().iterator();
		while (i.hasNext())
			stressModifier -= ((getOpinionOfPerson(person, i.next()) - 50D) / 50D);

		stressModifier = stressModifier * BASE_STRESS_MODIFIER * time;
		PhysicalCondition condition = person.getPhysicalCondition();
		condition.setStress(condition.getStress() + stressModifier);
	}

	/**
	 * Describes a relationship, given the opinion score
	 * 
	 * @param opinion
	 * @return the description
	 */
	public static String describeRelationship(double opinion) {
		String result = null;
		if (opinion < 5) result = Msg.getString("TabPanelSocial.opinion.0"); //$NON-NLS-1$
		else if (opinion < 15) result = Msg.getString("TabPanelSocial.opinion.1"); //$NON-NLS-1$
		else if (opinion < 25) result = Msg.getString("TabPanelSocial.opinion.2"); //$NON-NLS-1$
		else if (opinion < 40) result = Msg.getString("TabPanelSocial.opinion.3"); //$NON-NLS-1$
		else if (opinion < 55) result = Msg.getString("TabPanelSocial.opinion.4"); //$NON-NLS-1$
		else if (opinion < 70) result = Msg.getString("TabPanelSocial.opinion.5"); //$NON-NLS-1$
		else if (opinion < 85) result = Msg.getString("TabPanelSocial.opinion.6"); //$NON-NLS-1$
		else if (opinion < 95) result = Msg.getString("TabPanelSocial.opinion.7"); //$NON-NLS-1$
		else result = Msg.getString("TabPanelSocial.opinion.8"); //$NON-NLS-1$	
		return result.toLowerCase();
	}
	
	/**
	 * Computes the overall relationship score of a settlement
	 * 
	 * @param s Settlement
	 * @return the score
	 */
	public double getRelationshipScore(Settlement s) {
		double score = 0;

		List<Person> list0 = new ArrayList<>(s.getAllAssociatedPeople());

		int count = 0;
		for (Person pp : list0) {
			Map<Person, Double> friends = getTheirOpinionsOfMe(pp);//.getMyOpinionsOfThem(pp);
			if (!friends.isEmpty()) {
				List<Person> list = new ArrayList<>(friends.keySet());
				for (int i = 0; i < list.size(); i++) {
					Person p = list.get(i);
					score += friends.get(p);
					count++;
				}
			}
		}
		
		score = Math.round(score/count *100.0)/100.0;
		
		return score;
	}
	
	public static void initializeInstances(UnitManager u) {
		unitManager = u;		
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		relationshipGraph = null;
	}

}