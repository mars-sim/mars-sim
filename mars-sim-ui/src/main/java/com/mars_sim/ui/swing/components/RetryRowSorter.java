/*
 * Mars Simulation Project
 * RetryRowSorter.java
 * @date 2026-07023
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * This is a custom TableRowSorter that retries setting sort keys in case of a RuntimeException.
 * This is useful in scenarios where the underlying data model may change during sorting, leading to exceptions.
 */
public class RetryRowSorter<T extends TableModel> extends TableRowSorter<T> {

    private static final Logger logger = Logger.getLogger(RetryRowSorter.class.getName());
    private static final int RETRY_COUNT = 2;

    public RetryRowSorter(T model) {
        super(model);
        setSortsOnUpdates(false);
    }

    /**
     * Set the sort keys for the sorter, retrying in case of a RuntimeException.
     * @param sortKeys the list of sort keys to set
     */
    @Override
    public void setSortKeys(List<? extends SortKey> sortKeys) {
        for(int i = 0; i < RETRY_COUNT; i++) {
            try {
                super.setSortKeys(sortKeys);
                return; // Success, exit the method
            } catch (RuntimeException e) {
                // Log the exception or handle it as needed
                logger.warning("RetryRowSorter: Exception occurred while setting sort keys. " + e.getMessage());
            }
        }
    }

    
    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public void rowsUpdated(int firstRow, int endRow) {
        for(int i = 0; i < RETRY_COUNT; i++) {
            try {
                super.rowsUpdated(firstRow, endRow);
                return; // Success, exit the method
            } catch (RuntimeException e) {
                // Log the exception or handle it as needed
                logger.warning("RetryRowSorter: Exception occurred while updating rows. " + e.getMessage());
            }
        }
    }
}
