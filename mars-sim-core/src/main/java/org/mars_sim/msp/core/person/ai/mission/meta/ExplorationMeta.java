/**
 * Mars Simulation Project
 * ExplorationMeta.java
 * @version 3.1.0 2017-05-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Exploration mission.
 */
public class ExplorationMeta implements MetaMission {

	/** Mission name */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.exploration"); //$NON-NLS-1$

	private static final double VALUE = 500D;

    private static final double LIMIT = 10D;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ExplorationMeta.class.getName());

	@Override
	public String getName() {
		return DEFAULT_DESCRIPTION;
	}

	@Override
	public Mission constructInstance(Person person) {
		return new Exploration(person);
	}

	@Override
	public double getProbability(Person person) {

		double missionProbability = 0D;
		
		if (person.isInSettlement()) {

			Settlement settlement = person.getSettlement();
			
			// 1. Check if there are enough specimen containers at the settlement for
			// collecting rock samples.
			if (settlement.getInventory().findNumSpecimenBoxes(true, true) < Exploration.REQUIRED_SPECIMEN_CONTAINERS) {
				return 0;
			}
			
			missionProbability = settlement.getMissionBaseProbability(DEFAULT_DESCRIPTION);
	   		if (missionProbability <= 0)
    			return 0;
	   		
			int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
			int numThisMission = missionManager.numParticularMissions(DEFAULT_DESCRIPTION, settlement);
	
//			if (numThisMission > 1)	System.out.println(settlement + "  " + NAME + "'s numThisMission : " + numThisMission);
//			if (numEmbarked > 1) System.out.println(settlement + "  " + NAME + "'s numEmbarked : " + numEmbarked);
			
	   		// Check for # of embarking missions.
    		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
    			return 0;
    		}	
    		
    		if (numThisMission > 1)
    			return 0;	
    		
    		missionProbability = 0;

			try {
				// Get available rover.
				Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(Exploration.missionType, settlement, false);
				if (rover != null) {
					// Check if any mineral locations within rover range and obtain their concentration
					missionProbability = settlement.getTotalMineralValue(rover) / VALUE;
					if (missionProbability < 0)
						missionProbability = 0;
				}
				
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error determining mineral locations.", e);
			}

			int f1 = 2*numEmbarked + 1;
			int f2 = 2*numThisMission + 1;
			
			missionProbability *= settlement.getNumCitizens() / f1 / f2 * ( 1 + settlement.getMissionDirectiveModifier(4));
			
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null)
				// It this town has a tourist objective, add bonus
				missionProbability *= job.getStartMissionProbabilityModifier(Exploration.class)
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

//        if (missionProbability > 0)
//        	logger.info("ExplorationMeta's probability : " +
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