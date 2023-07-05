/*
 * Mars Simulation Project
 * TabPanelFavorite.java
 * @date 2023-06-22
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.Favorite;
import org.mars_sim.msp.core.person.ai.fav.Preference;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The TabPanelFavorite is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
public class TabPanelFavorite
extends TabPanel {

	private static final String FAV_ICON = "favourite"; //$NON-NLS-1$
	
	/** The Preference Table. */	
	private JTable table;
	/** The Preference Table Model. */	
	private PreferenceTableModel tableModel;
	/** The Person instance. */
	private Person person = null;

	/**
	 * Constructor.
	 * @param person the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelFavorite(Person person, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(FAV_ICON),	
			Msg.getString("TabPanelFavorite.title"), //$NON-NLS-1$
			desktop
		);

		this.person = person;
	}

	@Override
	protected void buildUI(JPanel content) {
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		content.add(topPanel, BorderLayout.NORTH);
		
		// Prepare SpringLayout for info panel.
		AttributePanel infoPanel = new AttributePanel(4);
		topPanel.add(infoPanel, BorderLayout.NORTH);

		Favorite fav = person.getFavorite();
		infoPanel.addTextField(Msg.getString("TabPanelFavorite.mainDish"), fav.getFavoriteMainDish(), null);
		infoPanel.addTextField(Msg.getString("TabPanelFavorite.sideDish"), fav.getFavoriteSideDish(), null);
		infoPanel.addTextField(Msg.getString("TabPanelFavorite.dessert"), Conversion.capitalize(fav.getFavoriteDessert()), null);
		infoPanel.addTextField(Msg.getString("TabPanelFavorite.activity"), fav.getFavoriteActivity().getName(), null);

		// Create label panel.
		JPanel labelPanel = new JPanel(new BorderLayout(5, 5));
		content.add(labelPanel, BorderLayout.CENTER);
		
		// Create preference title label
		JLabel preferenceLabel = new JLabel(Msg.getString("TabPanelFavorite.preferenceTable.title"), JLabel.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(preferenceLabel);
		labelPanel.add(preferenceLabel, BorderLayout.NORTH);
		
		// Create scroll panel
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		labelPanel.add(scrollPane, BorderLayout.CENTER);
		
		// Create skill table
		tableModel = new PreferenceTableModel(person);
		table = new JTable(tableModel);

		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer);

		table.setPreferredScrollableViewportSize(new Dimension(225, 200));
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(30);
		table.setRowSelectionAllowed(true);
		table.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		// Added sorting
		table.setAutoCreateRowSorter(true);

		scrollPane.setViewportView(table);
	}

	/**
	 * Internal class used as model for the skill table.
	 */
	private static class PreferenceTableModel
	extends AbstractTableModel {

		private Preference manager;
		private List<String> scoreStringList;
		private Map<String, Integer> scoreStringMap;

		private PreferenceTableModel(Person person) {

			manager = person.getPreference();

	        scoreStringList = manager.getTaskStringList();
	        scoreStringMap = manager.getScoreStringMap();
		}

		public int getRowCount() {
			return scoreStringMap.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			if (columnIndex == 1) dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelFavorite.column.metaTask"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelFavorite.column.like"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			Object name = scoreStringList.get(row);
			if (column == 0)
				return name;
			else if (column == 1) {
				return scoreStringMap.get(name);

			}
			else
				return null;
		}
	}

	/**
	 * This renderer uses a delegation software design pattern to delegate
	 * this rendering of the table cell header to the real default render
	 **/
	class TableHeaderRenderer implements TableCellRenderer {
		private TableCellRenderer defaultRenderer;

		public TableHeaderRenderer(TableCellRenderer theRenderer) {
			defaultRenderer = theRenderer;
		}


		/**
		 * Renderer the specified Table Header cell
		 **/
		public Component getTableCellRendererComponent(JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row,
				int column) {

			Component theResult = defaultRenderer.getTableCellRendererComponent(
					table, value, isSelected, hasFocus,
					row, column);

			if (theResult instanceof JLabel) {
				JLabel cell = (JLabel) theResult;
				cell.setText((String)value);
			}

			return theResult;
		}
	}
}
