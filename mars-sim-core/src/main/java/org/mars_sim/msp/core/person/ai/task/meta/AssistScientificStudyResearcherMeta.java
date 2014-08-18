/**
 * Mars Simulation Project
 * AssistScientificStudyResearcherMeta.java
 * @version 3.07 2014-08-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Collection;

import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * Meta task for the AssistScientificStudyResearcher task.
 */
public class AssistScientificStudyResearcherMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Assisting researcher";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new AssistScientificStudyResearcher(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;
        
        // Find potential researchers.
        Collection<Person> potentialResearchers = AssistScientificStudyResearcher.getBestResearchers(person);
        if (potentialResearchers.size() > 0) {
            result = 50D; 
        
            // If assistant is in a settlement, use crowding modifier.
            if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
                Person researcher = (Person) potentialResearchers.toArray()[0];

                Building building = BuildingManager.getBuilding(researcher);
                if (building != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                }
                else result = 0D;
            }
        }
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(AssistScientificStudyResearcher.class);
        }
        
        return result;
    }
}