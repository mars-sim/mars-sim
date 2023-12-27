/*
 * Mars Simulation Project
 * BacklogTab.java
 * @date 2022-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Set;

import javax.swing.table.TableColumnModel;

import com.mars_sim.core.structure.Settlement;

/**
 * This class displays the backlog of SettlementTasks at settlement displayed within
 * the Monitor Window.
 */
@SuppressWarnings("serial")
public class BacklogTab extends TableTab {
	private static final String TASK_ICON = "task";

	/**
	 * constructor.
	 *
	 * @param selectedSettlement
	 * @param window {@link MonitorWindow} the containing window.
	 */
	public BacklogTab(Set<Settlement> selectedSettlement, final MonitorWindow window) {
		// Use TableTab constructor
		super(window, new BacklogTableModel(selectedSettlement), true, false,
			  TASK_ICON);

		TableColumnModel m = table.getColumnModel();
		m.getColumn(BacklogTableModel.SCORE_COL).setCellRenderer(DIGIT2_RENDERER);
			  
		adjustColumnWidth(table);
	}
}
