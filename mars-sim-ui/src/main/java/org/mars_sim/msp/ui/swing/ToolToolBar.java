/*
 * Mars Simulation Project
 * ToolToolBar.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.tool.commander.CommanderWindow;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.MarsCalendarDisplay;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;


import com.alee.extended.window.PopOverDirection;
import com.alee.extended.window.WebPopOver;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.style.StyleId;

/**
 * The ToolToolBar class is a UI toolbar for holding tool buttons. There should
 * only be one instance and it is contained in the {@link MainWindow} instance.
 */
public class ToolToolBar extends WebToolBar implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final int HEIGHT = 36;

	public static final String WIKI_URL = Msg.getString("ToolToolBar.calendar.url"); //$NON-NLS-1$
	public static final String WIKI_TEXT = Msg.getString("ToolToolBar.calendar.title"); //$NON-NLS-1$

	// Data members
	/** List of tool buttons. */
	private Vector<ToolButton> toolButtons;

	/** Main window that contains this toolbar. */
	private MainWindow parentMainWindow;

	private MarsCalendarDisplay calendarDisplay; 
	
	private JLabel monthLabel;
	
	/** Sans serif font. */
	private Font SANS_SERIF_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

	/**
	 * Constructs a ToolToolBar object
	 * @param parentMainWindow the main window pane
	 */
	public ToolToolBar(MainWindow parentMainWindow) {

		// Use JToolBar constructor
		super(JToolBar.HORIZONTAL);
		// Set weblaf's particular toolbar style
		setStyleId(StyleId.toolbarAttachedNorth);

		// Initialize data members
		toolButtons = new Vector<>();
		this.parentMainWindow = parentMainWindow;

		// Set name
		setName(Msg.getString("ToolToolBar.toolbar")); //$NON-NLS-1$
		// Fix tool bar
		setFloatable(false);

		setPreferredSize(new Dimension(0, HEIGHT));

		setOpaque(false);

		setBackground(new Color(0, 0, 0, 128));

		// Prepare tool buttons
		prepareToolButtons();

		// Set border around toolbar
		setBorder(new BevelBorder(BevelBorder.RAISED));
	}

	/** Prepares tool buttons */
	private void prepareToolButtons() {

		ToolButton saveButton = new ToolButton(Msg.getString("mainMenu.save"), Msg.getString("icon.save")); //$NON-NLS-1$ //$NON-NLS-2$
		saveButton.addActionListener(e -> parentMainWindow.saveSimulation(true));
		add(saveButton);

		ToolButton saveAsButton = new ToolButton(Msg.getString("mainMenu.saveAs"), Msg.getString("icon.saveAs")); //$NON-NLS-1$ //$NON-NLS-2$
		saveAsButton.addActionListener(e -> parentMainWindow.saveSimulation(false));
		add(saveAsButton);

		ToolButton exitButton = new ToolButton(Msg.getString("mainMenu.exit"), Msg.getString("icon.exit")); //$NON-NLS-1$ //$NON-NLS-2$
		exitButton.addActionListener(e -> parentMainWindow.exitSimulation());
		add(exitButton);

		addSeparator(new Dimension(20, 20));

		// Add Tools buttons
		addToolButton(NavigatorWindow.NAME, "icon.mars"); //$NON-NLS-
		addToolButton(SearchWindow.NAME, "icon.find"); //$NON-NLS-1$
		addToolButton(TimeWindow.NAME, "icon.time"); //$NON-NLS-1$
		addToolButton(MonitorWindow.TITLE, "icon.monitor"); //$NON-NLS-1$
		addToolButton(MissionWindow.NAME, "icon.mission"); //$NON-NLS-1$
		addToolButton(SettlementWindow.NAME, "icon.map"); //$NON-NLS-1$
		addToolButton(ScienceWindow.NAME, "icon.science"); //$NON-NLS-1$
		addToolButton(ResupplyWindow.NAME, "icon.resupply"); //$NON-NLS-1$
		addToolButton(CommanderWindow.NAME, "icon.dashboard"); //$NON-NLS-1$

		addToEnd(parentMainWindow.getEarthDate());

		addToEnd(parentMainWindow.getSolLabel());

		addToEnd(parentMainWindow.getMarsTime());
		
		MarsClock marsClock = parentMainWindow.getSimulation().getMasterClock().getMarsClock();
		
	
		JButton calendarButton = setUpCalenders(marsClock);
	
		addToEnd(calendarButton);

		JButton starMap = createStarMapButton();

		addToEnd(starMap);
		
		addSeparatorToEnd();

		// Add guide button
		ToolButton guideButton = new ToolButton(GuideWindow.NAME, Msg.getString("img.guide")); //$NON-NLS-1$
		guideButton.addActionListener(this);
		addToEnd(guideButton);
		toolButtons.addElement(guideButton);
	}

	private void addToolButton(String name, String iconKey) {
		ToolButton toolButton = new ToolButton(name, Msg.getString(iconKey));
		toolButton.addActionListener(this);
		add(toolButton);
		toolButtons.addElement(toolButton);
	}

	private JButton setUpCalenders(MarsClock marsClock) {
		JPanel outerPane = setupCalendarPanel(marsClock);

		Icon calendarIcon = MainWindow.getIcon("calendar_mars");

		JButton calendarButton = new JButton(calendarIcon);

		calendarButton.addActionListener(e -> {
		    	calendarDisplay.update();
		
		    	String mn = "Month of {" + marsClock.getMonthName() + ":u}";
		    	monthLabel.setText(mn);
		
				Window parent = parentMainWindow.getFrame();
		        final WebPopOver popOver = new WebPopOver(StyleId.popover, parent);
		        popOver.setIconImages(WebLookAndFeel.getImages());
		        popOver.setCloseOnFocusLoss(true);
		        popOver.setPadding(5);
		        popOver.add(outerPane);
		        popOver.show(calendarButton, PopOverDirection.down);
				// final JDialog calPopOver = new JDialog(parent);
		        // calPopOver.setIconImages(WebLookAndFeel.getImages());
		        // //calPopOver.setCloseOnFocusLoss(true);
		        // //calPopOver.setPadding(5);
		        // calPopOver.add(outerPane);
		        // calPopOver.setVisible(true);
		});

		return calendarButton;
	}
	
	private JPanel setupCalendarPanel(MarsClock marsClock) {
		JPanel innerPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));

		calendarDisplay = new MarsCalendarDisplay(marsClock, parentMainWindow.getDesktop());
		innerPane.add(calendarDisplay);

		final JPanel midPane = new JPanel(new BorderLayout(0, 0));

		midPane.add(innerPane, BorderLayout.CENTER);
		midPane.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.ORANGE, new Color(210,105,30)));

		final JPanel outerPane = new JPanel(new BorderLayout(10, 10));
		outerPane.add(midPane, BorderLayout.CENTER);

		// Create martian month label
    	String mn = "Month of {" + marsClock.getMonthName() + ":u}";
    	monthLabel = new JLabel(mn, SwingConstants.CENTER);
		JPanel monthPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		monthPane.add(monthLabel);
		midPane.add(monthPane, BorderLayout.NORTH);

		JButton link = new JButton(WIKI_TEXT);
		link.setAlignmentX(.5f);
		link.setToolTipText("Open the Timekeeping wiki in mars-sim GitHub site");
		link.addActionListener(e -> {
							parentMainWindow.openBrowser(WIKI_URL);
							}
						);

		JPanel linkPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
		linkPane.add(link);
		outerPane.add(linkPane, BorderLayout.SOUTH);

    	JLabel headerLabel = new JLabel("Mars Calendar", JLabel.CENTER);
    	headerLabel.setFont(SANS_SERIF_FONT);

    	outerPane.add(headerLabel, BorderLayout.NORTH);
    	
    	return outerPane;
	}
	
	private JButton createStarMapButton() {
		JButton starMap = new JButton();
		String TELESCOPE_ICON = Msg.getString("icon.telescope"); //$NON-NLS-1$
		starMap.setIcon(ImageLoader.getNewIcon(TELESCOPE_ICON));//parentMainWindow.getTelescopeIcon());
		starMap.setToolTipText("Open the Orbit Viewer");

		starMap.addActionListener(e -> parentMainWindow.openOrbitViewer());
		
		return starMap;
	}
	
	/** ActionListener method overridden */
	@Override
	public void actionPerformed(ActionEvent event) {
		// show tool window on desktop
		parentMainWindow.getDesktop().openToolWindow(
			((ToolButton) event.getSource()).getToolName()
		);
	}

	public void destroy() {
		toolButtons.clear();
		toolButtons = null;
		parentMainWindow = null;
	}
}
