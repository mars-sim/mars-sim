/**
 * Mars Simulation Project
 * GuideWindow.java
 * @version 3.1.0 2017-01-21
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.swing.tool.guide;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JViewport;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.javafx.BrowserJFX;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import javafx.application.Platform;

/**
 * The GuideWindow is a tool window that displays the built-in User Guide, About
 * Box and Tutorial.
 */
public class GuideWindow extends ToolWindow implements ActionListener,
		// HyperlinkListener,
		ComponentListener {
	/** Default serial id. */
	private static final long serialVersionUID = 1L;

	/** Default logger. */
	private static Logger logger = Logger.getLogger(GuideWindow.class.getName());

	/** Tool name. */
	public static final String NAME = Msg.getString("GuideWindow.title"); //$NON-NLS-1$

	/** Data members. */
	private List<URL> history = new ArrayList<>();

	private int historyIndex;

	/** The view port for the text pane. */
	private JViewport viewPort;
	// private URL guideURL = GuideWindow.class.getClassLoader().getResource("docs"
	// + File.separator +
	// "help" + File.separator + "userguide.html");
	/** [landrus, 27.11.09]: load the url in the constructor. */
	private URL guideURL, aboutURL, tutorialURL, shortcutsURL;
	private String discussionURLstring, wikiURLstring, projectsiteURLstring;

	private JButton shortcutsButton = new JButton(Msg.getString("GuideWindow.button.shortcuts")); //$NON-NLS-1$
	private JButton aboutButton = new JButton(Msg.getString("GuideWindow.button.about")); //$NON-NLS-1$
	private JButton tutorialButton = new JButton(Msg.getString("GuideWindow.button.tutorial")); //$NON-NLS-1$
	private JButton userguideButton = new JButton(Msg.getString("GuideWindow.button.userguide")); //$NON-NLS-1$
	private JButton projectsiteButton = new JButton(Msg.getString("GuideWindow.button.projectsite")); //$NON-NLS-1$
	/**
	 * Private JButton discussionButton = new
	 * JButton(Msg.getString("GuideWindow.button.discussion")); //$NON-NLS-1$
	 */
	private JButton wikiButton = new JButton(Msg.getString("GuideWindow.button.wiki")); //$NON-NLS-1$

	private BrowserJFX browser;
	private JPanel browserPanel;

	/**
	 * Constructor.
	 * 
	 * @param desktop
	 *            the desktop pane
	 */
	public GuideWindow(MainDesktopPane desktop) {
		super(NAME, desktop);
		// logger.info("GuideWindow's constructor is on " +
		// Thread.currentThread().getName() + " Thread");

		/* [landrus, 27.11.09]: use classloader compliant paths */
		shortcutsURL = getClass().getResource(Msg.getString("doc.shortcuts")); //$NON-NLS-1$
		guideURL = getClass().getResource(Msg.getString("doc.guide")); //$NON-NLS-1$
		aboutURL = getClass().getResource(Msg.getString("doc.about")); //$NON-NLS-1$
		tutorialURL = getClass().getResource(Msg.getString("doc.tutorial")); //$NON-NLS-1$
		projectsiteURLstring = Msg.getString("url.projectsite"); //$NON-NLS-1$
		// discussionURLstring = Msg.getString("url.discussion"); //$NON-NLS-1$
		wikiURLstring = Msg.getString("url.wiki"); //$NON-NLS-1$

		browser = desktop.getBrowserJFX();
		browserPanel = browser.getPanel();// .init();

		// Create the main panel
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		shortcutsButton.setToolTipText(Msg.getString("GuideWindow.tooltip.shortcuts")); //$NON-NLS-1$
		shortcutsButton.addActionListener(this);

		userguideButton.setToolTipText(Msg.getString("GuideWindow.tooltip.userguide")); //$NON-NLS-1$
		userguideButton.addActionListener(this);

		aboutButton.setToolTipText(Msg.getString("GuideWindow.tooltip.about")); //$NON-NLS-1$
		aboutButton.addActionListener(this);

		tutorialButton.setToolTipText(Msg.getString("GuideWindow.tooltip.tutorial")); //$NON-NLS-1$
		tutorialButton.addActionListener(this);

		projectsiteButton.setToolTipText(Msg.getString("GuideWindow.tooltip.projectsite")); //$NON-NLS-1$
		projectsiteButton.addActionListener(this);

		// discussionButton.setToolTipText(Msg.getString("GuideWindow.tooltip.discussion"));
		// //$NON-NLS-1$
		// discussionButton.addActionListener(this);

		wikiButton.setToolTipText(Msg.getString("GuideWindow.tooltip.wiki")); //$NON-NLS-1$
		wikiButton.addActionListener(this);

		// A toolbar to hold all our buttons
		JPanel toolPanel = new JPanel();
		toolPanel.add(aboutButton);
		toolPanel.add(tutorialButton);
		toolPanel.add(userguideButton);
		toolPanel.add(shortcutsButton);
		toolPanel.add(projectsiteButton);
		toolPanel.add(wikiButton);
		// toolPanel.add(discussionButton);

		mainPane.add(browserPanel, BorderLayout.CENTER);
		mainPane.add(toolPanel, BorderLayout.NORTH);

		setResizable(true);
		setMaximizable(true);
		setVisible(true);

		setMinimumSize(new Dimension(800, 600));
		setSize(new Dimension(1024, 600));

		if (desktop.getMainScene() != null) {
			setClosable(false);
		} else {
			Dimension desktopSize = desktop.getSize();
			Dimension jInternalFrameSize = getSize();
			int width = (desktopSize.width - jInternalFrameSize.width) / 2;
			int height = (desktopSize.height - jInternalFrameSize.height) / 2;
			setLocation(width, height);
		}

		// Pack window.
		// pack(); // this will shrink the window to one line tall in swing mode
	}

	/** Set a display URL. */
	// 2016-06-07 Added displaying the hyperlink's path and html filename.
	@SuppressWarnings("restriction")
	public void setURL(String fileloc) {
		// goToURL(getClass().getResource(fileloc));
		// browser.getStatusBarLabel().setText(fileloc);
		String fullLink = getClass().getResource(fileloc).toExternalForm();
		Platform.runLater(() -> {
			browser.setTextInputCache(fullLink);
			browser.inputURLType(fullLink);// , BrowserJFX.REMOTE_HTML);
			browser.showFormattedURL();
			browser.fireButtonGo(fullLink);
		});
	}

	/** Gets the full URL string for internal html files. */
	// 2017-04-28 Added displaying the hyperlink's path and html filename.
	public String getFullURL(String fileloc) {
		return getClass().getResource(fileloc).toExternalForm();
	}

	/** Implementing ActionListener method. */
	@SuppressWarnings("restriction")
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == this.userguideButton) {
			String input = guideURL.toExternalForm();
			Platform.runLater(() -> {
				browser.setTextInputCache(input);
				browser.inputURLType(input);
				browser.showFormattedURL();
			});
		}

		else if (source == this.shortcutsButton) {
			String input = shortcutsURL.toExternalForm();
			Platform.runLater(() -> {
				browser.setTextInputCache(input);
				browser.inputURLType(input);
				browser.showFormattedURL();
			});
		}

		else if (source == this.aboutButton) {
			String input = aboutURL.toExternalForm();
			Platform.runLater(() -> {
				browser.setTextInputCache(input);
				browser.inputURLType(input);
				browser.showFormattedURL();
			});
		}

		else if (source == this.tutorialButton) {
			String input = tutorialURL.toExternalForm();
			Platform.runLater(() -> {
				browser.setTextInputCache(input);
				browser.inputURLType(input);
				browser.showFormattedURL();
			});
		}

		else if (source == this.projectsiteButton || source == this.wikiButton) {
			Platform.runLater(() -> {
				browser.setTextInputCache(projectsiteURLstring);
				browser.inputURLType(projectsiteURLstring);
				browser.showFormattedURL();
			});
			/*
			 * } else if (source == this.discussionButton) { Platform.runLater(()-> {
			 * browser.setTextInputCache(discussionURLstring);
			 * browser.inputURLType(discussionURLstring); browser.showURL(); });
			 */
		}
	}

	/**
	 * Implement ComponentListener interface. Make sure the text is scrolled to the
	 * top. Need to find a better way to do this
	 * 
	 * @author Scott
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		viewPort.setViewPosition(new Point(0, 0));
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	/** Prepare tool window for deletion. */
	@Override
	public void destroy() {
	}
}