/**
 * Mars Simulation Project
 * SettlementTable.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.config;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;

public class SettlementTable extends JTable {

	public static final long serialVersionUID = 1L;

	
	public static java.awt.Color BACK_COLOR = new java.awt.Color(209,103,0);//java.awt.Color.orange;//(229,171,0);//.orange);//(0, 167, 212));
	public static java.awt.Color FORE_COLOR = java.awt.Color.WHITE;
	
	public static final int NUM_DISPLAYING_COLUMNS = 7; // currently not displaying column 7 and 8

	public static final int COLUMN_PLAYER_NAME = 0;
	public static final int COLUMN_SETTLEMENT_NAME = 1;
	public static final int COLUMN_TEMPLATE = 2;
	public static final int COLUMN_POPULATION = 3;
	public static final int COLUMN_BOTS = 4;
	public static final int COLUMN_LATITUDE = 5;
	public static final int COLUMN_LONGITUDE = 6;
	public static final int COLUMN_HAS_MSD = 7;
	public static final int COLUMN_EDIT_MSD = 8;

	private JTableHeader header;
	//private JScrollPane settlementScrollPane;
	private TableCellEditor editor;

	private SettlementTableModel settlementTableModel;
	private SimulationConfig simulationConfig;
	private ScenarioConfigEditorFX configEditor;

	//private List<SettlementRegistry> settlementList;

	//public SettlementTable(SettlementTableModel md) {
	//	super(md);

	public SettlementTable(ScenarioConfigEditorFX configEditor, SettlementTableModel settlementTableModel) {
		super(settlementTableModel);

		this.configEditor = configEditor;
		this.settlementTableModel = settlementTableModel;
		this.simulationConfig = SimulationConfig.instance();

		// 2015-10-06 Added setTableStyle()
        JTable table = new ZebraJTable(settlementTableModel);
        init(table);

	}

	public void init(JTable t) {

		t.setRowSelectionAllowed(true);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setGridColor(java.awt.Color.WHITE);

		header = getTableHeader();
		header.setFont(new Font("Dialog", Font.BOLD, 12));
		header.setBackground(BACK_COLOR);//new java.awt.Color(229,171,0));//.orange);//(0, 167, 212));
		header.setForeground(FORE_COLOR);//java.awt.Color.white); 

		//configEditor.getSettlementScrollPane().setViewportView(this);

		// Create combo box for editing template column in settlement table.
		TableColumn templateColumn = getColumnModel().getColumn(COLUMN_TEMPLATE);
		JComboBoxMW<String> templateCB = new JComboBoxMW<String>();
		SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();
		Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
		while (i.hasNext()) {
			templateCB.addItem(i.next().getTemplateName());
		}

		templateColumn.setCellEditor(new DefaultCellEditor(templateCB));

		editor = getCellEditor();

		// 2015-10-03 Added the edit button
		Action editAction = new javax.swing.AbstractAction() {
		    public void actionPerformed(ActionEvent e){
		        JTable settlementTable = (JTable)e.getSource();
		        int modelRow = Integer.valueOf( e.getActionCommand() );
		        ((SettlementTableModel)settlementTable.getModel()).editMSD(modelRow);
		        settlementTableModel.editMSD(modelRow);
		    }};
		//ButtonColumn buttonColumn = new ButtonColumn(this, editAction, COLUMN_EDIT_MSD);
		//buttonColumn.setMnemonic(KeyEvent.VK_E);

		// 2015-10-03 Determines proper width for each column and center aligns each cell content
        adjustColumn();

	    //CheckBoxRenderer checkBoxRenderer = new CheckBoxRenderer();
	    //getColumnModel().getColumn(COLUMN_HAS_MSD).setCellRenderer(checkBoxRenderer);

	}

	public void setConfigEditor(ScenarioConfigEditorFX configEditor) {
		this.configEditor = configEditor;
	}

	//public void setSettlementList(List<SettlementRegistry> settlementList) {
	//	this.settlementList = settlementList;
	//}

    /*
     * Determines proper width for each column and center aligns each cell content
     */
	// 2015-10-03 Added adjustColumn()
    private void adjustColumn() {//JTable table) {
    	// If all column heads are wider than the column's cells'
        // contents, then you can just use column.sizeWidthToFit().
    	final Object[] longValues = {
    			"Jane123",
    			"Schiaparelli Point",
                "Mars Direct Base (phase 1)",
                new Integer(18),
                new Integer(16),
                new Integer(22),
                new Integer(22),
                //Boolean.TRUE,
                //"Edit "
                };

    	boolean DEBUG = false;
    	//SettlementTableModel model = settlementTableModel; //(SettlementTableModel)table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        TableCellRenderer headerRenderer = getTableHeader().getDefaultRenderer();

       	// 2015-10-03 Align content to center of cell
    	DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
    	defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < NUM_DISPLAYING_COLUMNS; i++) {

            column = getColumnModel().getColumn(i);

        	// 2015-10-03 Align content to center of cell
    		column.setCellRenderer(defaultTableCellRenderer);

            comp = headerRenderer.getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);

            headerWidth = comp.getPreferredSize().width;

            comp = getDefaultRenderer(settlementTableModel.getColumnClass(i)).
                             getTableCellRendererComponent(
                                 this, longValues[i],
                                 false, false, 0, i);

            cellWidth = comp.getPreferredSize().width;

            if (DEBUG) {
                System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            }

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    public java.awt.Component prepareRenderer(
        TableCellRenderer renderer, int row, int column) {
    	java.awt.Component c = super.prepareRenderer(renderer, row, column);
    	// if this is an existing settlement registered by another client machine,
    	if (configEditor.getHasSettlement() && row < configEditor.getSettlementList().size())
    		c.setForeground(new java.awt.Color(209,103,0));//255,60,8));//java.awt.Color.ORANGE); //BLUE);
    	else
    		c.setForeground(BACK_COLOR);//new java.awt.Color(255,143,8)); // pale orange
        return c;
    }

/*
	public class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		CheckBoxRenderer() {
          setHorizontalAlignment(JLabel.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        	//SettlementTableModel model = (SettlementTableModel) table.getModel();

	          if (isSelected) {
	              //super.setBackground(table.getSelectionBackground());
	        	  //table.setSelectionForeground(java.awt.Color.orange);
	        	  //table.setSelectionBackground(new java.awt.Color(255,226,197));
	        	  setForeground(new java.awt.Color(209,103,0));//table.getSelectionForeground());
	        	  setBackground(new java.awt.Color(255,226,197));//table.getSelectionBackground());

	          } else {
	            setForeground(table.getForeground());
	            setBackground(table.getBackground());
	          }
	          setSelected((value != null && ((Boolean) value).booleanValue()));

	          return this;
        }
	}
*/
}
