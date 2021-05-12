package org.mars.sim.console.chat.simcommand.unit;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
			response.appendHeading(mission.getName());
			CommandHelper.outputMissionDetails(response, mission);
			
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
