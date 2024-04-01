/*
 * Mars Simulation Project
 * TabPanelAttribute.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;

/**
 * The TabPanelAttribute is a tab panel for the natural attributes of a person.
 */
@SuppressWarnings("serial")
public class TabPanelAttribute extends TabPanelTable {
	
	private static final String ATTRIBUTE_ICON = "attribute"; //$NON-NLS-1$
	
	private AttributeTableModel attributeTableModel;
	
	/**
	 * Constructor 1.
	 * @param person {@link Person} the person.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelAttribute(Person person, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelAttribute.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(ATTRIBUTE_ICON),	
			Msg.getString("TabPanelAttribute.title"), //$NON-NLS-1$
			person,	desktop
		);
	}

	/**
	 * Constructor 2.
	 * @param robot{@link Robot} the robot.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelAttribute(Robot robot, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelAttribute.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(ATTRIBUTE_ICON),	
			Msg.getString("TabPanelAttribute.tooltip"), //$NON-NLS-1$
			robot, desktop
		);
	}

	@Override
	protected TableModel createModel() {
		// Create attribute table model
		attributeTableModel = new AttributeTableModel((Worker) getUnit());
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
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		attributeTableModel.update();
	}

	@Override
	public void destroy() {
		attributeTableModel = null;
	}
}

/**
 * Internal class used as model for the attribute table.
 */
@SuppressWarnings("serial")
class AttributeTableModel extends AbstractTableModel {

	private List<NaturalAttributeType> n_attributes;

	private NaturalAttributeManager n_manager;

	private Worker worker;

	/**
	 * hidden constructor.
	 * @param person {@link Person}
	 */
	AttributeTableModel(Worker unit) {

		worker = unit;
    	n_manager = worker.getNaturalAttributeManager();
		n_attributes = new ArrayList<>(n_manager.getAttributeMap().keySet());

	}

	@Override
	public int getRowCount() {
		return n_manager.getAttributeNum();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> dataType = super.getColumnClass(columnIndex);
		if (columnIndex == 0) dataType = String.class;
		if (columnIndex == 1) dataType = String.class;
		return dataType;
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
			return n_attributes.get(row).getName();
		}

		else if (column == 1) {
			int level = n_manager.getAttribute(n_attributes.get(row));
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

	/**
	 * Prepares the job history of the person
	 * @param
	 * @param
	 */
	void update() {
//    	fireTableDataChanged();
	}
	
	public void destroy() {
		n_attributes.clear();
		
		n_attributes = null;
		
		n_manager = null;
		worker = null;
	}
}
