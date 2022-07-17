/*
 * Mars Simulation Project
 * FieldStudyMeta.java
 * @date 2022-07-14
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.FieldStudyMission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

public class FieldStudyMeta extends AbstractMetaMission {

    /** default logger. */
    private static final Logger logger = Logger.getLogger(FieldStudyMeta.class.getName());

    private static final double WEIGHT = 10D;
	private ScienceType science;

	public FieldStudyMeta(MissionType type, String nameKey, Set<JobType> preferredLeaderJob,
			ScienceType science) {
		super(type, nameKey, preferredLeaderJob);
		this.science = science;
	}

	@Override
	public double getProbability(Person person) {

		if (FieldStudyMission.determineStudy(science, person) == null) {
			return 0;
		}

	    double missionProbability = 0D;
	    MissionType mType = getType();

	    if (person.isInSettlement()) {
	    	Settlement settlement = person.getSettlement();

	        RoleType roleType = person.getRole().getType();
			JobType jobType = person.getMind().getJob();

			if (getPreferredLeaderJob().contains(jobType)
					|| RoleType.CHIEF_OF_SCIENCE == roleType
					|| RoleType.MISSION_SPECIALIST == roleType
					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
					|| RoleType.SCIENCE_SPECIALIST == roleType
					|| RoleType.COMMANDER == roleType
					|| RoleType.SUB_COMMANDER == roleType
					) {

				missionProbability = 1D;
				
				int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
				int numThisMission = missionManager.numParticularMissions(mType, settlement);
				
		   		// Check for # of embarking missions.
	    		if (Math.max(1, settlement.getNumCitizens() / 4.0) < numEmbarked + numThisMission) {
	    			return 0;
	    		}

	            try {
	                // Get available rover.
	                Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(mType, settlement, false);
	                if (rover != null) {

	                    // Add probability for researcher's primary study (if any).
	                    ScientificStudy primaryStudy = person.getStudy();
	                    if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
	                    		&& !primaryStudy.isPrimaryResearchCompleted()
	                            && (science == primaryStudy.getScience())) {
	                    	missionProbability += WEIGHT;
	                    }

	                    // Add probability for each study researcher is collaborating on.
	                    Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
	                    while (i.hasNext()) {
	                        ScientificStudy collabStudy = i.next();
	                        if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
	                        		&& !collabStudy.isCollaborativeResearchCompleted(person)
	                        		&& (science == collabStudy.getContribution(person))) {
	                        	missionProbability += WEIGHT/2D;
	                        }
	                    }
	                }
	            }
	            catch (Exception e) {
	                logger.log(Level.SEVERE, "Error determining rover gpt mission " + mType.getName(), e);
	                return 0;
	            }

				int f1 = numEmbarked + 1;
				int f2 = numThisMission + 1;

				missionProbability *= 2.0 * settlement.getNumCitizens() / f1 / f2;

	            // Crowding modifier
	            int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
	            if (crowding > 0) missionProbability *= (crowding + 1);

	            // Job modifier.
	            missionProbability *= getLeaderSuitability(person)
	                	* (settlement.getGoodsManager().getTourismFactor()
	                    + settlement.getGoodsManager().getResearchFactor())/1.5;

				// if introvert, score  0 to  50 --> -2 to 0
				// if extrovert, score 50 to 100 -->  0 to 2
				// Reduce probability if introvert
				int extrovert = person.getExtrovertmodifier();
				missionProbability += extrovert;

				if (missionProbability > LIMIT)
					missionProbability = LIMIT;

				if (missionProbability < 0)
					missionProbability = 0;
	        }
	    }

	    return missionProbability;
	}
}