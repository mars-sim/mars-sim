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
import org.mars_sim.msp.core.robot.Robot;
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
            Settlement settlement = person.getParkedSettlement();

            boolean go = true;
  
            // Check if a mission-capable rover is available.
            if (!RoverMission.areVehiclesAvailable(settlement, false)){
            	go = false;
            	return 0;
            }

            // Check if available backup rover.
            else if (!RoverMission.hasBackupRover(settlement)){
            	go = false;
            	return 0;
            }
            
            // Check if minimum number of people are available at the settlement.
            // Plus one to hold down the fort.
            else if (!RoverMission.minAvailablePeopleAtSettlement(
                    settlement, (RoverMission.MIN_PEOPLE + 1))){
            	go = false;
            	return 0;
            }
            
            // Check if there are enough specimen containers at the settlement for collecting rock samples.
            //boolean enoughContainers = false;
            //int numContainers = settlement.getSettlementInventory().findNumEmptyUnitsOfClass(SpecimenContainer.class, false);
            else if (!(settlement.getInventory().findNumEmptyUnitsOfClass(SpecimenContainer.class, false) 
            		>= Exploration.REQUIRED_SPECIMEN_CONTAINERS)){
            	go = false;
            	return 0;
            }
            
            // Check for embarking missions.
            else if (!VehicleMission.hasEmbarkingMissions(settlement)){
            	go = false;
            	return 0;
            }
            
            // Check if settlement has enough basic resources for a rover mission.
            else if (!RoverMission.hasEnoughBasicResources(settlement)){
            	go = false;
            	return 0;
            }
            
            // Check if starting settlement has minimum amount of methane fuel.
            //AmountResource methane = AmountResource.findAmountResource("methane");
            else if (!(settlement.getInventory().getAmountResourceStored(AmountResource.findAmountResource("methane"), false) >= 
                    RoverMission.MIN_STARTING_SETTLEMENT_METHANE))
            		go = false;
            
            if (go) {
            //if (reservableRover && backupRover && minNum && enoughContainers && !embarkingMissions && hasBasicResources && enoughMethane) {
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
                result *= job.getStartMissionProbabilityModifier(Exploration.class)* settlement.getGoodsManager().getTourismFactor();
        }

        if (result > 0D) {
            // Check if min number of EVA suits at settlement.
            if (Mission.getNumberAvailableEVASuitsAtSettlement(person
                    .getParkedSettlement()) < RoverMission.MIN_PEOPLE)
                result = 0D;
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