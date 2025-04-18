/*
 * Mars Simulation Project
 * SalvageProcess.java
 * @date 2024-08-10
 * @author Scott Davis
 */
package com.mars_sim.core.manufacture;

import com.mars_sim.core.building.function.Manufacture;

/**
 * A process for salvaging a piece of equipment or vehicle.
 */
public class SalvageProcess extends WorkshopProcess {
	
	// Data members.    
    private Salvagable salvagedUnit;
        
    /**
     * Constructor.
     * 
     * @param info information about the salvage process.
     * @param workshop the manufacturing workshop where the salvage is taking place.
     */
    public SalvageProcess(SalvageProcessInfo info, Manufacture workshop, Salvagable salvagedUnit) {
        super(workshop, info);
        this.salvagedUnit = salvagedUnit;
    }

    /**
     * Gets the salvaged unit.
     * 
     * @return salvaged unit.
     */
    public Salvagable getSalvagedUnit() {
        return salvagedUnit;
    }
    
    /**
     * Prepares object for garbage collection.
     */
    @Override
    public void destroy() {
        super.destroy();
        salvagedUnit = null;
    }

    /**
     * Stop this salvage process.
     * @param premature is the process being stopped prematurely?
     */
    @Override
    public void stopProcess(boolean premature) {
        getWorkshop().endSalvageProcess(this, premature);
    }
}
