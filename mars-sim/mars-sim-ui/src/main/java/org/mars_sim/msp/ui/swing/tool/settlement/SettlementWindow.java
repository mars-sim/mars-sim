/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 3.03 2012-12-10
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;


/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
public class SettlementWindow extends ToolWindow {
	
    // Tool name
    public static final String NAME = "Settlement Map Tool";
    
    // Rotation change (radians per rotation button press).
    private static final double ROTATION_CHANGE = Math.PI / 20D;
    
    // Zoom change.
    private static final double ZOOM_CHANGE = 1D;

    private JComboBox settlementListBox; // Lists all settlements
    private JLabel zoomLabel; // Label for Zoom box
    private JSlider zoomSlider; // Slider for Zoom level
    private SettlementMapPanel mapPane; // Map panel.
    private int xLast; // Last X mouse drag position.
    private int yLast; // Last Y mouse drag position.
    private JPopupMenu labelsMenu; // Popup menu for label display options.
    
    /**
     * Constructor
     * @param desktop the main desktop panel.
     */
	public SettlementWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);
		
		// Set the tool window to be maximizable.
		setMaximizable(true);

		// Create content pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create top widget pane
		JPanel widgetPane = new JPanel();
		widgetPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPane.add(widgetPane, BorderLayout.NORTH);

		// Create bottom (map) pane
		mapPane = new SettlementMapPanel();
		mapPane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		mapPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                // Set initial mouse drag position.
                xLast = evt.getX();
                yLast = evt.getY();
            }
		});
		mapPane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                // Move map center based on mouse drag difference.
                double xDiff = evt.getX() - xLast;
                double yDiff = evt.getY() - yLast;
                mapPane.moveCenter(xDiff, yDiff);
                xLast = evt.getX();
                yLast = evt.getY();
            }
		});
		mainPane.add(mapPane, BorderLayout.CENTER);

		Object settlements[] = Simulation.instance().getUnitManager()
				.getSettlements().toArray();
		Arrays.sort(settlements);

		settlementListBox = new JComboBox(settlements);
		settlementListBox.setToolTipText("Select settlement");
		settlementListBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                // Set settlement to draw map for.
                Settlement settlement = (Settlement) event.getItem();
                mapPane.setSettlement(settlement);
                // Note: should we recenter map each time we change settlements?
            } 
		});
		widgetPane.add(settlementListBox);
		
		if (settlementListBox.getModel().getSize() > 0) {
            settlementListBox.setSelectedIndex(0);
            mapPane.setSettlement((Settlement) settlementListBox.getSelectedItem());
        }

		// Create zoom label and slider
		JPanel zoomPane = new JPanel(new BorderLayout());
		zoomLabel = new JLabel("Zoom", JLabel.CENTER);
		zoomPane.add(zoomLabel, BorderLayout.NORTH);

		zoomSlider = new JSlider(JSlider.HORIZONTAL, -10, 10, 0);
		zoomSlider.setMajorTickSpacing(5);
		zoomSlider.setMinorTickSpacing(1);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setToolTipText("Zoom view of settlement");
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
                mapPane.setScale(newScale);
            }
		});
		zoomPane.add(zoomSlider, BorderLayout.CENTER);

	    // Add mouse wheel listener for zooming.
        addMouseWheelListener(new MouseWheelListener() {
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
		
		widgetPane.add(zoomPane);

		// Create buttons panel.
		JPanel buttonsPane = new JPanel();
		widgetPane.add(buttonsPane);
		
		// Create rotate clockwise button.
		JButton rotateClockwiseButton = new JButton(ImageLoader.getIcon("Clockwise"));
		rotateClockwiseButton.setToolTipText("Rotate map clockwise");
		rotateClockwiseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mapPane.setRotation(mapPane.getRotation() + ROTATION_CHANGE);
            }
		});
		buttonsPane.add(rotateClockwiseButton);
		
		// Create rotate counter-clockwise button.
		JButton rotateCounterClockwiseButton = new JButton(ImageLoader.getIcon("CounterClockwise"));
        rotateCounterClockwiseButton.setToolTipText("Rotate map counter-clockwise");
        rotateCounterClockwiseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mapPane.setRotation(mapPane.getRotation() - ROTATION_CHANGE);
            }
        });
        buttonsPane.add(rotateCounterClockwiseButton);
        
        // Create recenter button.
		JButton recenterButton = new JButton("Recenter");
		recenterButton.setToolTipText("Recenter view to center, normal zoom");
		recenterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mapPane.reCenter();
                zoomSlider.setValue(0);
            }
        });
		buttonsPane.add(recenterButton);
		
		// Create labels button.
		JButton labelsButton = new JButton("Labels");
		labelsButton.setToolTipText("Add/remove label overlays");
		labelsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JButton button = (JButton) evt.getSource();
                if (labelsMenu == null) labelsMenu = createLabelsMenu();
                labelsMenu.show(button, 0, button.getHeight());
                //mapPane.setShowLabels(button.isSelected());
            }
        });
		buttonsPane.add(labelsButton);
		
		// Create open info button.
		JButton openInfoButton = new JButton("Open Info");
		openInfoButton.setToolTipText("Opens the Settlement Info Window");
		openInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Settlement settlement = mapPane.getSettlement();
                if (settlement != null) {
                    getDesktop().openUnitWindow(settlement, false);
                }
            }
        });
		buttonsPane.add(openInfoButton);
		
		// Pack window.
		pack();
	}
	
	/**
	 * Create the labels popup menu.
	 * @return popup menu.
	 */
	private JPopupMenu createLabelsMenu() {
	   JPopupMenu result = new JPopupMenu("Label Display Options:");
	   
	   // Create building label menu item.
	   JCheckBoxMenuItem buildingLabelMenuItem = new JCheckBoxMenuItem(
	           "Buildings", getMapPanel().isShowBuildingLabels());
	   buildingLabelMenuItem.addActionListener(new ActionListener() {
	       public void actionPerformed(ActionEvent arg0) {
	           getMapPanel().setShowBuildingLabels(!getMapPanel().isShowBuildingLabels());
	       }
	   });
	   result.add(buildingLabelMenuItem);
	   
	   // Create construction/salvage label menu item.
	   JCheckBoxMenuItem constructionLabelMenuItem = new JCheckBoxMenuItem(
	           "Construction Sites", getMapPanel().isShowConstructionLabels());
	   constructionLabelMenuItem.addActionListener(new ActionListener() {
	       public void actionPerformed(ActionEvent arg0) {
	            getMapPanel().setShowConstructionLabels(!getMapPanel().isShowConstructionLabels());
	        }
	   });
	   result.add(constructionLabelMenuItem);
	   
	   // Create vehicle label menu item.
	   JCheckBoxMenuItem vehicleLabelMenuItem = new JCheckBoxMenuItem(
	           "Vehicles", getMapPanel().isShowVehicleLabels());
	   vehicleLabelMenuItem.addActionListener(new ActionListener() {
	       public void actionPerformed(ActionEvent arg0) {
	            getMapPanel().setShowVehicleLabels(!getMapPanel().isShowVehicleLabels());
	        }
	   });
	   result.add(vehicleLabelMenuItem);
	   
	   // Create person label menu item.
	   JCheckBoxMenuItem personLabelMenuItem = new JCheckBoxMenuItem(
	           "People", getMapPanel().isShowPersonLabels());
	   personLabelMenuItem.addActionListener(new ActionListener() {
	       public void actionPerformed(ActionEvent arg0) {
               getMapPanel().setShowPersonLabels(!getMapPanel().isShowPersonLabels());
           }
	   });
	   result.add(personLabelMenuItem);
	   
	   result.pack();
	   
	   return result;
	}
	
	/**
	 * Gets the settlement map panel.
	 * @return the settlement map panel.
	 */
	private SettlementMapPanel getMapPanel() {
	    return mapPane;
	}
	
	/**
	 * Gets the main desktop panel for this tool.
	 * @return main desktop panel.
	 */
	private MainDesktopPane getDesktop() {
	    return desktop;
	}
	
	@Override
	public void destroy() {
	    mapPane.destroy();
	}
}