package com.mars_sim.console.chat.simcommand.robot;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.console.chat.simcommand.unit.AbstractUnitCommand;
import com.mars_sim.core.Unit;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.SystemCondition;

/**
 * This command details the System details of a Robot
 */
public class SystemCommand extends AbstractUnitCommand {

    private static final String KWH_FORMAT = "%.1f kWh";
    private static final String KW_FORMAT = "%,.1f kW";

    public static final SystemCommand SYSTEM = new SystemCommand();

    private SystemCommand() {
        super(RobotChat.ROBOT_GROUP, "sy", "system", "System state");
    }

    @Override
    protected boolean execute(Conversation context, String input, Unit source) {
        if (!(source instanceof Robot)) {
            context.println("Unit is not a Robot");
            return false;
        }

        StructuredResponse response = new StructuredResponse();
        Robot robot = ((Robot) source);
        SystemCondition sc = robot.getSystemCondition();
        response.appendLabeledString("Type", robot.getRobotType().getName());
        response.appendLabeledString("Model", robot.getModel());

        response.appendLabeledString("Battery Power", String.format(CommandHelper.PERC_FORMAT,
                                                                                        sc.getBatteryState()));
        response.appendLabeledString("Low Power Threshold", String.format(CommandHelper.PERC_FORMAT,
                                                                                        sc.getLowPowerPercent()));
        response.appendLabeledString("Recommended Charging Threshold", String.format(CommandHelper.PERC_FORMAT,
                                                                                        sc.getRecommendedThreshold()));
        response.appendLabeledString("Battery Capacity", String.format(KWH_FORMAT, sc.getBatteryCapacity()));   
        response.appendLabeledString("Charging", sc.isCharging() ? "Yes" : "No");
        response.appendLabeledString("Standby Power", String.format(KW_FORMAT, sc.getStandbyPowerConsumption()));                                                                                                                                                             

        if (robot.getOccupiedStation() != null) {
            response.appendLabeledString("Occupied Station", robot.getOccupiedStation().getBuilding().getName());
        }

        context.println(response.getOutput());
        return true;
    }
}
