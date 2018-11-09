/**
 * Mars Simulation Project
 * TabPanelAttribute.java
 * @version 3.1.0 2017-03-06
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;


/**
 * The TabPanelAttribute is a tab panel for the natural attributes of a person.
 */
public class TabPanelAttribute
extends TabPanel {

	private AttributeTableModel attributeTableModel;
	private WebTable attributeTable;

	//private Person person;
	//private Robot robot;

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
		//this.person = person;

		// Create attribute table model
		attributeTableModel = new AttributeTableModel(person);

		init();
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

		//this.robot = robot;

		// Create attribute table model
		attributeTableModel = new AttributeTableModel(robot);

		init();
	}

	public void init() {

		// Create attribute label panel.
		WebPanel attributeLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(attributeLabelPanel);

		// Create attribute label
		WebLabel attributeLabel = new WebLabel(Msg.getString("TabPanelAttribute.label"), WebLabel.CENTER); //$NON-NLS-1$
		attributeLabel.setFont(new Font("Serif", Font.BOLD, 16));
		attributeLabelPanel.add(attributeLabel);

		// Create attribute scroll panel
		WebScrollPane attributeScrollPanel = new WebScrollPane();
		attributeScrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(attributeScrollPanel);

		// Create attribute table
		attributeTable = new ZebraJTable(attributeTableModel); //new JTable(attributeTableModel);//
		attributeTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		attributeTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		attributeTable.getColumnModel().getColumn(1).setPreferredWidth(70);
		attributeTable.setCellSelectionEnabled(false);
		// attributeTable.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		attributeScrollPanel.setViewportView(attributeTable);

		// 2015-06-08 Added sorting
		attributeTable.setAutoCreateRowSorter(true);
        //if (!MainScene.OS.equals("linux")) {
        //	attributeTable.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		//}
		// 2015-09-24 Align the content to the center of the cell
        // Note: DefaultTableCellRenderer does NOT work well with nimrod
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		attributeTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		attributeTable.getColumnModel().getColumn(1).setCellRenderer(renderer);

		// 2015-06-08 Added setTableStyle()
        TableStyle.setTableStyle(attributeTable);
        update();
        //attributeTableModel.update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		TableStyle.setTableStyle(attributeTable);
		attributeTableModel.update();
	}

}

/**
 * Internal class used as model for the attribute table.
 */
class AttributeTableModel
extends AbstractTableModel {

	private List<Map<String, NaturalAttributeType>> n_attributes;
	private List<Map<String, RoboticAttributeType>> r_attributes;

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private NaturalAttributeManager n_manager;
	private RoboticAttributeManager r_manager;

    Person person = null;
    Robot robot = null;

	/**
	 * hidden constructor.
	 * @param person {@link Person}
	 */
	AttributeTableModel(Unit unit) {

        if (unit instanceof Person) {
         	person = (Person) unit;
         	n_manager = person.getNaturalAttributeManager();

    		n_attributes = n_manager.getAttributes();
        }

        else if (unit instanceof Robot) {
        	robot = (Robot) unit;
        	r_manager = robot.getRoboticAttributeManager();

    		r_attributes = r_manager.getAttributes();
        }


	}

	@Override
	public int getRowCount() {
		if (person != null)
			return n_manager.getAttributeNum();

		else if (robot != null)
			return r_manager.getAttributeNum();
		else
			return 0;
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
			if (person != null)
				return n_attributes.get(row).keySet().iterator().next();

			else if (robot != null)
				return r_attributes.get(row).keySet().iterator().next();
			else
				return null;

		}

		else if (column == 1) {
			if (person != null)
				return getLevelString(n_manager.getAttribute(n_attributes.get(row).values().iterator().next()));

			else if (robot != null)
				return getLevelString(r_manager.getAttribute(r_attributes.get(row).values().iterator().next()));
			else
				return null;
		}

		else return null;
	}
	/*
	public void update() {}
	 */
	public String getLevelString(int level) {
		String result = null;
		if (level < 5) result = Msg.getString("TabPanelAttribute.level.0"); //$NON-NLS-1$
		else if (level < 20) result = Msg.getString("TabPanelAttribute.level.1"); //$NON-NLS-1$
		else if (level < 35) result = Msg.getString("TabPanelAttribute.level.2"); //$NON-NLS-1$
		else if (level < 45) result = Msg.getString("TabPanelAttribute.level.3"); //$NON-NLS-1$
		else if (level < 55) result = Msg.getString("TabPanelAttribute.level.4"); //$NON-NLS-1$
		else if (level < 65) result = Msg.getString("TabPanelAttribute.level.5"); //$NON-NLS-1$
		else if (level < 80) result = Msg.getString("TabPanelAttribute.level.6"); //$NON-NLS-1$
		else if (level < 95) result = Msg.getString("TabPanelAttribute.level.7"); //$NON-NLS-1$
		else result = Msg.getString("TabPanelAttribute.level.8"); //$NON-NLS-1$
		return result;
	}

	/**
	 * Prepares the job history of the person
	 * @param
	 * @param
	 */
	void update() {
    	fireTableDataChanged();
	}
}
