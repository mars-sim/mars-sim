/*
 * Mars Simulation Project
 * UnitModel.java
 * @date 2023-02-21
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.utils;

import javax.swing.table.TableModel;

import org.mars_sim.msp.core.Unit;

/**
 * This represent a TableModel that has a Unit associated with each row.
 */
public interface UnitModel extends TableModel {
    /**
     * Get ther associated Unit at a row index.
     * @param row Index
     * @return Unit associated 
     */
    public Unit getAssociatedUnit(int row);
}
