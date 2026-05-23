/*
 * Mars Simulation Project
 * SettlementSuppliesPanel.java
 * @date 2023-04-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.transportable;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.mars_sim.core.building.BuildingTemplate;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.SettlementSupplies;
import com.mars_sim.ui.swing.components.NumberCellRenderer;

/**
 * Panel to show the various quantities of a SettlementSupply object in a tabbed set of tables.
 */
public class SettlementSuppliesPanel {

    private static final String QUANTITY = "Quantity";
    private JTabbedPane panel;

    public SettlementSuppliesPanel() {
        panel = new JTabbedPane();
    }

    public JComponent getComponent() {
        return panel;
    }

    /**
     * Clear down the panel
     */
    public void clear() {
        panel.removeAll();
    }

    /**
     * Display the details of a set of supplies
     * @param supplies
     */
    public void show(SettlementSupplies supplies) {
        
		// Clear any previous data.
		panel.removeAll();

		// Create buildings panel.
        List<BuildingTemplate> buildings = supplies.getBuildings();
        if (!buildings.isEmpty()) {
			panel.addTab("Building", createBuildingsDisplayPanel(buildings));
		}

		// Create vehicles panel.
        Map<String, Integer> vehicles = supplies.getVehicles();
        if (!vehicles.isEmpty()) {
			panel.addTab("Vehicles", createDisplayPanel(vehicles, "Vehicle Type"));
		}

		// Create Robot panel.
        Map<String, Integer> robots = supplies.getRobots();
        if (!robots.isEmpty()) {
			panel.addTab("Robots", createDisplayPanel(robots, "Robot Type"));
		}

		// Create equipment panel.
        Map<String, Integer> equipment = supplies.getEquipment();
        if (!equipment.isEmpty()) {
            panel.addTab("Equipment", createDisplayPanel(equipment, "Equipment Type"));
		}

		// Create resources panel.
        Map<AmountResource, Double> resources = supplies.getResources();
        if (!resources.isEmpty()) {
            panel.addTab("Resources", createResourcesDisplayPanel(resources));
		}

		// Create parts panel.
        Map<Part, Integer> parts = supplies.getParts();
		if (!parts.isEmpty()) {
			panel.addTab("Parts", createPartsDisplayPanel(parts));
		}
    }

    /**
	 * Creates the building display panel.
	 * @param buildings2
	 * 
	 * @return panel.
	 */
	private JPanel createBuildingsDisplayPanel(List<BuildingTemplate> buildings) {

		JPanel buildingsPanel = new JPanel(new BorderLayout());

        // Count up the types of buildings
        Map<String, Long> buildingMap = buildings.stream()
                            .map(BuildingTemplate::getBuildingType)
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // CReate table
        JScrollPane tablePane = createValueTable(buildingMap, "Building Type", QUANTITY, 0);
        buildingsPanel.add(tablePane, BorderLayout.CENTER);

		return buildingsPanel;
	}

	private JPanel createDisplayPanel(Map<String, Integer> values, String label) {
		JPanel valuePanel = new JPanel(new BorderLayout());
        JScrollPane tablePane = createValueTable(values, label, QUANTITY, 0);
        valuePanel.add(tablePane, BorderLayout.CENTER);
        return valuePanel;
	}

	/**
	 * Creates the resources display panel.
	 * @param resources
	 * 
	 * @return panel.
	 */
	private JPanel createResourcesDisplayPanel(Map<AmountResource, Double> resources) {
        // Convert Resource to it's name
        Map<String, Double> resources2 = resources.entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey().getName(), Entry::getValue));

		JPanel resourcesPanel = new JPanel(new BorderLayout());
        JScrollPane tablePane = createValueTable(resources2, "Resource Type", "Amount [kg]", 1);
        resourcesPanel.add(tablePane, BorderLayout.CENTER);
        return resourcesPanel;
	}

	/**
	 * Creates the parts display panel.
	 * @param parts2
	 * 
	 * @return panel.
	 */
	private JPanel createPartsDisplayPanel(Map<Part, Integer> parts) {
        // Convert Part to it's name
        Map<String, Integer> parts2 = parts.entrySet().stream()
                        .collect(Collectors.toMap(entry -> entry.getKey().getName(), Entry::getValue));

        return createDisplayPanel(parts2, "Part Type");
	}

    /**
     * Create a table to display the set of Supplies. The table is sortable and read only.
     */
    private static JScrollPane createValueTable(Map<String, ? extends Number> data, String itemName,
                                                String valueName, int precision) {

        // Create table model.
        DefaultTableModel tableModel = new SuppliesTableModel(data, itemName, valueName);

        // Create table
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setCellSelectionEnabled(false);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(precision));

        return new JScrollPane(table);
    }

    /**
     * Table model to provide the Map data in a read only model. 
     * Sort the model and derive the Class type of the value column from the first item.
     */
    private static class SuppliesTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;
		private Class<? extends Number> valueType = null;

        SuppliesTableModel(Map<String, ? extends Number> data, String itemName, String valueName) {
            super();
            addColumn(itemName);
            addColumn(valueName);   

            // Populate table model with data.
            for(Entry<String, ? extends Number> e : data.entrySet()) {
                Object [] rowData = {e.getKey(), e.getValue()};
                addRow(rowData);

                if (valueType == null) {
                    valueType = e.getValue().getClass();
                }
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            }
            else {
                return valueType;
            }
        }
    }
}
