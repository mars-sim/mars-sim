/**
 * Mars Simulation Project
 * EventTab.java
 * @version 3.1.0 2017-03-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.notification.NotificationWindow;

/**
 * This class represents a historical event table displayed within the Monitor
 * Window.
 */
@SuppressWarnings("serial")
public class EventTab extends TableTab {

	/**
	 * constructor.
	 * 
	 * @param window {@link MonitorWindow} the containing window
	 * @param notifyBox  {@link NotificationWindow}
	 * @param desktop
	 */
	public EventTab(final MonitorWindow window, NotificationWindow notifyBox, MainDesktopPane desktop) {
		// Use TableTab constructor
		super(window, new EventTableModel(notifyBox, desktop), true, false,
				MonitorWindow.EVENT_ICON);

	}

	void filterCategories(MainDesktopPane desktop) {
		EventFilter filter = new EventFilter((EventTableModel) getModel(), desktop);
		filter.show();
	}
}