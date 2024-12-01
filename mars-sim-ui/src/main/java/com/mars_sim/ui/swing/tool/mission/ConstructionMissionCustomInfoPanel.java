/*
 * Mars Simulation Project
 * ConstructionMissionCustomInfoPanel.java
 * @date 2023-07-24
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.construction.ConstructionSite;
import com.mars_sim.core.structure.construction.ConstructionStage;
import com.mars_sim.core.structure.construction.ConstructionStageInfo;
import com.mars_sim.core.structure.construction.ConstructionVehicleType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * A panel for displaying construction custom mission information.
 */
@SuppressWarnings("serial")
public class ConstructionMissionCustomInfoPanel
extends MissionCustomInfoPanel
implements UnitListener {

    // Data members.
    private MainDesktopPane desktop;
    private ConstructionMission mission;
    private ConstructionSite site;
    
    private JLabel stageLabel;
    private JLabel settlementLabel;
    private JLabel siteLabel;
    private JPanel processPanel;
    
    private BoundedRangeModel progressBarModel;
  
    private MaterialsTableModel materialsTableModel;
    
    private JScrollPane scrollPane;

    /**
     * Constructor.
     * 
     * @param desktop the main desktop panel.
     */
    public ConstructionMissionCustomInfoPanel(MainDesktopPane desktop) {
        // Use MissionCustomInfoPanel constructor.
        super();

        // Initialize data members.
        this.desktop = desktop;

        // Set layout.
        setLayout(new BorderLayout());

        JPanel contentsPanel = new JPanel(new BorderLayout(5, 5));
        add(contentsPanel, BorderLayout.NORTH);

        // Prepare SpringLayout for info panel.
     	AttributePanel infoPanel = new AttributePanel(3);
     	contentsPanel.add(infoPanel, BorderLayout.NORTH);

        String siteLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.titleLabel"); //$NON-NLS-1$
        siteLabel = infoPanel.addTextField(siteLabelString, "", null);

     	String settlementLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.settlementLabel"); //$NON-NLS-1$
     	settlementLabel = infoPanel.addTextField(settlementLabelString, "", null);

        String stageLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.stageLabel"); //$NON-NLS-1$
        stageLabel = infoPanel.addTextField(stageLabelString, "", null);
        
        // Process panel    
        processPanel = new JPanel(new GridLayout(2, 1));
        contentsPanel.add(processPanel,  BorderLayout.CENTER);
          
        JPanel progressBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        processPanel.add(progressBarPanel);

        // Add tooltip.
        processPanel.setToolTipText(getToolTipString());
        
        JLabel progressLabel = new JLabel("Site Completion", SwingConstants.CENTER);
        processPanel.add(progressLabel);
        
        JProgressBar progressBar = new JProgressBar();
        progressBarModel = progressBar.getModel();
        progressBar.setStringPainted(true);
        progressBarPanel.add(progressBar);
         
        // Create remaining construction materials label panel.
        JPanel remainingMaterialsLabelPane = new JPanel(new BorderLayout(1, 1));
        add(remainingMaterialsLabelPane, BorderLayout.CENTER);
        
        // Create remaining construction materials label.
        String remainingMaterialsLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.constructionMaterials"); //$NON-NLS-1$
        Border blackline = StyleManager.createLabelBorder(remainingMaterialsLabelString);
        remainingMaterialsLabelPane.setBorder(blackline);
        
        // Create the construction materials table and model.
        materialsTableModel = new MaterialsTableModel();
        JTable materialsTable = new JTable(materialsTableModel);
        materialsTable.setPreferredSize(new Dimension(-1, 100));  
        materialsTable.setRowSelectionAllowed(true);

        // Create a scroll pane for the remaining construction materials table.
        scrollPane = new JScrollPane();
        remainingMaterialsLabelPane.add(scrollPane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(materialsTable);
    }

    @Override
    public void updateMission(Mission mission) {
        // Remove as construction listener if necessary.
        if (site != null) {
            site.removeUnitListener(this);
        }

        if (mission instanceof ConstructionMission m) {
            this.mission = m;
            site = this.mission.getConstructionSite();
            
            if (site != null) {
                site.addUnitListener(this);
       
                siteLabel.setText(site.getName());
                
                settlementLabel.setText(m.getAssociatedSettlement().getName());
                
                stageLabel.setText(getStageString());
                
                updateProgressBar();

                // Update remaining construction materials table.
                materialsTableModel.updateTable();
            }
        }
    }

    @Override
    public void updateMissionEvent(MissionEvent e) {
        stageLabel.setText(getStageString());

        // Update remaining construction materials table.
        materialsTableModel.updateTable();
    }

    /**
     * Catches construction update event.
     * 
     * @param event the mission event.
     */
    @Override
    public void unitUpdate(UnitEvent event) {
        if (UnitEventType.ADD_CONSTRUCTION_WORK_EVENT == event.getType()) {
            // Update the progress bar
            updateProgressBar();

        }
        else if (UnitEventType.ADD_CONSTRUCTION_MATERIALS_EVENT == event.getType()) {
            // Update remaining construction materials table.
            materialsTableModel.updateTable();
        }
    }

    /**
     * Gets the stage label string.
     * 
     * @return stage string.
     */
    private String getStageString() {
        StringBuilder stageString = new StringBuilder();
        if (mission != null) {
            ConstructionStage stage = mission.getConstructionStage();
            if (stage != null) {
                stageString.append(stage.getInfo().getName());
            }
        }

        return stageString.toString();
    }

    /**
     * Updates the progress bar.
     */
    private void updateProgressBar() {
        int workProgress = 0;
        if (mission != null) {
            ConstructionStage stage = mission.getConstructionStage();
            if (stage != null) {
                double completedWork = stage.getCompletedWorkTime();
                double requiredWork = stage.getRequiredWorkTime();
                if (requiredWork > 0D) {
                    workProgress = (int) (100D * completedWork / requiredWork);
                }
            }
        }
        progressBarModel.setValue(workProgress);
        
        // Update the tool tip string.
        processPanel.setToolTipText(getToolTipString());
    }

    /**
     * Gets a tool tip string for the panel.
     */
    private String getToolTipString() {
        StringBuilder result = new StringBuilder(Msg.HTML_START);

        ConstructionStage stage = null;
        if (site != null) {
            stage = site.getCurrentConstructionStage();
        }

        if (stage != null) {
            ConstructionStageInfo info = stage.getInfo();
            result.append("Status: building ").append(info.getName()).append(Msg.BR);
            result.append("Stage Type: ").append(info.getType()).append(Msg.BR);
            result.append("Work Type: Construction").append(Msg.BR);
            String requiredWorkTime = StyleManager.DECIMAL_PLACES1.format(stage.getRequiredWorkTime() / 1000D);
            result.append("Work Time Required: ").append(requiredWorkTime).append(" Sols").append(Msg.BR);
            String completedWorkTime = StyleManager.DECIMAL_PLACES1.format(stage.getCompletedWorkTime() / 1000D);
            result.append("Work Time Completed: ").append(completedWorkTime).append(" Sols").append(Msg.BR);
            result.append("Architect Construction Skill Required: ").append(info.getArchitectConstructionSkill()).append(Msg.BR);

            // Add remaining construction resources.
            if (!stage.getMissingResources().isEmpty()) {
                result.append(Msg.BR).append("Missing Construction Resources:").append(Msg.BR);
                Iterator<Integer> i = stage.getMissingResources().keySet().iterator();
                while (i.hasNext()) {
                	Integer resource = i.next();
                    double amount = stage.getMissingResources().get(resource);
                    result.append(Msg.NBSP).append(Msg.NBSP)
                    .append(ResourceUtil.findAmountResource(resource).getName()).append(": ").append(amount).append(" kg").append(Msg.BR);
                }
            }

            // Add Missing construction parts.
            if (!stage.getMissingParts().isEmpty()) {
                result.append(Msg.BR).append("Missing Construction Parts:").append(Msg.BR);
                Iterator<Integer> j = stage.getMissingParts().keySet().iterator();
                while (j.hasNext()) {
                	Integer part = j.next();
                    int number = stage.getMissingParts().get(part);
                    result.append(Msg.NBSP).append(Msg.NBSP)
                    .append(ItemResourceUtil.findItemResourceName(part)).append(": ").append(number).append(Msg.BR);
                }
            }

            // Add construction vehicles.
            if (!info.getVehicles().isEmpty()) {
                result.append(Msg.BR).append("Construction Vehicles").append(Msg.BR);
                Iterator<ConstructionVehicleType> k = info.getVehicles().iterator();
                while (k.hasNext()) {
                    ConstructionVehicleType vehicle = k.next();
                    result.append(Msg.NBSP).append(Msg.NBSP).append("Vehicle Type: ").append(vehicle.getVehicleType()).append(Msg.BR);
                    result.append(Msg.NBSP).append(Msg.NBSP).append("Attachment Parts:").append(Msg.BR);
                    Iterator<Integer> l = vehicle.getAttachmentParts().iterator();
                    while (l.hasNext()) {
                        result.append(Msg.NBSP).append(Msg.NBSP).append(Msg.NBSP).append(Msg.NBSP)
                        .append("-").append(ItemResourceUtil.findItemResourceName(l.next())).append(Msg.BR);
                    }
                }
            }
        }

        result.append(Msg.HTML_STOP);

        return result.toString();
    }

    /**
     * Model for the construction materials table.
     */
    private class MaterialsTableModel extends AbstractTableModel {

        /** default serial id. */
        private static final long serialVersionUID = 1L;

        // Data members.
        protected Map<Good, Integer> missingMap;
        protected Map<Good, Integer> availableMap;
        protected Map<Good, Integer> originalMap;
        protected List<Good> goodsList;

        /**
         * Constructor.
         */
        private MaterialsTableModel() {
            // Use AbstractTableModel constructor.
            super();

            // Initialize goods map and list.
            goodsList = new ArrayList<>();
            originalMap = new HashMap<>();
            availableMap = new HashMap<>();
            missingMap = new HashMap<>();
        }

        /**
         * Returns the number of rows in the model.
         * 
         * @return number of rows.
         */
        @Override
        public int getRowCount() {
            return goodsList.size();
        }

        /**
         * Returns the number of columns in the model.
         * 
         * @return number of columns.
         */
        @Override
        public int getColumnCount() {
            return 4;
        }

        /**
         * Returns the name of the column at columnIndex.
         * 
         * @param columnIndex the column index.
         * @return column name.
         */
        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return Msg.getString("ConstructionMissionCustomInfoPanel.column.material"); //$NON-NLS-1$
            }
            else if (columnIndex == 1) {
                return Msg.getString("ConstructionMissionCustomInfoPanel.column.missing"); //$NON-NLS-1$
            }
            else if (columnIndex == 2) {
                return Msg.getString("ConstructionMissionCustomInfoPanel.column.available"); //$NON-NLS-1$
            }
            else {
                return Msg.getString("ConstructionMissionCustomInfoPanel.column.original"); //$NON-NLS-1$
            }
        }

        /**
         * Returns the value for the cell at columnIndex and rowIndex.
         * 
         * @param row the row whose value is to be queried.
         * @param column the column whose value is to be queried.
         * @return the value Object at the specified cell.
         */
        @Override
        public Object getValueAt(int row, int column) {
            Object result = Msg.getString("unknown"); //$NON-NLS-1$

            if (row < goodsList.size()) {
                Good good = goodsList.get(row);
                if (column == 0) {
                    result = good.getName();
                }
                else if (column == 1) {
                    result = missingMap.get(good);
                }
                else if (column == 2) {
                    result = availableMap.get(good);
                }
                else {
                    result = originalMap.get(good);
                }
            }

            return result;
        }

        /**
         * Updates the table data.
         */
        protected void updateTable() {
        	
            // Populate originalMap.
            ConstructionStage stage = mission.getConstructionStage();
            if (stage != null) {

                originalMap = new HashMap<>();
                
                // Add original resources.
                Iterator<Integer> i0 = stage.getOriginalResources().keySet().iterator();
                while (i0.hasNext()) {
                	Integer resource = i0.next();
                    double amount = stage.getOriginalResources().get(resource);
                    originalMap.put(GoodsUtil.getGood(resource), (int) amount);
                }

                int size = originalMap.size();
                if (size > 0)
                	scrollPane.setPreferredSize(new Dimension(-1, size * 40));
                
                // Add original parts.
                Iterator<Integer> j0 = stage.getOriginalParts().keySet().iterator();
                while (j0.hasNext()) {
                	Integer part = j0.next();
                    int num = stage.getOriginalParts().get(part);
                    originalMap.put(GoodsUtil.getGood(part), num);
                }

                goodsList = new ArrayList<>(originalMap.keySet());
                Collections.sort(goodsList);

                // Add available resources.
                availableMap = new HashMap<>();
                
                Iterator<Integer> i1 = stage.getAvailableResources().keySet().iterator();
                while (i1.hasNext()) {
                	Integer resource = i1.next();
                    double amount = stage.getAvailableResources().get(resource);
                    availableMap.put(GoodsUtil.getGood(resource), (int) amount);
                }

                Iterator<Integer> j1 = stage.getAvailableParts().keySet().iterator();
                while (j1.hasNext()) {
                	Integer part = j1.next();
                    int num = stage.getAvailableParts().get(part);
                    availableMap.put(GoodsUtil.getGood(part), num);
                }
                
        
                // Add missing resources.
                missingMap = new HashMap<>();
                
                Iterator<Integer> i2 = stage.getMissingResources().keySet().iterator();
                while (i2.hasNext()) {
                	Integer resource = i2.next();
                    double amount = stage.getMissingResources().get(resource);
                    missingMap.put(GoodsUtil.getGood(resource), (int) amount);
                }

                // Add missing parts.
                Iterator<Integer> j2 = stage.getMissingParts().keySet().iterator();
                while (j2.hasNext()) {
                	Integer part = j2.next();
                    int num = stage.getMissingParts().get(part);
                    missingMap.put(GoodsUtil.getGood(part), num);
                }
            }

            fireTableDataChanged();
        }
    }
}
