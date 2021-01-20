package org.mars.sim.console.chat.simcommand.person;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.command.InteractiveChatCommand;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars.sim.console.chat.simcommand.MissionCommand;
import org.mars.sim.console.chat.simcommand.UnitLocationCommand;
import org.mars.sim.console.chat.simcommand.UnitSkillsCommand;
import org.mars_sim.msp.core.person.Person;

/**
 * A connect to a Person object
 */
public class PersonChat extends ConnectedUnitCommand {

	public static final String PERSON_GROUP = "Person";

	private static final List<ChatCommand> COMMANDS = Arrays.asList(AirlockCommand.AIRLOCK,
																	AttributeCommand.ATTRIBUTES,
																	BedCommand.BED,
																	ProfileCommand.PROFILE,
																	EvaCommand.EVA,
																	FriendCommand.FRIEND,
																	JobProspectCommand.JOB_PROSPECT,
																	new UnitLocationCommand(PERSON_GROUP),
																	new MissionCommand(PERSON_GROUP),
																	new PersonHealthCommand(),
																    new PersonTrainingCommand(),
																    RoleProspectCommand.ROLE_PROSPECT,
																    ShiftCommand.SHIFT,
																    SleepCommand.SLEEP,
																    SocialCommand.SOCIAL,
																    StatusCommand.STATUS,
																    StudyCommand.STUDY,
																    TaskCommand.TASK,
																    new UnitSkillsCommand(PERSON_GROUP));
		
	
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
