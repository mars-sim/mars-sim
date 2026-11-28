/*
 * Mars Simulation Project
 * TabPanelLoading.java
 * @date 2026-11-26
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.resource.ResourceType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.task.LoadingController;
import com.mars_sim.core.vehicle.task.LoadingController.LoadItem;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AbstractEnhancedTableModel;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This shows a TabPanel for displaying the loading of items onto a vehicle.
 * It uses the associated LoadingController to display queued and loaded items.
 */
public class TabPanelLoading extends EntityTabPanel<Vehicle> 
		implements EntityListener {

    private LoadItemModel queued;
    private LoadingController controller;
    private LoadItemModel completed;

    public TabPanelLoading(Vehicle vehicle, UIContext context) {
        super(
            "Loading",
			ImageLoader.getIconByName("cart"),        	
        	"Loading items for the vehicle",
        	context, vehicle,
            false
        );    
    }

    /**
     * Build a UI panel to show 2 tables of the Load Items.
     */
    @Override
    protected void buildUI(JPanel centerContentPanel) {
        Box box = Box.createVerticalBox();
        centerContentPanel.add(box);
        
        queued = new LoadItemModel();
        box.add(SwingHelper.createScrolledTable(queued, getContext(), "Queued", null));

        completed = new LoadItemModel();
        box.add(SwingHelper.createScrolledTable(completed, getContext(), "Completed", null));

        checkLoadingStatus();
    }

    /**
     * Check if loading is active and the best Loading controller.
     * This implements a latch pattern so the last controller is always shown.
     */
    private void checkLoadingStatus() {
        if (controller == null) {
            controller = getEntity().getLoadingPlan();
            if (controller != null) {
                reloadModels();
            }
        }
        else {
            var newController = getEntity().getLoadingPlan();
            if ((newController != null) && !controller.equals(newController)) {
                controller = newController;
                reloadModels();
            }
        }
    }

    /**
     * Force the reloaded of all table models from the controller.
     */
    private void reloadModels() {
        if (controller != null) {
            queued.resetItems(controller.getToLoad());
            completed.resetItems(controller.getLoaded());
        }
        else {
            // No active loading controller, clear the tables.
            // Should never happen
            queued.resetItems(Collections.emptyList());
            completed.resetItems(Collections.emptyList());
        }
    }

    @Override
    public void entityUpdate(EntityEvent event) {
        switch (event.getType()) {
            case EntityEventType.STATUS_EVENT -> checkLoadingStatus();
            case LoadingController.LOAD_EVENT -> reloadModels();
            case EntityEventType.INVENTORY_RESOURCE_EVENT ->
                SwingHelper.runInEDT(() -> queued.fireTableDataChanged());
        }
    }

    private static class LoadItemModel extends AbstractEnhancedTableModel {
        private static final ColumnSpec[] COLUMN_SPECS = {
            new ColumnSpec("Resource", String.class),
            new ColumnSpec("Required", Double.class),
            new ColumnSpec("Loaded", Double.class),
            new ColumnSpec("Mandatory", Boolean.class)
        };

        private List<LoadItem> items = Collections.emptyList();
        
        private LoadItemModel() {
            super(COLUMN_SPECS);
        }

        private void resetItems(List<LoadItem> newItems) {
            items = newItems;
            SwingHelper.runInEDT(() -> fireTableDataChanged());
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= items.size()) {
                return null;
            }

            LoadItem item = items.get(rowIndex);
            switch (columnIndex) {
                case 0: return ResourceType.getName(item.getResourceID());
                case 1: return item.getRequired();
                case 2: return item.getLoaded();
                case 3: return item.isMandatory();
                default: return null;
            }
        }
    }
}
