/**
 * Mars Simulation Project
 * TabPanelFavorite.java
 * @version 3.08 2015-06-07
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Preference;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.MultisortTableHeaderCellRenderer;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import net.java.balloontip.BalloonToolTip;

/**
 * The TabPanelFavorite is a tab panel for general information about a person.
 */
public class TabPanelFavorite
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private JTable table;
	private PreferenceTableModel tableModel;
	private BalloonToolTip balloonToolTip = new BalloonToolTip();
	
	
	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelFavorite(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelFavorite.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelFavorite.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Person person = (Person) unit;

		// Create Favorite label panel.
		JPanel favoriteLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(favoriteLabelPanel);

		// Prepare  Favorite label
		JLabel favoriteLabel = new JLabel(Msg.getString("TabPanelFavorite.label"), JLabel.CENTER); //$NON-NLS-1$
		favoriteLabelPanel.add(favoriteLabel);

		// Prepare info panel.
		JPanel infoPanel = new JPanel(new GridLayout(4, 2, 0, 0));
		infoPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Prepare main dish name label
		JLabel mainDishNameLabel = new JLabel(Msg.getString("TabPanelFavorite.mainDish"), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(mainDishNameLabel);

		// Prepare main dish label
		String mainDish = person.getFavorite().getFavoriteMainDish();
		//JLabel mainDishLabel = new JLabel(mainDish, JLabel.RIGHT);
		//infoPanel.add(mainDishLabel);
		JPanel wrapper1 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		JTextField mainDishTF = new JTextField(Conversion.capitalize(mainDish));
		mainDishTF.setEditable(false);
		mainDishTF.setColumns(17);
		wrapper1.add(mainDishTF);
		infoPanel.add(wrapper1);

		// Prepare side dish name label
		JLabel sideDishNameLabel = new JLabel(Msg.getString("TabPanelFavorite.sideDish"), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(sideDishNameLabel);

		// Prepare side dish label
		String sideDish = person.getFavorite().getFavoriteSideDish();
		//JLabel sideDishLabel = new JLabel(sideDish, JLabel.RIGHT);
		//infoPanel.add(sideDishLabel);
		JPanel wrapper2 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		JTextField sideDishTF = new JTextField(Conversion.capitalize(sideDish));
		sideDishTF.setEditable(false);
		sideDishTF.setColumns(17);
		wrapper2.add(sideDishTF);
		infoPanel.add(wrapper2);

		// Prepare dessert name label
		JLabel dessertNameLabel = new JLabel(Msg.getString("TabPanelFavorite.dessert"), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(dessertNameLabel);

		// Prepare dessert label
		String dessert = person.getFavorite().getFavoriteDessert();
		//JLabel dessertLabel = new JLabel(Conversion.capitalize(dessert), JLabel.RIGHT);
		//infoPanel.add(dessertLabel);
		JPanel wrapper3 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		JTextField dessertTF = new JTextField(Conversion.capitalize(dessert));
		dessertTF.setEditable(false);
		dessertTF.setColumns(17);
		wrapper3.add(dessertTF);
		infoPanel.add(wrapper3);

		// Prepare activity name label
		JLabel activityNameLabel = new JLabel(Msg.getString("TabPanelFavorite.activity"), JLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(activityNameLabel);

		// Prepare activity label
		String activity = person.getFavorite().getFavoriteActivity();
		JPanel wrapper4 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		JTextField activityTF = new JTextField(Conversion.capitalize(activity));
		activityTF.setEditable(false);
		activityTF.setColumns(15);
		wrapper4.add(activityTF);
		infoPanel.add(wrapper4);

		//JLabel activityLabel = new JLabel(Conversion.capitalize(activity), JLabel.RIGHT);
		//infoPanel.add(activityLabel);

		// Create label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		centerContentPanel.add(labelPanel, BorderLayout.NORTH);

		// Create preference label
		JLabel label = new JLabel("Preferences");
		labelPanel.add(label);

		// Create scroll panel
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(new MarsPanelBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		centerContentPanel.add(scrollPane,  BorderLayout.CENTER);

		// Create skill table
		tableModel = new PreferenceTableModel(person);
		table = new ZebraJTable(tableModel);

		// 2015-09-24 Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		//table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer);

		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(30);
		table.setCellSelectionEnabled(false);
		table.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		// 2015-06-08 Added sorting
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());

		// 2015-06-08 Added setTableStyle()
		//TableStyle.setTableStyle(table);

		scrollPane.setViewportView(table);

	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		//tableModel.update();
		TableStyle.setTableStyle(table);
	}


	/**
	 * Internal class used as model for the skill table.
	 */
	private static class PreferenceTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Preference manager;
		private List<String> metaTaskStringList;
		private Map<String, Integer> metaTaskStringMap;

		private PreferenceTableModel(Unit unit) {
			Person person = null;
	        Robot robot = null;

	        if (unit instanceof Person) {
	         	person = (Person) unit;
				manager = person.getPreference();
	        }
	        else if (unit instanceof Robot) {

	        }

	        metaTaskStringList = manager.getMetaTaskStringList();
	        metaTaskStringMap = manager.getMetaTaskStringMap();

		}

		public int getRowCount() {
			return metaTaskStringMap.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			if (columnIndex == 1) dataType = Integer.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelFavorite.column.metaTask"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelFavorite.column.like"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			Object name = metaTaskStringList.get(row);
			if (column == 0) return name;
			else if (column == 1) return metaTaskStringMap.get(name);
			else return null;
		}

		public void update() {

/*
			List<String> n = manager.getMetaTaskNameList();
	        Map<String, Integer> m = manager.getMetaTaskMap();

			if (!metaTaskMap.equals(m)) {
				metaTaskNameList = n;
				metaTaskMap = m;
				fireTableDataChanged();
			}
*/

		}
	}
}