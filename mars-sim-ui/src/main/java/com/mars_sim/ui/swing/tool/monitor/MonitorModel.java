/*
 * Mars Simulation Project
 * MonitorModel.java
 * @date 2024-76-29
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Set;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;

/**
 * This defines a table model for use in the Monitor tool.
 * The subclasses on this model could provide data on any Entity within the
 * Simulation. This interface defines simple extra method that provide a richer
 * interface for the Monitor window to be based upon.
 */
interface MonitorModel extends ToolTipTableModel {

	/**
	 * Gets the name of this model. The name will be a description helping
	 * the user understand the contents.
	 * 
	 * @return Descriptive name.
	 */
	public String getName();


	/**
	 * Gets the model count string.
	 */
	public String getCountString();

	/**
	 * Sets the Settlement as a filter.
	 * 
	 * @param selectedSettlement Settlement
	 * @return 
	 */
	public boolean setSettlementFilter(Set<Settlement> selectedSettlement);

	/**
	 * Sets whether the changes to the Entities should be monitor for change.
	 * 
	 * @param activate 
	 */
    public void setMonitorEntities(boolean activate);

	/**
	 * Gets the index of the Settlement column if defined. This is a special column that can be visible/hidden according
	 * to the selection.
	 * 
	 * @return
	 */
	public int getSettlementColumn();
    
	/**
	 * Prepares the model for deletion.
	 */
	public void destroy();

	/**
	 * Get any defined style for this value.
	 * @param modelIndex the index of the column.
	 * @return see ColumnSpec for the style.
	 */
    public int getColumnStyle(int modelIndex);

}
