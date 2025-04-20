/*
 * Mars Simulation Project
 * ManufactureCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.structure.Settlement;

/**m
 * Command to display manufacturing in a Settlement
 * This is a singleton.
 */
public class ManufactureCommand extends AbstractSettlementCommand {

	public static final ChatCommand MANUFACTURE = new ManufactureCommand();

	private ManufactureCommand() {
		super("mf", "manufacture", "Manufacturing Processes");
	}

	/** 
	 * Outputs the current immediate location of the Unit.
	 * 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		List<Building> workshops = settlement.getBuildingManager().getBuildings(FunctionType.MANUFACTURE);
		
		StructuredResponse response = new StructuredResponse();
		for (Building building : workshops) {
			Manufacture workshop = building.getManufacture();
			response.appendHeading(building.getName());
			response.appendLabeledString("Processes Active", workshop.getCurrentTotalProcesses() + "/" + workshop.getMaxProcesses());
			var processes = workshop.getProcesses();
			if (!processes.isEmpty()) {
				response.appendTableHeading("Process", 42, "Work Left", "Process Left");
				for (var m : processes) {
					response.appendTableRow(m.getInfo().getName(),
										m.getProcessTimeRemaining(),
										m.getWorkTimeRemaining());
				}
			}
			response.appendBlankLine();
		}
		
		// Display Queue
		var mgr = settlement.getManuManager();
		response.appendHeading("Queued");
		response.appendTableHeading("Process", 42, "TechLevel", "Salvage");
		for(var qp : mgr.getQueue()) {
			response.appendTableRow(qp.getInfo().getName(), qp.getInfo().getTechLevelRequired(),
									(qp.getTarget() != null ? qp.getTarget().getName() : ""));	
		}

		context.println(response.getOutput());
		
		return true;
	}

}
