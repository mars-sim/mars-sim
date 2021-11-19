/**
 * Mars Simulation Project
 * BiologyStudyFieldWork.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for the EVA operation of performing biology field work at a research site
 * for a scientific study.
 */
public class BiologyStudyFieldWork
extends ScientificStudyFieldWork
implements Serializable {

    /** default serial id.*/
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.biologyFieldWork"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase FIELD_WORK = new TaskPhase(Msg.getString(
            "Task.phase.fieldWork.biology")); //$NON-NLS-1$
    /**
     * Constructor.
     * @param person the person performing the task.
     * @param leadResearcher the researcher leading the field work.
     * @param study the scientific study the field work is for.
     * @param rover the rover
     */
    public BiologyStudyFieldWork(Person person, Person leadResearcher, ScientificStudy study,
            Rover rover) {

        // Use EVAOperation parent constructor.
        super(NAME, FIELD_WORK, person, leadResearcher, study, rover);
    }
}
