/**
 * Mars Simulation Project
 * SalvageProjectPanel.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

public class SalvageProjectPanel
extends WizardPanel {

	/** The wizard panel name. */
    private final static String NAME = "Salvage Project";
    
    // Data members
    private JLabel errorMessageLabel;
    private DefaultListModel<Object> projectListModel;
    private JList<Object> projectList;
    private PartsTableModel partsTableModel;
    private JTable partsTable;
    
    /**
     * Constructor
     * @param wizard the create mission wizard.
     */
    SalvageProjectPanel(CreateMissionWizard wizard) {
        // Use WizardPanel constructor.
        super(wizard);
        
        // Set the layout.
        setLayout(new BorderLayout(10, 10));
        
        // Set the border.
        setBorder(new MarsPanelBorder());
        
        // Create the select salvage project label.
        JLabel titleLabel = new JLabel("Select a salvage project", JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        add(titleLabel, BorderLayout.NORTH);

        // Create the center panel.
        JPanel centerPane = new JPanel(new GridLayout(1, 2, 10, 10));
        add(centerPane, BorderLayout.CENTER);
        
        // Create the project panel.
        JPanel projectPane = new JPanel(new BorderLayout(0, 0));
        projectPane.setBorder(new MarsPanelBorder());
        centerPane.add(projectPane);
        
        // Create the salvage project label.
        JLabel projectLabel = new JLabel("Buildings and Construction Sites", JLabel.CENTER);
        projectPane.add(projectLabel, BorderLayout.NORTH);
        
        // Create scroll pane for salvage project selection list.
        JScrollPane projectListScrollPane = new JScrollPane();
        projectListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        projectPane.add(projectListScrollPane, BorderLayout.CENTER);
        
        // Create project selection list.
        projectListModel = new DefaultListModel<Object>();
        populateProjectListModel();
        projectList = new JList<Object>(projectListModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                partsTableModel.update();
                Object project = projectList.getSelectedValue();
                if (project != null) {
                    getWizard().setButtons(true);
                    errorMessageLabel.setText(" ");
                }
                else {
                    getWizard().setButtons(false);
                    errorMessageLabel.setText(" ");
                }
            }
        });
        projectListScrollPane.setViewportView(projectList);
        
        // Create the parts panel.
        JPanel partsPane = new JPanel(new BorderLayout(0, 0));
        partsPane.setBorder(new MarsPanelBorder());
        centerPane.add(partsPane);
        
        // Create the parts label.
        JLabel partsLabel = new JLabel("Estimated Salvaged Parts", JLabel.CENTER);
        partsPane.add(partsLabel, BorderLayout.NORTH);
        
        // Create scroll pane for parts table.
        JScrollPane partsTableScrollPane = new JScrollPane();
        partsTableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        partsPane.add(partsTableScrollPane, BorderLayout.CENTER);
        
        // Create the parts table model.
        partsTableModel = new PartsTableModel();
        
        // Create the parts table.
        partsTable = new JTable(partsTableModel);
		TableStyle.setTableStyle(partsTable);
        partsTable.setRowSelectionAllowed(false);
        partsTableScrollPane.setViewportView(partsTable);
        
        // Create the error message label.
        errorMessageLabel = new JLabel(" ", JLabel.CENTER);
        errorMessageLabel.setForeground(Color.RED);
        errorMessageLabel.setFont(errorMessageLabel.getFont().deriveFont(Font.BOLD));
        errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(errorMessageLabel, BorderLayout.SOUTH);
    }
    
    @Override
    void clearInfo() {
        projectListModel.clear();
        partsTableModel.update();
        getWizard().setButtons(false);
        errorMessageLabel.setText(" ");
    }

    @Override
    boolean commitChanges() {
        
        Object project = projectList.getSelectedValue();
        if (project != null) {
            if (project instanceof Building) {
                // Set salvage building.
                Building salvageBuilding = (Building) project;
                getWizard().getMissionData().setSalvageBuilding(salvageBuilding);
                return true;
            }
            else if (project instanceof ConstructionSite) {
                // Set salvage site.
                ConstructionSite salvageSite = (ConstructionSite) project;
                getWizard().getMissionData().setSalvageSite(salvageSite);
                return true;
            }
        }
        
        return false;
    }

    @Override
    String getPanelName() {
        return NAME;
    }

    @Override
    void updatePanel() {
        populateProjectListModel();
        getWizard().setButtons(false);
    }
    
    /**
     * Populates the project list model.
     */
    private void populateProjectListModel() {
        // Clear project list model.
        projectListModel.clear();
        
        Settlement salvageSettlement = getWizard().getMissionData().getSalvageSettlement();
        if (salvageSettlement != null) {
            
            // Add settlement buildings to list.
            java.util.List<Building> buildingList = salvageSettlement.getBuildingManager().getSortedBuildings();//.getACopyOfBuildings();
            //Collections.sort(buildingList);
            Iterator<Building> i = buildingList.iterator();
            while (i.hasNext()) projectListModel.addElement(i.next());
            
            // Add construction sites.
            Iterator<ConstructionSite> j = salvageSettlement.getConstructionManager().
                    getConstructionSitesNeedingSalvageMission().iterator();
            while (j.hasNext()) projectListModel.addElement(j.next());
        }
    }
    
    /**
     * A table model for salvage parts.
     */
    private class PartsTableModel extends AbstractTableModel {

        /** default serial id. */
    	private static final long serialVersionUID = 1L;

    	// Data members.
        private ConstructionStageInfo info = null;
        private Map<Integer, Integer> partsNumber;
        
        /**
         * Constructor.
         */
        private PartsTableModel() {
            // Use AbstractTableModel constructor.
            super();
            
            partsNumber = new HashMap<>();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        @Override
        public String getColumnName(int columnIndex) {
            String result = "";
            if (columnIndex == 0) result = "Part";
            else if (columnIndex == 1) result = "Estimated Salvage";
            return result;
        }

        public int getRowCount() {
            return partsNumber.keySet().size();
        }

        public Object getValueAt(int row, int col) {
            if ((row < partsNumber.keySet().size()) && (col < 2)) {
//                Part part = (Part) partsNumber.keySet().toArray()[row];
                Integer part = (int)partsNumber.keySet().toArray()[row];
                if (col == 0) {
                    return part.toString();
                }
                else if (col == 1) {
                    return partsNumber.get(part);
                }
                else return null;
            }
            else return null;
        }
        
        /**
         * Update the table.
         */
        private void update() {
            info = null;
            Object project = projectList.getSelectedValue();
            if (project instanceof Building) {
                Building salvageBuilding = (Building) project;
                try {
                    info = ConstructionUtil.getConstructionStageInfo(salvageBuilding.getBuildingType());
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            else if (project instanceof ConstructionSite) {
                ConstructionSite salvageSite = (ConstructionSite) project;
                info = salvageSite.getCurrentConstructionStage().getInfo();
            }
            partsNumber.clear();
            populatePartsNumber();
            fireTableStructureChanged();
        }
        
        /**
         * Populate the map of estimated salvage parts and their numbers.
         */
        private void populatePartsNumber() {
            
            if (info != null) {
                try {
                    // Get average construction skill of mission members.
                    double totalSkill = 0D;
                    double averageSkill = 0;
                    
                	Person person = null;
                	Robot robot = null;
                	
                    int memberNum = getWizard().getMissionData().getMixedMembers().size();
                    // Add mission members.
                    Iterator<MissionMember> i = getWizard().getMissionData().getMixedMembers().iterator();
                    while (i.hasNext()) {
                     	
                        // TODO Refactor
                        MissionMember member = i.next();
            	        if (member instanceof Person) {
            	        	person = (Person) member;
            	        	int constructionSkill = person.getSkillManager().getSkillLevel(SkillType.CONSTRUCTION);
                            totalSkill += constructionSkill;
            	        }
            	        else if (member instanceof Robot) {
            	        	robot = (Robot) member;
            	        	int constructionSkill = robot.getSkillManager().getSkillLevel(SkillType.CONSTRUCTION);
                            totalSkill += constructionSkill;
            	        }    
                    }
                    
                    averageSkill = totalSkill / memberNum;
                    
                    // Get chance of salvage.
                    double salvageChance = 50D + (averageSkill * 5D);
                    if (salvageChance > 100D) salvageChance = 100D;
                    
                    // Estimate parts salvaged.
                    Iterator<Integer> j = info.getParts().keySet().iterator();
                    while (j.hasNext()) {
                        Integer part = j.next();
                        int maxSalvage = info.getParts().get(part);
                        int estimatedSalvage = (int) Math.round((double) maxSalvage * (salvageChance / 100D));
                        partsNumber.put(part, estimatedSalvage);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }
}