package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Represents a connection to a Settlement.
 */
public class SettlementChat extends ConnectedUnitCommand {
	private static final List<ChatCommand> COMMANDS = Arrays.asList();

	public static final String SETTLEMENT_GROUP = "Settlement";

	private Settlement settlement;

	public SettlementChat(Settlement settlement) {
		super(settlement, COMMANDS);
		this.settlement = settlement;
	}

	@Override
	public String getIntroduction() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Connected to Settlement ");
		buffer.append(settlement.getName());
		buffer.append(" with the objective ");
		buffer.append(settlement.getObjective());
		buffer.append(System.lineSeparator());

		buffer.append("Location :");
		buffer.append(settlement.getCoordinates().getCoordinateString());
		buffer.append(System.lineSeparator());
		
		buffer.append("Population is people:");
		buffer.append(settlement.getNumCitizens());
		
		return buffer.toString();
	}
}
