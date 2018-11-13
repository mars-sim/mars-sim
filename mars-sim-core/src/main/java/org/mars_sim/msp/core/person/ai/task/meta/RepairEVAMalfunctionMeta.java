/**
 * Mars Simulation Project
 * RepairEVAMalfunctionMeta.java
 * @version 3.1.0 2017-03-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.RepairEVAMalfunction;
import org.mars_sim.msp.core.person.ai.task.RepairMalfunction;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

public class RepairEVAMalfunctionMeta implements MetaTask, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairEVAMalfunction"); //$NON-NLS-1$

//    private SurfaceFeatures surface;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Task constructInstance(Person person) {
		return new RepairEVAMalfunction(person);
	}

	@Override
	public double getProbability(Person person) {
		double result = 0D;

		if (person.isInSettlement()) {

			Settlement settlement = person.getSettlement();

			// Check for radiation events
			boolean[] exposed = settlement.getExposed();

			if (exposed[2]) {// SEP can give lethal dose of radiation
				return 0;
			}

			// Check if an airlock is available
			if (EVAOperation.getWalkableAvailableAirlock(person) == null)
				return 0;

			// Check if it is night time.
			SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
			if (surface.getSolarIrradiance(person.getCoordinates()) == 0D)
				if (!surface.inDarkPolarRegion(person.getCoordinates()))
					return 0;

			// Add probability for all malfunctionable entities in person's local.
			Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
			while (i.hasNext()) {
				Malfunctionable entity = i.next();
				MalfunctionManager manager = entity.getMalfunctionManager();

				// Check if entity has any EVA malfunctions.
				Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
				while (j.hasNext()) {
					Malfunction malfunction = j.next();
					try {
						if (RepairEVAMalfunction.hasRepairPartsForMalfunction(person, person.getTopContainerUnit(),
								malfunction)) {
							result += 100D;
						}
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}
				}

				// Check if entity requires an EVA and has any normal malfunctions.
				if (RepairEVAMalfunction.requiresEVA(person, entity)) {
					Iterator<Malfunction> k = manager.getNormalMalfunctions().iterator();
					while (k.hasNext()) {
						Malfunction malfunction = k.next();
						try {
							if (RepairMalfunction.hasRepairPartsForMalfunction(person, malfunction)) {
								result += 100D;
							}
						} catch (Exception e) {
							e.printStackTrace(System.err);
						}
					}
				}
			}

			// Effort-driven task modifier.
			result *= person.getPerformanceRating();

			// Job modifier if not in vehicle.
			Job job = person.getMind().getJob();
			if ((job != null)) {
				result *= job.getStartTaskProbabilityModifier(RepairEVAMalfunction.class);
			}

			// Modify if tinkering is the person's favorite activity.
			if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
				result *= 1.5D;
			}

			// 2015-06-07 Added Preference modifier
			if (result > 0D) {
				result = result + result * person.getPreference().getPreferenceScore(this) / 5D;
			}

			if (exposed[0]) {
				result = result / 2D;// Baseline can give a fair amount dose of radiation
			}

			if (exposed[1]) {// GCR can give nearly lethal dose of radiation
				result = result / 4D;
			}

			if (result < 0) {
				result = 0;
			}

		}

		return result;
	}

	@Override
	public Task constructInstance(Robot robot) {
		return null;// new RepairEVAMalfunction(robot);
	}

	@Override
	public double getProbability(Robot robot) {
		return 0;

//		double result = 0D;
//        if (robot.getBotMind().getRobotJob() instanceof Repairbot) {
//
//            // Add probability for all malfunctionable entities in person's local.
//            Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
//            while (i.hasNext()) {
//                Malfunctionable entity = i.next();
//                MalfunctionManager manager = entity.getMalfunctionManager();
//
//                // Check if entity has any EVA malfunctions.
//                Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
//                while (j.hasNext()) {
//                    Malfunction malfunction = j.next();
//                    try {
//                        if (RepairEVAMalfunction.hasRepairPartsForMalfunction(robot, robot.getTopContainerUnit(),
//                                malfunction)) {
//                            result += 100D;
//                        }
//                    }
//                    catch (Exception e) {
//                        e.printStackTrace(System.err);
//                    }
//                }
//
//                // Check if entity requires an EVA and has any normal malfunctions.
//                if (RepairEVAMalfunction.requiresEVA(robot, entity)) {
//                    Iterator<Malfunction> k = manager.getNormalMalfunctions().iterator();
//                    while (k.hasNext()) {
//                        Malfunction malfunction = k.next();
//                        try {
//                            if (RepairMalfunction.hasRepairPartsForMalfunction(robot, malfunction)) {
//                                result += 100D;
//                            }
//                        }
//                        catch (Exception e) {
//                            e.printStackTrace(System.err);
//                        }
//                    }
//                }
//            }
//
//            // Check if it is night time.
//            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
//            if (surface.getSolarIrradiance(robot.getCoordinates()) == 0D) {
//                if (!surface.inDarkPolarRegion(robot.getCoordinates())) {
//                    result = 0D;
//                }
//            }
//
//            if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
//                // Check if an airlock is available
//                if (EVAOperation.getWalkableAvailableAirlock(robot) == null) {
//                    result = 0D;
//                }
//            }
//
//            // Effort-driven task modifier.
//            result *= robot.getPerformanceRating();
//        }
//
//		return result;

	}
}