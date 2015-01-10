/**
 * Mars Simulation Project
 * BuildingPanelFarming.java
 * @version 3.07 2015-01-06
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.building.function.Crop;
import org.mars_sim.msp.core.structure.building.function.CropConfig;
import org.mars_sim.msp.core.structure.building.function.CropType;
import org.mars_sim.msp.core.structure.building.function.Farming;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.ColumnResizer;


/**
 * The FarmingBuildingPanel class is a building function panel representing 
 * the crop farming status of a settlement building.
 */
public class BuildingPanelFarming
extends BuildingFunctionPanel 
implements Serializable, MouseListener {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private static final int CENTER = 0;

	// Data members
	/** The farming building. */
	private Farming farm;
	/** The number of farmers label. */
	private JLabel farmersLabel;
	/** The number of crops label. */
	private JLabel cropsLabel;
	/** Table model for crop info. */
	private CropTableModel cropTableModel;

	// Data cache
	/** The number of farmers cache. */
	private int farmersCache;
	/** The number of crops cache. */
	private int cropsCache;

	// 2014-12-09 Added comboBox for crop queue
	private DefaultComboBoxModel<CropType> comboBoxModel;
	private JComboBoxMW<CropType> comboBox;
	private List<CropType> cropCache;
	private CropType cropType;
	//private String cropInQueue;
	private ListModel listModel;
	private JList<CropType> list;
	private JScrollPane listScrollPanel;
	private String deletingCrop = "";
	private int deletingCropIndex;
	private CropType deletingCropType;
	/**
	 * Constructor.
	 * @param farm {@link Farming} the farming building this panel is for.
	 * @param desktop {@link MainDesktopPane} The main desktop.
	 */
	// 2014-11-20 Added tooltip for crops 
	public BuildingPanelFarming(final Farming farm, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(farm.getBuilding(), desktop);

		// Initialize data members
		this.farm = farm;

		// Set panel layout
		setLayout(new BorderLayout());

		// Create label panel
		JPanel labelPanel = new JPanel(new GridLayout(3, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));
		
		// Prepare farming label
		// 2014-11-21 Changed font type, size and color and label text
		// 2014-11-21 Added internationalization for the three labels
		JLabel farmingLabel = new JLabel(Msg.getString("BuildingPanelFarming.title"), JLabel.CENTER);
		farmingLabel.setFont(new Font("Serif", Font.BOLD, 16));
		farmingLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(farmingLabel);

		// Prepare farmers label
		farmersCache = farm.getFarmerNum();
		farmersLabel = new JLabel(Msg.getString("BuildingPanelFarming.numberOfFarmers", farmersCache), JLabel.CENTER);
		labelPanel.add(farmersLabel);

		// Prepare crops label
		cropsCache = farm.getCrops().size();
		cropsLabel = new JLabel(Msg.getString("BuildingPanelFarming.numberOfCrops", cropsCache), JLabel.CENTER);
		labelPanel.add(cropsLabel);

		// Create scroll panel for crop table
		JScrollPane scrollPanel = new JScrollPane();
		// 2014-10-10 mkung: increased the height from 100 to 130 to make the first 5 rows of crop FULLY visible
		scrollPanel.setPreferredSize(new Dimension(200, 130));
		scrollPanel.setOpaque(false);
		scrollPanel.setBackground(new Color(0,0,0,128));
		add(scrollPanel, BorderLayout.CENTER);

		// Prepare crop table model
		cropTableModel = new CropTableModel(farm);

		// Prepare crop table
		JTable cropTable = new JTable(cropTableModel){
			private static final long serialVersionUID = 1L;
			// 2014-11-20 Implement Table Cell ToolTip for crops           
            public String getToolTipText(MouseEvent e) {
                String name = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                java.util.List<Crop> crops;
    			StringBuilder result = new StringBuilder("<html>");
    			
                try {
                        crops = farm.getCrops();
                        Crop crop = crops.get(rowIndex);
                        double time;
                        double mass0, mass1;
                        double water;
                        String cropName, cat;
                        cropName = crop.getCropType().getName();
                        cat = crop.getCropType().getCropCategory();
                    	mass0 = crop.getCropType().getEdibleBiomass();
                    	water = 100 * crop.getCropType().getEdibleWaterContent();
                    	mass1 = crop.getCropType().getInedibleBiomass();
                    	time = crop.getCropType().getGrowingTime() /1000;
	                	result.append("&emsp;&nbsp;Crop Name:&emsp;");
	                	result.append(cropName);
	                	result.append("<br>&emsp;&emsp;&nbsp;&nbsp;Category:&emsp;");
	                	result.append(cat);
	                	result.append("<br>&emsp;Edible Mass:&emsp;");
	                	result.append(mass0).append(" kg");
	                	result.append("<br>&nbsp;Inedible Mass:&emsp;");
	                	result.append(mass1).append(" kg");              
	                	result.append("<br>&nbsp;Growing Days:&emsp;");
	                	result.append(time); 
	                	result.append("<br>&nbsp;Water Content:&emsp;");
	                	result.append(water).append(" %"); 	                	
      
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }
    			result.append("</html>");
    			return result.toString();
            }
        }; // end of JTable
		
		
		cropTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
		cropTable.setCellSelectionEnabled(false);
		cropTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		cropTable.getColumnModel().getColumn(1).setPreferredWidth(60);
		cropTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		cropTable.getColumnModel().getColumn(3).setPreferredWidth(40);
		// 2014-10-10 mkung: added column 4 showing the crop's category
		cropTable.getColumnModel().getColumn(4).setPreferredWidth(40);
		//cropTable.setOpaque(false);
		//cropTable.setBackground(new Color(0,0,0,128));
		setTableStyle(cropTable);
		
		scrollPanel.setViewportView(cropTable);
		
		JPanel queuePanel = new JPanel(new BorderLayout());
	    add(queuePanel, BorderLayout.SOUTH);   
	    queuePanel.setOpaque(false);
	    queuePanel.setBackground(new Color(0,0,0,128));
		
	    JPanel selectPanel = new JPanel(new FlowLayout());
	    selectPanel.setOpaque(false);
	    selectPanel.setBackground(new Color(0,0,0,128));
	    JLabel selectLabel = new JLabel(" Select from : ");
	    //selectLabel.setFont(new Font("Serif", Font.BOLD, 16));
	    //selectLabel.setForeground(new Color(102, 51, 0)); // dark brown
	    selectPanel.add(selectLabel);
	    queuePanel.add(selectPanel, BorderLayout.NORTH); // 1st add
	    
       	// 2014-12-09 Added crop combo box model.
        CropConfig config = SimulationConfig.instance().getCropConfiguration();
		List<CropType> cropTypeList = config.getCropList();
		//2014-12-12 Enabled Collections.sorts by implementing Comparable<CropType> 
		Collections.sort(cropTypeList);
		cropCache = new ArrayList<CropType>(cropTypeList);
		comboBoxModel = new DefaultComboBoxModel<CropType>();
		Iterator<CropType> i = cropCache.iterator();

		while (i.hasNext()) {
			CropType c = i.next();
	    	comboBoxModel.addElement(c);
		}
		//cropType = cropTypeList.get(0);
		
		// Create comboBox.
		comboBox = new JComboBoxMW<CropType>(comboBoxModel);
		// 2014-12-01 Added PromptComboBoxRenderer() & setSelectedIndex(-1)
		comboBox.setRenderer(new PromptComboBoxRenderer(" List of Crops "));
		comboBox.setSelectedIndex(-1);
		comboBox.setOpaque(false);
		comboBox.setBackground(new Color(51,25,0,128));
		//comboBox.setBackground(Color.LIGHT_GRAY);
		comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	cropType = (CropType) comboBox.getSelectedItem();
            	//System.out.println("BuildingPanelFarming.java: Selected cropType is " + cropType );
            }
            });  
		comboBox.setMaximumRowCount(10);
		selectPanel.add(comboBox);

		//2014-12-09 Added addButton for adding a crop to queue
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setOpaque(false);
		buttonPanel.setBackground(new Color(0,0,0,128));
		JButton addButton = new JButton(Msg.getString(
				"BuildingPanelFarming.addButton")); //$NON-NLS-1$
		addButton.setPreferredSize(new Dimension(60, 20));
		addButton.setFont(new Font("Serif", Font.PLAIN, 9));
		addButton.setOpaque(false);
		addButton.setBackground(new Color(0,0,0,128));
		addButton.setForeground(Color.ORANGE);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cropType = (CropType) comboBox.getSelectedItem();
				farm.addCropListInQueue(cropType);
            	//System.out.println("BuildingPanelFarming.java: Just added " + cropType );
		        listUpdate();
				repaint();
			}
			});
		buttonPanel.add(addButton, BorderLayout.NORTH);	
		
		selectPanel.add(buttonPanel);
		

		JButton delButton = new JButton(Msg.getString(
				"BuildingPanelFarming.delButton")); //$NON-NLS-1$
		delButton.setPreferredSize(new Dimension(60, 20));
		delButton.setFont(new Font("Serif", Font.PLAIN, 9));
		delButton.setOpaque(false);
		delButton.setBackground(new Color(0,0,0,128));
		delButton.setForeground(Color.ORANGE);

		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!list.isSelectionEmpty() && (list.getSelectedValue() != null)) {
		           	selectCrop();
	            	farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
		           	//System.out.println("BuildingPanelFarming.java: Just deleted " + cropType );
			        // 2015-01-06 Added listUpdate()
	            	listUpdate(); 
	            	repaint();
				}
			}
			});
		buttonPanel.add(delButton, BorderLayout.CENTER);	
	    
	    
		JPanel queueListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); //new FlowLayout(FlowLayout.CENTER));
		queueListPanel.setOpaque(false);
		queueListPanel.setBackground(new Color(0,0,0,128));
		JPanel queueButtonLabelPanel = new JPanel(new BorderLayout()); //new FlowLayout(FlowLayout.CENTER));
		queueButtonLabelPanel.setOpaque(false);
		queueButtonLabelPanel.setBackground(new Color(0,0,0,128));
	    JLabel queueListLabel = new JLabel("<html><center>Crop(s)<br>in<br>Queue:</center><br><br><br></html>");
	    queueButtonLabelPanel.add(queueListLabel, BorderLayout.NORTH);
		queueListPanel.add(queueButtonLabelPanel);
		
	    queuePanel.add(queueListPanel, BorderLayout.CENTER); // 2nd add

		// Create scroll panel for population list.
		listScrollPanel = new JScrollPane();
		listScrollPanel.setPreferredSize(new Dimension(150, 200));
		listScrollPanel.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY) );
        //scrollPanel.setViewportBorder(null);
        //scrollPanel.setBorder(BorderFactory.createEmptyBorder());
		listScrollPanel.getViewport().setOpaque(false);
		listScrollPanel.getViewport().setBackground(new Color(0, 0, 0, 0));
		listScrollPanel.setOpaque(false);
		listScrollPanel.setBackground(new Color(0, 0, 0, 0));
		
		// Create list model
		listModel = new ListModel(); //settlement);
		// Create list
		list = new JList<CropType>(listModel);
		listScrollPanel.setViewportView(list);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
		        if (!event.getValueIsAdjusting() && event != null){
					selectCrop();
		            //JList source = (JList)event.getSource();  
		            //deletingCropIndex = source.getSelectedIndex();
		            //deletingCrop = source.getSelectedValue().toString();
					//if (listModel.getSize() > 0) listModel.removeElementAt(deletingCropIndex);
		        }
		    }
		});
		queueListPanel.add(listScrollPanel); 
		list.setOpaque(false);
		list.setBackground(new Color(0, 0, 0, 50));
	
	}
	
	
	
	/**
	 * Sets the style for the table
	 * @param table
	 */
	// 2015-01-01 Added setTableStyle()
	public void setTableStyle(JTable table) {
		
		//JTableHeader header = table.getTableHeader();
    	//TableHeaderRenderer theRenderer =
    	//	new TableHeaderRenderer(header.getDefaultRenderer());
    	//header.setDefaultRenderer(theRenderer);
    	
		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setOpaque(true); // need to be true for setBackground() to work
		headerRenderer.setBackground(new Color(205, 133, 63));//Color.ORANGE);
		headerRenderer.setForeground( Color.WHITE); 
		headerRenderer.setFont( new Font( "Dialog", Font.BOLD, 12 ) );

		for (int i = 0; i < table.getModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}
		MatteBorder border = new MatteBorder(1, 1, 0, 0, Color.orange);
		// set cell to have a light color border
		table.setBorder(border);
		table.setShowGrid(true);
	    table.setShowVerticalLines(true);
		table.setGridColor(new Color(222, 184, 135)); // 222 184 135burlywood
		table.setBorder(BorderFactory.createLineBorder(Color.orange,1)); // HERE  
	

        final JTable ctable = table;
	    SwingUtilities.invokeLater(new Runnable(){
	        public void run()  {
	        	ColumnResizer.adjustColumnPreferredWidths(ctable);	        	
	         } });
		
	}
	
	/**
	 * Selects Crop
	 * @param table
	 */
	public void selectCrop() {
		
		CropType cropType = (CropType) list.getSelectedValue();
		if (cropType != null) {
			deletingCropType = cropType;
			deletingCropIndex = list.getSelectedIndex();
		} else	
		//System.out.println( " deletingCropIndex is " + deletingCropIndex);
        //System.out.println( " deletingCropType is " + deletingCropType);
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
		comboBox.setRenderer(new PromptComboBoxRenderer(" Crops List "));
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
			farmersLabel.setText("Number of Farmers: " + farmersCache);
		}

		// Update crops label if necessary.
		if (cropsCache != farm.getCrops().size()) {
			cropsCache = farm.getCrops().size();
			cropsLabel.setText("Number of Crops: " + cropsCache);
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
	private class ListModel extends AbstractListModel<CropType> {

	    /** default serial id. */
	    private static final long serialVersionUID = 1L;
	    
	    //private Settlement settlement;
	    private List<CropType> list;
	    
	    private ListModel() {
	    	//System.out.println("ListModel constructor"); 
	 
        	List<CropType> c = farm.getCropListInQueue();
	        if (c != null)
	        	list = new ArrayList<CropType>(c);
	        else list = null;
	        //Collections.sort(list);
	    }
	    
        @Override
        public CropType getElementAt(int index) {
        	//System.out.println("ListModel : index is " + index); 
        	//System.out.println("ListModel : list.size() is " + list.size()); 

        	CropType result = null;
            
            if ((index >= 0) && (index < list.size())) {
                result = list.get(index);
            }
            
            return result;
        }

        @Override
        public int getSize() {
         	//System.out.println("ListModel : index is " + index); 
        	//System.out.println("ListModel : list.size() is " + list.size()); 
        	if (list == null)
        		return 0;
        	else return list.size();
        }
        
        /**
         * Update the list model.
         */
        public void update() {
            
        	List<CropType> c = farm.getCropListInQueue(); 
        	
        		//System.out.println("listModel.update() : Size is different. Proceed...");
        		// if the list contains duplicate items, it somehow pass this test
	         
        		if (list.size() != c.size() || !list.containsAll(c) || !c.containsAll(list)) { 
	                List<CropType> oldList = list;
	            	//System.out.println("listModel.update() : oldList.size() is " + oldList.size());          
	                List<CropType> tempList = new ArrayList<CropType>(c);
	                //Collections.sort(tempList);
	                
	             	//System.out.println("ListModel : index is " + index); 
	            	//System.out.println("listModel.update() : tempList.size() is " + tempList.size()); 
	
	                list = tempList;
	                fireContentsChanged(this, 0, getSize());
	                
	                oldList.clear();
	           }
        	
        }
	}

    //public void setCropInQueue(String cropInQueue) {
    //	this.cropInQueue = cropInQueue;
    //}
	/** 
	 * Internal class used as model for the crop table.
	 */
	private static class CropTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private Farming farm;
		private java.util.List<Crop> crops;
		private ImageIcon redDot;
		private ImageIcon yellowDot;
		private ImageIcon greenDot;

		private CropTableModel(Farming farm) {
			this.farm = farm;
			crops = farm.getCrops();
			redDot = ImageLoader.getIcon("RedDot");
			yellowDot = ImageLoader.getIcon("YellowDot");
			greenDot = ImageLoader.getIcon("GreenDot");
		}

		public int getRowCount() {
			return crops.size();
		}

		// 2014-10-10 mkung: change from 4 to 5 in order to include the crop's category as columnIndex 4
		public int getColumnCount() {
			return 5;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = ImageIcon.class;
			else if (columnIndex == 1) dataType = String.class;
			else if (columnIndex == 2) dataType = String.class;
			else if (columnIndex == 3) dataType = String.class;
			// 2014-10-10 mkung: added column 4 showing the crop's category
			else if (columnIndex == 4) dataType = String.class;			
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Health";
			else if (columnIndex == 1) return "Crop";
			else if (columnIndex == 2) return "Phase";
			else if (columnIndex == 3) return "Growth";
			// 2014-10-10 mkung: added column 4 showing the crop's category
			else if (columnIndex == 4) return "Category";			
			else return null;
		}

		public Object getValueAt(int row, int column) {

			Crop crop = crops.get(row);
			String phase = crop.getPhase();
			// 2014-10-10 mkung: added the crop's category
			String category = crop.getCategory();

			if (column == 0) {
				double condition = crop.getCondition();
				if (condition > ((double) 2 / (double) 3)) return greenDot;
				else if (condition > ((double) 1 / (double) 3)) return yellowDot;
				else return redDot;
			}
			else if (column == 1) return crop.getCropType().getName();
			else if (column == 2) return phase;
			else if (column == 3) {
				int growth = 0;
				if (phase.equals(Crop.GROWING)) {
					double growingCompleted = crop.getGrowingTimeCompleted() / crop.getCropType().getGrowingTime();
					growth = (int) (growingCompleted * 100D);
				}
				else if (phase.equals(Crop.HARVESTING) || phase.equals(Crop.FINISHED)) growth = 100;
				return String.valueOf(growth) + "%";
			}
			// 2014-10-10 mkung: added column 4 showing the crop's category
			else if (column == 4) return category;
			else return null;
		}

		public void update() {
			if (!crops.equals(farm.getCrops())) crops = farm.getCrops();
			fireTableDataChanged();
		}
	}


	// 2014-12-09 Added PromptComboBoxRenderer()
	class PromptComboBoxRenderer extends BasicComboBoxRenderer
	{

		private static final long serialVersionUID = 1L;
		private String prompt;

		private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	    // Width doesn't matter as the combo box will size
	    //private Dimension preferredSize = new Dimension(0, 20);

		/*
		 *  Set the text to display when no item has been selected.
		 */
		public PromptComboBoxRenderer(String prompt)
		{
			this.prompt = prompt;
		}

		/*
		 *  Custom rendering to display the prompt text when no item is selected
		 */
		// 2014-12-09 Added color rendering
		public Component getListCellRendererComponent(
			JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			Component c = defaultRenderer.getListCellRendererComponent(
	                list, value, index, isSelected, cellHasFocus);
			
			if (value == null) {
				setText( prompt );
				return this;
			}
			if (c instanceof JLabel) {
	            if (isSelected) {
	                c.setBackground(Color.orange);
	            } else {
	                c.setBackground(Color.white);
	                c.setBackground(new Color(51,25,0,128));
	            }
	        } else {
	        	//c.setBackground(Color.white);
	            c.setBackground(new Color(51,25,0,128));
	            c = super.getListCellRendererComponent(
	                    list, value, index, isSelected, cellHasFocus);
	        }
	        return c;
		}	
	}

}
