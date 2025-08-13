/*
 * Mars Simulation Project
 * BuildingPanelFarming.java
 * @date 2025-07-16
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.building.function.farming.Crop;
import com.mars_sim.core.building.function.farming.CropConfig;
import com.mars_sim.core.building.function.farming.CropSpec;
import com.mars_sim.core.building.function.farming.Farming;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.components.PercentageTableCellRenderer;
import com.mars_sim.ui.swing.tool.VerticalLabelUI;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;


/**
 * The BuildingPanelFarming class is a building function panel representing
 * the crop farm of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelFarming extends BuildingFunctionPanel {

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

	private static final DecimalFormat DECIMAL_KG_SOL = StyleManager.DECIMAL1_KG_SOL;
	private static final DecimalFormat DECIMAL_W_M2 = StyleManager.DECIMAL_W_M2;
	private static final DecimalFormat DECIMAL_KG2 = StyleManager.DECIMAL_KG2;
	
	// Data members	/** Is UI constructed. */
	private boolean uiDone = false;
	
	// Data cache
	private int rowCache;
	/** The number of farmers cache. */
	private int farmersCache;
	/** The number of crops cache. */
	private int cropsCache;
	/** The cache for the amount of solar irradiance. */
	private double radCache;
	/** The cache value for the work time done in this greenhouse. */
	private double workTimeCache;
	/** The cache value for the total growing area in this greenhouse. */
	private double totalAreaCache;
	/** The cache value for the remaining area in this greenhouse. */
	private double remainingAreaCache;
	/** The cache value for lighting (kW) in this greenhouse. */
	private double lightingCache;
	
	/** The cache value for the average water usage per sol. */
	private double waterPerSolCache;
	/** The cache value for the average grey water usage per sol. */
	private double greyWaterPerSolCache;
	/** The cache value for the average O2 generated per sol. */
	private double o2PersSolCache;
	/** The cache value for the average CO2 consumed per sol. */
	private double co2PerSolCache;
	
	/** The cache value for the cumulative water usage. */
	private double waterCumCache;
	/** The cache value for the average grey water usage. */
	private double greyWaterCumCache;
	/** The cache value for the average O2 generated. */
	private double o2CumCache;
	/** The cache value for the average CO2 consumed. */
	private double co2CumCache;
	
	private JLabel lightingLabel;
	private JLabel radLabel;
	private JLabel farmerLabel;
	private JLabel cropsLabel;
	private JLabel workTimeLabel;
	private JLabel areaUsageLabel;
	
	private JLabel waterPerSolLabel;
	private JLabel greyWaterPerSolLabel;
	private JLabel o2PerSolLabel;
	private JLabel co2PerSolLabel;

	private JLabel waterCumLabel;
	private JLabel greyWaterCumLabel;
	private JLabel o2CumLabel;
	private JLabel co2CumLabel;

	private JComboBox<CropSpec> comboBox;
	private ListModel listModel;
	/** Table model for crop info. */
	private CropTableModel cropTableModel;
	private JScrollPane listScrollPanel;

	/** The farming building. */
	private Farming farm;
	private Coordinates location;
	
	private Crop cropCache;

	private JList<String> list;

	private CropConfig cropConfig;
	private SurfaceFeatures surfaceFeatures;


	/**
	 * Constructor.
	 * @param farm {@link Farming} the farming building this panel is for.
	 * @param desktop {@link MainDesktopPane} The main desktop.
	 */
	public BuildingPanelFarming(final Farming farm, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelFarming.title"), 
			ImageLoader.getIconByName(PLANT_ICON), 
			farm.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.farm = farm;
		location = farm.getBuilding().getCoordinates();

		var sim = desktop.getSimulation();
		cropConfig = desktop.getSimulation().getConfig().getCropConfiguration();
	
		surfaceFeatures = sim.getSurfaceFeatures();
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create label panel
		AttributePanel springPanel = new AttributePanel(7, 2);
		center.add(springPanel, BorderLayout.CENTER);

		// Prepare solar irradiance label
		radCache = surfaceFeatures.getSolarIrradiance(location);
		radLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.solarIrradiance.title"),
				DECIMAL_W_M2.format(radCache), "Estimated sunlight on top of the greenhouse roof");

		// Prepare farmers label
		farmersCache = farm.getFarmerNum();
		farmerLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.numFarmers.title"),
				                 Integer.toString(farmersCache), "# of active gardeners tending the greenhouse");

		lightingCache = farm.getCombinedPowerLoad();
		lightingLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.lighting"),
				StyleManager.DECIMAL2_KW.format(lightingCache),
			 	Msg.getString("BuildingPanelFarming.lighting.tooltip"));
		
		// Prepare crops label
		cropsCache = farm.getCrops().size();
		cropsLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.numCrops.title"),
							   Integer.toString(cropsCache), null);

		// Calculate the area usage
		remainingAreaCache = farm.getRemainingArea();
		totalAreaCache = farm.getGrowingArea();
		areaUsageLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.areaUsage") ,
				StyleManager.DECIMAL1_PERC.format((totalAreaCache-remainingAreaCache)/totalAreaCache*100.0),
			 	Msg.getString("BuildingPanelFarming.areaUsage.tooltip"));
		

		// Compute the cumulative work time
		workTimeCache = farm.getCumulativeWorkTime()/1000.0;
		workTimeLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.workTime.title"),
									StyleManager.DECIMAL3_SOLS.format(workTimeCache),
									Msg.getString("BuildingPanelFarming.workTime.tooltip"));

		double[] water = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.WATER_ID);

		waterCumCache = water[0];
		waterCumLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.waterCum.title"),
									DECIMAL_KG2.format(waterCumCache),
									Msg.getString("BuildingPanelFarming.waterCum.tooltip"));
		waterPerSolCache = water[1];
		waterPerSolLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.waterDailyAve.title"),
									DECIMAL_KG_SOL.format(waterPerSolCache),
									Msg.getString("BuildingPanelFarming.waterDailyAve.tooltip"));
		
		double[] o2 = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.OXYGEN_ID);

		o2CumCache = o2[0];
		o2CumLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.o2Cum.title"),
									DECIMAL_KG2.format(-o2CumCache),
									Msg.getString("BuildingPanelFarming.o2Cum.tooltip"));
		
		o2PersSolCache = o2[1];
		o2PerSolLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.o2DailyAve.title"),
									DECIMAL_KG_SOL.format(-o2PersSolCache),
									Msg.getString("BuildingPanelFarming.o2DailyAve.tooltip"));

		double[] greyWater = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.GREY_WATER_ID);

		greyWaterCumCache = greyWater[0];
		greyWaterCumLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.greyWaterCum.title"),
									DECIMAL_KG2.format(greyWaterCumCache),
									Msg.getString("BuildingPanelFarming.greyWaterCum.tooltip"));
		
		greyWaterPerSolCache = greyWater[1];
		greyWaterPerSolLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.greyWaterDailyAve.title"),
									DECIMAL_KG_SOL.format(greyWaterPerSolCache),
									Msg.getString("BuildingPanelFarming.greyWaterDailyAve.tooltip"));

		double[] co2 = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.CO2_ID);

		co2CumCache = co2[0];
		co2CumLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.co2Cum.title"),
									DECIMAL_KG2.format(co2CumCache),
									Msg.getString("BuildingPanelFarming.co2Cum.tooltip"));
		
		co2PerSolCache = co2[1];
		co2PerSolLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.co2DailyAve.title"),
									DECIMAL_KG_SOL.format(co2PerSolCache),
								 	Msg.getString("BuildingPanelFarming.co2DailyAve.tooltip"));
		
		
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
	
		cropTable.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mousePressed(MouseEvent e) {
		    	java.awt.Point p = e.getPoint();
                int rowIndex = cropTable.rowAtPoint(p);
				RowSorter<? extends TableModel> sorter = cropTable.getRowSorter();
				if (sorter != null) {
					rowIndex = sorter.convertRowIndexToModel(rowIndex);
				}	
		    	
		        if (rowIndex >= 0 && rowIndex < cropTableModel.getRowCount()) {
		        	Crop crop = cropTableModel.getCrop(rowIndex);
		        	rowCache = rowIndex;
		        	cropCache = crop;
		        	cropTable.setRowSelectionInterval(rowIndex, rowIndex);
		        } else {
		        	cropTable.clearSelection();
		        	rowCache = -1;
		        	cropCache = null;
		        }
		    }
		});
		
		
		// Create a popup menu for the crop table
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem harvestItem = new JMenuItem("Early Harvest");
        harvestItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	JMenuItem item = (JMenuItem) e.getSource();
            	if (item == harvestItem) {
            		SwingUtilities.invokeLater(() -> {
                            int rowAtPoint = rowCache; 
                            if (rowAtPoint > -1) {
                            	cropTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
                            	Crop crop = cropCache;
                            	int reply = JOptionPane.showConfirmDialog(center, "Would you like to fast-track '" 
                            			+ crop.getName() + "' for an early harvest right now ? ", "Early Harvest", JOptionPane.YES_NO_OPTION);
                		        if (reply == JOptionPane.YES_OPTION) {
                		        	logger.info(building, 0, "Hand picked " + crop.getName() + " for an early harvest.");
                		        	crop.setToHarvest();
                		        }
                		        else {
                		        	logger.info(building, 0, "Not choosing " + crop.getName() + " for an early harvest.");
                		        }
                        }
                    });
            	}
            }
        });
        
        popupMenu.add(harvestItem); 
        cropTable.setComponentPopupMenu(popupMenu);
		
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
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		cropColumns.getColumn(CropTableModel.AREA).setCellRenderer(centerRenderer);
		cropColumns.getColumn(CropTableModel.WORK).setCellRenderer(centerRenderer);
		
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		cropColumns.getColumn(CropTableModel.HARVEST).setCellRenderer(rightRenderer);
		
		cropTable.setCellSelectionEnabled(false); // need it so that the tooltip can be displayed.
		
		cropTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		tableScrollPanel.setViewportView(cropTable);

		JPanel queuePanel = new JPanel(new BorderLayout());
	    southPanel.add(queuePanel, BorderLayout.CENTER);

	    JPanel selectPanel = new JPanel(new FlowLayout());
	    queuePanel.add(selectPanel, BorderLayout.NORTH); // 1st add

		JPanel buttonPanel = new JPanel(new BorderLayout());
		JButton addButton = new JButton(Msg.getString("BuildingPanelFarming.addButton")); //$NON-NLS-1$
		addButton.setPreferredSize(new Dimension(60, 20));
		addButton.setFont(new Font("Serif", Font.PLAIN, 9));
		addButton.addActionListener(s -> {
				CropSpec cs = (CropSpec) comboBox.getSelectedItem();
				farm.addCropListInQueue(cs.getName());
				listModel.update();
				repaint();
			});
		buttonPanel.add(addButton, BorderLayout.NORTH);
		selectPanel.add(buttonPanel);

		JButton delButton = new JButton(Msg.getString("BuildingPanelFarming.delButton")); //$NON-NLS-1$
		delButton.setPreferredSize(new Dimension(60, 20));
		delButton.setFont(new Font("Serif", Font.PLAIN, 9));

		delButton.addActionListener(s -> {
			if (!list.isSelectionEmpty()) {
				String deletingCropType = list.getSelectedValue();
				int deletingCropIndex = list.getSelectedIndex();
            	farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
				listModel.update();
            	repaint();
			}
		});
		buttonPanel.add(delButton, BorderLayout.CENTER);

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
	    selectPanel.add(comboBox);

		JPanel queueListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel queueButtonLabelPanel = new JPanel(new BorderLayout());
	    JLabel queueListLabel = new JLabel("     Crop Queue     ");
		queueListLabel.setUI(new VerticalLabelUI(false));
	    StyleManager.applySubHeading(queueListLabel);
		queueListLabel.setBorder(new MarsPanelBorder());
	    queueButtonLabelPanel.add(queueListLabel, BorderLayout.NORTH);
		queueListPanel.add(queueButtonLabelPanel);
	    queuePanel.add(queueListPanel, BorderLayout.CENTER);

		// Create scroll panel for population list.
		listScrollPanel = new JScrollPane();
		listScrollPanel.setPreferredSize(new Dimension(150, 150));

		// Create list model
		listModel = new ListModel();
		// Create list
		list = new JList<>(listModel);
		listScrollPanel.setViewportView(list);
		queueListPanel.add(listScrollPanel);
	}

	/**
	 * Mouse clicked event occurs.
	 * 
	 * @param event the mouse event
	 */
	
	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		// Update farmers label if necessary.
		int farmers = farm.getFarmerNum();
		if (farmersCache != farmers) {
			farmersCache = farmers;
			farmerLabel.setText(String.valueOf(farmers));
		}

		
		// Update crops label if necessary.
		int crops = farm.getCrops().size();
		if (cropsCache != crops) {
			cropsCache = crops;
			cropsLabel.setText(String.valueOf(crops));
		}

		// Update lighting label 
		double lighting = farm.getCombinedPowerLoad();
		if (Math.abs(lightingCache - lighting) > .04) {
			lightingCache = lighting;
			lightingLabel.setText(StyleManager.DECIMAL2_KW.format(lighting));
		}
		
		// Update solar irradiance label
		double rad = Math.round(surfaceFeatures.getSolarIrradiance(location)*10.0)/10.0;
		if (Math.abs(radCache - rad) > 1) {
			radCache = rad;
			radLabel.setText(DECIMAL_W_M2.format(rad));
		}

		
		// Update the water usage
		double[] water = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.WATER_ID);
	
		if (Math.abs(waterCumCache - water[0]) > .4) {
			waterCumCache = water[0];
			waterCumLabel.setText(DECIMAL_KG2.format(water[0]));
		}
		
		if (Math.abs(waterPerSolCache - water[1]) > .4) {
			waterPerSolCache = water[1];
			waterPerSolLabel.setText(DECIMAL_KG_SOL.format(water[1]));
		}

		
		// Update the O2 generated
		double[] o2 = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.OXYGEN_ID);
		
		if (Math.abs(o2CumCache - o2[0]) > .4) {
			o2CumCache = o2[0];
			o2CumLabel.setText(DECIMAL_KG2.format(-o2[0]));
		}
		
		if (Math.abs(o2PersSolCache - o2[1]) > .4) {
			o2PersSolCache = o2[1];
			o2PerSolLabel.setText(DECIMAL_KG_SOL.format(-o2[1]));
		}
		
		
		// Update the CO2 consumed
		double[] co2 = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.CO2_ID);
		
		if (Math.abs(co2CumCache - co2[0]) > .4) {
			co2CumCache = co2[0];
			co2CumLabel.setText(DECIMAL_KG2.format(co2[0]));
		}
		
		if (Math.abs(co2PerSolCache - co2[1]) > .4) {
			co2PerSolCache = co2[1];
			co2PerSolLabel.setText(DECIMAL_KG_SOL.format(co2[1]));
		}

		
		// Update the grey water usage
		double[] greyWater = farm.computeAllCropsCumulativeDailyAverage(ResourceUtil.GREY_WATER_ID);
		
		if (Math.abs(greyWaterCumCache - greyWater[0]) > .4) {
			greyWaterCumCache = greyWater[0];
			greyWaterCumLabel.setText(DECIMAL_KG2.format(greyWater[0]));
		}
		
		if (Math.abs(greyWaterPerSolCache - greyWater[1]) > .4) {
			greyWaterPerSolCache = greyWater[1];
			greyWaterPerSolLabel.setText(DECIMAL_KG_SOL.format(greyWater[1]));
		}
		
		
		// Update the cumulative work time
		double workTime = farm.getCumulativeWorkTime()/1000.0;
		if (Math.abs(workTimeCache - workTime) > .4) {
			workTimeCache = workTime;
			workTimeLabel.setText(StyleManager.DECIMAL3_SOLS.format(workTime));
		}
		
		
		// Update the area usage
		double remainingArea = farm.getRemainingArea();
		double totalArea = farm.getGrowingArea();
		if (Math.abs(remainingAreaCache - remainingArea) > .4 || Math.abs(totalAreaCache - totalArea) > .4) {
			remainingAreaCache = remainingArea;
			totalAreaCache = totalArea;
			areaUsageLabel.setText(StyleManager.DECIMAL1_PERC.format((totalArea-remainingArea)/totalArea*100.0));
		}
		
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
			crops = farm.getCrops();
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
			if (!crops.equals(farm.getCrops()))
				crops = farm.getCrops();
			fireTableDataChanged();
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

	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		radLabel = null;
		farmerLabel = null;
		cropsLabel = null;
		waterPerSolLabel = null;
		greyWaterPerSolLabel = null;
		o2PerSolLabel = null;
		co2PerSolLabel = null;
		workTimeLabel = null;
		areaUsageLabel = null;
	
		comboBox = null;
		listModel = null;
		cropTableModel = null;
		listScrollPanel = null;
		farm = null;
		location = null;
		list = null;
		cropConfig = null;
		surfaceFeatures = null;
	}
}
