/*
 * Mars Simulation Project
 * BuildingPanelFarming.java
 * @date 2025-08-13
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.farming.Crop;
import com.mars_sim.core.building.function.farming.CropConfig;
import com.mars_sim.core.building.function.farming.CropSpec;
import com.mars_sim.core.building.function.farming.Farming;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.components.PercentageTableCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;


/**
 * The BuildingPanelFarming class is a building function panel representing
 * the crop farm of a settlement building.
 */
@SuppressWarnings("serial")
class BuildingPanelFarming extends EntityTabPanel<Building> 
	implements TemporalComponent {

	/** Default logger. */
	private static SimLogger logger = SimLogger.getLogger(BuildingPanelFarming.class.getName());

	private static final String PLANT_ICON = "plant";
	private static final String KG_M2 = " kg/m\u00b2";
	private static final String HTML = "<html>";
	private static final String END_HTML = "</html>";
	private static final String CROP_NAME = "&emsp;&nbsp;Crop Name:&emsp;";
	private static final String CATEGORY = "<br>&emsp;&emsp;&nbsp;&nbsp;Category:&emsp;";
	private static final String GROWING_DAYS = "<br>&nbsp;Growing Days:&emsp;";
	private static final String EDIBLE_MASS = "<br>&emsp;Edible Mass:&emsp;";
	private static final String INEDIBLE_MASS = "<br>&nbsp;Inedible Mass:&emsp;";
	private static final String WATER_CONTENT = "<br>&nbsp;Water Content:&emsp;";
	private static final String PERCENT = " %";
	private static final String PAR_REQUIRED = "<br>&nbsp;&nbsp;PAR required:&emsp;";
	private static final String MOL_M2_DAY = " mol/m\u00b2/day";
	
	private JDoubleLabel lightingLabel;
	private JDoubleLabel radLabel;
	private JIntegerLabel farmerLabel;
	private JIntegerLabel cropsLabel;
	private JDoubleLabel workTimeLabel;
	private JDoubleLabel areaUsageLabel;
	
	private JDoubleLabel waterPerSolLabel;
	private JDoubleLabel greyWaterPerSolLabel;
	private JDoubleLabel o2PerSolLabel;
	private JDoubleLabel co2PerSolLabel;
	private JDoubleLabel soilPerSolLabel;
	private JDoubleLabel cropWastePerSolLabel;
	private JDoubleLabel leavesPerSolLabel;
	
	private JDoubleLabel waterCumLabel;
	private JDoubleLabel greyWaterCumLabel;
	private JDoubleLabel o2CumLabel;
	private JDoubleLabel co2CumLabel;
	private JDoubleLabel soilCumLabel;
	private JDoubleLabel cropWasteCumLabel;
	private JDoubleLabel leavesCumLabel;
	
	
	private JComboBox<CropSpec> comboBox;
	private ListModel listModel;
	/** Table model for crop info. */
	private CropTableModel cropTableModel;

	/** The farming building. */
	private Farming farm;
	private Coordinates location;

	private JList<String> list;

	private CropConfig cropConfig;

	private SurfaceFeatures surfaceFeatures;


	/**
	 * Constructor.
	 * @param farm {@link Farming} the farming building this panel is for.
	 * @param context {@link UIContext} The UI context.
	 */
	public BuildingPanelFarming(final Farming farm, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelFarming.title"), 
			ImageLoader.getIconByName(PLANT_ICON), null,
			context, farm.getBuilding()
		);

		// Initialize data members
		this.farm = farm;
		location = farm.getBuilding().getCoordinates();

		var sim = context.getSimulation();
		cropConfig = sim.getConfig().getCropConfiguration();
	
		surfaceFeatures = sim.getSurfaceFeatures();
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create label panel
		AttributePanel springPanel = new AttributePanel(10, 2);
		center.add(springPanel, BorderLayout.CENTER);

		// Prepare solar irradiance label
		radLabel = new JDoubleLabel(StyleManager.DECIMAL_W_M2, surfaceFeatures.getSolarIrradiance(location));
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.solarIrradiance.title"), radLabel, "Estimated sunlight on top of the greenhouse roof");

		// Prepare farmers label
		farmerLabel = new JIntegerLabel(farm.getFarmerNum());
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.numFarmers.title"), farmerLabel, "# of active gardeners tending the greenhouse");
		lightingLabel = new JDoubleLabel(StyleManager.DECIMAL2_KW, farm.getCombinedPowerLoad());
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.lighting"), lightingLabel,
			 	Msg.getString("BuildingPanelFarming.lighting.tooltip"));
		
		// Prepare crops label
		cropsLabel = new JIntegerLabel(farm.getCrops().size());
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.numCrops.title"), cropsLabel, null);
		// Calculate the area usage
		var remainingAreaCache = farm.getRemainingArea();
		var totalAreaCache = farm.getGrowingArea();
		areaUsageLabel = new JDoubleLabel(StyleManager.DECIMAL1_PERC, (totalAreaCache-remainingAreaCache)/totalAreaCache*100.0, 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.areaUsage") , areaUsageLabel,
			 						Msg.getString("BuildingPanelFarming.areaUsage.tooltip"));
		

		// Compute the cumulative work time
		workTimeLabel = new JDoubleLabel(StyleManager.DECIMAL3_SOLS, farm.getCumulativeWorkTime()/1000.0, 0.001);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.workTime.title"), workTimeLabel,
									Msg.getString("BuildingPanelFarming.workTime.tooltip"));

		double[] water = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.WATER_ID);
		waterCumLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, water[0]);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.waterCum.title"), waterCumLabel,
								Msg.getString("BuildingPanelFarming.waterCum.tooltip"));
		waterPerSolLabel = new JDoubleLabel(StyleManager.DECIMAL2_KG_SOL, water[1], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.waterDailyAve.title"), waterPerSolLabel,
									Msg.getString("BuildingPanelFarming.waterDailyAve.tooltip"));
		
		double[] o2 = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.OXYGEN_ID);
		o2CumLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, o2[0], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.o2Cum.title"), o2CumLabel,
								Msg.getString("BuildingPanelFarming.o2Cum.tooltip"));
		o2PerSolLabel = new JDoubleLabel(StyleManager.DECIMAL2_KG_SOL, o2[1], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.o2DailyAve.title"), o2PerSolLabel,
											Msg.getString("BuildingPanelFarming.o2DailyAve.tooltip"));

		double[] greyWater = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.GREY_WATER_ID);
		greyWaterCumLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, greyWater[0], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.greyWaterCum.title"), greyWaterCumLabel,
								Msg.getString("BuildingPanelFarming.greyWaterCum.tooltip"));
		greyWaterPerSolLabel = new JDoubleLabel(StyleManager.DECIMAL2_KG_SOL, greyWater[1], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.greyWaterDailyAve.title"), greyWaterPerSolLabel,
											Msg.getString("BuildingPanelFarming.greyWaterDailyAve.tooltip"));

		double[] co2 = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.CO2_ID);
		co2CumLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, co2[0], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.co2Cum.title"), co2CumLabel,
								Msg.getString("BuildingPanelFarming.co2Cum.tooltip"));
		co2PerSolLabel = new JDoubleLabel(StyleManager.DECIMAL2_KG_SOL, co2[1], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.co2DailyAve.title"), co2PerSolLabel,
								Msg.getString("BuildingPanelFarming.co2DailyAve.tooltip"));

		double[] soil = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.SOIL_ID);
		soilCumLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, soil[0], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.soilCum.title"), soilCumLabel,
								Msg.getString("BuildingPanelFarming.soilCum.tooltip"));
		soilPerSolLabel = new JDoubleLabel(StyleManager.DECIMAL2_KG_SOL, soil[1], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.soilDailyAve.title"), soilPerSolLabel,
											Msg.getString("BuildingPanelFarming.soilDailyAve.tooltip"));

		double[] cropWaste = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.CROP_WASTE_ID);
		cropWasteCumLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, cropWaste[0], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.cropWasteCum.title"), cropWasteCumLabel,
								Msg.getString("BuildingPanelFarming.cropWasteCum.tooltip"));
		cropWastePerSolLabel = new JDoubleLabel(StyleManager.DECIMAL2_KG_SOL, cropWaste[1], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.cropWasteDailyAve.title"), cropWastePerSolLabel,
								Msg.getString("BuildingPanelFarming.cropWasteDailyAve.tooltip"));

		double[] leaves = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.LEAVES_ID);
		leavesCumLabel = new JDoubleLabel(StyleManager.DECIMAL_KG2, leaves[0], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.leavesCum.title"), leavesCumLabel,
								Msg.getString("BuildingPanelFarming.leavesCum.tooltip"));
		leavesPerSolLabel = new JDoubleLabel(StyleManager.DECIMAL2_KG_SOL, leaves[1], 0.01);
		springPanel.addLabelledItem(Msg.getString("BuildingPanelFarming.leavesDailyAve.title"), leavesPerSolLabel,
											Msg.getString("BuildingPanelFarming.leavesDailyAve.tooltip"));

		JPanel southPanel = new JPanel(new BorderLayout());
		center.add(southPanel, BorderLayout.SOUTH);
		
		// Create scroll panel for crop table
		JScrollPane tableScrollPanel = new JScrollPane();
		// Set the height and width of the table
		tableScrollPanel.setPreferredSize(new Dimension(200, 290)); // 290 is the best fit for 10 crops

		southPanel.add(tableScrollPanel, BorderLayout.NORTH);

		// Prepare crop table model
		cropTableModel = new CropTableModel(farm);

		// Prepare crop table
		JTable cropTable = new JTable(cropTableModel) {
			@Override
            public String getToolTipText(MouseEvent e) {
				return ToolTipTableModel.extractToolTip(e, this);
            }
        };
		cropTable.setAutoCreateRowSorter(true);
		
		// Create a popup menu for the crop table
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem harvestItem = new JMenuItem("Early Harvest");
        harvestItem.addActionListener(e -> {
			int rowAtPoint = cropTable.getSelectedRow();
			if (rowAtPoint > -1) {
				rowAtPoint = cropTable.convertRowIndexToModel(rowAtPoint);
				Crop crop = cropTableModel.getCrop(rowAtPoint);
				int reply = JOptionPane.showConfirmDialog(center, "Would you like to fast-track '" 
						+ crop.getName() + "' for an early harvest right now ? ", "Early Harvest", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION) {
					logger.info(getEntity(), 0, "Hand picked " + crop.getName() + " for an early harvest.");
					crop.setToHarvest();
				}
			}
		});
        
        popupMenu.add(harvestItem); 
        cropTable.addMouseListener( new MouseAdapter() {
			@Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    JTable source = (JTable)e.getSource();
                    int row = source.rowAtPoint( e.getPoint() );
                    int column = source.columnAtPoint( e.getPoint() );

                    if (!source.isRowSelected(row))
                        source.changeSelection(row, column, false, false);

                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
		
		TableColumnModel cropColumns = cropTable.getColumnModel();
		cropColumns.getColumn(CropTableModel.HEALTH).setPreferredWidth(10);
		cropColumns.getColumn(CropTableModel.NAME).setPreferredWidth(80);
		cropColumns.getColumn(CropTableModel.PHASE).setPreferredWidth(110);
		cropColumns.getColumn(CropTableModel.AREA).setPreferredWidth(50);
		cropColumns.getColumn(CropTableModel.AREA).setCellRenderer(new NumberCellRenderer());
		cropColumns.getColumn(CropTableModel.GROWTH).setPreferredWidth(55);
		cropColumns.getColumn(CropTableModel.GROWTH).setCellRenderer(new PercentageTableCellRenderer(true));
		cropColumns.getColumn(CropTableModel.WORK).setPreferredWidth(50);
		cropColumns.getColumn(CropTableModel.WORK).setCellRenderer(new NumberCellRenderer());
		cropColumns.getColumn(CropTableModel.HARVEST).setPreferredWidth(65);
	
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		cropColumns.getColumn(CropTableModel.AREA).setCellRenderer(centerRenderer);
		cropColumns.getColumn(CropTableModel.WORK).setCellRenderer(centerRenderer);
		
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		cropColumns.getColumn(CropTableModel.HARVEST).setCellRenderer(rightRenderer);
		
		cropTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		tableScrollPanel.setViewportView(cropTable);

		JPanel queuePanel = new JPanel(new BorderLayout());
		queuePanel.setBorder(SwingHelper.createLabelBorder("Crop Queue"));
	    southPanel.add(queuePanel, BorderLayout.CENTER);

		JButton addButton = new JButton(ImageLoader.getIconByName("action/add"));
		addButton.addActionListener(s -> {
				CropSpec cs = (CropSpec) comboBox.getSelectedItem();
				farm.addCropListInQueue(cs.getName());
				listModel.update();
			});

		JButton delButton = new JButton(ImageLoader.getIconByName("action/delete"));
		delButton.addActionListener(s -> {
			if (!list.isSelectionEmpty()) {
				String deletingCropType = list.getSelectedValue();
				int deletingCropIndex = list.getSelectedIndex();
            	farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
				listModel.update();
			}
		});

       	// Set up crop combo box model.
		DefaultComboBoxModel<CropSpec> comboBoxModel = new DefaultComboBoxModel<>();
		for(CropSpec sp : cropConfig.getCropTypes()) {
	    	comboBoxModel.addElement(sp);
		}

		// Create comboBox.
		comboBox = new JComboBox<>(comboBoxModel);

		// Use tooltip to display the crop parameters for each crop in the combobox
	    ComboboxToolTipRenderer toolTipRenderer = new ComboboxToolTipRenderer();
	    comboBox.setRenderer(toolTipRenderer);
		comboBox.setMaximumRowCount(10);

	    JPanel selectPanel = new JPanel(new FlowLayout());
	    queuePanel.add(selectPanel, BorderLayout.NORTH); // 1st add
	    selectPanel.add(comboBox);
		selectPanel.add(addButton);
		selectPanel.add(delButton);

		// Create scroll panel for population list.
		var listScrollPanel = new JScrollPane();
		listScrollPanel.setPreferredSize(new Dimension(150, 100));

		// Create list model
		listModel = new ListModel();
		// Create list
		list = new JList<>(listModel);
		listScrollPanel.setViewportView(list);
		queuePanel.add(listScrollPanel, BorderLayout.CENTER);
	}

	/**
	 * Updates this panel on clock pulse.
	 * Ideally could be converted to event driven update later.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		
		// Update farmers label if necessary.
		farmerLabel.setValue(farm.getFarmerNum());
		
		// Update crops label if necessary.
		cropsLabel.setValue(farm.getCrops().size());

		// Update lighting label 
		lightingLabel.setValue(farm.getCombinedPowerLoad());
		
		// Update solar irradiance label
		radLabel.setValue(surfaceFeatures.getSolarIrradiance(location));
		
		// Update the water usage
		double[] water = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.WATER_ID);
	
		waterCumLabel.setValue(water[0]);
		waterPerSolLabel.setValue(water[1]);

		// Update the O2 generated
		double[] o2 = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.OXYGEN_ID);		
		o2CumLabel.setValue(o2[0]);
		o2PerSolLabel.setValue(o2[1]);
		
		// Update the CO2 consumed
		double[] co2 = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.CO2_ID);		
		co2CumLabel.setValue(co2[0]);
		co2PerSolLabel.setValue(co2[1]);
		
		// Update the grey water usage
		double[] greyWater = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.GREY_WATER_ID);
		greyWaterCumLabel.setValue(greyWater[0]);
		greyWaterPerSolLabel.setValue(greyWater[1]);
		
		// Update the soil generated
		double[] soil = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.SOIL_ID);
		soilCumLabel.setValue(soil[0]);
		soilPerSolLabel.setValue(soil[1]);
		
		// Update the crop waste generation
		double[] cropWaste = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.CROP_WASTE_ID);
		cropWasteCumLabel.setValue(cropWaste[0]);
		cropWastePerSolLabel.setValue(cropWaste[1]);
		
		// Update the leaves generation
		double[] leaves = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.LEAVES_ID);
		leavesCumLabel.setValue(leaves[0]);
		leavesPerSolLabel.setValue(leaves[1]);
		
		// Update the cumulative work time
		workTimeLabel.setValue(farm.getCumulativeWorkTime()/1000.0);
		
		// Update the area usage
		double remainingArea = farm.getRemainingArea();
		double totalArea = farm.getGrowingArea();
		areaUsageLabel.setValue((totalArea-remainingArea)/totalArea*100.0);
		
		// Update crop table.
		cropTableModel.update();

		// Update list
		listModel.update();
	}

	
	/**
	 * Generates a tool tip describing a Crop Spec.
	 * 
	 * @param cs
	 * @return
	 */
	private static String generateCropSpecTip(CropSpec cs) {
		StringBuilder result = new StringBuilder();
		result.append(HTML)
			.append(CROP_NAME).append(cs.getName())
			.append(CATEGORY).append(cs.getCropCategory().getName())
			.append(GROWING_DAYS).append(cs.getGrowingSols())
			.append(EDIBLE_MASS).append(cs.getEdibleBiomass()).append(KG_M2)
			.append(INEDIBLE_MASS).append(cs.getInedibleBiomass()).append(KG_M2)
			.append(WATER_CONTENT).append(100 * cs.getEdibleWaterContent()).append(PERCENT)
			.append(PAR_REQUIRED).append(cs.getDailyPAR()).append(MOL_M2_DAY)
			.append(END_HTML);
		
			return result.toString();
	}

	/**
	 * List model for the crops in queue.
	 */
	private class ListModel extends AbstractListModel<String> {

	    private List<String> list;

	    private ListModel() {

        	List<String> c = farm.getCropListInQueue();
	        if (c != null)
	        	list = new ArrayList<>(c);
	        else
	        	list = null;
	    }

        @Override
        public String getElementAt(int index) {
        	String result = null;

            if ((index >= 0) && (index < list.size())) {
                result = list.get(index);
            }

            return result;
        }

        @Override
        public int getSize() {
        	if (list == null)
        		return 0;
        	return list.size();
        }

        /**
         * Updates the list model.
         */
        public void update() {

        	List<String> c = farm.getCropListInQueue();
        		// if the list contains duplicate items, it somehow pass this test
        		if (list.size() != c.size() || !list.containsAll(c) || !c.containsAll(list)) {
	                List<String> oldList = list;
	                List<String> tempList = new ArrayList<>(c);
	 
	                list = tempList;
	                fireContentsChanged(this, 0, getSize());

	                oldList.clear();
	           }

        }
	}

	/**
	 * Internal class used as model for the crop table.
	 */
	private static class CropTableModel extends AbstractTableModel
				implements ToolTipTableModel {

		private static final int HEALTH = 0;
		private static final int NAME = 1;
		private static final int PHASE = 2;
		private static final int AREA = 3;
		private static final int GROWTH = 4;
		private static final int WORK = 5;
		private static final int HARVEST = 6;


		private Farming farm;
		private List<Crop> crops;	
		private static Icon redHalfDot;
		private static Icon redOneQuarterDot;
		private static Icon yellowHalfDot;
		private static Icon greenDot;
		private static Icon greenThreeQuarterDot;
		private static Icon greenHalfDot;

		private CropTableModel(Farming farm) {
			this.farm = farm;
			crops = new ArrayList<>(farm.getCrops());
			loadIcons();
		}

		private static void loadIcons() {
			if (redHalfDot != null)
				return;
				
			redHalfDot = ImageLoader.getIconByName("dot/red_half");
			redOneQuarterDot = ImageLoader.getIconByName("dot/red_one_quarter");
			yellowHalfDot = ImageLoader.getIconByName("dot/yellow_half");
			greenHalfDot = ImageLoader.getIconByName("dot/green_half");
			greenThreeQuarterDot = ImageLoader.getIconByName("dot/green_three_quarter");
			greenDot = ImageLoader.getIconByName("dot/green");
		}

		public Crop getCrop(int rowIndex) {
			return crops.get(rowIndex);
		}

		@Override
		public int getRowCount() {
			return crops.size();
		}

		@Override
		public int getColumnCount() {
			return HARVEST + 1;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch(columnIndex) {
				case HEALTH -> Icon.class;
				case NAME, PHASE, HARVEST -> String.class;
				case AREA, GROWTH, WORK -> Double.class;
				default -> null;
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case HEALTH -> "Health";
				case NAME -> "Name";
				case PHASE -> "Phase";
				case GROWTH -> "Growth";
				case AREA -> "Area";
				case WORK -> "Score";
				case HARVEST -> "Harvest";

				default -> null;
			};
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			int num = crops.size();
			if (row >= num)
				return null;
			
			Crop crop = crops.get(row);
			var currentPhase = crop.getPhase();

			switch(column) {
				case HEALTH:
					double condition = crop.getHealthCondition();
					if (condition > .95) return greenDot;
					else if (condition > .75) return greenThreeQuarterDot;
					else if (condition > .6) return greenHalfDot;
					else if (condition > .45 ) return yellowHalfDot;
					else if (condition > .3 ) return redHalfDot;
					else return redOneQuarterDot;
				case NAME:
					return crop.getName();
				case PHASE:
					return currentPhase.getName();
				case GROWTH: 
					return Math.round(crop.getPercentGrowth() * 10.0)/10.0;
				case AREA: 
					return Math.round(crop.getGrowingArea() * 10.0)/10.0;
				case WORK:
					return Math.round(crop.getTendingScore() * 10.0)/10.0;
				case HARVEST:
					var harvest = crop.getHarvest();
					return String.format("%.1f/%.0f", harvest.value(), harvest.max());
				default:
					return null;
				}
		}

		public void update() {
			if (!crops.equals(farm.getCrops())) {
			 	crops = new ArrayList<>(farm.getCrops());
				fireTableDataChanged();
			}

			fireTableRowsUpdated(0, crops.size() - 1);
		}

		@Override
		public String getToolTipAt(int row, int col) {
			Crop crop = crops.get(row);
			
			if (col == 0) {
				return Math.round(crop.getHealthCondition() * 1000.0)/10.0 + " %";
			}
			else if (col == 1) {
				return generateCropSpecTip(crop.getCropSpec());
			}
			
			double sols = Math.round(crop.getGrowingTimeCompleted()/1_000.0 *10.0)/10.0;
			return "# of sols since planted: " + sols;
		}
	}

	class ComboboxToolTipRenderer extends DefaultListCellRenderer {
	    @Override
	    public Component getListCellRendererComponent(JList<?> list, Object value,
	                        int index, boolean isSelected, boolean cellHasFocus) {

	    	JComponent comp = (JComponent) super.getListCellRendererComponent(list,
	                value, index, isSelected, cellHasFocus);

	        if (value instanceof CropSpec cs) {

	        	list.setToolTipText(generateCropSpecTip(cs));
	        }
	        return comp;
	    }
	}
}
