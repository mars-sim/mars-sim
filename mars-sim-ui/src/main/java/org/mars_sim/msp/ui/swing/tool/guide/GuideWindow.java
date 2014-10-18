/**
 * Mars Simulation Project
 * GuideWindow.java
 * @version 3.06 2014-01-29
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
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.HTMLContentPane;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/**
 * The GuideWindow is a tool window that displays the built-in User Guide,
 * About Box and Tutorial.
 */
public class GuideWindow
extends ToolWindow
implements ActionListener, HyperlinkListener, ComponentListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Tool name
	public static final String NAME = Msg.getString("GuideWindow.title"); //$NON-NLS-1$

	// Data members
	/** The view port for the text pane. */
	private JViewport viewPort;

	/** our HTML content pane. */
	private HTMLContentPane htmlPane;

	//private URL guideURL = GuideWindow.class.getClassLoader().getResource("docs" + File.separator + 
	//        "help" + File.separator + "userguide.html");
	/* [landrus, 27.11.09]: load the url in the constructor. */
	private URL guideURL;

	private JButton homeButton = new JButton(Msg.getString("GuideWindow.button.home")); //$NON-NLS-1$
	private JButton backButton = new JButton(Msg.getString("GuideWindow.button.back")); //$NON-NLS-1$
	private JButton forwardButton = new JButton(Msg.getString("GuideWindow.button.forward")); //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param desktop the desktop pane
	 */
	public GuideWindow(MainDesktopPane desktop) {
		// Use TableWindow constructor
		super(NAME, desktop);
		/* [landrus, 27.11.09]: use classloader compliant paths */
		guideURL = getClass().getResource(Msg.getString("doc.guide")); //$NON-NLS-1$
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

		htmlPane = new HTMLContentPane();
		htmlPane.addHyperlinkListener(this);
		htmlPane.goToURL(guideURL);

		htmlPane.setBackground(Color.lightGray);
		htmlPane.setBorder(new EmptyBorder(2, 2, 2, 2));

		JScrollPane scrollPane = new JScrollPane(htmlPane);
		scrollPane.setBorder(new MarsPanelBorder());
		viewPort = scrollPane.getViewport();
		viewPort.addComponentListener(this);
		viewPort.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

		mainPane.add(scrollPane);
		mainPane.add(toolPanel, BorderLayout.NORTH);

		// We have to define a starting size
		setSize(new Dimension(575, 475));

		// Allow the window to be resized by the user.
		setResizable(true);
		setMaximizable(true);
		updateButtons();

		// Show the window
		setVisible(true);
	}

	/**
	 * Handles a click on a link.
	 * @param event the HyperlinkEvent
	 */
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			htmlPane.goToURL(event.getURL());
			updateButtons();
		}
	}

	/**
	 * Updates navigation buttons.
	 */
	public void updateButtons() {
		homeButton.setEnabled(true);
		backButton.setEnabled(!htmlPane.isFirst());
		forwardButton.setEnabled(!htmlPane.isLast());
	}

	/**
	 * Set a display URL .
	 */
	public void setURL(String fileloc) {
		htmlPane.goToURL(getClass().getResource(fileloc));
	}

	/** Implementing ActionListener method. */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == this.homeButton) {
			htmlPane.goToURL(guideURL);
			updateButtons();
		} else if (source == this.backButton) {
			htmlPane.back();
			updateButtons();
		} else if (source == this.forwardButton) {
			htmlPane.forward();
			updateButtons();
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