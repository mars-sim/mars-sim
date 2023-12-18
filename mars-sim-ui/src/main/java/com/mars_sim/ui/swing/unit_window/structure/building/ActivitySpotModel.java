/*
 * Mars Simulation Project
 * ActivitySpotModel.java
 * @date 2023-12-17
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.structure.building.function.ActivitySpot;
import com.mars_sim.ui.swing.utils.UnitModel;

/**
 * Is a table model for a collection of activity spots. Renders the name, position and assignment.
 * Can also be used to launcher the entity launcher.
 */
public class ActivitySpotModel extends AbstractTableModel implements UnitModel {

    private static final int NAME_COL = 0;
    private static final int POS_COL = 1;
    private static final int WORK_COL = 2;
    
    private UnitManager um;
    private List<ActivitySpot> spots;

    public ActivitySpotModel(Set<ActivitySpot> activitySpots, UnitManager unitManager) {
        spots = new ArrayList<>(activitySpots);
        this.um = unitManager;
    }

    @Override
    public int getRowCount() {
        return spots.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ActivitySpot sp = spots.get(rowIndex);
        String result = null;
        switch(columnIndex) {
            case NAME_COL: result = sp.getName();
                        break;
            case POS_COL: result = sp.getPos().getShortFormat(); 
                        break;
            case WORK_COL: {
                Unit u = getAssociatedUnit(rowIndex);
                if (u != null) {
                    result = u.getName();
                }
            } break;
            default:
                break;
        }

        return result;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        return switch(column) {
            case NAME_COL -> "Name";
            case POS_COL -> "Position";
            case WORK_COL -> "Allocation";
            default -> "";
        };
    }

    @Override
    public Unit getAssociatedUnit(int row) {
        ActivitySpot sp = spots.get(row);
        int id = sp.getID();
        if (id >= 0) {
            return um.getUnitByID(id);
        }
        return null;
    }

}
