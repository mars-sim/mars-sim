/*
 * Mars Simulation Project
 * EntityTabPanel.java
 * @date 2025-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.Entity;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.monitor.EntityMonitorModel;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;

/**
 * This is a typesafe Panel to be used for Entity Content Panel. 
 * It represents a single Entity of type T.
 * The panel has a central content panel that subclasses must implement to build the UI as 
 * this uses a lazy loading approach to building the UI. It is only built when it is
 * first displayed.
 */
@SuppressWarnings("serial")
public abstract class EntityTabPanel<T extends Entity>  {

    // Name of the icon to use for the general tab
    protected static final String GENERAL_ICON = "info";
    protected static final String GENERAL_TITLE = Msg.getString("EntityGeneral.title");
    protected static final String GENERAL_TOOLTIP = Msg.getString("EntityGeneral.tooltip");

	private boolean isUIDone = false;
	
	private String tabTitle;
	private String tabToolTip;
	private Icon tabIcon;
	
	private JPanel mainPanel;
	private JPanel topContentPanel;
	private JPanel centerContentPanel;

	private UIContext context;
    private T entity;
	
	/**
	 * Constructor.
	 *
	 * @param tabTitle   the title to be displayed in the tab (may be null).
	 * @param tabIcon    the icon to be displayed in the tab (may be null).
	 * @param tabToolTip the tool tip to be displayed in the icon (may be null).
	 * @param context    the UI context.
	 */
    protected EntityTabPanel(String tabTitle, Icon tabIcon, String tabToolTip, UIContext context, T entity) {

		// Eventually tabTitle MUST be mandatory once all have been converted to UIContext
		if (tabTitle == null && tabToolTip == null) {
			throw new IllegalArgumentException("TabPanel must have either a title or a tool tip");
		}
		// Initialize data members
		this.tabTitle = (tabTitle != null) ? tabTitle : tabToolTip;
		this.tabIcon = tabIcon;
        this.entity = entity;
		this.tabToolTip = (tabToolTip != null) ? tabToolTip : tabTitle;
		this.context = context;

		mainPanel = new JPanel(new BorderLayout());

		// Create top content panel
		topContentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		mainPanel.add(topContentPanel, BorderLayout.NORTH);
				
		// Create center content panel
		centerContentPanel = new JPanel(new BorderLayout(0, 10));
		centerContentPanel.setBorder(new EmptyBorder(2, 2, 4, 2));

		// Create the view panel
		JScrollPane viewPanel = new JScrollPane();
		viewPanel.createVerticalScrollBar();
		viewPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		viewPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		viewPanel.setViewportView(centerContentPanel);
		mainPanel.add(viewPanel, BorderLayout.CENTER);
	}

    /**
     * Get the entity this panel is displaying.
     * @return
     */
    protected T getEntity() {
        return entity;
    }

    /**
	 * Displays a new Unit model in the monitor window.
	 *
	 * @param model the new model to display
	 */
	protected void showModel(EntityMonitorModel<?> model) {
		var cw = context.openToolWindow(MonitorWindow.NAME);
		((MonitorWindow)cw).displayModel(model);
	}

	/**
	 * Are all the UI element in place ?
	 * 
	 * @return
	 */
 	public boolean isUIDone() {
		return isUIDone;
	}
	
	/**
	 * This tab panel is being displayed for the first time, so initialize the UI elements.
	 */
	public void initializeUI() {
		if (!isUIDone) {
			// Create label in top panel
			String topLabel = getTabTitle();
			JLabel titleLabel = new JLabel(topLabel, SwingConstants.CENTER);
			StyleManager.applyHeading(titleLabel);

			topContentPanel.add(titleLabel);
			
			buildUI(centerContentPanel);
			
			isUIDone = true;
		}	
	}
	
	/**
     * Get the UI context.
     * @return
     */
    protected UIContext getContext() {
        return context;
    }

	/**
	 * Builds the UI element using the 3 components.
	 * 
	 * @param centerContentPanel The central panel to hold the content.
	 */
	protected abstract void buildUI(JPanel centerContentPanel);
	
	/**
	 * Gets the tab title.
	 *
	 * @return tab title or null.
	 */
	public String getTabTitle() {
		return tabTitle;
	}

	/**
	 * Gets the tab icon.
	 *
	 * @return tab icon or null.
	 */
	public Icon getTabIcon() {
		return tabIcon;
	}

	/**
	 * Gets the tab tool tip.
	 *
	 * @return tab tool tip.
	 */
	public String getTabToolTip() {
		return tabToolTip;
	}
	
	@Override
	public  String toString() {
		return entity.getName() + " : tab " + tabTitle;
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		// Do nothing BUT subclasses may override
	}

	/**
	 * Refresh the UI elements of this tab. Commonly called when the tab is selected.
	 * Can be overridden by subclasses. but should be rarely needed.
	 */
    public void refreshUI() {
        // Default does nothing
    }

	/**
	 * Get the main visual component for this tab.
	 * @return Component that is the main visual for this tab.
	 */
	JComponent getVisual() {
		return mainPanel;
	}
}
