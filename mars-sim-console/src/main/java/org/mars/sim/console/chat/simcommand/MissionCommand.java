package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.vehicle.Vehicle;

/** 
 * 
 */
public class MissionCommand extends ChatCommand {
	
	public MissionCommand(String group) {
		super(group, "m", "mission", "About my missions");
	}

	@Override
	public boolean execute(Conversation context, String input) {
		Mission mission = getMission(context);
		
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
	private Mission getMission(Conversation context) {
		Mission result = null;
		
		if (context.getCurrentCommand() instanceof ConnectedUnitCommand) {
			ConnectedUnitCommand parent = (ConnectedUnitCommand) context.getCurrentCommand();
			Unit source = parent.getUnit();
			if (source instanceof Vehicle) {
				result = ((Vehicle) source).getMission();
			}
			else if (source instanceof Person) {
				result = ((Person) source).getMind().getMission();
			}
			else if (source instanceof Robot) {
				result = ((Robot) source).getBotMind().getMission();
			}
		}
		return result;
	}

}
