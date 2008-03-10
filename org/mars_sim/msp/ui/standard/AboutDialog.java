/**
 * Mars Simulation Project
 * AboutDialog.java
 * @version 2.83 2008-02-29
 * @author Scott Davis
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.standard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;



/** The AboutDialog is an information window that is called from the
 *  "About The Mars Simulation Project" item in the MainWindowMenu.
 *  It provides information about the project, credit to contributors and the GPL license.
 */
public class AboutDialog extends JDialog implements ActionListener, ComponentListener {
    
    private static String CLASS_NAME = 
	    "org.mars_sim.msp.ui.standard.AboutDialog";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // Data members
    private JButton closeButton; // The close button
    private JViewport viewPort; // The view port for the text pane

    /** Constructs an AboutDialog object 
     *  @param mainWindow the main window
     */
    public AboutDialog(MainWindow mainWindow) {

    // Use JDialog constructor
    super(mainWindow, "About The Mars Simulation Project", true);

    // Create the main panel
    JPanel mainPane = new JPanel(new BorderLayout());
    mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(mainPane);

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

    // Create close button panel
    JPanel closeButtonPane = new JPanel(new FlowLayout());
    closeButtonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    mainPane.add(closeButtonPane, "South");

    // Create close button
    closeButton = new JButton("Close");
    closeButton.addActionListener(this);
    closeButtonPane.add(closeButton);

    // Set the size of the window
    setSize(400, 400);

    // Try to load the About text
    java.net.URL aboutURL = AboutDialog.class.getResource("about.html");
                       
   if (aboutURL != null) {
      try {
          editorPane.setPage(aboutURL);
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Attempted to read a bad URL: " + aboutURL, e);
        }
    } else {
       logger.log(Level.SEVERE, "Couldn't find file: about.html");
    }

    // Center the window on the parent window.
    Point parentLocation = mainWindow.getLocation();
    int Xloc = (int) parentLocation.getX() + ((mainWindow.getWidth() - 350) / 2);
    int Yloc = (int) parentLocation.getY() + ((mainWindow.getHeight() - 400) / 2);
    setLocation(Xloc, Yloc);

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
}
