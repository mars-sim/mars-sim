/*
 * Mars Simulation Project
 * EntityTableTabPanel.java
 * @date 2025-12-15
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.utils.EntityLauncher;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * This is a tab panel for display a table and an optional information panel
 * on the north of the table.
 */
@SuppressWarnings("serial")
public abstract class EntityTableTabPanel<T extends Entity> extends EntityTabPanel<T> {
	
	private String tableTitle;
	private JTable table;

	/**
	 * Constructor.
	 * 
	 * @param tabTitle   the title to be displayed in the tab (may be null).
	 * @param tabIcon    the icon to be displayed in the tab (may be null).
	 * @param tabToolTip the tool tip to be displayed in the icon (may be null).
	 * @param subject    the entity to be displayed in the tab.
	 * @param context    the UI context.
	 */
	protected EntityTableTabPanel(String tabTitle, Icon tabIcon, String tabToolTip, T subject, UIContext context) {
		// Use the TabPanel constructor
		super(tabTitle, tabIcon, tabToolTip, context, subject);
	}

	/**
	 * Set the optional table title, commonly used when there is an info panel.
	 * @param title
	 */
	protected void setTableTitle(String title) {
		this.tableTitle = title;
	}

	/**
	 * Called by Unit window framework, invoked subclass for the info panel and creates a table
	 * itself.
	 */
	@Override
	protected final void buildUI(JPanel content) {
		
		// Prepare  info panel.
		JPanel infoPanel = createInfoPanel();
		if (infoPanel != null) {
			content.add(infoPanel, BorderLayout.NORTH);
		}

		// Create scroll panel for the outer table panel.
		var scrollPane = new JScrollPane();
		if (tableTitle != null) {
			addBorder(scrollPane, tableTitle);
		}

		// increase vertical mousewheel scrolling speed for this one
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		content.add(scrollPane,BorderLayout.CENTER);
		
		// Prepare table model.
		var tableModel = createModel();
		
		// Prepare table.
		table = new JTable(tableModel);
		if (tableModel instanceof EntityModel) {
			// Call up the window when clicking on a row on the table
			EntityLauncher.attach(table, getContext());
		}
		
		table.setRowSelectionAllowed(true);
		var tc = table.getColumnModel();
		setColumnDetails(tc);
		
		// Resizable automatically when its Panel resizes
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Add sorting
		table.setAutoCreateRowSorter(true);

		scrollPane.setViewportView(table);
	}

	/**
	 * Get the central table of the panel
	 */
	protected JTable getMainTable() {
		return table;
	}

	/**
	 * This method should configure the table for any special renderers or
	 * column widths. It should be overridden by subclasses.
	 * 
	 * @param columnModel Columns to be configured
	 */
	protected void setColumnDetails(TableColumnModel columnModel) {
		// Default implementation does nothing
	}

	/**
	 * This is for the model to be displayed in the table.
	 * If the return is a UnitModel then the launcher will be enabled.
	 * 
	 * @return
	 */
	protected abstract TableModel createModel();

	/**
	 * If the panel has an info panel then it is created by this method. It will be placed
	 * above the table.
	 * Subclasses can override this if info is to be displayed.
	 * 
	 * @return Could return null
	 */
	protected JPanel createInfoPanel() {
		return null;
	}
}
