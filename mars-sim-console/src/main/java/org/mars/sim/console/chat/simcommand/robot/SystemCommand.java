package org.mars.sim.console.chat.simcommand.robot;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars.sim.console.chat.simcommand.unit.AbstractUnitCommand;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.SystemCondition;

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
        response.appendLabeledString("Min. Charging Threshold", String.format(CommandHelper.PERC_FORMAT,
                                                                                        sc.getMinimumChargeBattery()));
        response.appendLabeledString("Battery Capacity", String.format(KWH_FORMAT, sc.getBatteryCapacity()));   
        response.appendLabeledString("Charging", sc.isCharging() ? "Yes" : "No");
        response.appendLabeledString("Standby Power", String.format(KW_FORMAT, sc.getStandbyPowerConsumption()));                                                                                                                                                             

        if (robot.getStation() != null) {
            response.appendLabeledString("Station", robot.getStation().getBuilding().getName());
        }

        context.println(response.getOutput());
        return true;
    }
}
