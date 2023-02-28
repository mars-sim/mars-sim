/*
 * Mars Simulation Project
 * TabPanelCooking.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

/**
 * This is a tab panel for displaying a settlement's Food Menu.
 */
@SuppressWarnings("serial")
public class TabPanelCooking extends TabPanel {

	/** default logger. */
	// private static final Logger logger = Logger.getLogger(TabPanelCooking.class.getName());

	private static final String COOKING_ICON = "cooking";
	
	private static final FunctionType COOKING = FunctionType.COOKING;
	private static final FunctionType PREPARING_DESSERT = FunctionType.PREPARING_DESSERT;

	
	private int numRow = 0;
	private int dayCache = 1;

	private Set<String> nameSet;
	private List<String> nameList;

	private JTable table;
	private CookingTableModel cookingTableModel;

	/** The number of available meals. */
	private JLabel availableMealsLabel;
	private int availableMealsCache = 0;
	/** The number of meals cooked today. */
	private JLabel mealsTodayLabel;
	private int mealsTodayCache = 0;

	/** The number of available Desserts. */
	private JLabel availableDessertsLabel;
	private int availableDessertsCache = 0;
	/** The number of Desserts cooked today. */
	private JLabel dessertsTodayLabel;
	private int dessertsTodayCache = 0;

	private JLabel mealsReplenishmentLabel;
	private double mealsReplenishmentCache = 0;
	private JLabel dessertsReplenishmentLabel;
	private double dessertsReplenishmentCache = 0;
	
	/** The number of cooks label. */
	private JTextField numCooksLabel;
	private int numCooksCache = 0;

	/** The cook capacity label. */
	private JTextField cookCapacityLabel;
	private int cookCapacityCache = 0;

	private Settlement settlement;

	private static MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();

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
		
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(COOKING).iterator();
		while (i.hasNext()) {
			// for each building's kitchen in the settlement
			Building building = i.next();
			// System.out.println("Building is " + building.getNickName());
			if (building.hasFunction(COOKING)) {
				Cooking kitchen = building.getCooking();

				availableMealsCache += kitchen.getNumberOfAvailableCookedMeals();
				mealsTodayCache += kitchen.getTotalNumberOfCookedMealsToday();
				cookCapacityCache += kitchen.getCookCapacity();
				numCooksCache += kitchen.getNumCooks();
			}
		}

		Iterator<Building> j = settlement.getBuildingManager().getBuildings(PREPARING_DESSERT).iterator();
		while (j.hasNext()) {
			// for each building's kitchen in the settlement
			Building building = j.next();
			if (building.hasFunction(PREPARING_DESSERT)) {
				PreparingDessert kitchen = building.getPreparingDessert();

				availableDessertsCache += kitchen.getAvailableServingsDesserts();
				dessertsTodayCache += kitchen.getTotalServingsOfDessertsToday();
			}
		}
		
		JPanel northPanel = new JPanel(new BorderLayout());
		content.add(northPanel, BorderLayout.NORTH);
			
		JPanel topPanel = new JPanel(new SpringLayout()); //new GridLayout(4, 1, 0, 0));
		northPanel.add(topPanel, BorderLayout.NORTH);

		addTextField(topPanel, Msg.getString("TabPanelCooking.breakfastTime"), //$NON-NLS-1$
				CookMeal.getMealTimeString(settlement.getCoordinates(), 0), 6, null);
		
		addTextField(topPanel, Msg.getString("TabPanelCooking.lunchTime"), //$NON-NLS-1$
				CookMeal.getMealTimeString(settlement.getCoordinates(), 1), 6, null);
		
		addTextField(topPanel, Msg.getString("TabPanelCooking.dinnerTime"), //$NON-NLS-1$
				CookMeal.getMealTimeString(settlement.getCoordinates(), 2), 6, null);
		
		addTextField(topPanel, Msg.getString("TabPanelCooking.midnightTime"), //$NON-NLS-1$
				CookMeal.getMealTimeString(settlement.getCoordinates(), 3), 6, null);
		
		// Prepare cook number label
		numCooksLabel = addTextField(topPanel, Msg.getString("TabPanelCooking.numberOfCooks"), numCooksCache, 4, null); //$NON-NLS-1$
		cookCapacityLabel = addTextField(topPanel, Msg.getString("TabPanelCooking.cookCapacity"), cookCapacityCache, 4, null); //$NON-NLS-1$

		// Set up the spring layout.
		SpringUtilities.makeCompactGrid(topPanel, 3, 4, // rows, cols
				20, INITY_DEFAULT, // initX, initY
				5, 2); // xPad, yPad
		
		// Prepare cooking label panel.
		JPanel splitPanel = new JPanel(new GridLayout(1, 2, 0, 0));
		northPanel.add(splitPanel, BorderLayout.CENTER);

		// Add TitledBorder
		JPanel d = new JPanel(new GridLayout(3, 1, 0, 0));
		d.setBorder(StyleManager.createSubHeadingBorder("Desserts"));

		// Prepare # of available Desserts label
		availableDessertsLabel = new JLabel(Msg.getString("TabPanelCooking.availableDesserts", availableDessertsCache), //$NON-NLS-1$
				JLabel.LEFT);
		d.add(availableDessertsLabel);
		// Prepare # of Desserts label
		dessertsTodayLabel = new JLabel(Msg.getString("TabPanelCooking.dessertsToday", dessertsTodayCache), //$NON-NLS-1$
				JLabel.LEFT);
		d.add(dessertsTodayLabel);
		dessertsReplenishmentLabel = new JLabel(
				Msg.getString("TabPanelCooking.dessertsReplenishment", dessertsReplenishmentCache), JLabel.LEFT); //$NON-NLS-1$
		d.add(dessertsReplenishmentLabel);
		splitPanel.add(d);

		JPanel m = new JPanel(new GridLayout(3, 1, 0, 0));
		m.setBorder(StyleManager.createSubHeadingBorder("Meals"));

		// Prepare # of available meals label
		availableMealsLabel = new JLabel(Msg.getString("TabPanelCooking.availableMeals", availableMealsCache), //$NON-NLS-1$
				JLabel.LEFT);
		m.add(availableMealsLabel);
		// Prepare # of cooked meals label
		mealsTodayLabel = new JLabel(Msg.getString("TabPanelCooking.mealsToday", mealsTodayCache), JLabel.LEFT); //$NON-NLS-1$
		m.add(mealsTodayLabel);
		mealsReplenishmentLabel = new JLabel(
				Msg.getString("TabPanelCooking.mealsReplenishment", mealsReplenishmentCache), JLabel.LEFT); //$NON-NLS-1$
		m.add(mealsReplenishmentLabel);
		splitPanel.add(m);

		// Create scroll panel for the outer table panel.
		JScrollPane scrollPane = new JScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		content.add(scrollPane, BorderLayout.CENTER);

		// Prepare cooking table model.
		cookingTableModel = new CookingTableModel(settlement);

		// Prepare cooking table.
		table = new JTable(cookingTableModel) {

			public String getToolTipText(java.awt.event.MouseEvent e) {
				String personName = null;
				MarsClock time = null;

				StringBuilder result = new StringBuilder("<html>");

				try {

					result.append("&emsp;&nbsp;Eaten by :&emsp;");
					result.append(personName);
					result.append("&emsp;&nbsp;Time :&emsp;");
					result.append(time);

				} catch (RuntimeException e1) {
					// catch null pointer exception if mouse is over an empty line
				}
				result.append("</html>");
				return result.toString();
			}

		};

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

		repaint();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		cookingTableModel.update();
		updateMeals();
		updateDesserts();
	}

	private void updateMeals() {
		int numCooks = 0;
		int cookCapacity = 0;
		int availableMeals = 0;
		int mealsToday = 0;
		double mealsReplenishment = 0D;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(COOKING).iterator();
		while (i.hasNext()) {
			// for each building's kitchen in the settlement
			Building building = i.next();
			if (building.hasFunction(COOKING)) {
				Cooking kitchen = building.getCooking();

				availableMeals += kitchen.getNumberOfAvailableCookedMeals();
				mealsToday += kitchen.getTotalNumberOfCookedMealsToday();
				cookCapacity += kitchen.getCookCapacity();
				numCooks += kitchen.getNumCooks();
				mealsReplenishment = settlement.getMealsReplenishmentRate();
				;
			}
		}

		mealsReplenishment = Math.round(mealsReplenishment * 100.0) / 100.0;

		// Update # of meals replenishment rate
		if (mealsReplenishmentCache != mealsReplenishment) {
			mealsReplenishmentCache = mealsReplenishment;
			mealsReplenishmentLabel
					.setText(Msg.getString("TabPanelCooking.mealsReplenishment", mealsReplenishmentCache)); //$NON-NLS-1$
		}

		// Update # of available meals
		if (availableMealsCache != availableMeals) {
			availableMealsCache = availableMeals;
			availableMealsLabel.setText(Msg.getString("TabPanelCooking.availableMeals", availableMealsCache)); //$NON-NLS-1$
		}

		// Update # of meals cooked today
		if (mealsTodayCache != mealsToday) {
			mealsTodayCache = mealsToday;
			mealsTodayLabel.setText(Msg.getString("TabPanelCooking.mealsToday", mealsTodayCache)); //$NON-NLS-1$
		}

		// Update cook number
		if (numCooksCache != numCooks) {
			numCooksCache = numCooks;
			numCooksLabel.setText(numCooksCache + ""); //$NON-NLS-1$
		}

		// Update cook capacity
		if (cookCapacityCache != cookCapacity) {
			cookCapacityCache = cookCapacity;
			cookCapacityLabel.setText(cookCapacityCache + ""); //$NON-NLS-1$
		}
	}

	private void updateDesserts() {

		int availableDesserts = 0;
		int dessertsToday = 0;
		double dessertsReplenishment = 0;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(PREPARING_DESSERT).iterator();
		while (i.hasNext()) {
			// for each building's kitchen in the settlement
			Building building = i.next();
//			if (building.hasFunction(PREPARING_DESSERT)) {
				PreparingDessert kitchen = building.getPreparingDessert();

				availableDesserts += kitchen.getAvailableServingsDesserts();
				dessertsToday += kitchen.getTotalServingsOfDessertsToday();
				dessertsReplenishment = settlement.getDessertsReplenishmentRate();
//			}
		}

		dessertsReplenishment = Math.round(dessertsReplenishment * 100.0) / 100.0;

		// Update # of desserts replenishment rate
		if (dessertsReplenishmentCache != dessertsReplenishment) {
			dessertsReplenishmentCache = dessertsReplenishment;
			dessertsReplenishmentLabel
					.setText(Msg.getString("TabPanelCooking.dessertsReplenishment", dessertsReplenishment)); //$NON-NLS-1$
		}
		// Update # of available Desserts
		if (availableDessertsCache != availableDesserts) {
			availableDessertsCache = availableDesserts;
			availableDessertsLabel.setText(Msg.getString("TabPanelCooking.availableDesserts", availableDesserts)); //$NON-NLS-1$
		}

		// Update # of Desserts cooked today
		if (dessertsTodayCache != dessertsToday) {
			dessertsTodayCache = dessertsToday;
			dessertsTodayLabel.setText(Msg.getString("TabPanelCooking.dessertsToday", dessertsToday)); //$NON-NLS-1$
		}

	}

	/**
	 * Internal class used as model for the cooking table.
	 */
	private class CookingTableModel extends AbstractTableModel {

		private Settlement settlement;

		private Multiset<String> allServingsSet;

		private Multimap<String, Double> qualityMap;
		private Multimap<String, Double> allQualityMap;

		private Multimap<String, MarsClock> timeMap;
		private Multimap<String, MarsClock> allTimeMap;

		private Collection<Map.Entry<String, Double>> allQualityMapE;
		private Collection<Entry<String, MarsClock>> allTimeMapE;


		private String[] columnNames = { "Meal", "# Servings",
				"Best", "Worst" };
		
		private CookingTableModel(Settlement settlement) {
			this.settlement = settlement;

			allServingsSet = HashMultiset.create();
			allQualityMap = ArrayListMultimap.create();
			allTimeMap = ArrayListMultimap.create();
		}

		public int getRowCount() {
			return numRow;

		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0)
				dataType = String.class;
			else if (columnIndex == 1)
				dataType = Double.class;
			else if (columnIndex == 2)
				dataType = Double.class;
			else if (columnIndex == 3)
				dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return columnNames[0];
			if (columnIndex == 1)
				return columnNames[1];
			if (columnIndex == 2)
				return columnNames[2];
			if (columnIndex == 3)
				return columnNames[3];

			return null;
		}

		/***
		 * Converts a numeral quality to letter grade for a meal
		 * 
		 * @param quality
		 * @return grade
		 */
		public String computeGrade(double quality) {
			String grade = "";

			if (quality < -4)
				grade = "C-";
			else if (quality < -3)
				grade = "C";
			else if (quality < -2)
				grade = "C+";
			else if (quality < -1)
				grade = "B-";
			else if (quality < 0)
				grade = "B";
			else if (quality < 1)
				grade = "B+";
			else if (quality < 2)
				grade = "A-";
			else if (quality < 3)
				grade = "A";
			else
				grade = "A+";

			return grade;
		}

		public Object getValueAt(int row, int column) {
			if (nameList.isEmpty())
				return null;
			
			Object result = null;
			
			String name = nameList.get(row);

			if (column == 0)
				result = name;

			else if (column == 1) {
				// Use Multimap.get(key) returns a view of the values 
				// associated with the specified key
				int numServings = allServingsSet.count(name);
				result = numServings;
				// allServingsSet.clear();
			} else if (column == 2) {
				double best = 0;
				double value = 0;
				for (Map.Entry<String, Double> entry : allQualityMapE) {
					String key = entry.getKey();
					if (name.equals(key)) {
						value = entry.getValue();
						if (value > best)
							best = value;
					}
					result = computeGrade(best);
					// allQualityMap.clear();
				}
			} else if (column == 3) {
				double worst = 10;
				double value = 0;
				for (Map.Entry<String, Double> entry : allQualityMapE) {
					String key = entry.getKey();
					if (name.equals(key)) {
						value = entry.getValue();

						if (value < worst)
							worst = value;
					}
					result = computeGrade(worst);
					// allTimeMap.clear();
				}
			} else
				result = null;
			return result;
		}

		// TODO: decide in what situation it needs update and at what time ?
		// update every second or after each meal or once a day ?
		public void update() {
			// System.out.println("CookingTableModel : entering update()");
			cleanUpTable();
			getMultimap();
			fireTableDataChanged();
		}

		public void getMultimap() {

			Iterator<Building> i = settlement.getBuildingManager().getBuildings(COOKING).iterator();

			while (i.hasNext()) { 
				// for each building's kitchen in the settlement
				Building building = i.next();
				Cooking kitchen = building.getCooking();

				qualityMap = kitchen.getQualityMap();
				timeMap = kitchen.getTimeMap();

				allQualityMap.putAll(qualityMap);
				allTimeMap.putAll(timeMap);
			}

			allQualityMapE = allQualityMap.entries();
			allTimeMapE = allTimeMap.entries();
			allServingsSet = allQualityMap.keys();

			numRow = allTimeMap.keySet().size();
			nameSet = allTimeMap.keySet();
			// nameSet = servingsSet.elementSet(); // or using servingsSet
			nameList = new ArrayList<>(nameSet);
		}

		/**
		 * Removes all entries on all maps at the beginning of each new sol.
		 */
		public void cleanUpTable() {
			// 1. find any expired meals
			// 2. remove any expired meals from all 3 maps
			// 3. call cookingTableModel.update()

			int currentDay = currentTime.getSolOfMonth();

			if (dayCache != currentDay) {
				if (!allTimeMap.isEmpty())
					allTimeMap.clear();
				if (allTimeMapE != null)
					allTimeMapE.clear();

				if (!allQualityMap.isEmpty())
					allQualityMap.clear();
				if (allQualityMapE != null)
					allQualityMapE.clear();

				if (!allServingsSet.isEmpty())
					allServingsSet.clear();

				dayCache = currentDay;
			}
		}
	}
}
