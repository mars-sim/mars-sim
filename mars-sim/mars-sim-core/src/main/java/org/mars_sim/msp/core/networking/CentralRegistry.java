/**
 * Mars Simulation Project
 * CentralRegistry.java
 * @version 3.08 2015-04-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.networking;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/*
 * The CentralRegistry class maintains the record of vital settlements info in multiplayer mode simulation
 */
public class CentralRegistry implements Serializable{

	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(CentralRegistry.class.getName());

	public static final int MAX = 30;

	private static String SETTLEMENT_REGISTRY = "registry.txt";

	//private int numSettlements;       // number of settlements in the array

	//private SettlementRegistry registry[];
	private List<SettlementRegistry> settlementList;

  /*
   * Returns a string with this format: "RECORDS name1 & lat1 & long1 & ... nameN & latN & longN"
   */
  public CentralRegistry() {
	settlementList = new CopyOnWriteArrayList<>();
    //loadRecords();
  }

	/*
	 * Returns a formatted with settlements info. e.g. "RECORDS name1 & lat1 & long1 & ... nameN & latN & longN"
	 * @return formatted String
	 */
	public String toString() {
	    String details = null;
	    int size = settlementList.size();

	    if (size == 0) {
	    	details = "RECORDS 0";
	    }

	    else {
		    details = "RECORDS ";
		    for(int i = 0; i < settlementList.size(); i++) {
		    details += settlementList.get(i).getPlayerName() + " & " + settlementList.get(i).getClientID()
		    		+ " & " + settlementList.get(i).getName() + " & " + settlementList.get(i).getTemplate()
		    		+ " & " + settlementList.get(i).getPopulation() + " & " + settlementList.get(i).getNumOfRobots()
		    		+ " & " + settlementList.get(i).getLatitude() + " & " + settlementList.get(i).getLongitude() + " & ";
		    }
	    }

	    logger.info("Sent : "+ details);
	    return details;
  }

  /*
   * Returns a formatted string with clientID. e.g. "NEW_ID 4"
   * @param id
   * @return formatted String with id#
   */
    public String returnID(int id, String userName) {
    	//String details = "NEW_ID " + userName + " " + id;
    	String details = "NEW_ID " + id;
    	logger.info("Sent : " + details);
    	return details;
    }
 /*
  * Parses and add only one entry (e.g. "name & lat & long") into the java objects
  */
  public void addEntry(String line) {
     StringTokenizer st = new StringTokenizer(line, "&");
     try {
    	 String playerName = st.nextToken().trim();
    	 int clientID = Integer.parseInt( st.nextToken().trim() );
    	 String name = st.nextToken().trim();
    	 String template = st.nextToken().trim();
    	 int pop = Integer.parseInt( st.nextToken().trim() );
    	 int bots = Integer.parseInt( st.nextToken().trim() );
    	 double lat = Double.parseDouble( st.nextToken().trim() );
    	 double lo = Double.parseDouble( st.nextToken().trim() );
    	 settlementList.add(new SettlementRegistry(playerName, clientID, name, template, pop, bots, lat, lo));
     }
     catch(Exception e) {
    	 logger.info("Problem parsing new entry:\n" + e);
			e.printStackTrace();
	}
  }


  /* Adds an entry to the array
   *

  public void addEntry(String playerName, int clientID, String name, String template, int pop, int bots, double lat, double lo) {
    //int i = 0;
    //while ((i < numSettlements))
    //  i++;
    // add in the new entry
    settlementList.add(new SettlementRegistry(playerName, clientID, name, template, pop, bots, lat, lo));
    //numSettlements++;
  }
*/

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
      if (in != null) {
	      while ((line = in.readLine()) != null)
	        addEntry(line);
	      in.close();
	    }
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
      for (int i=0; i < settlementList.size(); i++) {
         line = settlementList.get(i).getClientID() + " & " + settlementList.get(i).getName() + " & " + settlementList.get(i).getTemplate() + " & " + settlementList.get(i).getPopulation() + " & "
        		 + settlementList.get(i).getNumOfRobots() + " & " + settlementList.get(i).getLatitude() + " & " + settlementList.get(i).getLongitude() + " & ";
         out.println(line);
      }
      //out.println();
      out.close();
      logger.info("just done with saveRecords()");
    }
    catch(IOException e)
    { e.printStackTrace();}
  }

	public List<SettlementRegistry> getSettlementRegistryList() {
		return settlementList;
	}

	public void destroy() {
		//registry = null;
		settlementList.clear();
		settlementList = null;
	}

}