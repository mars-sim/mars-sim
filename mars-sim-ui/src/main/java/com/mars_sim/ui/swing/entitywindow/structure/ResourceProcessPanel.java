/*
 * Mars Simulation Project
 * ResourceProcessPanel.java
 * @date 2026-07-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resourceprocess.ResourceProcess;
import com.mars_sim.core.resourceprocess.ResourceProcess.ProcessState;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.ToolTipTableModel;
import com.mars_sim.ui.swing.utils.EntityLauncher;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.JProcessButton;

/**
 * Creates a JPanel that will render a list of ResourceProcesses in a JTable.
 * This includes creating a dynamic tooltip.
 */
@SuppressWarnings("serial")
public class ResourceProcessPanel extends JPanel {
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ResourceProcessPanel.class.getName());

    public static final Icon IDLE_DOT = ImageLoader.getIconByName("dot/yellow");

    private static final String KG_SOL = " kg/sol";
	private static final String BR = "<br>";
 	private static final String NON_BREAKING_SPACE = "&nbsp;";
	private static final String EN_SPACE = "&ensp;";
 	private static final String EM_SPACE = "&emsp;"; // typically twice as wide as EN_SPACE or four times as wide as NON_BREAKING_SPACE
	private static final String TABS = EM_SPACE + EM_SPACE + EM_SPACE + EM_SPACE + EM_SPACE + EM_SPACE + EN_SPACE; //"&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;";
	
	private static final String INPUTS = EM_SPACE + EM_SPACE + NON_BREAKING_SPACE + "Inputs:" + EM_SPACE;
	private static final String OUTPUTS = EM_SPACE + NON_BREAKING_SPACE + "Outputs:" + EM_SPACE;

	private static final String PROCESS = EM_SPACE + NON_BREAKING_SPACE + "Process:" + EM_SPACE;
	private static final String BUILDING_HEADER = EM_SPACE + NON_BREAKING_SPACE + "Building:" + EM_SPACE;
	private static final String MAX_NUM_MODULES = "Max # Modules:" + EM_SPACE;
	private static final String POWER_REQ   = "Power Req:" + EM_SPACE;
	private static final String NOTE = EM_SPACE + "<i>Note:  * denotes an ambient resource</i>";

    private ResourceProcessTableModel resourceProcessTableModel;

	private JSpinner topSpinner;
	
	protected JTable pTable;

    /**
     * Creates a resource panel for a single Build.
     */
    public ResourceProcessPanel(Building building, List<ResourceProcess> source) {
        
    	resourceProcessTableModel = new ResourceProcessTableModel(building, source);
     
        buildUI();
    }

    /**
     * Creates a resource panel that encompasses multiple Buildings each with dedicated Resource Processes.
     * 
     * @param processes A map of Buildings and their associated Resource Processes.
     * @param context The UI context.
     */
    public ResourceProcessPanel(Map<Building, List<ResourceProcess>> processes, UIContext context) {
       
    	resourceProcessTableModel = new ResourceProcessTableModel(processes);
    	 
    	buildUI();
        // In the multi-building mode add a mouse listener to open Details window
        EntityLauncher.attach(pTable, context);
    }

    private JTable buildUI() {
        // Create scroll panel for storage table
		JScrollPane scrollPanel = new JScrollPane();
	    scrollPanel.getViewport().setOpaque(false);
	    scrollPanel.setOpaque(false);

		pTable = new JTable(resourceProcessTableModel) {
            // Implement table cell tool tips. 
            @Override          
            public String getToolTipText(MouseEvent e) {
                return ToolTipTableModel.extractToolTip(e, this);
            }
        };
        
        pTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);   
		pTable.setCellSelectionEnabled(false);
		pTable.setAutoCreateRowSorter(true);
		
		scrollPanel.setViewportView(pTable);
		
        TableColumnModel columnModel = pTable.getColumnModel();
        columnModel.getColumn(0).setCellRenderer(new RunningCellRenderer());
        columnModel.getColumn(0).setCellEditor(new RunningCellEditor());
        columnModel.getColumn(0).setPreferredWidth(20);
        columnModel.getColumn(1).setPreferredWidth(90);

        // Initialize with min 1, max 100, step 1
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 200, 1);
        
        topSpinner = new JSpinner(spinnerModel);	
		
     // In the renderer's constructor or setup:
        pTable.addPropertyChangeListener("tableCellEditor", e -> {
        	topSpinner.setEnabled(!pTable.isEditing());
        });
        
        topSpinner.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

		int spinnerHeight = topSpinner.getPreferredSize().height + 2;  // +2 for cell border/margin
        pTable.setRowHeight(spinnerHeight);
        
		// 1. Get the editor component of your spinner:
		Component spinnerEditor = topSpinner.getEditor();
		// 2. Get the text field of your spinner's editor:
		JFormattedTextField jftf = ((JSpinner.DefaultEditor) spinnerEditor).getTextField();
		// 3. Set a default size to the text field:
//		jftf.setColumns(1);
		// 4. Set the horizontal alignment
		jftf.setHorizontalAlignment(SwingConstants.CENTER);
		jftf.setAlignmentY(TOP_ALIGNMENT);
		
		columnModel.getColumn(2).setCellRenderer(new SpinnerRenderer(topSpinner));
        columnModel.getColumn(2).setCellEditor(new SpinnerEditor());
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                setHorizontalAlignment(SwingConstants.CENTER);
                setVerticalAlignment(SwingConstants.CENTER);
                
                // Call super to set the text/value
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };

        // Apply renderer to the spinner column
        pTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
	

        columnModel.getColumn(2).setPreferredWidth(50);
        		
        columnModel.getColumn(3).setPreferredWidth(150);
        columnModel.getColumn(4).setPreferredWidth(40);
        columnModel.getColumn(5).setPreferredWidth(40);
        columnModel.getColumn(6).setPreferredWidth(40);
        columnModel.getColumn(7).setPreferredWidth(40);
        
        setLayout(new BorderLayout());
        add(scrollPanel, BorderLayout.CENTER);

        return pTable;
    }

    /**
     * Sets the level of Effort.
     * 
     * @param level
     */
    public void setLevelOfEffort(int level) {
    	for (ResourceProcess p: resourceProcessTableModel.getProcesses()) {
    		p.setLevel(level);
    	}
    }
    
    /**
     * Updates the status of any resource processes.
     */
    public void update() {
    	int numRow = resourceProcessTableModel.getRowCount();
    	for (int i=0; i< numRow; i++) {	
    		resourceProcessTableModel.fireTableCellUpdated(i, 0);
    		
    		resourceProcessTableModel.fireTableCellUpdated(i, 2);
    		
    		resourceProcessTableModel.fireTableCellUpdated(i, 4);
    		resourceProcessTableModel.fireTableCellUpdated(i, 5);
    		resourceProcessTableModel.fireTableCellUpdated(i, 6);
    	}
    }
	
    /**
     * Private table model to manage the Resource Processes. 
     * - Single building mode
     * - Multiple building mode
     */
	class ResourceProcessTableModel extends AbstractTableModel
                implements EntityModel, ToolTipTableModel {
		private static final int RUNNING_STATE = 0;
        private static final int BUILDING_NAME = 1;		
        private static final int MODULE_NAME = 2;
        private static final int PROCESS_NAME = 3;
        private static final int DUTY_PERCENT = 4;
        private static final int INPUT_SCORE = 5;
        private static final int OUTPUT_SCORE = 6;
        private static final int SCORE = 7;

        private static final String BUILDING = Msg.getString("building.singular");
        private static final String BUILDING_TOOLTIP = Msg.getString("entity.doubleClick");
         
        private Building mainBuilding;
    
        private List<ResourceProcess> processes = new ArrayList<>();

        private List<Building> buildings;
        
        /**
         * Constructor 1 : for one single building.
         * 
         * @param building
         * @param source
         */
		public ResourceProcessTableModel(Building building, List<ResourceProcess> source) {
			processes = new ArrayList<>(source);
            mainBuilding = building;
		}

		/**
		 * Constructor 2 : for the whole settlement.
		 * 
		 * @param buildingProcs
		 */
        public ResourceProcessTableModel(Map<Building, List<ResourceProcess>> buildingProcs) {
            // Unpack map into a single list
            buildings = new ArrayList<>();
            for (Entry<Building, List<ResourceProcess>> entry : buildingProcs.entrySet()) {
                for (ResourceProcess p : entry.getValue()) {
                    processes.add(p);
                    Building building = entry.getKey();
                    if (building != null)
                    	buildings.add(building);
                }
            }
        }
        
        @Override
		public int getRowCount() {
			return processes.size();
		}

        @Override
		public int getColumnCount() {
        	return 8; 
		}

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == RUNNING_STATE || columnIndex == MODULE_NAME);
        }

        @Override
		public Class<?> getColumnClass(int columnIndex) {
            switch(columnIndex) {
                case RUNNING_STATE: return ResourceProcess.ProcessState.class;
                case BUILDING_NAME: return String.class;
                case MODULE_NAME: return JSpinner.class;
                case PROCESS_NAME: return ResourceProcess.class;
                case DUTY_PERCENT: return Double.class; 
                case INPUT_SCORE: return Double.class;
                case OUTPUT_SCORE: return Double.class;
                case SCORE: return Double.class;
                default:
                    throw new IllegalArgumentException("Column unknown " + columnIndex);
            }
		}

        @Override
		public String getColumnName(int columnIndex) {
            switch(columnIndex) {
                case RUNNING_STATE: return "S";
                case BUILDING_NAME: return BUILDING;
                case MODULE_NAME: return "M #";
                case PROCESS_NAME: return "Process";
                case DUTY_PERCENT: return "% Duty";
                case INPUT_SCORE: return "In";
                case OUTPUT_SCORE: return "Out";
                case SCORE: return "Score";
                default:
                    throw new IllegalArgumentException("Column unknown " + columnIndex);
            }
		}

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            ResourceProcess p = processes.get(rowIndex);
            
            if (columnIndex == 0) {
	            ProcessState s = (ProcessState) aValue;
	            p.setProcessState(s);
            }
            
            else if (columnIndex == 2) {
            	int newModules = (Integer) aValue;
            	p.setModules(newModules); 
            }
        }

        @Override
		public Object getValueAt(int row, int column) {
            ResourceProcess p = processes.get(row);

            
            switch(column) {
                case RUNNING_STATE: return p.getState();
                case BUILDING_NAME: return getBuilding(row).getName();
                case MODULE_NAME: return p.getNumModules();
                case PROCESS_NAME: return p;
                case DUTY_PERCENT: return getFormattedScore(p.getPercentDuty());
                case INPUT_SCORE: return getFormattedScore(p.getInputScore());
                case OUTPUT_SCORE: return getFormattedScore(p.getOutputScore());
                case SCORE: return getFormattedScore(p.getOverallScore());
                default:
                    throw new IllegalArgumentException("Column unknown " + column);
            }
		}

        /**
         * Returns the score in a formatted manner for easy reading.
         * 
         * @param score
         * @return
         */
        public double getFormattedScore(double score) {
        	if (score > 1) {
        		return Math.round(score * 10.0)/10.0;
        	}
        	return Math.round(score * 100.0)/100.0;
        }
        
        /**
         * Gets the associated process object.
         */
        public ResourceProcess getProcess(int rowIndex) {
            return processes.get(rowIndex);
        }
        
        /**
         * Gets a list of processes.
         * 
         * @return
         */
        List<ResourceProcess> getProcesses() {
        	return processes;
        }

        /**
         * Gets the building hosting a process.
         * 
         * @param rowIndex
         * @return
         */
        Building getBuilding(int rowIndex) {
            if (buildings == null) 
                return mainBuilding;
        	// Convert to model index — THIS is the critical step
        	int modelIndex = pTable.convertRowIndexToModel(rowIndex);
        	
            return buildings.get(modelIndex);
        }

        @Override
        public Entity getAssociatedEntity(int row) {
            return getBuilding(row);
        }

        @Override
        public String getToolTipAt(int row, int col) {
            if (col == RUNNING_STATE) {
                return switch(getProcess(row).getState()) {
                    case RUNNING -> "Running";
                    case LOCK_ON -> "Lock-On (Always) Running";
                    case IDLE -> "Idle";
                    case INPUTS_UNAVAILABLE -> "Input Resource Unavailable";
                };
            }
            else if (col == MODULE_NAME) {
                return "The number of running modules currently activated";
            }
            // Only display tooltip if hovering over the 3rd column named "Process"
            else if (col == PROCESS_NAME) {
                return generateProcessTooltip(getProcess(row), getBuilding(row));
            }
            else if (col == BUILDING_NAME && buildings != null) {
                return BUILDING_TOOLTIP;
            }

            return null;
        }

        /**
         * Generates the tooltip.
         * 
         * @param process
         * @param building
         * @return
         */
        private String generateProcessTooltip(ResourceProcess process, Building building) {

            // NOTE: internationalize the resource processes' dynamic tooltip.
            StringBuilder result = new StringBuilder("<html>");
            // Future: Use another tool tip manager to align text to improve tooltip readability			
            result.append(PROCESS).append(process.getProcessName()).append(BR);
            result.append(BUILDING_HEADER).append(building.getName()).append(BR);
            result.append(MAX_NUM_MODULES).append(process.getMaxModules()).append(BR);
            result.append(POWER_REQ).append(StyleManager.DECIMAL2_KW.format
            		(process.getkWRequired())).append(BR);

            result.append(INPUTS);
            boolean firstItem = true;
            boolean hasAmbient = false;
            for (Integer resource: process.getInputResources()) {
                if (!firstItem) 
                    result.append(TABS);
                double fullRate = process.getBaseFullInputRate(resource) * 1000D;
                String rateString = StyleManager.DECIMAL_PLACES2.format(fullRate);

                result.append(ResourceUtil.findAmountResource(resource).getName());
                if (process.isAmbientInputResource(resource)) {
                    result.append("*");
                    hasAmbient = true;
                }
                result.append(" - ").append(rateString).append(KG_SOL).append(BR);
                firstItem = false;    
            }

            result.append(OUTPUTS);
            firstItem = true;
            for (Integer resource : process.getOutputResources()) {
                if (!firstItem)
                    result.append(TABS);
                double fullRate = process.getBaseFullOutputRate(resource) * 1000D;
                String rateString = StyleManager.DECIMAL_PLACES2.format(fullRate);
                result.append(ResourceUtil.findAmountResource(resource).getName())
                    .append(" - ").append(rateString).append(KG_SOL).append(BR);
                firstItem = false;    
            }
            // Add a note to denote an ambient input resource
            if (hasAmbient)
                result.append(NOTE);
            result.append("</html>");   
            
            return result.toString();
        }
	}

	private class SpinnerRenderer extends JPanel implements TableCellRenderer {
		
	    private JSpinner spinner;
	    private int currentRow = -1;
	    private int currentCol = -1;

	    public SpinnerRenderer(JSpinner spinner) {
	    	this.spinner = spinner;
	        add(spinner);
	        
	        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
	        editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
	        
	        // This is what makes the arrows interactive
	        spinner.addChangeListener(e -> {
	            if (currentRow >= 0 && !pTable.isEditing()) {
//	                JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, this);
	                // Update the model directly
	                if (pTable != null) {
	                	pTable.setValueAt(spinner.getValue(), currentRow, currentCol);
	                }
	            }
	        });
	    }
	    
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int row, int column) {
	
	    	currentRow = row;
	        currentCol = column;
	        spinner.setValue(value);
	        spinner.setEnabled(table.isCellEditable(row, column));
	        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
	        
	        return this;
	    }
	}
	
	private class SpinnerEditor extends DefaultCellEditor {
		
		private JSpinner spinner;
		private SpinnerNumberModel spinnerModel;
		
        public SpinnerEditor() { //SpinnerNumberModel model) {
            super(new JTextField()); // Required by DefaultCellEditor
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
        	
        	ResourceProcess rp = getProcess(row);
        	int maxModules = rp.getMaxModules();
        	int modules = (Integer) value;
        	logger.info("Selecting '" + rp + "': " + modules + "/" + maxModules + "activated/max modules");
        	
            spinnerModel = new SpinnerNumberModel(modules, 1, maxModules, 1);
            
            spinner = new JSpinner(spinnerModel);	
        	
            spinner.setValue(modules); // Set initial value from cell
            
            return spinner;          // Return the spinner component
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue(); // Return the selected value
        }
        
        /**
         * Gets the associated process object.
         */
        public ResourceProcess getProcess(int rowIndex) {
        	// Convert to model index — THIS is the critical step
        	int modelIndex = pTable.convertRowIndexToModel(rowIndex);

        	// Now safely access the underlying data
        	Object value = pTable.getModel().getValueAt(modelIndex, 3);   
        	
        	return (ResourceProcess)value;
        }
    }
	
    /**
     * Renders a boolean value displaying Green/Red dots.
     */
	private class RunningCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment(SwingConstants.CENTER);

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setText(null);
            
            if (value instanceof ResourceProcess.ProcessState state) {
                var icon = switch(state) {
                    case RUNNING 			-> JProcessButton.RUNNING_DOT;
                    case IDLE 				-> IDLE_DOT;
                    case INPUTS_UNAVAILABLE -> JProcessButton.STOPPED_DOT;
                    case LOCK_ON 			-> JProcessButton.LOCK_ON_DOT;
                };
                setIcon(icon);
            }
            return this;
        }        
    }

    /**
     * Allows a cell to be edited.
     */
	private class RunningCellEditor extends DefaultCellEditor {

        public RunningCellEditor() {
            super(new JCheckBox());
        }

        private JProcessButton button;
        
        private ProcessState selected;
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                boolean isSelected,
                                int row, int column) {
        	
        	selected = (ResourceProcess.ProcessState) value;

            button = new JProcessButton();
            
            button.setIcon(selected);
            
            button.addActionListener(e -> {
            	
            	if (selected == ProcessState.RUNNING) {
            		selected = ProcessState.LOCK_ON;
            	}
            	else if (selected == ProcessState.LOCK_ON) {
            		selected = ProcessState.IDLE;
            	}
//            	else if (selected == ProcessState.INPUTS_UNAVAILABLE) {
//            		selected = ProcessState.INPUTS_UNAVAILABLE;
//            	}
            	else if (selected == ProcessState.IDLE) {
            		selected = ProcessState.RUNNING;
            	}
                
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
}
