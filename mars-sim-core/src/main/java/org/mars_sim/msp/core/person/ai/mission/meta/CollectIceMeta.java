/**
 * Mars Simulation Project
 * CollectIceMeta.java
 * @version 3.1.0 2017-05-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A meta mission for the CollectIce mission.
 */
public class CollectIceMeta implements MetaMission {

	// private static Logger logger =
	// Logger.getLogger(CollectIceMeta.class.getName());

	/** Mission name */
	private static final String NAME = Msg.getString("Mission.description.collectIce"); //$NON-NLS-1$

	private static final int VALUE = 500;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Mission constructInstance(Person person) {
		return new CollectIce(person);
	}

	@Override
	public double getProbability(Person person) {

		double result = 0D;

		if (person.isInSettlement()) {

			Settlement settlement = person.getSettlement();

			int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
			int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(NAME, settlement);
			
			// a settlement with <= 4 population can always do DigLocalRegolith task
			// should avoid the risk of mission.
			if (settlement.getIndoorPeopleCount() <= 1)// .getAllAssociatedPeople().size() <= 4)
				return 0;

			// Check if available rover.
			else if (!RoverMission.areVehiclesAvailable(settlement, false)) {
				return 0;
			}

			// Check if available backup rover.
			else if (!RoverMission.hasBackupRover(settlement)) {
				return 0;
			}

			// Check if minimum number of people are available at the settlement.
			else if (!RoverMission.minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_STAYING_MEMBERS)) {
				return 0;
			}

			// Check if min number of EVA suits at settlement.
			else if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_GOING_MEMBERS) {
				return 0;
			}

			// Check if settlement has enough basic resources for a rover mission.
			else if (!RoverMission.hasEnoughBasicResources(settlement, false)) {
				return 0;
			}

//            // Check for embarking missions.
//            else if (VehicleMission.hasEmbarkingMissions(settlement)) {
//                return 0;
//            }

			// Check for embarking missions.
			else if (settlement.getNumCitizens() / 4.0 < numEmbarked) {
				return 0;
			}

			// Check if starting settlement has minimum amount of methane fuel.
			else if (settlement.getInventory().getAmountResourceStored(ResourceUtil.methaneID,
					false) < RoverMission.MIN_STARTING_SETTLEMENT_METHANE) {
				return 0;
			}

			else {
				result = settlement.getIceProbabilityValue() / VALUE;
			}


			if (result <= 0)
				return 0;
			
			// Crowding modifier.
			int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
			if (crowding > 0) {
				result *= (crowding + 1);
			}

			int f1 = numEmbarked;
			int f2 = numThisMission;
			if (numEmbarked == 0)
				f1 = 1;
			if (numThisMission == 0)
				f2 = 1;
			
			result *= settlement.getNumCitizens() / 2.0 / f1 / f2;
			
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) {
				result *= job.getStartMissionProbabilityModifier(CollectIce.class);
			}

			
			// logger.info("CollectIceMeta's probability : " +
			// Math.round(result*100D)/100D);

			if (result > 1D)
				result = 1D;
			else if (result < 0.5)
				result = 0;
		}

		return result;
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