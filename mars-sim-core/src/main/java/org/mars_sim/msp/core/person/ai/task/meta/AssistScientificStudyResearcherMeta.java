/**
 * Mars Simulation Project
 * AssistScientificStudyResearcherMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryExperiment;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * Meta task for the AssistScientificStudyResearcher task.
 */
public class AssistScientificStudyResearcherMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.assistScientificStudyResearcher"); //$NON-NLS-1$

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
        
        if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {	
	        // Check if person is in a moving rover.
	        if (PerformLaboratoryExperiment.inMovingRover(person)) {
	            result = 0D;
	            return 0;
	        } 	       
	        else
	        // the penalty for performing experiment inside a vehicle
	        	result = -20D;
        }
        
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT
            	|| person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
	        // Find potential researchers.
	        Collection<Person> potentialResearchers = AssistScientificStudyResearcher.getBestResearchers(person);
	        if (potentialResearchers.size() > 0) {
	            result = 50D;

	            // If assistant is in a settlement, use crowding modifier.
                Person researcher = (Person) potentialResearchers.toArray()[0];

                Building building = BuildingManager.getBuilding(researcher);
                if (building != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                }

	            // Job modifier.
	            Job job = person.getMind().getJob();
	            if (job != null) {
	                result *= job.getStartTaskProbabilityModifier(AssistScientificStudyResearcher.class)
	                		* person.getParkedSettlement().getGoodsManager().getResearchFactor();
	            }

	            // Modify if research is the person's favorite activity.
	            if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Research")) {
	                result *= 2D;
	            }

                // 2015-06-07 Added Preference modifier
	            if (result > 0)
	            	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

                if (result < 0) result = 0;

	        }
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}