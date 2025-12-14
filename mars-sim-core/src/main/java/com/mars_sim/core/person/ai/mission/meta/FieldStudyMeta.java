/*
 * Mars Simulation Project
 * FieldStudyMeta.java
 * @date 2025-07-06
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.mission.meta;

import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.FieldStudyMission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.RoverMission;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;

public class FieldStudyMeta extends AbstractMetaMission {

	private static final Set<RoleType> PREFERRED_ROLES = Set.of(RoleType.CREW_SCIENTIST,
							RoleType.CHIEF_OF_LOGISTIC_OPERATION, RoleType.LOGISTIC_SPECIALIST,
							RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST,
							RoleType.CHIEF_OF_SUPPLY_RESOURCE, RoleType.RESOURCE_SPECIALIST,
							RoleType.CHIEF_OF_MISSION_PLANNING, RoleType.MISSION_SPECIALIST, 
							RoleType.COMMANDER, RoleType.SUB_COMMANDER);

	private static final String PRI_STUDY_BASE = "study.primary";
	private static final String COL_STUDY_BASE = "study.collaborative";
	private static final String PROGRESS_BASE = "progress";
	private static final int WEIGHT = 5;
	private static final int BASE_SCORE = 10;
	private ScienceType scienceType;

	public FieldStudyMeta(MissionType type, Set<JobType> preferredLeaderJob,
			ScienceType scienceType) {
		super(type, preferredLeaderJob);
		this.scienceType = scienceType;
	}

	@Override
	public RatingScore getProbability(Person person) {

		// Check if mission is possible for person based on their circumstance.
		Settlement settlement = person.getAssociatedSettlement();
		
		if (settlement.isFirstSol()) return RatingScore.ZERO_RATING;
		
		RoleType roleType = person.getRole().getType();
		JobType jobType = person.getMind().getJobType();

		if (FieldStudyMission.determineStudy(scienceType, person) == null
			|| !person.isInSettlement()
			|| (!getPreferredLeaderJob().contains(jobType)
			&& !PREFERRED_ROLES.contains(roleType))) {
			return RatingScore.ZERO_RATING;
		}
		
		// Get available rover.
		Rover rover = RoverMission.getVehicleWithGreatestRange(settlement, false);
		
		if (rover == null) {
			return RatingScore.ZERO_RATING;
		}

		double newBase = BASE_SCORE;
		
		RatingScore missionProbability = null;
		
		// Add probability for researcher's primary study (if any).
		ScientificStudy primaryStudy = person.getResearchStudy().getStudy();
		
		if (primaryStudy != null) {
			boolean isOngoing = (StudyStatus.PROPOSAL_PHASE == primaryStudy.getPhase()
					|| StudyStatus.INVITATION_PHASE == primaryStudy.getPhase()
					|| 	StudyStatus.RESEARCH_PHASE == primaryStudy.getPhase()
					|| StudyStatus.PAPER_PHASE == primaryStudy.getPhase());
			
			if (isOngoing
					&& !primaryStudy.isPrimaryResearchCompleted()
					&& (scienceType == primaryStudy.getScience())) {
				newBase += WEIGHT;
			}
			
			missionProbability = new RatingScore(PRI_STUDY_BASE, newBase);
			
			if (StudyStatus.INVITATION_PHASE == primaryStudy.getPhase())
				missionProbability.addModifier(PROGRESS_BASE, 1.5);
			else if (StudyStatus.RESEARCH_PHASE == primaryStudy.getPhase())
				missionProbability.addModifier(PROGRESS_BASE, 2);
			else if (StudyStatus.PAPER_PHASE == primaryStudy.getPhase())
				missionProbability.addModifier(PROGRESS_BASE, 1.5);
		}

		newBase = BASE_SCORE;
		
		// Add probability for each study researcher is collaborating on.
		for (ScientificStudy collabStudy : person.getResearchStudy().getCollabStudies()) {
			
			if (collabStudy != null) {

				boolean isOngoing = (StudyStatus.PROPOSAL_PHASE == collabStudy.getPhase()
						|| StudyStatus.INVITATION_PHASE == collabStudy.getPhase()
						|| 	StudyStatus.RESEARCH_PHASE == collabStudy.getPhase()
						|| StudyStatus.PAPER_PHASE == collabStudy.getPhase());
			
				if (isOngoing
						&& !collabStudy.isCollaborativeResearchCompleted(person)
						&& (scienceType == collabStudy.getContribution(person))) {
					newBase += WEIGHT/2D;
				}
			}
			
			if (missionProbability == null)
				missionProbability = new RatingScore(COL_STUDY_BASE, newBase);
			else
				missionProbability.addBase(COL_STUDY_BASE, newBase);
						
			if (StudyStatus.INVITATION_PHASE == primaryStudy.getPhase())
				missionProbability.addModifier(PROGRESS_BASE, 1.5);
			else if (StudyStatus.RESEARCH_PHASE == primaryStudy.getPhase())
				missionProbability.addModifier(PROGRESS_BASE, 2);
			else if (StudyStatus.PAPER_PHASE == primaryStudy.getPhase())
				missionProbability.addModifier(PROGRESS_BASE, 1.5);
		}

		if (missionProbability != null) {
			// Crowding modifier
			int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
			if (crowding > 0) missionProbability.addModifier(OVER_CROWDING, crowding + 1D);
	
			// Job modifier.
			missionProbability.addModifier(LEADER, getLeaderSuitability(person));
			missionProbability = applyCommerceAverage(missionProbability, settlement, CommerceType.TOURISM,
														CommerceType.RESEARCH);
	
			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
			// Reduce probability if introvert
			int extrovert = person.getExtrovertmodifier();
			missionProbability.addModifier(PERSON_EXTROVERT, (1D + extrovert/2.0));
	
			missionProbability.applyRange(0, LIMIT);
		}

		if (missionProbability == null) {
			return RatingScore.ZERO_RATING;
		}
		
	    return missionProbability;
	}
}