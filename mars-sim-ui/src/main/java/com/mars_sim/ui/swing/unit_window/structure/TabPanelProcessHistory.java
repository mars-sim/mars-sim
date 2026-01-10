/*
 * Mars Simulation Project
 * TabPanelProcessHistory.java
 * @date 2024-09-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.mars_sim.core.data.History;
import com.mars_sim.core.process.CompletedProcess;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.JHistoryPanel;

/**
 * Tab panel showing the process history of a settlement
 */
@SuppressWarnings("serial")
class TabPanelProcessHistory extends EntityTabPanel<Settlement> implements TemporalComponent {
	private static final String ID_ICON = "history"; //$NON-NLS-1$

    private HistoryPanel historyPanel;
	
    /**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param context the UI context.
	 */
	public TabPanelProcessHistory(Settlement settlement, UIContext context) {
		// Use the TabPanel constructor
		super(
			"Process History",
			ImageLoader.getIconByName(ID_ICON),		
			"History of completed processes",
			context, settlement
		);
	}
    
	@Override
	protected void buildUI(JPanel content) {

		historyPanel = new HistoryPanel(getEntity().getProcessHistory());
		historyPanel.setPreferredSize(new Dimension(225, 100));

		content.add(historyPanel, BorderLayout.CENTER);
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		historyPanel.refresh();
	}

    /**
	 * Internal class used as model for the attribute table.
	 */
	private class HistoryPanel extends JHistoryPanel<CompletedProcess> {
		private static final ColumnSpec[] COLUMNS = {
                        new ColumnSpec("Process", String.class),
                        new ColumnSpec("Type", String.class),
                        new ColumnSpec(Msg.getString("Entity.internalPosn"), String.class)
                    };


		HistoryPanel(History<CompletedProcess> source) {
			super(source, COLUMNS);
		}

		/**
		 * Gets the individual values from a CompletedProcess.
		 * 
		 * @param value Process rendered
		 * @param columnIndex Column
		 */
		@Override
		protected Object getValueFrom(CompletedProcess value, int columnIndex) {
            switch(columnIndex) {
                case 0: return value.process();
                case 1: return value.type();
                case 2: return value.buildingName();
                default: return null;
            }
		}

		/**
		 * Creates a tooltip of the ProcessInfo.
		 * 
		 * @param value Value to be rendered as tooltip
		 * @return
		 */
		@Override
		protected String getTooltipFrom(CompletedProcess value) {
			if (value == null) {
				return null;
			}
			return value.process();
		}
	}
}
