/*
 * Mars Simulation Project
 * MissionCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Unit;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.vehicle.Vehicle;

/** 
 * 
 */
public class MissionCommand extends AbstractUnitCommand {
	
	public MissionCommand(String group) {
		super(group, "m", "mission", "About my missions");
	}

	@Override
	protected boolean execute(Conversation context, String input, Unit source) {
		Mission mission = getMission(context, source);
		
		if (mission != null) {
			StructuredResponse response = new StructuredResponse();
			response.appendBlankLine();
			response.appendHeading(mission.getName());
			CommandHelper.outputMissionDetails(response, mission);
			response.appendBlankLine();
			context.println(response.getOutput());
		}
		else {
			context.println("No mission");
		}
		return true;
	}

	/**
	 * Find the Mission off the context.
	 * @param context
	 * @return Null if one can not be found
	 */
	private Mission getMission(Conversation context, Unit source) {
		Mission result = null;

		if (source instanceof Vehicle) {
			result = ((Vehicle) source).getMission();
		}
		else if (source instanceof Worker) {
			result = ((Worker) source).getMission();
		}

		return result;
	}

}
