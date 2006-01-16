/**
 * Mars Simulation Project
 * USGSMarsMap.java
 * @version 2.76 2004-08-06
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import org.mars_sim.msp.simulation.Coordinates;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** 
 * Access the Mars maps provided by the The Unites States Geological
 * Survey - Astrogeology Team and The Planetary Data System - Imaging
 * Node. Specifically from the Planetary Data Systems (PDS) Mars
 * Explorer. Behind their web server is a Solaris application called
 * MapMaker that generates the maps.
 * @see <a href="http://www-pdsimage.wr.usgs.gov/PDS/public/mapmaker/faq.htm">PDS Mars Explorer</a>
 */
public class USGSMarsMap implements Map, ActionListener {

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
    private boolean goodConnection = false;
    private boolean connectionTimeout = false;
    private Image img;
    private Timer connectionTimer = null;

    /** Constructs a USGSMarsMap object */
    public USGSMarsMap() {}

    /** Constructs a USGSMarsMap object 
     *  @param comp the map's container component
     */
    public USGSMarsMap(Component comp) {
        component = comp;
    }

    /** 
     * Creates a 2D map at a given center point.
     *
     * @param newCenter the center location.
     * @throws Exception if error in drawing map.
     */
    public void drawMap(Coordinates newCenter) throws Exception {
     
        connectionTimeout = false;
        
        startPdsImageRetrieval(90 - Math.toDegrees(newCenter.getPhi()),
                               360 - Math.toDegrees(newCenter.getTheta()));
                               
        // Starts a 10 second timer to see if the connection times out.
        connectionTimer = new Timer(10000, this);
        connectionTimer.start();
    }

    /**
     * Checks if the connection has timed out.
     * @return boolean
     */
    public boolean isConnectionTimeout() {
        return connectionTimeout;
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

    /**
     * Starts the PDS image retrieval process.
     * @param lat the latitude of the center of the image.
     * @param lon the longitude of the center of the image.
     * @throws IOException if there is an IO problem.
     */
    private void startPdsImageRetrieval(double lat, double lon) throws IOException {
        
        imageDone = false;
        goodConnection = false;
        URL url = null;
        try {
            // Get URL connection to PDS CGI.
            url = getPDSURL(lat, lon);
            HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
            
            // Connect with PDS CGI.
            new PDSConnectionManager(urlCon, this);
        } 
        catch (MalformedURLException e) {
            System.out.println("URL not valid: " + url.toString());
            throw new IOException("URL not valid");
        }
        catch (IOException e) {
            throw new IOException("Internet connection required");
        }
        catch (Exception e) {
            throw new IOException("Exception retrieving map image");
        }
    }
    
    /**
     * Determines the URL for the USGS PDS server.
     * @param lat the latitude
     * @param lon the longitude
     * @return URL the URL created.
     * @throws Exception if the URL is malformed.
     */
    private URL getPDSURL(double lat, double lon) throws Exception {

        StringBuffer urlBuff = new StringBuffer(psdUrl + psdCgi + "?");
        urlBuff.append("DATA_SET_NAME=" + dataSet);
        urlBuff.append("&VERSION=" + version);
        urlBuff.append("&PIXEL_TYPE=" + pixelType);
        urlBuff.append("&PROJECTION=" + projection);
        urlBuff.append("&STRETCH=" + stretch);
        urlBuff.append("&GRIDLINE_FREQUENCY=" + gridlineFrequency);
        urlBuff.append("&SCALE=" + URLEncoder.encode(scale, "UTF-8"));
        urlBuff.append("&RESOLUTION=" + resolution);
        urlBuff.append("&LATBOX=" + latbox);
        urlBuff.append("&LONBOX=" + lonbox);
        urlBuff.append("&BANDS_SELECTED=" + bandsSelected);
        urlBuff.append("&LAT=" + lat);
        urlBuff.append("&LON=" + lon);
        
        return new URL(urlBuff.toString());
    }
    
    /**
     * Reads HTML data from the connection and determines the image's URL.
     * @param connection the URL connection to use.
     * @throws IOException if there is an IO problem.
     */
    private void connectionEstablished(URLConnection connection) throws IOException {
        
        goodConnection = true;
        
        try {
            // Create a buffered reader from the input stream.
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = null;
            String line;
            String imageSrc;

            // <fragile>
            // Get the result from the 6th line.
            int count = 0;
            while ((line = in.readLine()) != null) {
                if (count == 6) result = line;
                count++;
            }

            // Find the image URL based on its location within the HTML.
            int startIndex = result.indexOf("<TH COLSPAN=2 ROWSPAN=2><IMG SRC = \"") + 36;
            int endIndex = result.indexOf("\"", startIndex);
            imageSrc = result.substring(startIndex, endIndex);
            // </fragile>

            // Download the image at the image URL.
            URL imageUrl = new URL(imageSrc);
            img = (Toolkit.getDefaultToolkit().getImage(imageUrl));
            
            // Wait until the image is fully downloaded.
            waitForMapLoaded();
        }
        catch (IOException e) {
            throw new IOException("Internet connection required");
        }
    }
    
    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        connectionTimer.stop();
        connectionTimeout = !goodConnection;
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
    }
    
    /**
     * Internal class for connecting to the USGS PDS image server.  
     * Uses its own thread.
     */
    private class PDSConnectionManager implements Runnable {
        
        private Thread connectionThread = null;
        private URLConnection connection = null;
        private USGSMarsMap map = null;
        
        /**
         * Constructor
         * @param connection the URL connection to use.
         * @param map the parent map class.
         */
        private PDSConnectionManager(URLConnection connection, USGSMarsMap map) {
           
            this.connection = connection;
            this.map = map;
            
            if ((connectionThread == null) || (!connectionThread.isAlive())) {
                connectionThread = new Thread(this, "HTTP connection");
                connectionThread.start();
            } 
        }
        
        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                connection.connect();
                map.connectionEstablished(connection);
            }
            catch (IOException e) {
                System.out.println("Unable to connect to: " + e.getMessage());
            }
        }
    }
}
