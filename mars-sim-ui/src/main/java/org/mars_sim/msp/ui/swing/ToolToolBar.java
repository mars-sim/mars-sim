/**
 * Mars Simulation Project
 * ToolToolBar.java
 * @version 3.2.0 2021-06-20
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
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
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

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.link.UrlLinkAction;
import com.alee.extended.link.WebLink;
import com.alee.extended.window.PopOverDirection;
import com.alee.extended.window.WebPopOver;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.icon.LazyIcon;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.alee.utils.CoreSwingUtils;

/**
 * The ToolToolBar class is a UI toolbar for holding tool buttons. There should
 * only be one instance and it is contained in the {@link MainWindow} instance.
 */
public class ToolToolBar extends WebToolBar implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

//	private static final int ICON_H = 16;
//	private static final int ICON_W = 16;
//	private static final int EMPTY_W = GameManager.mode == GameMode.COMMAND  ? MainWindow.WIDTH - (15 + 4) * 18 - 330 : MainWindow.WIDTH - (15 + 4) * 18 - 300;//735;
//	private static final int EMPTY_H = 32;

	public static final String WIKI_URL = Msg.getString("ToolToolBar.calendar.url"); //$NON-NLS-1$
	public static final String WIKI_TEXT = Msg.getString("ToolToolBar.calendar.title"); //$NON-NLS-1$

	// Data members
	/** List of tool buttons. */
	private Vector<ToolButton> toolButtons;

	/** Main window that contains this toolbar. */
	private MainWindow parentMainWindow;

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
		toolButtons = new Vector<ToolButton>();
		this.parentMainWindow = parentMainWindow;

		// Set name
		setName(Msg.getString("ToolToolBar.toolbar")); //$NON-NLS-1$
		// Fix tool bar
		setFloatable(false);

		setPreferredSize(new Dimension(0, 32));

		setOpaque(false);

		setBackground(new Color(0,0,0,128));

		// Prepare tool buttons
		prepareToolButtons();

		// Set border around toolbar
		setBorder(new BevelBorder(BevelBorder.RAISED));
	}

	/** Prepares tool buttons */
	private void prepareToolButtons() {

//		ToolButton openButton = new ToolButton(Msg.getString("mainMenu.open"), Msg.getString("img.open")); //$NON-NLS-1$ //$NON-NLS-2$
//		openButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				parentMainWindow.loadSimulation(false);
//			};
//		});
//		add(openButton);
//
//		ToolButton openAutosaveButton = new ToolButton(Msg.getString("mainMenu.openAutosave"), Msg.getString("img.openAutosave")); //$NON-NLS-1$ //$NON-NLS-2$
//		openAutosaveButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				parentMainWindow.loadSimulation(true);
//			};
//		});
//		add(openAutosaveButton);

		ToolButton saveButton = new ToolButton(Msg.getString("mainMenu.save"), Msg.getString("img.save")); //$NON-NLS-1$ //$NON-NLS-2$
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.saveSimulation(true, false);
			};
		});
		add(saveButton);

		ToolButton saveAsButton = new ToolButton(Msg.getString("mainMenu.saveAs"), Msg.getString("img.saveAs")); //$NON-NLS-1$ //$NON-NLS-2$
		saveAsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.saveSimulation(false, false);
			};
		});
		add(saveAsButton);

		ToolButton exitButton = new ToolButton(Msg.getString("mainMenu.exit"), Msg.getString("img.exit")); //$NON-NLS-1$ //$NON-NLS-2$
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.exitSimulation();
			};
		});
		add(exitButton);

		addSeparator(new Dimension(20, 20));

		// Add Mars navigator button
		ToolButton navButton = new ToolButton(NavigatorWindow.NAME, Msg.getString("img.planet")); //$NON-NLS-1$
		navButton.addActionListener(this);
		add(navButton);
		toolButtons.addElement(navButton);

		// Add search tool button
		ToolButton searchButton = new ToolButton(SearchWindow.NAME, Msg.getString("img.find")); //$NON-NLS-1$
		searchButton.addActionListener(this);
		add(searchButton);
		toolButtons.addElement(searchButton);

		// Add time tool button
		ToolButton timeButton = new ToolButton(TimeWindow.NAME, Msg.getString("img.time")); //$NON-NLS-1$
		timeButton.addActionListener(this);
		add(timeButton);
		toolButtons.addElement(timeButton);

		// Add monitor tool button
		ToolButton monitorButton = new ToolButton(MonitorWindow.NAME, Msg.getString("img.monitor")); //$NON-NLS-1$
		monitorButton.addActionListener(this);
		add(monitorButton);
		toolButtons.addElement(monitorButton);

		// Add mission tool button
		ToolButton missionButton = new ToolButton(MissionWindow.NAME, Msg.getString("img.mission")); //$NON-NLS-1$
		missionButton.addActionListener(this);
		add(missionButton);
		toolButtons.addElement(missionButton);

		// Add settlement tool button
		ToolButton settlementButton = new ToolButton(SettlementWindow.NAME, Msg.getString("img.settlementMapTool")); //$NON-NLS-1$
		settlementButton.addActionListener(this);
		add(settlementButton);
		toolButtons.addElement(settlementButton);

		// Add science tool button
		ToolButton scienceButton = new ToolButton(ScienceWindow.NAME, Msg.getString("img.science")); //$NON-NLS-1$
		scienceButton.addActionListener(this);
		add(scienceButton);
		toolButtons.addElement(scienceButton);

		// Add resupply tool button
		ToolButton resupplyButton = new ToolButton(ResupplyWindow.NAME, Msg.getString("img.resupply")); //$NON-NLS-1$
		resupplyButton.addActionListener(this);
		add(resupplyButton);
		toolButtons.addElement(resupplyButton);

		// Add the command dashboard button
		if (GameManager.getGameMode() == GameMode.COMMAND) {
			// Add commander dashboard button
			ToolButton dashboardButton = new ToolButton(CommanderWindow.NAME, Msg.getString("img.dashboard")); //$NON-NLS-1$
			dashboardButton.addActionListener(this);
			add(dashboardButton);
			toolButtons.addElement(dashboardButton);
		}

//		addSeparator();

		addToEnd(parentMainWindow.getEarthDate());

		addToEnd(parentMainWindow.getSolLabel());

		addToEnd(parentMainWindow.getMarsTime());

		Icon calendarIcon = new LazyIcon("calendar_mars").getIcon();

		WebPanel innerPane = new WebPanel(StyleId.panelTransparent, new FlowLayout(FlowLayout.CENTER, 2, 2));

		MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();
		MarsCalendarDisplay calendarDisplay = new MarsCalendarDisplay(marsClock, parentMainWindow.getDesktop());
		innerPane.add(calendarDisplay);

		final WebPanel midPane = new WebPanel(StyleId.panelTransparent, new BorderLayout(0, 0));
//		calendarPane.setPreferredSize(new Dimension(140, 80));

		midPane.add(innerPane, BorderLayout.CENTER);
		midPane.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.ORANGE, new Color(210,105,30)));

		final WebPanel outerPane = new WebPanel(StyleId.panelTransparent, new BorderLayout(10, 10));
		outerPane.add(midPane, BorderLayout.CENTER);

		// Create martian month label
//		WebLabel monthLabel = new WebLabel("Month of " + marsClock.getMonthName(), WebLabel.CENTER);
    	String mn = "Month of {" + marsClock.getMonthName() + ":u}";
    	WebStyledLabel monthLabel = new WebStyledLabel(StyleId.styledlabelShadow, mn, WebLabel.CENTER);
//		monthLabel.setFont(SANS_SERIF_FONT);
//    	monthLabel.setAlignmentY(1f);
		WebPanel monthPane = new WebPanel(StyleId.panelTransparent, new FlowLayout(FlowLayout.CENTER, 2, 2));
		monthPane.add(monthLabel);
		midPane.add(monthPane, BorderLayout.NORTH);

		WebLink link = new WebLink(StyleId.linkShadow, new UrlLinkAction(WIKI_URL));
//		link = new WebLink(StyleId.linkShadow, WIKI_TEXT, new UrlLinkAction(WIKI_URL));
//		link.setAlignmentY(1f);
		link.setAlignmentX(.5f);
		link.setText(WIKI_TEXT);
//		link.setIcon(new SvgIcon("github.svg")); // github19
		TooltipManager.setTooltip(link, "Open the Timekeeping wiki in mars-sim GitHub site", TooltipWay.down);
		WebPanel linkPane = new WebPanel(StyleId.panelTransparent, new FlowLayout(FlowLayout.RIGHT, 2, 2));
		linkPane.add(link);
		outerPane.add(linkPane, BorderLayout.SOUTH);

    	WebStyledLabel headerLabel = new WebStyledLabel(StyleId.styledlabelShadow, "Mars Calendar", WebLabel.CENTER);
    	headerLabel.setFont(SANS_SERIF_FONT);

    	outerPane.add(headerLabel, BorderLayout.NORTH);

		WebButton calendarButton = new WebButton(StyleId.buttonIconHover, calendarIcon);
		calendarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e){
            	calendarDisplay.update();

            	String mn = "Month of {" + marsClock.getMonthName() + ":u}";
            	monthLabel.setText(mn);

            	final Window parent = CoreSwingUtils.getNonNullWindowAncestor(calendarButton);
                final WebPopOver popOver = new WebPopOver(StyleId.popover, parent);
                popOver.setIconImages(WebLookAndFeel.getImages());
                popOver.setCloseOnFocusLoss(true);
                popOver.setPadding(5);
                popOver.add(outerPane);
                popOver.show(calendarButton, PopOverDirection.down);
            }
        } );

		addToEnd(calendarButton);

		addSeparatorToEnd();

//		addSeparatorToMiddle();

//		addSeparator(new Dimension(20, 20));

//		addSeparator();

		// Add guide button
		ToolButton guideButton = new ToolButton(GuideWindow.NAME, Msg.getString("img.guide")); //$NON-NLS-1$
		guideButton.addActionListener(this);
		addToEnd(guideButton);
		toolButtons.addElement(guideButton);

//		addSeparator();

//		JPanel emptyPanel = new JPanel();
//		emptyPanel.setPreferredSize(new Dimension(EMPTY_W, EMPTY_H));
//		add(emptyPanel);

//		add(Box.createGlue());
//
//		addSeparator();

//		ToolButton slowDownButton = new ToolButton("Slow Down", Msg.getString("img.speed.slowDown")); //$NON-NLS-1$ //$NON-NLS-2$
////		slowDownButton.setPreferredSize(new Dimension(ICON_W, ICON_H));
//		slowDownButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				int ratio = (int)masterClock.getTimeRatio();
//				if (ratio >= 2)
//					masterClock.setTimeRatio(ratio/2.0);
//			};
//		});
//		add(slowDownButton);
//
//		ToolButton pauseButton = new ToolButton("Pause", Msg.getString("img.speed.pause")); //$NON-NLS-1$ //$NON-NLS-2$
////		pauseButton.setPreferredSize(new Dimension(ICON_W, ICON_H));
//		pauseButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if (!masterClock.isPaused())
//					masterClock.setPaused(true, false);
//			};
//		});
//		add(pauseButton);
//
//
//		ToolButton resumeButton = new ToolButton("Resume", Msg.getString("img.speed.play")); //$NON-NLS-1$ //$NON-NLS-2$
////		resumeButton.setPreferredSize(new Dimension(ICON_W, ICON_H));
//		resumeButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if (masterClock.isPaused())
//					masterClock.setPaused(false, false);
//			};
//		});
//		add(resumeButton);
//
//		ToolButton speedUpButton = new ToolButton("Speed Up", Msg.getString("img.speed.speedUp")); //$NON-NLS-1$ //$NON-NLS-2$
////		speedUpButton.setPreferredSize(new Dimension(ICON_W, ICON_H));
//		speedUpButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				int ratio = (int)masterClock.getTimeRatio();
//				if (ratio <= 4096)
//					masterClock.setTimeRatio(ratio*2.0);
//			};
//		});
//		add(speedUpButton);

//		addToEnd(new MagnifierToggleTool(parentMainWindow.getFrame()) );
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
