/*
 * Mars Simulation Project
 * SettlementPOI.java
 * @date 2026-09-19
 * @author Barry Evans
 */
package com.mars_sim.core.map.location;

import com.mars_sim.core.Entity;
import com.mars_sim.core.structure.Settlement;

/**
 * This class represents a Point of Interest (POI) within a settlement. It has a position and potentially other attributes.
 */
public interface SettlementPOI extends Entity {

    /**
     * The local position of the POI within the settlement. This may return null if the object is not currently
     * within a Settlement.
     * @return the local position of the POI within the settlement.
     */
    LocalPosition getPosition();

    /**
     * The Settlement where the POI resides.
     * @return the Settlement where the POI resides. Could be null if not in a Settlement.
     */
    Settlement getSettlement();
}
