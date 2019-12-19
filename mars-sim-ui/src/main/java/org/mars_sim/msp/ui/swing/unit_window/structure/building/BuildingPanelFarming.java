/**
 * Mars Simulation Project
 * BuildingPanelFarming.java
 * @version 3.1.0 2017-03-31
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
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.structure.building.function.farming.CropType;
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

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;
import com.alee.laf.text.WebTextField;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;


/**
 * The FarmingBuildingPanel class is a building function panel representing
 * the crop farming status of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelFarming
extends BuildingFunctionPanel
implements MouseListener {
	
	// Data members
	private WebTextField radTF, farmersTF, cropsTF, fishTF, weedTF, waterUsageTF, o2TF, co2TF;
	
	// Data cache
	/** The number of farmers cache. */
	private int farmersCache;
	/** The number of crops cache. */
	private int cropsCache;
	/** The number of fish cache. */
	private int fishCache;
	/** The mass of weed cache. */
	private double weedCache;
	/** The index cache for the crop to be deleted. */
	private int deletingCropIndex;
	/** The cache for the amount of solar irradiance. */
	private double radCache;
	/** The cache value for the average water usage per sol per square meters. */
	private double waterUsageCache;
	/** The cache value for the average O2 generated per sol per square meters. */
	private double o2Cache;
	/** The cache value for the average CO2 consumed per sol per square meters. */
	private double co2Cache;
	
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
		
	private static SurfaceFeatures surface;
	
	/**
	 * Constructor.
	 * @param farm {@link Farming} the farming building this panel is for.
	 * @param desktop {@link MainDesktopPane} The main desktop.
	 */
	@SuppressWarnings("unchecked")
	public BuildingPanelFarming(final Farming farm, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(farm.getBuilding(), desktop);
		
		// Initialize data members
		this.farm = farm;
		location = farm.getBuilding().getCoordinates();
		surface = Simulation.instance().getMars().getSurfaceFeatures();
		
		// Set panel layout
		setLayout(new BorderLayout()); //new GridLayout(6, 1, 0, 0));//

		// Prepare farming label
		WebLabel farmingLabel = new WebLabel(Msg.getString("BuildingPanelFarming.title"), WebLabel.CENTER);
		WebPanel farmingPanel = new WebPanel(new FlowLayout());
	    farmingPanel.add(farmingLabel);
		farmingLabel.setFont(new Font("Serif", Font.BOLD, 16));
		add(farmingLabel, BorderLayout.NORTH);
		//farmingLabel.setForeground(new Color(102, 51, 0)); // dark brown

		// Create label panel
		WebPanel springPanel = new WebPanel(new SpringLayout());//GridLayout(5, 1, 0, 0));
		add(springPanel, BorderLayout.CENTER);
		
		// Prepare solar irradiance label
		WebLabel radLabel = new WebLabel(Msg.getString("BuildingPanelFarming.solarIrradiance.title", radCache), WebLabel.RIGHT);
		TooltipManager.setTooltip(radLabel, "Estimated sunlight on top of the greenhouse roof", TooltipWay.down);
		springPanel.add(radLabel);

		radCache = Math.round(surface.getSolarIrradiance(location)*10.0)/10.0;
		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		radTF = new WebTextField(radCache + "");
		radTF.setEditable(false);
		radTF.setColumns(7);
		radTF.setPreferredSize(new Dimension(120, 25));
		wrapper1.add(radTF);
		springPanel.add(wrapper1);
		
		
		// Prepare farmers label
		WebLabel farmersLabel = new WebLabel(Msg.getString("BuildingPanelFarming.numFarmers.title"), WebLabel.RIGHT);
	    //farmersPanel.add(farmersLabel);
		TooltipManager.setTooltip(radLabel, "# of active gardeners tending the greenhouse", TooltipWay.down);
		springPanel.add(farmersLabel);

		farmersCache = farm.getFarmerNum();
		WebPanel wrapper2 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		farmersTF = new WebTextField(farmersCache + "");
		farmersTF.setEditable(false);
		farmersTF.setColumns(3);
		farmersTF.setPreferredSize(new Dimension(120, 25));
		wrapper2.add(farmersTF);
		springPanel.add(wrapper2);
		
		// Prepare crops label
		WebLabel cropsLabel = new WebLabel(Msg.getString("BuildingPanelFarming.numCrops.title"), WebLabel.RIGHT);
		springPanel.add(cropsLabel);

		cropsCache = farm.getCrops().size();
		WebPanel wrapper3 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		cropsTF = new WebTextField(cropsCache + "");
		cropsTF.setEditable(false);
		cropsTF.setColumns(3);
		cropsTF.setPreferredSize(new Dimension(120, 25));
		wrapper3.add(cropsTF);
		springPanel.add(wrapper3);
		
		// Prepare fish label
		WebLabel fishLabel = new WebLabel(Msg.getString("BuildingPanelFarming.numFish.title"), WebLabel.RIGHT);
		springPanel.add(fishLabel);

		fishCache = farm.getNumFish();
		WebPanel wrapper3a = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		fishTF = new WebTextField(fishCache + "");
		fishTF.setEditable(false);
		fishTF.setColumns(6);
		fishTF.setPreferredSize(new Dimension(120, 25));
		wrapper3a.add(fishTF);
		springPanel.add(wrapper3a);
		
		// Prepare weed label
		WebLabel weedLabel = new WebLabel(Msg.getString("BuildingPanelFarming.massWeed.title"), WebLabel.RIGHT);
		springPanel.add(weedLabel);

		weedCache = farm.getWeedMass();
		WebPanel wrapper3b = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		weedTF = new WebTextField(weedCache + "");
		weedTF.setEditable(false);
		weedTF.setColumns(6);
		weedTF.setPreferredSize(new Dimension(120, 25));
		wrapper3b.add(weedTF);
		springPanel.add(wrapper3b);
		
		WebLabel waterUsageLabel = new WebLabel(Msg.getString("BuildingPanelFarming.waterUsage.title"), WebLabel.RIGHT);
		//waterUsagePanel.add(waterUsageLabel);
		waterUsageLabel.setToolTipText(Msg.getString("BuildingPanelFarming.waterUsage.tooltip"));
		springPanel.add(waterUsageLabel);
		
		waterUsageCache = farm.computeUsage(0);
		WebPanel wrapper4 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		waterUsageTF = new WebTextField(Msg.getString("BuildingPanelFarming.waterUsage", waterUsageCache + ""));
		waterUsageTF.setEditable(false);
		waterUsageTF.setColumns(10);
		waterUsageTF.setPreferredSize(new Dimension(120, 25));
		wrapper4.add(waterUsageTF);
		springPanel.add(wrapper4);
		
		WebLabel o2Label = new WebLabel(Msg.getString("BuildingPanelFarming.o2.title"), WebLabel.RIGHT);
		o2Label.setToolTipText(Msg.getString("BuildingPanelFarming.o2.tooltip"));
		springPanel.add(o2Label);
		
		o2Cache = farm.computeUsage(1);
		WebPanel wrapper5 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		o2TF = new WebTextField(Msg.getString("BuildingPanelFarming.o2", o2Cache + ""));
		o2TF.setEditable(false);
		o2TF.setColumns(10);
		o2TF.setPreferredSize(new Dimension(120, 25));
		wrapper5.add(o2TF);
		springPanel.add(wrapper5);

		WebLabel co2Label = new WebLabel(Msg.getString("BuildingPanelFarming.co2.title"), WebLabel.RIGHT);
		co2Label.setToolTipText(Msg.getString("BuildingPanelFarming.co2.tooltip"));
		springPanel.add(co2Label);
		
		co2Cache = farm.computeUsage(2);
		WebPanel wrapper6 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		co2TF = new WebTextField(Msg.getString("BuildingPanelFarming.co2", co2Cache + ""));
		co2TF.setEditable(false);
		co2TF.setColumns(10);
		co2TF.setPreferredSize(new Dimension(120, 25));
		wrapper6.add(co2TF);
		springPanel.add(wrapper6);
	
		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(springPanel,
		                                8, 2, //rows, cols
		                                65, 20,        //initX, initY
		                                3, 1);       //xPad, yPad
		

//		// Add opsPanel and opsButton
//		WebPanel opsPanel = new WebPanel(new FlowLayout());
//		labelPanel.add(opsPanel);
//        opsButton = new WebButton("Ops Panel");
//        //slotButton.setOpaque(false);
//        //slotButton.setBackground(new Color(51,25,0,128));
//        //slotButton.setForeground(Color.ORANGE);
//        //slotButton.setEnabled(processComboBox.getItemCount() > 0);
//        //opsButton.setToolTipText("Click to enter the greenhouse ops panel");
//		balloonToolTip.createBalloonTip(opsButton, "<html>Enter the greenhouse ops panel.</html>"); //$NON-NLS-1$
//
//        opsButton.addActionListener(new ActionListener() {
//        	public void actionPerformed(ActionEvent event) {
//        		//try {
//        			openGreenhouseOps();
//        		//}
//        		//catch (Exception e) {
//        		//	logger.log(Level.SEVERE, "new slot button", e);
//        		//}
//        	}
//        });
//        opsPanel.add(opsButton);

		WebPanel southPanel = new WebPanel(new BorderLayout());
		add(southPanel, BorderLayout.SOUTH);
		
		// Create scroll panel for crop table
		WebScrollPane tableScrollPanel = new WebScrollPane();
//		if (farm.getBuilding().getBuildingType().equalsIgnoreCase("Large Greenhouse"))
//			tableScrollPanel.setPreferredSize(new Dimension(200, 280)); // 280 is the best fit for 15 crops
//		else
			// Increase the height from 100 to 130 to make the first 5 rows of crop FULLY visible
			tableScrollPanel.setPreferredSize(new Dimension(200, 140)); // 140 is the best fit for 5 crops

		southPanel.add(tableScrollPanel, BorderLayout.NORTH);

		// Prepare crop table model
		cropTableModel = new CropTableModel(farm);

		// Prepare crop table
		WebTable cropTable = new WebTable(cropTableModel){
			private static final long serialVersionUID = 1L;

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
//                String name = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
    			StringBuilder result = new StringBuilder("");

                try {
                	//if (colIndex == 1)
                		result.append(buildCropToolTip(rowIndex, colIndex, null));
                	} catch (RuntimeException e1) {//catch null pointer exception if mouse is over an empty line
                }
    			return result.toString();

            }
        }; // end of WebTable

		cropTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
		cropTable.setCellSelectionEnabled(false); // need it so that the tooltip can be displayed.
		cropTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		cropTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		cropTable.getColumnModel().getColumn(2).setPreferredWidth(40);
		cropTable.getColumnModel().getColumn(3).setPreferredWidth(20);
		cropTable.getColumnModel().getColumn(4).setPreferredWidth(30);

		TableStyle.setTableStyle(cropTable);
		tableScrollPanel.setViewportView(cropTable);

		WebPanel queuePanel = new WebPanel(new BorderLayout());
	    //add(queuePanel, BorderLayout.SOUTH);
	    southPanel.add(queuePanel, BorderLayout.CENTER);
	    
	    WebPanel selectPanel = new WebPanel(new FlowLayout());
	    queuePanel.add(selectPanel, BorderLayout.NORTH); // 1st add

		//2014-12-09 Added addButton for adding a crop to queue
		WebPanel buttonPanel = new WebPanel(new BorderLayout());
		WebButton addButton = new WebButton(Msg.getString("BuildingPanelFarming.addButton")); //$NON-NLS-1$
	    //balloonToolTip.createBalloonTip(addButton, "<html>Select a crop from <br> the left to add</html>");
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
	    //balloonToolTip.createBalloonTip(delButton, "<html>Highlight a crop in <br> the queue below to delete </html>");
		delButton.setPreferredSize(new Dimension(60, 20));
		delButton.setFont(new Font("Serif", Font.PLAIN, 9));

		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!list.isSelectionEmpty() && (list.getSelectedValue() != null)) {
		           	selectCrop();
	            	farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
	            	listUpdate();
	            	repaint();
				}
			}
			});
		buttonPanel.add(delButton, BorderLayout.CENTER);

       	// Set up crop combo box model.
		List<String> nameList = CropConfig.getCropTypeNames();
		cropCache = new ArrayList<String>(nameList);
		comboBoxModel = new DefaultComboBoxModel<String>();

		//tooltipArray = new String[cropCache.size()];
		tooltipArray = new ArrayList<String>();

		Iterator<String> i = cropCache.iterator();
		int j = 0;
		while (i.hasNext()) {
			String n = i.next();
	    	comboBoxModel.addElement(n);
			//tooltipArray[j] = buildCropToolTip(j, c).toString();
	    	tooltipArray.add(buildCropToolTip(j, -1, n).toString());
	    	j++;
		}

		// Create comboBox.
		comboBox = new JComboBoxMW<String>(comboBoxModel);

		// Add ComboboxToolTipRenderer to use tooltip to display the crop parameters for each crop in the combobox
	    ComboboxToolTipRenderer toolTipRenderer = new ComboboxToolTipRenderer();
	    comboBox.setRenderer(toolTipRenderer);
	    toolTipRenderer.setTooltips(tooltipArray);

		comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	cropName = (String) comboBox.getSelectedItem();
            }
        });
		comboBox.setMaximumRowCount(10);
	    //balloonToolTip.createBalloonTip(comboBox, "<html>Select a crop from here</html>");
	    selectPanel.add(comboBox);

		WebPanel queueListPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		WebPanel queueButtonLabelPanel = new WebPanel(new BorderLayout());
	    WebLabel queueListLabel = new WebLabel("     Crop Queue     ");
	    //<html><center>Crop<br>Queue:</center></html>");
		//queueListLabel.setUI(new org.mars_sim.msp.ui.swing.tool.VerticalLabelUI(false));
		queueListLabel.setUI(new com.jidesoft.plaf.xerto.VerticalLabelUI(false));
	    queueListLabel.setFont( new Font( "Dialog", Font.PLAIN, 14) );
		queueListLabel.setBorder(new MarsPanelBorder());
	    queueButtonLabelPanel.add(queueListLabel, BorderLayout.NORTH);
		queueListPanel.add(queueButtonLabelPanel);
	    queuePanel.add(queueListPanel, BorderLayout.CENTER); // 2nd add
	    
		// Create scroll panel for population list.
		listScrollPanel = new WebScrollPane();
		listScrollPanel.setPreferredSize(new Dimension(150, 150));
		listScrollPanel.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY) );

		// Create list model
		listModel = new ListModel();
		// Create list
		list = new JList<String>(listModel);
	    //balloonToolTip.createBalloonTip(list, "<html>Crops in the queue</html>");
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
	public StringBuilder buildCropToolTip(int row, int col, String n) {

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
            int id = crop.getCropTypeID();
            CropType ct = CropConfig.getCropTypeByID(id);
        	cropName = Conversion.capitalize(crop.getCropName());
            cat = CropConfig.getCropCategoryType(id).getName();
        	mass0 = ct.getEdibleBiomass();
        	water = 100 * ct.getEdibleWaterContent();
        	mass1 = ct.getInedibleBiomass();
        	time = ct.getGrowingTime() /1000;
        	PAR = ct.getDailyPAR();
        	health =  Math.round(crop.getHealthCondition()*10.0 * 100.0)/10.0;
        	sols = Math.round(crop.getGrowingTimeCompleted()*10.0 /1_000.0)/10.0;
        	
//        	result.append("<html><p width=\"500\">Crop Name: ").append(cropName).append(System.lineSeparator())
//        		.append("Category: ").append(cat).append(System.lineSeparator())
//        		.append("Growing Days: ").append(time);
        	
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
        	CropType cType = CropConfig.getCropTypeByName(n);
            cat = cType.getCropCategoryType().getName();
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
	
	/*
	 * Creates a TWL display window for greenhouse operations
	 */
    public void openGreenhouseOps() {
//        try {
//            Display.setDisplayMode(new DisplayMode(800, 600));
//            Display.create();
//            Display.setTitle("Greenhouse Operations Panel");
//            Display.setVSyncEnabled(true);
//
//            Mouse.setClipMouseCoordinatesToWindow(false);
//
//            InventoryDemo ops = new InventoryDemo();
//
//            LWJGLRenderer renderer = new LWJGLRenderer();
//            GUI gui = new GUI(ops, renderer);
//
//            ThemeManager theme = ThemeManager.createThemeManager(
//                    InventoryDemo.class.getResource("/twl/inventory/inventory.xml"), renderer);
//            gui.applyTheme(theme);
//
//            gui.validateLayout();
//            ops.positionFrame();
//
//            while(!Display.isCloseRequested() && !ops.quit) {
//                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
//
//                gui.update();
//                Display.update();
//            }
//
//            gui.destroy();
//            theme.destroy();
//        } catch (Exception ex) {
//            TestUtils.showErrMsg(ex);
//        }
//        Display.destroy();

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
    	//list.clearSelection(); // cause setting deletingCropIndex to -1
    	//list.setSelectedIndex(0);
	}

	/**
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {

		// TODO: If double-click, open tooltip for the selected crop?
		if (event.getClickCount() >= 2) {
			selectCrop();
			if (deletingCropType != null) {
            	//farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
            	//listModel.update();
			}
		}
	}

	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}


	/**
	 * Update this panel
	 */
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

		// Update fish label if necessary.
		if (fishCache != farm.getNumFish()) {
			fishCache = farm.getNumFish();
			fishTF.setText(fishCache + "");
		}
		
		// Update weed label if necessary.
		double newWeed = farm.getWeedMass();
		if (weedCache != newWeed) {
			weedCache = newWeed;
			weedTF.setText(weedCache + "");
		}
		
		// Update solar irradiance label if necessary.
		//Coordinates location = farm.getBuilding().getCoordinates();
		double rad = Math.round(surface.getSolarIrradiance(location)*10.0)/10.0;
		if (radCache != rad) {
			radCache = rad;
			radTF.setText(Msg.getString("BuildingPanelFarming.solarIrradiance", radCache));
		    //balloonToolTip.createBalloonTip(radLabel, "<html>Estimated amount of available <br> sunlight on top of the <br> greenhouse roof outside</html>");
		}

		// Update the average water usage
		double new_water = farm.computeUsage(0);
		if (waterUsageCache != new_water) {
			waterUsageCache = new_water;
			waterUsageTF.setText(Msg.getString("BuildingPanelFarming.waterUsage", waterUsageCache));
		}

		// Update the average O2 generated
		double new_o2 = farm.computeUsage(1);
		if (o2Cache != new_o2) {
			o2Cache = new_o2;
			o2TF.setText(Msg.getString("BuildingPanelFarming.o2", o2Cache));
		}

		// Update the average CO2 consumed
		double new_co2 = farm.computeUsage(2);
		if (co2Cache != new_co2) {
			co2Cache = new_co2;
			co2TF.setText(Msg.getString("BuildingPanelFarming.co2", co2Cache));
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

	    /** default serial id. */
	    private static final long serialVersionUID = 1L;

	    private List<String> list;

	    private ListModel() {

        	List<String> c = farm.getCropListInQueue();
	        if (c != null)
	        	list = new ArrayList<String>(c);
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
	                List<String> tempList = new ArrayList<String>(c);
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
		private java.util.List<Crop> crops;
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
			return 5;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = ImageIcon.class;
			else if (columnIndex == 1) dataType = String.class;
			else if (columnIndex == 2) dataType = String.class;
			else if (columnIndex == 3) dataType = String.class;
			// Aadd column 4 showing the crop's category
			else if (columnIndex == 4) dataType = String.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Health";
			else if (columnIndex == 1) return "Name";
			else if (columnIndex == 2) return "Phase";
			else if (columnIndex == 3) return "Growth";
			// Add column 4 showing the crop's category
			else if (columnIndex == 4) return "Category";
			else return null;
		}

		public Object getValueAt(int row, int column) {

			Crop crop = crops.get(row);
			//String phase = crop.getPhase();
			PhaseType currentPhase = crop.getPhaseType();
            int id = crop.getCropTypeID();
//            CropType ct = CropConfig.getCropTypeByID(id);
			String category = CropConfig.getCropCategoryType(id).getName();

			if (column == 0) {
				double condition = crop.getHealthCondition();
				if (condition > .9) return greenDot;
				else if (condition > .75) return greenHalfDot;
				else if (condition > .5 ) return yellowDot;
				else if (condition > .25 ) return yellowHalfDot;
				else if (condition > .1 ) return redDot;
				else return redHalfDot;
			}
			else if (column == 1) return Conversion.capitalize(crop.getCropName());
			else if (column == 2) return currentPhase.getName();
			else if (column == 3) {
				double growth = 0;
				//if (phaseType == PhaseType.GERMINATION || phaseType == PhaseType.SPROUTING) {
				//	growth = (int) (growingCompleted * 100D);
				//}
				//else if (phaseType == PhaseType.GROWING) {
				//	growth = (int) (growingCompleted * 100D);
				//}
				//else
//				if (currentPhase == PhaseType.HARVESTING) {
////					double growingCompleted = crop.getGrowingTimeCompleted() / ct.getGrowingTime();
////					growth = Math.round(growingCompleted * 1000D)/10D;
//					growth = crop.getPercentGrowth();
////					if (growth > 100)
////						growth = 100;
//				}
//				else if (currentPhase == PhaseType.FINISHED) {
//					growth = crop.getPercentGrowth();
////					if (growth > 100)
////						growth = 100;
//				}
//				else {
////					double growingCompleted = crop.getGrowingTimeCompleted() / ct.getGrowingTime();
////					growth = Math.round(growingCompleted * 1000D)/10D;
//					growth = crop.getPercentGrowth();
////					if (growth > 100)
////						growth = 100;
//				}

				growth = crop.getPercentGrowth();
				if (growth > 100)
					growth = 100;
				return String.valueOf(growth) + "%";
			}
			// Add column 4 showing the crop's category
			else if (column == 4) return Conversion.capitalize(category);
			else return null;
		}

		public void update() {
			if (!crops.equals(farm.getCrops())) crops = farm.getCrops();
			fireTableDataChanged();
		}
	}


	@SuppressWarnings("serial")
	class ComboboxToolTipRenderer extends DefaultListCellRenderer {
	    private ArrayList<String> tooltips;

	    @Override
	    public Component getListCellRendererComponent(JList<?> list, Object value,
	                        int index, boolean isSelected, boolean cellHasFocus) {

	    	JComponent comp = (JComponent) super.getListCellRendererComponent(list,
	                value, index, isSelected, cellHasFocus);

	        if (-1 < index && null != value && null != tooltipArray) {
	        	list.setToolTipText((String) tooltipArray.get(index));
	        	//System.out.println("value.toString is "+ value.toString());
	        	//System.out.println("list.toString is "+ list.toString());
	        	//balloonToolTip.createListItemBalloonTip(list, (String)(tooltipArray.get(index)), index);
	        }
	        return comp;
	    }

	    public void setTooltips(ArrayList<String> tooltipArray) {
	        this.tooltips = tooltipArray;

	    }
	}

	@SuppressWarnings("serial")
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
//				Component c = defaultRenderer.getListCellRendererComponent(
//		                list, value, index, isSelected, cellHasFocus);

			if (value == null) {
				setText(Conversion.capitalize(prompt));
				return this;
			}
			//if (index == -1) {
				//value = prompt.toString();
			//	setText(prompt);
			//	return this;
			//}
			//else {
				//setText(Conversion.capitalize(value.toString()));
				//c = super.getListCellRendererComponent(
	            //        list, Conversion.capitalize(value.toString()), index, isSelected, cellHasFocus);
				//CropType ct = (CropType) value;
				//setText(Conversion.capitalize(ct.getName()));
				//String s = buildCropToolTip(index).toString();
			    //balloonToolTip.createBalloonTip(list, s);
			//}

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
	public void destroy() {
		// take care to avoid null exceptions
		if (cropCache != null) {
			cropCache.clear();
			cropCache = null;
		}

		farm = null;
		tooltipArray = null;
		//balloonToolTip = null;
		comboBoxModel= null;
		comboBox= null;
		list= null;
		//opsButton= null;
		listModel= null;
		cropTableModel= null;
		listScrollPanel= null;
		cropName= null;
		deletingCropType= null;
	}
}
