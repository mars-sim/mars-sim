/**
 * Mars Simulation Project
 * AboutWindow.java
 * @version 2.90 2010-07-27
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.swing.tool.about;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.*;

import org.mars_sim.msp.ui.swing.HTMLContentPane;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/** The AboutWindow is a tool window that displays credits
 *  for the Mars Simulation Project.
 */
public class AboutWindow extends ToolWindow implements ActionListener, ComponentListener {

	// Tool name
	public static final String NAME = "About The Mars Simulation Project";

    // Data members
    private JViewport viewPort; // The view port for the text pane
    private HTMLContentPane editorPane; // our HTML content pane
    /* [landrus, 26.11.09]: load the url in the contructor. */
    private URL guideURL;

    /** Constructs a TableWindow object
     *  @param desktop the desktop pane
     */
    public AboutWindow(MainDesktopPane desktop) {

        // Use TableWindow constructor
        super(NAME, desktop);
        

    // Create the main panel
    JPanel mainPane = new JPanel(new BorderLayout());
    mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(mainPane);

    // Create the text panel
    editorPane = new HTMLContentPane();
    editorPane.setBackground(Color.lightGray);
    editorPane.setBorder(new EmptyBorder(2, 2, 2, 2));
    editorPane.setEditable(false);

    JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(new MarsPanelBorder());
    viewPort = scrollPane.getViewport();
    viewPort.addComponentListener(this);
    viewPort.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

    mainPane.add(scrollPane);

    // Have to define a starting size
    setSize(new Dimension(475, 375));
    /* [landrus, 26.11.09]: use classloader compliant paths */
    guideURL = getClass().getResource("/docs/help/about.html");
    editorPane.goToURL(guideURL);

    // Prevent the window from being resized by the user.
    setResizable(false);

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