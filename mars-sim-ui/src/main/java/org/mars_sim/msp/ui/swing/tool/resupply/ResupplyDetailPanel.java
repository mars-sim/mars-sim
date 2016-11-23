/**
 * Mars Simulation Project
 * ResupplyDetailPanel.java
 * @version 3.07 2014-12-01
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;

/**
 * A panel showing a selected resupply mission details.
 */
public class ResupplyDetailPanel
extends JPanel
implements ClockListener, HistoricalEventListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private Resupply resupply;
	private JLabel destinationValueLabel;
	private JLabel stateValueLabel;
	private JLabel arrivalDateValueLabel;
	private JLabel launchDateValueLabel;
	private JLabel timeArrivalValueLabel;
	private JLabel immigrantsValueLabel;
	private int solsToArrival = -1;
	private JPanel innerSupplyPane;

	/**
	 * Constructor.
	 */
	public ResupplyDetailPanel() {

		// Use JPanel constructor
		super();

		// Initialize data members.
		resupply = null;

		setLayout(new BorderLayout(0, 10));
		setBorder(new MarsPanelBorder());

		// Create the info panel.
		JPanel infoPane = new JPanel(new BorderLayout());
		add(infoPane, BorderLayout.NORTH);

		// Create the title label.
		JLabel titleLabel = new JLabel("Resupply Mission", JLabel.CENTER);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setPreferredSize(new Dimension(-1, 25));
		infoPane.add(titleLabel, BorderLayout.NORTH);

		// Create the info2 panel.
		JPanel info2Pane = new JPanel(new GridLayout(6, 1, 3, 3));
		infoPane.add(info2Pane, BorderLayout.CENTER);

		// Create destination panel.
		JPanel destinationPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(destinationPane);

		// Create destination title label.
		JLabel destinationTitleLabel = new JLabel("  Destination : ", JLabel.LEFT);
		destinationPane.add(destinationTitleLabel);

		// Create destination value label.
		destinationValueLabel = new JLabel("", JLabel.LEFT);
		destinationPane.add(destinationValueLabel);

		// Create state panel.
		JPanel statePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(statePane);

		// Create state title label.
		JLabel stateTitleLabel = new JLabel("  State : ", JLabel.LEFT);
		statePane.add(stateTitleLabel);

		// Create state value label.
		stateValueLabel = new JLabel("", JLabel.LEFT);
		statePane.add(stateValueLabel);

		// 2016-11-23 Create launch date panel.
		JPanel launchDatePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(launchDatePane);

		// 2016-11-23 Create launch date title label.
		JLabel launchDateTitleLabel = new JLabel("  Launch Date : ", JLabel.LEFT);
		launchDatePane.add(launchDateTitleLabel);

		// 2016-11-23  Create launch date value label.
		launchDateValueLabel = new JLabel("", JLabel.LEFT);
		launchDatePane.add(launchDateValueLabel);
		
		// Create arrival date panel.
		JPanel arrivalDatePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(arrivalDatePane);

		// Create arrival date title label.
		JLabel arrivalDateTitleLabel = new JLabel("  Arrival Date : ", JLabel.LEFT);
		arrivalDatePane.add(arrivalDateTitleLabel);

		// Create arrival date value label.
		arrivalDateValueLabel = new JLabel("", JLabel.LEFT);
		arrivalDatePane.add(arrivalDateValueLabel);
	
		// Create time arrival panel.
		JPanel timeArrivalPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(timeArrivalPane);

		// Create time arrival title label.
		JLabel timeArrivalTitleLabel = new JLabel("  Time Until Arrival : ", JLabel.LEFT);
		timeArrivalPane.add(timeArrivalTitleLabel);

		// Create time arrival value label.
		timeArrivalValueLabel = new JLabel("", JLabel.LEFT);
		timeArrivalPane.add(timeArrivalValueLabel);

		// Create immigrants panel.
		JPanel immigrantsPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info2Pane.add(immigrantsPane);

		// Create immigrants title label.
		JLabel immigrantsTitleLabel = new JLabel("  Immigrants : ", JLabel.LEFT);
		immigrantsPane.add(immigrantsTitleLabel);

		// Create immigrants value label.
		immigrantsValueLabel = new JLabel("", JLabel.LEFT);
		immigrantsPane.add(immigrantsValueLabel);

		// Create the outer supply panel.
		JPanel outerSupplyPane = new JPanel(new BorderLayout(0, 0));
		outerSupplyPane.setBorder(new TitledBorder("Supplies"));
		add(outerSupplyPane, BorderLayout.CENTER);

		// Create the inner supply panel.
		innerSupplyPane = new JPanel();
		innerSupplyPane.setLayout(new BoxLayout(innerSupplyPane, BoxLayout.Y_AXIS));
		outerSupplyPane.add(new JScrollPane(innerSupplyPane), BorderLayout.CENTER);

		// Set as clock listener.
		Simulation.instance().getMasterClock().addClockListener(this);

		// Set as historical event listener.
		Simulation.instance().getEventManager().addListener(this);
	}

	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		resupply = null;
		Simulation.instance().getEventManager().removeListener(this);
		Simulation.instance().getMasterClock().removeClockListener(this);
	}

	/**
	 * Set the resupply mission to show.
	 * If resupply is null, clear displayed info.
	 * @param resupply the resupply mission.
	 */
	public void setResupply(Resupply resupply) {
		if (this.resupply != resupply) {
			this.resupply = resupply;
			if (resupply == null) {
				clearInfo();
			}
			else {
				updateResupplyInfo();
			}
		}
	}

	/**
	 * Clear the resupply info.
	 */
	private void clearInfo() {
		destinationValueLabel.setText("");
		stateValueLabel.setText("");
		launchDateValueLabel.setText("");
		arrivalDateValueLabel.setText("");
		timeArrivalValueLabel.setText("");
		immigrantsValueLabel.setText("");
		innerSupplyPane.removeAll();
	}

	/**
	 * Update the resupply info with the current resupply mission.
	 */
	private void updateResupplyInfo() {

		destinationValueLabel.setText(resupply.getSettlement().getName());

		stateValueLabel.setText(Conversion.capitalize(resupply.getTransitState().getName()));

		launchDateValueLabel.setText(resupply.getLaunchDate().getDateString());

		arrivalDateValueLabel.setText(resupply.getArrivalDate().getDateString());

		updateTimeToArrival();

		immigrantsValueLabel.setText(Integer.toString(resupply.getNewImmigrantNum()));

		updateSupplyPanel();

		validate();
	}

	/**
	 * Update the supply panel with the current resupply mission.
	 */
	private void updateSupplyPanel() {

		// Clear any previous data.
		innerSupplyPane.removeAll();

		// Create buildings panel.
		JPanel buildingsPanel = createBuildingsDisplayPanel();
		if (buildingsPanel != null) {
			innerSupplyPane.add(buildingsPanel);
			innerSupplyPane.add(Box.createVerticalStrut(10));
		}

		// Create vehicles panel.
		JPanel vehiclesPanel = createVehiclesDisplayPanel();
		if (vehiclesPanel != null) {
			innerSupplyPane.add(vehiclesPanel);
			innerSupplyPane.add(Box.createVerticalStrut(10));
		}

		// Create equipment panel.
		JPanel equipmentPanel = createEquipmentDisplayPanel();
		if (equipmentPanel != null) {
			innerSupplyPane.add(equipmentPanel);
			innerSupplyPane.add(Box.createVerticalStrut(10));
		}

		// Create resources panel.
		JPanel resourcesPanel = createResourcesDisplayPanel();
		if (resourcesPanel != null) {
			innerSupplyPane.add(resourcesPanel);
			innerSupplyPane.add(Box.createVerticalStrut(10));
		}

		// Create parts panel.
		JPanel partsPanel = createPartsDisplayPanel();
		if (partsPanel != null) {
			innerSupplyPane.add(partsPanel);
		}

		innerSupplyPane.add(Box.createVerticalGlue());
	}

	/**
	 * Create the building display panel.
	 * @return panel.
	 */
	private JPanel createBuildingsDisplayPanel() {

		JPanel buildingsPanel = null;

		List<BuildingTemplate> buildings = resupply.getNewBuildings();
		if (buildings.size() > 0) {
			// Create buildings panel.
			buildingsPanel = new JPanel(new BorderLayout());

			// Create buildings label.
			JLabel buildingsLabel = new JLabel("Buildings", JLabel.CENTER);
			buildingsPanel.add(buildingsLabel, BorderLayout.NORTH);

			// Create table data.
			Map<String, Integer> buildingMap = new HashMap<String, Integer>(buildings.size());
			Iterator<BuildingTemplate> i = buildings.iterator();
			while (i.hasNext()) {
				BuildingTemplate buildingTemplate = i.next();
				if (buildingMap.containsKey(buildingTemplate.getBuildingType())) {
					int num = buildingMap.get(buildingTemplate.getBuildingType()) + 1;
					buildingMap.put(buildingTemplate.getBuildingType(), num);
				}
				else {
					buildingMap.put(buildingTemplate.getBuildingType(), 1);
				}
			}

			// Create table model.
			DefaultTableModel tableModel = new DefaultTableModel();
			tableModel.addColumn("Building Type");
			tableModel.addColumn("Num");

			// Populate table model with data.
			List<String> buildingTypes = new ArrayList<String>(buildingMap.keySet());
			Collections.sort(buildingTypes);
			Iterator<String> j = buildingTypes.iterator();
			while (j.hasNext()) {
				String buildingName = j.next();
				int num = buildingMap.get(buildingName);
				Vector<Comparable<?>> rowData = new Vector<Comparable<?>>(2);
				rowData.add(buildingName);
				rowData.add(num);
				tableModel.addRow(rowData);
			}

			// Create table
			JTable buildingTable = new JTable(tableModel);
			buildingTable.setCellSelectionEnabled(false);
			buildingTable.getColumnModel().getColumn(1).setMaxWidth(50);
			buildingTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(0));
			buildingsPanel.add(new JScrollPane(buildingTable), BorderLayout.CENTER);

			// Set preferred height for panel to show all of table.
			int panelHeight = buildingTable.getPreferredSize().height +
					buildingTable.getTableHeader().getPreferredSize().height +
					buildingsLabel.getPreferredSize().height + 7;
			buildingsPanel.setPreferredSize(new Dimension(100, panelHeight));
		}

		return buildingsPanel;
	}

	/**
	 * Create the vehicle display panel.
	 * @return panel.
	 */
	private JPanel createVehiclesDisplayPanel() {

		JPanel vehiclesPanel = null;

		List<String> vehicles = resupply.getNewVehicles();
		if (vehicles.size() > 0) {
			// Create vehicles panel.
			vehiclesPanel = new JPanel(new BorderLayout());

			// Create vehicles label.
			JLabel vehiclesLabel = new JLabel("Vehicles", JLabel.CENTER);
			vehiclesPanel.add(vehiclesLabel, BorderLayout.NORTH);

			// Create table data.
			Map<String, Integer> vehicleMap = new HashMap<String, Integer>(vehicles.size());
			Iterator<String> i = vehicles.iterator();
			while (i.hasNext()) {
				String vehicle = i.next();
				if (vehicleMap.containsKey(vehicle)) {
					int num = vehicleMap.get(vehicle) + 1;
					vehicleMap.put(vehicle, num);
				}
				else {
					vehicleMap.put(vehicle, 1);
				}
			}

			// Create table model.
			DefaultTableModel tableModel = new DefaultTableModel();
			tableModel.addColumn("Vehicle Type");
			tableModel.addColumn("Num");

			// Populate table model with data.
			List<String> vehicleTypes = new ArrayList<String>(vehicleMap.keySet());
			Collections.sort(vehicleTypes);
			Iterator<String> j = vehicleTypes.iterator();
			while (j.hasNext()) {
				String vehicleName = j.next();
				int num = vehicleMap.get(vehicleName);
				Vector<Comparable<?>> rowData = new Vector<Comparable<?>>(2);
				rowData.add(vehicleName);
				rowData.add(num);
				tableModel.addRow(rowData);
			}

			// Create table
			JTable vehicleTable = new JTable(tableModel);
			vehicleTable.setCellSelectionEnabled(false);
			vehicleTable.getColumnModel().getColumn(1).setMaxWidth(50);
			vehicleTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(0));
			vehiclesPanel.add(new JScrollPane(vehicleTable), BorderLayout.CENTER);

			// Set preferred height for panel to show all of table.
			int panelHeight = vehicleTable.getPreferredSize().height +
					vehicleTable.getTableHeader().getPreferredSize().height +
					vehiclesLabel.getPreferredSize().height + 7;
			vehiclesPanel.setPreferredSize(new Dimension(100, panelHeight));
		}

		return vehiclesPanel;
	}

	/**
	 * Create the equipment display panel.
	 * @return panel.
	 */
	private JPanel createEquipmentDisplayPanel() {

		JPanel equipmentPanel = null;

		Map<String, Integer> equipment = resupply.getNewEquipment();
		if (equipment.size() > 0) {
			// Create equipment panel.
			equipmentPanel = new JPanel(new BorderLayout());

			// Create equipment label.
			JLabel equipmentLabel = new JLabel("Equipment", JLabel.CENTER);
			equipmentPanel.add(equipmentLabel, BorderLayout.NORTH);

			// Create table model.
			DefaultTableModel tableModel = new DefaultTableModel();
			tableModel.addColumn("Equipment Type");
			tableModel.addColumn("Num");

			// Populate table model with data.
			List<String> equipmentTypes = new ArrayList<String>(equipment.keySet());
			Collections.sort(equipmentTypes);
			Iterator<String> j = equipmentTypes.iterator();
			while (j.hasNext()) {
				String equipmentType = j.next();
				int num = equipment.get(equipmentType);
				Vector<Comparable<?>> rowData = new Vector<Comparable<?>>(2);
				rowData.add(equipmentType);
				rowData.add(num);
				tableModel.addRow(rowData);
			}

			// Create table
			JTable equipmentTable = new JTable(tableModel);
			equipmentTable.setCellSelectionEnabled(false);
			equipmentTable.getColumnModel().getColumn(1).setMaxWidth(50);
			equipmentTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(0));
			equipmentPanel.add(new JScrollPane(equipmentTable), BorderLayout.CENTER);

			// Set preferred height for panel to show all of table.
			int panelHeight = equipmentTable.getPreferredSize().height +
					equipmentTable.getTableHeader().getPreferredSize().height +
					equipmentLabel.getPreferredSize().height + 7;
			equipmentPanel.setPreferredSize(new Dimension(100, panelHeight));
		}

		return equipmentPanel;
	}

	/**
	 * Create the resources display panel.
	 * @return panel.
	 */
	private JPanel createResourcesDisplayPanel() {

		JPanel resourcesPanel = null;

		Map<AmountResource, Double> resources = resupply.getNewResources();
		if (resources.size() > 0) {
			// Create resources panel.
			resourcesPanel = new JPanel(new BorderLayout());

			// Create resources label.
			JLabel resourcesLabel = new JLabel("Resources", JLabel.CENTER);
			resourcesPanel.add(resourcesLabel, BorderLayout.NORTH);

			// Create table model.
			DefaultTableModel tableModel = new DefaultTableModel();
			tableModel.addColumn("Resource Type");
			tableModel.addColumn("Amount");

			// Populate table model with data.
			List<AmountResource> resourceTypes = new ArrayList<AmountResource>(resources.keySet());
			Collections.sort(resourceTypes);
			Iterator<AmountResource> j = resourceTypes.iterator();
			while (j.hasNext()) {
				AmountResource resourceType = j.next();
				double amount = resources.get(resourceType);
				// 2014-12-01 Added Conversion.capitalize()
				String resourceName = Conversion.capitalize(resourceType.getName());
				Vector<Comparable<?>> rowData = new Vector<Comparable<?>>(2);
				//rowData.add(resourceType);
				rowData.add(resourceName);
				rowData.add(amount);
				tableModel.addRow(rowData);
			}

			// Create table
			JTable resourcesTable = new JTable(tableModel);
			resourcesTable.setCellSelectionEnabled(false);
			resourcesTable.getColumnModel().getColumn(1).setMaxWidth(70);
			resourcesTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(1));
			resourcesPanel.add(new JScrollPane(resourcesTable), BorderLayout.CENTER);

			// Set preferred height for panel to show all of table.
			int panelHeight = resourcesTable.getPreferredSize().height +
					resourcesTable.getTableHeader().getPreferredSize().height +
					resourcesLabel.getPreferredSize().height + 7;
			resourcesPanel.setPreferredSize(new Dimension(100, panelHeight));
		}

		return resourcesPanel;
	}

	/**
	 * Create the parts display panel.
	 * @return panel.
	 */
	private JPanel createPartsDisplayPanel() {

		JPanel partsPanel = null;

		Map<Part, Integer> parts = resupply.getNewParts();
		if (parts.size() > 0) {
			// Create parts panel.
			partsPanel = new JPanel(new BorderLayout());

			// Create parts label.
			JLabel partsLabel = new JLabel("Parts", JLabel.CENTER);
			partsPanel.add(partsLabel, BorderLayout.NORTH);

			// Create table model.
			DefaultTableModel tableModel = new DefaultTableModel();
			tableModel.addColumn("Part Type");
			tableModel.addColumn("Num");

			// Populate table model with data.
			List<Part> partTypes = new ArrayList<Part>(parts.keySet());
			Collections.sort(partTypes);
			Iterator<Part> j = partTypes.iterator();
			while (j.hasNext()) {
				Part partType = j.next();
				int num = parts.get(partType);
				Vector<Comparable<?>> rowData = new Vector<Comparable<?>>(2);
				// 2014-12-01 Added Conversion.capitalize()
				String partName = Conversion.capitalize(partType.getName());
				rowData.add(partName);
				//rowData.add(partType);
				rowData.add(num);
				tableModel.addRow(rowData);
			}

			// Create table
			JTable partsTable = new JTable(tableModel);
			partsTable.setCellSelectionEnabled(false);
			partsTable.getColumnModel().getColumn(1).setMaxWidth(50);
			partsTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(0));
			partsPanel.add(new JScrollPane(partsTable), BorderLayout.CENTER);

			// Set preferred height for panel to show all of table.
			int panelHeight = partsTable.getPreferredSize().height +
					partsTable.getTableHeader().getPreferredSize().height +
					partsLabel.getPreferredSize().height + 7;
			partsPanel.setPreferredSize(new Dimension(100, panelHeight));
		}

		return partsPanel;
	}

	/**
	 * Update the time to arrival label.
	 */
	private void updateTimeToArrival() {
		String timeArrival = "---";
		solsToArrival = -1;
		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		double timeDiff = MarsClock.getTimeDiff(resupply.getArrivalDate(), currentTime);
		if (timeDiff > 0D) {
			solsToArrival = (int) Math.abs(timeDiff / 1000D);
			timeArrival = Integer.toString(solsToArrival) + " Sols";
		}
		timeArrivalValueLabel.setText(timeArrival);
	}

	@Override
	public void eventAdded(int index, HistoricalEvent event) {
		if (HistoricalEventCategory.TRANSPORT == event.getCategory() &&
				EventType.TRANSPORT_ITEM_MODIFIED.equals(event.getType())) {
			if ((resupply != null) && event.getSource().equals(resupply)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Update resupply info.
						if (resupply != null) {
							updateResupplyInfo();
						}
					}
				});
			}
		}
	}

	@Override
	public void eventsRemoved(int startIndex, int endIndex) {
		// Do nothing.
	}

	@Override
	public void clockPulse(double time) {
		// Determine if change in time to arrival display value.
		if ((resupply != null) && (solsToArrival >= 0)) {
			MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
			double timeDiff = MarsClock.getTimeDiff(resupply.getArrivalDate(), currentTime);
			double newSolsToArrival = (int) Math.abs(timeDiff / 1000D);
			if (newSolsToArrival != solsToArrival) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Update time to arrival label.
						if (resupply != null) {
							updateTimeToArrival();
						}
					}
				});
			}
		}
	}

	@Override
	public void pauseChange(boolean isPaused) {
		// Do nothing
	}
}