/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 3.00 2010-08-22
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;


/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
public class SettlementWindow extends ToolWindow {
	
    // Tool name
    public static final String NAME = "Settlement Map Tool";

    private JComboBox settlementListBox; // Lists all settlements
    private JLabel zoomLabel; // Label for Zoom box
    private JSlider zoomSlider; // Slider for Zoom level
    private SettlementMapPanel mapPane; // Map panel.
    
    /**
     * Constructor
     * @param desktop the main desktop panel.
     */
	public SettlementWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);

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
		mainPane.add(mapPane, BorderLayout.CENTER);

		Object settlements[] = Simulation.instance().getUnitManager()
				.getSettlements().toArray();
		Arrays.sort(settlements);

		settlementListBox = new JComboBox(settlements);
		settlementListBox.setToolTipText("Select settlement");
		settlementListBox.setSelectedIndex(0);
		settlementListBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                Settlement settlement = (Settlement) event.getItem();
                mapPane.setSettlement(settlement);
            } 
		});
		widgetPane.add(settlementListBox);
		mapPane.setSettlement((Settlement) settlementListBox.getSelectedItem());

		// Create zoom label and slider
		JPanel zoomPane = new JPanel(new BorderLayout());
		zoomLabel = new JLabel("Zoom", JLabel.CENTER);
		zoomPane.add(zoomLabel, BorderLayout.NORTH);

		zoomSlider = new JSlider(JSlider.HORIZONTAL, -10, 10, 0);
		zoomSlider.setMajorTickSpacing(5);
		zoomSlider.setMinorTickSpacing(1);
		//zoomSlider.setPaintTicks(true);
		//zoomSlider.setPaintLabels(true);
		zoomSlider.setToolTipText("Zoom view of settlement");
		zoomSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                int sliderValue = zoomSlider.getValue();
                double defaultScale = SettlementMapPanel.DEFAULT_SCALE;
                double newScale = defaultScale;
                if (sliderValue > 0) newScale += defaultScale * (double) sliderValue;
                else if (sliderValue < 0) newScale = defaultScale / 
                        (1D + ((double) sliderValue * -1D));
                mapPane.setScale(newScale);
            }
		});
		zoomPane.add(zoomSlider, BorderLayout.CENTER);

	    // Add mouse wheel listener for zooming.
        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent evt) {
                int numClicks = evt.getWheelRotation();
                if (numClicks > 0) {
                    if (zoomSlider.getValue() < zoomSlider.getMaximum()) 
                        zoomSlider.setValue(zoomSlider.getValue() + 1);
                }
                else if (numClicks < 0) {
                    if (zoomSlider.getValue() > zoomSlider.getMinimum()) 
                        zoomSlider.setValue(zoomSlider.getValue() - 1);
                }
            }
        });
		
		widgetPane.add(zoomPane);

		JPanel buttonsPane = new JPanel();
		widgetPane.add(buttonsPane);
		
		JButton rotateClockwiseButton = new JButton(ImageLoader.getIcon("Clockwise"));
		rotateClockwiseButton.setToolTipText("Rotate map clockwise");
		rotateClockwiseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                mapPane.setRotation(mapPane.getRotation() + (Math.PI / 20D));
            }
		});
		buttonsPane.add(rotateClockwiseButton);
		
		JButton rotateCounterClockwiseButton = new JButton(ImageLoader.getIcon("CounterClockwise"));
        rotateCounterClockwiseButton.setToolTipText("Rotate map counter-clockwise");
        rotateCounterClockwiseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                mapPane.setRotation(mapPane.getRotation() - (Math.PI / 20D));
            }
        });
        buttonsPane.add(rotateCounterClockwiseButton);
        
		JButton recenterButton = new JButton("Recenter");
		recenterButton.setToolTipText("Recenter view to center, normal zoom");
		buttonsPane.add(recenterButton);
		
		JButton labelsButton = new JButton("Labels");
		labelsButton.setToolTipText("Add/remove label overlays");
		buttonsPane.add(labelsButton);
		
		JButton openInfoButton = new JButton("Open Info");
		openInfoButton.setToolTipText("Opens the Settlement Info Window");
		buttonsPane.add(openInfoButton);
		
		// Pack window.
		pack();
	}
}