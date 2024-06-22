/*
 * Mars Simulation Project
 * UnitModel.java
 * @date 2023-02-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import com.mars_sim.core.Entity;

/**
 * This represent a Model that has a Entity associated with each row.
 */
public interface EntityModel {
    /**
     * Get the associated Entity at a row index.
     * @param row Index
     * @return Entity associated 
     */
    public Entity getAssociatedEntity(int row);
}
