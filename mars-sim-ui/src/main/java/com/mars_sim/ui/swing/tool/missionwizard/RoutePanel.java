/*
 * Mars Simulation Project
 * RoutePanel.java
 * @date 2026-02-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.tool.map.MapPanel;
import com.mars_sim.ui.swing.tool.map.RoutePath;
import com.mars_sim.ui.swing.tool.map.RoutePathLayer;
import com.mars_sim.ui.swing.tool.map.UnitMapLayer;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;
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
    
    private MapPanel mapPanel;
    private RoutePathLayer navpointLayer;
    private RoutePathAdapter routePath;
    
    /**
     * Constructor.
     * 
     * @param wizard the create mission wizard
     * @param state the mission data bean
     */
    public RoutePanel(MissionCreate wizard, MissionDataBean state, UIContext context) {
        super(ID, wizard);

        this.maxLeg = 10;
        var mType = state.getMissionType();
        if (mType == MissionType.AREOLOGY || mType == MissionType.BIOLOGY
                || mType == MissionType.METEOROLOGY) {
            this.maxLeg = 1;
        }

        // Create the model to hold the new route
        legTableModel = new LegTableModel();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        var mapPane = initMapPane(state, context);
        add(mapPane);

        // Create the selection pane (displays selected coordinate)
        JPanel selectionPane = createSelectionPane();
        add(selectionPane);
        
        // Create the path table pane (displays route legs)
        JPanel tablePane = createTablePane();
        add(tablePane);
    }
    
    
	private JComponent initMapPane(MissionDataBean state, UIContext context) {
		// Create the map panel.
		mapPanel = new MapPanel(context);
		mapPanel.setBackground(new Color(0, 0, 0, 128));
		mapPanel.setOpaque(false);
					
		// Always add unit layer
		mapPanel.addMapLayer(new UnitMapLayer(mapPanel));

		// Lastly add navpoint layer
		navpointLayer = new RoutePathLayer(mapPanel);
		mapPanel.addMapLayer(navpointLayer);
        mapPanel.setMouseClickListener(c -> setPointSelection(c));

        // Add a single route path that is a proxy to the leg model
        routePath = new RoutePathAdapter(state.getStartingSettlement().getCoordinates(), legTableModel);
        navpointLayer.addPath(routePath);

		var mapPane = new JPanel(new BorderLayout());
		mapPane.setBorder(SwingHelper.createLabelBorder("Route"));
        
		var dims = new Dimension(10, 200);
		mapPane.setPreferredSize(dims);
		mapPane.setMinimumSize(dims);
		mapPane.add(mapPanel, BorderLayout.CENTER);

       	return mapPane;	
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
        legTable = new JTable(legTableModel);
        legTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        legTable.getSelectionModel().addListSelectionListener(e -> legSelectionChanged());
        
        // Configure table renderers for better appearance
        var distanceRenderer = new NumberCellRenderer(3);
        distanceRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        legTable.getColumnModel().getColumn(2).setCellRenderer(distanceRenderer);
        legTable.getColumnModel().getColumn(3).setCellRenderer(distanceRenderer);
        
        // Create scroll pane for table with a fixed size
        JScrollPane scrollPane = new JScrollPane(legTable);
        tablePane.add(scrollPane, BorderLayout.CENTER);
        var dims = new Dimension(1024, 200);
		scrollPane.setMaximumSize(dims);
        return tablePane;
    }
    
    /**
     * Sets the selected coordinate in the selection pane.
     * @param newPoint the coordinate to set
     */
    private void setPointSelection(Coordinates newPoint) {
        double legDistance;
        double totalDistance;
        var currentRoute = legTableModel.getAllLegs();
        if (currentRoute.isEmpty()) {
            // First point - no leg distance
            legDistance = newPoint.getDistance(routePath.getStart());
            totalDistance = legDistance;
        } else {
            // Calculate distance from last point
            RoutePoint lastLeg = currentRoute.get(currentRoute.size() - 1);
            legDistance = lastLeg.getCoordinates().getDistance(newPoint);
            totalDistance = lastLeg.getTotalDistance() + legDistance;
        }
        
        // Create and add the route leg
        RoutePoint newLeg = new RoutePoint("#" + (currentRoute.size() + 1), newPoint,
                                    legDistance, totalDistance);
        routePath.setPending(newLeg);
        navpointLayer.setSelectedNavpoint(newLeg);
        mapPanel.repaint();

        selectedCoordinateLabel.setText(newPoint.getFormattedString());
        distanceLabel.setValue(newLeg.getLegDistance());

        addButton.setEnabled(legTableModel.getRowCount() < maxLeg);
    }
    
    /**
     * Handles the Add button action.
     * Adds the selected coordinate to the route table and calculates distances.
     */
    private void handleAdd() {
        // Move the pending point into the model
        var newLeg = routePath.getPending();
        routePath.setPending(null);                  
        legTableModel.addLeg(newLeg);
        
        // Clear the selection and update state
        selectedCoordinateLabel.setText("None");
        addButton.setEnabled(false);
        
        navpointLayer.setSelectedNavpoint(null);
        updateDisplay();
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
        legTableModel.recalculateDistances(routePath.getStart());
        
        updateDisplay();
    }
    
    /**
     * Updates the enabled state of the Remove button based on table selection.
     */
    private void legSelectionChanged() {
        removeButton.setEnabled(legTable.getSelectedRow() >= 0);

        SurfacePOI selected = null;
        int selectedRow = legTable.getSelectedRow();
        if (selectedRow >= 0) {
            selected = legTableModel.getAllLegs().get(selectedRow);
        }
        else {
            // If no row is selected, show the pending point if it exists
            selected = routePath.getPending();
        }
        
        // Remove from local route list
        navpointLayer.setSelectedNavpoint(selected);
        mapPanel.repaint();
    }
    
    /**
     * Updates the mandatory state and enables/disables the next button.
     */
    private void updateDisplay() {
        boolean hasRoute = !legTableModel.getAllLegs().isEmpty();
        setMandatoryDone(hasRoute);

        mapPanel.repaint();
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

    /**
     * Release any listeners from the route panel.
     */
    @Override
    protected void release() {
        mapPanel.destroy();
        super.release();
    }

    /**
     * This adapters the internal state to a RoutePath so it can be rendered
     */
    private static class RoutePathAdapter implements RoutePath {
        private final Coordinates startingPoint;
        private final LegTableModel legTableModel;
        private RoutePoint pendingNavPoint;

        public RoutePathAdapter(Coordinates startingPoint, LegTableModel legTableModel) {
            this.startingPoint = startingPoint;
            this.legTableModel = legTableModel;
        }

        public RoutePoint getPending() {
            return pendingNavPoint;
        }

        public void setPending(RoutePoint pending) {
            this.pendingNavPoint = pending;
        }

        @Override
        public String getContext() {
            return "New Mission Route";
        }

        @Override
        public Coordinates getStart() {
            return startingPoint;
        }

        @Override
        public List<? extends SurfacePOI> getNavpoints() {
            if (pendingNavPoint != null) {
                // Create a temporary list with the pending navpoint added
                var tempList = new ArrayList<>(legTableModel.getAllLegs());
                tempList.add(pendingNavPoint);
                return tempList;
            }
            return legTableModel.getAllLegs();
        }
    }
}
