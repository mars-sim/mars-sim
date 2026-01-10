/*
 * Mars Simulation Project
 * TabPanelFoodProduction.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FoodProduction;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.food.FoodProductionProcess;
import com.mars_sim.core.food.FoodProductionProcessInfo;
import com.mars_sim.core.food.FoodProductionUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.NamedListCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.entitywindow.structure.FoodProductionPanel;

/**
 * TabPanelFoodProduction is a panel that displays a settlement's food
 * production information.
 */
@SuppressWarnings("serial")
class TabPanelFoodProduction extends EntityTabPanel<Settlement> implements TemporalComponent {

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(TabPanelFoodProduction.class.getName());

	private static final int WORD_WIDTH = 50;
	private static final String FOOD_ICON = "food";
	
	private JPanel foodProductionListPane;
	private JScrollPane foodProductionScrollPane;
	private List<FoodProductionProcess> processCache;

	/** building selector. */
	private JComboBox<Building> buildingComboBox;
	/** List of available foodProduction buildings. */
	private List<Building> buildingComboBoxCache;
	/** Process selector. */
	private JComboBox<FoodProductionProcessInfo> processSelection;
	/** List of available processes. */
	private List<FoodProductionProcessInfo> processSelectionCache;

	/** Process selection button. */
	private JButton newProcessButton;
	/** Checkbox for overriding food production. */
	private JCheckBox overrideCheckbox;

	/**
	 * Constructor.
	 * 
	 * @param settlement The settlement to display.
	 * @param context The UI context.
	 */
	public TabPanelFoodProduction(Settlement settlement, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelFoodProduction.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(FOOD_ICON), null,
			context, settlement);
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Create scroll panel for foodProduction list pane.
		foodProductionScrollPane = new JScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		foodProductionScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		foodProductionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		foodProductionScrollPane.setPreferredSize(new Dimension(220, 215));
		content.add(foodProductionScrollPane, BorderLayout.CENTER);

		// Prepare foodProduction outer list pane.
		JPanel foodProductionOuterListPane = new JPanel(new BorderLayout(0, 0));
		foodProductionScrollPane.setViewportView(foodProductionOuterListPane);

		// Prepare foodProduction list pane.
		foodProductionListPane = new JPanel();
		foodProductionListPane.setLayout(new BoxLayout(foodProductionListPane, BoxLayout.Y_AXIS));
		foodProductionOuterListPane.add(foodProductionListPane, BorderLayout.NORTH);

		var settlement = getEntity();
		// Create the process panels.
		processCache = getFoodProductionProcesses(settlement);
		processCache.forEach(fp -> foodProductionListPane.add(new FoodProductionPanel(fp, true, WORD_WIDTH, getContext())));

		// Create interaction panel.
		JPanel interactionPanel = new JPanel(new GridLayout(4, 1, 0, 0));
		content.add(interactionPanel, BorderLayout.NORTH);

		// Create new building selection.
		buildingComboBoxCache = getFoodProductionBuildings(settlement);
		Collections.sort(buildingComboBoxCache);
		buildingComboBox = new JComboBox<>();
		buildingComboBoxCache.forEach(b -> buildingComboBox.addItem(b));
		buildingComboBox.setRenderer(new NamedListCellRenderer(" (1). Select a Building", SwingConstants.LEFT));
		buildingComboBox.setSelectedIndex(-1);
		buildingComboBox.setToolTipText(Msg.getString("TabPanelFoodProduction.tooltip.selectBuilding")); //$NON-NLS-1$
		buildingComboBox.addItemListener(event -> refreshDetails());
		interactionPanel.add(buildingComboBox);

		// Create new foodProduction process selection.
		Building foodFactoryBuilding = (Building) buildingComboBox.getSelectedItem();
		processSelectionCache = getAvailableProcesses(foodFactoryBuilding);
		processSelection = new JComboBox<>();
		processSelectionCache.forEach(p -> processSelection.addItem(p));
		processSelection.setSelectedIndex(-1);
		processSelection.setRenderer(new FoodProductionSelectionListCellRenderer(" (2). Select a Process"));
		processSelection.setToolTipText(Msg.getString("TabPanelFoodProduction.tooltip.selectAvailableProcess")); //$NON-NLS-1$
		interactionPanel.add(processSelection);

		// Create new process button.
		newProcessButton = new JButton(Msg.getString("TabPanelFoodProduction.button.createNewProcess")); //$NON-NLS-1$
		newProcessButton.setEnabled(processSelection.getItemCount() > 0);
		newProcessButton.setToolTipText(Msg.getString("TabPanelFoodProduction.tooltip.createNewProcess")); //$NON-NLS-1$
		newProcessButton.addActionListener(e -> {
				try {
					Building b = (Building) buildingComboBox.getSelectedItem();
					if (b != null) {
						FoodProduction foodFactory = b.getFoodProduction();
						Object selectedItem = processSelection.getSelectedItem();
						if (selectedItem instanceof FoodProductionProcessInfo sp
								&& FoodProductionUtil.canProcessBeStarted(sp, foodFactory)) {
							foodFactory.addProcess(new FoodProductionProcess(sp, foodFactory));
							refreshDetails();
							
							logger.log(b, Level.CONFIG, 0L, "Player starts the '" 
									+ sp.getName() + "'.");
							
							buildingComboBox.setRenderer(new NamedListCellRenderer(" (1). Select a Building"));
							buildingComboBox.setSelectedIndex(-1);
							processSelection.setRenderer(
									new FoodProductionSelectionListCellRenderer(" (2). Select a Process"));
							processSelection.setSelectedIndex(-1);
						}	
					}
				} catch (Exception ex) {
					logger.severe(Msg.getString("TabPanelFoodProduction.log.newProcessButton"), ex); //$NON-NLS-1$
				}
		});
		interactionPanel.add(newProcessButton);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelFoodProduction.checkbox.overrideProduction")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelFoodProduction.tooltip.overrideProduction")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(e -> setOverride(OverrideType.FOOD_PRODUCTION, overrideCheckbox.isSelected()));
		overrideCheckbox.setSelected(getEntity().getProcessOverride(OverrideType.FOOD_PRODUCTION));
		interactionPanel.add(overrideCheckbox);
	}

	/**
	 * Update the dynamic details of this panel.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		refreshDetails();
	}

	/**
	 * Update the details on this panel.
	 */
	private void refreshDetails() {
		var settlement = getEntity();

		// Update processes if necessary.
		List<FoodProductionProcess> processes = getFoodProductionProcesses(settlement);
		if (!processCache.equals(processes)) {
			// Add foodProduction panels for new processes.
			processes.stream()
				.filter(p -> !processCache.contains(p))
				.forEach(p -> foodProductionListPane.add(new FoodProductionPanel(p, true, WORD_WIDTH, getContext())));

			// Remove foodProduction panels for old processes.
			processCache.stream()
				.filter(p -> !processes.contains(p))
				.forEach(p -> {
					FoodProductionPanel panel = getFoodProductionPanel(p);
					if (panel != null)
						foodProductionListPane.remove(panel);
				});

			foodProductionScrollPane.validate();

			// Update processCache
			processCache.clear();
			processCache.addAll(processes);

		}

		// Update all process panels.
		processes.forEach(process -> {
			FoodProductionPanel panel = getFoodProductionPanel(process);
			if (panel != null)
				panel.update();
		});

		// Update building selection list.
		var newBuildings = getFoodProductionBuildings(settlement);
		if (!newBuildings.equals(buildingComboBoxCache)) {
			buildingComboBoxCache = newBuildings;
			Building currentSelection = (Building) buildingComboBox.getSelectedItem();
			buildingComboBox.removeAllItems();
			buildingComboBoxCache.forEach(b -> buildingComboBox.addItem(b));

			if (currentSelection != null && buildingComboBoxCache.contains(currentSelection))
				buildingComboBox.setSelectedItem(currentSelection);
		}

		// Update process selection list.
		Building selectedBuilding = (Building) buildingComboBox.getSelectedItem();
		var newProcesses = getAvailableProcesses(selectedBuilding);
		if (!newProcesses.equals(processSelectionCache)) {

			processSelectionCache = newProcesses;
			Object currentSelection = processSelection.getSelectedItem();
			processSelection.removeAllItems();

			processSelectionCache.forEach(p -> processSelection.addItem(p));

			if (currentSelection != null && processSelectionCache.contains(currentSelection))
				processSelection.setSelectedItem(currentSelection);
			
		}

		// Update new process button.
		newProcessButton.setEnabled(processSelection.getItemCount() > 0);

		// Update override check box.
		if (settlement.getProcessOverride(OverrideType.FOOD_PRODUCTION) != overrideCheckbox.isSelected())
			overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.FOOD_PRODUCTION));
	}

	/**
	 * Gets all the foodProduction processes at the settlement.
	 * 
	 * @return list of foodProduction processes.
	 */
	private static List<FoodProductionProcess> getFoodProductionProcesses(Settlement settlement) {
		List<FoodProductionProcess> result = new ArrayList<>();

		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.FOOD_PRODUCTION).iterator();
		while (i.hasNext()) {
			FoodProduction foodFactory = i.next().getFoodProduction();
			result.addAll(foodFactory.getProcesses());
		}

		return result;
	}

	/**
	 * Gets the panel for a foodProduction process.
	 * 
	 * @param process the foodProduction process.
	 * @return foodProduction panel or null if none.
	 */
	private FoodProductionPanel getFoodProductionPanel(FoodProductionProcess process) {
		FoodProductionPanel result = null;
		for (int x = 0; x < foodProductionListPane.getComponentCount(); x++) {
			Component component = foodProductionListPane.getComponent(x);
			if (component instanceof FoodProductionPanel panel && panel.getFoodProductionProcess().equals(process))
				return panel;
			
		}
		return result;
	}

	/**
	 * Gets all food production buildings at a settlement.
	 * 
	 * @return vector of buildings.
	 */
	private static List<Building> getFoodProductionBuildings(Settlement settlement) {
		return new ArrayList<>(settlement.getBuildingManager().getBuildingSet(FunctionType.FOOD_PRODUCTION));
	}

	/**
	 * Gets all food production processes available at the foodFactory.
	 * 
	 * @param foodProductionBuilding the manufacturing building.
	 * @return vector of processes.
	 */
	private List<FoodProductionProcessInfo> getAvailableProcesses(Building foodProductionBuilding) {
		List<FoodProductionProcessInfo> result = new ArrayList<>();

		try {
			if (foodProductionBuilding != null) {

				// Determine highest materials science skill level at settlement.
				Settlement settlement = foodProductionBuilding.getSettlement();
				int highestSkillLevel = 0;
				Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
				while (i.hasNext()) {
					Person tempPerson = i.next();
					SkillManager skillManager = tempPerson.getSkillManager();
					int skill = skillManager.getSkillLevel(SkillType.COOKING);
					if (skill > highestSkillLevel) {
						highestSkillLevel = skill;
					}
				}

				// Note: Allow a low material science skill person to have access to 
				// do the next 2 levels of skill process or else difficult 
				// tasks are not learned.
				highestSkillLevel = highestSkillLevel + 2;
				
				// Determine highest materials science skill level at settlement.
				Iterator<Robot> k = settlement.getAllAssociatedRobots().iterator();
				while (k.hasNext()) {
					Robot r = k.next();
					SkillManager skillManager = r.getSkillManager();
					int skill = skillManager.getSkillLevel(SkillType.COOKING);
					if (skill > highestSkillLevel) {
						highestSkillLevel = skill;
					}
				}

				FoodProduction foodFactory = foodProductionBuilding.getFoodProduction();
				if (foodFactory.getCurrentTotalProcesses() < foodFactory.getNumPrintersInUse()) {
					Iterator<FoodProductionProcessInfo> j = FoodProductionUtil
							.getProcessesForTechSkillLevel(foodFactory.getTechLevel(), highestSkillLevel)
							.iterator();
					while (j.hasNext()) {
						FoodProductionProcessInfo process = j.next();
						if (FoodProductionUtil.canProcessBeStarted(process, foodFactory))
							result.add(process);
					}
				}
			}
		} catch (Exception e) {
		}
		// Enable Collections.sorts by implementing Comparable<>
		Collections.sort(result);
		return result;
	}

	/**
	 * Sets the settlement food production override flag.
	 * 
	 * @param override the food production override flag.
	 */
	private void setOverride(OverrideType type, boolean override) {
		getEntity().setProcessOverride(OverrideType.FOOD_PRODUCTION, override);
	}

	/**
	 * Inner class for the food production selection list cell renderer.
	 */
	private static class FoodProductionSelectionListCellRenderer extends DefaultListCellRenderer {

		private static final int PROCESS_NAME_LENGTH = 50;
		private String prompt;

		/*
		 * Set the text to display when no item has been selected.
		 */

		public FoodProductionSelectionListCellRenderer(String prompt) {
			this.prompt = prompt;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof FoodProductionProcessInfo) {
				FoodProductionProcessInfo info = (FoodProductionProcessInfo) value;
				if (info != null) {
					String processName = info.getName();
					if (processName.length() > PROCESS_NAME_LENGTH)
						processName = processName.substring(0, PROCESS_NAME_LENGTH)
								+ Msg.getString("TabPanelFoodProduction.cutOff"); //$NON-NLS-1$

					((JLabel) result).setText(processName);
					((JComponent) result).setToolTipText(FoodProductionPanel.getToolTipString(info));
				}
			}

			if (value == null)
				setText(prompt);

			return result;
		}
	}
}
