/**
 * Mars Simulation Project
 * PersonChat.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.command.InteractiveChatCommand;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars.sim.console.chat.simcommand.unit.EquipmentCommand;
import org.mars.sim.console.chat.simcommand.unit.InventoryCommand;
import org.mars.sim.console.chat.simcommand.unit.MissionCommand;
import org.mars.sim.console.chat.simcommand.unit.UnitLocationCommand;
import org.mars.sim.console.chat.simcommand.unit.WorkerActivityCommand;
import org.mars.sim.console.chat.simcommand.unit.WorkerAttributeCommand;
import org.mars.sim.console.chat.simcommand.unit.WorkerSkillsCommand;
import org.mars.sim.console.chat.simcommand.unit.WorkerTaskCommand;
import org.mars.sim.console.chat.simcommand.unit.WorkerWorkCommand;
import org.mars_sim.msp.core.person.Person;

/**
 * A connect to a Person object
 */
public class PersonChat extends ConnectedUnitCommand {

	public static final String PERSON_GROUP = "Person";

	private static final List<ChatCommand> COMMANDS = Arrays.asList(
																	new WorkerAttributeCommand(PERSON_GROUP),
																	BedCommand.BED,
																	ProfileCommand.PROFILE,
																	EvaCommand.EVA,
																	FriendCommand.FRIEND,
																	JobProspectCommand.JOB_PROSPECT,
																	new EquipmentCommand(PERSON_GROUP),
																	new UnitLocationCommand(PERSON_GROUP),
																	new InventoryCommand(PERSON_GROUP),
																	new MissionCommand(PERSON_GROUP),
																	new PersonHealthCommand(),
																    new PersonTrainingCommand(),
																    RoleProspectCommand.ROLE_PROSPECT,
																    ShiftCommand.SHIFT,
																    SleepCommand.SLEEP,
																    SocialCommand.SOCIAL,
																    StatusCommand.STATUS,
																    StudyCommand.STUDY,
																    SuicideCommand.SUICIDE,
																    new WorkerActivityCommand(PERSON_GROUP),
																    new WorkerTaskCommand(PERSON_GROUP),
																    new WorkerWorkCommand(PERSON_GROUP),
																    new WorkerSkillsCommand(PERSON_GROUP));
		
	
	public PersonChat(Person person, InteractiveChatCommand parent) {
		super(person, COMMANDS, parent);
	}

	/**
	 * Repeat the status command
	 */
	@Override
	public String getIntroduction() {
		return StatusCommand.getStatus(getPerson());
	}

	public Person getPerson() {
		return (Person) getUnit();
	}
}
