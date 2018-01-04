/**
 * Mars Simulation Project
 * RelaxMeta.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Relax;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * Meta task for the Relax task.
 */
public class RelaxMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.relax"); //$NON-NLS-1$

    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .1D;

    /** default logger. */
    private static Logger logger = Logger.getLogger(RelaxMeta.class.getName());

    private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock;// = sim.getMasterClock();
	private static MarsClock marsClock;// = masterClock.getMarsClock();

	public RelaxMeta() {
        masterClock = sim.getMasterClock();
        if (masterClock != null) { // to avoid NullPointerException during maven test
	        marsClock = masterClock.getMarsClock();
        }
	}

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Relax(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 5D;

        // Crowding modifier
        if (person.isInside()) {

            // Stress modifier
            result += person.getStress() * 5D;
            // fatigue modifier
            result += (person.getFatigue()-100) / 50D;

            if (result < 0) result = 0;
            
            try {
                Building recBuilding = Relax.getAvailableRecreationBuilding(person);
                if (recBuilding != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, recBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, recBuilding);
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }


        // Modify probability if during person's work shift.
        int millisols = marsClock.getMsol0();
        boolean isShiftHour = person.getTaskSchedule().isShiftHour(millisols);
        if (isShiftHour) {
            result*= WORK_SHIFT_MODIFIER;
        }

        // 2015-06-07 Added Preference modifier
        if (result > 0D) {
            result = result + result * person.getPreference().getPreferenceScore(this)/5D;
        }

        if (result < 0) result = 0;

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new Relax(robot);
	}

	@Override
	public double getProbability(Robot robot) {
        double result = 0D;

        // TODO: in what case should a bot "relax" or slow down its pace?
        // result += robot.getPhysicalCondition().getStress();

        /*


        // Crowding modifier
        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            try {
                Building recBuilding = Relax.getAvailableRecreationBuilding(robot);
                if (recBuilding != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, recBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(robot, recBuilding);
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
*/
        return result;
	}
}
