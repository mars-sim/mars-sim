/*
 * Mars Simulation Project
 * CommerceMission.java
 * @date 2022-07-19
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.structure.Settlement;

/**
 * A mission that is undertaking commerce between 2 settlements
 */
public interface CommerceMission {

    /**
     * Common Mission Status for commerce
     */
    public static final MissionStatus NO_TRADING_SETTLEMENT = new MissionStatus("Mission.status.noTradeSettlement");

    /**
     * Settlement starting the commerce action.
     */
    Settlement getStartingSettlement();

    /**
     * Settlement trading with
     */
    Settlement getTradingSettlement();
}
