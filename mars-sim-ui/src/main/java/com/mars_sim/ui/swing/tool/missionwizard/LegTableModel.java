/*
 * Mars Simulation Project
 * LegTableModel.java
 * @date 2026-02-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model for displaying route legs in a route definition panel.
 * Shows coordinates, leg distance, and total distance for each waypoint.
 */
@SuppressWarnings("serial")
public class LegTableModel extends AbstractTableModel {

    private static final int NAME_COL = 0;
    private static final int COORDINATE_COL = 1;
    private static final int LEG_DISTANCE_COL = 2;
    private static final int TOTAL_DISTANCE_COL = 3;

    private final List<RoutePoint> legs = new ArrayList<>();
    
    /**
     * Gets the number of rows in the table.
     * 
     * @return number of route legs
     */
    @Override
    public int getRowCount() {
        return legs.size();
    }
    
    /**
     * Gets the number of columns in the table.
     * 
     * @return always 3
     */
    @Override
    public int getColumnCount() {
        return 4;
    }
    
    /**
     * Gets the name of a column.
     * 
     * @param column the column index
     * @return the column name
     */
    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case NAME_COL -> "Name";
            case COORDINATE_COL -> "Coordinates";
            case LEG_DISTANCE_COL -> "Leg Distance (km)";
            case TOTAL_DISTANCE_COL -> "Total Distance (km)";
            default -> "";
        };
    }
    
    /**
     * Gets the class of values in a column.
     * 
     * @param column the column index
     * @return the column class
     */
    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case NAME_COL, COORDINATE_COL -> String.class;
            case LEG_DISTANCE_COL, TOTAL_DISTANCE_COL -> Double.class;
            default -> Object.class;
        };
    }
    
    /**
     * Gets the value at a specific cell.
     * 
     * @param row the row index
     * @param column the column index
     * @return the cell value
     */
    @Override
    public Object getValueAt(int row, int column) {
        if (row >= legs.size()) {
            return null;
        }
        
        RoutePoint leg = legs.get(row);
        return switch (column) {
            case NAME_COL -> leg.getName();
            case COORDINATE_COL -> leg.getCoordinates().getFormattedString();
            case LEG_DISTANCE_COL -> leg.getLegDistance();
            case TOTAL_DISTANCE_COL -> leg.getTotalDistance();
            default -> null;
        };
    }
    
    /**
     * Adds a route leg to the table.
     * 
     * @param leg the route leg to add
     */
    public void addLeg(RoutePoint leg) {
        legs.add(leg);
        fireTableRowsInserted(legs.size() - 1, legs.size() - 1);
    }
    
    /**
     * Removes a route leg from the table by row index.
     * 
     * @param row the row index to remove
     */
    public void removeLeg(int row) {
        if (row >= 0 && row < legs.size()) {
            legs.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
    
    /**
     * Removes all route legs from the table.
     */
    public void clear() {
        int size = legs.size();
        legs.clear();
        if (size > 0) {
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    /**
     * Gets all route legs in the table.
     * 
     * @return list of route legs
     */
    public List<RoutePoint> getAllLegs() {
        return new ArrayList<>(legs);
    }
}
