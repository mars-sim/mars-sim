/*
 * Mars Simulation Project
 * BiologyFieldStudy.java
 * @date 2021-08-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.BiologyStudyFieldWork;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A mission to do biology research at a remote field location for a scientific
 * study. TODO externalize strings
 */
public class BiologyFieldStudy extends FieldStudyMission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Default description. */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.biologyFieldStudy"); //$NON-NLS-1$

	/** Amount of time to field a site. */
	public static final double FIELD_SITE_TIME = 1000D;


	/**
	 * Constructor.
	 * 
	 * @param startingPerson {@link Person} the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public BiologyFieldStudy(Person startingPerson) {
		super(DEFAULT_DESCRIPTION, MissionType.BIOLOGY, startingPerson,
			  ScienceType.BIOLOGY, FIELD_SITE_TIME);

	}

	/**
	 * Constructor with explicit information.
	 * 
	 * @param members            the mission members.
	 * @param leadResearcher     the lead researcher
	 * @param study              the scientific study.
	 * @param rover              the rover used by the mission.
	 * @param fieldSite          the field site to research.
	 * @param description        the mission description.
	 */
	public BiologyFieldStudy(Collection<MissionMember> members, Person leadResearcher,
			ScientificStudy study, Rover rover, Coordinates fieldSite, String description) {
		super(description, MissionType.BIOLOGY, leadResearcher, rover,
				  study, FIELD_SITE_TIME, members, fieldSite);
	}

	@Override
	protected Task createFieldStudyTask(Person person, Person leadResearcher, ScientificStudy study, Rover vehicle) {
		return new BiologyStudyFieldWork(person, leadResearcher, study, vehicle);
	}
}
