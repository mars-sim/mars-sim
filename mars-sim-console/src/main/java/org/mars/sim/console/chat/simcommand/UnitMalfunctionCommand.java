package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;

/**
 * Command to create a malfunction in a Malfunctionable.
 */
public class UnitMalfunctionCommand extends ChatCommand {

	/**
	 * Create a command that is assigned to a command group
	 * @param group
	 */
	public UnitMalfunctionCommand(String group) {
		super(group, "ml", "malfunction", "Display any malfunctions");
	}
	
	@Override
	public boolean execute(Conversation context, String input) {
		
		MalfunctionManager mgr = null;

		if (context.getCurrentCommand() instanceof ConnectedUnitCommand) {
			Unit source = ((ConnectedUnitCommand) context.getCurrentCommand()).getUnit();

			if (source instanceof Malfunctionable) {
				mgr = ((Malfunctionable) source).getMalfunctionManager();
			}
		}
		
		if (mgr == null) {
			context.println("Sorry not connected to a Malfunctionable Unit");
			return false;
		}

		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Malfunction", CommandHelper.MALFUNCTION_WIDTH, "Severity", "%age fixed");
		for (Malfunction malfunction : mgr.getMalfunctions()) {
			response.appendTableRow(malfunction.getName(), malfunction.getSeverity(), malfunction.getPercentageFixed());
		}
		context.println(response.getOutput());
		return true;
	}
}
