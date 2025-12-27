/*
 * Mars Simulation Project
 * ListenerTabPanel.java
 * @date 2025-12-27
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow;

import java.awt.BorderLayout;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.mars_sim.core.EntityListener;
import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This tab panel displays the active listeners registered on a MonitorableEntity.
 */
@SuppressWarnings("serial")
public class ListenerTabPanel extends EntityTabPanel<MonitorableEntity> {
	
	private static final String TAB_ICON = "info";
	
	private JList<String> listenerList;
	private DefaultListModel<String> listModel;
	private JLabel lastRefreshLabel;
	private JScrollPane scrollPane;
	
	/**
	 * Constructor.
	 * 
	 * @param entity the MonitorableEntity to display.
	 * @param context the UI context.
	 */
	public ListenerTabPanel(MonitorableEntity entity, UIContext context) {
		super(
			Msg.getString("EntityListeners.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(TAB_ICON),
			Msg.getString("EntityListeners.tooltip"), //$NON-NLS-1$
			context, entity
		);
	}

	@Override
	protected void buildUI(JPanel content) {
		
		// Create north panel for the refresh time
		JPanel northPanel = new JPanel(new BorderLayout());
		content.add(northPanel, BorderLayout.NORTH);
		
		AttributePanel infoPanel = new AttributePanel();
		northPanel.add(infoPanel, BorderLayout.CENTER);
		
		lastRefreshLabel = new JLabel();
		infoPanel.addLabelledItem(Msg.getString("EntityListeners.lastRefresh"), lastRefreshLabel);
		
		// Create list model and list for displaying listeners
		listModel = new DefaultListModel<>();
		listenerList = new JList<>(listModel);
		
		scrollPane = new JScrollPane(listenerList);
		addBorder(scrollPane, Msg.getString("EntityListeners.count") + ": 0");
		content.add(scrollPane, BorderLayout.CENTER);
		
		// Initial population of the list
		refreshUI();
	}
	
	/**
	 * Refreshes the UI by updating the listener list and the Mars time.
	 */
	private void refreshUI() {
		// Update the Mars time
		MarsTime now = getContext().getSimulation().getMasterClock().getMarsTime();
		lastRefreshLabel.setText(now.getDateTimeStamp());
		
		// Get the current listeners
		Set<EntityListener> listeners = getEntity().getListeners();
		
		// Clear the list model
		listModel.clear();
		
		// Add each listener to the list
		for (EntityListener listener : listeners) {
			// Use the full class name
			String listenerInfo = listener.getClass().getName();
			listModel.addElement(listenerInfo);
		}
		
		// Update the border with the count
		scrollPane.setBorder(SwingHelper.createLabelBorder(
			Msg.getString("EntityListeners.count") + ": " + listeners.size())
		);
	}
	
	@Override
	public void update() {
		refreshUI();
	}
}
