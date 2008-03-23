/**
 * Mars Simulation Project
 * GuideWindow.java
 * @version 2.84 2008-03-19
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.standard.tool.guide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;
import org.mars_sim.msp.ui.standard.tool.guide.HTMLContentPane;


/** The GuideWindow is a tool window that displays
 *  the built-in User Guide.
 */
public class GuideWindow extends ToolWindow implements ActionListener, HyperlinkListener, ComponentListener {

	// Tool name
	public static final String NAME = "User Guide";
        private List history = new ArrayList();
        private int historyIndex;

    private static String CLASS_NAME = 
	    "org.mars_sim.msp.ui.standard.tool.guide.GuideWindow";

    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // Data members
    private JViewport viewPort; // The view port for the text pane
    private HTMLContentPane htmlPane; // our HTML content pane
    private URL guideURL = GuideWindow.class.getResource("../../../../../../../docs/help/userguide.html");
    private URL tempURL; // for debugging


    /** Constructs a TableWindow object
     *  @param desktop the desktop pane
     */
    public GuideWindow(MainDesktopPane desktop) {

        // Use TableWindow constructor
        super(NAME, desktop);
        

    // Create the main panel
    JPanel mainPane = new JPanel(new BorderLayout());
    mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(mainPane);

    JButton topButton = new JButton ("Top");
    topButton.setActionCommand("top");
    topButton.setToolTipText("Go to Top");
    topButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
    htmlPane.goToURL(guideURL);
				}
        }
    );

    JButton backButton = new JButton ("Back");
    backButton.setActionCommand("back");
    backButton.setToolTipText("Back");
    backButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
	URL tempURL= htmlPane.back();			
				}
        });
    JButton forwardButton = new JButton ("Forward");
    forwardButton.setActionCommand("forward");
    forwardButton.setToolTipText("Forward");
    forwardButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
	URL tempURL= htmlPane.forward();
				}
        });


    //A toolbar to hold all our buttons
    JPanel toolPanel = new JPanel();
    toolPanel.add(topButton);
    toolPanel.add(backButton);
    toolPanel.add(forwardButton);

    htmlPane = new HTMLContentPane();
    htmlPane.setEditable(false);
    htmlPane.addHyperlinkListener(this);
	htmlPane.goToURL(guideURL);

    htmlPane.setBackground(Color.lightGray);
    htmlPane.setBorder(new EmptyBorder(2, 2, 2, 2));
    htmlPane.addHyperlinkListener(this);

    JScrollPane scrollPane = new JScrollPane(htmlPane);
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

    // Show the window
    setVisible(true);

}

  public void hyperlinkUpdate(HyperlinkEvent event) {
    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        htmlPane.goToURL(event.getURL());
    }
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