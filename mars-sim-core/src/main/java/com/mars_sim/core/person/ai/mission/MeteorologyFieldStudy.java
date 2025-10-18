/*
 * Mars Simulation Project
 * MeteorologyFieldStudy.java
 * @date 2021-08-27
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.mission;

import java.util.Collection;
import java.util.Set;

import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.task.MeteorologyStudyFieldWork;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.vehicle.Rover;

/**
 * A mission to do meteorology research at a remote field location for a scientific
 * study.
 */
public class MeteorologyFieldStudy extends FieldStudyMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Amount of time to field a site. */
	public static final double FIELD_SITE_TIME = 500D;

	/** How many specimen boxes per mission member */
	private static final int SPECIMEN_BOX_MEMBER = 4;

	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.TOURISM);

	/**
	 * Constructor.
	 * 
	 * @param startingPerson {@link Person} the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public MeteorologyFieldStudy(Person startingPerson, boolean needsReview) {
		
		super(MissionType.METEOROLOGY, startingPerson,
			  ScienceType.METEOROLOGY, FIELD_SITE_TIME, needsReview);
		
		setEVAEquipment(EquipmentType.SPECIMEN_BOX,
			  getMembers().size() * SPECIMEN_BOX_MEMBER);
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
	public MeteorologyFieldStudy(Collection<Worker> members, Person leadResearcher,
			ScientificStudy study, Rover rover, Coordinates fieldSite) {

		super(MissionType.METEOROLOGY, leadResearcher, rover,
				  study, FIELD_SITE_TIME, members, fieldSite);

		setEVAEquipment(EquipmentType.SPECIMEN_BOX,
						getMembers().size() * SPECIMEN_BOX_MEMBER);
	}
	
	@Override
	protected Task createFieldStudyTask(Person person, Person leadResearcher, ScientificStudy study, Rover vehicle) {
		return new MeteorologyStudyFieldWork(person, leadResearcher, study, vehicle);
	}

	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return OBJECTIVES;
	}
}
