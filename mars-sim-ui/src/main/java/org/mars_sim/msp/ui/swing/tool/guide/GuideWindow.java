/**
 * Mars Simulation Project
 * GuideWindow.java
 * @version 3.07 2014-12-06

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
	/** our HTML content pane. */
	//private HTMLContentPane htmlPane;
	//private URL guideURL = GuideWindow.class.getClassLoader().getResource("docs" + File.separator +
	//        "help" + File.separator + "userguide.html");
	/* [landrus, 27.11.09]: load the url in the constructor. */
	private URL guideURL;

	private JButton homeButton = new JButton(Msg.getString("GuideWindow.button.home")); //$NON-NLS-1$
	private JButton backButton = new JButton(Msg.getString("GuideWindow.button.back")); //$NON-NLS-1$
	private JButton forwardButton = new JButton(Msg.getString("GuideWindow.button.forward")); //$NON-NLS-1$

	private BrowserJFX browser;
	private JPanel browserPanel;
	/**
	 * Constructor.
	 * @param desktop the desktop pane
	 */
	public GuideWindow(MainDesktopPane desktop) {
		// Use TableWindow constructor
		super(NAME, desktop);
	   	//logger.info("GuideWindow's constructor is on " + Thread.currentThread().getName() + " Thread");

		/* [landrus, 27.11.09]: use classloader compliant paths */
		guideURL = getClass().getResource(Msg.getString("doc.guide")); //$NON-NLS-1$

		//SwingUtilities.invokeLater(new Runnable() {
		//	public void run() {
				browser = new BrowserJFX(this);
				browserPanel = browser.init();
				//browser.loadURL("http://mars-sim.sourceforge.net");
		//	}
		//});

		// Create the main panel
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		homeButton.setToolTipText(Msg.getString("GuideWindow.tooltip.home")); //$NON-NLS-1$
		homeButton.addActionListener(this);
		backButton.setToolTipText(Msg.getString("GuideWindow.tooltip.back")); //$NON-NLS-1$
		backButton.addActionListener(this);
		forwardButton.setToolTipText(Msg.getString("GuideWindow.tooltip.forward")); //$NON-NLS-1$
		forwardButton.addActionListener(this);

		// A toolbar to hold all our buttons
		JPanel toolPanel = new JPanel();
		toolPanel.add(homeButton);
		toolPanel.add(backButton);
		toolPanel.add(forwardButton);

		//browser.addHyperlinkListener(this);
		
		//goToURL(guideURL);
		
		
	   	//logger.info("GuideWindow's constructor: initialize buttons and toolPanel");
/*
		htmlPane = new HTMLContentPane();
		htmlPane.addHyperlinkListener(this);
		htmlPane.goToURL(guideURL);
		htmlPane.setBackground(Color.lightGray); (65,65,65)
		htmlPane.setBorder(new EmptyBorder(2, 2, 2, 2));
		JScrollPane scrollPane = new JScrollPane(htmlPane);

	   	//logger.info("GuideWindow's constructor: initialize htmlPane");
*/
		//JScrollPane scrollPane = new JScrollPane(browserPanel);
		//scrollPane.setBorder(new MarsPanelBorder());
		//viewPort = scrollPane.getViewport();
		//viewPort.addComponentListener(this);
		//viewPort.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);	
		//mainPane.add(scrollPane, BorderLayout.CENTER);
		
		mainPane.add(browserPanel, BorderLayout.CENTER);
		mainPane.add(toolPanel, BorderLayout.NORTH);

	   	//logger.info("GuideWindow's constructor: initialize mainPane");
		// We have to define a starting size
		//setSize(new Dimension(575, 475));
		setSize(new Dimension(1024, 640));
	   	//logger.info("GuideWindow's constructor: setSize");
		// Allow the window to be resized by the user.
		//setResizable(true);
		setMaximizable(true);
		// Show the window
		setVisible(true);
	   	//logger.info("GuideWindow's constructor: done!");

		updateButtons();
	}

	/**
	 * Handles a click on a link.
	 * @param event the HyperlinkEvent
	 */
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			goToURL(event.getURL());
			updateButtons();
		}
	}

	/**
	 * Updates navigation buttons.
	 */
	public void updateButtons() {
		//System.out.println("GuideWindow: Calling updateButtons(). historyIndex is " + historyIndex);
		homeButton.setEnabled(true);
		backButton.setEnabled(!isFirst());
		forwardButton.setEnabled(!isLast());
	}

	/**
	 * Set a display URL .
	 */
	public void setURL(String fileloc) {
		//htmlPane.goToURL(getClass().getResource(fileloc));
		//String path = getClass().getResource(fileloc).toExternalForm();
		//browser.loadLocalURL(path);
		//browser.loadLocalURL(getClass().getResource(fileloc).toExternalForm());
		//System.out.println("GuideWindow's setURL() : fileloc is " + fileloc);
		//System.out.println("GuideWindow's setURL() : URL is " + getClass().getResource(fileloc));
		goToURL(getClass().getResource(fileloc));
	}

	/** Implementing ActionListener method. */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == this.homeButton) {
			//browser.loadLocalURL(guideURL.toExternalForm());
			goToURL(guideURL);
			updateButtons();
		} else if (source == this.backButton) {
			back();
			updateButtons();
		} else if (source == this.forwardButton) {
			forward();
			updateButtons();
		}
	}


	public void goToURL(URL url) {
		//System.out.println("GuideWindow's goToURL()");
		displayPage(url);
		updateHistory(url);
	}
	
	// 2016-04-18 Added updateHistory()
	public void updateHistory(URL url) {
		if (historyIndex < history.size() - 1) {
			historyIndex++;
			history.set(historyIndex, url);
			while (historyIndex < history.size() - 1) {
				history.remove(historyIndex + 1);
			}
		}
		else {
			history.add(url);
			historyIndex = history.size() - 1;
		}
	}

	public URL forward() {
		historyIndex++;
		if (historyIndex >= history.size()) {
			historyIndex = history.size() - 1;
		}
		URL url = history.get(historyIndex);
		displayPage(url);

		return url;
	}

	public URL back() {
		historyIndex--;
		if (historyIndex < 0) {
			historyIndex = 0;
		}
		URL url = history.get(historyIndex);
		displayPage(url);

		return url;
	}

	public boolean isFirst() {
		return (historyIndex == 0);
	}

	public boolean isLast() {
		return (historyIndex == history.size() - 1);
	}

	private void displayPage(URL pageURL) {
	    SwingUtilities.invokeLater(() -> {
	    	String path = pageURL.toExternalForm();
			browser.loadLocalURL(path);
			//System.out.println("GuideWindow's displayPage() : URL path is " + path);
			//setPage(pageURL);
		});
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

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	/**
	 * Prepare tool window for deletion.
	 */
	@Override
	public void destroy() {
	}
}