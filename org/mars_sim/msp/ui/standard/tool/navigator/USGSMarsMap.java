/**
 * Mars Simulation Project
 * USGSMarsMap.java
 * @version 2.75 2003-08-03
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import org.mars_sim.msp.simulation.Coordinates;
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

/** 
 * Access the Mars maps provided by the The Unites States Geological
 * Survey - Astrogeology Team and The Planetary Data System - Imaging
 * Node. Specifically from the Planetary Data Systems (PDS) Mars
 * Explorer. Behind their web server is a Solaris application called
 * MapMaker that generates the maps.
 * @see <a href="http://www-pdsimage.wr.usgs.gov/PDS/public/mapmaker/faq.htm" target="_top">
 * PDS Mars Explorer</a>
 * - Be sure to see the FAQ on the "face".
 */
public class USGSMarsMap implements Map {

    // Data members
   
    private static final String psdUrl = "http://pdsmaps.wr.usgs.gov";
    private static final String psdCgi = "/explorer-bin/mapmars3.cgi";
    
    //private static final String psdUrl = "http://www-pdsimage.wr.usgs.gov";
    //private static final String psdCgi = "/cgi-bin/panpic.cgi";

    //private static final String psdUrl = "http://192.168.1.50";
    //private static final String psdUrl = "http://63.229.31.9";
    //private static final String psdCgi = "/cgi-bin/mapmaker/mapmaker.py";

    private static final String projection = "MERCATOR";
    private static final String stretch = "AUTO";
    private static final String gridlineFrequency = "none";
    private static final String scale = "pixels/degree";
    private static final String resolution = "64";
    private static final String latbox = "5";
    private static final String lonbox = "5";
    private static final String bandsSelected = "1";
    private static final String dataSet = "mars_viking_merged";
    private static final String version = "ADVANCED";
    private static final String pixelType = "BIT8";

    private boolean imageDone = false;
    private Component component;

    private Image img;
    private Coordinates currentView; // for future use

    private Image prefetchedImage;

    /** Constructs a USGSMarsMap object */
    public USGSMarsMap() {}

    /** Constructs a USGSMarsMap object 
     *  @param comp the map's container component
     */
    public USGSMarsMap(Component comp) {
        component = comp;
    }

    /** creates a 2D map at a given center point 
     *  @param newCenter the center location
     */
    public void drawMap(Coordinates newCenter) {
        if (imageInCache(newCenter)) {
            // simply translate the image
        } else {
            img = retrievePdsImage(90 - Math.toDegrees(newCenter.getPhi()),
                                   360 - Math.toDegrees(newCenter.getTheta()));
            // prefetchImage(90 - Math.toDegrees(newCenter.getPhi()),
	    //		  360 - Math.toDegrees(newCenter.getTheta()));
            currentView = newCenter;
            waitForMapLoaded();
        }
    }

    /** determines if a requested map is complete 
     *  @return true if requested map is complete
     */
    public boolean isImageDone() {
        return imageDone;
    }

    /** Returns map image 
     *  @return map image
     */
    public Image getMapImage() {
        return img;
    }

    /** Returns true if map image is cached 
     *  @return true if map image is cached
     */
    private boolean imageInCache(Coordinates location) {
        return false;
    }

    /** requests an image from the PDS web server.
     *  @param lat latitude
     *  @param lon longitude
     */
    private Image retrievePdsImage(double lat, double lon) {
        imageDone = false;
        try {
            //URL url =
            //        new URL(psdUrl + psdCgi + "?DATA_SET_NAME=" + dataSet + "&PROJECTION=" +
            //        projection + "&RESOLUTION=" + resolution + "&SIZE=" + size +
            //        "&LAT=" + lat + "&LON=" + lon);
	    StringBuffer urlBuff = new StringBuffer(psdUrl + psdCgi + "?");
	    urlBuff.append("DATA_SET_NAME=" + dataSet);
	    urlBuff.append("&VERSION=" + version);
	    urlBuff.append("&PIXEL_TYPE=" + pixelType);
	    urlBuff.append("&PROJECTION=" + projection);
	    urlBuff.append("&STRETCH=" + stretch);
	    urlBuff.append("&GRIDLINE_FREQUENCY=" + gridlineFrequency);
	    urlBuff.append("&SCALE=" + URLEncoder.encode(scale));
	    urlBuff.append("&RESOLUTION=" + resolution);
	    urlBuff.append("&LATBOX=" + latbox);
	    urlBuff.append("&LONBOX=" + lonbox);
	    urlBuff.append("&BANDS_SELECTED=" + bandsSelected);
	    urlBuff.append("&LAT=" + lat);
	    urlBuff.append("&LON=" + lon);
            URL url = new URL(urlBuff.toString());

            // System.out.println(url);

            BufferedReader in =
                    new BufferedReader(new InputStreamReader(url.openStream()));
            String result = null;
            String line;
            String imageSrc;

            // <fragile>
            int count = 0;
            while ((line = in.readLine()) != null) {
                if (count == 6) result = line;
		count++;
            }
	    // System.out.println(result);
            int startIndex = result.indexOf("<TH COLSPAN=2 ROWSPAN=2><IMG SRC = \"") + 36;
            int endIndex = result.indexOf("\"", startIndex);
            imageSrc = result.substring(startIndex, endIndex);
            // </fragile>

            URL imageUrl = new URL(imageSrc);
            // System.out.println(imageUrl);

            return (Toolkit.getDefaultToolkit().getImage(imageUrl));
	    // return null;

        } catch (MalformedURLException e) {
            // System.out.println("Weirdness" + e);
        }
        catch (IOException e) {
            // should deal with the case where a user has no internet connection
            // System.out.println("Weirdness" + e);
        }

        return null;
    }

    /** requests an image from the PDS web server.
     *  @param size pixels per degree
     *  @param lat latitude
     *  @param lon longitude
     */
    private Image retrieveImage(int size, double lat, double lon) {
        imageDone = false;
        try {
            URL imageUrl =
                    new URL(psdUrl + psdCgi + "?DATA_SET_NAME=" + dataSet + "&PROJECTION=" +
                    projection + "&RESOLUTION=" + resolution + "&SIZE=" + size +
                    "&LAT=" + lat + "&LON=" + lon);

            System.out.println(imageUrl);

            //imageUrl = new URL("file:tmp.968014862.jpg");
            return (Toolkit.getDefaultToolkit().getImage(imageUrl));

        } catch (MalformedURLException e) {
            System.out.println("Weirdness" + e);
        }

        return null;
    }

    /** Prefetch the map image
     *  @param lat latitude
     *  @param lon longitude
     */
    private void prefetchImage(double lat, double lon) {
        prefetchedImage = retrievePdsImage(lat, lon);
    }

    /** Wait for USGS map image to load */
    private void waitForMapLoaded() {
        MediaTracker tracker = new MediaTracker(component);
        tracker.addImage(img, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        imageDone = true;
        // System.out.println("Done loading USGS image");
    }

    /** for component testing. Creates a frame and fills it with a map */
    private void test() {
        JFrame j = new JFrame("USGSMarsMap test");
        component = j;
        j.setSize(300, 300);
        j.setVisible(true);
        Graphics g = j.getGraphics();
        waitForMapLoaded();
        g.drawImage(img, 0, 0, null);
    }

    /** for component testing 
     *  @param argv an array of command line arguments
     */
    public static void main(String argv[]) {
        USGSMarsMap map = new USGSMarsMap();
        map.retrievePdsImage(0, 0);
        map.test();
    }
}
