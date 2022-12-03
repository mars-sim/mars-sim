/*
 * Mars Simulation Project
 * BacklogTab.java
 * @date 2022-12-02
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This class displays the backlog of SettlementTasks at settlement displayed within
 * the Monitor Window.
 */
@SuppressWarnings("serial")
public class BacklogTab extends TableTab {
	private static final String TASK_ICON = Msg.getString("icon.task"); //$NON-NLS-1$

	/**
	 * constructor.
	 *
	 * @param selectedSettlement
	 * @param window {@link MonitorWindow} the containing window.
	 */
	public BacklogTab(Settlement selectedSettlement, final MonitorWindow window) {
		// Use TableTab constructor
		super(window, new BacklogTableModel(selectedSettlement), true, false,
			  TASK_ICON);

		TableColumnModel m = table.getColumnModel();
		m.getColumn(BacklogTableModel.SCORE_COL).setCellRenderer(DIGIT2_RENDERER);
			  
		super.adjustColumnWidth(table);
	}
}
