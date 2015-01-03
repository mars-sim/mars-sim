/**
 * Mars Simulation Project
 * SettlementTransparentPanel.java
 * @version 3.07 2015-01-01
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.JSliderMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

public class SettlementTransparentPanel  {
	
	private SettlementMapPanel mapPanel;
	private MainDesktopPane desktop;
	//private Settlement settlement;
	private JSliderMW zoomSlider;
	/** Rotation change (radians per rotation button press). */
	private static final double ROTATION_CHANGE = Math.PI / 20D;
	/** Zoom change. */
	private static final double ZOOM_CHANGE = 1D;
	JPanel topPanel, rightPane, borderPane, nameBtnPane, namePane, zoomPane, labelPane, buttonPane, controlPane, settlementPanel, infoP, renameP ; 
	JButton renameBtn, infoButton;
	JLabel zoomLabel; 
	/** Lists all settlements. */
	JComboBoxMW<?> settlementListBox;
	/** Combo box model. */
	SettlementComboBoxModel settlementCBModel;
	private JPopupMenu labelsMenu;
	
    public SettlementTransparentPanel(MainDesktopPane desktop, SettlementMapPanel mapPanel) {
    	
        this.mapPanel = mapPanel;
        this.desktop = desktop;
        createAndShowGUI();
    }
    
    
    public void createAndShowGUI()	   {   
    		        
    	//mapPanel.setLayout(new BorderLayout());

        buildSettlementNameComboBox();
        buildInfoP(); 
        buildrenameBtn();
        
        buildZoomLabel();	
        buildZoomSlider();
        buildButtonPane();    
        buildLabelPane();

		
		namePane = new JPanel(new BorderLayout());
		namePane.setBackground(new Color(0,0,0,0));
        namePane.setOpaque(false);
  		
		nameBtnPane = new JPanel(new FlowLayout());
		nameBtnPane.setBackground(new Color(0,0,0,0));
        nameBtnPane.setOpaque(false);
        
      	nameBtnPane.add(infoP);
       	nameBtnPane.add(renameP);
       	nameBtnPane.add(new JLabel(""));
		
		settlementPanel = new JPanel();//new BorderLayout());
		settlementPanel.setBackground(new Color(0,0,0,0));
		settlementPanel.add(settlementListBox);//, BorderLayout.CENTER);
		
       	namePane.add(nameBtnPane, BorderLayout.CENTER);
    	namePane.add(settlementPanel, BorderLayout.NORTH);

		topPanel = new JPanel();//new BorderLayout());
		topPanel.setBackground(new Color(0,0,0,15));
		topPanel.add(namePane);//, BorderLayout.CENTER);
		
        // Make panel drag-able
    	ComponentMover cmName = new ComponentMover();
    	cmName.registerComponent(topPanel);
		
	    borderPane = new JPanel(new BorderLayout());//new GridLayout(2,1,2,2));
	    borderPane.setBackground(new Color(0,0,0,0));
	    controlPane = new JPanel(new GridLayout(2,1,2,2));
	    controlPane.setBackground(new Color(0,0,0,0));
	    zoomPane = new JPanel(new GridLayout(3,1,2,2));
		zoomPane.setBackground(new Color(0,0,0,15));
	    rightPane = new JPanel(new BorderLayout());
		rightPane.setBackground(new Color(0,0,0,15));
        // Make panel drag-able
  		ComponentMover cmZoom = new ComponentMover();
		cmZoom.registerComponent(rightPane);
		
	    controlPane.add(buttonPane);
	    controlPane.add(zoomLabel);
	    borderPane.add(controlPane, BorderLayout.SOUTH);
	    
        zoomPane.add(borderPane); 
        zoomPane.add(zoomSlider);
        zoomPane.add(labelPane); 
        
        rightPane.add(zoomPane, BorderLayout.CENTER);
        
        mapPanel.add(topPanel, BorderLayout.NORTH);   
        mapPanel.add(rightPane, BorderLayout.EAST);      
        mapPanel.setVisible(true);
    }
     
    
	@SuppressWarnings("unchecked")
	public void buildSettlementNameComboBox() { 
		
		settlementCBModel = new SettlementComboBoxModel();
		settlementListBox = new JComboBoxMW(settlementCBModel);
		//settlementListBox.setBorder(null);
		settlementListBox.setOpaque(false);//setBackground(new Color(139,69,19));
		//settlementListBox.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
		//((JLabel)settlementListBox.getRenderer()).setBackground(Color.darkGray);;//SwingConstants.CENTER);
		settlementListBox.setBackground(new Color(51,25,0,128));
		settlementListBox.setFont(new Font("Dialog", Font.BOLD, 18));
		settlementListBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$ 
		settlementListBox.setRenderer(new PromptComboBoxRenderer());
		settlementListBox.addItemListener(new ItemListener() {
			@Override
			// unitUpdate will update combobox when a new building is added
			public void itemStateChanged(ItemEvent event) {
				Settlement s;
				// 2014-12-19 Added if else clause for selecting the settlement that the new building is arriving 
				if (desktop.getIsTransportingBuilding()) {
					s = desktop.getSettlement();
					settlementListBox.setSelectedItem(s);
					//settlementListBox.setForeground(Color.green);
				}
				else {
					s = (Settlement) event.getItem();
				}
				//System.out.println(" settlement is " + settlement.getName());
				mapPanel.setSettlement(s);
			} 
		});

		
		if (settlementListBox.getModel().getSize() > 0) {
			settlementListBox.setSelectedIndex(0);
			Settlement s;
			// 2014-12-19 Added if else clause for selecting the settlement that the new building is arriving 
			if (desktop.getIsTransportingBuilding()) {
				s = desktop.getSettlement();
				settlementListBox.setSelectedItem(s);
				//settlementListBox.setForeground(Color.green);
			}
			else {
				s = (Settlement) settlementListBox.getSelectedItem();
			}
			//System.out.println(" settlement is " + settlement.getName());
			mapPanel.setSettlement(s);
		}
    
	}
    /*
     * settlement = (Settlement) settlementListBox.getSelectedItem();
	public void makeTransparent(Component[] comp)  
	  {  
	    for(int x = 0; x < comp.length; x++)  
	    {  
	      if(comp[x] instanceof javax.swing.plaf.metal.MetalComboBoxButton)  
	      {  
	        ((javax.swing.plaf.metal.MetalComboBoxButton)comp[x]).setOpaque(false);  
	        ((javax.swing.plaf.metal.MetalComboBoxButton)comp[x]).setBorder(null);  
	      }  
	      else if(comp[x] instanceof JTextField)  
	      {  
	        ((JTextField)comp[x]).setOpaque(false);  
	        ((JTextField)comp[x]).setBorder(null);  
	      }  
	    }  
	  }  
	*/
	   
	class PromptComboBoxRenderer extends BasicComboBoxRenderer {	
			
		private static final long serialVersionUID = 1L;
		private String prompt;		
		//public boolean isOptimizedDrawingEnabled();
		//private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		public PromptComboBoxRenderer(){	
			//defaultRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		    //settlementListBox.setRenderer(defaultRenderer);		
		    //setOpaque(false);
		    setHorizontalAlignment(CENTER);
		    setVerticalAlignment(CENTER);
		}
		
		public PromptComboBoxRenderer(String prompt){
				this.prompt = prompt;
			}
			
			@Override
		    public Component getListCellRendererComponent(JList list, Object value,
		            int index, boolean isSelected, boolean cellHasFocus) {
		        JComponent result = (JComponent)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        //Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        //component.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);		        
				if (value == null) {
					setText( prompt );
					//this.setForeground(Color.green);
			        //this.setBackground(new Color(184,134,11));
					return this;
				}
				
				if (isSelected) {
					result.setForeground(Color.green);
			        result.setBackground(new Color(184,134,11));
		
		          // unselected, and not the DnD drop location
		          } else {
		        	  result.setForeground(Color.green);
		        	  result.setBackground(new Color(255,229,204));
				      //result.setBackground(new Color(184,134,11)); //brown
		          }
				
		        //result.setOpaque(false);

		        return result;
		    }
	}

	
    public void buildZoomLabel() { 

		zoomLabel = new JLabel(Msg.getString("SettlementTransparentPanel.label.zoom")); //$NON-NLS-1$
		//zoomLabel.setPreferredSize(new Dimension(60, 20));
		zoomLabel.setFont(new Font("Dialog", Font.BOLD, 14));
		zoomLabel.setForeground(Color.GREEN);
		//zoomLabel.setContentAreaFilled(false);
		zoomLabel.setOpaque(false);
		zoomLabel.setVerticalAlignment(JLabel.BOTTOM);
		zoomLabel.setHorizontalAlignment(JLabel.CENTER);
		//zoomLabel.setBorder(new LineBorder(Color.green, 1, true));
		//zoomLabel.setBorderPainted(true);
		zoomLabel.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
		
    }
    
    public void buildZoomSlider() { 
		
		zoomSlider = new JSliderMW(JSlider.VERTICAL, -10, 10, 0);
		zoomSlider.setMajorTickSpacing(5);
		zoomSlider.setMinorTickSpacing(1);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setForeground(Color.GREEN);
		zoomSlider.setBackground(new Color(0,0,0,15));
		//zoomSlider.setContentAreaFilled(false);
		zoomSlider.setOpaque(false);
		zoomSlider.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				// Change scale of map based on slider position.
				int sliderValue = zoomSlider.getValue();
				double defaultScale = SettlementMapPanel.DEFAULT_SCALE;
				double newScale = defaultScale;
				if (sliderValue > 0) {
					newScale += defaultScale * (double) sliderValue * ZOOM_CHANGE;
				}
				else if (sliderValue < 0) {
					newScale = defaultScale / (1D + ((double) sliderValue * -1D * ZOOM_CHANGE));
				}
				mapPanel.setScale(newScale);
			}
		});
		
		//zoomPane.add(zoomSlider);		
        
		// Add mouse wheel listener for zooming.
		mapPanel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent evt) {
				int numClicks = evt.getWheelRotation();
				if (numClicks > 0) {
					// Move zoom slider down.
					if (zoomSlider.getValue() > zoomSlider.getMinimum()) 
						zoomSlider.setValue(zoomSlider.getValue() - 1);
				}
				else if (numClicks < 0) {
					// Move zoom slider up.
					if (zoomSlider.getValue() < zoomSlider.getMaximum()) 
						zoomSlider.setValue(zoomSlider.getValue() + 1);
				}
			}
		});
		
    }
    
    public void buildInfoP() {
       
		infoP = new JPanel(new FlowLayout());
		infoP.setBackground(new Color(0,0,0,0));
		infoP.setAlignmentX(FlowLayout.CENTER); 
		
		infoButton = new JButton(Msg.getString("SettlementTransparentPanel.button.info")); //$NON-NLS-1$
		infoButton.setPreferredSize(new Dimension(35, 20));
		infoButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		infoButton.setForeground(Color.GREEN);
		infoButton.setContentAreaFilled(false);
		//infoButton.setOpaque(false); // text disappeared if setOpaque(false)
		infoButton.setBorder(new LineBorder(Color.green, 1, true));
		//infoButton.setBorderPainted(true);
		infoButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.info")); //$NON-NLS-1$
		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Settlement settlement = mapPanel.getSettlement();				
				if (settlement != null) {
					// 2014-10-26 obtained settlement object
					//setCurrentSettlement(settlement);					
					desktop.openUnitWindow(settlement, false);
				}
			}
		});
		infoP.add(infoButton);
    }

		
		/*
		nameLabel = new JLabel(nameCache);//Msg.getString("SettlementWindow.button.info")); //$NON-NLS-1$
		if (settlement != null) 
			name = settlement.getName();
		else if (mapPanel.getSettlement() != null) 
			name = mapPanel.getSettlement().getName();
		// or settlementWindow.getSettlement().getName();
		if (!name.equals(nameCache)) {
			nameCache = name;
			nameLabel.setText(nameCache);
		}
		//nameLabel.setPreferredSize(new Dimension(60, 20));
		nameLabel.setFont(new Font("Serif", Font.BOLD, 20));
		nameLabel.setForeground(Color.GREEN);
		//nameLabel.setContentAreaFilled(false);
		nameLabel.setOpaque(false);
		//nameLabel.setBorder(new LineBorder(Color.green, 1, true));
		//nameLabel.setBorderPainted(true);
		nameLabel.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.info")); //$NON-NLS-1$

		JPanel nameP = new JPanel(new FlowLayout());
		nameP.setBackground(new Color(0,0,0,0));
		nameP.add(nameLabel);
		nameP.setAlignmentX(FlowLayout.CENTER); 
		namePane.add(nameP);
		*/

		
    public void buildrenameBtn() {
	    	
		renameP  = new JPanel(new FlowLayout());
		renameP.setBackground(new Color(0,0,0,0));
		renameP.setAlignmentX(FlowLayout.CENTER); 

		renameBtn = new JButton(Msg.getString("SettlementTransparentPanel.button.rename")); //$NON-NLS-1$
		renameBtn.setPreferredSize(new Dimension(65, 20));
		renameBtn.setFont(new Font("Dialog", Font.PLAIN, 12));
		renameBtn.setForeground(Color.GREEN);
		renameBtn.setContentAreaFilled(false);
		//renameBtn.setOpaque(false); // text disappeared if setOpaque(false)
		renameBtn.setBorder(new LineBorder(Color.green, 1, true));
		infoButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.rename")); //$NON-NLS-1$
		renameBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
				mapPanel.getSettlementWindow().renameSettlement();
			}
		});
		renameP.add(renameBtn);
    }
    
    
    public void buildButtonPane() {
  		
        buttonPane = new JPanel();
        buttonPane.setBackground(new Color(0,0,0,0));
		JButton rotateClockwiseButton = new JButton(ImageLoader.getIcon(Msg.getString("img.clockwise"))); //$NON-NLS-1$
		rotateClockwiseButton.setPreferredSize(new Dimension(20, 20));
		rotateClockwiseButton.setOpaque(false);
		rotateClockwiseButton.setForeground(Color.GREEN);
		rotateClockwiseButton.setBorder(new LineBorder(Color.GREEN, 1, true));
		rotateClockwiseButton.setContentAreaFilled(false);
		rotateClockwiseButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.clockwise")); //$NON-NLS-1$
		rotateClockwiseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.setRotation(mapPanel.getRotation() + ROTATION_CHANGE);
			}
		});

		buttonPane.add(rotateClockwiseButton);
	
		JButton recenterButton = new JButton(" + ");//Msg.getString("SettlementTransparentPanel.button.recenter")); //$NON-NLS-1$
		recenterButton.setPreferredSize(new Dimension(20, 20));
		recenterButton.setOpaque(false);
		recenterButton.setForeground(Color.BLUE);
		recenterButton.setBorder(new LineBorder(Color.GREEN, 1, true));
		recenterButton.setContentAreaFilled(false);
		recenterButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.recenter")); //$NON-NLS-1$
		recenterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.reCenter();
				zoomSlider.setValue(0);
			}
		});

		buttonPane.add(recenterButton);

		// Create rotate counter-clockwise button.
		JButton rotateCounterClockwiseButton = new JButton(ImageLoader.getIcon(Msg.getString("img.counterClockwise"))); //$NON-NLS-1$
		rotateCounterClockwiseButton.setPreferredSize(new Dimension(20, 20));
		rotateCounterClockwiseButton.setOpaque(false);
		rotateCounterClockwiseButton.setForeground(Color.GREEN);
		rotateCounterClockwiseButton.setContentAreaFilled(false);
		rotateCounterClockwiseButton.setBorder(new LineBorder(Color.GREEN, 1, true));
		rotateCounterClockwiseButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise")); //$NON-NLS-1$
		rotateCounterClockwiseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.setRotation(mapPanel.getRotation() - ROTATION_CHANGE);
			}
		});

		buttonPane.add(rotateCounterClockwiseButton);
		
    }
    
    public void buildLabelPane() {

        labelPane = new JPanel();
        labelPane.setBackground(new Color(0,0,0,0));      
        
		JButton labelsButton = new JButton(Msg.getString("SettlementTransparentPanel.button.labels")); //$NON-NLS-1$
		labelsButton.setOpaque(false);
		//labelsButton.setFont(new Font("Dialog", Font.BOLD, 16));
		//labelsButton.setBackground(new Color(139,69,19)); // (139,69,19) is brown
		labelsButton.setBackground(new Color(0,0,0,0)); 
		labelsButton.setPreferredSize(new Dimension(80, 20));
		labelsButton.setForeground(Color.green);
		labelsButton.setVerticalAlignment(JLabel.TOP);
		labelsButton.setHorizontalAlignment(JLabel.CENTER);
		//labelsButton.setContentAreaFilled(false);
		labelsButton.setBorder(new LineBorder(Color.GREEN, 1, true));
		labelsButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.labels")); //$NON-NLS-1$
		labelsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JButton button = (JButton) evt.getSource();
				if (labelsMenu == null) {
					labelsMenu = mapPanel.getSettlementWindow().createLabelsMenu();
				}
				labelsMenu.show(button, 0, button.getHeight());
			}
		});
		
		labelPane.add(labelsButton);	
   
	}
    
    class MyCellRenderer extends JLabel implements ListCellRenderer<Object>  {

		private static final long serialVersionUID = 1L;
	
		public MyCellRenderer() {
	          setOpaque(true);
	      }
	
	      public Component getListCellRendererComponent(JList<?> list,
	                                                    Object value,
	                                                    int index,
	                                                    boolean isSelected,
	                                                    boolean cellHasFocus) {
	
	          setText(value.toString());
	  		  setBackground(new Color(0,0,0,0)); 
	  		  
	          Color background = Color.orange;
	          Color foreground = Color.green;
	
	          // check if this cell represents the current DnD drop location
	          JList.DropLocation dropLocation = list.getDropLocation();
	
	          if (dropLocation != null
	                  && !dropLocation.isInsert()
	                  && dropLocation.getIndex() == index) {
	
	
	
	          // check if this cell is selected
	          } else if (isSelected) {
	              background = Color.orange;
	              foreground = Color.green;
	
	          // unselected, and not the DnD drop location
	          } else {
	          };
	
	          setBackground(background);
	          setForeground(foreground);
	
	          return this;
	      }
    }


	/**
	 * Inner class combo box model for settlements.
	 */
	public class SettlementComboBoxModel
	extends DefaultComboBoxModel<Object>
	implements 
	UnitManagerListener,
	UnitListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 */
		public SettlementComboBoxModel() {
			// User DefaultComboBoxModel constructor.
			super();
			// Initialize settlement list.
			updateSettlements();
			// Add this as a unit manager listener.
			UnitManager unitManager = Simulation.instance().getUnitManager();
			unitManager.addUnitManagerListener(this);
			/*
			// 2014-12-19 Added addUnitListener
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().addUnitListener(this);			
			}
			*/
		}

		/**
		 * Update the list of settlements.
		 */
		private void updateSettlements() {

			removeAllElements();
			UnitManager unitManager = Simulation.instance().getUnitManager();
			List<Settlement> settlements = new ArrayList<Settlement>(unitManager.getSettlements());
			Collections.sort(settlements);

			Iterator<Settlement> i = settlements.iterator();
			while (i.hasNext()) {
				addElement(i.next());
			}
		}

		@Override
		public void unitManagerUpdate(UnitManagerEvent event) {
			if (event.getUnit() instanceof Settlement) {
				updateSettlements();
			}
		}
		
		//2014-12-19 Added unitUpdate()
		public void unitUpdate(UnitEvent event) {	
			// Note: Easily 100+ UnitEvent calls every second
			UnitEventType eventType = event.getType();
			if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
				Object target = event.getTarget();
				Building building = (Building) target; // overwrite the dummy building object made by the constructor
				BuildingManager mgr = building.getBuildingManager();
				Settlement s = mgr.getSettlement();
				mapPanel.setSettlement(s);
				// Updated ComboBox
				settlementListBox.setSelectedItem(s);
				//System.out.println("SettlementWindow : The settlement the new building is transporting to is " + settlement);
				// Select the relevant settlement
				//this.pack();
			}
		}
		
		/**
		 * Prepare class for deletion.
		 */
		public void destroy() {
			removeAllElements();
			UnitManager unitManager = Simulation.instance().getUnitManager();
			unitManager.removeUnitManagerListener(this);
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().removeUnitListener(this);			
			}
			
		}
	}
    
}
 