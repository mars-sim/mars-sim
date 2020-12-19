package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Unit;

/**
 * Command to stop speaking with an entity.
 * This is a singleton.
 */
public class UnitLocationCommand extends ChatCommand {

	public static final ChatCommand LOCATION = new UnitLocationCommand();

	private UnitLocationCommand() {
		super(TopLevel.SIMULATION_GROUP, "l", "location", "What is the location.");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	public void execute(Conversation context, String input) {
		ConnectedUnitCommand parent = (ConnectedUnitCommand) context.getCurrentCommand();
		
		Unit target = parent.getUnit();
		context.println(target.getLocationTag().getImmediateLocation());
	}

}
