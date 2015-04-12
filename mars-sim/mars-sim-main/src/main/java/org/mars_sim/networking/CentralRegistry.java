/**
 * Mars Simulation Project
 * CentralRegistry.java
 * @version 3.08 2015-04-10
 * @author Manny Kung
 */
package org.mars_sim.networking;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/*
 * The CentralRegistry class maintains the record of vital settlements info in multiplayer mode simulation
 */
public class CentralRegistry {

	/** default logger. */
	private static Logger logger = Logger.getLogger(CentralRegistry.class.getName());

	private static final int MAX = 30;

	private static String SETTLEMENT_REGISTRY = "registry.txt";

	private int numSettlements;       // number of settlements in the array

	private SettlementRegistry registry[];

  /*
   * Returns a string with this format: "RECORDS name1 & lat1 & long1 & ... nameN & latN & longN"
   */
  public CentralRegistry() {
	registry = new SettlementRegistry[MAX];
    numSettlements = 0;
    loadRecords();
  }

/*
 * Returns a formatted with settlements info. e.g. "RECORDS name1 & lat1 & long1 & ... nameN & latN & longN"
 * @return formatted String
 */
  public String toString() {
    String details = "RECORDS ";
    for(int i = 0; i < numSettlements; i++)
      details += registry[i].getName() + " & " + registry[i].getLatitude() + " & " + registry[i].getLongitude() + " & ";
    logger.info("Sent : "+ details);
    return details;
  }

  /*
   * Returns a formatted string with clientID. e.g. "NEW_ID 4"
   * @param id
   * @return formatted String with id#
   */
    public String returnID(int id) {
      String details = "NEW_ID ";
        details += id;
      logger.info("Sent : "+ details);
      return details;
    }
 /*
  * Parses and add only one entry (e.g. "name & lat & long") into the java objects
  */
  public void addEntry(String line) {
     StringTokenizer st = new StringTokenizer(line, "&");
     try {
       String name = st.nextToken().trim();
       double lat = Double.parseDouble( st.nextToken().trim() );
       double lo = Double.parseDouble( st.nextToken().trim() );
       addEntry(name, lat, lo);
     }
     catch(Exception e) {
    	 logger.info("Problem parsing new entry:\n" + e);
	}
  }


  /* Adds an entry to the array
   *
   */
  public void addEntry(String name, double lat, double lo) {
    int i = 0;

    while ((i < numSettlements))
      i++;

    // add in the new entry
    registry[i] = new SettlementRegistry(name, lat, lo);

    numSettlements++;
  }


  /*
   * Loads settlement info from a formatted file with all data on one single line e.g. "name & lat & long...."
   */
  private void loadRecords() {
    String line;
    try {
      BufferedReader in =
	  //new BufferedReader(new FileReader(SETTLEMENT_REGISTRY));
      //new BufferedReader(new FileReader(CentralRegistry.class.getClassLoader().getResource(SETTLEMENT_REGISTRY).getPath().replaceAll("%20", " ")));
      new BufferedReader(new FileReader(this.getClass().getResource(SETTLEMENT_REGISTRY).getPath().replaceAll("%20", " ")));

      while ((line = in.readLine()) != null)
        addEntry(line);
      in.close();
    }
    catch(IOException e)
    { e.printStackTrace();}
  }

  /* Saves the settlement registry into a file
   *
   */
  public void saveRecords() {
    String line;
    try {
      PrintWriter out = new PrintWriter(
			new BufferedWriter( new FileWriter(this.getClass().getResource(SETTLEMENT_REGISTRY).getPath().replaceAll("%20", " ")) ), true);
      for (int i=0; i < numSettlements; i++) {
         line = registry[i].getName() + " & " + registry[i].getLatitude() + " & " + registry[i].getLongitude() + " &";
         out.println(line);
      }
      //out.println();
      out.close();
      logger.info("just done with saveRecords()");
    }
    catch(IOException e)
    { e.printStackTrace();}
  }

  public void destroy() {
	  registry = null;
  }

}

	/*
	 * Maintains the settlement name and coordinate
	 */
	class SettlementRegistry {
	private int clientID;
	private String name;
	private String template;
	private double longitude;
	private double latitude;

	  public SettlementRegistry(String n, double lo, double lat) {
		  name = n;  longitude = lo; latitude = lat;
	  }

	  public String getName()
	  { return name;  }

	  public String getTemplate()
	  { return template;  }

	  public double getLatitude()
	  { return latitude; }

	  public double getLongitude()
	  { return longitude; }


	}

