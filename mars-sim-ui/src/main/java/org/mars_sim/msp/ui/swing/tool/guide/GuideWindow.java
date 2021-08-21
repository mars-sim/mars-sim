/*
 * Mars Simulation Project
 * GuideWindow.java
 * @date 2021-08-15
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.swing.tool.guide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import com.alee.extended.canvas.WebCanvas;
import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.link.UrlLinkAction;
import com.alee.extended.link.WebLink;
import com.alee.extended.statusbar.WebStatusBar;
import com.alee.laf.button.WebButton;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The GuideWindow is a tool window that displays built-in html pages such as User Guide, Quick Tutorial, Keyboard Shortcuts, etc.
 */
@SuppressWarnings("serial")
public class GuideWindow extends ToolWindow implements ActionListener, HyperlinkListener {

	/** Tool name. */
	public static final String NAME = Msg.getString("GuideWindow.title"); //$NON-NLS-1$
	public static final String HOME_ICON = Msg.getString("img.home"); //$NON-NLS-1$
	
	public static final String WIKI_URL = Msg.getString("GuideWindow.githubwiki.url"); //$NON-NLS-1$
	public static final String WIKI_TEXT = Msg.getString("GuideWindow.githubwiki.title"); //$NON-NLS-1$
    
	public static WebLink link;
	
	private WebStyledLabel urlLabel;
	
	/** Data members. */
	/** our HTML content pane. */
	private HTMLContentPane htmlPane;
	/** The view port for the text pane. */
	private JViewport viewPort;
	/** The guide window URL. */
	private URL guideURL;
	/** The What's New URL. */
	private URL whatsNewURL;
	
	private ImageIcon icon = new ImageIcon(GuideWindow.class.getResource(HOME_ICON));
	
	private WebButton homeButton = new WebButton(icon);
	private WebButton backButton = new WebButton("<");
	private WebButton forwardButton = new WebButton(">");

	/**
	 * Constructor.
	 * 
	 * @param desktop
	 *            the desktop pane
	 */
	public GuideWindow(MainDesktopPane desktop) {
		super(NAME, desktop);

		EventQueue.invokeLater(new Runnable(){
	        public void run() {
	        	init();            
	        }
	    });   
	}
	
	public void init() {
		
		guideURL = getClass().getResource(Msg.getString("doc.guide")); //$NON-NLS-1$
		whatsNewURL = getClass().getResource(Msg.getString("doc.whatsnew")); //$NON-NLS-1$
				
		homeButton.setToolTipText(Msg.getString("GuideWindow.tooltip.home")); //$NON-NLS-1$
		homeButton.setSize(16, 16);
		homeButton.addActionListener(this);

		backButton.setToolTipText(Msg.getString("GuideWindow.tooltip.back")); //$NON-NLS-1$
		backButton.addActionListener(this);

		forwardButton.setToolTipText(Msg.getString("GuideWindow.tooltip.forward")); //$NON-NLS-1$
		forwardButton.addActionListener(this);

		// Create the main panel
		JPanel mainPane = new JPanel(new BorderLayout());
		setContentPane(mainPane);

		JPanel topPanel = new JPanel(new BorderLayout());
		mainPane.add(topPanel, BorderLayout.NORTH);
		
		// A toolbar to hold all our buttons
		JPanel homePanel = new JPanel(new FlowLayout(3, 3, FlowLayout.CENTER));
		topPanel.add(homePanel, BorderLayout.CENTER);
		
		homePanel.add(backButton);
		homePanel.add(homeButton);
		homePanel.add(forwardButton);
		
		JPanel linkPanel = new JPanel(new FlowLayout(3, 3, FlowLayout.TRAILING));
		topPanel.add(linkPanel, BorderLayout.EAST);
		
		link = new WebLink(StyleId.linkShadow, new UrlLinkAction(WIKI_URL));
		link.setAlignmentY(1f);
		link.setText(WIKI_TEXT);
		TooltipManager.setTooltip(link, "Open mars-sim wiki in GitHub", TooltipWay.down);
		linkPanel.add(link);

		// Initialize the status bar
		mainPane.add(initializeStatusBar(),  BorderLayout.SOUTH);

		htmlPane = new HTMLContentPane();
		htmlPane.addHyperlinkListener(this);
		htmlPane.goToURL(guideURL);
		htmlPane.setContentType("text/html");
		htmlPane.setBackground(Color.lightGray);
		htmlPane.setBorder(new EmptyBorder(2, 2, 2, 2));

		JScrollPane scrollPane = new JScrollPane(htmlPane);
		viewPort = (JViewport) scrollPane.getViewport();
		viewPort.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		mainPane.add(scrollPane,  BorderLayout.CENTER);
		
		updateButtons();
		
		setResizable(false);
		setMaximizable(true);
		setVisible(true);
	
		setSize(new Dimension(800, 600));

		Dimension desktopSize = desktop.getMainWindow().getFrame().getSize();
		Dimension windowSize = getSize();

		int width = (desktopSize.width - windowSize.width) / 2;
		int height = (desktopSize.height - windowSize.height - 100) / 2;
		setLocation(width, height);
		
		// Pack window.
		// WARNING: using pack() here will shrink the window to one line tall in swing mode

		setURL(Msg.getString("doc.whatsnew")); //$NON-NLS-1$
	}

    /**
     * Initializes status bar and its content.
     */
    private WebStatusBar initializeStatusBar() {
    	WebStatusBar statusBar = new WebStatusBar();

        urlLabel = new WebStyledLabel(StyleId.styledlabelShadow);
        urlLabel.setFont(new Font("Times New Roman", Font.ITALIC, 10));
        urlLabel.setForeground(Color.DARK_GRAY);
		statusBar.add(urlLabel);

        final WebCanvas resizeCorner = new WebCanvas(StyleId.canvasGripperSE);
        statusBar.addToEnd(resizeCorner);

        return statusBar;
    }
    
    private void updateHoveringURL(String value) {
    	urlLabel.setText(value);
    }
    
	/**
	 * Set a display URL .
	 */
	public void setURL(String fileloc) {
		if (htmlPane != null)
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
	 * Handles a click on a link.
	 * @param event the HyperlinkEvent
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			updateHoveringURL(event.getURL().toString());
		}
		else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
			updateHoveringURL("");
		}
		else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
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
	 * Prepare tool window for deletion. 
	 */
	@Override
	public void destroy() {
		htmlPane = null;
		viewPort = null;
		guideURL = null;
		whatsNewURL = null;
		homeButton = null;
		backButton = null;
		forwardButton = null;
	}
}
