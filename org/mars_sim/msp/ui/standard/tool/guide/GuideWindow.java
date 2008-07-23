/**
 * Mars Simulation Project
 * GuideWindow.java
 * @version 2.85 2008-07-23
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.standard.tool.guide;

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
import javax.swing.border.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

import org.mars_sim.msp.ui.standard.tool.ToolWindow;
import org.mars_sim.msp.ui.standard.HTMLContentPane;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;


/** The GuideWindow is a tool window that displays
 *  the built-in User Guide.
 */
public class GuideWindow extends ToolWindow implements ActionListener, HyperlinkListener, ComponentListener {

	// Tool name
	public static final String NAME = "User Guide";

    // Data members
    private JViewport viewPort; // The view port for the text pane
    private HTMLContentPane htmlPane; // our HTML content pane
    private URL guideURL = GuideWindow.class.getResource("../../../../../../../docs/help/userguide.html");
    private JButton homeButton = new JButton ("Home");
    private JButton backButton = new JButton ("Back");
    private JButton forwardButton = new JButton ("Forward");


    /** Constructs a TableWindow object
     *  @param desktop the desktop pane
     */
    public GuideWindow(MainDesktopPane desktop) {

        // Use TableWindow constructor
        super(NAME, desktop);
        

    // Create the main panel
    JPanel mainPane = new JPanel(new BorderLayout());
    mainPane.setBorder(new MarsPanelBorder());
    setContentPane(mainPane);

    homeButton.setActionCommand("home");
    homeButton.setToolTipText("Go to Home");
    homeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
    htmlPane.goToURL(guideURL);
    updateButtons();			}
        }
    );

    backButton.setActionCommand("back");
    backButton.setToolTipText("Back");
    backButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {			
	updateButtons();		}
        });

    forwardButton.setActionCommand("forward");
    forwardButton.setToolTipText("Forward");
    forwardButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
	updateButtons();	}
        });


    //A toolbar to hold all our buttons
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
    scrollPane.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                new LineBorder(Color.green)));
    viewPort = scrollPane.getViewport();
    viewPort.addComponentListener(this);
    viewPort.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

    mainPane.add(scrollPane);
    mainPane.add(toolPanel, BorderLayout.NORTH);

        // Have to define a starting size
        setSize(new Dimension(475, 375));


    // Allow the window to be resized by the user.
    setResizable(true);
    setMaximizable(true);
    updateButtons();

    // Show the window
    setVisible(true);

}
    /** Handles a click on a link
     *  @param event the HyperlinkEvent
     */
  public void hyperlinkUpdate(HyperlinkEvent event) {
    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        htmlPane.goToURL(event.getURL());
        updateButtons();
    }
  }
    /**
     *  Updates navigation buttons.
     */
  public void updateButtons() {
    homeButton.setEnabled(true);
    backButton.setEnabled(!htmlPane.isFirst());
    forwardButton.setEnabled(!htmlPane.isLast());
  }

  // Implementing ActionListener method
  public void actionPerformed(ActionEvent event) {
      dispose();
  }

  // Implement ComponentListener interface.
  // Make sure the text is scrolled to the top.
  // Need to find a better way to do this <Scott>
  public void componentResized(ComponentEvent e) {
       viewPort.setViewPosition(new Point(0, 0));
  }
  
  public void componentMoved(ComponentEvent e) {}
  public void componentShown(ComponentEvent e) {}
  public void componentHidden(ComponentEvent e) {}

    
    /**
     * Prepare tool window for deletion.
     */
    public void destroy() {
    }
}