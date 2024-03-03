/*
 * Mars Simulation Project
 * TabPanelLog.java
 * @date 2023-01-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.data.History;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.JHistoryPanel;


@SuppressWarnings("serial")
public class TabPanelLog extends TabPanel {

	private static final String LOG_ICON = "log"; //$NON-NLS-1$
	
	private JLabel odometerTF;
	private JLabel maintTF;
	
	/** The Vehicle instance. */
	private Vehicle vehicle;

	private LogPanel statusPanel;
	
	public TabPanelLog(Vehicle vehicle, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super(
			Msg.getString("TabPanelLog.title"),
			ImageLoader.getIconByName(LOG_ICON),
			Msg.getString("TabPanelLog.title"), //$NON-NLS-1$
			desktop
		);
		
		this.vehicle = vehicle;

	}

	@Override
	protected void buildUI(JPanel content) {
		
        // Create spring layout dataPanel
        AttributePanel springPanel = new AttributePanel(2);
        content.add(springPanel, BorderLayout.NORTH);

		odometerTF = springPanel.addTextField( Msg.getString("TabPanelLog.label.odometer"),
								  	StyleManager.DECIMAL_KM.format(vehicle.getOdometerMileage()), null);

		maintTF = springPanel.addTextField(Msg.getString("TabPanelLog.label.maintDist"),
				 					StyleManager.DECIMAL_KM.format(vehicle.getDistanceLastMaintenance()), null);	
		
		statusPanel = new LogPanel(vehicle.getVehicleLog());
		statusPanel.setPreferredSize(new Dimension(225, 100));

		content.add(statusPanel, BorderLayout.CENTER);

		// Update will refresh data
		update();
	}

	@Override
	public void update() {

		// Update the odometer reading
		odometerTF.setText(StyleManager.DECIMAL_PLACES2.format(vehicle.getOdometerMileage()));
				
		// Update distance last maintenance 
		maintTF.setText(StyleManager.DECIMAL_PLACES2.format(vehicle.getDistanceLastMaintenance()));
				
		statusPanel.refresh();
	}
		
	/**
	 * Internal class used as model for the attribute table.
	 */
	private class LogPanel extends JHistoryPanel<Set<StatusType>> {
		private static final ColumnSpec[] COLUMNS = {new ColumnSpec("Status", String.class)};

		LogPanel(History<Set<StatusType>> source) {
			super(source, COLUMNS);
		}

		@Override
		protected Object getValueFrom(Set<StatusType> value, int columnIndex) {
			String s = Conversion.capitalize(value.toString());
			return s.substring(1 , s.length() - 1);
		}
	}
}
