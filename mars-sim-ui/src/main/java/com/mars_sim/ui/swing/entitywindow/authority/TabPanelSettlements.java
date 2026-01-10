/*
 * Mars Simulation Project
 * TabPanelSettlements.java
 * @date 2025-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.authority;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * This tab shows Settlements and Arriving Settlements associated with an Authority.
 * This is displayed as a table supporting clicking to open the an entity window.
 */
@SuppressWarnings("serial")
class TabPanelSettlements extends EntityTableTabPanel<Authority>
        implements EntityManagerListener {

    private SettlementModel model;

    public TabPanelSettlements(Authority authority, UIContext context) {
        super(
            Msg.getString("Settlement.plural"), // Tab title
            ImageLoader.getIconByName("settlement"),          // Tab icon
            null,          // Tab tooltip
            authority, context
        );
    }

    /**
     * Table model showing Settlements and Arriving Settlements for the Authority.
     */
    @SuppressWarnings("serial")
    private static class SettlementModel extends AbstractTableModel
            implements EntityModel {
        private List<Entity> settlements = new ArrayList<>();

        private void addEntity(Entity e) {
            settlements.add(e);
        }

        @Override
        public int getRowCount() {
            return settlements.size();
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> Msg.getString(("Entity.name"));
                case 1 -> "Status";
                case 2 -> Msg.getString("Settlement.population");
                default -> null;
            };
        }

        @Override
        public int getColumnCount() {
            return 3; // Example: Name and Population
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            var settlement = getAssociatedEntity(rowIndex);
            return switch (columnIndex) {
                case 0 -> settlement.getName();
                case 1 -> (settlement instanceof Settlement ? "Established" : "Arriving");
                case 2 -> {
                    if (settlement instanceof Settlement s) {
                        yield s.getCitizens().size();
                    }
                    else if (settlement instanceof ArrivingSettlement a) {
                        yield a.getPopulationNum();
                    }
                    else {
                        yield 0;
                    }
                }
                default -> null;
            };
        }

        @Override
        public Entity getAssociatedEntity(int row) {
            return settlements.get(row);
        }
    }

    /**
     * Return a model of Settlements and Arriving Settlements for the Authority.
     */
    @Override
    protected TableModel createModel() {
        model = new SettlementModel();
        var authority = getEntity();

        // Load established settlements
        var uMgr = getContext().getSimulation().getUnitManager();
        uMgr.getSettlements().stream()
            .filter(s -> s.getReportingAuthority().equals(authority))
            .forEach(model::addEntity);

        // Load arriving settlements
        var tMgr = getContext().getSimulation().getTransportManager();
        tMgr.getTransportItems().stream()
            .filter(ArrivingSettlement.class::isInstance)
            .map(ArrivingSettlement.class::cast)
            .filter(a -> a.getSponsorCode().equals(authority.getName()))
            .forEach(model::addEntity);

        // Connect up listener to update the table when entities change
        var sim = getContext().getSimulation();
        sim.getUnitManager().addEntityManagerListener(UnitType.SETTLEMENT, this);
        return model;
    }

    /**
     * Clear down the listeners
     */
    @Override
    public void destroy() {
        var sim = getContext().getSimulation();
        sim.getUnitManager().removeEntityManagerListener(UnitType.SETTLEMENT, this);
        super.destroy();
    }

    /**
     * New Settlement has been added
     * @param newEntity
     */
    @Override
    public void entityAdded(Entity newEntity) {
        if (newEntity instanceof Settlement s
                    && s.getReportingAuthority().equals(getEntity())) {

            // Remove the matching Transportable if it 
            model.settlements.removeIf(e -> {
                if (e instanceof ArrivingSettlement a) {
                    return a.getName().equals(s.getName());
                }
                return false;
            });

            // Add in the real Settlement
            model.addEntity(s);
            model.fireTableDataChanged();
        }
    }

    /**
     * Settlement has been removed
     * @param removedEntity
     */
    @Override
    public void entityRemoved(Entity removedEntity) {
        if (removedEntity instanceof Settlement s
                    && s.getReportingAuthority().equals(getEntity())) {
            // Remove the settlement from the model
            model.settlements.remove(s);
            model.fireTableDataChanged();
        }
    }
}
