/*
 * Mars Simulation Project
 * TabPanelManufacture.java
 * @date 2024-09-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.mars_sim.core.Unit;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufactureUtil;
import com.mars_sim.core.manufacture.SalvageProcess;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.Manufacture;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.JComboBoxMW;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.ProcessInfoRenderer;
import com.mars_sim.ui.swing.utils.ProcessListPanel;
import com.mars_sim.ui.swing.utils.SalvagePanel;

/**
 * A tab panel displaying settlement manufacturing information.
 */
@SuppressWarnings("serial")
public class TabPanelManufacture extends TabPanel {

	/** Default logger. */
	private static final SimLogger logger = SimLogger.getLogger(TabPanelManufacture.class.getName());
	
	private static final String MANU_ICON ="manufacture";
	private static final String BUTTON_TEXT = Msg.getString("TabPanelManufacture.button.createNewProcess"); // $NON-NLS-1$
	
	/** The Settlement instance. */
	private Settlement target;
	
	private ProcessListPanel manufactureListPane;
	private JScrollPane manufactureScrollPane;
	
	/** building selector. */
	private JComboBoxMW<Building> buildingComboBox;
	/** List of available manufacture buildings. */
	private Set<Building> buildingComboBoxCache;
	/** Process selector. */
	private JComboBoxMW<ManufactureProcessInfo> processSelection;
	/** List of available processes. */
	private List<ManufactureProcessInfo> processSelectionCache;
	/** List of available salvage processes. */
	private List<SalvageProcessInfo> salvageSelectionCache;
	

	/**
	 * Constructor.
	 * 
	 * @param unit    {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelManufacture(Settlement unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelManufacture.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(MANU_ICON),
			Msg.getString("TabPanelManufacture.title"), //$NON-NLS-1$
			unit, desktop
		);

		target = unit;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	protected void buildUI(JPanel content) {
		// Create scroll panel for manufacture list pane.
		manufactureScrollPane = new JScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		manufactureScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		manufactureScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		manufactureScrollPane.setPreferredSize(new Dimension(220, 215));
		content.add(manufactureScrollPane, BorderLayout.CENTER);

		// Prepare manufacture outer list pane.
		JPanel manufactureOuterListPane = new JPanel(new BorderLayout(0, 0));
		manufactureScrollPane.setViewportView(manufactureOuterListPane);

		// Prepare manufacture list pane.
		manufactureListPane = new ProcessListPanel(true);
		manufactureOuterListPane.add(manufactureListPane, BorderLayout.NORTH);

		// Create the process panels.
		manufactureListPane.update(getManufactureProcesses(), getSalvageProcesses());
		
		// Create interaction panel.
		JPanel interactionPanel = new JPanel(new GridLayout(4, 1, 0, 0));
		content.add(interactionPanel, BorderLayout.NORTH);

		// Create new building selection.
		buildingComboBoxCache = getManufacturingBuildings();
		buildingComboBox = new JComboBoxMW<>();
		buildingComboBoxCache.forEach(p -> buildingComboBox.addItem(p));
		buildingComboBox.setRenderer(new PromptComboBoxRenderer(" (1). Select a Building"));
		buildingComboBox.setSelectedIndex(-1);
		buildingComboBox.setToolTipText(Msg.getString("TabPanelManufacture.tooltip.selectBuilding")); //$NON-NLS-1$
		buildingComboBox.addItemListener(event -> loadPotentialProcesses());
		interactionPanel.add(buildingComboBox);

		// Create new manufacture process selection.
		Building workshopBuilding = (Building) buildingComboBox.getSelectedItem();
		processSelectionCache = getAvailableProcesses(workshopBuilding);
		processSelection = new JComboBoxMW<>();
		processSelectionCache.forEach(p -> processSelection.addItem(p));
		processSelection.setSelectedIndex(-1);
		processSelection.setRenderer(new ManufactureSelectionListCellRenderer(" (2). Select a Process"));
		processSelection.setToolTipText(Msg.getString("TabPanelManufacture.tooltip.selectAvailableProcess")); //$NON-NLS-1$
		interactionPanel.add(processSelection);

		// Add available salvage processes.
		salvageSelectionCache = getAvailableSalvageProcesses(workshopBuilding);
		salvageSelectionCache.forEach(k -> processSelection.addItem(k));

		// Create new process button.
		var newProcessButton = new JButton(BUTTON_TEXT); //$NON-NLS-1$
		newProcessButton.setEnabled(false);
		newProcessButton.setToolTipText("Create a New Manufacturing Process or Salvage a Process"); //$NON-NLS-1$
		newProcessButton.addActionListener(event -> createNewProcess());
		interactionPanel.add(newProcessButton);

		// Link the enabled button to the process selection
		processSelection.addItemListener(event -> newProcessButton.setEnabled(event.getStateChange() == ItemEvent.SELECTED));

		// Create manufacturing override check box.
		var controlPanel = new JPanel();
		
		var overrideManuCheckbox = new JCheckBox(Msg.getString("TabPanelManufacture.checkbox.overrideManufacturing")); //$NON-NLS-1$
		overrideManuCheckbox.setToolTipText(Msg.getString("TabPanelManufacture.tooltip.overrideManufacturing")); //$NON-NLS-1$
		overrideManuCheckbox.addActionListener(arg0 ->
						setOverride(OverrideType.MANUFACTURE, overrideManuCheckbox.isSelected()));
		overrideManuCheckbox.setSelected(target.getProcessOverride(OverrideType.MANUFACTURE));
		controlPanel.add(overrideManuCheckbox);
		
		// Create salvaging override check box.
		JCheckBox overrideSalvageCheckbox = new JCheckBox(Msg.getString("TabPanelManufacture.checkbox.overrideSalvaging")); //$NON-NLS-1$
		overrideSalvageCheckbox.setToolTipText(Msg.getString("TabPanelManufacture.tooltip.overrideSalvaging")); //$NON-NLS-1$
		overrideSalvageCheckbox.addActionListener(arg0 ->
						setOverride(OverrideType.SALVAGE, overrideSalvageCheckbox.isSelected()));
		overrideSalvageCheckbox.setSelected(target.getProcessOverride(OverrideType.SALVAGE));
		controlPanel.add(overrideSalvageCheckbox);
		interactionPanel.add(controlPanel);

	}

	/**
	 * Creates a new process in a given building.
	 */
	private void createNewProcess() {
		Building workshopBuilding = (Building) buildingComboBox.getSelectedItem();
		if (workshopBuilding != null) {
			Manufacture workshop = workshopBuilding.getManufacture();
			Object selectedItem = processSelection.getSelectedItem();
			if (selectedItem != null) {
				if (selectedItem instanceof ManufactureProcessInfo selectedProcess) {
					if (ManufactureUtil.canProcessBeStarted(selectedProcess, workshop)) {
						workshop.addProcess(new ManufactureProcess(selectedProcess, workshop));
						
						update();

						logger.log(workshopBuilding, Level.CONFIG, 0, "Player selected the manufacturing process '" 
								+ selectedProcess.getName() + "'.");
						
						showRenderer();
					}
					
					else if (ManufactureUtil.canProcessBeQueued(selectedProcess, workshop)) {
						workshop.addToManuQueue(new ManufactureProcess(selectedProcess, workshop));
						
						update();

						logger.log(workshopBuilding, Level.CONFIG, 0, "Player queued the manufacturing process '" 
								+ selectedProcess.getName() + "'.");
						
						showRenderer();
					}
				} 
				
				else if (selectedItem instanceof SalvageProcessInfo selectedSalvage) {
					if (ManufactureUtil.canSalvageProcessBeStarted(selectedSalvage, workshop)) {
						var salvagedUnit = ManufactureUtil.findUnitForSalvage(selectedSalvage, target);
						workshop.addSalvageProcess(
								new SalvageProcess(selectedSalvage, workshop, salvagedUnit));
						update();

						logger.log(workshopBuilding, Level.CONFIG, 0, "Player selected the salvaging process '" 
								+ salvagedUnit.getName() + "'.");
					}
					else if (ManufactureUtil.canSalvageProcessBeQueued(selectedSalvage, workshop)) {
						var salvagedUnit = ManufactureUtil.findUnitForSalvage(selectedSalvage, target);
						workshop.addToSalvageQueue(
								new SalvageProcess(selectedSalvage, workshop, salvagedUnit));
						update();

						logger.log(workshopBuilding, Level.CONFIG, 0, "Player queued the salvaging process '" 
								+ salvagedUnit.getName() + "'.");
					}
				}
			}
		}
	}

	private void showRenderer() {
		buildingComboBox.setRenderer(new PromptComboBoxRenderer(" (1). Select a Building"));
		buildingComboBox.setSelectedIndex(-1);
		
		processSelection.setRenderer(
				new ManufactureSelectionListCellRenderer(" (2). Select a Process"));
		processSelection.setSelectedIndex(-1);
	}
	
	private class PromptComboBoxRenderer extends DefaultListCellRenderer {

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
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if (value == null) {
				setText(prompt);
				return this;
			}
			return c;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void update() {

		// Update processes if necessary.
		manufactureListPane.update(getManufactureProcesses(), getSalvageProcesses());
		manufactureScrollPane.validate();

		// Update building selection list.
		Set<Building> newBuildings = getManufacturingBuildings();
		if (!newBuildings.equals(buildingComboBoxCache)) {
			buildingComboBoxCache = newBuildings;
			Building currentSelection = (Building) buildingComboBox.getSelectedItem();
			buildingComboBox.removeAllItems();
			buildingComboBoxCache.forEach(b -> buildingComboBox.addItem(b));

			if ((currentSelection != null) && buildingComboBoxCache.contains(currentSelection)) {
				buildingComboBox.setSelectedItem(currentSelection);
			}
		}
	}

	/**
	 * For the current building, load the processes that could potentially be started
	 */
	private void loadPotentialProcesses() {
		// Update process selection list.
		Building selectedBuilding = (Building) buildingComboBox.getSelectedItem();
		List<ManufactureProcessInfo> newProcesses = getAvailableProcesses(selectedBuilding);
		List<SalvageProcessInfo> newSalvages = getAvailableSalvageProcesses(selectedBuilding);
		
		if (!newProcesses.equals(processSelectionCache) || !newSalvages.equals(salvageSelectionCache)) {
			
			processSelectionCache = newProcesses;
			salvageSelectionCache = newSalvages;
			Object currentSelection = processSelection.getSelectedItem();
			processSelection.removeAllItems();
			processSelectionCache.forEach(p -> processSelection.addItem(p));
			salvageSelectionCache.forEach(s -> processSelection.addItem(s));

			if ((currentSelection != null) && processSelectionCache.contains(currentSelection)) {
					processSelection.setSelectedItem(currentSelection);
			}
		}
	}

	/**
	 * Gets all the manufacture processes at the settlement.
	 * 
	 * @return list of manufacture processes.
	 */
	private List<ManufactureProcess> getManufactureProcesses() {
		List<ManufactureProcess> result = new ArrayList<>();

		for(var i : target.getBuildingManager().getBuildingSet(FunctionType.MANUFACTURE)) {
			Manufacture manufacture = i.getManufacture();
			result.addAll(manufacture.getProcesses());
			
			if (!manufacture.isFull()) {
				Iterator<ManufactureProcess> j = manufacture.getQueueManuProcesses().iterator();
				while (j.hasNext()) {
					ManufactureProcess process = j.next();
					result.add(process);
					manufacture.loadFromManuQueue(process);
					// Add only one at a time to ensure it's not full
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Gets all the salvage processes at the settlement.
	 * 
	 * @return list of salvage processes.
	 */
	private List<SalvageProcess> getSalvageProcesses() {
		List<SalvageProcess> result = new ArrayList<>();

		for(var i : target.getBuildingManager().getBuildingSet(FunctionType.MANUFACTURE)) {	
			Manufacture manufacture = i.getManufacture();
			result.addAll(manufacture.getSalvageProcesses());
			
			if (!manufacture.isFull()) {
				for(var process : manufacture.getQueueSalvageProcesses()) {
					result.add(process);
					manufacture.loadFromSalvageQueue(process);
					// Add only one at a time to ensure it's not full
					break;
				}	
			}
		}

		return result;
	}

	/**
	 * Gets all manufacturing buildings at a settlement.
	 * 
	 * @return vector of buildings.
	 */
	private Set<Building> getManufacturingBuildings() {
		return target.getBuildingManager().getBuildingSet(FunctionType.MANUFACTURE);
	}

	/**
	 * Gets all manufacturing processes available at the workshop.
	 * 
	 * @param manufactureBuilding the manufacturing building.
	 * @return vector of processes.
	 */
	private List<ManufactureProcessInfo> getAvailableProcesses(Building manufactureBuilding) {
		List<ManufactureProcessInfo> result = Collections.emptyList();

		if (manufactureBuilding != null) {

			// Determine highest materials science skill level at settlement.
			Settlement settlement = manufactureBuilding.getSettlement();
			int highestSkillLevel = settlement.getAllAssociatedPeople().stream()
				.map(Person::getSkillManager)
				.map(sm -> sm.getSkillLevel(SkillType.MATERIALS_SCIENCE))
				.mapToInt(v -> v)
				.max().orElse(0);
			
			// Note: Allow a low material science skill person to have access to 
			// do the next 2 levels of skill process or else difficult 
			// tasks are not learned.
			highestSkillLevel = highestSkillLevel + 2;

			// Get skill for robots
			int highestRobotSkillLevel = settlement.getAllAssociatedRobots().stream()
				.map(Robot::getSkillManager)
				.map(sm -> sm.getSkillLevel(SkillType.MATERIALS_SCIENCE))
				.mapToInt(v -> v)
				.max().orElse(0);
			highestSkillLevel = Math.max(highestSkillLevel, highestRobotSkillLevel);
					
			Manufacture workshop = manufactureBuilding.getManufacture();
			if (workshop.getCurrentTotalProcesses() < workshop.getNumPrintersInUse()) {
				result = ManufactureUtil.getManufactureProcessesForTechSkillLevel(workshop.getTechLevel(), highestSkillLevel)
							.stream()
							.filter(v -> ManufactureUtil.canProcessBeStarted(v, workshop))
							.sorted()
							.toList();
			}
		}

		return result;
	}

	/**
	 * Gets all salvage processes available at the workshop.
	 * 
	 * @param manufactureBuilding the manufacturing building.
	 * @return vector of processes.
	 */
	private List<SalvageProcessInfo> getAvailableSalvageProcesses(Building manufactureBuilding) {
		List<SalvageProcessInfo> result = Collections.emptyList();
		if (manufactureBuilding != null) {
			Manufacture workshop = manufactureBuilding.getManufacture();
			result = ManufactureUtil.getSalvageProcessesForTechLevel(workshop.getTechLevel()).stream()
					.filter(v -> ManufactureUtil.canSalvageProcessBeStarted(v, workshop))
					.sorted()
					.toList();
		}

		return result;
	}

	/**
	 * Sets the settlement override flag.
	 * 
	 * @param override the override flag.
	 */
	private void setOverride(OverrideType type, boolean override) {
		target.setProcessOverride(type, override);
	}

	/**
	 * Inner class for the manufacture selection list cell renderer.
	 */
	private static class ManufactureSelectionListCellRenderer extends DefaultListCellRenderer {

		private static final int PROCESS_NAME_LENGTH = 70;
		private String prompt;

		/*
		 * Set the text to display when no item has been selected.
		 */
		public ManufactureSelectionListCellRenderer(String prompt) {
			this.prompt = prompt;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				var pinfo = (ProcessInfo) value;
				String processName = pinfo.getName();
				if (processName.length() > PROCESS_NAME_LENGTH)
					processName = processName.substring(0, PROCESS_NAME_LENGTH)
							+ Msg.getString("TabPanelManufacture.cutOff"); //$NON-NLS-1$

				result.setText(processName);
				
				if (value instanceof ManufactureProcessInfo info) {
					result.setToolTipText(ProcessInfoRenderer.getToolTipString(info));
				} else if (value instanceof SalvageProcessInfo info) {
					result.setToolTipText(SalvagePanel.getToolTipString(null, info, null));
				}
			}
			else {
				setText(prompt);
			}
			return result;
		}
	}

	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		target = null;
		manufactureListPane = null;
		manufactureScrollPane = null;
		
		buildingComboBox = null;
		buildingComboBoxCache = null;
		processSelection = null;
		processSelectionCache = null;
		salvageSelectionCache = null;

	}
}
