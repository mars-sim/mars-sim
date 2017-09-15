/**
 * Mars Simulation Project
 * MonitorModel.java
 * @version 3.1.0 2017-09-14
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import javax.swing.table.TableModel;

/**
 * This defines a table model for use in the Monitor tool.
 * The subclasses on this model could provide data on any Entity within the
 * Simulation. This interface defines simple extra method that provide a richer
 * interface for the Monitor window to be based upon.
 */
interface MonitorModel extends TableModel {

	/**
	 * Get the name of this model. The name will be a description helping
	 * the user understand the contents.
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
	 * Has this model got a natural order that the model conforms to. If this
	 * value is true, then it implies that the user should not be allowed to
	 * order.
	 */
	public boolean getOrdered();

	/**
	 * Prepares the model for deletion.
	 */
	public void destroy();

	/**
	 * Gets the model count string.
	 */
	public String getCountString();
}