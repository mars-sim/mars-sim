/**
 * Mars Simulation Project
 * FoodProductionBuildingPanel.java
 * @version 3.07 2014-11-23
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.text.WordUtils;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcess;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessInfo;
import org.mars_sim.msp.core.foodProduction.FoodProductionUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.FoodProduction;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.FoodProductionPanel;

/**
 * A building panel displaying the foodProduction building function.
 */
public class BuildingPanelFoodProduction
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(BuildingPanelFoodProduction.class.getName());

	/** The foodProduction building. */
	private FoodProduction foodFactory;
	/** Panel for displaying process panels. */
	private JPanel processListPane;
	private JScrollPane processScrollPane;
	/** List of foodProduction processes in building. */
	private List<FoodProductionProcess> processCache;
	/** List of salvage processes in building. */
	//private List<SalvageProcess> salvageCache;
	/** Process selector. */
	private JComboBoxMW processSelection;
	/** List of available processes. */
	private Vector<FoodProductionProcessInfo> processSelectionCache;
	/** List of available salvage processes. */
	//private Vector<SalvageProcessInfo> salvageSelectionCache;
	/** Process selection button. */
	private JButton newProcessButton;

	/**
	 * Constructor.
	 * @param foodFactory the manufacturing building function.
	 * @param desktop the main desktop.
	 */
	public BuildingPanelFoodProduction(FoodProduction foodFactory, MainDesktopPane desktop) {
		// Use BuildingFunctionPanel constructor.
		super(foodFactory.getBuilding(), desktop);

		// Initialize data model.
		this.foodFactory = foodFactory;
		
        // Set panel layout
        setLayout(new BorderLayout());
        
        // Prepare label panel
        //JPanel labelPane = new JPanel(new GridLayout(3, 1, 0, 0));
        JPanel labelPane = new JPanel();
        labelPane.setLayout(new GridLayout(3, 1, 0, 0));
        
        add(labelPane, BorderLayout.NORTH);
        
        // Prepare manufacturing label
        JLabel foodProductionLabel = new JLabel("Food Production", JLabel.CENTER);
        foodProductionLabel.setFont(new Font("Serif", Font.BOLD, 16));
        foodProductionLabel.setForeground(new Color(102, 51, 0)); // dark brown
        labelPane.add(foodProductionLabel);
        
        // Prepare tech level label
        JLabel techLabel = new JLabel("Tech Level: " + foodFactory.getTechLevel(), JLabel.CENTER);
        labelPane.add(techLabel);
        
        // Prepare processCapacity label
        JLabel processCapacityLabel = new JLabel("Process Capacity: " + foodFactory.getConcurrentProcesses(), JLabel.CENTER);
        labelPane.add(processCapacityLabel);
        
        // Create scroll pane for manufacturing processes
        processScrollPane = new JScrollPane();
        processScrollPane.setPreferredSize(new Dimension(170, 90));
        add(processScrollPane, BorderLayout.CENTER);
        
        // Create process list main panel
        JPanel processListMainPane = new JPanel(new BorderLayout(0, 0));
        processScrollPane.setViewportView(processListMainPane);
        
        // Create process list panel
        processListPane = new JPanel();
        processListPane.setLayout(new BoxLayout(processListPane, BoxLayout.Y_AXIS));
        processListMainPane.add(processListPane, BorderLayout.NORTH);
        
        // Create process panels
        processCache = new ArrayList<FoodProductionProcess>(foodFactory.getProcesses());
        Iterator<FoodProductionProcess> i = processCache.iterator();
        while (i.hasNext()) processListPane.add(new FoodProductionPanel(i.next(), false, 23));
        
        // Create salvage panels.
        //salvageCache = new ArrayList<SalvageProcess>(foodFactory.getSalvageProcesses());
        //Iterator<SalvageProcess> j = salvageCache.iterator();
        //while (j.hasNext()) processListPane.add(new SalvagePanel(j.next(), false, 23));
        
        // Create interaction panel.
        JPanel interactionPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        add(interactionPanel, BorderLayout.SOUTH);
        
        // Create new foodProduction process selection.
        processSelectionCache = getAvailableProcesses();
        processSelection = new JComboBoxMW(processSelectionCache);
        processSelection.setRenderer(new FoodProductionSelectionListCellRenderer());
        processSelection.setToolTipText("Select An Available Food Production Process");
        interactionPanel.add(processSelection);
        
        // Add available salvage processes.
        //salvageSelectionCache = getAvailableSalvageProcesses();
        //Iterator<SalvageProcessInfo> k = salvageSelectionCache.iterator();
        //while (k.hasNext()) processSelection.addItem(k.next());
        
        // Create new process button.
        newProcessButton = new JButton("Create New Process");
        newProcessButton.setEnabled(processSelection.getItemCount() > 0);
        newProcessButton.setToolTipText("Create a New Manufacturing Process or Salvage a Process");
        newProcessButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		try {
        		    Object selectedItem = processSelection.getSelectedItem();
        		    if (selectedItem != null) {
        		        if (selectedItem instanceof FoodProductionProcessInfo) {
        		            FoodProductionProcessInfo selectedProcess = (FoodProductionProcessInfo) selectedItem;
        		            if (FoodProductionUtil.canProcessBeStarted(selectedProcess, getFoodFactory())) {
                                getFoodFactory().addProcess(new FoodProductionProcess(selectedProcess, getFoodFactory()));
                                update();
                            }
        		        }
        		        /*
        		        else if (selectedItem instanceof SalvageProcessInfo) {
        		            SalvageProcessInfo selectedSalvage = (SalvageProcessInfo) selectedItem;
        		            if (FoodProductionUtil.canSalvageProcessBeStarted(selectedSalvage, getFoodFactory())) {
        		                Unit salvagedUnit = FoodProductionUtil.findUnitForSalvage(selectedSalvage, 
        		                        getFoodFactory().getBuilding().getBuildingManager().getSettlement());
                                getFoodFactory().addSalvageProcess(new SalvageProcess(selectedSalvage, 
                                        getFoodFactory(), salvagedUnit));
                                update();
                            }
        		        }
        		        */
        		    }
        		}
        		catch (Exception e) {
        			logger.log(Level.SEVERE, "new process button", e);
        		}
        	}
        });
        interactionPanel.add(newProcessButton);
	}
	
	
	@Override
	public void update() {
		
		// Update processes and salvage processes if necessary.
		List<FoodProductionProcess> processes = foodFactory.getProcesses();
		//List<SalvageProcess> salvages = foodFactory.getSalvageProcesses();
		//if (!processCache.equals(processes) || !salvageCache.equals(salvages)) {
		if (!processCache.equals(processes)) {
			
			// Add process panels for new processes.
			Iterator<FoodProductionProcess> i = processes.iterator();
			while (i.hasNext()) {
				FoodProductionProcess process = i.next();
				if (!processCache.contains(process)) 
					processListPane.add(new FoodProductionPanel(process, false, 23));
			}
			/*
			// Add salvage panels for new salvage processes.
			Iterator<SalvageProcess> k = salvages.iterator();
			while (k.hasNext()) {
			    SalvageProcess salvage = k.next();
			    if (!salvageCache.contains(salvage))
			        processListPane.add(new SalvagePanel(salvage, false, 23));
			}
			*/
			// Remove process panels for old processes.
			Iterator<FoodProductionProcess> j = processCache.iterator();
			while (j.hasNext()) {
				FoodProductionProcess process = j.next();
				if (!processes.contains(process)) {
					FoodProductionPanel panel = getFoodProductionPanel(process);
					if (panel != null) processListPane.remove(panel);
				}
			}
			/*
			// Remove salvage panels for old salvages.
			Iterator<SalvageProcess> l = salvageCache.iterator();
            while (l.hasNext()) {
                SalvageProcess salvage = l.next();
                if (!salvages.contains(salvage)) {
                    SalvagePanel panel = getSalvagePanel(salvage);
                    if (panel != null) processListPane.remove(panel);
                }
            }
			*/
			// Update processCache
			processCache.clear();
			processCache.addAll(processes);
			
			// Update salvageCache
			//salvageCache.clear();
			//salvageCache.addAll(salvages);
			
			processScrollPane.validate();
		}
		
		// Update all process panels.
		Iterator<FoodProductionProcess> i = processes.iterator();
		while (i.hasNext()) {
			FoodProductionPanel panel = getFoodProductionPanel(i.next());
			if (panel != null) panel.update();
		}
		/*
		// Update all salvage panels.
		Iterator<SalvageProcess> j = salvages.iterator();
		while (j.hasNext()) {
		    SalvagePanel panel = getSalvagePanel(j.next());
		    if (panel != null) panel.update();
		}
		*/
		// Update process selection list.
		Vector<FoodProductionProcessInfo> newProcesses = getAvailableProcesses();
		//Vector<SalvageProcessInfo> newSalvages = getAvailableSalvageProcesses();
		//if (!newProcesses.equals(processSelectionCache) || 
		 //       !newSalvages.equals(salvageSelectionCache)) {
			if (!newProcesses.equals(processSelectionCache)) {

			processSelectionCache = newProcesses;
			//salvageSelectionCache = newSalvages;
			Object currentSelection = processSelection.getSelectedItem();
			processSelection.removeAllItems();
			
			Iterator<FoodProductionProcessInfo> k = processSelectionCache.iterator();
			while (k.hasNext()) processSelection.addItem(k.next());
			
			//Iterator<SalvageProcessInfo> l = salvageSelectionCache.iterator();
            //while (l.hasNext()) processSelection.addItem(l.next());
			
			if (currentSelection != null) {
				if (processSelectionCache.contains(currentSelection)) 
					processSelection.setSelectedItem(currentSelection);
			}
		}
		
		// Update new process button.
		newProcessButton.setEnabled(processSelection.getItemCount() > 0);
	}
	
	/**
	 * Gets the panel for a foodProduction process.
	 * @param process the foodProduction process.
	 * @return foodProduction panel or null if none.
	 */
	private FoodProductionPanel getFoodProductionPanel(FoodProductionProcess process) {
		FoodProductionPanel result = null;
		
		for (int x = 0; x < processListPane.getComponentCount(); x++) {
			Component component = processListPane.getComponent(x);
			if (component instanceof FoodProductionPanel) {
				FoodProductionPanel panel = (FoodProductionPanel) component;
				if (panel.getFoodProductionProcess().equals(process)) result = panel;
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the panel for a salvage process.
	 * @param process the salvage process.
	 * @return the salvage panel or null if none.
	
	private SalvagePanel getSalvagePanel(SalvageProcess process) {
	    SalvagePanel result = null;
        
        for (int x = 0; x < processListPane.getComponentCount(); x++) {
            Component component = processListPane.getComponent(x);
            if (component instanceof SalvagePanel) {
                SalvagePanel panel = (SalvagePanel) component;
                if (panel.getSalvageProcess().equals(process)) result = panel;
            }
        }
        
        return result;
	}
	 */
	/**
	 * Gets all manufacturing processes available at the foodFactory.
	 * @return vector of processes.
	 */
	private Vector<FoodProductionProcessInfo> getAvailableProcesses() {
		Vector<FoodProductionProcessInfo> result = new Vector<FoodProductionProcessInfo>();
		
		if (foodFactory.getProcesses().size() < foodFactory.getConcurrentProcesses()) {
		    
		    // Determine highest materials science skill level at settlement.
		    Settlement settlement = foodFactory.getBuilding().getBuildingManager().getSettlement();
		    int highestSkillLevel = 0;
            Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
            while (i.hasNext()) {
                Person tempPerson = i.next();
                SkillManager skillManager = tempPerson.getMind().getSkillManager();
                int skill = skillManager.getSkillLevel(SkillType.COOKING);
                if (skill > highestSkillLevel) {
                    highestSkillLevel = skill;
                }
            }
		    
			try {
				Iterator<FoodProductionProcessInfo> j = Collections.unmodifiableList(
				        FoodProductionUtil.getFoodProductionProcessesForTechSkillLevel(
				        foodFactory.getTechLevel(), highestSkillLevel)).iterator();
				while (j.hasNext()) {
					FoodProductionProcessInfo process = j.next();
					if (FoodProductionUtil.canProcessBeStarted(process, foodFactory)) 
						result.add(process);
				}
			}
			catch (Exception e) {}
		}
		
		return result;
	}
	
	/**
	 * Gets all salvage processes available at the foodFactory.
	 * @return vector of salvage processes.
	 
	private Vector<SalvageProcessInfo> getAvailableSalvageProcesses() {
	    Vector<SalvageProcessInfo> result = new Vector<SalvageProcessInfo>();
	    
	    if (foodFactory.getProcesses().size() < foodFactory.getConcurrentProcesses()) {
            try {
                Iterator<SalvageProcessInfo> i = Collections.unmodifiableList(
                        FoodProductionUtil.getSalvageProcessesForTechLevel(
                        foodFactory.getTechLevel())).iterator();
                while (i.hasNext()) {
                    SalvageProcessInfo process = i.next();
                    if (FoodProductionUtil.canSalvageProcessBeStarted(process, foodFactory))
                        result.add(process);
                }
            }
            catch (Exception e) {}
	    }
	    
	    return result;
	}
	*/
	/**
	 * Gets the foodFactory for this panel.
	 * @return foodFactory
	 */
	private FoodProduction getFoodFactory() {
		return foodFactory;
	}
	
	/**
	 * Inner class for the foodProduction selection list cell renderer.
	 */
	private static class FoodProductionSelectionListCellRenderer extends DefaultListCellRenderer {
		
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
				boolean isSelected, boolean cellHasFocus) {
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, 
					cellHasFocus);
			if (value instanceof FoodProductionProcessInfo) {
			    FoodProductionProcessInfo info = (FoodProductionProcessInfo) value;
			    if (info != null) {
			    	// 2014-11-21 Capitalized processName
			        String processName = WordUtils.capitalize(info.getName());
			        if (processName.length() > 30) processName = processName.substring(0, 30) + "...";
			        ((JLabel) result).setText(processName);
			        ((JComponent) result).setToolTipText(FoodProductionPanel.getToolTipString(info, null));
			    }
			}/*
			else if (value instanceof SalvageProcessInfo) {
			    SalvageProcessInfo info = (SalvageProcessInfo) value;
			    if (info != null) {
			    	// 2014-11-21 Capitalized processName
			        String processName = WordUtils.capitalize(info.toString());
			        if (processName.length() > 30) processName = processName.substring(0, 30) + "...";
                    ((JLabel) result).setText(processName);
                    ((JComponent) result).setToolTipText(SalvagePanel.getToolTipString(null, info, null));
			    }
			}*/
			return result;
		}
	}
}