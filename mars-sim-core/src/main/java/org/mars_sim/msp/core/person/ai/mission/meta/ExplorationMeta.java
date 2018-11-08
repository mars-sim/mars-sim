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
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.SpecimenContainer;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Exploration mission.
 */
public class ExplorationMeta implements MetaMission {

	/** Mission name */
	private static final String NAME = Msg.getString("Mission.description.exploration"); //$NON-NLS-1$

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

		double result = 0D;

		if (person.isInSettlement()) {

			Settlement settlement = person.getSettlement();

			int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
			int numThisMission = Simulation.instance().getMissionManager().numParticularMissions(NAME, settlement);
	
			// Check if a mission-capable rover is available.
			if (!RoverMission.areVehiclesAvailable(settlement, false)) {
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
			// Check if there are enough specimen containers at the settlement for
			// collecting rock samples.
			// boolean enoughContainers = false;
			// int numContainers =
			// settlement.getSettlementInventory().findNumEmptyUnitsOfClass(SpecimenContainer.class,
			// false);
			else if (!(settlement.getInventory().findNumEmptyUnitsOfClass(SpecimenContainer.class,
					false) >= Exploration.REQUIRED_SPECIMEN_CONTAINERS)) {
				return 0;
			}

			// Check for embarking missions.
			else if (settlement.getNumCitizens() / 4.0 < VehicleMission.numEmbarkingMissions(settlement)) {
				return 0;
			}

			// Check if settlement has enough basic resources for a rover mission.
			else if (!RoverMission.hasEnoughBasicResources(settlement, true)) {
				return 0;
			}

			// Check if starting settlement has minimum amount of methane fuel.
			// AmountResource methane = AmountResource.findAmountResource("methane");
			else if (!(settlement.getInventory().getAmountResourceStored(ResourceUtil.methaneID,
					false) < RoverMission.MIN_STARTING_SETTLEMENT_METHANE))
				return 0;

			// if (reservableRover && backupRover && minNum && enoughContainers &&
			// !embarkingMissions && hasBasicResources && enoughMethane) {
			try {
				// Get available rover.
				Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(settlement, false);
				if (rover != null) {
					// Check if any mineral locations within rover range.
					if (Exploration.hasNearbyMineralLocations(rover, settlement)) {
						result = 1D;
					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error determining mineral locations.", e);
			}

			// Crowding modifier
			int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
			if (crowding > 0)
				result *= (crowding + 1);

			int f1 = numEmbarked;
			int f2 = numThisMission;
			if (numEmbarked == 0)
				f1 = 1;
			if (numThisMission == 0)
				f2 = 1;
			
			result *= settlement.getNumCitizens() / 2.0 / f1 / f2;
			
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null)
				// It this town has a tourist objective, add bonus
				result *= job.getStartMissionProbabilityModifier(Exploration.class)
				* (settlement.getGoodsManager().getTourismFactor()
               		 + settlement.getGoodsManager().getResearchFactor())/1.5;
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