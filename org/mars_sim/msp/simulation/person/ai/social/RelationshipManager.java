/**
 * Mars Simulation Project
 * RelationshipManager.java
 * @version 2.77 2004-09-09
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.social;

import com.phoenixst.plexus.*;
import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.PersonalityType;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The RelationshipManager class keeps track of all the social 
 * relationships between people.
 *
 * The simulation instance has only one relationship manager. 
 */
public class RelationshipManager implements Serializable {
	
	// The base % chance of a relationship change per millisol.
	private static final double BASE_RELATIONSHIP_CHANGE_PROBABILITY = .1D;
	
	// The base change amount per millisol. 
	private static final double BASE_RELATIONSHIP_CHANGE_AMOUNT = .1D;
	
	// The base stress modifier per millisol for relationships.
	private static final double BASE_STRESS_MODIFIER = .1D;
	
	// The base opinion modifier per millisol for relationship change.
	private static final double BASE_OPINION_MODIFIER = .2D;
	
	// The base conversation modifier per millisol for relationship change.
	private static final double BASE_CONVERSATION_MODIFIER = .2D;
	
	// The base attractiveness modifier per millisol for relationship change.
	private static final double BASE_ATTRACTIVENESS_MODIFIER = .1D;
	
	// The base gender bonding modifier per millisol for relationship change.
	private static final double BASE_GENDER_BONDING_MODIFIER = .02D;
	
	// The base personality diff modifier per millisol for relationship change.
	private static final double PERSONALITY_DIFF_MODIFIER = .1D;
	
	private Graph relationshipGraph; // The relationship graph
	int count = 0;

	/**
	 * Constructor
	 */
	public RelationshipManager() {
		
		// Create new graph for relationships.
		relationshipGraph = new DefaultGraph(); 
	}
	
	/**
	 * Adds an innitial settler who will have an existing relationship with all the 
	 * other inhabitants if his/her settlement.
	 * @param person the person to add.
	 * @param settlement the settlement the person starts at.
	 */
	public void addInitialSettler(Person person, Settlement settlement) {
		addPerson(person, settlement.getInhabitants());
	}
	
	/**
	 * Adds a new resupply immigrant who will have an existing relationship with the
	 * other immigrants in his/her group.
	 * @param person the person to add.
	 * @param immigrantGroup the groups of immigrants this person belongs to.
	 */
	public void addNewImmigrant(Person person, PersonCollection immigrantGroup) {
		addPerson(person, immigrantGroup);
	}
	
	/**
	 * Adds a new person for the relationship manager.
	 * @param person the new person
	 * @param initialGroup the group that this person has existing relationships with.
	 */
	private void addPerson(Person person, PersonCollection initialGroup) {
		if ((person == null) || (initialGroup == null)) 
			throw new IllegalArgumentException("RelationshipManager.addPerson(): null parameter.");
		
		if (!relationshipGraph.containsNode(person)) {
			relationshipGraph.addNode(person);
			
			PersonIterator i = initialGroup.iterator();
			while (i.hasNext()) {
				Person person2 = i.next();
				if (person2 != person) {
					addRelationship(person, person2, Relationship.EXISTING_RELATIONSHIP);
					// System.out.println(person.getName() + " and " + person2.getName() + " have existing relationship.  " + count);
				} 
			}
		}
	}
	
	/**
	 * Adds a new relationship between two people.
	 * @param person1 the first person (order isn't important)
	 * @param person2 the second person (order isn't important)
	 * @param relationshipType the type of relationship (see Relationship static members)
	 */
	private void addRelationship(Person person1, Person person2, String relationshipType) {
		Relationship relationship = new Relationship(person1, person2, relationshipType);
		relationshipGraph.addEdge(relationship, person1, person2, false);
		count++;
	}
	
	/**
	 * Checks if a person has a relationship with another person.
	 * @param person1 the first person (order isn't important)
	 * @param person2 the second person (order isn't important)
	 * @return true if the two people have a relationship
	 */
	public boolean hasRelationship(Person person1, Person person2) {
		EdgePredicate edgePredicate = EdgePredicateFactory.createEqualsNodes(person1, person2, GraphUtils.UNDIRECTED_MASK);
		return (relationshipGraph.getEdge(edgePredicate) != null);
	}

	/**
	 * Gets the relationship between two people.
	 * @param person1 the first person (order isn't important)
	 * @param person2 the second person (order isn't important)
	 * @return the relationship or null if none.
	 */
	public Relationship getRelationship(Person person1, Person person2) {
		Relationship result = null;
		if (hasRelationship(person1, person2)) {
			EdgePredicate edgePredicate = EdgePredicateFactory.createEqualsNodes(person1, person2, GraphUtils.UNDIRECTED_MASK);
			result = (Relationship) relationshipGraph.getEdge(edgePredicate).getUserObject();
		}
		return result;
	}
	
	/**
	 * Gets all of a person's relationships.
	 * @param person the person 
	 * @return a list of the person's Relationship objects.
	 */
	public List getAllRelationships(Person person) {
		List result = new ArrayList();
		Traverser traverser = relationshipGraph.traverser(person, GraphUtils.UNDIRECTED_TRAVERSER_PREDICATE);
		while (traverser.hasNext()) {
			traverser.next();
			Relationship relationship = (Relationship) traverser.getEdge().getUserObject();
			result.add(relationship);
		}
		return result;
	}
	
	/**
	 * Gets all the people that a person knows (has met).
	 * @param person the person
	 * @return a list of the people the person knows.
	 */
	public PersonCollection getAllKnownPeople(Person person) {
		PersonCollection result = new PersonCollection();
		Traverser traverser = relationshipGraph.traverser(person, GraphUtils.UNDIRECTED_TRAVERSER_PREDICATE);
		while (traverser.hasNext()) {
			Person knownPerson = (Person) traverser.next();
			result.add(knownPerson);
		}
		return result;
	}
	
	/**
	 * Gets the opinion that a person has of another person.
	 * Note: If the people don't have a relationship, return default value of 50.
	 * @param person1 the person holding the opinion.
	 * @param person2 the person who the opinion is of.
	 * @return opinion value from 0 (enemy) to 50 (indifferent) to 100 (close friend).
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
	 * Time passing for a person's relationships.
	 * @param person the person 
	 * @param time the time passing (millisols)
	 * @throws Exception if error.
	 */
	public void timePassing(Person person, double time) throws Exception {
		
		// Update the person's relationships.
		updateRelationships(person, time);
		
		// Modify the person's stress based on relationships with local people.
		modifyStress(person, time);
	}
	
	/**
	 * Updates the person's relationships
	 * @param person the person to update
	 * @param time the time passing (millisols)
	 * @throws Exception if error
	 */
	private void updateRelationships(Person person, double time) throws Exception {
		
		double personStress = person.getPhysicalCondition().getStress();
		
		// Get the person's local group of people.
		PersonCollection localGroup = person.getLocalGroup();
		
		// Go through each person in local group.
		PersonIterator i = localGroup.iterator();
		while (i.hasNext()) {
			Person localPerson = i.next();
			double localPersonStress = localPerson.getPhysicalCondition().getStress(); 
			
			// Check if new relationship.
			if (!hasRelationship(person, localPerson)) {
				addRelationship(person, localPerson, Relationship.FIRST_IMPRESSION);
				// System.out.println(person.getName() + " and " + localPerson.getName() + " meet for the first time.  " + count);
			}
			
			// Determine probability of relationship change per millisol.
			double changeProbability = BASE_RELATIONSHIP_CHANGE_PROBABILITY * time;
			double stressProbModifier = 1D + ((personStress + localPersonStress) / 100D);
			if (RandomUtil.lessThanRandPercent(changeProbability * stressProbModifier)) {
				
				// Randomly determine change amount (negative or positive)
				double changeAmount = RandomUtil.getRandomDouble(BASE_RELATIONSHIP_CHANGE_AMOUNT) * time;
				if (RandomUtil.lessThanRandPercent(50)) changeAmount = 0 - changeAmount;
				
				// Modify based on difference in other person's opinion.
				double otherOpinionModifier = (getOpinionOfPerson(localPerson, person) - getOpinionOfPerson(person, localPerson)) / 100D;
				otherOpinionModifier*= BASE_OPINION_MODIFIER * time;
				changeAmount+= RandomUtil.getRandomDouble(otherOpinionModifier);
				
				// Modify based on the conversation attribute of other person.
				double conversation = localPerson.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.CONVERSATION);
				double conversationModifier = (conversation - 50D) / 50D;
				conversationModifier*= BASE_CONVERSATION_MODIFIER * time;
				changeAmount+= RandomUtil.getRandomDouble(conversationModifier);
				
				// Modify based on attractiveness attribute if people are of opposite genders.
				// Note: We may add sexual orientation later that will add further complexity to this.
				double attractiveness = localPerson.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.ATTRACTIVENESS);
				double attractivenessModifier = (attractiveness - 50D) / 50D;
				attractivenessModifier*= BASE_ATTRACTIVENESS_MODIFIER * time;
				boolean oppositeGenders = (!person.getGender().equals(localPerson.getGender()));
				if (oppositeGenders) RandomUtil.getRandomDouble(changeAmount+= attractivenessModifier);
				
				// Modify based on same-gender bonding.
				double genderBondingModifier = BASE_GENDER_BONDING_MODIFIER * time;
				if (!oppositeGenders) RandomUtil.getRandomDouble(changeAmount+= genderBondingModifier);
				
				// Modify based on personality differences.
				PersonalityType personPersonality = person.getMind().getPersonalityType();
				PersonalityType localPersonality = localPerson.getMind().getPersonalityType();
				double personalityDiffModifier = (2D - (double) personPersonality.getPersonalityDifference(localPersonality.getTypeString())) / 2D;
				personalityDiffModifier*= PERSONALITY_DIFF_MODIFIER * time;
				changeAmount+= RandomUtil.getRandomDouble(personalityDiffModifier);
				
				// Modify magnitude based on the collective stress of the two people.
				double stressChangeModifier = 1 + ((personStress + localPersonStress) / 100D);
				changeAmount*= stressChangeModifier;
				
				// Change the person's opinion of the other person.
				Relationship relationship = getRelationship(person, localPerson);
				relationship.setPersonOpinion(person, relationship.getPersonOpinion(person) + changeAmount);
				// System.out.println(person.getName() + " has changed opinion of " + localPerson.getName() + " by " + changeAmount);
			}
		}	
	}
	
	/**
	 * Modifies the person's stress based on relationships with local people.
	 * @param person the person
	 * @param time the time passing (millisols)
	 * @throws Exception if error
	 */
	private void modifyStress(Person person, double time) throws Exception {
		double stressModifier = 0D;
		
		PersonIterator i = person.getLocalGroup().iterator();
		while (i.hasNext()) stressModifier-= ((getOpinionOfPerson(person, i.next()) - 50D) / 50D);
		
		stressModifier = stressModifier * BASE_STRESS_MODIFIER * time;
		PhysicalCondition condition = person.getPhysicalCondition();
		condition.setStress(condition.getStress() + stressModifier);
	}
}