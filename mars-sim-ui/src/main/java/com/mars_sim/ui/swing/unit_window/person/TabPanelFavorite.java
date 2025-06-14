/*
 * Mars Simulation Project
 * TabPanelFavorite.java
 * @date 2023-06-22
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.Favorite;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The TabPanelFavorite is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
public class TabPanelFavorite extends TabPanelTable {

	private static final String FAV_ICON = "favourite"; //$NON-NLS-1$
		
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
	protected JPanel createInfoPanel() {
		// Prepare SpringLayout for info panel.
		AttributePanel infoPanel = new AttributePanel(2);

		Favorite fav = person.getFavorite();
		infoPanel.addTextField(Msg.getString("TabPanelFavorite.activity"), fav.getFavoriteActivity().getName(), null);

		String dishes = fav.getFavoriteDishes().stream().collect(Collectors.joining("<br>", "<html>", "</html>"));
		infoPanel.addTextField(Msg.getString("TabPanelFavorite.dishes"), dishes, null);
		
		return infoPanel;
	}

	@Override
	protected TableModel createModel() {
  PreferenceTableModel tableModel;
		tableModel = new PreferenceTableModel(person);
		return tableModel;
	}
	
	@Override
	protected void setColumnDetails(TableColumnModel model) {
		model.getColumn(0).setPreferredWidth(150);
		model.getColumn(1).setPreferredWidth(30);
		model.getColumn(1).setCellRenderer(new NumberCellRenderer());
	}

	/**
	 * Internal class used as model for the skill table.
	 */
	private static class PreferenceTableModel
	extends AbstractTableModel {

		private List<String> scoreStringList;
		private Map<String, Integer> scoreStringMap;

		private PreferenceTableModel(Person person) {

	        scoreStringMap = person.getPreference().getScoreStringMap();
			scoreStringList = new ArrayList<>(scoreStringMap.keySet());
			Collections.sort(scoreStringList);
		}

		@Override
		public int getRowCount() {
			return scoreStringMap.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> String.class;
				case 1 -> Double.class;
				default -> null;
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> Msg.getString("TabPanelFavorite.column.metaTask");
				case 1 -> Msg.getString("TabPanelFavorite.column.like");
				default -> null;
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			String name = scoreStringList.get(row);
			return switch (column) {
				case 0 -> name;
				case 1 -> scoreStringMap.get(name);
				default -> null;
			};
		}
	}
}
