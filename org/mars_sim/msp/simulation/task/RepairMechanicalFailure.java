/**
 * Mars Simulation Project
 * RepairMechanicalFailure.java
 * @version 2.72 2001-07-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The RepairMechanicalFailure class is a task for fixing a mechanical failure on a vehicle. 
 */
class RepairMechanicalFailure extends Task {

    // Data members
    private Vehicle vehicle;
    private MechanicalFailure failure;

    /** Constructs a RepairMechanicalFailure object with a given mechanical failure. 
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param failure mechanical failure to be repaired 
     */
    public RepairMechanicalFailure(Person person, VirtualMars mars, MechanicalFailure failure) {
        super("Reparing " + failure.getName() + " on " + person.getVehicle().getName(), person, mars);

        this.failure = failure;
        vehicle = person.getVehicle();
    }

    /** Performs this task for a given period of time 
     *  @param time amount of time to perform task (in millisols) 
     */
    double doTask(double time) {
        double timeLeft = super.doTask(time);
        if (subTask != null) return timeLeft;

        // Determine effective work time person completes based on "Vehicle Mechanic" skill.
        double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getSkillLevel("Vehicle Mechanic");
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

        // If failure is repaired, task is done.
        if (failure.isFixed()) {
            vehicle.setStatus("Moving");
            isDone = true;
        }

        return 0;
    }
}
