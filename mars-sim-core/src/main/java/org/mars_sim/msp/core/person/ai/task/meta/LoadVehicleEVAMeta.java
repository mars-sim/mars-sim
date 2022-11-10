/*
 * Mars Simulation Project
 * LoadVehicleEVAMeta.java
 * @date 2022-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the LoadVehicleEVA task.
 */
public class LoadVehicleEVAMeta extends MetaTask {

	/**
	 * The amount of resources (kg) one person of average strength can load per
	 * millisol.
	 */
	private static final double WATER_NEED = 10D;
	private static final double OXYGEN_NEED = 10D;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicleEVA"); //$NON-NLS-1$

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(LoadVehicleEVAMeta.class.getName());

    public LoadVehicleEVAMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.LOADERS);
	}


    @Override
    public Task constructInstance(Person person) {
        return new LoadVehicleEVA(person);
    }

    @Override
    public double getProbability(Person person) {
    	if (person.isOutside()) {
    		return 0;
    	}

        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person, false) == null)
    		return 0;

        // Check if it is night time.
		if (EVAOperation.isGettingDark(person))
			return 0;

        // Checks if the person's settlement is at meal time and is hungry
        if (EVAOperation.isHungryAtMealTime(person))
        	return 0;

        // Checks if the person is physically fit for heavy EVA tasks
		if (!EVAOperation.isEVAFit(person))
			return 0;

    	double result = 0D;

        Settlement settlement = person.getSettlement();

    	if (settlement != null) {

   			double score = person.getPhysicalCondition().computeHealthScore();
			score *= getRadiationModifier(settlement);

	        // Check all vehicle missions occurring at the settlement.
	        try {
	            List<Mission> missions = LoadVehicleGarage.getAllMissionsNeedingLoading(settlement, false);
                int num = missions.size();
               	if (num == 0)
               		return 0;
               	else
               		result += score * num;
	        }
	        catch (Exception e) {
	            logger.severe(person, "Error finding loading missions.", e);
	        }

	        // Check if any rovers are in need of EVA suits to allow occupants to exit.
	        if (!getRoversNeedingEVASuits(settlement).isEmpty()) {
	            int numEVASuits = settlement.findNumContainersOfType(EquipmentType.EVA_SUIT);
	            if (numEVASuits == 0)
	            	return 0;
	            else if (numEVASuits >= 2) {
	                result += 100D;
	            }
	        }

	        result *= settlement.getGoodsManager().getTransportationFactor();

	        double shiftBonus = person.getTaskSchedule().obtainScoreAtStartOfShift();
	        
	        // Encourage to get this task done early in a work shift
	        result *= shiftBonus / 10;
	        
	        result *= getPersonModifier(person);
    	}

        if (result < 0)
            result = 0;

        return result;
    }

		
	/**
	 * Gets a list of rovers with crew who are missing EVA suits.
	 * 
	 * @param settlement the settlement.
	 * @return list of rovers.
	 */
	private List<Rover> getRoversNeedingEVASuits(Settlement settlement) {

		List<Rover> result = new ArrayList<>();

		for(Vehicle vehicle : settlement.getParkedVehicles()) {
			if (vehicle instanceof Rover) {
				Rover rover = (Rover) vehicle;
				int peopleOnboard = rover.getCrewNum();
				if (peopleOnboard > 0) {
					int numSuits = rover.findNumEVASuits();
					double water = rover.getAmountResourceStored(ResourceUtil.waterID);
					double oxygen = rover.getAmountResourceStored(ResourceUtil.oxygenID);
					if (((numSuits == 0) || (water < WATER_NEED) || (oxygen < OXYGEN_NEED))
						&& !settlement.getBuildingManager().isInGarage(vehicle)) {
						logger.warning(rover, "Parked with crew but no EVA Suits");
						result.add(rover);
					}
				}
			}
		}

		return result;
	}
}
