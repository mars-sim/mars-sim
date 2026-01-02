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
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.data.History;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.JHistoryPanel;


@SuppressWarnings("serial")
class TabPanelLog extends EntityTabPanel<Vehicle> implements EntityListener{

	private static final String LOG_ICON = "log";
	
	private JLabel odometerTF;
	private JLabel maintTF;

	private LogPanel statusPanel;
	
	public TabPanelLog(Vehicle vehicle, UIContext context) {
		// Use TabPanel constructor.
		super(
			Msg.getString("TabPanelLog.title"),
			ImageLoader.getIconByName(LOG_ICON),
			Msg.getString("TabPanelLog.title"), //$NON-NLS-1$
			context, vehicle
		);
	}

	@Override
	protected void buildUI(JPanel content) {
		
		var vehicle = getEntity();

        // Create spring layout dataPanel
        AttributePanel springPanel = new AttributePanel(2);
        content.add(springPanel, BorderLayout.NORTH);

		odometerTF = springPanel.addTextField(Msg.getString("Vehicle.odometer"),
								  	StyleManager.DECIMAL_KM.format(vehicle.getOdometerMileage()), null);

		maintTF = springPanel.addTextField(Msg.getString("TabPanelLog.label.maintDist"),
				 					StyleManager.DECIMAL_KM.format(vehicle.getDistanceLastMaintenance()), null);	
		
		statusPanel = new LogPanel(vehicle.getVehicleLog());
		statusPanel.setPreferredSize(new Dimension(225, 100));

		content.add(statusPanel, BorderLayout.CENTER);

		// Update will refresh data
		statusPanel.refresh();
		updateMileage();
	}


	private void updateMileage() {
		var vehicle = getEntity();

		// Update the odometer reading
		odometerTF.setText(StyleManager.DECIMAL_PLACES2.format(vehicle.getOdometerMileage()));
				
		// Update distance last maintenance 
		maintTF.setText(StyleManager.DECIMAL_PLACES2.format(vehicle.getDistanceLastMaintenance()));
	}
		
	/**
	 * Internal class used as model for the attribute table.
	 */
	private static class LogPanel extends JHistoryPanel<Set<StatusType>> {
		private static final ColumnSpec[] COLUMNS = {new ColumnSpec(Msg.getString("Vehicle.status"), String.class)};

		LogPanel(History<Set<StatusType>> source) {
			super(source, COLUMNS);
		}

		@Override
		protected Object getValueFrom(Set<StatusType> value, int columnIndex) {
			return value.stream()
					.map(StatusType::getName)
					.collect(Collectors.joining(", "));
		}
	}

	/**
	 * Monitor changes to Vehicle status or coordinates.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (EntityEventType.COORDINATE_EVENT.equals(event.getType())) {
			updateMileage();
		}
		else if (EntityEventType.STATUS_EVENT.equals(event.getType())) {
			statusPanel.refresh();
		}
	}
}
