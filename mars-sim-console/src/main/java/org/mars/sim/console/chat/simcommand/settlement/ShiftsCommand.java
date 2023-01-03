/*
 * Mars Simulation Project
 * ShiftsCommand.java
 * @date 2022-11-22
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.Shift;
import org.mars_sim.msp.core.structure.ShiftManager;

/**
 * Command to display the details of a Settlement's Shift
 */
public class ShiftsCommand extends AbstractSettlementCommand {

	public static final ChatCommand SHIFTS = new ShiftsCommand();

	private ShiftsCommand() {
		super("sh", "shifts", "Display Shifts status");
	}

	/** 
	 * Output the answer
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		ShiftManager sm = settlement.getShiftManager();

		response.appendLabeledString("Name", sm.getName());
		response.appendLabeledString("Shift Rotation", sm.getRotationSols() + " sols");
		response.appendLabeledString("Maximum Worker Rotation", sm.getMaxOnLeave() + "%");
		response.appendLabeledString("Offset", sm.getOffset() + " mSol");

		response.appendTableHeading("Name", 10, "Allocated", "Start", "End");
		for(Shift s : sm.getShifts()) {
			response.appendTableRow(s.getName(), s.getSlotNumber(), s.getStart(), s.getEnd());
		}
	
		context.println(response.getOutput());
		return true;
	}
}
