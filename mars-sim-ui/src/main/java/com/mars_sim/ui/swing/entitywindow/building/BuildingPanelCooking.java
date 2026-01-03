/*
 * Mars Simulation Project
 * BuildingPanelCooking.java
 * @date 2022-07-11
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.cooking.Cooking;
import com.mars_sim.core.building.function.cooking.DishRecipe;
import com.mars_sim.core.building.function.cooking.PreparedDish;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * This class is a building function panel representing
 * the cooking and food prepation info of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelCooking extends EntityTabPanel<Building>
	implements TemporalComponent {

	private static final String COOKING_ICON = "cooking";
	
	// Domain members
	private Cooking kitchen;
	private JLabel numCooksLabel;
	private JLabel numMealsTodayLabel;
	private JLabel mealGradeLabel;

	// Cache
	private int numCooksCache;
	private String gradeCache = "";
	
	private int numMealsTodayCache;

	private DishTableModel dishTableModel;

	/**
	 * Constructor.
	 * 
	 * @param kitchen the cooking building this panel is for.
	 * @param context the UI context
	 */
	public BuildingPanelCooking(Cooking kitchen, UIContext context) {

		super(
			Msg.getString("BuildingPanelCooking.title"),
			ImageLoader.getIconByName(COOKING_ICON), null,
			context, kitchen.getBuilding()
		);

		this.kitchen = kitchen;
	}
	
	@Override
	protected void buildUI(JPanel center) {
		// Prepare label panel
		AttributePanel labelPanel = new AttributePanel(4);
		center.add(labelPanel, BorderLayout.NORTH);

		// Prepare cook number label
		numCooksCache = kitchen.getNumCooks();
		numCooksLabel = labelPanel.addTextField( Msg.getString("BuildingPanelCooking.numberOfCooks"), 
									Integer.toString(numCooksCache), null); //-NLS-1$

		// Prepare cook capacity label
		labelPanel.addTextField( Msg.getString("BuildingPanelCooking.cookCapacity"), 
									Integer.toString(kitchen.getCookCapacity()), null);

		// Prepare # of today cooked meal label
		numMealsTodayCache = kitchen.getTotalNumberOfCookedMealsToday();
		numMealsTodayLabel = labelPanel.addTextField(Msg.getString("BuildingPanelCooking.mealsToday"),
									Integer.toString(numMealsTodayCache), null); //-NLS-1$

		// Prepare meal grade label
		String grade = DishRecipe.qualityToString(kitchen.getBestMealQuality());
		mealGradeLabel = labelPanel.addTextField(Msg.getString("BuildingPanelCooking.bestQualityOfMeals"),
									grade, null); //-NLS-1$

		
		// Create scroll panel for the outer table panel.
		JScrollPane scrollPane = new JScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		center.add(scrollPane, BorderLayout.CENTER);

		// Prepare cooking table model.
		dishTableModel = new DishTableModel(kitchen);

		// Prepare cooking table.
		var table = new JTable(dishTableModel);

		scrollPane.setViewportView(table);

		// Add the two methods below to make all heatTable columns
		// resizable automatically when its Panel resizes
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setAutoCreateRowSorter(true);							
	}

	/**
	 * Updates this panel on clock pulse.
	 * Ideally could be converted to event driven update later.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		int numCooks = 0;
		numCooks = kitchen.getNumCooks();
		// Update cook number
		if (numCooksCache != numCooks) {
			numCooksCache = numCooks;
			numCooksLabel.setText(Integer.toString(numCooks));
		}


		int numMealsToday = 0;
		numMealsToday = kitchen.getTotalNumberOfCookedMealsToday();
		// Update # of meals cooked today
		if (numMealsTodayCache != numMealsToday) {
			numMealsTodayCache = numMealsToday;
			numMealsTodayLabel.setText(Integer.toString(numMealsToday));
		}

		double mealQuality = kitchen.getBestMealQuality();
		String grade = DishRecipe.qualityToString(mealQuality);
		// Update meal grade
		if (!gradeCache.equals(grade)) {
			gradeCache = grade;
			mealGradeLabel.setText(grade); 
		}

		dishTableModel.update();
	}

	/**
	 * Internal class used as model for the cooking table.
	 */
	private static class DishTableModel extends AbstractTableModel {

		private Cooking kitchen;

		private List<PreparedDish> dishes;
		
		private DishTableModel(Cooking k) {
			this.kitchen = k;

			dishes = new ArrayList<>();
		}

		public int getRowCount() {
			return dishes.size();

		}

		public int getColumnCount() {
			return 3;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType;
			if (columnIndex == 2)
				dataType = MarsTime.class;
			else 
				dataType = String.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> "Dish";
				case 1 -> "Quality";
				case 2 -> "Expires";
				default -> "";
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row >= dishes.size())
				return null;
			
			var d = dishes.get(row);

			return switch(column) {
				case 0 -> d.getName();
				case 1 -> DishRecipe.qualityToString(d.getQuality());
				case 2 -> d.getExpirationTime();
				default -> null;
			};
		}

		public void update() {
			dishes = new ArrayList<>(kitchen.getCookedMealList());
					
			fireTableDataChanged();
		}
	}
}
