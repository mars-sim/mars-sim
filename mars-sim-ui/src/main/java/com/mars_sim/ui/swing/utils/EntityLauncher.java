/*
 * Mars Simulation Project
 * UnitTableLauncher.java
 * @date 2023-02-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableModel;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.ui.swing.MainDesktopPane;

/**
 * This class listens for double click event on a JTable. When an event triggers; a UnitDetail window
 * is launched. The JTable has to be using a UnitTable model that allows the associated Unit
 * of the selected Row.
 * 
 * @see EntityModel
 */
public class EntityLauncher extends MouseInputAdapter {

    private static final String TOOLTIP = "Double click to view details.";
    private static SimLogger logger = SimLogger.getLogger(EntityLauncher.class.getName());

    /**
     * Attached the launcher to a JTable
     * @param table Table to monitor for double clicks
     * @param desktop Parent desktop to handle view requests
     */
    public static void attach(JTable table, MainDesktopPane desktop) {
        if (table.getModel() instanceof EntityModel) {
            table.addMouseListener(new EntityLauncher(desktop));
            table.setToolTipText(TOOLTIP);
        }
        else {
            logger.warning("Table " + table.toString() + " does not use an EntityModel");
        }
    }

    private MainDesktopPane desktop;


    /**
     * Creates a launcher that will create a UnitDetail window.
     * 
     * @param desktop
     */
    private EntityLauncher(MainDesktopPane desktop) {
        this.desktop = desktop;
    }

    /**
     * Catches the double click mouse event. The component under the click event is retrieved
     * which should be a JTable; from this the assigned UnitModel is used to find the
     * associated Unit. This desktop is that used to open the appropriate Unit window.
     * This method supports the JTable being sorted.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isConsumed()) {
            JTable table = (JTable) e.getComponent();
            // Get the mouse-selected row
            int r = table.getSelectedRow();
            RowSorter<? extends TableModel> sorter = table.getRowSorter();
            if (sorter != null && r >= 0) {
                r = sorter.convertRowIndexToModel(r);
            }
            EntityModel model = (EntityModel)table.getModel();
            desktop.showDetails(model.getAssociatedEntity(r));
        }
    }
}
