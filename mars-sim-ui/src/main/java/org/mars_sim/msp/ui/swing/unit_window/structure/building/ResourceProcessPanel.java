/*
 * Mars Simulation Project
 * ResourceProcessPanel.java
 * @date 2023-02-20
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.JProcessButton;
import org.mars_sim.msp.ui.swing.utils.UnitTableLauncher;
import org.mars_sim.msp.ui.swing.utils.UnitModel;

/**
 * Creates a JPanel that will render a list of ResourceProcesses in a JTable.
 * This includes creating a dynamic tooltip.
 */
public class ResourceProcessPanel extends JPanel {
    private static final Icon RED_DOT = ImageLoader.getIconByName("dot/red");
    private static final Icon GREEN_DOT = ImageLoader.getIconByName("dot/green");

    private static final String KG_SOL = " kg/sol";
	private static final String BR = "<br>";
	private static final String INPUTS = "&emsp;&emsp;&nbsp;Inputs:&emsp;";
	private static final String OUTPUTS = "&emsp;&nbsp;&nbsp;Outputs:&emsp;";
	private static final String SPACES = "&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;";
	private static final String PROCESS = "&emsp;&nbsp;Process:&emsp;";
	private static final String BUILDING_HEADER = "&emsp;&nbsp;Building:&emsp;";
	private static final String POWER_REQ = "Power Req:&emsp;";
	private static final String NOTE = "&emsp;<i>Note:  * denotes an ambient resource</i>";

    /**
     * Private table model to manage the Resource Processes. It  c an act it 2 modes
     * - Single building mode
     * - Multipel builbing mode
     */
	private static class ResourceProcessTableModel extends AbstractTableModel
                implements UnitModel {
		private static final int RUNNING_STATE = 0;
        private static final int PROCESS_NAME = 1;
        private static final int BUILDING_NAME = 2;

        private List<ResourceProcess> processes = new ArrayList<>();

        private Building mainBuilding;
        private List<Building> buildings;

		public ResourceProcessTableModel(Building building, List<ResourceProcess> source) {
			processes = new ArrayList<>(source);
            mainBuilding = building;
		}

        public ResourceProcessTableModel(Map<Building, List<ResourceProcess>> buildingProcs) {
            // Unpacke map into a single list
            buildings = new ArrayList<>();
            for(Entry<Building, List<ResourceProcess>> entry : buildingProcs.entrySet()) {
                for(ResourceProcess p : entry.getValue()) {
                    processes.add(p);
                    buildings.add(entry.getKey());
                }
            }
        }

        @Override
		public int getRowCount() {
			return processes.size();
		}

        @Override
		public int getColumnCount() {
            if (buildings == null)
			    return 2;
            else
                return 3;
		}

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 0);
        }

        @Override
		public Class<?> getColumnClass(int columnIndex) {
            int realColumn = getPropFromColumn(columnIndex);
            switch(realColumn) {
                case RUNNING_STATE: return Boolean.class;
                case PROCESS_NAME: return String.class;
                case BUILDING_NAME: return String.class;
                default:
                    throw new IllegalArgumentException("Column unknown " + columnIndex);
            }
		}

        @Override
		public String getColumnName(int columnIndex) {
            int realColumn = getPropFromColumn(columnIndex);
            switch(realColumn) {
                case RUNNING_STATE: return "Active";
                case PROCESS_NAME: return "Process";
                case BUILDING_NAME: return "Building";
                default:
                    throw new IllegalArgumentException("Column unknown " + columnIndex);
            }
		}

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            ResourceProcess p = processes.get(rowIndex);
            p.setProcessRunning((boolean) aValue);
        }

        @Override
		public Object getValueAt(int row, int column) {
            ResourceProcess p = processes.get(row);
            int realColumn = getPropFromColumn(column);
            switch(realColumn) {
                case RUNNING_STATE: return p.isProcessRunning();
                case PROCESS_NAME: return p.getProcessName();
                case BUILDING_NAME: return buildings.get(row);
                default:
                    throw new IllegalArgumentException("Column unknown " + column);
            }
		}

        /**
         * This maps the column index into the logicla property
         * @param column
         * @return
         */
        private int getPropFromColumn(int column) {
            switch(column) {
                case 0: return RUNNING_STATE;
                case 1: {
                    if (buildings == null)
                        return PROCESS_NAME;
                    else
                        return BUILDING_NAME;
                }
                case 2: return PROCESS_NAME;
                default: return -1;
            }
        }

        /**
         * Get the associated process object
         */
        ResourceProcess getProcess(int rowIndex) {
            return processes.get(rowIndex);
        }

        /**
         * Get the building hosting a process
         * @param rowIndex
         * @return
         */
        Building getBuilding(int rowIndex) {
            if (buildings == null) 
                return mainBuilding;
            else
                return buildings.get(rowIndex);
        }

        @Override
        public Unit getAssociatedUnit(int row) {
            return getBuilding(row);
        }
	}

    /**
     * Render a booena value displaying Green/Red dots
     */
    private static class RunningCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment( JLabel.CENTER );

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(null);
            if (value instanceof Boolean && (Boolean)value) {
                setIcon(GREEN_DOT);
            }
            else {
                setIcon(RED_DOT);
            }
            return this;
        }        
    }

    /**
     * Allows a cell to be editted
     */
    private static class RunningCellEditor extends DefaultCellEditor {

        public RunningCellEditor() {
            super(new JCheckBox());
        }

        private JProcessButton button;
        private boolean selected;
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                boolean isSelected,
                                int row, int column) {
            selected = (boolean) value;

            button = new JProcessButton();
            button.setRunning(selected);
            button.addActionListener(e -> {
                selected = !selected;
                button.setRunning(selected);

                // Stop after one click
                stopCellEditing();
            });

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return selected;
        }

    }

    private ResourceProcessTableModel model;

    /**
     * Create a resource panel for a single Build
     */
    public ResourceProcessPanel(Building building, List<ResourceProcess> source) {
        
        model = new ResourceProcessTableModel(building, source);

        buildUI();
    }

    /**
     * Create a resource panel that encompasses multiple Buildings each with dedciated Resoruce Processes.
     * @param processes Map.
     */
    public ResourceProcessPanel(Map<Building, List<ResourceProcess>> processes, MainDesktopPane desktop) {
        model = new ResourceProcessTableModel(processes);

        JTable table = buildUI();

        // In the multi-building mode add a mouse listner to open Details window
        table.addMouseListener(new UnitTableLauncher(desktop));
    }

    private JTable buildUI() {
        // Create scroll panel for storage table
		JScrollPane scrollPanel = new JScrollPane();
	    scrollPanel.getViewport().setOpaque(false);
	    scrollPanel.setOpaque(false);


		JTable pTable = new JTable(model) {
            //Implement table cell tool tips.           
            public String getToolTipText(MouseEvent e) {
                Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                if (rowIndex < 0) {
                    return null;
                }
                rowIndex = getRowSorter().convertRowIndexToModel(rowIndex);
                int colIndex = columnAtPoint(p);

                if (colIndex == 0) {
                    return Msg.getString("ResourceProcessPanel.tooltip.toggling");
                }
                // Only display tooltip in last column
                if ((colIndex-1) != model.getColumnCount()) {
                    return null;
                }

                return generateToolTip(model.getProcess(rowIndex), model.getBuilding(rowIndex));
            }
        };

		pTable.setCellSelectionEnabled(false);
		pTable.setAutoCreateRowSorter(true);
		scrollPanel.setViewportView(pTable);

        TableColumnModel columnModel = pTable.getColumnModel();
        columnModel.getColumn(0).setCellRenderer(new RunningCellRenderer());
        columnModel.getColumn(0).setCellEditor(new RunningCellEditor());
        columnModel.getColumn(0).setMaxWidth(50);

        setLayout(new BorderLayout());
        add(scrollPanel, BorderLayout.CENTER);

        return pTable;
    }

    /**
     * Update the status of any resource processes
     */
    public void update() {
        model.fireTableDataChanged();
    }
    
    private String generateToolTip(ResourceProcess process, Building building) {

        // NOTE: internationalize the resource processes' dynamic tooltip.
        StringBuilder result = new StringBuilder("<html>");
        // Future: Use another tool tip manager to align text to improve tooltip readability			
        result.append(PROCESS).append(process.getProcessName()).append(BR);
        result.append(BUILDING_HEADER).append(building.getNickName()).append(BR);
        result.append(POWER_REQ).append(StyleManager.DECIMAL_KW.format(process.getPowerRequired()))
        .append(BR);

        result.append(INPUTS);
        boolean firstItem = true;
        boolean hasAmbient = false;
        for(Integer resource: process.getInputResources()) {
            if (!firstItem) 
                result.append(SPACES);
            double rate = process.getMaxInputRate(resource) * 1000D;
            String rateString = StyleManager.DECIMAL_PLACES2.format(rate);

            result.append(ResourceUtil.findAmountResource(resource).getName());
            if (process.isAmbientInputResource(resource)) {
                result.append("*");
                hasAmbient = true;
            }
            result.append(" @ ").append(rateString).append(KG_SOL).append(BR);
            firstItem = false;    
        }

        result.append(OUTPUTS);
        firstItem = true;
        for(Integer resource : process.getOutputResources()) {
            if (!firstItem)
                result.append(SPACES);
            double rate = process.getMaxOutputRate(resource) * 1000D;
            String rateString = StyleManager.DECIMAL_PLACES2.format(rate);
            result.append(ResourceUtil.findAmountResource(resource).getName())
                .append(" @ ").append(rateString).append(KG_SOL).append(BR);
            firstItem = false;    
        }
        // Add a note to denote an ambient input resource
        if (hasAmbient)
            result.append(NOTE);
        result.append("</html>");   
        
        return result.toString();
    }
}
