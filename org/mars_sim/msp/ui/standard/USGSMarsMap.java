/**
 * Mars Simulation Project
 * USGSMarsMap.java
 * @version 2.71 2000-10-08
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.Coordinates;
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

/** Access the Mars maps provided by the The Unites States Geological
 *  Survey - Astrogeology Team and The Planetary Data System - Imaging
 *  Node. Specifically from the Planetary Data Systems (PDS) Mars
 *  Explorer. Behind their web server is a Solaris application called
 *  MapMaker that generates the maps.
 *  @see http://www-pdsimage.wr.usgs.gov/PDS/public/mapmaker/faq.htm
 *  Be sure to see the FAQ on the "face".
 */
public class USGSMarsMap implements Map {

    // Data members
    private static final String psdUrl = "http://www-pdsimage.wr.usgs.gov";
    private static final String psdCgi = "/cgi-bin/panpic.cgi";
    private static final String projection = "SIMP";
    private static final String resolution = "3";
    private static final String dataSet = "merged.des";

    private boolean imageDone = false;
    private Component component;

    private Image img;
    private Coordinates currentView; // for future use

    private Image prefetchedImage;

    /** Constructs a USGSMarsMap object */
    public USGSMarsMap() {}

    /** Constructs a USGSMarsMap object */
    public USGSMarsMap(Component comp) {
        component = comp;
    }

    /** creates a 2D map at a given center point */
    public void drawMap(Coordinates newCenter) {
        if (imageInCache(newCenter)) {
            // simply translate the image
        } else {
            img = retrieveImage(3, // pixels per degree
                                90 - Math.toDegrees(newCenter.getPhi()),
                                        360 - Math.toDegrees(newCenter.getTheta()));
            prefetchImage(10, // pixels per degree
                          90 - Math.toDegrees(newCenter.getPhi()),
                                  360 - Math.toDegrees(newCenter.getTheta()));
            currentView = newCenter;
            waitForMapLoaded();
        }
    }

    /** determines if a requested map is complete */
    public boolean isImageDone() {
        return imageDone;
    }

    /** Returns map image */
    public Image getMapImage() {
        return img;
    }

    /** Returns true if map image is cached */
    private boolean imageInCache(Coordinates location) {
        return false;
    }

    /** requests an image from the PDS web server.
      *  @param size is pixels per degree
      */
    private Image retrieveImage(int size, double lat, double lon) {
        imageDone = false;
        try {
            URL url =
                    new URL(psdUrl + psdCgi + "?DATA_SET_NAME=" + dataSet + "&PROJECTION=" +
                    projection + "&RESOLUTION=" + resolution + "&SIZE=" + size +
                    "&LAT=" + lat + "&LON=" + lon);

            System.out.println(url);

            BufferedReader in =
                    new BufferedReader(new InputStreamReader(url.openStream()));
            String result = null;
            String line;
            String imageSrc;

            // <fragile>
            int count = 0;

            while ((line = in.readLine()) != null) {
                count++;
                if (count == 6) {
                    result = line;
                    in.close();
                    break;
                }
            }
            int startIndex = result.indexOf("IMG SRC=\"") + 9;
            int endIndex = result.indexOf("\"", startIndex);
            imageSrc = result.substring(startIndex, endIndex);
            // </fragile>

            URL imageUrl = new URL(psdUrl + imageSrc);
            System.out.println(imageUrl);

            //imageUrl = new URL("file:tmp.968014862.jpg");
            return (Toolkit.getDefaultToolkit().getImage(imageUrl));

        } catch (MalformedURLException e) {
            System.out.println("Weirdness" + e);
        }
        catch (IOException e) {
            // should deal with the case where a user has no internet connection
            System.out.println("Weirdness" + e);
        }

        return null;
    }

    private void prefetchImage(int size, double lat, double lon) {
        prefetchedImage = retrieveImage(size, lat, lon);
    }

    private void waitForMapLoaded() {
        MediaTracker tracker = new MediaTracker(component);
        tracker.addImage(img, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        imageDone = true;
        System.out.println("Done loading USGS image");
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

    /** for component testing */
    public static void main(String argv[]) {
        USGSMarsMap map = new USGSMarsMap();
        map.retrieveImage(3, 0, 0);
        map.test();
    }
}
