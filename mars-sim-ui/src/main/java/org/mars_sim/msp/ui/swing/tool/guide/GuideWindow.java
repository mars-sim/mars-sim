/**
 * Mars Simulation Project
 * GuideWindow.java
 * @version 3.1.0 2016-10-08
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.swing.tool.guide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.javafx.BrowserJFX;
import org.mars_sim.msp.ui.swing.HTMLContentPane;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import javafx.application.Platform;

/**
 * The GuideWindow is a tool window that displays the built-in User Guide,
 * About Box and Tutorial.
 */
public class GuideWindow
extends ToolWindow
implements ActionListener,
//HyperlinkListener, 
ComponentListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(GuideWindow.class.getName());

	// Tool name
	public static final String NAME = Msg.getString("GuideWindow.title"); //$NON-NLS-1$

	// Data members
	private List<URL> history = new ArrayList<URL>();

	private int historyIndex = 0;

	/** The view port for the text pane. */
	private JViewport viewPort;
	//private URL guideURL = GuideWindow.class.getClassLoader().getResource("docs" + File.separator +
	//        "help" + File.separator + "userguide.html");
	/* [landrus, 27.11.09]: load the url in the constructor. */
	private URL guideURL, aboutURL, tutorialURL;
	private String discussionURLstring, projectsiteURLstring;

	private JButton aboutButton = new JButton(Msg.getString("GuideWindow.button.about")); //$NON-NLS-1$
	private JButton tutorialButton = new JButton(Msg.getString("GuideWindow.button.tutorial")); //$NON-NLS-1$
	private JButton userguideButton = new JButton(Msg.getString("GuideWindow.button.userguide")); //$NON-NLS-1$
	private JButton projectsiteButton = new JButton(Msg.getString("GuideWindow.button.projectsite")); //$NON-NLS-1$
	private JButton discussionButton = new JButton(Msg.getString("GuideWindow.button.discussion")); //$NON-NLS-1$
	
	private BrowserJFX browser;
	private JPanel browserPanel;
	/**
	 * Constructor.
	 * @param desktop the desktop pane
	 */
	public GuideWindow(MainDesktopPane desktop) {
		super(NAME, desktop);
	   	//logger.info("GuideWindow's constructor is on " + Thread.currentThread().getName() + " Thread");

		/* [landrus, 27.11.09]: use classloader compliant paths */
		guideURL = getClass().getResource(Msg.getString("doc.guide")); //$NON-NLS-1$
		aboutURL = getClass().getResource(Msg.getString("doc.about")); //$NON-NLS-1$
		tutorialURL = getClass().getResource(Msg.getString("doc.tutorial")); //$NON-NLS-1$
		discussionURLstring = Msg.getString("url.discussion"); //$NON-NLS-1$
		projectsiteURLstring = Msg.getString("url.projectsite"); //$NON-NLS-1$

		browser = desktop.getBrowserJFX();
		browserPanel = browser.getPanel();//.init();

		// Create the main panel
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		userguideButton.setToolTipText(Msg.getString("GuideWindow.tooltip.userguide")); //$NON-NLS-1$
		userguideButton.addActionListener(this);

		aboutButton.setToolTipText(Msg.getString("GuideWindow.tooltip.about")); //$NON-NLS-1$
		aboutButton.addActionListener(this);

		tutorialButton.setToolTipText(Msg.getString("GuideWindow.tooltip.tutorial")); //$NON-NLS-1$
		tutorialButton.addActionListener(this);

		projectsiteButton.setToolTipText(Msg.getString("GuideWindow.tooltip.projectsite")); //$NON-NLS-1$
		projectsiteButton.addActionListener(this);

		discussionButton.setToolTipText(Msg.getString("GuideWindow.tooltip.discussion")); //$NON-NLS-1$
		discussionButton.addActionListener(this);

		// A toolbar to hold all our buttons
		JPanel toolPanel = new JPanel();
		toolPanel.add(aboutButton);
		toolPanel.add(tutorialButton);		
		toolPanel.add(userguideButton);

		toolPanel.add(projectsiteButton);
		toolPanel.add(discussionButton);
	
		mainPane.add(browserPanel, BorderLayout.CENTER);
		mainPane.add(toolPanel, BorderLayout.NORTH);
		
		
		if (desktop.getMainScene() != null)
			setClosable(false);		

		setSize(new Dimension(1024, 600));
		setResizable(false);
		setMaximizable(true);		
		setVisible(true);
	   	//logger.info("GuideWindow's constructor: done!");

		//userguideButton.doClick(); // not useful
	}

	

	/**
	 * Set a display URL .
	 */
	// 2016-06-07 Added displaying the hyperlink's path and html filename.
	public void setURL(String fileloc) {
		//goToURL(getClass().getResource(fileloc));
		//browser.getStatusBarLabel().setText(fileloc);
		String fullLink = getClass().getResource(fileloc).toExternalForm();
		Platform.runLater(()-> {
			browser.setTextInputCache(fullLink);
			browser.inputURLType(fullLink);//, BrowserJFX.REMOTE_HTML);
			browser.showURL();
		});
		browser.fireButtonGo(fullLink);
	}

	/** Implementing ActionListener method. */
	@SuppressWarnings("restriction")
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == this.userguideButton) {
			String input = guideURL.toExternalForm();
			Platform.runLater(()-> {
				browser.setTextInputCache(input);
				browser.inputURLType(input);
				browser.showURL();
			});
		} 
		
		else if (source == this.aboutButton) {
			String input = aboutURL.toExternalForm();
			Platform.runLater(()-> {
				browser.setTextInputCache(input);
				browser.inputURLType(input);
				browser.showURL();
			});
		} 

		else if (source == this.tutorialButton) {
			String input = tutorialURL.toExternalForm();
			Platform.runLater(()-> {
				browser.setTextInputCache(input);
				browser.inputURLType(input);
				browser.showURL();
			});
		} 
		
		else if (source == this.projectsiteButton) {
			Platform.runLater(()-> {
				browser.setTextInputCache(projectsiteURLstring);
				browser.inputURLType(projectsiteURLstring);
				browser.showURL();
			});
			
		} else if (source == this.discussionButton) {
			Platform.runLater(()-> {
				browser.setTextInputCache(discussionURLstring);
				browser.inputURLType(discussionURLstring);
				browser.showURL();
			});

		}
		
	}

	
	/**
	 * Implement ComponentListener interface.
	 * Make sure the text is scrolled to the top.
	 * Need to find a better way to do this
	 * @author Scott
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		viewPort.setViewPosition(new Point(0, 0));
	}

	public void componentMoved(ComponentEvent e) {}

	public void componentShown(ComponentEvent e) {}

	public void componentHidden(ComponentEvent e) {}

	/**
	 * Prepare tool window for deletion.
	 */
	@Override
	public void destroy() {
	}
}