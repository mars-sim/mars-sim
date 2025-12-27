/*
 * Mars Simulation Project
 * ListenerTabPanel.java
 * @date 2025-12-27
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.mars_sim.core.EntityListener;
import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * This tab panel displays the active listeners registered on a MonitorableEntity.
 */
@SuppressWarnings("serial")
public class ListenerTabPanel extends EntityTabPanel<MonitorableEntity> {
	
	private static final String TAB_ICON = "info";
	private static final String TAB_TITLE = "Listeners";
	private static final String TAB_TOOLTIP = "Active Event Listeners";
	private static final String LAST_REFRESH_LABEL = "Last Refresh";
	private static final String REFRESH_BUTTON_TEXT = "Refresh";
	
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
			TAB_TITLE,
			ImageLoader.getIconByName(TAB_ICON),
			TAB_TOOLTIP,
			context, entity
		);
	}

	@Override
	protected void buildUI(JPanel content) {
		
		// Create north panel for the refresh time and button
		JPanel northPanel = new JPanel(new BorderLayout());
		content.add(northPanel, BorderLayout.NORTH);
		
		AttributePanel infoPanel = new AttributePanel();
		northPanel.add(infoPanel, BorderLayout.CENTER);
		
		// Create panel for last refresh time and refresh button
		JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lastRefreshLabel = new JLabel();
		refreshPanel.add(lastRefreshLabel);
		
		JButton refreshButton = new JButton(REFRESH_BUTTON_TEXT);
		refreshButton.addActionListener(e -> refreshUI());
		refreshPanel.add(refreshButton);
		
		infoPanel.addLabelledItem(LAST_REFRESH_LABEL, refreshPanel);
		
		// Create list model and list for displaying listeners
		listModel = new DefaultListModel<>();
		listenerList = new JList<>(listModel);
		
		// Configure scrollPane with horizontal scrollbar
		scrollPane = new JScrollPane(listenerList);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
		Set<EntityListener> listeners = getEntity().getEntityListeners();
		
		// Clear the list model
		listModel.clear();
		
		// Add each listener to the list using toString()
		for (EntityListener listener : listeners) {
			String listenerInfo = listener.toString();
			listModel.addElement(listenerInfo);
		}
	}
	
	@Override
	public void update() {
		refreshUI();
	}
}
