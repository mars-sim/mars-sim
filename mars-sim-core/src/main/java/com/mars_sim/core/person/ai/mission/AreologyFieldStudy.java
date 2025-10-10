/*
 * Mars Simulation Project
 * AreologyFieldStudy.java
 * @date 2021-08-15
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.Collection;
import java.util.Set;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.task.ScientificStudyFieldWork;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.vehicle.Rover;

/**
 * A mission to do areology research at a remote field location for a scientific
 * study.
 */
public class AreologyFieldStudy extends FieldStudyMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Amount of time to field a site. */
	private static final double FIELD_SITE_TIME = 1000D;

	
	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.TOURISM);

	/**
	 * Constructor.
	 * 
	 * @param startingPerson {@link Person} the person starting the mission.
	 * @param needsReview
	 * @throws MissionException if problem constructing mission.
	 */
	public AreologyFieldStudy(Person startingPerson, boolean needsReview) {
		
		super(MissionType.AREOLOGY, startingPerson, 
			  ScienceType.AREOLOGY, FIELD_SITE_TIME, needsReview);
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
	 * Factory method to create a Task to perform the require field study.
	 */
	@Override
	protected Task createFieldStudyTask(Person person, Person leadResearcher, ScientificStudy study, Rover vehicle) {
		return ScientificStudyFieldWork.createFieldStudy(ScienceType.AREOLOGY, person, leadResearcher, study, vehicle);
	}

	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return OBJECTIVES;
	}
}
