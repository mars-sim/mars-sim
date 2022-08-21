/*
 * Mars Simulation Project
 * BuildingPanelFarming.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.structure.building.function.farming.CropSpec;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.building.function.farming.PhaseType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.VerticalLabelUI;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;


/**
 * The BuildingPanelFarming class is a building function panel representing
 * the crop farm of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelFarming extends BuildingFunctionPanel
implements MouseListener {

	private static final String PLANT_ICON = Msg.getString("icon.plant"); //$NON-NLS-1$

	// Data members
	private JTextField radTF, farmersTF, cropsTF, waterUsageTF, greyWaterUsageTF, o2TF, co2TF, workTimeTF;

	// Data cache
	/** The number of farmers cache. */
	private int farmersCache;
	/** The number of crops cache. */
	private int cropsCache;
	/** The index cache for the crop to be deleted. */
	private int deletingCropIndex;
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

	private DefaultComboBoxModel<String> comboBoxModel;
	private JComboBoxMW<String> comboBox;
	private ListModel listModel;
	/** Table model for crop info. */
	private CropTableModel cropTableModel;
	private WebScrollPane listScrollPanel;

	/** The farming building. */
	private Farming farm;
	private String cropName;
	private String deletingCropType;
	private Coordinates location;

	private ArrayList<String> tooltipArray;
	private List<String> cropCache;
	private JList<String> list;

	private CropConfig cropConfig;

	private static SurfaceFeatures surface;

	/**
	 * Constructor.
	 * @param farm {@link Farming} the farming building this panel is for.
	 * @param desktop {@link MainDesktopPane} The main desktop.
	 */
	public BuildingPanelFarming(final Farming farm, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelFarming.title"), 
			ImageLoader.getNewIcon(PLANT_ICON), 
			farm.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.farm = farm;
		location = farm.getBuilding().getCoordinates();
		surface = desktop.getSimulation().getSurfaceFeatures();
		cropConfig = SimulationConfig.instance().getCropConfiguration();
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create label panel
		WebPanel springPanel = new WebPanel(new SpringLayout());
		center.add(springPanel, BorderLayout.CENTER);

		// Prepare solar irradiance label
		radCache = Math.round(surface.getSolarIrradiance(location)*10.0)/10.0;
		radTF = addTextField(springPanel, Msg.getString("BuildingPanelFarming.solarIrradiance.title"),
							 radCache + "", "Estimated sunlight on top of the greenhouse roof");

		// Prepare farmers label
		farmersCache = farm.getFarmerNum();
		farmersTF = addTextField(springPanel, Msg.getString("BuildingPanelFarming.numFarmers.title"),
				                 farmersCache + "", "# of active gardeners tending the greenhouse");

		// Prepare crops label
		cropsCache = farm.getCrops().size();
		cropsTF = addTextField(springPanel, Msg.getString("BuildingPanelFarming.numCrops.title"),
							   cropsCache + "", null);

		waterUsageCache = farm.computeUsage(0);
		waterUsageTF = addTextField(springPanel, Msg.getString("BuildingPanelFarming.waterUsage.title"),
									Msg.getString("BuildingPanelFarming.waterUsage", waterUsageCache + ""),
									Msg.getString("BuildingPanelFarming.waterUsage.tooltip"));

		greyWaterUsageCache = farm.computeUsage(3);
		greyWaterUsageTF = addTextField(springPanel, Msg.getString("BuildingPanelFarming.greyWaterUsage.title"),
									Msg.getString("BuildingPanelFarming.greyWaterUsage", greyWaterUsageCache + ""),
									Msg.getString("BuildingPanelFarming.greyWaterUsage.tooltip"));
		
		o2Cache = farm.computeUsage(1);
		o2TF = addTextField(springPanel, Msg.getString("BuildingPanelFarming.o2.title"),
							Msg.getString("BuildingPanelFarming.o2", o2Cache + ""),
							Msg.getString("BuildingPanelFarming.o2.tooltip"));

		co2Cache = farm.computeUsage(2);
		co2TF = addTextField(springPanel, Msg.getString("BuildingPanelFarming.co2.title"),
							 Msg.getString("BuildingPanelFarming.co2", co2Cache + ""),
							 Msg.getString("BuildingPanelFarming.co2.tooltip"));

		// Update the cumulative work time
		workTimeCache = Math.round(farm.getCumulativeWorkTime())/1000.0;
		workTimeTF = addTextField(springPanel, Msg.getString("BuildingPanelFarming.workTime.title"),
				 Msg.getString("BuildingPanelFarming.workTime", workTimeCache + ""),
				 Msg.getString("BuildingPanelFarming.workTime.tooltip"));
		
		// Lay out the spring panel.
		SpringUtilities.makeCompactGrid(springPanel,
		                                8, 2, //rows, cols
		                                INITX_DEFAULT, INITY_DEFAULT,        //initX, initY
		                                XPAD_DEFAULT, YPAD_DEFAULT);       //xPad, yPad

		WebPanel southPanel = new WebPanel(new BorderLayout());
		center.add(southPanel, BorderLayout.SOUTH);
		
		// Create scroll panel for crop table
		WebScrollPane tableScrollPanel = new WebScrollPane();
		// Set the height and width of the table
		tableScrollPanel.setPreferredSize(new Dimension(200, 290)); // 290 is the best fit for 10 crops

		southPanel.add(tableScrollPanel, BorderLayout.NORTH);

		// Prepare crop table model
		cropTableModel = new CropTableModel(farm);

		// Prepare crop table
		WebTable cropTable = new WebTable(cropTableModel) {

			public Component prepareRenderer(TableCellRenderer renderer,int Index_row, int Index_col) {
			                Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
			                //even index, selected or not selected
			                if (Index_row % 2 == 0 && !isCellSelected(Index_row, Index_col)) {
			                    comp.setBackground(new Color(242, 242, 242));
			                }
			                else {
			                    comp.setBackground(Color.white);
			                }
			                return comp;
			            }

			// Implement Table Cell ToolTip for crops
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
    			StringBuilder result = new StringBuilder("");

                try {
                	//if (colIndex == 1)
                		result.append(buildCropToolTip(rowIndex, colIndex, null));
                	} catch (RuntimeException e1) {
                		//catch null pointer exception if mouse is over an empty line
                }
    			return result.toString();

            }
        }; // end of WebTable

        cropTable.setRowSelectionAllowed(true);
		cropTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
		cropTable.setCellSelectionEnabled(false); // need it so that the tooltip can be displayed.
		cropTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		cropTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		cropTable.getColumnModel().getColumn(2).setPreferredWidth(40);
		cropTable.getColumnModel().getColumn(3).setPreferredWidth(20);
		cropTable.getColumnModel().getColumn(4).setPreferredWidth(30);
		cropTable.getColumnModel().getColumn(5).setPreferredWidth(30);
		cropTable.setAutoCreateRowSorter(true);
		
		TableStyle.setTableStyle(cropTable);
		tableScrollPanel.setViewportView(cropTable);

		WebPanel queuePanel = new WebPanel(new BorderLayout());
	    southPanel.add(queuePanel, BorderLayout.CENTER);

	    WebPanel selectPanel = new WebPanel(new FlowLayout());
	    queuePanel.add(selectPanel, BorderLayout.NORTH); // 1st add

		WebPanel buttonPanel = new WebPanel(new BorderLayout());
		WebButton addButton = new WebButton(Msg.getString("BuildingPanelFarming.addButton")); //$NON-NLS-1$
		addButton.setPreferredSize(new Dimension(60, 20));
		addButton.setFont(new Font("Serif", Font.PLAIN, 9));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cropName = (String) comboBox.getSelectedItem();
				farm.addCropListInQueue(cropName);
		        listUpdate();
				repaint();
			}
			});
		buttonPanel.add(addButton, BorderLayout.NORTH);
		selectPanel.add(buttonPanel);

		WebButton delButton = new WebButton(Msg.getString("BuildingPanelFarming.delButton")); //$NON-NLS-1$
		delButton.setPreferredSize(new Dimension(60, 20));
		delButton.setFont(new Font("Serif", Font.PLAIN, 9));

		delButton.addActionListener(s -> {
				if (!list.isSelectionEmpty() && (list.getSelectedValue() != null)) {
		           	selectCrop();
	            	farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
	            	listUpdate();
	            	repaint();
				}
			});
		buttonPanel.add(delButton, BorderLayout.CENTER);

       	// Set up crop combo box model.
		List<String> nameList = cropConfig.getCropTypeNames();
		cropCache = new ArrayList<>(nameList);
		comboBoxModel = new DefaultComboBoxModel<>();

		tooltipArray = new ArrayList<>();

		Iterator<String> i = cropCache.iterator();
		int j = 0;
		while (i.hasNext()) {
			String n = i.next();
	    	comboBoxModel.addElement(n);
	    	tooltipArray.add(buildCropToolTip(j, -1, n).toString());
	    	j++;
		}

		// Create comboBox.
		comboBox = new JComboBoxMW<>(comboBoxModel);

		// Use tooltip to display the crop parameters for each crop in the combobox
	    ComboboxToolTipRenderer toolTipRenderer = new ComboboxToolTipRenderer();
	    comboBox.setRenderer(toolTipRenderer);
	    toolTipRenderer.setTooltips(tooltipArray);

		comboBox.addActionListener(s -> cropName = (String) comboBox.getSelectedItem());
		comboBox.setMaximumRowCount(10);
	    selectPanel.add(comboBox);

		WebPanel queueListPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		WebPanel queueButtonLabelPanel = new WebPanel(new BorderLayout());
	    WebLabel queueListLabel = new WebLabel("     Crop Queue     ");
		queueListLabel.setUI(new VerticalLabelUI(false));
	    queueListLabel.setFont( new Font( "Dialog", Font.PLAIN, 14) );
		queueListLabel.setBorder(new MarsPanelBorder());
	    queueButtonLabelPanel.add(queueListLabel, BorderLayout.NORTH);
		queueListPanel.add(queueButtonLabelPanel);
	    queuePanel.add(queueListPanel, BorderLayout.CENTER);

		// Create scroll panel for population list.
		listScrollPanel = new WebScrollPane();
		listScrollPanel.setPreferredSize(new Dimension(150, 150));
		listScrollPanel.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY) );

		// Create list model
		listModel = new ListModel();
		// Create list
		list = new JList<>(listModel);
		listScrollPanel.setViewportView(list);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
		        if (!event.getValueIsAdjusting() && event != null){
					selectCrop();
		        }
		    }
		});
		queueListPanel.add(listScrollPanel);
	}

	/*
	 * Builds an tooltip for displaying the growth parameters of a crop
	 */
	private StringBuilder buildCropToolTip(int row, int col, String n) {

		StringBuilder result = new StringBuilder("");
		String cropName, cat;
		double time;
		double mass0, mass1;
		double water, PAR;
		double sols = 0;
		double health = 0;

        if (n == null || n.equals("")) {
    		List<Crop> crops = farm.getCrops();
            Crop crop = crops.get(row);
            CropSpec ct = crop.getCropSpec();
        	cropName = Conversion.capitalize(crop.getCropName());
            cat = ct.getCropCategory().getName();
        	mass0 = ct.getEdibleBiomass();
        	water = 100 * ct.getEdibleWaterContent();
        	mass1 = ct.getInedibleBiomass();
        	time = ct.getGrowingTime() /1000;
        	PAR = ct.getDailyPAR();
        	health =  Math.round(crop.getHealthCondition()*10.0 * 100.0)/10.0;
        	sols = Math.round(crop.getGrowingTimeCompleted()*10.0 /1_000.0)/10.0;

        	if (col == 0) {
	        	result.append("Health: ").append(health).append(" %");
        	}

        	else if (col == 1) {
	            result.append("<html>").append("&emsp;&nbsp;Crop Name:&emsp;").append(cropName);
	        	result.append("<br>&emsp;&emsp;&nbsp;&nbsp;Category:&emsp;").append(cat);
	           	result.append("<br>&nbsp;Growing Days:&emsp;").append(time);
	        	result.append("<br>&emsp;Edible Mass:&emsp;").append(mass0).append(" g/m2/day");
	        	result.append("<br>&nbsp;Inedible Mass:&emsp;").append(mass1).append(" g/m2/day");
	        	result.append("<br>&nbsp;Water Content:&emsp;").append(water).append(" %");
	        	result.append("<br>&nbsp;&nbsp;PAR required:&emsp;").append(PAR).append(" mol/m2/day").append("</html>");
        	}

        	else {
	        	result.append("# of Sols since planted: ").append(sols);
        	}

        }

        if (col == -1) {
        	cropName = Conversion.capitalize(n);
        	CropSpec cType = cropConfig.getCropTypeByName(n);
            cat = cType.getCropCategory().getName();
        	mass0 = cType.getEdibleBiomass();
        	water = 100 * cType.getEdibleWaterContent();
        	mass1 = cType.getInedibleBiomass();
        	time = cType.getGrowingTime() /1000;
        	PAR = cType.getDailyPAR();

            result.append("<html>").append("&emsp;&nbsp;Crop Name:&emsp;").append(cropName);
        	result.append("<br>&emsp;&emsp;&nbsp;&nbsp;Category:&emsp;").append(cat);
           	result.append("<br>&nbsp;Growing Days:&emsp;").append(time);
        	result.append("<br>&emsp;Edible Mass:&emsp;").append(mass0).append(" g/m2/day");
        	result.append("<br>&nbsp;Inedible Mass:&emsp;").append(mass1).append(" g/m2/day");
        	result.append("<br>&nbsp;Water Content:&emsp;").append(water).append(" %");
        	result.append("<br>&nbsp;&nbsp;PAR required:&emsp;").append(PAR).append(" mol/m2/day").append("</p></html>");
        }

    	return result;
	}

	/**
	 * Selects Crop
	 * @param table
	 */
	public void selectCrop() {

		String n = (String) list.getSelectedValue();
		if (n != null) {
			deletingCropType = n;
			deletingCropIndex = list.getSelectedIndex();
		}
		else
			listUpdate();
	}

	@SuppressWarnings("unchecked")
	public void listUpdate() {

		listModel.update();
 		list.validate();
 		list.revalidate();
 		list.repaint();
 		listScrollPanel.validate();
 		listScrollPanel.revalidate();
 		listScrollPanel.repaint();
		comboBox.setRenderer(new PromptComboBoxRenderer("A list of crops"));
		comboBox.setSelectedIndex(-1);
	}

	/**
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	@Override
	public void mouseClicked(MouseEvent event) {

		// Note: If double-click, open tooltip for the selected crop?
		if (event.getClickCount() >= 2) {
			selectCrop();
			if (deletingCropType != null) {
            	// do something
            	//listModel.update();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {}
	
	@Override
	public void mouseReleased(MouseEvent event) {}
	
	@Override
	public void mouseEntered(MouseEvent event) {}
	
	@Override
	public void mouseExited(MouseEvent event) {}

	/**
	 * Update this panel
	 */
	@Override
	public void update() {

		// Update farmers label if necessary.
		if (farmersCache != farm.getFarmerNum()) {
			farmersCache = farm.getFarmerNum();
			farmersTF.setText(farmersCache + "");
		}

		// Update crops label if necessary.
		if (cropsCache != farm.getCrops().size()) {
			cropsCache = farm.getCrops().size();
			cropsTF.setText(cropsCache + "");
		}

		// Update solar irradiance label
		double rad = Math.round(surface.getSolarIrradiance(location)*10.0)/10.0;
		if (radCache != rad) {
			radCache = rad;
			radTF.setText(Msg.getString("BuildingPanelFarming.solarIrradiance", radCache));
		}

		// Update the average water usage
		double newWater = farm.computeUsage(0);
		if (waterUsageCache != newWater) {
			waterUsageCache = newWater;
			waterUsageTF.setText(Msg.getString("BuildingPanelFarming.waterUsage", newWater));
		}

		// Update the average O2 generated
		double newO2 = farm.computeUsage(1);
		if (o2Cache != newO2) {
			o2Cache = newO2;
			o2TF.setText(Msg.getString("BuildingPanelFarming.o2", newO2));
		}

		// Update the average CO2 consumed
		double newCo2 = farm.computeUsage(2);
		if (co2Cache != newCo2) {
			co2Cache = newCo2;
			co2TF.setText(Msg.getString("BuildingPanelFarming.co2", newCo2));
		}

		// Update the average grey water usage
		double newGreyWater = farm.computeUsage(3);
		if (greyWaterUsageCache != newGreyWater) {
			greyWaterUsageCache = newGreyWater;
			greyWaterUsageTF.setText(Msg.getString("BuildingPanelFarming.greyWaterUsage", newGreyWater));
		}
		
		// Update the cumulative work time
		double workTime = Math.round(farm.getCumulativeWorkTime())/1000.0;
		if (workTimeCache != workTime) {
			workTimeCache = workTime;
			workTimeTF.setText(Msg.getString("BuildingPanelFarming.workTime", workTime));
		}
		
		
		// Update crop table.
		cropTableModel.update();

		// Update list
		listModel.update();
 		list.validate();
 		list.revalidate();
 		list.repaint();
 		listScrollPanel.validate();
 		listScrollPanel.revalidate();
 		listScrollPanel.repaint();
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
         * Update the list model.
         */
        public void update() {

        	List<String> c = farm.getCropListInQueue();
        		// if the list contains duplicate items, it somehow pass this test
        		if (list.size() != c.size() || !list.containsAll(c) || !c.containsAll(list)) {
	                List<String> oldList = list;
	                List<String> tempList = new ArrayList<>(c);
	                //Collections.sort(tempList);

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

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private Farming farm;
		private List<Crop> crops;
		private ImageIcon redDot;
		private ImageIcon redHalfDot;
		private ImageIcon yellowDot;
		private ImageIcon yellowHalfDot;
		private ImageIcon greenDot;
		private ImageIcon greenHalfDot;

		private CropTableModel(Farming farm) {
			this.farm = farm;
			crops = farm.getCrops();
			redDot = ImageLoader.getIcon("RedDot");
			redHalfDot = ImageLoader.getIcon("dot_red_half");
			yellowDot = ImageLoader.getIcon("YellowDot");
			yellowHalfDot = ImageLoader.getIcon("dot_yellow_half");
			greenDot = ImageLoader.getIcon("GreenDot");
			greenHalfDot = ImageLoader.getIcon("dot_green_half");


		}

		public int getRowCount() {
			return crops.size();
		}

		// Change from 4 to 5 in order to include the crop's category as columnIndex 4
		public int getColumnCount() {
			return 6;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = ImageIcon.class;
			else if (columnIndex == 1) dataType = String.class;
			else if (columnIndex == 2) dataType = String.class;
			else if (columnIndex == 3) dataType = String.class;
			else if (columnIndex == 4) dataType = String.class;
			else if (columnIndex == 5) dataType = Double.class;
			
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Health";
			if (columnIndex == 1) return "Name";
			if (columnIndex == 2) return "Phase";
			if (columnIndex == 3) return "Growth";
			if (columnIndex == 4) return "Category";
			if (columnIndex == 5) return "Work";
			return null;
		}

		public Object getValueAt(int row, int column) {

			Crop crop = crops.get(row);
			PhaseType currentPhase = crop.getPhaseType();
			String category = crop.getCropSpec().getCropCategory().getName();

			if (column == 0) {
				double condition = crop.getHealthCondition();
				if (condition > .95) return greenDot;
				else if (condition > .75) return greenHalfDot;
				else if (condition > .55 ) return yellowDot;
				else if (condition > .35 ) return yellowHalfDot;
				else if (condition > .2 ) return redDot;
				else return redHalfDot;
			}
			else if (column == 1) return Conversion.capitalize(crop.getCropName());
			else if (column == 2) return currentPhase.getName();
			else if (column == 3) {
				double growth = crop.getPercentGrowth();
				if (growth > 100)
					growth = 100;
				return String.valueOf(growth) + "%";
			}
			else if (column == 4) return Conversion.capitalize(category);
			else if (column == 5) return DECIMAL_PLACES1.format(crop.getCurrentWorkRequired());
	
			return null;
		}

		public void update() {
			if (!crops.equals(farm.getCrops())) crops = farm.getCrops();
			fireTableDataChanged();
		}
	}

	class ComboboxToolTipRenderer extends DefaultListCellRenderer {
	    private ArrayList<String> tooltips;

	    @Override
	    public Component getListCellRendererComponent(JList<?> list, Object value,
	                        int index, boolean isSelected, boolean cellHasFocus) {

	    	JComponent comp = (JComponent) super.getListCellRendererComponent(list,
	                value, index, isSelected, cellHasFocus);

	        if (-1 < index && null != value && null != tooltipArray) {
	        	list.setToolTipText((String) tooltipArray.get(index));
	        }
	        return comp;
	    }

	    public void setTooltips(ArrayList<String> tooltipArray) {
	        this.tooltips = tooltipArray;

	    }
	}

	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private String prompt;

//		private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	    // Width doesn't matter as the combo box will size
	    //private Dimension preferredSize = new Dimension(0, 20);

		/*
		 *  Set the text to display when no item has been selected.
		 */
		public PromptComboBoxRenderer(String prompt) {
			this.prompt = prompt;
		}

		/*
		 *  Custom rendering to display the prompt text when no item is selected
		 */
		// Add color rendering
		public Component getListCellRendererComponent(
				JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value == null) {
				setText(Conversion.capitalize(prompt));
				return this;
			}


			if (c instanceof WebLabel) {

	            if (isSelected) {
	                //c.setBackground(Color.orange);
	            } else {
	                //c.setBackground(Color.white);
	                //c.setBackground(new Color(51,25,0,128));
	            }

	        } else {
	        	//c.setBackground(Color.white);
	            //c.setBackground(new Color(51,25,0,128));
	            c = super.getListCellRendererComponent(
	                    list, value, index, isSelected, cellHasFocus);
	        }
	        return c;
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		// take care to avoid null exceptions
		if (cropCache != null) {
			cropCache.clear();
			cropCache = null;
		}

		farm = null;
		tooltipArray = null;
		comboBoxModel= null;
		comboBox= null;
		list= null;
		listModel= null;
		cropTableModel= null;
		listScrollPanel= null;
		cropName= null;
		deletingCropType= null;
	}
}
