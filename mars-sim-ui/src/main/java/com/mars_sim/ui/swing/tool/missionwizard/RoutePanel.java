/*
 * Mars Simulation Project
 * RoutePanel.java
 * @date 2026-02-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.JCoordinateEditor;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

/**
 * A wizard panel for defining a proposed navigation route.
 * Allows users to select coordinates, add them to a route table,
 * and remove waypoints from the route.
 */
@SuppressWarnings("serial")
class RoutePanel extends WizardStep<MissionDataBean> {
    
    // Wizard panel identifier
    public static final String ID = "Route";
    
    // UI Components
    private JLabel selectedCoordinateLabel;
    private JDoubleLabel distanceLabel;
    private JButton addButton;
    private JButton removeButton;
    private JTable legTable;
    private LegTableModel legTableModel;
    private int maxLeg;
    
    private Coordinates startingPoint;
    private Coordinates selectedCoordinate;
    
    /**
     * Constructor.
     * 
     * @param wizard the create mission wizard
     * @param state the mission data bean
     */
    public RoutePanel(MissionCreate wizard, MissionDataBean state) {
        super(ID, wizard);

        this.startingPoint = state.getStartingSettlement().getLocation();
        this.maxLeg = 10;
        var mType = state.getMissionType();
        if (mType == MissionType.AREOLOGY || mType == MissionType.BIOLOGY
                || mType == MissionType.METEOROLOGY) {
            this.maxLeg = 1;
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Create the entry pane (for input)
        JPanel entryPane = createEntryPane();
        add(entryPane);
        
        // Create the selection pane (displays selected coordinate)
        JPanel selectionPane = createSelectionPane();
        add(selectionPane);
        
        // Create the path table pane (displays route legs)
        JPanel tablePane = createTablePane();
        add(tablePane);
    }
    
    /**
     * Creates the entry pane with coordinate editor and accept button.
     * 
     * @return the entry pane
     */
    private JPanel createEntryPane() {
        JPanel entryPane = new JPanel();
        entryPane.setLayout(new BoxLayout(entryPane, BoxLayout.Y_AXIS));
        entryPane.setBorder(new TitledBorder("Entry Pane"));
        
        // Create the coordinate editor
        var coordinateEditor = new JCoordinateEditor(false);
        coordinateEditor.setCoordinates(startingPoint);
        entryPane.add(coordinateEditor);
        
        // Create the Accept button
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        var acceptButton = new JButton("Accept");
        acceptButton.addActionListener(e -> setPointSelection(coordinateEditor.getCoordinates()));

        buttonPane.add(acceptButton);
        
        entryPane.add(buttonPane);
        entryPane.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 150));
        
        return entryPane;
    }
    
    /**
     * Creates the selection pane showing the currently selected coordinate.
     * 
     * @return the selection pane
     */
    private JPanel createSelectionPane() {
        JPanel selectionPane = new JPanel();
        selectionPane.setLayout(new BoxLayout(selectionPane, BoxLayout.X_AXIS));
        selectionPane.setBorder(new TitledBorder("Selection Pane"));
        
        // Create header label
        var details = new AttributePanel();
        selectionPane.add(details);
        selectedCoordinateLabel = details.addTextField("Selected Point", "None", null);
        distanceLabel = new JDoubleLabel(StyleManager.DECIMAL3_KM);
        details.addLabelledItem("Leg Distance (km)", distanceLabel);
        
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        // Create Add button
        addButton = new JButton("Add");
        addButton.setEnabled(false);
        addButton.addActionListener(e -> handleAdd());
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(addButton);
        
        // Create Remove button
        removeButton = new JButton("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> handleRemove());
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createVerticalGlue());
        
        selectionPane.add(buttonPanel);
        
        return selectionPane;
    }
    
    /**
     * Creates the table pane displaying the proposed route legs.
     * 
     * @return the table pane
     */
    private JPanel createTablePane() {
        JPanel tablePane = new JPanel(new BorderLayout());
        tablePane.setBorder(new TitledBorder("Path Table"));
        
        // Create the table model and table
        legTableModel = new LegTableModel();
        legTable = new JTable(legTableModel);
        legTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        legTable.getSelectionModel().addListSelectionListener(e -> updateRemoveButton());
        
        // Configure table renderers for better appearance
        var distanceRenderer = new NumberCellRenderer(3);
        distanceRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        legTable.getColumnModel().getColumn(2).setCellRenderer(distanceRenderer);
        legTable.getColumnModel().getColumn(3).setCellRenderer(distanceRenderer);
        
        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(legTable);
        tablePane.add(scrollPane, BorderLayout.CENTER);
        
        return tablePane;
    }
    
    /**
     * Sets the selected coordinate in the selection pane.
     * @param newPoint the coordinate to set
     */
    private void setPointSelection(Coordinates newPoint) {
        selectedCoordinate = newPoint;
        selectedCoordinateLabel.setText(selectedCoordinate.getFormattedString());

        var lastPoint = startingPoint;
        var points = legTableModel.getAllLegs();
        if (!points.isEmpty()) {
            RoutePoint lastLeg = points.get(points.size() - 1);
            lastPoint = lastLeg.getCoordinates();
        }
        distanceLabel.setValue(selectedCoordinate.getDistance(lastPoint));
        addButton.setEnabled(legTableModel.getRowCount() < maxLeg);
    }
    
    /**
     * Handles the Add button action.
     * Adds the selected coordinate to the route table and calculates distances.
     */
    private void handleAdd() {
        if (selectedCoordinate == null) {
            return;
        }
        
        double legDistance;
        double totalDistance;
        var currentRoute = legTableModel.getAllLegs();
        
        if (currentRoute.isEmpty()) {
            // First point - no leg distance
            legDistance = selectedCoordinate.getDistance(startingPoint);
            totalDistance = legDistance;
        } else {
            // Calculate distance from last point
            RoutePoint lastLeg = currentRoute.get(currentRoute.size() - 1);
            legDistance = lastLeg.getCoordinates().getDistance(selectedCoordinate);
            totalDistance = lastLeg.getTotalDistance() + legDistance;
        }
        
        // Create and add the route leg
        RoutePoint newLeg = new RoutePoint("#" + (currentRoute.size() + 1), selectedCoordinate,
                                    legDistance, totalDistance);
        legTableModel.addLeg(newLeg);
        
        // Clear the selection and update state
        selectedCoordinate = null;
        selectedCoordinateLabel.setText("None");
        addButton.setEnabled(false);
        
        updateMandatoryState();
    }
    
    /**
     * Handles the Remove button action.
     * Removes the selected row from the table and recalculates distances.
     */
    private void handleRemove() {
        int selectedRow = legTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        
        // Remove from local route list
        legTableModel.removeLeg(selectedRow);
        
        // Recalculate distances for remaining legs
        legTableModel.recalculateDistances(startingPoint);
        
        updateMandatoryState();
    }
    
    /**
     * Updates the enabled state of the Remove button based on table selection.
     */
    private void updateRemoveButton() {
        removeButton.setEnabled(legTable.getSelectedRow() >= 0);
    }
    
    /**
     * Updates the mandatory state and enables/disables the next button.
     */
    private void updateMandatoryState() {
        boolean hasRoute = !legTableModel.getAllLegs().isEmpty();
        setMandatoryDone(hasRoute);
    }
    
    /**
     * Updates the mission state with the proposed route.
     * This is called by the wizard when the step is complete.
     * 
     * @param state the mission data bean to update
     */
    @Override
    public void updateState(MissionDataBean state) {
        var points = legTableModel.getAllLegs().stream()
            .map(RoutePoint::getCoordinates)
            .toList();
        state.setRoutePoints(points);
    }

    @Override
    public void clearState(MissionDataBean state) {
        legTableModel.clear();

        super.clearState(state);
    }
}
