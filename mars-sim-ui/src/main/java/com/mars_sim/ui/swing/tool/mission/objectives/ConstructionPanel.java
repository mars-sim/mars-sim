/*
 * Mars Simulation Project
 * ConstructionPanel.java
 * @date 2023-07-24
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.objectives;

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
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.mission.objectives.ConstructionObjective;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.person.ai.mission.MissionListener;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.tool.mission.ObjectivesPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ConstructionStageFormat;

/**
 * A panel for displaying construction custom mission information.
 */
@SuppressWarnings("serial")
public class ConstructionPanel extends JPanel implements MissionListener, ObjectivesPanel, UnitListener {

    private JPanel processPanel;
    private BoundedRangeModel progressBarModel;
    private MaterialsTableModel materialsTableModel;
    private JScrollPane scrollPane;
    private ConstructionObjective objective;

    /**
     * Constructor.
     * 
     * @param desktop the main desktop panel.
     */
    public ConstructionPanel(ConstructionObjective objective, MainDesktopPane desktop) {
        // Use MissionCustomInfoPanel constructor.
        super();
        setName(objective.getName());
        this.objective = objective;

        // Set layout.
        setLayout(new BorderLayout());

        JPanel contentsPanel = new JPanel(new BorderLayout(5, 5));
        add(contentsPanel, BorderLayout.NORTH);

        // Prepare SpringLayout for info panel.
     	AttributePanel infoPanel = new AttributePanel();
     	contentsPanel.add(infoPanel, BorderLayout.NORTH);

        var site = objective.getSite();

        String siteLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.titleLabel"); //$NON-NLS-1$
        infoPanel.addLabelledItem(siteLabelString, new EntityLabel(site, desktop));

        var stage = objective.getStage();
        String stageLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.stageLabel"); //$NON-NLS-1$
        infoPanel.addTextField(stageLabelString, stage.getInfo().getName(), null);
        
        // Process panel    
        processPanel = new JPanel(new GridLayout(2, 1));
        contentsPanel.add(processPanel,  BorderLayout.CENTER);
          
        JPanel progressBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        processPanel.add(progressBarPanel);

        // Add tooltip.
        processPanel.setToolTipText(ConstructionStageFormat.getTooltip(stage, true));
        
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


        site.addUnitListener(this);    
        
        updateProgressBar();

        // Update remaining construction materials table.
        materialsTableModel.updateTable();
    }

    @Override
    public void missionUpdate(MissionEvent event) {
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
     * Updates the progress bar.
     */
    private void updateProgressBar() {
        int workProgress = 0;
        ConstructionStage stage = objective.getStage();
        if (stage != null) {
            double completedWork = stage.getCompletedWorkTime();
            double requiredWork = stage.getRequiredWorkTime();
            if (requiredWork > 0D) {
                workProgress = (int) (100D * completedWork / requiredWork);
            }
        }

        progressBarModel.setValue(workProgress);
        
        // Update the tool tip string.
        processPanel.setToolTipText(ConstructionStageFormat.getTooltip(stage, true));
    }

  

    /**
     * Model for the construction materials table.
     */
    private final class MaterialsTableModel extends AbstractTableModel {

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
            return switch (columnIndex) {
              case 0 -> Msg.getString("ConstructionMissionCustomInfoPanel.column.material");
              case 1 -> Msg.getString("ConstructionMissionCustomInfoPanel.column.missing");
              case 2 -> Msg.getString("ConstructionMissionCustomInfoPanel.column.available");
              default -> Msg.getString("ConstructionMissionCustomInfoPanel.column.original");
            };
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
            if (row < goodsList.size()) {
                Good good = goodsList.get(row);
                return switch (column) {
                  case 0 ->  good.getName();
                  case 1 -> missingMap.get(good);
                  case 2 -> availableMap.get(good);
                  default -> originalMap.get(good);
                };
            }

            return null;
        }

        /**
         * Updates the table data.
         */
        protected void updateTable() {
        	
            // Populate originalMap.
            ConstructionStage stage = objective.getStage();
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

    /**
     * Stop listening to event on the Site
     */
    @Override
    public void unregister() {
        objective.getSite().addUnitListener(this);    
    }
}
