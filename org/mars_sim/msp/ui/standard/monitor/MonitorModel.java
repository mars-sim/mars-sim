package org.mars_sim.msp.ui.standard.monitor;

import java.util.ArrayList;
import javax.swing.table.TableModel;

/**
 * This defines a table modle for use in the Monitor tool.
 * The subclasses on this model could provide data on any Entity within the
 * Simulation. This interface defines simple extra method that provide a richer
 * interface for the Monitor window to be based upon.
 */
interface MonitorModel extends TableModel {

    /**
     * Get the name of this model. The name will be a description helping
     * the user understand the contents.
     *
     * @return Descriptive name.
     */
    public String getName();


    /**
     * Return the object at the specified row indexes.
     * @param row Index of the row object.
     * @return Object at the specified row.
     */
    public Object getObject(int row);

    /**
     * The Model should be updated to reflect any changes in the underlying
     * data
     */
    public void update();
}