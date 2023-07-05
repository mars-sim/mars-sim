/*
 * Mars Simulation Project
 * MonitorTab.java
 * @date 2022-07-02
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

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
	private boolean navigatable;
	private boolean filtered;
	private boolean hasEntity;

	/**
	 * Tee constructor that creates a view within a tab displaying the specified model.
	 *
	 * @param model     The model of entities to display.
	 * @param mandatory This view is a mandatory view can can not be removed.
	 * @param ownModel The model is owned by this tab and not shared
	 * @param icon      Iconic representation.
	 */
	public MonitorTab(MonitorModel model, boolean mandatory, boolean ownModel, Icon icon) {
		this.model = model;
		this.icon = icon;
		this.mandatory = mandatory;
		this.ownModel = ownModel;

		this.setOpaque(false);

		// Create a panel
		setLayout(new BorderLayout());
	}

	/**
	 * Remove this view.
	 */
	public void removeTab() {
		if (ownModel) {
			model.destroy();
		}
		model = null;
	}

	/**
	 * Display property window controlling this view.
	 */
	public abstract void displayProps(MainDesktopPane desktop);

	/**
	 * Gets the tab count string.
	 */
	public String getCountString() {
		return model.getCountString();
	}

	/**
	 * Get the icon associated with this view.
	 *
	 * @return Icon for this view
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * Get the associated model.
	 *
	 * @return Monitored model associated to the tab.
	 */
	public MonitorModel getModel() {
		return model;
	}

	/**
	 * Get the mandatory state of this view
	 *
	 * @return Mandatory view.
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	protected void setNavigatable(boolean b) {
		navigatable = b;
	}

    public boolean isNavigatable() {
        return navigatable;
    }

	protected void setFilterable(boolean b) {
		filtered = b;
	}

    public boolean isFilterable() {
        return filtered;
    }

	protected void setEntityDriven(boolean b) {
		hasEntity = b;
	}

    public boolean isEntityDriven() {
        return hasEntity;
    }

	/**
	 * Get the Coordinates that best represent the selected rows
	 * @return Cooridnates, maybe null
	 */
    public Coordinates getSelectedCoordinates() {
        return null;
    }
}
