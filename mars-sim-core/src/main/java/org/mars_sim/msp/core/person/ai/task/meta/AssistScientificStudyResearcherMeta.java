/**
 * Mars Simulation Project
 * AssistScientificStudyResearcherMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
        
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();
        
        if (fatigue > 1000 || stress > 50 || hunger > 500)
        	return 0;
        
        if (person.isInside()) {
	        // Find potential researchers.
	        Collection<Person> potentialResearchers = AssistScientificStudyResearcher.getBestResearchers(person);
	        int size = potentialResearchers.size();
	        if (size == 0)
	        	return 0;
	        
	        else {
	            result += size * RandomUtil.getRandomInt(1, 10);

	            if (person.isInVehicle()) {	
	    	        // Check if person is in a moving rover.
	    	        if (Vehicle.inMovingRover(person)) {
	    		        // the bonus for proposing scientific study inside a vehicle, 
	    	        	// rather than having nothing to do if a person is not driving
	    	        	result += 20;
	    	        }
	            }
	            
                Person researcher = (Person) potentialResearchers.toArray()[0];

	            // If assistant is in a settlement, use crowding modifier.
                Building building = BuildingManager.getBuilding(researcher);
                if (building != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                }

	            // Job modifier.
	            Job job = person.getMind().getJob();
	            if (job != null) {
	                result *= job.getStartTaskProbabilityModifier(AssistScientificStudyResearcher.class)
	                		* person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
	            }

	            // Modify if research is the person's favorite activity.
	            if (person.getFavorite().getFavoriteActivity() == FavoriteType.RESEARCH) {
		        	result *= RandomUtil.getRandomDouble(3.0);
	            }

                // Add Preference modifier
	            if (result > 0)
	            	result = result + result * person.getPreference().getPreferenceScore(this)/2D;

	        }
        }

        if (result <= 0) result = 0;
        
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