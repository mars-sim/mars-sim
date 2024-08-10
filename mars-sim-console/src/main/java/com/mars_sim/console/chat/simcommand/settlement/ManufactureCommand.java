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
import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.Manufacture;

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
			response.appendLabelledDigit("Printers In Use", workshop.getNumPrintersInUse());
			response.appendLabeledString("Processes Active", workshop.getCurrentTotalProcesses() + "/" + workshop.getMaxProcesses());
			List<ManufactureProcess> processes = workshop.getProcesses();
			if (!processes.isEmpty()) {
				response.appendTableHeading("Process", 42, "Work Left", "Process Left");
				for (ManufactureProcess m : processes) {
					response.appendTableRow(m.getInfo().getName(),
										m.getProcessTimeRemaining(),
										m.getWorkTimeRemaining());
				}
			}
			response.appendBlankLine();
		}
		
		context.println(response.getOutput());
		
		return true;
	}

}
