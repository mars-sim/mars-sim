/**
 * Mars Simulation Project
 * ResupplyDetailPanel.java
 * @version 3.1.0 2017-11-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.SimpleEvent;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * A panel showing a selected resupply mission details.
 */
public class ResupplyDetailPanel
extends WebPanel
implements ClockListener, HistoricalEventListener {

//	private static final double PERIOD_IN_MILLISOLS = 10D * 500D / MarsClock.SECONDS_PER_MILLISOL;//3;

	// Data members
	private Resupply resupply;
	private WebLabel destinationValueLabel;
	private WebLabel stateValueLabel;
	private WebLabel arrivalDateValueLabel;
	private WebLabel launchDateValueLabel;
	private WebLabel timeArrivalValueLabel;
	private WebLabel immigrantsValueLabel;
	private WebPanel innerSupplyPane;

	private static MarsClock currentTime;
	private static MasterClock masterClock;
	
	private MainDesktopPane desktop;
//	private MainScene mainScene;
	
//	private double timeCache = 0;
	private int solsToArrival = -1;
	
	/**
	 * Constructor.
	 */
	public ResupplyDetailPanel(MainDesktopPane desktop) {

		// Use WebPanel constructor
		super();

		this.desktop = desktop;
//		this.mainScene = desktop.getMainScene();
		
		masterClock = Simulation.instance().getMasterClock();
		currentTime = masterClock.getMarsClock();
	
		// Initialize data members.
		resupply = null;

		setLayout(new BorderLayout(0, 10));
		setBorder(new MarsPanelBorder());

		// Create the info panel.
		WebPanel infoPane = new WebPanel(new BorderLayout());
		add(infoPane, BorderLayout.NORTH);

		// Create the title label.
		WebLabel titleLabel = new WebLabel("Resupply Mission", WebLabel.CENTER);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setPreferredSize(new Dimension(-1, 25));
		infoPane.add(titleLabel, BorderLayout.NORTH);

		// Create the spring panel.
		WebPanel springPane = new WebPanel(new SpringLayout());//GridLayout(6, 1, 3, 3));
		infoPane.add(springPane, BorderLayout.CENTER);

		// Create destination panel.
		//WebPanel destinationPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		//info2Pane.add(destinationPane);

		// Create destination title label.
		WebLabel destinationTitleLabel = new WebLabel("Destination : ", WebLabel.RIGHT);
		springPane.add(destinationTitleLabel);

		// Create destination value label.
		destinationValueLabel = new WebLabel("", WebLabel.CENTER);
		springPane.add(destinationValueLabel);

		// Create state panel.
		//WebPanel statePane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//springPane.add(statePane);

		// Create state title label.
		WebLabel stateTitleLabel = new WebLabel("State : ", WebLabel.RIGHT);
		springPane.add(stateTitleLabel);

		// Create state value label.
		stateValueLabel = new WebLabel("", WebLabel.CENTER);
		springPane.add(stateValueLabel);

		// Create launch date panel.
		//WebPanel launchDatePane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//springPane.add(launchDatePane);

		// reate launch date title label.
		WebLabel launchDateTitleLabel = new WebLabel("Launch Date : ", WebLabel.RIGHT);
		springPane.add(launchDateTitleLabel);

		// 2016-11-23  Create launch date value label.
		launchDateValueLabel = new WebLabel("", WebLabel.CENTER);
		springPane.add(launchDateValueLabel);

		// Create arrival date panel.
		//WebPanel arrivalDatePane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//springPane.add(arrivalDatePane);

		// Create arrival date title label.
		WebLabel arrivalDateTitleLabel = new WebLabel("Arrival Date : ", WebLabel.RIGHT);
		springPane.add(arrivalDateTitleLabel);

		// Create arrival date value label.
		arrivalDateValueLabel = new WebLabel("", WebLabel.CENTER);
		springPane.add(arrivalDateValueLabel);

		// Create time arrival panel.
		//WebPanel timeArrivalPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//springPane.add(timeArrivalPane);

		// Create time arrival title label.
		WebLabel timeArrivalTitleLabel = new WebLabel("Time Until Arrival : ", WebLabel.RIGHT);
		springPane.add(timeArrivalTitleLabel);

		// Create time arrival value label.
		timeArrivalValueLabel = new WebLabel("", WebLabel.CENTER);
		springPane.add(timeArrivalValueLabel);

		// Create immigrants panel.
		//WebPanel immigrantsPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//springPane.add(immigrantsPane);

		// Create immigrants title label.
		WebLabel immigrantsTitleLabel = new WebLabel("Immigrants : ", WebLabel.RIGHT);
		springPane.add(immigrantsTitleLabel);

		// Create immigrants value label.
		immigrantsValueLabel = new WebLabel("", WebLabel.CENTER);
		springPane.add(immigrantsValueLabel);

		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(springPane,
		                                6, 2, //rows, cols
		                                20, 5,        //initX, initY
		                                20, 2);       //xPad, yPad


		// Create the outer supply panel.
		WebPanel outerSupplyPane = new WebPanel(new BorderLayout(0, 0));
		outerSupplyPane.setBorder(new TitledBorder("Supplies"));
		add(outerSupplyPane, BorderLayout.CENTER);

		// Create the inner supply panel.
		innerSupplyPane = new WebPanel();
		innerSupplyPane.setLayout(new BoxLayout(innerSupplyPane, BoxLayout.Y_AXIS));
		outerSupplyPane.add(new WebScrollPane(innerSupplyPane), BorderLayout.CENTER);

		// Set as clock listener.
		Simulation.instance().getMasterClock().addClockListener(this);

		// Set as historical event listener.
		Simulation.instance().getEventManager().addListener(this);
	}

	/**
	 * Set the resupply mission to show.
	 * If resupply is null, clear displayed info.
	 * 
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
		WebPanel buildingsPanel = createBuildingsDisplayPanel();
		if (buildingsPanel != null) {
			innerSupplyPane.add(buildingsPanel);
			innerSupplyPane.add(Box.createVerticalStrut(10));
		}

		// Create vehicles panel.
		WebPanel vehiclesPanel = createVehiclesDisplayPanel();
		if (vehiclesPanel != null) {
			innerSupplyPane.add(vehiclesPanel);
			innerSupplyPane.add(Box.createVerticalStrut(10));
		}

		// Create equipment panel.
		WebPanel equipmentPanel = createEquipmentDisplayPanel();
		if (equipmentPanel != null) {
			innerSupplyPane.add(equipmentPanel);
			innerSupplyPane.add(Box.createVerticalStrut(10));
		}

		// Create resources panel.
		WebPanel resourcesPanel = createResourcesDisplayPanel();
		if (resourcesPanel != null) {
			innerSupplyPane.add(resourcesPanel);
			innerSupplyPane.add(Box.createVerticalStrut(10));
		}

		// Create parts panel.
		WebPanel partsPanel = createPartsDisplayPanel();
		if (partsPanel != null) {
			innerSupplyPane.add(partsPanel);
		}

		innerSupplyPane.add(Box.createVerticalGlue());
	}

	/**
	 * Create the building display panel.
	 * 
	 * @return panel.
	 */
	private WebPanel createBuildingsDisplayPanel() {

		WebPanel buildingsPanel = null;

		List<BuildingTemplate> buildings = resupply.getNewBuildings();
		if (buildings.size() > 0) {
			// Create buildings panel.
			buildingsPanel = new WebPanel(new BorderLayout());

			// Create buildings label.
			WebLabel buildingsLabel = new WebLabel("Buildings", WebLabel.CENTER);
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
			DefaultTableModel tableModel = new DefaultTableModel() {
			    @Override
			    public boolean isCellEditable(int row, int column) {
			       //all cells false
			       return false;
			    }
			};
			tableModel.addColumn("Building Type");
			tableModel.addColumn("Quantity");

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
			JTable buildingTable = new ZebraJTable(tableModel);
			TableStyle.setTableStyle(buildingTable);
			buildingTable.setAutoCreateRowSorter(true);
			buildingTable.setCellSelectionEnabled(false);
			buildingTable.getColumnModel().getColumn(1).setMaxWidth(100);
			buildingTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(0));
			buildingsPanel.add(new WebScrollPane(buildingTable), BorderLayout.CENTER);

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
	 * 
	 * @return panel.
	 */
	private WebPanel createVehiclesDisplayPanel() {

		WebPanel vehiclesPanel = null;

		List<String> vehicles = resupply.getNewVehicles();
		if (vehicles.size() > 0) {
			// Create vehicles panel.
			vehiclesPanel = new WebPanel(new BorderLayout());

			// Create vehicles label.
			WebLabel vehiclesLabel = new WebLabel("Vehicles", WebLabel.CENTER);
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
			DefaultTableModel tableModel = new DefaultTableModel(){
			    @Override
			    public boolean isCellEditable(int row, int column) {
			       //all cells false
			       return false;
			    }
			};
			tableModel.addColumn("Vehicle Type");
			tableModel.addColumn("Quantity");

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
			JTable vehicleTable = new ZebraJTable(tableModel);
			TableStyle.setTableStyle(vehicleTable);
			vehicleTable.setAutoCreateRowSorter(true);
			vehicleTable.setCellSelectionEnabled(false);
			vehicleTable.getColumnModel().getColumn(1).setMaxWidth(100);
			vehicleTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(0));
			vehiclesPanel.add(new WebScrollPane(vehicleTable), BorderLayout.CENTER);

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
	 * 
	 * @return panel.
	 */
	private WebPanel createEquipmentDisplayPanel() {

		WebPanel equipmentPanel = null;

		Map<String, Integer> equipment = resupply.getNewEquipment();
		if (equipment.size() > 0) {
			// Create equipment panel.
			equipmentPanel = new WebPanel(new BorderLayout());

			// Create equipment label.
			WebLabel equipmentLabel = new WebLabel("Equipment", WebLabel.CENTER);
			equipmentPanel.add(equipmentLabel, BorderLayout.NORTH);

			// Create table model.
			DefaultTableModel tableModel = new DefaultTableModel() {
			    @Override
			    public boolean isCellEditable(int row, int column) {
			       //all cells false
			       return false;
			    }
			};
			tableModel.addColumn("Equipment Type");
			tableModel.addColumn("Quantity");

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
			JTable equipmentTable = new ZebraJTable(tableModel);
			TableStyle.setTableStyle(equipmentTable);
			equipmentTable.setAutoCreateRowSorter(true);
			equipmentTable.setCellSelectionEnabled(false);
			equipmentTable.getColumnModel().getColumn(1).setMaxWidth(100);
			equipmentTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(0));
			equipmentPanel.add(new WebScrollPane(equipmentTable), BorderLayout.CENTER);

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
	 * 
	 * @return panel.
	 */
	private WebPanel createResourcesDisplayPanel() {

		WebPanel resourcesPanel = null;

		Map<AmountResource, Double> resources = resupply.getNewResources();
		if (resources.size() > 0) {
			// Create resources panel.
			resourcesPanel = new WebPanel(new BorderLayout());

			// Create resources label.
			WebLabel resourcesLabel = new WebLabel("Resources", WebLabel.CENTER);
			resourcesPanel.add(resourcesLabel, BorderLayout.NORTH);

			// Create table model.
			DefaultTableModel tableModel = new DefaultTableModel(){
			    @Override
			    public boolean isCellEditable(int row, int column) {
			       //all cells false
			       return false;
			    }
			};
			tableModel.addColumn("Resource Type");
			tableModel.addColumn("Amount [kg]");

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
			JTable resourcesTable = new ZebraJTable(tableModel);
			TableStyle.setTableStyle(resourcesTable);
			resourcesTable.setAutoCreateRowSorter(true);
			resourcesTable.setCellSelectionEnabled(false);
			resourcesTable.getColumnModel().getColumn(1).setMaxWidth(120);
			resourcesTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(1));
			resourcesPanel.add(new WebScrollPane(resourcesTable), BorderLayout.CENTER);

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
	 * 
	 * @return panel.
	 */
	private WebPanel createPartsDisplayPanel() {

		WebPanel partsPanel = null;

		Map<Part, Integer> parts = resupply.getNewParts();
		if (parts.size() > 0) {
			// Create parts panel.
			partsPanel = new WebPanel(new BorderLayout());

			// Create parts label.
			WebLabel partsLabel = new WebLabel("Parts", WebLabel.CENTER);
			partsPanel.add(partsLabel, BorderLayout.NORTH);

			// Create table model.
			DefaultTableModel tableModel = new DefaultTableModel() {
			    @Override
			    public boolean isCellEditable(int row, int column) {
			       //all cells false
			       return false;
			    }
			};
			tableModel.addColumn("Part Type");
			tableModel.addColumn("Quantity");

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
			JTable partsTable = new ZebraJTable(tableModel);
			TableStyle.setTableStyle(partsTable);
			partsTable.setAutoCreateRowSorter(true);
			partsTable.setAutoCreateRowSorter(true);
			partsTable.setCellSelectionEnabled(false);
			partsTable.getColumnModel().getColumn(1).setMaxWidth(100);
			partsTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(0));
			partsPanel.add(new WebScrollPane(partsTable), BorderLayout.CENTER);

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
//		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		double timeDiff = MarsClock.getTimeDiff(resupply.getArrivalDate(), currentTime);
		if (timeDiff > 0D) {
			solsToArrival = (int) Math.abs(timeDiff / 1000D);
			timeArrival = Integer.toString(solsToArrival) + " Sols";
		}
		timeArrivalValueLabel.setText(timeArrival);
	}

	@Override
	public void eventAdded(int index, SimpleEvent se, HistoricalEvent he) {
		if (HistoricalEventCategory.TRANSPORT == he.getCategory() &&
				EventType.TRANSPORT_ITEM_MODIFIED.equals(he.getType())) {
			if ((resupply != null) && he.getSource().equals(resupply)) {
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

	public void updateArrival() {
		// Determine if change in time to arrival display value.
		if ((resupply != null) && (solsToArrival >= 0)) {
			//MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
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
	public void clockPulse(double time) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void uiPulse(double time) {
		if (desktop.isToolWindowOpen(ResupplyWindow.NAME)) {
			updateArrival();
		}	
	}
	
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		resupply = null;
//		Simulation.instance().getEventManager().removeListener(this);
//		Simulation.instance().getMasterClock().removeClockListener(this);

		destinationValueLabel = null;
		stateValueLabel = null;
		arrivalDateValueLabel = null;
		launchDateValueLabel = null;
		timeArrivalValueLabel = null;
		immigrantsValueLabel = null;
		innerSupplyPane = null;
		currentTime = null;
		masterClock = null;
		
		desktop = null;

	}
	
}