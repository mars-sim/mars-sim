/*
 * Mars Simulation Project
 * TabPanel.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

@SuppressWarnings("serial")
public abstract class TabPanel extends JScrollPane {

	// Font used in tab panel title
	protected static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 16);
	protected static final Font SUBTITLE_FONT = new Font("Serif", Font.BOLD, 14);
	protected static final Font SUBTITLE_FONT_1 = new Font(Font.DIALOG, Font.BOLD, 14);

	
	// Default Decimal formatter
	protected static final DecimalFormat DECIMAL_PLACES3 = new DecimalFormat("0.000");
	protected static final DecimalFormat DECIMAL_PLACES2 = new DecimalFormat("0.00");
	protected static final DecimalFormat DECIMAL_PLACES1 = new DecimalFormat("0.0");
	protected static final DecimalFormat DECIMAL_KG = new DecimalFormat("0.0 kg");
	
	// Default values for any top level Spring panel holding values
	protected static final int INITY_DEFAULT = 5;
	protected static final int INITX_DEFAULT = 75;
	protected static final int YPAD_DEFAULT = 1;
	protected static final int XPAD_DEFAULT = 5;
	
	protected static final int NUM_COL = 15;
	
	private boolean isUIDone = false;
	
	private String description;
	private String tabTitle;
	private String tabToolTip;
	
	private transient Icon tabIcon;
	
	// These can be made private once all tabs converted
	private JPanel topContentPanel;
	private JPanel centerContentPanel;
	
	private Unit unit;
	private MainDesktopPane desktop;


	/**
	 * Constructor.
	 *
	 * @param tabTitle   the title to be displayed in the tab (may be null).
	 * @param tabIcon    the icon to be displayed in the tab (may be null).
	 * @param tabToolTip the tool tip to be displayed in the icon (may be null).
	 * @param unit       the unit to display.
	 * @param desktop    the main desktop.
	 */
	protected TabPanel(String tabTitle, Icon tabIcon, String tabToolTip, Unit unit, MainDesktopPane desktop) {
		this((tabTitle != null) ? tabTitle : tabToolTip, tabTitle, tabIcon, tabToolTip, unit, desktop);
	}
	
	/**
	 * Constructor.
	 *
	 * @param tabTitle   the title to be displayed in the tab (may be null).
	 * @param description A longer descriptive title displayed at the top of the panel.
	 * @param tabIcon    the icon to be displayed in the tab (may be null).
	 * @param tabToolTip the tool tip to be displayed in the icon (may be null).
	 * @param unit       the unit to display.
	 * @param desktop    the main desktop.
	 */
	protected TabPanel(String tabTitle, String description, Icon tabIcon, String tabToolTip, Unit unit, MainDesktopPane desktop) {

		// Use JScrollPane constructor
		super();

		// Initialize data members
		this.tabTitle = tabTitle;
		this.description = description;
		this.tabIcon = tabIcon;
		this.tabToolTip = tabToolTip;
		this.unit = unit;
		this.desktop = desktop;

		if (unit.getUnitType() == UnitType.PERSON) {
			this.setMaximumSize(new Dimension(UnitWindow.WIDTH - 30, UnitWindow.HEIGHT - 140));
			this.setPreferredSize(new Dimension(UnitWindow.WIDTH - 30, UnitWindow.HEIGHT - 140));
		}
		else {
			this.setMaximumSize(new Dimension(UnitWindow.WIDTH - 30, UnitWindow.HEIGHT - 90));
			this.setPreferredSize(new Dimension(UnitWindow.WIDTH - 30, UnitWindow.HEIGHT - 90));
		}
		
		// Create the view panel
		JPanel viewPanel = new JPanel(new BorderLayout(0, 0));
		createViewport();
		setViewportView(viewPanel);
		createVerticalScrollBar();
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);

		JScrollBar vertical = getVerticalScrollBar();
		vertical.setValue(0);//vertical.getMinimum());
		
		// Create top content panel
		topContentPanel = new JPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		topContentPanel.setBorder(MainDesktopPane.newEmptyBorder());
		viewPanel.add(topContentPanel, BorderLayout.NORTH);

		Border margin = new EmptyBorder(5,5,5,5);
		
		// Create center content panel
		centerContentPanel = new JPanel(new BorderLayout(0, 10));
		centerContentPanel.setBorder(margin);
		viewPanel.add(centerContentPanel, BorderLayout.CENTER);
	}

 	public boolean isUIDone() {
		return isUIDone;
	}
	
	public void initializeUI() {
		if (!isUIDone) {
			// Create label in top panel
			String topLabel = (description != null ? description : getTabTitle());
			WebLabel titleLabel = new WebLabel(topLabel, SwingConstants.CENTER);
			titleLabel.setFont(TITLE_FONT);
			
			JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			labelPanel.add(titleLabel);
			topContentPanel.add(labelPanel);
			
			buildUI(centerContentPanel);
			
			isUIDone = true;
		}	
	}
	
	/**
	 * Builds the UI element using the 3 components.
	 * 
	 * @param centerContentPanel
	 */
	protected void buildUI(JPanel centerContentPanel) {
		throw new UnsupportedOperationException("Build UI not implemented yet");
	}
	
	/**
	 * Adds a text field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param parent Parent panel
	 * @param label The fixed label
	 * @param content Initial content of the text field as an integer
	 * @param tooltip Optional tooltip
	 * @return The JTextField that can be updated.
	 */
	protected JTextField addTextField(JPanel parent, String label, int content, String tooltip) {
		return createTextField(parent, label, Integer.toString(content), NUM_COL, tooltip);
	}

	/**
	 * Adds a text field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param parent Parent panel
	 * @param label The fixed label
	 * @param content Initial content of the text field as an integer
	 * @param tooltip Optional tooltip
	 * @return The JTextField that can be updated.
	 */
	protected JTextField addTextField(JPanel parent, int label, int content, String tooltip) {
		return createTextField(parent, Integer.toString(label), Integer.toString(content), NUM_COL, tooltip);
	}
	
	/**
	 * Adds a text field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param parent Parent panel
	 * @param label The fixed label
	 * @param content Initial content of the text field
	 * @param col number of columns
	 * @param tooltip Optional tooltip
	 * @return The JTextField that can be updated.
	 */
	protected JTextField addTextField(JPanel parent, String label, String content, int col, String tooltip) {
		return createTextField(parent, label, content, col, tooltip);
	}
	
	/**
	 * Adds a text field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param parent Parent panel
	 * @param label The fixed label
	 * @param content Initial content of the text field
	 * @param col number of columns
	 * @param tooltip Optional tooltip
	 * @return The JTextField that can be updated.
	 */
	protected JTextField addTextField(JPanel parent, String label, int content, int col, String tooltip) {
		return createTextField(parent, label, Integer.toString(content), col, tooltip);
	}
	
	
	/**
	 * Adds a text field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param parent Parent panel
	 * @param label The fixed label
	 * @param content Initial content of the text field
	 * @param tooltip Optional tooltip
	 * @return The JTextField that can be updated.
	 */
	protected JTextField addTextField(JPanel parent, String label, String content, String tooltip) {
		return createTextField(parent, label, content, NUM_COL, tooltip);
	}
	
	/**
	 * Adds a text field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param parent Parent panel
	 * @param label The fixed label
	 * @param content Initial content of the text field
	 * @param col number of columns
	 * @param tooltip Optional tooltip
	 * @return The JTextField that can be updated.
	 */
	private JTextField createTextField(JPanel parent, String label, String content, int col, String tooltip) {
		parent.add(new WebLabel(label, SwingConstants.RIGHT));
						
		WebPanel wrapper3 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		JTextField typeTF = new JTextField();
		typeTF.setText(content);
		typeTF.setEditable(false);
		typeTF.setColumns(col);
		if (tooltip != null) {
			typeTF.setToolTipText(tooltip);
		}
		wrapper3.add(typeTF);
		parent.add(wrapper3);
		return typeTF;
	}
	
	/**
	 * Adds a standard titled border.
	 * 
	 * @param panel
	 * @param title The title to display
	 */
	protected void addBorder(JComponent panel, String title) {
		panel.setBorder(new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
										 SUBTITLE_FONT, null));
	}
	
	/**
	 * Gets the tab title.
	 *
	 * @return tab title or null.
	 */
	public String getTabTitle() {
		return tabTitle;
	}

	/**
	 * Gets the tab icon.
	 *
	 * @return tab icon or null.
	 */
	public Icon getTabIcon() {
		return tabIcon;
	}

	/**
	 * Gets the tab tool tip.
	 *
	 * @return tab tool tip.
	 */
	public String getTabToolTip() {
		return tabToolTip;
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		// No updated required
	}

	/**
	 * Gets the main desktop.
	 * 
	 * @return desktop.
	 */
	protected MainDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * Gets the unit.
	 * 
	 * @return unit.
	 */
	protected Unit getUnit() {
		return unit;
	}
	
	/**
	 * Gets the simulation being monitored.
	 * 
	 * @return
	 */
	protected Simulation getSimulation() {
		return desktop.getSimulation();
	}
	
	/**
	 * Gets the master clock instance.
	 * 
	 * @return
	 */
	protected MasterClock getMasterClock() {
		return getSimulation().getMasterClock();
	}
	
	/**
	 * Gets the mars clock instance.
	 * 
	 * @return
	 */
	protected MarsClock getMarsClock() {
		return getMasterClock().getMarsClock();
	}

	
	@Override
	public  String toString() {
		return tabTitle;
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		tabIcon = null;
		topContentPanel = null;
		centerContentPanel = null;
		unit = null;
		desktop = null;
	}

}
