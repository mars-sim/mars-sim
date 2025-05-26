/*
 * Mars Simulation Project
 * TabPanelCooking.java
 * @date 2023-04-18
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.cooking.Cooking;
import com.mars_sim.core.building.function.cooking.Cooking.DishStats;
import com.mars_sim.core.building.function.cooking.DishRecipe;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.NumberCellRenderer;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * This is a tab panel for displaying a settlement's Food Menu.
 */
@SuppressWarnings("serial")
public class TabPanelCooking extends TabPanel {

	private static final String COOKING_ICON = "cooking";

	private CookingTableModel cookingTableModel;

	/** The number of available meals. */
	private JLabel availableMealsLabel;
	/** The number of meals cooked today. */
	private JLabel mealsTodayLabel;

	private JLabel mealsReplenishmentLabel;
	
	/** The number of cooks label. */
	private JLabel numCooksLabel;

	/** The cook capacity label. */
	private JLabel cookCapacityLabel;

	private Settlement settlement;

	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelCooking(Settlement unit, MainDesktopPane desktop) {

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCooking.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(COOKING_ICON),
			Msg.getString("TabPanelCooking.title"), //$NON-NLS-1$
			desktop);

		settlement = unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		
		JPanel northPanel = new JPanel(new BorderLayout());
		content.add(northPanel, BorderLayout.NORTH);
			
		var meals = settlement.getMealTimes();

		AttributePanel topPanel = new AttributePanel(3, 2);
		northPanel.add(topPanel, BorderLayout.NORTH);

		for(var m : meals.getMeals()) {
			var msg = m.period().start() + " - " + m.period().end();
			topPanel.addTextField(m.name(), msg, null);
		}
		
		// Prepare cook number label
		numCooksLabel = topPanel.addTextField(Msg.getString("TabPanelCooking.numberOfCooks"),
											  "", null); //$NON-NLS-1$
		cookCapacityLabel = topPanel.addTextField(Msg.getString("TabPanelCooking.cookCapacity"),
													"", null); //$NON-NLS-1$

		// Prepare available meals label
		AttributePanel m = new AttributePanel(3);
		m.setBorder(StyleManager.createLabelBorder("Dishes"));

		availableMealsLabel = m.addTextField(Msg.getString("TabPanelCooking.available"), //$NON-NLS-1$
												"", null);
		mealsTodayLabel = m.addTextField(Msg.getString("TabPanelCooking.madeToday"), //$NON-NLS-1$
												"",null); 
		mealsReplenishmentLabel = m.addTextField(
				Msg.getString("TabPanelCooking.replenishment"), //$NON-NLS-1$
				"", null); 
		northPanel.add(m, BorderLayout.CENTER);

		// Create scroll panel for the outer table panel.
		JScrollPane scrollPane = new JScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		content.add(scrollPane, BorderLayout.CENTER);

		// Prepare cooking table model.
		cookingTableModel = new CookingTableModel(settlement);

		// Prepare cooking table.
		var table = new JTable(cookingTableModel);

		scrollPane.setViewportView(table);
		table.setRowSelectionAllowed(true);
		table.setDefaultRenderer(Double.class, new NumberCellRenderer());
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(140);
		columnModel.getColumn(1).setPreferredWidth(47);
		columnModel.getColumn(2).setPreferredWidth(45);
		columnModel.getColumn(3).setPreferredWidth(45);
		// Add the two methods below to make all heatTable columns
		// resizable automatically when its Panel resizes
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		columnModel.getColumn(1).setCellRenderer(renderer);
		columnModel.getColumn(2).setCellRenderer(renderer);
		columnModel.getColumn(3).setCellRenderer(renderer);

		table.setAutoCreateRowSorter(true);

		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		cookingTableModel.update();
		updaetDishes();
	}

	private void updaetDishes() {
		int numCooks = 0;
		int cookCapacity = 0;
		int availableMeals = 0;
		int mealsToday = 0;
		for(Building b : settlement.getBuildingManager().getBuildingSet(FunctionType.COOKING)) {
			// for each building's kitchen in the settlement
			Cooking kitchen = b.getCooking();
			availableMeals += kitchen.getNumberOfAvailableCookedMeals();
			mealsToday += kitchen.getTotalNumberOfCookedMealsToday();
			cookCapacity += kitchen.getCookCapacity();
			numCooks += kitchen.getNumCooks();
		}

		double mealsReplenishment = Math.round(settlement.getMealsReplenishmentRate() * 100.0) / 100.0;


		mealsReplenishmentLabel.setText(
							StyleManager.DECIMAL_PLACES1.format(mealsReplenishment));
		availableMealsLabel.setText(Integer.toString(availableMeals)); //$NON-NLS-1$
		mealsTodayLabel.setText(Integer.toString(mealsToday)); //$NON-NLS-1$
		numCooksLabel.setText(Integer.toString(numCooks)); //$NON-NLS-1$
		cookCapacityLabel.setText(Integer.toString(cookCapacity)); //$NON-NLS-1$
	}

	/**
	 * Internal class used as model for the cooking table.
	 */
	private class CookingTableModel extends AbstractTableModel {

		private Settlement settlement;

		private List<String> nameList;
		private Map<String, DishStats> qualityMap = new HashMap<>();

		private String[] columnNames = { "Dish", "# Servings",
				"Best", "Worst" };
		
		private CookingTableModel(Settlement settlement) {
			this.settlement = settlement;

			nameList = new ArrayList<>();
		}

		public int getRowCount() {
			return nameList.size();

		}

		public int getColumnCount() {
			return 4;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType;
			if (columnIndex == 0)
				dataType = String.class;
			else 
				dataType = Double.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (nameList.isEmpty())
				return null;
			
			Object result = null;
			
			String name = nameList.get(row);
			var stats = qualityMap.get(name);

			switch(column) {
			case 0:
				result = name;
				break;

			case 1:
				if (stats != null)
					result = stats.getNumber();
				break;

			case 2:
				if (stats != null)
					result = DishRecipe.qualityToString(stats.getBestQuality());
				break;

			case 3:
				if (stats != null)
					result = DishRecipe.qualityToString(stats.getWorseQuality());
				break;
			default:
				break;
			}
			return result;
		}

		public void update() {
			buildQualityMap();
			fireTableDataChanged();
		}

		private void buildQualityMap() {

			qualityMap = settlement.getBuildingManager().getBuildings(FunctionType.COOKING).stream()
				.map(c -> c.getCooking().getQualityMap())
				.flatMap(m -> m.entrySet().stream())
         		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, DishStats::sum));


			nameList = new ArrayList<>(qualityMap.keySet());
		}
	}
}
