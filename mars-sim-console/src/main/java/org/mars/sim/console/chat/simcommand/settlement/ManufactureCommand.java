/**
 * Mars Simulation Project
 * ManufactureCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Manufacture;

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
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		List<Building> workshops = settlement.getBuildingManager().getBuildings(FunctionType.MANUFACTURE);
		
		StructuredResponse response = new StructuredResponse();
		for (Building building : workshops) {
			Manufacture workshop = building.getManufacture();
			response.appendHeading(building.getNickName());
			response.appendLabelledDigit("Printers In Use", workshop.getNumPrintersInUse());
			response.appendLabeledString("Processes Active", workshop.getCurrentProcesses() + "/" + workshop.getMaxProcesses());
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
