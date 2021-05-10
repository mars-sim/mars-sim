package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.person.health.HealthProblem;

/** 
 * 
 */
public class SuicideCommand extends AbstractPersonCommand {
	public static final ChatCommand SUICIDE = new SuicideCommand();
	
	private SuicideCommand() {
		super("su", "suicide", "Commit suicide");
		
		setInteractive(true);
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		String toExit = context.getInput("Get the person to commit suicide (Y/N)?");
        if ("Y".equalsIgnoreCase(toExit)) {
        	HealthProblem problem = new HealthProblem(ComplaintType.SUICIDE, person);
			person.getPhysicalCondition().recordDead(problem, true, "I've had enough");
        }
		return true;
	}
}
