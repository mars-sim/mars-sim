/*
 * Mars Simulation Project
 * ObserveAstronomicalObjectsMeta.java
 * @date 2022-08-06
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.AstronomicalObservation;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Meta task for the ObserveAstronomicalObjects task.
 */
public class ObserveAstronomicalObjectsMeta extends MetaTask implements SettlementMetaTask {
     /**
     * Represents a Job needed to Observer Astronomical objects for a science study
     */
    private static class AstronomicalTaskJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        public AstronomicalTaskJob(SettlementMetaTask owner, ScientificStudy s, RatingScore score) {
            super(owner, "Astronomy Observations", s, score);
        }

        @Override
        public Task createTask(Person person) {
            return new ObserveAstronomicalObjects(person, (ScientificStudy) getFocus());
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.observeAstronomicalObjects"); //$NON-NLS-1$
    private static ScientificStudyManager ssm;

    public ObserveAstronomicalObjectsMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		
		setFavorite(FavoriteType.ASTRONOMY, FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC, TaskTrait.ARTISTIC);
		setPreferredJob(JobType.ASTRONOMER);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST,
				RoleType.CREW_SCIENTIST, RoleType.CREW_ENGINEER);
	}
    
    /**
     * Gets task for any Scientific study that needs Astronomy observation time.
     * Assessment is based on it getting dark and available Observatory.
     * 
     * @param target Settlement being checked
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement target) {
        List<SettlementTask> result = new ArrayList<>();
        if (ObserveAstronomicalObjects.areConditionsSuitable(target)
                && !target.getBuildingManager().getBuildingSet(FunctionType.ASTRONOMICAL_OBSERVATION).isEmpty()) {    
            // Any Astro based study active at this Settlement
            for (ScientificStudy s : getAstroStudies(target)) {
            	// Suitable study so create tasks for each Observatory
                RatingScore score = new RatingScore(100);
                score = applyCommerceFactor(score, target, CommerceType.TOURISM);
                result.add(new AstronomicalTaskJob(this, s, score));
            }      
        }
        return result;
    }

	/**
	 * Gets a list of astronomy studies a settlement is primary for.
	 * Note: Either primary science type or has a collaboration
     * of Astronomy from at least one collaborator.
	 * 
	 * @param settlement the settlement.
	 * @return list of scientific studies.
	 */
	public List<ScientificStudy> getAstroStudies(Settlement settlement) {
		return ssm.getAllStudies(settlement).stream().filter(s -> 
				(StudyStatus.RESEARCH_PHASE == s.getPhase())
	            && ((ScienceType.ASTRONOMY == s.getScience())
	                || s.getCollaborationScience().contains(ScienceType.ASTRONOMY))
						&& (s.getPrimarySettlement() != null) 
						&& s.getPrimarySettlement().equals(settlement))
				.collect(Collectors.toList());		
	}
	
    /**
     * Assesses the suitability of a Person do to an Observation task. Based largely on the Study
     * the person is performing.
     * 
     * @param st Task on offer
     * @param p Person being assessed
     */
    @Override
    public RatingScore assessPersonSuitability(SettlementTask st, Person p) {
        if (!p.isInSettlement()
            || !p.getPhysicalCondition().isFitByLevel(500, 50, 500)) {
            return RatingScore.ZERO_RATING;
        }

        // Check these is a Observatory usable
        var observatory = determineObservatory(p.getSettlement());
        if (observatory == null) {
            return RatingScore.ZERO_RATING;
        }

        double researchModifier = 0D;

        // Add probability for researcher's primary study (if any).
        ScientificStudy s = (ScientificStudy) st.getFocus();
        if (s.equals(p.getResearchStudy().getStudy())) {
            // Lead researcher and it's astronomy
            if ((ScienceType.ASTRONOMY == s.getScience())
                && !s.isPrimaryResearchCompleted()) {
                researchModifier = 1.3D;
            }
        }
        // Add probability for each study researcher is collaborating on.
        else if ((ScienceType.ASTRONOMY == s.getContribution(p))
            && !s.isCollaborativeResearchCompleted(p)) {
            researchModifier = 1.1D;
        }

        // Can person contribute
        if (researchModifier == 0D) {
            return RatingScore.ZERO_RATING;
        }

        RatingScore result = super.assessPersonSuitability(st, p);
        result.addModifier("research", researchModifier);

        // If researcher's current job isn't related to astronomy, divide by two.
        JobType job = p.getMind().getJob();
        if (job != null) {
            ScienceType jobScience = ScienceType.getJobScience(job);
            if (ScienceType.ASTRONOMY != jobScience) {
                result.addModifier("science", 1.2D);
            }
        }

        result = assessBuildingSuitability(result, observatory.getBuilding(), p);

        return result;
    }

    /**
	 * Gets the preferred local astronomical observatory for an observer.
	 * 
	 * @param observer the observer.
	 * @return observatory or null if none found.
	 */
	public static AstronomicalObservation determineObservatory(Settlement target) {

		BuildingManager manager = target.getBuildingManager();
		Set<Building> observatoryBuildings = manager.getBuildingSet(FunctionType.ASTRONOMICAL_OBSERVATION);
		observatoryBuildings = BuildingManager.getNonMalfunctioningBuildings(observatoryBuildings);

		if (observatoryBuildings == null || observatoryBuildings.isEmpty()) {
			return null;
		}
		
		Building selected = RandomUtil.getARandSet(observatoryBuildings);
		if (selected == null) {
			return null;
		}
		return selected.getAstronomicalObservation();
	}

    public static void initialiseInstances(ScientificStudyManager scientificStudyManager) {
        ssm = scientificStudyManager;
    }
}
