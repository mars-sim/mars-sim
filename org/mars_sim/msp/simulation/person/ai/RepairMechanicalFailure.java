/**
 * Mars Simulation Project
 * RepairMechanicalFailure.java
 * @version 2.74 2002-02-07
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.io.Serializable;

/** The RepairMechanicalFailure class is a task for fixing a mechanical failure on a vehicle.
 */
class RepairMechanicalFailure extends Task implements Serializable {

    // Data members
    private Vehicle vehicle;
    private MechanicalFailure failure;

    /** Constructs a RepairMechanicalFailure object.
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public RepairMechanicalFailure(Person person, VirtualMars mars) {
        super("Repairing", person, true, mars);

        vehicle = person.getVehicle();
        failure = vehicle.getMechanicalFailure();
        name = "Repairing " + failure.getName() + " on " + vehicle.getName();
        description = name;
        // System.out.println(person.getName() + " " + name);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return 0 if there is no chance to perform this task given the person and the situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, VirtualMars mars) {
        double result = 0D;

        if (person.getLocationSituation() == Person.INVEHICLE) {
            Vehicle vehicle = person.getVehicle();
            MechanicalFailure failure = vehicle.getMechanicalFailure();
            if ((failure != null) && !failure.isFixed()) result = 100D;
        }

        return result;
    }

    /** Performs this task for a given period of time
     *  @param time amount of time to perform task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        if (!failure.isFixed()) {
            // Determine effective work time person completes based on "Vehicle Mechanic" skill.
            double workTime = timeLeft;
            int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Vehicle Mechanic");
            if (mechanicSkill == 0) workTime /= 2;
            if (mechanicSkill > 1) workTime += Math.round((double) workTime * (.2D * (double) mechanicSkill));

            // Add work time to repair failure.
            failure.addWorkTime(workTime);

            // Add experience to "Vehicle Mechanic" skill.
            // (.3 base experience points per millisol of work)
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double experience = .3D * workTime;
            double aptitudeModifier = person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
            experience += experience * ((aptitudeModifier - 50D) / 100D);
            person.getSkillManager().addExperience("Vehicle Mechanic", experience);
        }
        else {
            // If failure is repaired, task is done.
            done = true;
            return timeLeft;
        }

        return 0;
    }
}
