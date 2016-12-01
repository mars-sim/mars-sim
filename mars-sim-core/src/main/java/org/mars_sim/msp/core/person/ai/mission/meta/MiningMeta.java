/**
 * Mars Simulation Project
 * MiningMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Mining mission.
 */
public class MiningMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.mining"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(MiningMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mission constructInstance(Person person) {
        return new Mining(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();

            // Check if a mission-capable rover is available.
            boolean reservableRover = RoverMission.areVehiclesAvailable(
                    settlement, false);

            // Check if available backup rover.
            boolean backupRover = RoverMission.hasBackupRover(settlement);

            // Check if minimum number of people are available at the settlement.
            // Plus one to hold down the fort.
            boolean minNum = RoverMission.minAvailablePeopleAtSettlement(
                    settlement, (RoverMission.MIN_PEOPLE + 1));

            // Check if there are enough bags at the settlement for collecting minerals.
            boolean enoughBags = false;

            int numBags = settlement.getInventory().findNumEmptyUnitsOfClass(
                    Bag.class, false);
            enoughBags = (numBags >= Mining.NUMBER_OF_BAGS);

            // Check for embarking missions.
            boolean embarkingMissions = VehicleMission
                    .hasEmbarkingMissions(settlement);

            // Check if settlement has enough basic resources for a rover mission.
            boolean hasBasicResources = RoverMission
                    .hasEnoughBasicResources(settlement);

            // Check if available light utility vehicles.
            boolean reservableLUV = Mining.isLUVAvailable(settlement);

            // Check if LUV attachment parts available.
            boolean availableAttachmentParts = Mining.areAvailableAttachmentParts(settlement);

            // Check if starting settlement has minimum amount of methane fuel.
            AmountResource methane = AmountResource.findAmountResource("methane");
            boolean enoughMethane = settlement.getInventory().getAmountResourceStored(methane, false) >= 
                    RoverMission.MIN_STARTING_SETTLEMENT_METHANE;
            
            if (reservableRover && backupRover && minNum && enoughBags
                    && !embarkingMissions && reservableLUV
                    && availableAttachmentParts && hasBasicResources && enoughMethane) {

                try {
                    // Get available rover.
                    Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(
                            settlement, false);
                    if (rover != null) {

                        // Find best mining site.
                        ExploredLocation miningSite = Mining.determineBestMiningSite(
                                rover, settlement);
                        if (miningSite != null) {
                            result = Mining.getMiningSiteValue(miningSite, settlement);
                            if (result > 1D) {
                                result = 1D;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error getting mining site.", e);
                }
            }

            // Crowding modifier
            int crowding = settlement.getCurrentPopulationNum()
                    - settlement.getPopulationCapacity();
            if (crowding > 0) {
                result *= (crowding + 1);
            }

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartMissionProbabilityModifier(Mining.class);
            }
        }

        if (result > 0D) {
            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(person
                    .getSettlement()) < RoverMission.MIN_PEOPLE) {
                result = 0D;
            }
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