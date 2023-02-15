/*
 * Mars Simulation Project
 * TabPanelFoodProduction.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.food.FoodProductionProcess;
import org.mars_sim.msp.core.food.FoodProductionProcessInfo;
import org.mars_sim.msp.core.food.FoodProductionUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FoodProduction;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.FoodProductionPanel;

/**
 * TabPanelFoodProduction is a panel that displays a settlement's food
 * production information.
 */
@SuppressWarnings("serial")
public class TabPanelFoodProduction extends TabPanel {

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(TabPanelFoodProduction.class.getName());

	private static final int WORD_WIDTH = 50;
	private static final String FOOD_ICON = "food";
	
	// Data members
	/** The Settlement instance. */
	private Settlement settlement;
	
	private JPanel foodProductionListPane;
	private JScrollPane foodProductionScrollPane;
	private List<FoodProductionProcess> processCache;

	/** building selector. */
	private JComboBoxMW<Building> buildingComboBox;
	/** List of available foodProduction buildings. */
	private Vector<Building> buildingComboBoxCache;
	/** Process selector. */
	private JComboBoxMW<Object> processSelection;
	/** List of available processes. */
	private Vector<FoodProductionProcessInfo> processSelectionCache;

	/** Process selection button. */
	private JButton newProcessButton;
	/** Checkbox for overriding food production. */
	private JCheckBox overrideCheckbox;

	/**
	 * Constructor.
	 * 
	 * @param unit    {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelFoodProduction(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelFoodProduction.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(FOOD_ICON),
			Msg.getString("TabPanelFoodProduction.title"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void buildUI(JPanel content) {
//		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// Create scroll panel for foodProduction list pane.
		foodProductionScrollPane = new JScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		foodProductionScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		foodProductionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		foodProductionScrollPane.setPreferredSize(new Dimension(220, 215));
		content.add(foodProductionScrollPane, BorderLayout.CENTER);

		// Prepare foodProduction outer list pane.
		JPanel foodProductionOuterListPane = new JPanel(new BorderLayout(0, 0));
//		foodProductionOuterListPane.setBorder(new MarsPanelBorder());
		foodProductionScrollPane.setViewportView(foodProductionOuterListPane);

		// Prepare foodProduction list pane.
		foodProductionListPane = new JPanel();
		foodProductionListPane.setLayout(new BoxLayout(foodProductionListPane, BoxLayout.Y_AXIS));
		foodProductionOuterListPane.add(foodProductionListPane, BorderLayout.NORTH);

		// Create the process panels.
		processCache = getFoodProductionProcesses();
		Iterator<FoodProductionProcess> i = processCache.iterator();
		while (i.hasNext())
			foodProductionListPane.add(new FoodProductionPanel(i.next(), true, WORD_WIDTH));

		// Create interaction panel.
		JPanel interactionPanel = new JPanel(new GridLayout(4, 1, 0, 0));
		content.add(interactionPanel, BorderLayout.NORTH);

		// Create new building selection.
		buildingComboBoxCache = getFoodProductionBuildings();
		Collections.sort(buildingComboBoxCache);
		buildingComboBox = new JComboBoxMW<>(buildingComboBoxCache);
		// AddePromptComboBoxRenderer() & setSelectedIndex(-1)
		buildingComboBox.setRenderer(new PromptComboBoxRenderer(" (1). Select a Building"));
		buildingComboBox.setSelectedIndex(-1);
		buildingComboBox.setToolTipText(Msg.getString("TabPanelFoodProduction.tooltip.selectBuilding")); //$NON-NLS-1$
		buildingComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				update();
			}
		});
		interactionPanel.add(buildingComboBox);

		// Create new foodProduction process selection.
		Building foodFactoryBuilding = (Building) buildingComboBox.getSelectedItem();
		processSelectionCache = getAvailableProcesses(foodFactoryBuilding);
		processSelection = new JComboBoxMW(processSelectionCache);
		processSelection.setSelectedIndex(-1);
		processSelection.setRenderer(new FoodProductionSelectionListCellRenderer(" (2). Select a Process"));
		processSelection.setToolTipText(Msg.getString("TabPanelFoodProduction.tooltip.selectAvailableProcess")); //$NON-NLS-1$
		interactionPanel.add(processSelection);

		// Create new process button.
		newProcessButton = new JButton(Msg.getString("TabPanelFoodProduction.button.createNewProcess")); //$NON-NLS-1$
		newProcessButton.setEnabled(processSelection.getItemCount() > 0);
		newProcessButton.setToolTipText(Msg.getString("TabPanelFoodProduction.tooltip.createNewProcess")); //$NON-NLS-1$
		newProcessButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					Building foodFactoryBuilding = (Building) buildingComboBox.getSelectedItem();
					if (foodFactoryBuilding != null) {
						FoodProduction foodFactory = foodFactoryBuilding.getFoodProduction();
						Object selectedItem = processSelection.getSelectedItem();
						if (selectedItem != null) {
							if (selectedItem instanceof FoodProductionProcessInfo) {
								FoodProductionProcessInfo selectedProcess = (FoodProductionProcessInfo) selectedItem;
								if (FoodProductionUtil.canProcessBeStarted(selectedProcess, foodFactory)) {
									foodFactory.addProcess(new FoodProductionProcess(selectedProcess, foodFactory));
									update();
									
									logger.log(foodFactoryBuilding, Level.CONFIG, 0L, "Player starts the '" 
											+ selectedProcess.getName() + "'.");
									
									buildingComboBox.setRenderer(new PromptComboBoxRenderer(" (1). Select a Building"));
									buildingComboBox.setSelectedIndex(-1);
									processSelection.setRenderer(
											new FoodProductionSelectionListCellRenderer(" (2). Select a Process"));
									processSelection.setSelectedIndex(-1);
								}
							}
						}
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, Msg.getString("TabPanelFoodProduction.log.newProcessButton"), e); //$NON-NLS-1$
				}
			}
		});
		interactionPanel.add(newProcessButton);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelFoodProduction.checkbox.overrideProduction")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelFoodProduction.tooltip.overrideProduction")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setOverride(OverrideType.FOOD_PRODUCTION, overrideCheckbox.isSelected());
			}
		});
		overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.FOOD_PRODUCTION));
		interactionPanel.add(overrideCheckbox);
	}

	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private String prompt;

		/*
		 * Set the text to display when no item has been selected.
		 */
		public PromptComboBoxRenderer(String prompt) {
			this.prompt = prompt;
		}

		/*
		 * Custom rendering to display the prompt text when no item is selected
		 */
		// Add color rendering
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value == null) {
				setText(prompt);
				return this;
			}
//			if (c instanceof JLabel) {
//				if (isSelected) {
//					c.setBackground(Color.orange);
//				} else {
//					c.setBackground(Color.white);
//				}
//			} else {
//				c.setBackground(Color.white);
//				c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//			}
			return c;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update() {
		
		// Update processes if necessary.
		List<FoodProductionProcess> processes = getFoodProductionProcesses();
		if (!processCache.equals(processes)) {

			// Add foodProduction panels for new processes.
			Iterator<FoodProductionProcess> i = processes.iterator();
			while (i.hasNext()) {
				FoodProductionProcess process = i.next();
				if (!processCache.contains(process))
					foodProductionListPane.add(new FoodProductionPanel(process, true, WORD_WIDTH));
			}

			// Remove foodProduction panels for old processes.
			Iterator<FoodProductionProcess> j = processCache.iterator();
			while (j.hasNext()) {
				FoodProductionProcess process = j.next();
				if (!processes.contains(process)) {
					FoodProductionPanel panel = getFoodProductionPanel(process);
					if (panel != null)
						foodProductionListPane.remove(panel);
				}
			}

			foodProductionScrollPane.validate();

			// Update processCache
			processCache.clear();
			processCache.addAll(processes);

		}

		// Update all process panels.
		Iterator<FoodProductionProcess> i = processes.iterator();
		while (i.hasNext()) {
			FoodProductionPanel panel = getFoodProductionPanel(i.next());
			if (panel != null)
				panel.update();
		}

		// Update building selection list.
		Vector<Building> newBuildings = getFoodProductionBuildings();
		if (!newBuildings.equals(buildingComboBoxCache)) {
			buildingComboBoxCache = newBuildings;
			Building currentSelection = (Building) buildingComboBox.getSelectedItem();
			buildingComboBox.removeAllItems();
			Iterator<Building> k = buildingComboBoxCache.iterator();
			while (k.hasNext())
				buildingComboBox.addItem(k.next());

			if (currentSelection != null) {
				if (buildingComboBoxCache.contains(currentSelection))
					buildingComboBox.setSelectedItem(currentSelection);
			}
		}

		// Update process selection list.
		Building selectedBuilding = (Building) buildingComboBox.getSelectedItem();
		Vector<FoodProductionProcessInfo> newProcesses = getAvailableProcesses(selectedBuilding);
		
		if (!newProcesses.equals(processSelectionCache)) {

			processSelectionCache = newProcesses;
			Object currentSelection = processSelection.getSelectedItem();
			processSelection.removeAllItems();

			Iterator<FoodProductionProcessInfo> l = processSelectionCache.iterator();
			while (l.hasNext())
				processSelection.addItem(l.next());

			if (currentSelection != null) {
				if (processSelectionCache.contains(currentSelection))
					processSelection.setSelectedItem(currentSelection);
			}
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
	private List<FoodProductionProcess> getFoodProductionProcesses() {
		List<FoodProductionProcess> result = new ArrayList<>();

		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.FOOD_PRODUCTION).iterator();
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
			if (component instanceof FoodProductionPanel) {
				FoodProductionPanel panel = (FoodProductionPanel) component;
				if (panel.getFoodProductionProcess().equals(process))
					result = panel;
			}
		}
		return result;
	}

	/**
	 * Gets all food production buildings at a settlement.
	 * 
	 * @return vector of buildings.
	 */
	private Vector<Building> getFoodProductionBuildings() {
		return new Vector<>(settlement.getBuildingManager().getBuildings(FunctionType.FOOD_PRODUCTION));
	}

	/**
	 * Gets all food production processes available at the foodFactory.
	 * 
	 * @param foodProductionBuilding the manufacturing building.
	 * @return vector of processes.
	 */
	private Vector<FoodProductionProcessInfo> getAvailableProcesses(Building foodProductionBuilding) {
		Vector<FoodProductionProcessInfo> result = new Vector<>();

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
							.getFoodProductionProcessesForTechSkillLevel(foodFactory.getTechLevel(), highestSkillLevel)
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
		settlement.setProcessOverride(OverrideType.FOOD_PRODUCTION, override);
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

		// TODO check actual combobox size before cutting off too much of the processes'
		// names
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
					((JComponent) result).setToolTipText(FoodProductionPanel.getToolTipString(info, null));
				}
			}

			if (value == null)
				setText(prompt);

			return result;
		}
	}

	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		settlement = null;
		foodProductionListPane = null;
		foodProductionScrollPane = null;
		processCache = null;

		buildingComboBox = null;
		buildingComboBoxCache = null;
		processSelection = null;
		processSelectionCache = null;
		newProcessButton = null;
		overrideCheckbox = null;
	}
}
