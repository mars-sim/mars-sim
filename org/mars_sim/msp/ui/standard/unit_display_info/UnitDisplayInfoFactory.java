/**
 * Mars Simulation Project
 * UnitDisplayInfoFactory.java
 * @version 2.75 2003-07-16
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_display_info;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.equipment.Equipment;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.*;

/**
 * Factory for unit display info beans.
 */
public final class UnitDisplayInfoFactory {
    
    // Static bean instances.
    private static UnitDisplayInfo settlementBean = new SettlementDisplayInfoBean();
    private static UnitDisplayInfo personBean = new PersonDisplayInfoBean();
    private static UnitDisplayInfo transportRoverBean = new TransportRoverDisplayInfoBean();
    private static UnitDisplayInfo explorerRoverBean = new ExplorerRoverDisplayInfoBean();
    private static UnitDisplayInfo equipmentBean = new EquipmentDisplayInfoBean();
    
    /**
     * Private constructor
     */
    private UnitDisplayInfoFactory() {}
    
    /**
     * Gets a display information about a given unit.
     *
     * @param unit the unit to display.
     * @return unit display info instance.
     */
    public static UnitDisplayInfo getUnitDisplayInfo(Unit unit) {
        if (unit instanceof Settlement) return settlementBean;
        else if (unit instanceof Person) return personBean;
        else if (unit instanceof TransportRover) return transportRoverBean;
        else if (unit instanceof ExplorerRover) return explorerRoverBean;
        else if (unit instanceof Equipment) return equipmentBean;
        else return null;
    }
}
