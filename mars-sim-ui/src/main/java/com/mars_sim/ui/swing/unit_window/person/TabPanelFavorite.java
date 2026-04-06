/*
 * Mars Simulation Project
 * TabPanelFavorite.java
 * @date 2025-07-04
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
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
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The TabPanelFavorite is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
class TabPanelFavorite extends EntityTableTabPanel<Person> {

	private static final String FAV_ICON = "favourite"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param person the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelFavorite(Person person, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelFavorite.title"),
			ImageLoader.getIconByName(FAV_ICON),	
			Msg.getString("TabPanelFavorite.title"), //$NON-NLS-1$
			person, context
		);
	}

	@Override
	protected JPanel createInfoPanel() {
		
		JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
		
		// Prepare SpringLayout for info panel.
		AttributePanel activityPanel = new AttributePanel(1);
		mainPanel.add(activityPanel, BorderLayout.NORTH);
		
		Favorite fav = getEntity().getFavorite();
		activityPanel.addTextField(Msg.getString("TabPanelFavorite.activity"), fav.getFavoriteActivity().getName(), null);

				
		String dishes = fav.getFavoriteDishes().stream().collect(Collectors.joining(", "));	
		var dishBlock = SwingHelper.createTextBlock(Msg.getString("TabPanelFavorite.dishes"), dishes);
		
		mainPanel.add(dishBlock, BorderLayout.CENTER);
		return mainPanel;
	}

	@Override
	protected TableModel createModel() {
  		return new PreferenceTableModel(getEntity());
	}
	
	@Override
	protected void setColumnDetails(TableColumnModel model) {
		model.getColumn(0).setPreferredWidth(150);
		model.getColumn(1).setPreferredWidth(30);
	}

	/**
	 * Internal class used as model for the skill table.
	 */
	private static class PreferenceTableModel extends AbstractTableModel {

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
				case 0 -> Msg.getString("task.singular");
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