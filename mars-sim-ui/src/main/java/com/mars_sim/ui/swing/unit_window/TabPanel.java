/*
 * Mars Simulation Project
 * TabPanel.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;

/**
 * This is the base class for all tab panels used in EntityContentPanels.
 * It provides a lazy loading of the UI and only builds the UI elements when the tab
 * is first displayed.
 */
@SuppressWarnings("serial")
public abstract class TabPanel extends JScrollPane {

	private boolean isUIDone = false;
	
	private String tabTitle;
	private String tabToolTip;
	
	private transient Icon tabIcon;
	
	// These can be made private once all tabs converted
	private JPanel topContentPanel;
	private JPanel centerContentPanel;

	private UIContext context;

	
	/**
	 * Constructor.
	 *
	 * @param tabTitle   the title to be displayed in the tab (may be null).
	 * @param tabIcon    the icon to be displayed in the tab (may be null).
	 * @param tabToolTip the tool tip to be displayed in the icon (may be null).
	 * @param context    the UI context.
	 */
	protected TabPanel(String tabTitle, Icon tabIcon, String tabToolTip, UIContext context) {
		super();

		// Eventually tabTitle MUST be mandatory once all have been converted to UIContext
		if (tabTitle == null && tabToolTip == null) {
			throw new IllegalArgumentException("TabPanel must have either a title or a tool tip");
		}
		// Initialize data members
		this.tabTitle = (tabTitle != null) ? tabTitle : tabToolTip;
		this.tabIcon = tabIcon;
		this.tabToolTip = (tabToolTip != null) ? tabToolTip : tabTitle;
		this.context = context;

		// Create the view panel
		JPanel viewPanel = new JPanel();
		viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.Y_AXIS));
		createViewport();
		setViewportView(viewPanel);
		createVerticalScrollBar();
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);

		JScrollBar vertical = getVerticalScrollBar();
		vertical.setValue(0);
		
		// Create top content panel
		topContentPanel = new JPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		topContentPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		viewPanel.add(topContentPanel, BorderLayout.NORTH);

		Box.createVerticalGlue();
		
		// Create center content panel
		centerContentPanel = new JPanel(new BorderLayout(0, 10));
		centerContentPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		viewPanel.add(centerContentPanel, BorderLayout.CENTER);
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
			
			JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			labelPanel.add(titleLabel);
			topContentPanel.add(labelPanel);
			
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
	 * @param centerContentPanel
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
		return tabTitle;
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
}
