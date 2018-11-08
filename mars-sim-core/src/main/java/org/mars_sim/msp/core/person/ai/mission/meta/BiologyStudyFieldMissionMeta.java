/**
 * Mars Simulation Project
 * BiologyStudyFieldMissionMeta.java
 * @version 3.1.0 2017-05-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.BiologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A meta mission for the BiologyStudyFieldMission mission.
 */
public class BiologyStudyFieldMissionMeta implements MetaMission {

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

        double result = 0D;

        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();

            // Check if a mission-capable rover is available.
            if (!RoverMission.areVehiclesAvailable(settlement, false)) {
            	return 0;
            }

            // Check if available backup rover.
            else if (!RoverMission.hasBackupRover(settlement)) {
            	return 0;
            }

    	    // Check if minimum number of people are available at the settlement.
    	    if (!RoverMission.minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_STAYING_MEMBERS)) {
    	        return 0;
    	    }

    	    // Check if min number of EVA suits at settlement.
    	    if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_GOING_MEMBERS) {
    	        return 0;
    	    }

            // Check for embarking missions.
            else if (VehicleMission.hasEmbarkingMissions(settlement)) {
            	return 0;
            }

            // Check if settlement has enough basic resources for a rover mission.
            else if (!RoverMission.hasEnoughBasicResources(settlement, true)) {
            	return 0;
            }

            // Check if starting settlement has minimum amount of methane fuel.
            //AmountResource methane = AmountResource.findAmountResource("methane");
            else if (settlement.getInventory().getAmountResourceStored(ResourceUtil.methaneAR, false) <
                    RoverMission.MIN_STARTING_SETTLEMENT_METHANE) {
            	return 0;
            }

            // Get available rover.
            Rover rover = (Rover) RoverMission.getVehicleWithGreatestRange(settlement, false);
            if (rover != null) {

                ScienceType biology = ScienceType.BIOLOGY;

                // Add probability for researcher's primary study (if any).
                ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
                ScientificStudy primaryStudy = studyManager.getOngoingPrimaryStudy(person);
                if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
                    if (!primaryStudy.isPrimaryResearchCompleted()) {
                        if (biology == primaryStudy.getScience())
                            result += 2D;
                    }
                }

                // Add probability for each study researcher is collaborating on.
                Iterator<ScientificStudy> i = studyManager.getOngoingCollaborativeStudies(person).iterator();
                while (i.hasNext()) {
                    ScientificStudy collabStudy = i.next();
                    if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
                        if (!collabStudy.isCollaborativeResearchCompleted(person)) {
                            if (biology == collabStudy.getCollaborativeResearchers().get(person))
                                result += 1D;
                        }
                    }
                }
            }


            // Crowding modifier
            int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
            if (crowding > 0) result *= (crowding + 1);

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
            	// If this town has a tourist objective, add bonus
                result *= job.getStartMissionProbabilityModifier(BiologyStudyFieldMission.class) 
                		* (settlement.getGoodsManager().getTourismFactor()
                		 + settlement.getGoodsManager().getResearchFactor())/1.5;
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