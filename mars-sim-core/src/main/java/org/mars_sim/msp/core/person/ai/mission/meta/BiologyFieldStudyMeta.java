/**
 * Mars Simulation Project
 * BiologyFieldStudyMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the BiologyFieldStudy.
 */
public class BiologyFieldStudyMeta implements MetaMission {

    /** default logger. */
	private static Logger logger = Logger.getLogger(BiologyFieldStudyMeta.class.getName());

    private static final double WEIGHT = 4D;
    
    /** Mission name */
    private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.biologyFieldStudy"); //$NON-NLS-1$

    @Override
    public String getName() {
        return DEFAULT_DESCRIPTION;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new BiologyFieldStudy(person);
    }

    @Override
    public double getProbability(Person person) {

    	if (BiologyFieldStudy.determineStudy(person) == null)
			return 0;
    	
        double missionProbability = 0D;

        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();

            RoleType roleType = person.getRole().getType();
			
			if (person.getMind().getJob() == JobType.BIOLOGIST
					|| RoleType.MISSION_SPECIALIST == roleType
					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
					|| RoleType.CHIEF_OF_SCIENCE == roleType
					|| RoleType.SCIENCE_SPECIALIST == roleType
					|| RoleType.COMMANDER == roleType
					|| RoleType.SUB_COMMANDER == roleType
					) {

				if (settlement.getMissionBaseProbability(DEFAULT_DESCRIPTION))
	            	missionProbability = 1;
	            else
	    			return 0;
	       		
				int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
				int numThisMission = missionManager.numParticularMissions(DEFAULT_DESCRIPTION, settlement);
	
		   		// Check for # of embarking missions.
	    		if (Math.max(1, settlement.getNumCitizens() / 4.0) < numEmbarked + numThisMission) {
	    			return 0;
	    		}
	    		
	            try {
		            // Get available rover.
		            Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(BiologyFieldStudy.missionType, settlement, false);
		            if (rover != null) {
		
		                ScienceType biology = ScienceType.BIOLOGY;
		
		                // Add probability for researcher's primary study (if any).
		                ScientificStudy primaryStudy = person.getStudy();
		                if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
		                    if (!primaryStudy.isPrimaryResearchCompleted()) {
		                        if (biology == primaryStudy.getScience()) {
		                            missionProbability += WEIGHT;
		                        }
		                    }
		                }
		
		                // Add probability for each study researcher is collaborating on.
		                Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
		                while (i.hasNext()) {
		                    ScientificStudy collabStudy = i.next();
		                    if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
		                        if (!collabStudy.isCollaborativeResearchCompleted(person)) {
		                            if (biology == collabStudy.getContribution(person)) {
		                                missionProbability += WEIGHT/2D;
		                            }
		                        }
		                    }
		                }
		            }
	            }
	            catch (Exception e) {
	                logger.log(Level.SEVERE, "Error determining rover.", e);
	                return 0;
	            }
	
	            int f1 = 2*numEmbarked + 1;
				int f2 = 2*numThisMission + 1;
				
				missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D * ( 1 + settlement.getMissionDirectiveModifier(1));
				
	            // Crowding modifier
	            int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
	            if (crowding > 0) missionProbability *= (crowding + 1);
	
	            // Job modifier.
	            JobType job = person.getMind().getJob();
	            if (job != null) {
	            	// If this town has a tourist objective, add bonus
	                missionProbability *= JobUtil.getStartMissionProbabilityModifier(job, BiologyFieldStudy.class) 
	                	* (settlement.getGoodsManager().getTourismFactor()
	                	+ settlement.getGoodsManager().getResearchFactor())/1.5;
	            }
	            
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

//        if (missionProbability > 0)
//        	logger.info("BiologyStudyFieldMissionMeta's probability : " +
//				 Math.round(missionProbability*100D)/100D);

        return missionProbability;
    }

	@Override
	public Mission constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}
