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
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.StatusType;
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
        
        if (person.isInVehicle()) {	
	        // Check if person is in a moving rover.
	        if (inMovingRover(person)) {
	            return 0;
	        } 	       
	        else
	        // the penalty for performing experiment inside a vehicle
	        	result = -50D;
        }
        
        if (person.isInside()) {
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
	                		* person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
	            }

	            // Modify if research is the person's favorite activity.
	            if (person.getFavorite().getFavoriteActivity() == FavoriteType.RESEARCH) {
	                result *= 2D;
	            }

                // 2015-06-07 Added Preference modifier
	            if (result > 0)
	            	result = result + result * person.getPreference().getPreferenceScore(this)/2D;

	        }
        }

        if (result < 0) result = 0;
        
        return result;
    }

    /**
     * Checks if the person is in a moving vehicle.
     * @param person the person.
     * @return true if person is in a moving vehicle.
     */
    public static boolean inMovingRover(Person person) {

        boolean result = false;

        if (person.isInVehicle()) {
            Vehicle vehicle = person.getVehicle();
            if (vehicle.getStatus() == StatusType.MOVING) {
                result = true;
            }
            else if (vehicle.getStatus() == StatusType.TOWED) {
                Vehicle towingVehicle = vehicle.getTowingVehicle();
                if (towingVehicle.getStatus() == StatusType.MOVING ||
                        towingVehicle.getStatus() == StatusType.TOWED) {
                    result = false;
                }
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