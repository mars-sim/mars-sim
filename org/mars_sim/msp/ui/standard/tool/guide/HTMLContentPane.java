/**
 * Mars Simulation Project
 * GuideWindow.java
 * @version 2.84 2008-03-24
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.standard.tool.guide;

import java.net.URL;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JEditorPane;

class HTMLContentPane extends JEditorPane {

  private List history = new ArrayList();
    private static String CLASS_NAME = 
	    "org.mars_sim.msp.ui.standard.tool.guide.HTMLContentPane";

  private static Logger logger = Logger.getLogger(CLASS_NAME);

  private int historyIndex;

  public HTMLContentPane() {
    setEditable(false);
  }

  public void goToURL(URL url) {
    displayPage(url);
    history.add(url);
    historyIndex = history.size() - 1;
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

  private void displayPage(URL pageURL) {
    try {
      setPage(pageURL);
    } catch (IOException ioException) {
          logger.log(Level.SEVERE, "Attempted to read a bad URL: "+pageURL.toString(), ioException);
    }
  }
}
