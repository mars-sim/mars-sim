/*
 * Mars Simulation Project
 * HistoryTabPanel.java
 * @date 2026-09-30
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.data.History;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.MsgContext;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.JHistoryPanel;

/**
 * Displays the history of an entity in a table using the JHistoryPanel component.
 */
@SuppressWarnings("serial")
public class HistoryTabPanel extends EntityTabPanel<Entity> implements EntityListener{

	private static final String LOG_ICON = "log";

	private LogPanel statusPanel;
	private History<MsgContext> history;
	
	public HistoryTabPanel(Entity entity, History<MsgContext> history, UIContext context) {
		// Use TabPanel constructor.
		super(
			Msg.getString("HistoryTabPanel.title"),
			ImageLoader.getIconByName(LOG_ICON), null,
			context, entity
		);

		this.history = history;
	}

	@Override
	protected void buildUI(JPanel content) {
		
		statusPanel = new LogPanel(history);
		statusPanel.setPreferredSize(new Dimension(225, 100));
		content.add(statusPanel, BorderLayout.CENTER);

		// Update will refresh data
		statusPanel.refresh();
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private static class LogPanel extends JHistoryPanel<MsgContext> {
		private static final ColumnSpec[] COLUMNS = {new ColumnSpec(Msg.getString("entityhistory.event"), String.class)};

		LogPanel(History<MsgContext> source) {
			super(source, COLUMNS);
		}

		@Override
		protected Object getValueFrom(MsgContext value, int columnIndex) {
			return value.getMessage();
		}
	}

	/**
	 * Monitor changes to history.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (EntityEventType.HISTORY_EVENT.equals(event.getType())) {
			statusPanel.refresh();
		}
	}
}