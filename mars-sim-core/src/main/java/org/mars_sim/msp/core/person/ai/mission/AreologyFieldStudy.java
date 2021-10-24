/*
 * Mars Simulation Project
 * AreologyFieldStudy.java
 * @date 2021-08-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.AreologyStudyFieldWork;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A mission to do areology research at a remote field location for a scientific
 * study.
 */
public class AreologyFieldStudy extends FieldStudyMission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Default description. */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.areologyFieldStudy"); //$NON-NLS-1$
	

	/** Minimum number of people to do mission. */
	private static final int MIN_PEOPLE = 2;

	/** Amount of time to field a site. */
	private static final double FIELD_SITE_TIME = 1000D;

	/**
	 * Constructor.
	 * 
	 * @param startingPerson {@link Person} the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public AreologyFieldStudy(Person startingPerson) {
		super(DEFAULT_DESCRIPTION, MissionType.AREOLOGY, startingPerson,  MIN_PEOPLE, ScienceType.AREOLOGY, FIELD_SITE_TIME);
	}

	/**
	 * Constructor with explicit information.
	 * 
	 * @param members            the mission members.
	 * @param startingSettlement the settlement the mission starts at.
	 * @param leadResearcher     the lead researcher
	 * @param study              the scientific study.
	 * @param rover              the rover used by the mission.
	 * @param fieldSite          the field site to research.
	 * @param description        the mission description.
	 * @throws MissionException if error creating mission.
	 */
	public AreologyFieldStudy(Collection<MissionMember> members, Settlement startingSettlement, Person leadResearcher,
			ScientificStudy study, Rover rover, Coordinates fieldSite, String description) {

		super(description, MissionType.AREOLOGY, leadResearcher, MIN_PEOPLE, rover,
			  study, FIELD_SITE_TIME, members, startingSettlement, fieldSite);
	}

	/**
	 * Factory method to create a Task to perform the require field study
	 */
	@Override
	protected Task createFieldStudyTask(Person person, Person leadResearcher, ScientificStudy study, Rover vehicle) {
		return new AreologyStudyFieldWork(person, leadResearcher, study, vehicle);
	}

	/**
	 * Can a researcher do the field study
	 * @param researcher
	 * @param rover Current vehicle
	 */
	@Override
	protected boolean canResearchSite(MissionMember researcher) {
		return AreologyStudyFieldWork.canResearchSite(researcher, getRover()); 
	}

}
