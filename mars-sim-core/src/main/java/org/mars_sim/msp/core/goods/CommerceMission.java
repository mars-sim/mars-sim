/*
 * Mars Simulation Project
 * CommerceMission.java
 * @date 2022-07-19
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.structure.Settlement;

/**
 * A mission that is undertaking Commerce between 2 Settlemments
 */
public interface CommerceMission {

    /**
     * Settlement starting the commerce action.
     */
    Settlement getStartingSettlement();

    /**
     * Settlement trading with
     */
    Settlement getTradingSettlement();

}
