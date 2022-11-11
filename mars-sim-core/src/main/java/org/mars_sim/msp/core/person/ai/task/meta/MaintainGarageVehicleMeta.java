/*
 * Mars Simulation Project
 * MaintainGarageVehicleMeta.java
 * @date 2022-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.MaintainGarageVehicle;
import org.mars_sim.msp.core.person.ai.task.MaintainBuilding;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Flyer;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the MaintainGarageVehicle task.
 */
public class MaintainGarageVehicleMeta extends MetaTask {

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainGarageVehicle"); //$NON-NLS-1$

	private static final int CAP = 3_000;
	
    public MaintainGarageVehicleMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		
		setPreferredJob(JobType.MECHANICS);
	}

	@Override
	public Task constructInstance(Person person) {
		return new MaintainGarageVehicle(person);
	}

	@Override
	public double getProbability(Person person) {

		double result = 0D;

		if (person.isInSettlement()) {

            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
			result = getSettlementProbability(person);

			Settlement settlement = person.getAssociatedSettlement();
			
            int num = settlement.getIndoorPeopleCount();
            result = result 
            		+ result * num / settlement.getPopulationCapacity() / 4D;
            
			result *= settlement.getGoodsManager().getTransportationFactor();

			result = applyPersonModifier(result, person);
		}

        if (result > CAP)
        	result = CAP;
        
		return result;
	}

	@Override
	public Task constructInstance(Robot robot) {
		return new MaintainGarageVehicle(robot);
	}

	@Override
	public double getProbability(Robot robot) {

		double result = 0D;

		if (robot.isInSettlement() && robot.getRobotType() == RobotType.REPAIRBOT) {

			result = getSettlementProbability(robot);
			
			// Effort-driven task modifier.
			result *= robot.getPerformanceRating();

			// Job modifier.
			RobotJob job = robot.getBotMind().getRobotJob();
			if (job != null) {
				result *= job.getStartTaskProbabilityModifier(MaintainGarageVehicle.class)
						* robot.getSettlement().getGoodsManager().getTransportationFactor();
			}

			if (result < 0)
				result = 0;

		}

		return result;
	}
	
	private double getSettlementProbability(Worker mechanic) {
		double result = 0D;
		
		for (Vehicle vehicle : MaintainGarageVehicle.getAllVehicleCandidates(mechanic, false)) {
			MalfunctionManager manager = vehicle.getMalfunctionManager();
			boolean hasMalfunction = manager.hasMalfunction();
			// Go to the next Vehicle if malfunction is found
			if (hasMalfunction)
				continue;
			
			boolean hasParts = manager.hasMaintenanceParts(mechanic.getSettlement());
			// Go to the next Vehicle if repair parts are not available
			if (!hasParts)
				continue;
			
			double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
			boolean minTime = (effectiveTime >= manager.getMaintenancePeriod());
			if (hasParts && minTime) {
				double entityProb = effectiveTime / 5;
				if (entityProb > 1000D) {
					entityProb = 1000D;
				}
				result += entityProb;
			}
			
			 int num = mechanic.getSettlement().getIndoorPeopleCount();
			 int total = mechanic.getSettlement().getOwnedVehicleNum(); 
			 int onMission = mechanic.getSettlement().getMissionVehicles().size();
				
             result = result 
             		+ result * num / mechanic.getSettlement().getPopulationCapacity() / 4D
             		+ result * (total - onMission) / 2.5;

             result *= mechanic.getSettlement().getGoodsManager().getTransportationFactor();
		}

		// Determine if settlement has available space in garage.
		boolean garageSpace = false;
		boolean needyVehicleInGarage = false;
		boolean needyFlyerInGarage = false;
		
		Settlement settlement = mechanic.getSettlement();
		Iterator<Building> j = settlement.getBuildingManager().getBuildings(FunctionType.VEHICLE_MAINTENANCE)
				.iterator();
		while (j.hasNext() && !garageSpace) {
			Building building = j.next();
			VehicleMaintenance garage = building.getVehicleParking();
			
			garageSpace = (garage.getAvailableCapacity() + garage.getAvailableFlyerCapacity() > 0);

			Iterator<Vehicle> i = garage.getVehicles().iterator();
			while (i.hasNext()) {
				if (i.next().isReservedForMaintenance()) {
					needyVehicleInGarage = true;
				}
			}
			
			Iterator<Flyer> ii = garage.getFlyers().iterator();
			while (ii.hasNext()) {
				if (ii.next().isReservedForMaintenance()) {
					needyFlyerInGarage = true;
				}
			}
		}

		if (!garageSpace && !needyVehicleInGarage && !needyFlyerInGarage) {
			result = 0D;
		}
		return result;
	}
}
