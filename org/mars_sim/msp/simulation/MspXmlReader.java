/**
 * Mars Simulation Project
 * MspXmlReader.java
 * @version 2.73 2001-11-18
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import com.microstar.xml.*;

public abstract class MspXmlReader extends HandlerBase {

    private String documentName; // The XML document to be read

    // Constructor
    public MspXmlReader(String documentName) {
        this.documentName = documentName;
    }

    /** Parses the XML document */ 
    public void parse() {
        try {
            Reader reader = new FileReader(documentName);
            XmlParser parser = new XmlParser();
            parser.setHandler(this);
            parser.parse(null, null, reader);
            reader.close();
        }
        catch (Exception e) {
	    e.printStackTrace();
        }
    }

  /** Resolve an entity and print an event.
   *  @see com.microstar.xml.XmlHandler#resolveEntity
   */
  public Object resolveEntity (String publicId, String systemId)
  {
    System.out.println("Resolving entity: pubid="+
                       publicId + ", sysid=" + systemId);
    return null;
  }

  /** Handle an attribute value assignment by printing an event.
   *  @see com.microstar.xml.XmlHandler#attribute
   */
  public void attribute (String name, String value, boolean isSpecified)
  {
    String s;
    if (isSpecified) s = " (specified)";
    else s = " (defaulted)";
  }

  /** Handle character data by printing an event.
   *  @see com.microstar.xml.XmlHandler#charData
   */
  public void charData (char ch[], int start, int length)
  {
    String data = new String(ch, start, length).trim();
  }
}
