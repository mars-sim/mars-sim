/**
 * Mars Simulation Project
 * USGSMarsMap.java
 * @version 2.87 2009-11-20
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.swing.tool.map;

import org.mars_sim.msp.core.Coordinates;

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    private static String CLASS_NAME = "org.mars_sim.msp.ui.standard.tool.map.USGSMarsMap";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

	// The map type.
	public static final String TYPE = "USGS map";
	
	public static final double HALF_MAP_ANGLE = .06106D;
	public static final int MAP_HEIGHT = 11458; // Source map height in pixels (calculated).
	public static final int MAP_WIDTH = 22916; // Source map width in pixels (calculated).
	public static final double PIXEL_RHO = (double) MAP_HEIGHT / Math.PI;
    
    public static final double HALF_MAP_ANGLE_DEG = 150D / 64D;
	
    private static final String psdUrl = "http://www.mapaplanet.org";
    private static final String psdCgi = "/explorer-bin/explorer.cgi";
    
    // Old USGS PDS website URL's.
    
    //private static final String psdUrl = "http://pdsmaps.wr.usgs.gov";
    //private static final String psdCgi = "/explorer-bin/mapmars3.cgi";
    
    //private static final String psdUrl = "http://www-pdsimage.wr.usgs.gov";
    //private static final String psdCgi = "/cgi-bin/panpic.cgi";

    //private static final String psdUrl = "http://192.168.1.50";
    //private static final String psdUrl = "http://63.229.31.9";
    //private static final String psdCgi = "/cgi-bin/mapmaker/mapmaker.py";

    private static final String map = "Mars";
    private static final String layers = "mars_viking_merged";
    private static final String info = "NO";
    private static final String advoption = "YES";
    private static final String lines = "2668";
    private static final String samples = "720";
    private static final String sizeSelector = "resolution";
    private static final String Resolution = "64";
    private static final String R = "1";
    private static final String G = "2";
    private static final String B = "3";
    private static final String projection = "MERC";
    private static final String grid = "none";
    private static final String stretch = "none";
    private static final String resamp_method = "nearest_neighbor";
    private static final String center = "0";
    private static final String defaultcenter = "on";
    private static final String center_lat = "0";

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
        
        double lat = 90D - Math.toDegrees(newCenter.getPhi());
        double lon = 360D - Math.toDegrees(newCenter.getTheta());
        // Convert from lon (0 -> 360) to (-180 -> 180).
        if (lon > 180D) lon = lon - 360D;
        
        startPdsImageRetrieval(lon, lat);
                               
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
            // System.out.println("url: " + url);
            
            HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
            
            // Connect with PDS CGI.
            new PDSConnectionManager(urlCon, this);
        } 
        catch (MalformedURLException e) {
            logger.log(Level.SEVERE,"URL not valid: " + url.toString());
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
        
        // Find map rectangle boundries.
        DecimalFormat formatter = new DecimalFormat("0.000");
        double westSide = lon + HALF_MAP_ANGLE_DEG;
        if (westSide > 180D) westSide = (westSide - 180D) - 180D;
        double eastSide = lon - HALF_MAP_ANGLE_DEG;
        if (eastSide < -180D) eastSide = (eastSide + 180D) + 180D;
        double northSide = lat + HALF_MAP_ANGLE_DEG;
        if (northSide > 90D) northSide = 90D + (90D - northSide);
        double southSide = lat - HALF_MAP_ANGLE_DEG;
        if (southSide < -90D) southSide = -90D + (-90D - southSide);
        
        StringBuffer urlBuff = new StringBuffer(psdUrl + psdCgi + "?");
        urlBuff.append("map=" + map);
        urlBuff.append("&layers=" + layers);
        urlBuff.append("&info=" + info);
        urlBuff.append("&advoption=" + advoption);
        urlBuff.append("&lines=" + lines);
        urlBuff.append("&samples=" + samples);
        urlBuff.append("&sizeSelector=" + sizeSelector);
        urlBuff.append("&Resolution=" + Resolution);
        urlBuff.append("&R=" + R);
        urlBuff.append("&G=" + G);
        urlBuff.append("&B=" + B);
        urlBuff.append("&projection=" + projection);
        urlBuff.append("&grid=" + grid);
        urlBuff.append("&stretch=" + stretch);
        urlBuff.append("&resamp_method=" + resamp_method);
        urlBuff.append("&north=" + formatter.format(northSide));
        urlBuff.append("&west=" + formatter.format(westSide));
        urlBuff.append("&east=" + formatter.format(eastSide));
        urlBuff.append("&south=" + formatter.format(southSide));
        urlBuff.append("&center=" + center);
        urlBuff.append("&defaultcenter=" + defaultcenter);
        urlBuff.append("&center_lat=" + center_lat);
        
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
            String imageSrc = "";

            // Parse through the returned HTML and find the image URL.
            // This is a bit of a hack to get this working.
            String line = null;
            while ((line = in.readLine()) != null) {
                if (line.indexOf("View and Save") > -1) {
                    String line2 = in.readLine();
                    if (line2 != null) {
                        int startIndex = line2.indexOf("/explorer");
                        int endIndex = line2.indexOf("jpg") + 3;
                        String relativeUrl = line2.substring(startIndex, endIndex);
                        //System.out.println("Relative URL: " + relativeUrl);
                        imageSrc = psdUrl + relativeUrl;
                    }
                }
            }
            
            //System.out.println("Image source: " + imageSrc);
            
            // Download the image at the image URL.
            URL imageUrl = new URL(imageSrc);
            img = (Toolkit.getDefaultToolkit().getImage(imageUrl));
            
            // Wait until the image is fully downloaded.
            waitForMapLoaded();
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
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
            logger.log(Level.SEVERE,"waitForMapLoaded()",e);
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
                logger.log(Level.SEVERE,"Unable to connect to: " + e.getMessage());
            }
        }
    }
}
