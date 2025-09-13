/*
 * Mars Simulation Project
 * ScientificStudyUtil.java
 * @date 2022-06-10
 * @author Scott Davis
 */
package com.mars_sim.core.science;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;

/**
 * A utility class for scientific studies.
 */
public class ScientificStudyUtil {

	private static UnitManager unitManager = Simulation.instance().getUnitManager();
	
	/**
	 * Private constructor for utility class.
	 */
	private ScientificStudyUtil() {}

	/**
	 * Gets a list of all available collaborators that can be invited to a study.
	 * 
	 * @param study the scientific study.
	 * @return list of potential collaborators.
	 */
	public static int getAvailableNumCollaboratorsForInvite(ScientificStudy study) {
		int result = 0;

		Set<Person> alreadyInvited = study.getInvitedResearchers();
        Collection<Person> allPeople = unitManager.getPeople();
        Iterator<Person> i = allPeople.iterator();
        while (i.hasNext()) {
            Person person = i.next();

            // Make sure person is not already part of study
            if (!person.equals(study.getPrimaryResearcher()) &&
                    !alreadyInvited.contains(person) &&
                    !person.getPhysicalCondition().isDead()) {
                JobType job = person.getMind().getJobType();
                if (job != null) {
                    ScienceType jobScience = ScienceType.getJobScience(job);

                    // Is their Job Science suitable for the study
                    if ((jobScience != null) && (jobScience == study.getScience()
                        		|| ScienceType.isCollaborativeScience(study.getScience(), jobScience))) {
                    	result++;
                    }
                }
            }
        }

        return result;
    }
	
	/**
	 * Gets a list of all available collaborators that can be invited to a study.
	 * 
	 * @param study the scientific study.
	 * @return list of potential collaborators.
	 */
	public static List<Person> getAvailableCollaboratorsForInvite(ScientificStudy study) {
		List<Person> result = new ArrayList<>();

		Set<Person> alreadyInvited = study.getInvitedResearchers();
        Collection<Person> allPeople = unitManager.getPeople();
        Iterator<Person> i = allPeople.iterator();
        while (i.hasNext()) {
            Person person = i.next();

            // Make sure person is not already part of study
            if (!person.equals(study.getPrimaryResearcher()) &&
                    !alreadyInvited.contains(person) &&
                    !person.getPhysicalCondition().isDead()) {
                JobType job = person.getMind().getJobType();
                if (job != null) {
                    ScienceType jobScience = ScienceType.getJobScience(job);

                    // Is their Job Science suitable for the study
                    if ((jobScience != null) && (jobScience == study.getScience()
                        		|| ScienceType.isCollaborativeScience(study.getScience(), jobScience))) {
                    	result.add(person);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Modifies researchers relationships with other known scientists in the same field due to new achievement.
     * 
     * @param researcher the achieving researcher.
     * @param science the field of science.
     * @param achievement the new achievement credit.
     */
    static void modifyScientistRelationshipsFromAchievement(Person researcher,
            ScienceType science, double achievement) {

        Iterator<Person> i = RelationshipUtil.getAllKnownPeople(researcher).iterator();
        while (i.hasNext()) {
            Person person = i.next();
            if (science == ScienceType.getJobScience(person.getMind().getJobType())) {
                RelationshipUtil.changeOpinion(researcher, person, achievement);
            }
        }
    }

	/**
	 * Initializes instances after loading from a saved sim.
	 *
	 * @param {{@link MarsClock}
	 */
	public static void initializeInstances(UnitManager u) {
		unitManager = u;
	}
}
