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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;


import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;

/** The GuideWindow is a tool window that displays
 *  the built-in User Guide.
 */
public class GuideWindow extends ToolWindow implements ActionListener, ComponentListener {

	// Tool name
	public static final String NAME = "User Guide";
	
    private static String CLASS_NAME = 
	    "org.mars_sim.msp.ui.standard.tool.guide.GuideWindow";

    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // Data members
    private JButton topButton; // The button that takes you back to the top
    private JViewport viewPort; // The view port for the text pane


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
				}
        });
    JButton backButton = new JButton ("Back");
    backButton.setActionCommand("back");
    backButton.setToolTipText("Back");
    backButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
				}
        });
    JButton forwardButton = new JButton ("Forward");
    forwardButton.setActionCommand("forward");
    forwardButton.setToolTipText("Forward");
    forwardButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
				}
        });


    //A toolbar to hold all our buttons
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false); // could be removed
    toolBar.add(topButton);
    toolBar.add(backButton);
    toolBar.add(forwardButton);

    // Create the text panel
    JEditorPane editorPane = new JEditorPane();
    editorPane.setBackground(Color.lightGray);
    editorPane.setBorder(new EmptyBorder(2, 2, 2, 2));
    editorPane.setEditable(false);

    JScrollPane scrollPane = new JScrollPane(editorPane);
    viewPort = scrollPane.getViewport();
    viewPort.addComponentListener(this);
    viewPort.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

    mainPane.add(scrollPane);
    mainPane.add(toolBar, BorderLayout.NORTH);

        // Have to define a starting size
        setSize(new Dimension(475, 375));

    // Try to load the Guide text
    java.net.URL guideURL = GuideWindow.class.getResource("../../../../../../../docs/help/userguide.html");
                       
   if (guideURL != null) {
      try {
          editorPane.setPage(guideURL);
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Attempted to read a bad URL: " + guideURL, e);
        }
    } else {
       logger.log(Level.SEVERE, "Couldn't find file: userguide.html");
    }

    // Allow the window to be resized by the user.
    setResizable(true);
    setMaximizable(true);

    // Show the window
    setVisible(true);

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