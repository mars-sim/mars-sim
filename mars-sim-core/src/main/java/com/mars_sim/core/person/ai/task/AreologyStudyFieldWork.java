/*
 * Mars Simulation Project
 * AreologyStudyFieldWork.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.tools.Msg;

/**
 * A task for the EVA operation of performing areology field work at a research
 * site for a scientific study.
 */
public class AreologyStudyFieldWork extends ScientificStudyFieldWork {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.areologyFieldWork"); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase FIELD_WORK = new TaskPhase(Msg.getString("Task.phase.fieldWork.areology")); //$NON-NLS-1$

	/**
	 * Constructor.
	 * 
	 * @param person         the person performing the task.
	 * @param leadResearcher the researcher leading the field work.
	 * @param study          the scientific study the field work is for.
	 * @param rover          the rover
	 */
	public AreologyStudyFieldWork(Person person, Person leadResearcher, ScientificStudy study, Rover rover) {

		// Use EVAOperation parent constructor.
		super(NAME, FIELD_WORK, person, leadResearcher, study, rover);
	}
}
