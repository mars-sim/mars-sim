/*
 * Mars Simulation Project
 * UnitModel.java
 * @date 2023-02-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import javax.swing.table.TableModel;

import com.mars_sim.core.Unit;

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
