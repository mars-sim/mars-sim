/*
 * Mars Simulation Project
 * ConstructionMissionCustomInfoPanel.java
 * @date 2023-07-24
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.person.ai.fav.Favorite;
import org.mars_sim.msp.core.person.ai.mission.ConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionEvent;
import org.mars_sim.msp.core.structure.construction.ConstructionListener;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * A panel for displaying construction custom mission information.
 */
@SuppressWarnings("serial")
public class ConstructionMissionCustomInfoPanel
extends MissionCustomInfoPanel
implements ConstructionListener {

    // Data members.
    private MainDesktopPane desktop;
    private ConstructionMission mission;
    private ConstructionSite site;
    
    private JLabel stageLabel;
    private JLabel settlementLabel;
    private JLabel siteLabel;
    
    private BoundedRangeModel progressBarModel;
//    private JButton settlementButton;
    
    private RemainingMaterialsTableModel remainingMaterialsTableModel;

    /**
     * Constructor.
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
        
        // Create remaining construction materials label panel.
        JPanel remainingMaterialsLabelPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        contentsPanel.add(remainingMaterialsLabelPane,  BorderLayout.CENTER);
        
        // Create remaining construction materials label.
        String remainingMaterialsLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.remainingMaterialsLabel"); //$NON-NLS-1$
        Border blackline = StyleManager.createLabelBorder(remainingMaterialsLabelString);
        remainingMaterialsLabelPane.setBorder(blackline);
        
        // Create a scroll pane for the remaining construction materials table.
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        scrollPane.setPreferredSize(new Dimension(-1, 100));
        scrollPane.add(remainingMaterialsLabelPane);
        
        // Create the remaining construction materials table and model.
        remainingMaterialsTableModel = new RemainingMaterialsTableModel();
        JTable remainingMaterialsTable = new JTable(remainingMaterialsTableModel);
        scrollPane.setViewportView(remainingMaterialsTable);
   
        add(scrollPane,  BorderLayout.CENTER);
        
        // Add tooltip.
        setToolTipText(getToolTipString());
        
        // Process panel    
        JPanel processPanel = new JPanel(new GridLayout(2, 1));
        add(processPanel,  BorderLayout.SOUTH);
          
        JPanel progressBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        processPanel.add(progressBarPanel);

        JLabel progressLabel = new JLabel("Site Completion", JLabel.CENTER);
        processPanel.add(progressLabel);
        
        JProgressBar progressBar = new JProgressBar();
        progressBarModel = progressBar.getModel();
        progressBar.setStringPainted(true);
        progressBarPanel.add(progressBar);
        
    }

    @Override
    public void updateMission(Mission mission) {
        // Remove as construction listener if necessary.
        if (site != null) {
            site.removeConstructionListener(this);
        }

        if (mission instanceof ConstructionMission) {
            this.mission = (ConstructionMission) mission;
            site = this.mission.getConstructionSite();
            
            if (site != null) {
                site.addConstructionListener(this);
       
                siteLabel.setText(site.getName());
                
                settlementLabel.setText(mission.getAssociatedSettlement().getName());
                
                stageLabel.setText(getStageString());
                
                updateProgressBar();

                // Update remaining construction materials table.
                remainingMaterialsTableModel.updateTable();

                // Update the tool tip string.
                setToolTipText(getToolTipString());
            }
        }
    }

    @Override
    public void updateMissionEvent(MissionEvent e) {
        stageLabel.setText(getStageString());

        // Update remaining construction materials table.
        remainingMaterialsTableModel.updateTable();
    }

    /**
     * Catch construction update event.
     * @param event the mission event.
     */
    public void constructionUpdate(ConstructionEvent event) {
        if (ConstructionStage.ADD_CONSTRUCTION_WORK_EVENT.equals(event.getType())) {
            updateProgressBar();

            // Update the tool tip string.
            setToolTipText(getToolTipString());
        }
        else if (ConstructionStage.ADD_CONSTRUCTION_MATERIALS_EVENT.equals(event.getType())) {

            // Update remaining construction materials table.
            remainingMaterialsTableModel.updateTable();
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
    }

    /**
     * Gets the main desktop.
     * 
     * @return desktop.
     */
    private MainDesktopPane getDesktop() {
        return desktop;
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
            if (stage.getRemainingResources().size() > 0) {
                result.append(Msg.BR).append("Remaining Construction Resources:").append(Msg.BR);
                Iterator<Integer> i = stage.getRemainingResources().keySet().iterator();
                while (i.hasNext()) {
                	Integer resource = i.next();
                    double amount = stage.getRemainingResources().get(resource);
                    result.append(Msg.NBSP).append(Msg.NBSP)
                    .append(ResourceUtil.findAmountResource(resource).getName()).append(": ").append(amount).append(" kg").append(Msg.BR);
                }
            }

            // Add remaining construction parts.
            if (stage.getRemainingParts().size() > 0) {
                result.append(Msg.BR).append("Remaining Construction Parts:").append(Msg.BR);
                Iterator<Integer> j = stage.getRemainingParts().keySet().iterator();
                while (j.hasNext()) {
                	Integer part = j.next();
                    int number = stage.getRemainingParts().get(part);
                    result.append(Msg.NBSP).append(Msg.NBSP)
                    .append(ItemResourceUtil.findItemResourceName(part)).append(": ").append(number).append(Msg.BR);
                }
            }

            // Add construction vehicles.
            if (info.getVehicles().size() > 0) {
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
     * Model for the remaining construction materials table.
     */
    private class RemainingMaterialsTableModel extends AbstractTableModel {

        /** default serial id. */
        private static final long serialVersionUID = 1L;

        // Data members.
        protected Map<Good, Integer> missingMap;
        protected Map<Good, Integer> originalMap;
        protected List<Good> goodsList;

        /**
         * Constructor.
         */
        private RemainingMaterialsTableModel() {
            // Use AbstractTableModel constructor.
            super();

            // Initialize goods map and list.
            goodsList = new ArrayList<>();
            originalMap = new HashMap<>();
            missingMap = new HashMap<>();
        }

        /**
         * Returns the number of rows in the model.
         * 
         * @return number of rows.
         */
        public int getRowCount() {
            return goodsList.size();
        }

        /**
         * Returns the number of columns in the model.
         * 
         * @return number of columns.
         */
        public int getColumnCount() {
            return 3;
        }

        /**
         * Returns the name of the column at columnIndex.
         * 
         * @param columnIndex the column index.
         * @return column name.
         */
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return Msg.getString("ConstructionMissionCustomInfoPanel.column.material"); //$NON-NLS-1$
            }
            else if (columnIndex == 1) {
                return Msg.getString("ConstructionMissionCustomInfoPanel.column.missing"); //$NON-NLS-1$
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

                // Add original parts.
                Iterator<Integer> j0 = stage.getOriginalParts().keySet().iterator();
                while (j0.hasNext()) {
                	Integer part = j0.next();
                    int num = stage.getOriginalParts().get(part);
                    originalMap.put(GoodsUtil.getGood(part), num);
                }

                goodsList = new ArrayList<>(originalMap.keySet());
                Collections.sort(goodsList);

                missingMap = new HashMap<>();
                
                // Add remaining resources.
                Iterator<Integer> i = stage.getRemainingResources().keySet().iterator();
                while (i.hasNext()) {
                	Integer resource = i.next();
                    double amount = stage.getRemainingResources().get(resource);
                    missingMap.put(GoodsUtil.getGood(resource), (int) amount);
                }

                // Add remaining parts.
                Iterator<Integer> j = stage.getRemainingParts().keySet().iterator();
                while (j.hasNext()) {
                	Integer part = j.next();
                    int num = stage.getRemainingParts().get(part);
                    missingMap.put(GoodsUtil.getGood(part), num);
                }
            }

            fireTableDataChanged();
        }
    }
}
