/*
 * Mars Simulation Project
 * MeteorologyFieldStudy.java
 * @date 2021-08-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.MeteorologyStudyFieldWork;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A mission to do meteorology research at a remote field location for a scientific
 * study. TODO externalize strings
 */
public class MeteorologyFieldStudy extends FieldStudyMission implements Serializable {


	/** default serial id. */
	private static final long serialVersionUID = 1L;

	
	/** Default description. */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.meteorologyFieldStudy"); //$NON-NLS-1$

	/** Minimum number of people to do mission. */
	private static final int MIN_PEOPLE = 2;

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
	public MeteorologyFieldStudy(Person startingPerson) {
		super(DEFAULT_DESCRIPTION, MissionType.METEOROLOGY, startingPerson,
				  MIN_PEOPLE, ScienceType.METEOROLOGY, FIELD_SITE_TIME);
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
	public MeteorologyFieldStudy(Collection<MissionMember> members, Settlement startingSettlement, Person leadResearcher,
			ScientificStudy study, Rover rover, Coordinates fieldSite, String description) {

		super(description, MissionType.METEOROLOGY, leadResearcher, MIN_PEOPLE, rover,
				  study, FIELD_SITE_TIME, members, startingSettlement, fieldSite);
	}
	
	@Override
	protected Task createFieldStudyTask(Person person, Person leadResearcher, ScientificStudy study, Rover vehicle) {
		return new MeteorologyStudyFieldWork(person, leadResearcher, study, vehicle);
	}

	@Override
	protected boolean canResearchSite(MissionMember researcher) {
		return MeteorologyStudyFieldWork.canResearchSite(researcher, getRover());
	}

	/**
	 * Need some Specimen boxes
	 */
	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Integer> required = super.getEquipmentNeededForRemainingMission(useBuffer);
		
		required.put(EquipmentType.getResourceID(EquipmentType.SPECIMEN_BOX),
												 getMembersNumber() * SPECIMEN_BOX_MEMBER);
		
		return required;
	}
}
