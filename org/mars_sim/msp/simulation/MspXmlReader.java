/**
 * Mars Simulation Project
 * MspXmlReader.java
 * @version 2.73 2001-10-23
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import com.microstar.xml.*;

abstract class MspXmlReader extends HandlerBase {

    private String documentName;

    public MspXmlReader(String documentName) {

        this.documentName = documentName;
    }

    public void parse() {
        
        try {
            Reader reader = new FileReader(documentName);
            XmlParser parser = new XmlParser();
            parser.setHandler(this);
            parser.parse(null, null, reader);
            reader.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

  /**
    * Resolve an entity and print an event.
    * @see com.microstar.xml.XmlHandler#resolveEntity
    */
  public Object resolveEntity (String publicId, String systemId)
  {
    System.out.println("Resolving entity: pubid="+
                       publicId + ", sysid=" + systemId);
    return null;
  }

  /**
    * Handle the start of the document by printing an event.
    * @see com.microstar.xml.XmlHandler#startDocument
    */
  public void startDocument ()
  {
    System.out.println("Start document");
  }


  /**
    * Handle the end of the document by printing an event.
    * @see com.microstar.xml.XmlHandler#endDocument
    */
  public void endDocument ()
  {
    System.out.println("End document");
  }


  /**
    * Handle an attribute value assignment by printing an event.
    * @see com.microstar.xml.XmlHandler#attribute
    */
  public void attribute (String name, String value, boolean isSpecified)
  {
    String s;
    if (isSpecified) {
      s = " (specified)";
    } else {
      s = " (defaulted)";
    }
    System.out.println("Attribute:  name=" + name + ", value=" + value + s);
  }


  /**
    * Handle the start of an element by printing an event.
    * @see com.microstar.xml.XmlHandler#startElement
    */
  public void startElement (String name)
  {
    System.out.println("Start element:  name=" + name);
  }


  /**
    * Handle the end of an element by printing an event.
    * @see com.microstar.xml.XmlHandler#endElement
    */
  public void endElement (String name)
  {
    System.out.println("End element:  " + name);
  }


  /**
    * Handle character data by printing an event.
    * @see com.microstar.xml.XmlHandler#charData
    */
  public void charData (char ch[], int start, int length)
  {
    String data = new String(ch, start, length).trim();
    if (!data.equals("")) {
        System.out.println("Character data:  \"" + data + '"');
    }
  }
}

