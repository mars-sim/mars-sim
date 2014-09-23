/**
 * Mars Simulation Project
 * ExplorationMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.SpecimenContainer;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the Exploration mission.
 */
public class ExplorationMeta implements MetaMission {

    /** Mission name */
    private static final String NAME = Msg.getString(
            "Mission.description.exploration"); //$NON-NLS-1$
    
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

            // Check if there are enough specimen containers at the settlement for collecting rock samples.
            boolean enoughContainers = false;
            int numContainers = settlement.getInventory()
                    .findNumEmptyUnitsOfClass(SpecimenContainer.class, false);
            enoughContainers = (numContainers >= Exploration.REQUIRED_SPECIMEN_CONTAINERS);

            // Check for embarking missions.
            boolean embarkingMissions = VehicleMission
                    .hasEmbarkingMissions(settlement);

            // Check if settlement has enough basic resources for a rover mission.
            boolean hasBasicResources = RoverMission
                    .hasEnoughBasicResources(settlement);

            // Check if starting settlement has minimum amount of methane fuel.
            AmountResource methane = AmountResource.findAmountResource("methane");
            boolean enoughMethane = settlement.getInventory().getAmountResourceStored(methane, false) >= 
                    RoverMission.MIN_STARTING_SETTLEMENT_METHANE;

            if (reservableRover && backupRover && minNum && enoughContainers
                    && !embarkingMissions && hasBasicResources && enoughMethane) {
                try {
                    // Get available rover.
                    Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(
                            settlement, false);
                    if (rover != null) {
                        // Check if any mineral locations within rover range.
                        if (Exploration.hasNearbyMineralLocations(rover, settlement)) {
                            result = 1D;
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE,
                            "Error determining mineral locations.", e);
                }
            }

            // Crowding modifier
            int crowding = settlement.getCurrentPopulationNum()
                    - settlement.getPopulationCapacity();
            if (crowding > 0)
                result *= (crowding + 1);

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null)
                result *= job
                        .getStartMissionProbabilityModifier(Exploration.class);
        }

        if (result > 0D) {
            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(person
                    .getSettlement()) < RoverMission.MIN_PEOPLE)
                result = 0D;
        }

        return result;
    }
}