/**
 * Mars Simulation Project
 * ScientificStudyUtil.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.science;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * A utility class for scientific studies.
 */
public class ScientificStudyUtil {

	private static Simulation sim = Simulation.instance();
	private static UnitManager unitManager = sim.getUnitManager();
	private static RelationshipManager relationshipManager = sim.getRelationshipManager();
    
	/**
	 * Private constructor for utility class.
	 */
	private ScientificStudyUtil() {}

	/**
	 * Gets a list of all available collaborators that can be invited to a study.
	 * @param study the scientific study.
	 * @return list of potential collaborators.
	 */
	public static List<Person> getAvailableCollaboratorsForInvite(ScientificStudy study) {
		List<Person> result = new CopyOnWriteArrayList<>();

		Set<Person> alreadyInvited = study.getInvitedResearchers();
        Collection<Person> allPeople = unitManager.getPeople();
        Iterator<Person> i = allPeople.iterator();
        while (i.hasNext()) {
            Person person = i.next();
            
            // Make sure person is not already part of study
            if (!person.equals(study.getPrimaryResearcher()) && 
                    !alreadyInvited.contains(person) &&
                    !person.getPhysicalCondition().isDead()) {
                JobType job = person.getMind().getJob();
                if (job != null) {
                    ScienceType jobScience = ScienceType.getJobScience(job);
                    
                    // Is their Job Science suitable for the study
                    if ((jobScience != null) && (jobScience.equals(study.getScience())
                        		|| ScienceType.isCollaborativeScience(study.getScience(), jobScience))) {
                    	result.add(person);
                    }
                }
            }
        }
        
        return result;
    }
  
    /**
     * Modify researchers relationships with other known scientists in the same field due to new achievement.
     * @param researcher the achieving researcher.
     * @param science the field of science.
     * @param achievement the new achievement credit.
     */
    static void modifyScientistRelationshipsFromAchievement(Person researcher, 
            ScienceType science, double achievement) {
        
        Iterator<Person> i = relationshipManager.getAllKnownPeople(researcher).iterator();
        while (i.hasNext()) {
            Person person = i.next();
            if (science == ScienceType.getJobScience(person.getMind().getJob())) {
                Relationship relationship = relationshipManager.getRelationship(researcher, person);
                if (relationship != null) {
                    double currentOpinion = relationship.getPersonOpinion(person);
                    relationship.setPersonOpinion(person, currentOpinion + achievement);
                }
            }
        }
    }
    
	/**
	 * initializes instances after loading from a saved sim
	 * 
	 * @param {{@link MarsClock}
	 */
	public static void initializeInstances(RelationshipManager r, UnitManager u) {
		unitManager = u;		
		relationshipManager = r;
	}
}
