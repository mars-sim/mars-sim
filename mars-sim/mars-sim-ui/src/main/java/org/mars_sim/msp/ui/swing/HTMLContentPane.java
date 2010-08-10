/**
 * Mars Simulation Project
 * GuideWindow.java
 * @version 3.00 2010-08-10
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.swing;

import java.net.URL;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;

/** The HTMLContentPane class provides a pane for displaying
 *  the help files in the Guide window.
 */

public class HTMLContentPane extends JEditorPane {

  private List<URL> history = new ArrayList<URL>();
    private static String CLASS_NAME = 
	    "org.mars_sim.msp.ui.standard.HTMLContentPane";

  private static Logger logger = Logger.getLogger(CLASS_NAME);

  private int historyIndex;

  public HTMLContentPane() {
    setEditable(false);
  }

  public void goToURL(URL url) {
    displayPage(url);
    if (historyIndex < history.size()-1)
      {
    historyIndex++;
    history.set(historyIndex, url);
    while (historyIndex < history.size()-1)
         {
           history.remove(historyIndex+1);
         }
      }
    else
      {
    history.add(url);
    historyIndex = history.size() - 1;
      }
  }

  public URL forward() {
    historyIndex++;
    if (historyIndex >= history.size())
      historyIndex = history.size() - 1;

    URL url = (URL) history.get(historyIndex);
    displayPage(url);

    return url;
  }

  public URL back() {
    historyIndex--;
    if (historyIndex < 0)
      historyIndex = 0;
    URL url = (URL) history.get(historyIndex);
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
    try {
      setPage(pageURL);
    } catch (IOException ioException) {
          logger.log(Level.SEVERE, "Attempted to read a bad URL: "+pageURL.toString(), ioException);
    }
  }
}
