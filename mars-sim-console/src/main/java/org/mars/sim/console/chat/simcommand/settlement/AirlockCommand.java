package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

public class AirlockCommand extends AbstractSettlementCommand {

	public final static ChatCommand AIRLOCK = new AirlockCommand();
	
	private AirlockCommand() {
		super("ai", "airlocks", "Status of all airlocks");

	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		
		BuildingManager bm = settlement.getBuildingManager();
		List<Building> i = bm.getBuildings(FunctionType.EVA);
		
		response.appendTableHeading("Building", 16, "State", 14, "Active", "Operator", PERSON_WIDTH,
									"Inner Door", "Outer Door");
		for (Building building : i) {
			Airlock airlock = building.getEVA().getAirlock();
			response.appendTableRow(building.getNickName(), airlock.getState().name(),
									(airlock.isActivated() ? "Yes" : "No"),
									airlock.getOperatorName(),
									(airlock.isInnerDoorLocked() ? "Locked" : "Unlocked"),
									(airlock.isOuterDoorLocked() ? "Locked" : "Unlocked"));
		}

		context.println(response.getOutput());
		return true;
	}
}
