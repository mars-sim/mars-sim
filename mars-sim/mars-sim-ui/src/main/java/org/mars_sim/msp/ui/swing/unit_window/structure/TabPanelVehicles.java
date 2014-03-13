/**
 * Mars Simulation Project
 * VehicleTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/** 
 * The VehicleTabPanel is a tab panel for parked vehicle information.
 */
public class TabPanelVehicles
extends TabPanel
implements MouseListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private DefaultListModel<Vehicle> vehicleListModel;
	private JList<Vehicle> vehicleList;
	private Collection<Vehicle> vehicleCache;

	/**
	 * Constructor.
	 * @param unit the unit to display
	 * @param desktop the main desktop.
	 */
	public TabPanelVehicles(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelVehicles.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelVehicles.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Settlement settlement = (Settlement) unit;

		// Create vehicle label panel
		JPanel vehicleLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(vehicleLabelPanel);

		// Create vehicle label
		JLabel vehicleLabel = new JLabel(Msg.getString("TabPanelVehicles.parkedVehicles"), JLabel.CENTER); //$NON-NLS-1$
		vehicleLabelPanel.add(vehicleLabel);

		// Create vehicle display panel
		JPanel vehicleDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		vehicleDisplayPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(vehicleDisplayPanel);

		// Create scroll panel for vehicle list.
		JScrollPane vehicleScrollPanel = new JScrollPane();
		vehicleScrollPanel.setPreferredSize(new Dimension(175, 200));
		vehicleDisplayPanel.add(vehicleScrollPanel);

		// Create vehicle list model
		vehicleListModel = new DefaultListModel<Vehicle>();
		vehicleCache = settlement.getParkedVehicles();
		Iterator<Vehicle> i = vehicleCache.iterator();
		while (i.hasNext()) vehicleListModel.addElement(i.next());

		// Create vehicle list
		vehicleList = new JList<Vehicle>(vehicleListModel);
		vehicleList.addMouseListener(this);
		vehicleScrollPanel.setViewportView(vehicleList);
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		Settlement settlement = (Settlement) unit;

		// Update vehicle list
		if (!CollectionUtils.isEqualCollection(vehicleCache, settlement.getParkedVehicles())) {
			vehicleCache = new ArrayList<Vehicle>(settlement.getParkedVehicles());
			vehicleListModel.clear();
			Iterator<Vehicle> i = vehicleCache.iterator();
			while (i.hasNext()) vehicleListModel.addElement(i.next());
		}
	}

	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {
		// If double-click, open person window.
		if (event.getClickCount() >= 2) {
			Vehicle vehicle = (Vehicle) vehicleList.getSelectedValue();
			if (vehicle != null) {
				desktop.openUnitWindow(vehicle, false);
			}
		}
	}

	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
}
