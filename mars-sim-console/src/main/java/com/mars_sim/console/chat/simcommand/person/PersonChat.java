/*
 * Mars Simulation Project
 * PersonChat.java
 * @date 2023-06-14
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import java.util.Arrays;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.command.InteractiveChatCommand;
import com.mars_sim.console.chat.simcommand.ConnectedUnitCommand;
import com.mars_sim.console.chat.simcommand.unit.EquipmentCommand;
import com.mars_sim.console.chat.simcommand.unit.InventoryCommand;
import com.mars_sim.console.chat.simcommand.unit.MissionCommand;
import com.mars_sim.console.chat.simcommand.unit.UnitLocationCommand;
import com.mars_sim.console.chat.simcommand.unit.WorkerActivityCommand;
import com.mars_sim.console.chat.simcommand.unit.WorkerAttributeCommand;
import com.mars_sim.console.chat.simcommand.unit.WorkerSkillsCommand;
import com.mars_sim.console.chat.simcommand.unit.WorkerTaskCommand;
import com.mars_sim.console.chat.simcommand.unit.WorkerWorkCommand;
import com.mars_sim.core.person.Person;

/**
 * A connect to a Person object.
 */
public class PersonChat extends ConnectedUnitCommand {

	public static final String PERSON_GROUP = "Person";

	private static final List<ChatCommand> COMMANDS = Arrays.asList(
																	new WorkerAttributeCommand(PERSON_GROUP),
																	BedCommand.BED,
																	CareerCommand.FRIEND,														
																	ProfileCommand.PROFILE,
																	LoadingCommand.LOADING,
																	EvaCommand.EVA,
																	FriendCommand.FRIEND,
																	PersonResetCommand.RESET,
																	JobProspectCommand.JOB_PROSPECT,
																	new EquipmentCommand(PERSON_GROUP),
																	new UnitLocationCommand(PERSON_GROUP),
																	new InventoryCommand(PERSON_GROUP),
																	new MissionCommand(PERSON_GROUP),
																	new PersonHealthCommand(),
																    new PersonTrainingCommand(),
																    RoleProspectCommand.ROLE_PROSPECT,
																    SleepCommand.SLEEP,
																    SocialCommand.SOCIAL,
																    StatusCommand.STATUS,
																    StudyCommand.STUDY,
																    IllnessCommand.ILLNESS,
																    new WorkerActivityCommand(PERSON_GROUP),
																    new WorkerTaskCommand(PERSON_GROUP),
																    new WorkerWorkCommand(PERSON_GROUP),
																    new WorkerSkillsCommand(PERSON_GROUP));
		
	
	public PersonChat(Person person, InteractiveChatCommand parent) {
		super(person, COMMANDS, parent);
	}

	/**
	 * Repeats the status command.
	 */
	@Override
	public String getIntroduction() {
		return StatusCommand.getStatus(getPerson());
	}

	public Person getPerson() {
		return (Person) getUnit();
	}
}
