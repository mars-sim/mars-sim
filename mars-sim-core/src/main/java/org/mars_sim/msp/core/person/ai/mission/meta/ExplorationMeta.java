/**
 * Mars Simulation Project
 * ExplorationMeta.java
 * @version 3.1.0 2017-05-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.SpecimenContainer;
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
	private static final String NAME = Msg.getString("Mission.description.exploration"); //$NON-NLS-1$

	private static final double FACTOR = 50D;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ExplorationMeta.class.getName());

	@Override
	public String getName() {
		return NAME;
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
			if (settlement.getInventory().findNumEmptyUnitsOfClass(SpecimenContainer.class,
					false) < Exploration.REQUIRED_SPECIMEN_CONTAINERS) {
				return 0;
			}
			
			missionProbability = settlement.getMissionBaseProbability();
	   		if (missionProbability == 0)
    			return 0;
	   		
			int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
			int numThisMission = missionManager.numParticularMissions(NAME, settlement);
	
	   		// Check for # of embarking missions.
    		if (Math.max(1, settlement.getNumCitizens() / 8.0) < numEmbarked + numThisMission) {
    			return 0;
    		}	
    		
    		else if (numThisMission > 1)
    			return 0;	
    		
    		missionProbability = 0;

			try {
				// Get available rover.
				Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(settlement, false);
				if (rover != null) {
					// Check if any mineral locations within rover range and obtain their concentration
					missionProbability = settlement.getTotalMineralValue(rover) / FACTOR;
					if (missionProbability < 0)
						missionProbability = 0;
				}
				
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error determining mineral locations.", e);
			}

			int f1 = 2*numEmbarked + 1;
			int f2 = 2*numThisMission + 1;
			
			missionProbability *= settlement.getNumCitizens() / f1 / f2;
			
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null)
				// It this town has a tourist objective, add bonus
				missionProbability *= job.getStartMissionProbabilityModifier(Exploration.class)
					* (settlement.getGoodsManager().getTourismFactor()
               		 + settlement.getGoodsManager().getResearchFactor())/1.5;
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