/**
 * Mars Simulation Project
 * CompileScientificStudyResultsMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.Iterator;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.CompileScientificStudyResults;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the CompileScientificStudyResults task.
 */
public class CompileScientificStudyResultsMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.compileScientificStudyResults"); //$NON-NLS-1$

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(CompileScientificStudyResultsMeta.class.getName());

    public CompileScientificStudyResultsMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST);
		
	}

    @Override
    public Task constructInstance(Person person) {
        return new CompileScientificStudyResults(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
        	return 0;
        }

        if (person.isInside()) {

	        // Add probability for researcher's primary study (if any).
            ScientificStudy primaryStudy = person.getStudy();
	        if ((primaryStudy != null)
        		&& ScientificStudy.PAPER_PHASE.equals(primaryStudy.getPhase())
            	&& !primaryStudy.isPrimaryPaperCompleted()) {
                try {
                    double primaryResult = 50D;

                    // If researcher's current job isn't related to study science, divide by two.
                    JobType job = person.getMind().getJob();
                    if (job != null) {
                        //ScienceType jobScience = ScienceType.getJobScience(job);
                        if (primaryStudy.getScience() != ScienceType.getJobScience(job))
                        	primaryResult /= 2D;
                    }

                    result += primaryResult;
                }
                catch (Exception e) {
                    logger.severe(person, "getProbability(): ", e);
                }
	        }

	        // Add probability for each study researcher is collaborating on.
	        Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.PAPER_PHASE.equals(collabStudy.getPhase())
	            		&& !collabStudy.isCollaborativePaperCompleted(person)) {
                    try {
                        ScienceType collabScience = collabStudy.getContribution(person);

                        double collabResult = 25D;

                        // If researcher's current job isn't related to study science, divide by two.
                        JobType job = person.getMind().getJob();
                        if (job != null) {
                            ScienceType jobScience = ScienceType.getJobScience(job);
                            if (collabScience != jobScience)
                            	collabResult /= 2D;
                        }

                        result += collabResult;
                    }
                    catch (Exception e) {
                        logger.severe(person, "getProbability(): ", e);
                    }
                }
	        }

	        if (result > 0) {
	            if (person.isInVehicle()) {
	    	        // Check if person is in a moving rover.
	    	        if (Vehicle.inMovingRover(person)) {
	    	        	result += 20;
	    	        }
	    	        else
	    	        	result += 10;
	            }
	        }
	        else
	        	return 0;

	        // Crowding modifier
            Building b = BuildingManager.getAvailableBuilding(primaryStudy, person);
            result *= getBuildingModifier(b, person);

            result *= person.getAssociatedSettlement().getGoodsManager().getResearchFactor();

            result *= getPersonModifier(person);
        }

        return result;
    }
}
