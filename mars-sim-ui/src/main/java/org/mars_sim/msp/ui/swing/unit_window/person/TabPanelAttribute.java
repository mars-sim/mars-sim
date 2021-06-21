/**
 * Mars Simulation Project
 * TabPanelAttribute.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * The TabPanelAttribute is a tab panel for the natural attributes of a person.
 */
@SuppressWarnings("serial")
public class TabPanelAttribute
extends TabPanel {

	/** Is UI constructed. */
	private boolean uiDone = false;

	/** The Person instance. */
	private Worker worker;
	
	private AttributeTableModel attributeTableModel;
	private JTable attributeTable;

	/**
	 * Constructor 1.
	 * @param person {@link Person} the person.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelAttribute(Person person, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelAttribute.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelAttribute.tooltip"), //$NON-NLS-1$
			person,
			desktop
		);
		this.worker = person;
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
			null,
			Msg.getString("TabPanelAttribute.tooltip"), //$NON-NLS-1$
			robot,
			desktop
		);
		this.worker = robot;
	}

	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;

		// Create attribute label panel.
		WebPanel attributeLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(attributeLabelPanel);

		// Create attribute label
		WebLabel attributeLabel = new WebLabel(Msg.getString("TabPanelAttribute.label"), WebLabel.CENTER); //$NON-NLS-1$
		attributeLabel.setFont(new Font("Serif", Font.BOLD, 14));
		attributeLabelPanel.add(attributeLabel);

		// Create attribute scroll panel
		WebScrollPane attributeScrollPanel = new WebScrollPane();
//		attributeScrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(attributeScrollPanel);

		// Create attribute table model
		attributeTableModel = new AttributeTableModel(worker);
		
		// Create attribute table
		attributeTable = new ZebraJTable(attributeTableModel); //new JTable(attributeTableModel);//
		attributeTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		attributeTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		attributeTable.getColumnModel().getColumn(1).setPreferredWidth(70);
		attributeTable.setRowSelectionAllowed(true);
//		attributeTable.setDefaultRenderer(Integer.class, new NumberCellRenderer());
		
		attributeScrollPanel.setViewportView(attributeTable);

		attributeTable.setAutoCreateRowSorter(true);
 
		// Align the content to the center of the cell
        // Note: DefaultTableCellRenderer does NOT work well with nimrod
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		attributeTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		attributeTable.getColumnModel().getColumn(1).setCellRenderer(renderer);

        TableStyle.setTableStyle(attributeTable);
        update();
        
        //attributeTableModel.update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		TableStyle.setTableStyle(attributeTable);
		attributeTableModel.update();
	}

	public void destroy() {
		attributeTableModel = null;
		attributeTable = null;
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
