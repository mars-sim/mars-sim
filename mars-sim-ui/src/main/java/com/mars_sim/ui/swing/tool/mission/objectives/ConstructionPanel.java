/*
 * Mars Simulation Project
 * ConstructionPanel.java
 * @date 2023-07-24
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.objectives;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
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

    private MaterialsTableModel materialsTableModel;
    private JScrollPane scrollPane;
    private ConstructionObjective objective;
    private JLabel stageLabel;
    private JLabel workRemaining;

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

        AttributePanel infoPanel = new AttributePanel();
        add(infoPanel, BorderLayout.NORTH);

        var site = objective.getSite();

        String siteLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.titleLabel"); //-NLS-1$
        infoPanel.addLabelledItem(siteLabelString, new EntityLabel(site, desktop));

        var stage = objective.getStage();
        String stageLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.stageLabel"); //-NLS-1$
        stageLabel = infoPanel.addTextField(stageLabelString, stage.getInfo().getName(),
                        ConstructionStageFormat.getTooltip(stage, true));
        infoPanel.addTextField("Work Type", site.isConstruction() ? "Build" : "Salvage", null);

        workRemaining = infoPanel.addTextField("Work Remaining", "",  null);
         
        if (objective.getStage().isConstruction()) {
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
            materialsTable.setRowSelectionAllowed(true);

            // Create a scroll pane for the remaining construction materials table.
            scrollPane = new JScrollPane();
            remainingMaterialsLabelPane.add(scrollPane);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setPreferredSize(new Dimension(-1, 100));  
            scrollPane.setViewportView(materialsTable);

            // Update remaining construction materials table.
            materialsTableModel.updateTable();
        }

        site.addUnitListener(this);    
        
        updateProgressBar();
    }

    @Override
    public void missionUpdate(MissionEvent event) {
        if (materialsTableModel != null) {
            materialsTableModel.updateTable();
        }
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
        else if (UnitEventType.ADD_CONSTRUCTION_MATERIALS_EVENT == event.getType()
                && materialsTableModel != null) {
            // Update remaining construction materials table.
            materialsTableModel.updateTable();
        }
    }

    /**
     * Updates the progress bar.
     */
    private void updateProgressBar() {
        ConstructionStage stage = objective.getStage();
        if (stage != null) {
            double workLeft = stage.getRequiredWorkTime() - stage.getCompletedWorkTime();
            workRemaining.setText(StyleManager.DECIMAL_MSOL.format(workLeft));
        
            // Update the tool tip string.
            stageLabel.setToolTipText(ConstructionStageFormat.getTooltip(stage, true));
        }
    }

  

    /**
     * Model for the construction materials table.
     */
    private final class MaterialsTableModel extends AbstractTableModel {

        /** default serial id. */
        private static final long serialVersionUID = 1L;

        // Data members.
        private Map<Good, Integer> missingMap;
        private Map<Good, Integer> availableMap;
        private Map<Good, Integer> originalMap;
        private List<Good> goodsList;

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
            return 5;
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
              case 1 -> Msg.getString("ConstructionMissionCustomInfoPanel.column.type");
              case 2 -> Msg.getString("ConstructionMissionCustomInfoPanel.column.missing");
              case 3 -> Msg.getString("ConstructionMissionCustomInfoPanel.column.available");
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
                  case 1 ->  good.getCategory().getName();
                  case 2 -> missingMap.get(good);
                  case 3 -> availableMap.get(good);
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
                stage.getOriginalResources().entrySet().forEach(i0 -> 
                        originalMap.put(GoodsUtil.getGood(i0.getKey()), (int) i0.getValue().doubleValue()));
                stage.getOriginalParts().entrySet().forEach(j0 ->
                        originalMap.put(GoodsUtil.getGood(j0.getKey()), j0.getValue()));

                if (!originalMap.isEmpty())
                	scrollPane.setPreferredSize(new Dimension(-1, originalMap.size() * 40));

                goodsList = new ArrayList<>(originalMap.keySet());
                Collections.sort(goodsList);

                // Add available resources.
                availableMap = new HashMap<>();
                stage.getAvailableResources().entrySet().forEach(i1 ->
                    availableMap.put(GoodsUtil.getGood(i1.getKey()), (int) i1.getValue().doubleValue()));
                stage.getAvailableParts().entrySet().forEach(j1 ->
                    availableMap.put(GoodsUtil.getGood(j1.getKey()), j1.getValue()));
        
                // Add missing resources.
                missingMap = new HashMap<>();
                stage.getMissingResources().entrySet().forEach(i2 ->
                    missingMap.put(GoodsUtil.getGood(i2.getKey()), (int) i2.getValue().doubleValue()));
                stage.getMissingParts().entrySet().forEach(j2 ->
                    missingMap.put(GoodsUtil.getGood(j2.getKey()), j2.getValue()));
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
