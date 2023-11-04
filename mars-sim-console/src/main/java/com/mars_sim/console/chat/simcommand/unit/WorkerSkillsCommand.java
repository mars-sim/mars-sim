/**
 * Mars Simulation Project
 * WorkerSkillsCommand.java
 * @date 2023-10-31
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import java.util.List;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Unit;
import com.mars_sim.core.person.ai.Skill;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.task.util.Worker;

/**
 * Command to display the Skills of a Worker
 */
public class WorkerSkillsCommand extends AbstractUnitCommand {

	public WorkerSkillsCommand(String group) {
		super(group, "sk", "skills", "What skills to I have?");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit target) {

		SkillManager skillManager = null;
		if (target instanceof Worker w) {
			skillManager = w.getSkillManager();
		}
		else {
			context.println("Sorry I am not a Worker");
			return false;
		}

		boolean result = false;
		if (skillManager != null) {
			StructuredResponse responseText = new StructuredResponse();
			responseText.appendTableHeading("Type of Skill", CommandHelper.TASK_WIDTH, "Level", "Exp. Needed", "Labor Time [sols]");

			List<Skill> skills = skillManager.getSkills();

			for (Skill n : skills) {
				responseText.appendTableRow(n.getType().getName(), n.getLevel(),
											n.getNeededExp(),
											String.format(CommandHelper.DOUBLE_FORMAT,
														n.getTime()/1000D));	
			}
			context.println(responseText.getOutput());
			
			result = true;
		}
		else {
			context.println("Sorry I can not provide that information");
		}
		
		return result;
	}
}
