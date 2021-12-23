/**
 * Mars Simulation Project
 * MaintainGroundVehicleGarageMeta.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the MaintainGroundVehicleGarage task.
 */
public class MaintainGroundVehicleGarageMeta extends MetaTask {

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainGroundVehicleGarage"); //$NON-NLS-1$

    public MaintainGroundVehicleGarageMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		
		setPreferredJob(JobType.MECHANIICS);
	}

	@Override
	public Task constructInstance(Person person) {
		return new MaintainGroundVehicleGarage(person);
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

		return result;
	}

	@Override
	public Task constructInstance(Robot robot) {
		return new MaintainGroundVehicleGarage(robot);
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
				result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleGarage.class)
						* robot.getSettlement().getGoodsManager().getTransportationFactor();
			}

			if (result < 0)
				result = 0;

		}

		return result;
	}
	
	private double getSettlementProbability(Worker mechanic) {
		double result = 0D;
		
		for( Vehicle vehicle : MaintainGroundVehicleGarage.getAllVehicleCandidates(mechanic, false)) {
			MalfunctionManager manager = vehicle.getMalfunctionManager();
			boolean hasMalfunction = manager.hasMalfunction();
			if (hasMalfunction)
				return 0;
			
			boolean hasParts = Maintenance.hasMaintenanceParts(mechanic, vehicle);
			if (!hasParts)
				return 0;
			
			double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
			boolean minTime = (effectiveTime >= 1000D);
			if (hasParts && minTime) {
				double entityProb = effectiveTime / 50D;
				if (entityProb > 100D) {
					entityProb = 100D;
				}
				result += entityProb;
			}
		}

		// Determine if settlement has available space in garage.
		boolean garageSpace = false;
		boolean needyVehicleInGarage = false;

		Settlement settlement = mechanic.getSettlement();
		Iterator<Building> j = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE)
				.iterator();
		while (j.hasNext() && !garageSpace) {
			Building building = j.next();
			VehicleMaintenance garage = building.getGroundVehicleMaintenance();
			if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity()) {
				garageSpace = true;
			}

			Iterator<Vehicle> i = garage.getVehicles().iterator();
			while (i.hasNext()) {
				if (i.next().isReservedForMaintenance()) {
					needyVehicleInGarage = true;
				}
			}
		}

		if (!garageSpace && !needyVehicleInGarage) {
			result = 0D;
		}
		return result;
	}
}
