/**
 * Mars Simulation Project
 * SalvagaInfo.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Information about the salvage of a particular salvagable item.
 */
public class SalvageInfo implements Serializable {

	private static final long serialVersionUID = 1L;
    // Data members
    private int settlementID;

	private Salvagable item;
    private SalvageProcessInfo processInfo;
    private MarsClock startTime;
    private MarsClock finishTime;

    private Map<Integer, Integer> partsSalvaged;
    
	private static UnitManager unitManager = Simulation.instance().getUnitManager();

    /**
     * Constructor
     * @param item the salvaged item.
     * @param processInfo the salvage process info.
     */
    public SalvageInfo(Salvagable item,  SalvageProcessInfo processInfo, int settlementID) {
        this.item = item;
        this.processInfo = processInfo;
        this.settlementID = settlementID;
        startTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
        finishTime = null;
        partsSalvaged = new HashMap<>(processInfo.getPartSalvageList().size());
    }
    
    /**
     * Finish the salvage.
     * @param partsSalvaged a map of the parts salvaged and their number or an empty map if none.
     */
    public void finishSalvage(Map<Integer, Integer> partsSalvaged) {
        this.partsSalvaged = partsSalvaged;
        finishTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
    }
    
    /**
     * Gets the salvagable item.
     * @return item.
     */
    public Salvagable getItem() {
        return item;
    }
    
    /**
     * Gets the salvage process info.
     * @return process info.
     */
    public SalvageProcessInfo getProcessInfo() {
        return processInfo;
    }
    
    /**
     * Gets the time when the salvage process is started.
     * @return start time.
     */
    public MarsClock getStartTime() {
        return startTime;
    }
    
    /**
     * Gets the time when the salvage process is finished.
     * @return finish time or null if not finished yet.
     */
    public MarsClock getFinishTime() {
        return finishTime;
    }
    
    /**
     * Gets a map of the parts salvaged and their number from this item.
     * @return map of parts and their number or empty map if salvage not finished.
     */
    public Map<Integer, Integer> getPartsSalvaged() {
        return new HashMap<>(partsSalvaged);
    }
    
    /**
     * Gets the settlement where the salvage took or is taking place.
     * @return settlement
     */
    public Settlement getSettlement() {
        return unitManager.getSettlementByID(settlementID);
    }
	
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        item = null;
        processInfo.destroy();
        processInfo = null;
        startTime = null;
        finishTime = null;
        partsSalvaged.clear();
        partsSalvaged = null;
    }
}