/*
 * Mars Simulation Project
 * MonitorTab.java
 * @date 2022-07-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;

import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.monitor.FilteredTableModel.Filter;

/**
 * This class represents an abstraction of a view displayed in the Monitor
 * Window. The view is displayed inside a tab panel within the window and
 * depends on a UnitTableModel
 */
@SuppressWarnings("serial")
public abstract class MonitorTab extends JPanel {

	/** Model providing the data. */
	private MonitorModel model;
	private Icon icon;
	private boolean mandatory;
	private boolean ownModel = true;

	/**
	 * The constructor that creates a view within a tab displaying the specified model.
	 *
	 * @param model     The model of entities to display.
	 * @param mandatory This view is a mandatory view can can not be removed.
	 * @param ownModel The model is owned by this tab and not shared
	 * @param icon      Iconic representation.
	 */
	protected MonitorTab(MonitorModel model, boolean mandatory, boolean ownModel, Icon icon) {
		this.model = model;
		this.icon = icon;
		this.mandatory = mandatory;
		this.ownModel = ownModel;

		this.setOpaque(false);

		// Create a panel
		setLayout(new BorderLayout());
	}

	/**
	 * Removes this view.
	 */
	public void removeTab() {
		if (ownModel) {
			model.release();
		}
		model = null;
	}

	/**
	 * Displays property window controlling this view.
	 * @param context main window of simulation.
	 */
	public abstract void displayProps(UIContext context);

	/**
	 * Gets the tab count string.
	 */
	public String getCountString() {
		return model.getCountString();
	}

	/**
	 * Gets the icon associated with this view.
	 *
	 * @return Icon for this view
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * Gets the associated model.
	 *
	 * @return Monitored model associated to the tab.
	 */
	public MonitorModel getModel() {
		return model;
	}

	/**
	 * Gets the mandatory state of this view.
	 *
	 * @return Mandatory view.
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * Default implementation returns false.
	 * @return Does this tab support filters?
	 */
    public boolean isFilterable() {
        return false;
    }

	/**
	 * Get the filters supported by this tab. The default implementation returns an empty list.
	 */
	public List<Filter> getFilters() {
		return Collections.emptyList();
	}

    public boolean isEntityDriven() {
        return false;
    }
}
