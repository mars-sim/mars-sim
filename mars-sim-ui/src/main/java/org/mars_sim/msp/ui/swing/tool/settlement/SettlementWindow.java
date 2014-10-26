/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 3.06 2014-10-26
 * @author Lars Naesbye Christensen
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.JSliderMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
public class SettlementWindow extends ToolWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = Msg.getString("SettlementWindow.title"); //$NON-NLS-1$

	//private JLabel settlementNameLabel;
	private String settlementNewName;
	private JButton settlementNameChangeBtn;
	private Settlement settlement;
	/** The main desktop. */
	private MainDesktopPane desktop;
	
	/** Rotation change (radians per rotation button press). */
	private static final double ROTATION_CHANGE = Math.PI / 20D;

	/** Zoom change. */
	private static final double ZOOM_CHANGE = 1D;

	/** Lists all settlements. */
	private JComboBoxMW<?> settlementListBox;
	/** Combo box model. */
	private SettlementComboBoxModel settlementCBModel;
	/** Label for Zoom box. */
	private JLabel zoomLabel;
	/** Slider for Zoom level. */
	private JSliderMW zoomSlider;
	/** Map panel. */
	private SettlementMapPanel mapPane;
	/** Last X mouse drag position. */
	private int xLast;
	/** Last Y mouse drag position. */
	private int yLast;
	/** Popup menu for label display options. */
	private JPopupMenu labelsMenu;

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public SettlementWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);

		this.desktop = desktop;
		
		// Set the tool window to be maximizable.
		setMaximizable(true);

		// Create content pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create top widget pane
		JPanel widgetPane = new JPanel();
		widgetPane.setBorder(MainDesktopPane.newEmptyBorder());
		mainPane.add(widgetPane, BorderLayout.NORTH);

		// Create bottom (map) pane
		mapPane = new SettlementMapPanel();
		mapPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Set initial mouse drag position.
				xLast = evt.getX();
				yLast = evt.getY();
			}

			@Override
			public void mouseClicked(MouseEvent evt) {

				// Select person if clicked on.
				mapPane.selectPersonAt(evt.getX(), evt.getY());
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

		settlementCBModel = new SettlementComboBoxModel();
		settlementListBox = new JComboBoxMW(settlementCBModel);
		settlementListBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		settlementListBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				// Set settlement to draw map for.
				Settlement settlement = (Settlement) event.getItem();
				mapPane.setSettlement(settlement);
				// 2014-10-26 mkung: obtained settlement object
				setCurrentSettlement(settlement);
				
				// Note: should we recenter map each time we change settlements?
			} 
		});
		widgetPane.add(settlementListBox);

		if (settlementListBox.getModel().getSize() > 0) {
			settlementListBox.setSelectedIndex(0);
			settlement = (Settlement) settlementListBox.getSelectedItem();
			// 2014-10-25 mkung: obtained settlement object
			mapPane.setSettlement(settlement);
			setCurrentSettlement(settlement);
				//System.out.println("settlement is "+ settlement);
		}

		// 2014-10-25 mkung: Added Rename button for settlement name change
		settlementNameChangeBtn = new JButton("Rename");
		settlementNameChangeBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
				renameSettlement();
			}
		});
		widgetPane.add(settlementNameChangeBtn);
	
		
		
		// Create zoom label and slider
		JPanel zoomPane = new JPanel(new BorderLayout());
		zoomLabel = new JLabel(Msg.getString("SettlementWindow.zoom"), JLabel.CENTER); //$NON-NLS-1$
		zoomPane.add(zoomLabel, BorderLayout.NORTH);

		zoomSlider = new JSliderMW(JSlider.HORIZONTAL, -10, 10, 0);
		zoomSlider.setMajorTickSpacing(5);
		zoomSlider.setMinorTickSpacing(1);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setToolTipText(Msg.getString("SettlementWindow.tooltip.zoom")); //$NON-NLS-1$
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
		JButton rotateClockwiseButton = new JButton(ImageLoader.getIcon(Msg.getString("img.clockwise"))); //$NON-NLS-1$
		rotateClockwiseButton.setToolTipText(Msg.getString("SettlementWindow.tooltip.clockwise")); //$NON-NLS-1$
		rotateClockwiseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPane.setRotation(mapPane.getRotation() + ROTATION_CHANGE);
			}
		});
		buttonsPane.add(rotateClockwiseButton);

		// Create rotate counter-clockwise button.
		JButton rotateCounterClockwiseButton = new JButton(ImageLoader.getIcon(Msg.getString("img.counterClockwise"))); //$NON-NLS-1$
		rotateCounterClockwiseButton.setToolTipText(Msg.getString("SettlementWindow.tooltip.counterClockwise")); //$NON-NLS-1$
		rotateCounterClockwiseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPane.setRotation(mapPane.getRotation() - ROTATION_CHANGE);
			}
		});
		buttonsPane.add(rotateCounterClockwiseButton);

		// Create recenter button.
		JButton recenterButton = new JButton(Msg.getString("SettlementWindow.button.recenter")); //$NON-NLS-1$
		recenterButton.setToolTipText(Msg.getString("SettlementWindow.tooltip.recenter")); //$NON-NLS-1$
		recenterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPane.reCenter();
				zoomSlider.setValue(0);
			}
		});
		buttonsPane.add(recenterButton);

		// Create labels button.
		JButton labelsButton = new JButton(Msg.getString("SettlementWindow.button.labels")); //$NON-NLS-1$
		labelsButton.setToolTipText(Msg.getString("SettlementWindow.tooltip.labels")); //$NON-NLS-1$
		labelsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JButton button = (JButton) evt.getSource();
				if (labelsMenu == null) {
					labelsMenu = createLabelsMenu();
				}
				labelsMenu.show(button, 0, button.getHeight());
			}
		});
		buttonsPane.add(labelsButton);

		// Create open info button.
		JButton openInfoButton = new JButton(Msg.getString("SettlementWindow.button.info")); //$NON-NLS-1$
		openInfoButton.setToolTipText(Msg.getString("SettlementWindow.tooltip.info")); //$NON-NLS-1$
		openInfoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Settlement settlement = mapPane.getSettlement();
				
				if (settlement != null) {
					// 2014-10-26 mkung: obtained settlement object
					setCurrentSettlement(settlement);
					
					getDesktop().openUnitWindow(settlement, false);
				}
			}
		});
		buttonsPane.add(openInfoButton);

		// Pack window.
		pack();
	}
	/**
	 * Ask for a new Settlement name
	 * @return pop up jDialog
	 */
	// 2014-10-26 mkung: Added askNameDialog()
	public String askNameDialog() {
		return JOptionPane
			.showInputDialog(desktop, 
					Msg.getString("SettlementWindow.JDialog.changeSettlementName.input"),
					Msg.getString("SettlementWindow.JDialog.changeSettlementName.title"),
			        JOptionPane.QUESTION_MESSAGE);
	}
	/**
	 * Change and validate the new name of the Settlement
	 * @return call Dialog popup
	 */
	// 2014-10-26 mkung: Modified renameSettlement()
	private void renameSettlement() {
		JDialog.setDefaultLookAndFeelDecorated(true);
		String nameCache = settlement.getName();
		String settlementNewName = askNameDialog();
				
		if (settlementNewName.trim().equals(null) || (settlementNewName.trim().length() == 0))
			settlementNewName = askNameDialog();
		else {
			settlement.changeName(settlementNewName);
		}
		// desktop.clearDesktop();
		desktop.closeToolWindow(SettlementWindow.NAME);
		desktop.openToolWindow(SettlementWindow.NAME);	
	}
	
	public void setCurrentSettlement(Settlement settlement) {
		this.settlement = settlement;
	}
	/**
	 * Create the labels popup menu.
	 * @return popup menu.
	 */
	private JPopupMenu createLabelsMenu() {
		JPopupMenu result = new JPopupMenu(Msg.getString("SettlementWindow.menu.labelOptions")); //$NON-NLS-1$

		// Create building label menu item.
		JCheckBoxMenuItem buildingLabelMenuItem = new JCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.buildings"), getMapPanel().isShowBuildingLabels()); //$NON-NLS-1$
		buildingLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getMapPanel().setShowBuildingLabels(!getMapPanel().isShowBuildingLabels());
			}
		});
		result.add(buildingLabelMenuItem);

		// Create construction/salvage label menu item.
		JCheckBoxMenuItem constructionLabelMenuItem = new JCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.constructionSites"), getMapPanel().isShowConstructionLabels()); //$NON-NLS-1$
		constructionLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getMapPanel().setShowConstructionLabels(!getMapPanel().isShowConstructionLabels());
			}
		});
		result.add(constructionLabelMenuItem);

		// Create vehicle label menu item.
		JCheckBoxMenuItem vehicleLabelMenuItem = new JCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.vehicles"), getMapPanel().isShowVehicleLabels()); //$NON-NLS-1$
		vehicleLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getMapPanel().setShowVehicleLabels(!getMapPanel().isShowVehicleLabels());
			}
		});
		result.add(vehicleLabelMenuItem);

		// Create person label menu item.
		JCheckBoxMenuItem personLabelMenuItem = new JCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.people"), getMapPanel().isShowPersonLabels()); //$NON-NLS-1$
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
		settlementCBModel.destroy();
		mapPane.destroy();
	}

	/**
	 * Inner class combo box model for settlements.
	 */
	private class SettlementComboBoxModel
	extends DefaultComboBoxModel<Object>
	implements UnitManagerListener {

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
		}

		/**
		 * Update the list of settlements.
		 */
		private void updateSettlements() {

			Settlement selectedSettlement = (Settlement) getSelectedItem();

			// 2014-10-26 mkung: obtained settlement object
			setCurrentSettlement(selectedSettlement);
			
			removeAllElements();

			UnitManager unitManager = Simulation.instance().getUnitManager();
			List<Settlement> settlements = new ArrayList<Settlement>(unitManager.getSettlements());
			Collections.sort(settlements);

			Iterator<Settlement> i = settlements.iterator();
			while (i.hasNext()) {
				addElement(i.next());
			}

			if (selectedSettlement != null) {
				setSelectedItem(selectedSettlement);
				// 2014-10-26 mkung: obtained settlement object
				setCurrentSettlement(selectedSettlement);
			}
		}

		@Override
		public void unitManagerUpdate(UnitManagerEvent event) {
			if (event.getUnit() instanceof Settlement) {
				updateSettlements();
			}
		}

		/**
		 * Prepare class for deletion.
		 */
		public void destroy() {
			removeAllElements();
			UnitManager unitManager = Simulation.instance().getUnitManager();
			unitManager.removeUnitManagerListener(this);
		}
	}
}