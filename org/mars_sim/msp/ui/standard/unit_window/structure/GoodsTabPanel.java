/**
 * Mars Simulation Project
 * GoodsTabPanel.java
 * @version 2.81 2007-04-16
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.structure.goods.GoodsManager;
import org.mars_sim.msp.simulation.structure.goods.GoodsUtil;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.NumberCellRenderer;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

public class GoodsTabPanel extends TabPanel {

	// Data members
	private GoodsTableModel goodsTableModel;
	
    /**
     * Constructor
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
	public GoodsTabPanel(Unit unit, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super("Trade", null, "Trade Goods", unit, desktop);
		
        // Prepare goods label panel.
        JPanel goodsLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(goodsLabelPanel);
        
        // Prepare goods label.
        JLabel goodsLabel = new JLabel("Trade Goods", JLabel.CENTER);
        goodsLabelPanel.add(goodsLabel);
        
		// Create scroll panel for the outer table panel.
		JScrollPane goodsScrollPanel = new JScrollPane();
		goodsScrollPanel.setPreferredSize(new Dimension(220, 260));
		topContentPanel.add(goodsScrollPanel);         
        
        // Prepare outer table panel.
        JPanel outerTablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        outerTablePanel.setBorder(new MarsPanelBorder());
        goodsScrollPanel.setViewportView(outerTablePanel);   
        
        // Prepare goods table panel.
        JPanel goodsTablePanel = new JPanel(new BorderLayout(0, 0));
        outerTablePanel.add(goodsTablePanel);
        
        // Prepare goods table model.
        goodsTableModel = new GoodsTableModel(((Settlement) unit).getGoodsManager());
        
        // Prepare goods table.
        JTable goodsTable = new JTable(goodsTableModel);
        goodsTable.setCellSelectionEnabled(false);
        goodsTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2));
        goodsTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        goodsTablePanel.add(goodsTable.getTableHeader(), BorderLayout.NORTH);
        goodsTablePanel.add(goodsTable, BorderLayout.CENTER);
	}
	
	@Override
	public void update() {
		goodsTableModel.update();
	} 
	
    /** 
     * Internal class used as model for the power table.
     */
    private class GoodsTableModel extends AbstractTableModel {
    	
    	// Data members
    	GoodsManager manager;
    	List goods;
    	
    	private GoodsTableModel(GoodsManager manager) {
    		this.manager = manager;
    		goods = GoodsUtil.getGoodsList();
    	}
    	
        public int getRowCount() {
            return goods.size();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Class getColumnClass(int columnIndex) {
            Class dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = String.class;
            else if (columnIndex == 1) dataType = Double.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Good";
            else if (columnIndex == 1) return "Value (VP/kg)";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
        	if (row < getRowCount()) {
        		Good good = (Good) goods.get(row);
        		if (column == 0) return good.getName();
        		else if (column == 1) return manager.getGoodValuePerMass(good);
        		else return "unknown";
            }
        	else return "";
        }
  
        public void update() {
            fireTableDataChanged();
        }
    }
}