/**
 * Mars Simulation Project
 * LandmarksXmlReader.java
 * @version 
 * @author Dalen Kruse
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/** The LandmarksXmlReader class parses the landmarks.xml XML file and
 *  ???creates surface feature unit objects???.
 */

public class LandmarksXmlReader extends MspXmlReader {

    // XML element types
    private static final int LANDMARKS_LIST = 0;
    private static final int LANDMARK = 1;
    private static final int NAME = 2;
    private static final int LOCATION = 4;
    private static final int LATITUDE = 5;
    private static final int LONGITUDE = 6;

    // Data members
    private int elementType; // The current element type being parsed
    private Mars mars; // The virtual Mars instance
    private String currentName; // The current landmark name parsed
    private String currentLatitude; // The current latitude string parsed
    private String currentLongitude; // The current longitude string parsed
    private Coordinates currentLocation; // The current settlement location created
    private Landmark[] landmarks; // Collection of all landmarks
    private int index = 0; // Array index


    /** Constructor
     */

    public LandmarksXmlReader() {
        super("landmarks");
    }


    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */

    public void startElement(String name) throws Exception {
        super.startElement(name);
        if (name.equals("LANDMARKS_LIST")) {
            elementType = LANDMARKS_LIST;
            //System.err.println("startElement - LANDMARKS_LIST");
        }

        if (name.equals("LANDMARK")) {
            elementType = LANDMARK;
            currentName = "";
            currentLatitude = "";
            currentLongitude = "";
            currentLocation = null;
            //System.err.println("startElement - LANDMARK");
        }

        if (name.equals("NAME")) elementType = NAME;

        if (name.equals("LOCATION")) elementType = LOCATION;

        if (name.equals("LATITUDE")) elementType = LATITUDE;

        if (name.equals("LONGITUDE")) elementType = LONGITUDE;

    }


    /** Handle the end of an element by printing an event.
     *  @param name the name of the ended element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#endElement
     */

    public void endElement(String name) throws Exception {
        super.endElement(name);

        switch (elementType) {
            case NAME:
            case LOCATION:
                elementType = LANDMARK;
                break;

            case LATITUDE:
            case LONGITUDE:
                elementType = LOCATION;
                break;

            case LANDMARK:
                // Add code to add LANDMARK to a LANDMARK collection
                currentLocation = LocationConverter.createLocation(currentLatitude,
                    currentLongitude);
                //landmarks = new Landmark[];
                //landmarks[index] = new Landmark(currentName, currentLocation);
                index++;
                elementType = LANDMARKS_LIST;
                //System.err.println("endElement - LANDMARK");
                break;
        }
    }


    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */

    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);
        String data = new String(ch, start, length).trim();

        switch (elementType) {
            case NAME:
                currentName = data;
                //System.err.println("NAME = " + currentName);
                break;

            case LATITUDE:
                currentLatitude = data;
                //System.err.println("LATITUDE = " + currentLatitude);
                break;

            case LONGITUDE:
                currentLongitude = data;
                //System.err.println("LONGITUDE = " + currentLongitude);
                break;

        }
    }


    /** Returns the array of landmarks created from the XML file.
     *  @return the array of landmarks
     */

    public Landmark[] getLandmarks() {
        return landmarks;
    }
}
