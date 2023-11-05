/*
 * Mars Simulation Project
 * IllnessCommand.java
 * @date 2023-11-02
 * @author Manny Kung
 */

package com.mars_sim.console.chat.simcommand.person;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblem;

/** 
 * 
 */
public class IllnessCommand extends AbstractPersonCommand {
	public static final ChatCommand ILLNESS = new IllnessCommand();
	
	private IllnessCommand() {
		super("il", "illness", "Get ill");
		
		setInteractive(true);
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
    	String illnessStr = context.getInput("1. Poisoned; "
    										+ "2. Suffocation; "
    										+ "3. Heart Attack; "
    										+ "4. High Fatigue Collapse; "
    										+ "5. Ruptured Appendix "
    										+ "[Press any other key to exit]");
    	if ("1".equalsIgnoreCase(illnessStr)) {
        	HealthProblem problem = new HealthProblem(ComplaintType.FOOD_POISONING, person);
			person.getPhysicalCondition().recordDead(problem, true, "I was poisoned.");
			return true;
    	}
    	else if ("2".equalsIgnoreCase(illnessStr)) {	  		
        	HealthProblem problem = new HealthProblem(ComplaintType.SUFFOCATION, person);
			person.getPhysicalCondition().recordDead(problem, true, "I suffocated.");
    	}
    	else if ("3".equalsIgnoreCase(illnessStr)) {	  		
        	HealthProblem problem = new HealthProblem(ComplaintType.HEART_ATTACK, person);
			person.getPhysicalCondition().recordDead(problem, true, "I had a heart attack.");
    	}
    	else if ("4".equalsIgnoreCase(illnessStr)) {	  		
        	HealthProblem problem = new HealthProblem(ComplaintType.HIGH_FATIGUE_COLLAPSE, person);
			person.getPhysicalCondition().recordDead(problem, true, "I fainted.");
    	}
    	else if ("5".equalsIgnoreCase(illnessStr)) {	  		
        	HealthProblem problem = new HealthProblem(ComplaintType.RUPTURED_APPENDIX, person);
			person.getPhysicalCondition().recordDead(problem, true, "My appendix ruptured.");
    	}

		return true;
	}
}
