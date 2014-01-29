/**
 * Mars Simulation Project
 * ScientificStudyUtil.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.science;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.structure.Settlement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A utility class for scientific studies.
 */
public class ScientificStudyUtil {

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
        List<Person> result = new ArrayList<Person>();
        
        Collection<Person> allPeople = Simulation.instance().getUnitManager().getPeople();
        Iterator<Person> i = allPeople.iterator();
        while (i.hasNext()) {
            Person person = i.next();
            boolean available = false;
            
            if (!person.equals(study.getPrimaryResearcher()) && 
                    !study.hasResearcherBeenInvited(person)) {
                Job job = person.getMind().getJob();
                if (job != null) {
                    Science jobScience = ScienceUtil.getAssociatedScience(job);
                    if (jobScience != null) {
                        if (jobScience.equals(study.getScience())) available = true;
                        else {
                            if (ScienceUtil.isCollaborativeScience(study.getScience(), jobScience)) 
                                available = true;
                        }
                    }
                }
            }
            
            // Make sure person is alive.
            if (person.getPhysicalCondition().isDead()) available = false;
            
            if (available) result.add(person);
        }
        
        return result;
    }
    
    /**
     * Determine the results of a study's peer review process.
     * @param study the scientific study.
     * @return true if study passes peer review, false if it fails to pass.
     */
    static boolean determinePeerReviewResults(ScientificStudy study) {
        
        double baseChance = 50D;
        
        // Modify based on primary researcher's academic aptitude attribute.
        int academicAptitude = study.getPrimaryResearcher().getNaturalAttributeManager().
                getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
        double academicAptitudeModifier = (academicAptitude - 50) / 2D;
        baseChance += academicAptitudeModifier;
        
        Iterator<Person> i = study.getCollaborativeResearchers().keySet().iterator();
        while (i.hasNext()) {
            Person researcher = i.next();
            double collaboratorModifier = 10D;
            
            // Modify based on collaborative researcher skill in their science.
            Science collaborativeScience = study.getCollaborativeResearchers().get(researcher);
            String skillName = ScienceUtil.getAssociatedSkill(collaborativeScience);
            int skillLevel = researcher.getMind().getSkillManager().getSkillLevel(skillName);
            collaboratorModifier *= (double) skillLevel / (double) study.getDifficultyLevel();
            
            // Modify based on researcher's academic aptitude attribute.
            int collaboratorAcademicAptitude = researcher.getNaturalAttributeManager().
                    getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
            double collaboratorAcademicAptitudeModifier = (collaboratorAcademicAptitude - 50) / 10D;
            collaboratorModifier += collaboratorAcademicAptitudeModifier;
            
            // Modify based on if collaborative science is different from primary science.
            if (!collaborativeScience.equals(study.getScience())) collaboratorModifier /= 2D;
            
            baseChance += collaboratorModifier;
        }
        
        // Randomly determine if study passes peer review.
        return RandomUtil.getRandomDouble(100D) < baseChance;
    }
    
    /**
     * Provide achievements for the completion of a study.
     * @param study the scientific study.
     */
    static void provideCompletionAchievements(ScientificStudy study) {
        
        double baseAchievement = study.getDifficultyLevel();
        Science primaryScience = study.getScience();
        
        // Add achievement credit to primary researcher.
        Person primaryResearcher = study.getPrimaryResearcher();
        primaryResearcher.addScientificAchievement(baseAchievement, primaryScience);
        study.setPrimaryResearchEarnedScientificAchievement(baseAchievement);
        modifyScientistRelationshipsFromAchievement(primaryResearcher, primaryScience, baseAchievement);
        
        // Add achievement credit to primary settlement.
        Settlement primarySettlement = study.getPrimarySettlement();
        primarySettlement.addScientificAchievement(baseAchievement, primaryScience);
        
        // Add achievement credit to collaborative researchers.
        double collaborativeAchievement = baseAchievement / 3D;
        Iterator<Person> i = study.getCollaborativeResearchers().keySet().iterator();
        while (i.hasNext()) {
            Person researcher = i.next();
            Science collaborativeScience = study.getCollaborativeResearchers().get(researcher);
            researcher.addScientificAchievement(collaborativeAchievement, collaborativeScience);
            study.setCollaborativeResearcherEarnedScientificAchievement(researcher, collaborativeAchievement);
            modifyScientistRelationshipsFromAchievement(researcher, collaborativeScience, collaborativeAchievement);
            
            // Add achievement credit to the collaborative researcher's current settlement.
            Settlement collaboratorSettlement = researcher.getAssociatedSettlement();
            if (collaboratorSettlement != null) collaboratorSettlement.addScientificAchievement(
                    collaborativeAchievement, collaborativeScience);
        }
    }
    
    /**
     * Modify researchers relationships with other known scientists in the same field due to new achievement.
     * @param researcher the achieving researcher.
     * @param science the field of science.
     * @param achievement the new achievement credit.
     */
    private static void modifyScientistRelationshipsFromAchievement(Person researcher, 
            Science science, double achievement) {
        
        RelationshipManager manager = Simulation.instance().getRelationshipManager();
        Iterator<Person> i = manager.getAllKnownPeople(researcher).iterator();
        while (i.hasNext()) {
            Person person = i.next();
            if (science.equals(ScienceUtil.getAssociatedScience(person.getMind().getJob()))) {
                Relationship relationship = manager.getRelationship(researcher, person);
                if (relationship != null) {
                    double currentOpinion = relationship.getPersonOpinion(person);
                    relationship.setPersonOpinion(person, currentOpinion + achievement);
                }
            }
        }
    }
}