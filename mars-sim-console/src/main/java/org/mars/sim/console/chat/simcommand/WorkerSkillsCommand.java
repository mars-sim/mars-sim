package org.mars.sim.console.chat.simcommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;

/**
 * Command to display the Skills of a Worker
 */
public class WorkerSkillsCommand extends ChatCommand {

	public WorkerSkillsCommand(String group) {
		super(group, "sk", "skills", "What skills to I have?");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		ConnectedUnitCommand parent = (ConnectedUnitCommand) context.getCurrentCommand();	
		Unit target = parent.getUnit();

		SkillManager skillManager = null;
		if (target instanceof Worker) {
			skillManager = ((Worker)target).getSkillManager();
		}
		else {
			context.println("Sorry I am not a Worker");
			return false;
		}

		boolean result = false;
		if (skillManager != null) {
			StructuredResponse responseText = new StructuredResponse();
			responseText.appendTableHeading("Type of Skill", CommandHelper.TASK_WIDTH, "Level", "Exp. Needed", "Labor Time [sols]");

			Map<String, Integer> levels = skillManager.getSkillLevelMap();
			Map<String, Integer> exps = skillManager.getSkillDeltaExpMap();
			Map<String, Integer> times = skillManager.getSkillTimeMap();
			List<String> skillNames = skillManager.getKeyStrings();
			Collections.sort(skillNames);

			for (String n : skillNames) {
				responseText.appendTableRow(n, levels.get(n), exps.get(n),
											Math.round(100.0 * times.get(n))/100000.0);	
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
