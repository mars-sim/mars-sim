/**
 * Mars Simulation Project
 * Salvagable.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import org.mars_sim.msp.core.structure.Settlement;

/**
 * An interface for salvagable items.
 */
public interface Salvagable {

    /**
     * Checks if the item is salvaged.
     * @return true if salvaged.
     */
    public boolean isSalvaged();
    
    /**
     * Indicate the start of a salvage process on the item.
     * @param info the salvage process info.
     * @param settlement the settlement where the salvage is taking place.
     */
    public void startSalvage(SalvageProcessInfo info, Settlement settlement);
    
    /**
     * Gets the salvage info.
     * @return salvage info or null if item not salvaged.
     */
    public SalvageInfo getSalvageInfo();
}