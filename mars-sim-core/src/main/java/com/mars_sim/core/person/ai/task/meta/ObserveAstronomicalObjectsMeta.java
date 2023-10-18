/*
 * Mars Simulation Project
 * ObserveAstronomicalObjectsMeta.java
 * @date 2022-08-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.logging.Logger;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.ObserveAstronomicalObjects;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.building.function.AstronomicalObservation;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the ObserveAstronomicalObjects task.
 */
public class ObserveAstronomicalObjectsMeta extends FactoryMetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.observeAstronomicalObjects"); //$NON-NLS-1$

    /** default logger. */
    private static final Logger logger = Logger.getLogger(ObserveAstronomicalObjectsMeta.class.getName());

    public ObserveAstronomicalObjectsMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		
		setFavorite(FavoriteType.ASTRONOMY, FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC, TaskTrait.ARTISTIC);
		setPreferredJob(JobType.ASTRONOMER);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST);
	}
    
    @Override
    public Task constructInstance(Person person) {
        return new ObserveAstronomicalObjects(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // Get local observatory if available.
        AstronomicalObservation observatory = ObserveAstronomicalObjects.determineObservatory(person);
        
        if (null != observatory && person.isInSettlement()) {

            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(500, 50, 500))
            	return 0;
            
            // Check if it is getting dark outside.
            if (EVAOperation.isGettingDark(person)) {

                ScienceType astronomy = ScienceType.ASTRONOMY;

                // Add probability for researcher's primary study (if any).
                ScientificStudy primaryStudy = person.getStudy();
                if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(
                        primaryStudy.getPhase())) {
                    if (!primaryStudy.isPrimaryResearchCompleted() &&
                            astronomy == primaryStudy.getScience()) {
                        try {
                            double primaryResult = 100D;

                            // Get observatory building crowding modifier.
                            primaryResult *= ObserveAstronomicalObjects.getObservatoryCrowdingModifier(person, observatory);

                            // If researcher's current job isn't related to astronomy, divide by two.
                            JobType job = person.getMind().getJob();
                            if (job != null) {
                                ScienceType jobScience = ScienceType.getJobScience(job);
                                if (astronomy != jobScience) {
                                    primaryResult /= 2D;
                                }
                            }

                            result += primaryResult;
                        }
                        catch (Exception e) {
                            logger.severe("getProbability(): " + e.getMessage());
                        }
                    }
                }

                // Add probability for each study researcher is collaborating on.
                Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
                while (i.hasNext()) {
                    ScientificStudy collabStudy = i.next();
                    if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
                        if (!collabStudy.isCollaborativeResearchCompleted(person)) {
                            if (astronomy == collabStudy.getContribution(person)) {
                                try {
                                    double collabResult = 50D;


                                    // Get observatory building crowding modifier.
                                    collabResult *= ObserveAstronomicalObjects.getObservatoryCrowdingModifier(person, observatory);

                                    // If researcher's current job isn't related to astronomy, divide by two.
                                    JobType job = person.getMind().getJob();
                                    if (job != null) {
                                        ScienceType jobScience = ScienceType.getJobScience(job);
                                        if (astronomy != jobScience) {
                                            collabResult /= 2D;
                                        }
                                    }

                                    result += collabResult;
                                }
                                catch (Exception e) {
                                    logger.severe("getProbability(): " + e.getMessage());
                                }
                            }
                        }
                    }
                }

                if (result <= 0) return 0;
                result *= (person.getAssociatedSettlement().getGoodsManager().getTourismFactor()
	               		 + person.getAssociatedSettlement().getGoodsManager().getResearchFactor())/1.5;
                
                result *= getPersonModifier(person);
            }
        }

        return result;
    }
}
