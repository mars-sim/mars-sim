/*
 * Mars Simulation Project
 * BuildingPanelFarming.java
 * @date 2023-08-25
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
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.building.function.farming.Crop;
import com.mars_sim.core.structure.building.function.farming.CropConfig;
import com.mars_sim.core.structure.building.function.farming.CropSpec;
import com.mars_sim.core.structure.building.function.farming.Farming;
import com.mars_sim.core.structure.building.function.farming.PhaseType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.NumberCellRenderer;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.VerticalLabelUI;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.PercentageCellRenderer;


/**
 * The BuildingPanelFarming class is a building function panel representing
 * the crop farm of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelFarming extends BuildingFunctionPanel {

	/** Default logger. */
	private static SimLogger logger = SimLogger.getLogger(BuildingPanelFarming.class.getName());

	private static final String PLANT_ICON = "plant";
	private static final String G_M2_DAY = " g/m\u00b2/day";
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

	private static final DecimalFormat DECIMAL_KG_SOL = new DecimalFormat("#,##0.0 kg/sol");
	private static final DecimalFormat DECIMAL_W_M2 = new DecimalFormat("#,##0.0 W/m\u00b2");
	
	
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
	/** The cache value for the average water usage per sol per square meters. */
	private double waterUsageCache;
	/** The cache value for the average grey water usage per sol per square meters. */
	private double greyWaterUsageCache;
	/** The cache value for the average O2 generated per sol per square meters. */
	private double o2Cache;
	/** The cache value for the average CO2 consumed per sol per square meters. */
	private double co2Cache;
	/** The cache value for the work time done in this greenhouse. */
	private double workTimeCache;
	/** The cache value for the total growing area in this greenhouse. */
	private double totalAreaCache;
	/** The cache value for the remaining area in this greenhouse. */
	private double remainingAreaCache;
	
	private JLabel radLabel;
	private JLabel farmerLabel;
	private JLabel cropsLabel;
	private JLabel waterLabel;
	
	private JLabel greyWaterLabel;
	private JLabel o2Label;
	private JLabel co2Label;
	private JLabel workTimeLabel;
	
	private JLabel totalAreaLabel;
	private JLabel remainingAreaLabel;

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
		AttributePanel springPanel = new AttributePanel(5, 2);
		center.add(springPanel, BorderLayout.CENTER);

		// Prepare solar irradiance label
		radCache = surfaceFeatures.getSolarIrradiance(location);
		radLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.solarIrradiance.title"),
				DECIMAL_W_M2.format(radCache), "Estimated sunlight on top of the greenhouse roof");

		// Prepare farmers label
		farmersCache = farm.getFarmerNum();
		farmerLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.numFarmers.title"),
				                 Integer.toString(farmersCache), "# of active gardeners tending the greenhouse");

		totalAreaCache = farm.getGrowingArea();
		totalAreaLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.area.total"),
				StyleManager.DECIMAL_M2.format(totalAreaCache),
			 	Msg.getString("BuildingPanelFarming.area.total.tooltip"));
		
		// Prepare crops label
		cropsCache = farm.getCrops().size();
		cropsLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.numCrops.title"),
							   Integer.toString(cropsCache), null);

		remainingAreaCache = farm.getRemainingArea();
		remainingAreaLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.area.remaining"),
				StyleManager.DECIMAL_M2.format(remainingAreaCache),
			 	Msg.getString("BuildingPanelFarming.area.remaining.tooltip"));
		

		// Update the cumulative work time
		workTimeCache = farm.getCumulativeWorkTime()/1000.0;
		workTimeLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.workTime.title"),
									StyleManager.DECIMAL3_SOLS.format(workTimeCache),
									Msg.getString("BuildingPanelFarming.workTime.tooltip"));

		waterUsageCache = farm.computeUsage(ResourceUtil.waterID);
		waterLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.waterUsage.title"),
									DECIMAL_KG_SOL.format(waterUsageCache),
									Msg.getString("BuildingPanelFarming.waterUsage.tooltip"));
		
		o2Cache = farm.computeUsage(ResourceUtil.oxygenID);
		o2Label = springPanel.addTextField(Msg.getString("BuildingPanelFarming.o2.title"),
									DECIMAL_KG_SOL.format(o2Cache),
									Msg.getString("BuildingPanelFarming.o2.tooltip"));

		greyWaterUsageCache = farm.computeUsage(ResourceUtil.greyWaterID);
		greyWaterLabel = springPanel.addTextField(Msg.getString("BuildingPanelFarming.greyWaterUsage.title"),
									DECIMAL_KG_SOL.format(greyWaterUsageCache),
									Msg.getString("BuildingPanelFarming.greyWaterUsage.tooltip"));

		co2Cache = farm.computeUsage(ResourceUtil.co2ID);
		co2Label = springPanel.addTextField(Msg.getString("BuildingPanelFarming.co2.title"),
									DECIMAL_KG_SOL.format(co2Cache),
								 	Msg.getString("BuildingPanelFarming.co2.tooltip"));
		
		
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
			// Implement Table Cell ToolTip for crops
			@Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
				RowSorter<? extends TableModel> sorter = getRowSorter();
				if (sorter != null) {
					rowIndex = sorter.convertRowIndexToModel(rowIndex);
				}	

				CropTableModel model = (CropTableModel) getModel();
				if ((rowIndex < 0) || (rowIndex >= model.getRowCount())) {
					return "";
				}

				Crop crop = model.getCrop(rowIndex);
                int colIndex = columnAtPoint(p);
                if (colIndex == 0) {
					return Math.round(crop.getHealthCondition() * 1000.0)/10.0 + " %";
				}
                else if (colIndex == 1) {
					return generateCropSpecTip(crop.getCropSpec());
				}
				
				double sols = Math.round(crop.getGrowingTimeCompleted()/1_000.0 *10.0)/10.0;
				return "# of sols since planted: " + sols;
            }
        }; // end of WebTable
        
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
                            			+ crop.getCropName() + "' for an early harvest right now ? ", "Early Harvest", JOptionPane.YES_NO_OPTION);
                		        if (reply == JOptionPane.YES_OPTION) {
                		        	logger.info(building, 0, "Hand picked " + crop.getCropName() + " for an early harvest.");
                		        	crop.setToHarvest();
                		        }
                		        else {
                		        	logger.info(building, 0, "Not choosing " + crop.getCropName() + " for an early harvest.");
                		        }
                        }
                    });
            	}
            }
        });
        
        popupMenu.add(harvestItem); 
        cropTable.setComponentPopupMenu(popupMenu);
		
		TableColumnModel cropColumns = cropTable.getColumnModel();
		cropColumns.getColumn(CropTableModel.HEALTH).setPreferredWidth(5);
		cropColumns.getColumn(CropTableModel.NAME).setPreferredWidth(40);
		cropColumns.getColumn(CropTableModel.PHASE).setPreferredWidth(30);
		cropColumns.getColumn(CropTableModel.GROWTH).setPreferredWidth(15);
		cropColumns.getColumn(CropTableModel.GROWTH).setCellRenderer(new PercentageCellRenderer(true));
		cropColumns.getColumn(CropTableModel.AREA).setPreferredWidth(20);
		cropColumns.getColumn(CropTableModel.AREA).setCellRenderer(new NumberCellRenderer());
		cropColumns.getColumn(CropTableModel.CAT).setPreferredWidth(30);
		cropColumns.getColumn(CropTableModel.WORK).setPreferredWidth(20);
		cropColumns.getColumn(CropTableModel.WORK).setCellRenderer(new NumberCellRenderer());

		cropTable.setCellSelectionEnabled(false); // need it so that the tooltip can be displayed.
		
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

		// Update solar irradiance label
		double rad = Math.round(surfaceFeatures.getSolarIrradiance(location)*10.0)/10.0;
		if (radCache != rad) {
			radCache = rad;
			radLabel.setText(DECIMAL_W_M2.format(rad));
		}

		// Update the average water usage
		double newWater = farm.computeUsage(ResourceUtil.waterID);
		if (waterUsageCache != newWater) {
			waterUsageCache = newWater;
			waterLabel.setText(DECIMAL_KG_SOL.format(newWater));
		}

		// Update the average O2 generated
		double newO2 = farm.computeUsage(ResourceUtil.oxygenID);
		if (o2Cache != newO2) {
			o2Cache = newO2;
			o2Label.setText(DECIMAL_KG_SOL.format(newO2));
		}

		// Update the average CO2 consumed
		double newCo2 = farm.computeUsage(ResourceUtil.co2ID);
		if (co2Cache != newCo2) {
			co2Cache = newCo2;
			co2Label.setText(DECIMAL_KG_SOL.format(newCo2));
		}

		// Update the average grey water usage
		double newGreyWater = farm.computeUsage(ResourceUtil.greyWaterID);
		if (greyWaterUsageCache != newGreyWater) {
			greyWaterUsageCache = newGreyWater;
			greyWaterLabel.setText(DECIMAL_KG_SOL.format(newGreyWater));
		}
		
		// Update the cumulative work time
		double workTime = farm.getCumulativeWorkTime()/1000.0;
		if (workTimeCache != workTime) {
			workTimeCache = workTime;
			workTimeLabel.setText(StyleManager.DECIMAL3_SOLS.format(workTime));
		}
		
		// Update the total growing area
		double totalArea = farm.getGrowingArea();
		if (totalAreaCache != totalArea) {
			totalAreaCache = totalArea;
			totalAreaLabel.setText(StyleManager.DECIMAL_M2.format(totalArea));
		}
		
		// Update the remaining growing area
		double remainingArea = farm.getRemainingArea();
		if (remainingAreaCache != remainingArea) {
			remainingAreaCache = remainingArea;
			remainingAreaLabel.setText(StyleManager.DECIMAL_M2.format(remainingArea));
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
			.append(EDIBLE_MASS).append(cs.getEdibleBiomass()).append(G_M2_DAY)
			.append(INEDIBLE_MASS).append(cs.getInedibleBiomass()).append(G_M2_DAY)
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
	private static class CropTableModel extends AbstractTableModel {

		private static final int HEALTH = 0;
		private static final int NAME = 1;
		private static final int PHASE = 2;
		private static final int CAT = 3;
		private static final int AREA = 4;
		private static final int GROWTH = 5;
		private static final int WORK = 6;

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

		public int getRowCount() {
			return crops.size();
		}

		public int getColumnCount() {
			return WORK + 1;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch(columnIndex) {
				case HEALTH -> Icon.class;
				case NAME, PHASE, CAT -> String.class;
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
				case CAT -> "Category";
				case WORK -> "Due";
				default -> null;
			};
		}
		
		public Object getValueAt(int row, int column) {

			Crop crop = crops.get(row);
			PhaseType currentPhase = crop.getPhaseType();
			String category = crop.getCropSpec().getCropCategory().getName();

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
					return Math.round(crop.getPercentGrowth() * 100.0)/100.0;
				case AREA: 
					return Math.round(crop.getGrowingArea() * 10.0)/10.0;
				case CAT:
					return category;
				case WORK:
					return Math.round(crop.getTendingScore() * 10.0)/10.0;
				default:
					return null;
				}
		}

		public void update() {
			if (!crops.equals(farm.getCrops()))
				crops = farm.getCrops();
			fireTableDataChanged();
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
		waterLabel = null;
		greyWaterLabel = null;
		o2Label = null;
		co2Label = null;
		workTimeLabel = null;
		totalAreaLabel = null;
		remainingAreaLabel = null;
		
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
