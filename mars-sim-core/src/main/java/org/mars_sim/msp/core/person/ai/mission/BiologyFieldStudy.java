/*
 * Mars Simulation Project
 * BiologyFieldStudy.java
 * @date 2021-08-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.Set;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.BiologyStudyFieldWork;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A mission to do biology research at a remote field location for a scientific
 * study. 
 */
public class BiologyFieldStudy extends FieldStudyMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Amount of time to field a site. */
	public static final double FIELD_SITE_TIME = 1000D;

	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.CROP_FARM, ObjectiveType.TOURISM);

	/**
	 * Constructor.
	 * 
	 * @param startingPerson {@link Person} the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public BiologyFieldStudy(Person startingPerson, boolean needsReview) {
		super(MissionType.BIOLOGY, startingPerson,
			  ScienceType.BIOLOGY, FIELD_SITE_TIME, needsReview);

	}

	/**
	 * Constructor with explicit information.
	 * 
	 * @param members            the mission members.
	 * @param leadResearcher     the lead researcher
	 * @param study              the scientific study.
	 * @param rover              the rover used by the mission.
	 * @param fieldSite          the field site to research.
	 */
	public BiologyFieldStudy(Collection<Worker> members, Person leadResearcher,
			ScientificStudy study, Rover rover, Coordinates fieldSite) {
		super(MissionType.BIOLOGY, leadResearcher, rover,
				  study, FIELD_SITE_TIME, members, fieldSite);
	}

	@Override
	protected Task createFieldStudyTask(Person person, Person leadResearcher, ScientificStudy study, Rover vehicle) {
		return new BiologyStudyFieldWork(person, leadResearcher, study, vehicle);
	}

	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return OBJECTIVES;
	}
}
