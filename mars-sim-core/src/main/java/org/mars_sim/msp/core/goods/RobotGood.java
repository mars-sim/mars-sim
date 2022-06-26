/*
 * Mars Simulation Project
 * RobotGood.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;

/**
 * This class represents how a Robot can be traded.
 */
class RobotGood extends Good {
	
	private static final int ROBOT_VALUE = 200;

    private RobotType robotType;

    public RobotGood(RobotType type) {
        super(type.getName(), RobotType.getResourceID(type));
        this.robotType = type;
    }

    @Override
    public GoodCategory getCategory() {
        return GoodCategory.ROBOT;
    }

    @Override
    public double getMassPerItem() {
        return Robot.EMPTY_MASS;
    }

    @Override
    public GoodType getGoodType() {
        // TODO Must be a better way to use GoodType
        switch(robotType) {
            case CHEFBOT: return GoodType.CHEFBOT;
            case CONSTRUCTIONBOT: return GoodType.CONSTRUCTIONBOT;
            case DELIVERYBOT: return GoodType.DELIVERYBOT;
            case GARDENBOT: return GoodType.GARDENBOT;
            case MAKERBOT: return GoodType.MAKERBOT;
            case MEDICBOT: return GoodType.MEDICBOT;
            case REPAIRBOT: return GoodType.REPAIRBOT;
            case UNKNOWN: break;
        }

        throw new IllegalStateException("Cannot mapt robot type " + robotType + " to GoodType");
    }

    @Override
    protected double computeCostModifier() {
        return ROBOT_VALUE;
    }
}
