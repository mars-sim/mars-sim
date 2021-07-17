/**
 * Mars Simulation Project
 * MeteorologyFieldStudyMeta.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.MeteorologyFieldStudy;
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
 * A meta mission for the MeteorologyFieldStudy.
 */
public class MeteorologyFieldStudyMeta implements MetaMission {

    /** Mission name */
    private static final String DEFAULT_DESCRIPTION = Msg.getString(
            "Mission.description.meteorologyFieldStudy"); //$NON-NLS-1$

    private static final double WEIGHT = 10D;
       
    /** default logger. */
    private static Logger logger = Logger.getLogger(MeteorologyFieldStudyMeta.class.getName());

    @Override
    public String getName() {
        return DEFAULT_DESCRIPTION;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new MeteorologyFieldStudy(person);
    }

    @Override
    public double getProbability(Person person) {

    	if (MeteorologyFieldStudy.determineStudy(person) == null)
			return 0;
    	
        double missionProbability = 0D;

        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();

            RoleType roleType = person.getRole().getType();
			
 			if (person.getMind().getJob() == JobType.METEOROLOGIST
 					|| RoleType.CHIEF_OF_SCIENCE == roleType
 					|| RoleType.SCIENCE_SPECIALIST == roleType
 					|| RoleType.MISSION_SPECIALIST == roleType
 					|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType	
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
	    		
	    		if (numThisMission > 1)
	    			return 0;	
		
	            try {
	                // Get available rover.
	                Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(MeteorologyFieldStudy.missionType, settlement, false);
	                if (rover != null) {
	
	                    ScienceType meteorology = ScienceType.METEOROLOGY;
	
	                    // Add probability for researcher's primary study (if any).
	                    ScientificStudy primaryStudy = person.getStudy();
	                    if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
	                        if (!primaryStudy.isPrimaryResearchCompleted()) {
	                            if (meteorology == primaryStudy.getScience()) {
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
	                                if (meteorology == collabStudy.getContribution(person)) {
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
	
				int f1 = numEmbarked + 1;
				int f2 = numThisMission + 1;
				
				missionProbability *= settlement.getNumCitizens() / f1 / f2 * ( 1 + settlement.getMissionDirectiveModifier(7));
				
	            // Crowding modifier
	            int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
	            if (crowding > 0) missionProbability *= (crowding + 1);
	
	            // Job modifier.
	            JobType job = person.getMind().getJob();
	            if (job != null) {
	            	// If this town has a tourist objective, add bonus
	                missionProbability *= JobUtil.getStartMissionProbabilityModifier(job, MeteorologyFieldStudy.class) 
	                	* (settlement.getGoodsManager().getTourismFactor()
	                    + settlement.getGoodsManager().getResearchFactor())/1.5;
	                
	                if (missionProbability > LIMIT)
	        			missionProbability = LIMIT;
	        		
	        		// if introvert, score  0 to  50 --> -2 to 0
	        		// if extrovert, score 50 to 100 -->  0 to 2
	        		// Reduce probability if introvert
	        		int extrovert = person.getExtrovertmodifier();
	        		missionProbability += extrovert;	
	        		
	        		if (missionProbability < 0)
	        			missionProbability = 0;		
	            }	
	        }
        }
		
        if (missionProbability > 0)
        	logger.info("MeteorologyStudyFieldMissionMeta's probability : " +
				 Math.round(missionProbability*100D)/100D);
		
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
