/*
 * Mars Simulation Project
 * AreologyFieldStudy.java
 * @date 2021-08-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.AreologyStudyFieldWork;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A mission to do areology research at a remote field location for a scientific
 * study.
 */
public class AreologyFieldStudy extends FieldStudyMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Amount of time to field a site. */
	private static final double FIELD_SITE_TIME = 1000D;

	/**
	 * Constructor.
	 * 
	 * @param startingPerson {@link Person} the person starting the mission.
	 * @param needsReview
	 * @throws MissionException if problem constructing mission.
	 */
	public AreologyFieldStudy(Person startingPerson, boolean needsReview) {
		super(MissionType.AREOLOGY, startingPerson, ScienceType.AREOLOGY, FIELD_SITE_TIME, needsReview);
	}

	/**
	 * Constructor with explicit information.
	 * 
	 * @param members            the mission members.
	 * @param leadResearcher     the lead researcher
	 * @param study              the scientific study.
	 * @param rover              the rover used by the mission.
	 * @param fieldSite          the field site to research.
	 * @throws MissionException if error creating mission.
	 */
	public AreologyFieldStudy(Collection<Worker> members, Person leadResearcher,
			ScientificStudy study, Rover rover, Coordinates fieldSite) {

		super(MissionType.AREOLOGY, leadResearcher, rover,
			  study, FIELD_SITE_TIME, members, fieldSite);
	}

	/**
	 * Factory method to create a Task to perform the require field study
	 */
	@Override
	protected Task createFieldStudyTask(Person person, Person leadResearcher, ScientificStudy study, Rover vehicle) {
		return new AreologyStudyFieldWork(person, leadResearcher, study, vehicle);
	}
}
