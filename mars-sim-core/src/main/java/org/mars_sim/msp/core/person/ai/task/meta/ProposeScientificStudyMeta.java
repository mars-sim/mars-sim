/*
 * Mars Simulation Project
 * ProposeScientificStudyMeta.java
 * @date 2021-11-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.role.Role;
import org.mars_sim.msp.core.person.ai.task.ProposeScientificStudy;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the ProposeScientificStudy task.
 */
public class ProposeScientificStudyMeta extends MetaTask {

    private static final double FACTOR = 2D;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.proposeScientificStudy"); //$NON-NLS-1$

    public ProposeScientificStudyMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC, TaskTrait.LEADERSHIP);
		setPreferredJob(JobType.ACADEMICS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ProposeScientificStudy(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

		Settlement settlement = person.getAssociatedSettlement();
        int pop = settlement.getInitialPopulation();
		JobType job = person.getMind().getJob();

        if (pop <= 6) {
	        if (!JobType.isAcademicType(job)) {
	        	return 0;
	        }
        }
        else if (pop <= 12) {
        	if (!JobType.isMedical(job)
        			&& !JobType.isScienceType(job)) {
	        	return 0;
	        }
        }
        else {
        	if (!JobType.isScienceType(job)) {
	        	return 0;
	        }
        }

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
        	return 0;

        if (person.isInside()) {

	        ScientificStudy study = person.getStudy();
	        if (study != null) {
	            // Check if study is in proposal phase.
	            if (study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
	                // Once a person starts a Study they should focus on the Proposal
	                result += 100D;
	            }
	        }

	        else {
	            // Probability of starting a new scientific study.
	            Role role = person.getRole();
	            // Check person has a science role
	            if (role != null) {
					switch(role.getType()) {
	            		case CHIEF_OF_SCIENCE:
	            		case SCIENCE_SPECIALIST:
	            			result += 50D;
							break;
	            		case CHIEF_OF_COMPUTING:
		            	case COMPUTING_SPECIALIST:
            				result += 20D;
							break;
            			case CHIEF_OF_AGRICULTURE:
		            	case AGRICULTURE_SPECIALIST:
            				result += 10D;
							break;
						default:
							result += 5D;
							break;
            		}
	            }
	            else {
					result += 5D;
				}

				// Check the favourited research of the Reporting Authority
				ScienceType science = ScienceType.getJobScience(job);
				result *= settlement.getSponsor().getStudyRatio(science);
	        }

	        // Crowding modifier
	        if (person.isInSettlement()) {
	            Building b = BuildingManager.getAvailableBuilding(study, person);
	            if (b != null) {
	                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, b);
	                result *= TaskProbabilityUtil.getRelationshipModifier(person, b);
	            }
	        }

	        else if (person.isInVehicle()) {
		        // Check if person is in a moving rover.
		        if (Vehicle.inMovingRover(person)) {
			        // the bonus for proposing scientific study inside a vehicle,
		        	// rather than having nothing to do if a person is not driving
		        	result += 20;
		        }
		        else
		        	// the bonus for proposing scientific study inside a vehicle,
		        	// rather than having nothing to do if a person is not driving
		        	result += 10;
	        }

	        result *= FACTOR * person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
	        result = applyPersonModifier(result, person);
        }

        if (result < 0) result = 0;

        return result;
    }
}
