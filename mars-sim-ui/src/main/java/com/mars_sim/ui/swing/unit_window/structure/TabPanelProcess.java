/*
 * Mars Simulation Project
 * TabPanelProcess.java
 * @date 2024-03-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.mars_sim.core.data.History;
import com.mars_sim.core.process.CompletedProcess;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.JHistoryPanel;
import com.mars_sim.ui.swing.utils.ProcessInfoRenderer;

/**
 * Tab panel showing the process history of a settlement
 */
@SuppressWarnings("serial")
public class TabPanelProcess extends TabPanel {
	private static final String ID_ICON = "time"; //$NON-NLS-1$

    private HistoryPanel historyPanel;
    private Settlement settlement;
	
    /**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelProcess(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			"Process History",
			ImageLoader.getIconByName(ID_ICON),		
			"History of completed processes",
			desktop
		);

		this.settlement = settlement;
	}
    
	@Override
	protected void buildUI(JPanel content) {

		historyPanel = new HistoryPanel(settlement.getProcessHistory());
		historyPanel.setPreferredSize(new Dimension(225, 100));

		content.add(historyPanel, BorderLayout.CENTER);

		update();
	}

    @Override
    public void update() {
        historyPanel.refresh();
    }

    /**
	 * Internal class used as model for the attribute table.
	 */
    @SuppressWarnings("serial")
	private class HistoryPanel extends JHistoryPanel<CompletedProcess> {
		private static final ColumnSpec[] COLUMNS = {
                        new ColumnSpec("Process", String.class),
                        new ColumnSpec("Type", String.class),
                        new ColumnSpec("Location", String.class)
                    };


		HistoryPanel(History<CompletedProcess> source) {
			super(source, COLUMNS);
		}

		/**
		 * Get the individual values from a CompletedProcess
		 * @param value Process rendered
		 * @param columnIndex Column
		 */
		@Override
		protected Object getValueFrom(CompletedProcess value, int columnIndex) {
            switch(columnIndex) {
                case 0: return value.process().getName();
                case 1: return value.type();
                case 2: return value.buildingName();
                default: return null;
            }
		}

		/**
		 * Create a tooltip of the ProcessInfo
		 * @param value Value to be rendered as tooltip
		 * @return
		 */
		@Override
		protected String getTooltipFrom(CompletedProcess value) {
			if (value == null) {
				return null;
			}
			return ProcessInfoRenderer.getToolTipString(value.process());
		}
	}
}
