/**
 * Mars Simulation Project
 * RescueSalvageVehicleMeta.java
 * @version 3.1.0 2017-04-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A meta mission for the RescueSalvageVehicle mission.
 */
public class RescueSalvageVehicleMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.rescueSalvageVehicle"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new RescueSalvageVehicle(person);
    }

    @Override
    public double getProbability(Person person) {

        double missionProbability = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check if mission is possible for person based on their circumstance.
            //boolean missionPossible = true;

            Settlement settlement = person.getSettlement();

            Vehicle vehicleTarget = null;

            boolean rescuePeople = false;

            // Check if there are any beacon vehicles within range that need help.
            try {
                Vehicle vehicle = RoverMission.getVehicleWithGreatestRange(settlement, true);
                if (vehicle != null) {
                    vehicleTarget = RescueSalvageVehicle.findAvailableBeaconVehicle(settlement,
                            vehicle.getRange());
                    if (vehicleTarget == null) {
                        return 0;
                    }
                    else if (!RescueSalvageVehicle.isClosestCapableSettlement(settlement, vehicleTarget)) {
                        return 0;
                    }
                }
            }
            catch (Exception e) {
    			e.printStackTrace();
    			return 0;
            }

            // Check if available rover.
            if (!RoverMission.areVehiclesAvailable(settlement, true)) {
                return 0;
            }

            // Check if min number of EVA suits at settlement.
            else if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) <
                    RescueSalvageVehicle.MIN_GOING_MEMBERS) {
                return 0;
            }

            // Check for embarking missions.
            else if (VehicleMission.hasEmbarkingMissions(settlement)) {
                return 0;
            }

            // Check if person is last remaining person at settlement (for salvage mission but not rescue mission).
            // Also check for backup rover for salvage mission.
            //boolean rescue = false;
            else if (vehicleTarget != null) {
                rescuePeople = (RescueSalvageVehicle.getRescuePeopleNum(vehicleTarget) > 0);
                if (rescuePeople) {
                    //if (!atLeastOnePersonRemainingAtSettlement(settlement, person))
                    //	return 0;
                	//if (!RoverMission.minAvailablePeopleAtSettlement(settlement, 0))
                    //    return 0;
                }
                else {
                    // Check if minimum number of people are available at the settlement.
                    if (!RoverMission.minAvailablePeopleAtSettlement(settlement,
                            (RescueSalvageVehicle.MIN_STAYING_MEMBERS))) {
                        return 0;
                    }

                    // Check if available backup rover.
                    else if (!RoverMission.hasBackupRover(settlement)) {
                        return 0;
                    }
                }
            }

            // Determine mission probability.
            if (rescuePeople) {
                missionProbability = RescueSalvageVehicle.BASE_RESCUE_MISSION_WEIGHT;
            }
            else {
                missionProbability = RescueSalvageVehicle.BASE_SALVAGE_MISSION_WEIGHT;
            }

            // Crowding modifier.
            int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
            if (crowding > 0) {
                missionProbability *= (crowding + 1);
            }

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                missionProbability *= job.getStartMissionProbabilityModifier(RescueSalvageVehicle.class);
            }

        }

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