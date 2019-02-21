/**
 * Mars Simulation Project
 * BiologyStudyFieldMissionMeta.java
 * @version 3.1.0 2017-05-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.BiologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the BiologyStudyFieldMission mission.
 */
public class BiologyStudyFieldMissionMeta implements MetaMission {

    /** default logger. */
	private static Logger logger = Logger.getLogger(BiologyStudyFieldMissionMeta.class.getName());

    private static final double WEIGHT = 4D;
    
    private static final double LIMIT = 10D;
    
    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.biologyStudyFieldMission"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new BiologyStudyFieldMission(person);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;

        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();

            missionProbability = settlement.getMissionBaseProbability();
       		if (missionProbability == 0)
    			return 0;
       		
			int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
			int numThisMission = missionManager.numParticularMissions(NAME, settlement);

	   		// Check for # of embarking missions.
    		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
    			return 0;
    		}	
    		
    		missionProbability = 0;
    		
            try {
	            // Get available rover.
	            Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(settlement, false);
	            if (rover != null) {
	
	                ScienceType biology = ScienceType.BIOLOGY;
	
	                // Add probability for researcher's primary study (if any).
	                ScientificStudy primaryStudy = studyManager.getOngoingPrimaryStudy(person);
	                if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
	                    if (!primaryStudy.isPrimaryResearchCompleted()) {
	                        if (biology == primaryStudy.getScience()) {
	                            missionProbability += WEIGHT;
	                        }
	                    }
	                }
	
	                // Add probability for each study researcher is collaborating on.
	                Iterator<ScientificStudy> i = studyManager.getOngoingCollaborativeStudies(person).iterator();
	                while (i.hasNext()) {
	                    ScientificStudy collabStudy = i.next();
	                    if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
	                        if (!collabStudy.isCollaborativeResearchCompleted(person)) {
	                            if (biology == collabStudy.getCollaborativeResearchers().get(person.getIdentifier())) {
	                                missionProbability += WEIGHT/2D;
	                            }
	                        }
	                    }
	                }
	            }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error determining rover.", e);
            }

            int f1 = 2*numEmbarked + 1;
			int f2 = 2*numThisMission + 1;
			
			missionProbability *= settlement.getNumCitizens() / f1 / f2 / 2D;
			
            // Crowding modifier
            int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
            if (crowding > 0) missionProbability *= (crowding + 1);

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
            	// If this town has a tourist objective, add bonus
                missionProbability *= job.getStartMissionProbabilityModifier(BiologyStudyFieldMission.class) 
                	* (settlement.getGoodsManager().getTourismFactor()
                	+ settlement.getGoodsManager().getResearchFactor())/1.5;
            }
            
			if (missionProbability > LIMIT)
				missionProbability = LIMIT;
			else if (missionProbability < 0)
				missionProbability = 0;
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