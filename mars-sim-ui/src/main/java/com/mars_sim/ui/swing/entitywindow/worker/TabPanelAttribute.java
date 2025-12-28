/*
 * Mars Simulation Project
 * TabPanelAttribute.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.worker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;

/**
 * The TabPanelAttribute is a tab panel for the natural attributes of a person.
 */
@SuppressWarnings("serial")
public class TabPanelAttribute extends EntityTableTabPanel<Worker> {
	
	private static final String ATTRIBUTE_ICON = "attribute"; //$NON-NLS-1$
	
	private AttributeTableModel attributeTableModel;
	
	/**
	 * Constructor 1.
	 * @param worker {@link Worker} the worker.
	 * @param context {@link UIContext} the UI context.
	 */
	public TabPanelAttribute(Worker worker, UIContext context) {
		super(
			Msg.getString("TabPanelAttribute.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(ATTRIBUTE_ICON),	
			null, worker, context
			);
	}

	@Override
	protected TableModel createModel() {
		// Create attribute table model
		attributeTableModel = new AttributeTableModel(getEntity());
		return attributeTableModel;
	}
	
	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
		columnModel.getColumn(0).setPreferredWidth(100);
		columnModel.getColumn(1).setPreferredWidth(70);
 
		// Align the content to the center of the cell
        // Note: DefaultTableCellRenderer does NOT work well with nimrod
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		columnModel.getColumn(0).setCellRenderer(renderer);
		columnModel.getColumn(1).setCellRenderer(renderer);
	}

	/**
	 * Refresh the UI elements will update the attribute table.
	 */
	@Override
	public void refreshUI() {
		// Notify the table model data has changed
		attributeTableModel.refresh();
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private static class AttributeTableModel extends AbstractTableModel {

		private List<NaturalAttributeType> attributes;
		private NaturalAttributeManager attrMgr;

		/**
		 * hidden constructor.
		 * @param worker {@link Worker}
		 */
		AttributeTableModel(Worker worker) {
			attrMgr = worker.getNaturalAttributeManager();
			attributes = new ArrayList<>(attrMgr.getAttributeMap().keySet());
		}

		private void refresh() {
			attributes = new ArrayList<>(attrMgr.getAttributeMap().keySet());
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return attrMgr.getAttributeNum();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelAttribute.column.attribute"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelAttribute.column.level"); //$NON-NLS-1$
			else return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) {
				return attributes.get(row).getName();
			}

			else if (column == 1) {
				int level = attrMgr.getAttribute(attributes.get(row));
				return " " + level + " - " + getLevelString(level);
			}

			else return null;
		}

		/**
		 * Converts the numeric attribute points to a description of level
		 * 
		 * @param level
		 * @return
		 */
		public String getLevelString(int level) {
			String result = null;
			if (level < 10) result = Msg.getString("TabPanelAttribute.level.0"); //$NON-NLS-1$
			else if (level < 20) result = Msg.getString("TabPanelAttribute.level.1"); //$NON-NLS-1$
			else if (level < 30) result = Msg.getString("TabPanelAttribute.level.2"); //$NON-NLS-1$
			else if (level < 40) result = Msg.getString("TabPanelAttribute.level.3"); //$NON-NLS-1$
			else if (level < 50) result = Msg.getString("TabPanelAttribute.level.4"); //$NON-NLS-1$
			else if (level < 60) result = Msg.getString("TabPanelAttribute.level.5"); //$NON-NLS-1$
			else if (level < 70) result = Msg.getString("TabPanelAttribute.level.6"); //$NON-NLS-1$
			else if (level < 80) result = Msg.getString("TabPanelAttribute.level.7"); //$NON-NLS-1$
			else if (level < 90) result = Msg.getString("TabPanelAttribute.level.7"); //$NON-NLS-1$		
			else result = Msg.getString("TabPanelAttribute.level.8"); //$NON-NLS-1$
			return result;
		}
	}
}
