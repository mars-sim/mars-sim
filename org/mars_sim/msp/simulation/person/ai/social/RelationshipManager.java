/**
 * Mars Simulation Project
 * RelationshipManager.java
 * @version 2.77 2004-09-06
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.social;

import com.phoenixst.plexus.*;
import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The RelationshipManager class keeps track of all the social 
 * relationships between people.
 *
 * The simulation instance has only one relationship manager. 
 */
public class RelationshipManager implements Serializable {
	
	private Graph relationshipGraph; // The relationship graph

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
				if (person2 != person) addRelationship(person, person2, Relationship.EXISTING_RELATIONSHIP);
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
		Relationship relationship = new Relationship(person1, person2, Relationship.EXISTING_RELATIONSHIP);
		relationshipGraph.addEdge(relationship, person1, person2, false);
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
		// TraverserPredicate traverserPredicate = TraverserPredicateFactory.createEqualsNode(person, GraphUtils.UNDIRECTED_MASK);
		Traverser traverser = relationshipGraph.traverser(person, GraphUtils.UNDIRECTED_TRAVERSER_PREDICATE);
		while (traverser.hasNext()) {
			Person knownPerson = (Person) traverser.next();
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
		// TraverserPredicate traverserPredicate = TraverserPredicateFactory.createEqualsNode(person, GraphUtils.UNDIRECTED_MASK);
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
}