/*
 * Mars Simulation Project
 * MeteorologyFieldStudy.java
 * @date 2021-08-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.MeteorologyStudyFieldWork;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.vehicle.Rover;

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
	private static final int SPECIMEN_BOX_MEMBER = 3;

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
}
