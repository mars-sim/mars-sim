/**
 * Mars Simulation Project
 * CentralRegistry.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */
package org.mars_sim.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/*
 * The CentralRegistry class maintains the record of vital settlements info in multiplayer mode simulation
 */
public class CentralRegistry implements Serializable{

	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(CentralRegistry.class.getName());

	public static final int MAX = 30;

	private int id = 1;

	private static String SETTLEMENT_REGISTRY = "registry.txt";

	//private int numSettlements;       // number of settlements in the array

	//private SettlementRegistry registry[];
	private List<SettlementRegistry> settlementList;

	private Map<Integer, String> idMap = new ConcurrentHashMap<>(); // store clientID & player name
	private Map<Integer, String> addressMap = new ConcurrentHashMap<>(); // store clientID & address
	private Map<Integer, String> timeTagMap = new ConcurrentHashMap<>(); // store String & Date


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
		    for(int i = 0; i < size ; i++) {
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
   * Returns a formatted msg with a newly assigned clientID. e.g. "NEW_ID is 4 for mk0"
   * @param id
   * @param playerName
   * @return formatted String
   */
    public String approveID(int id, String playerName) {
    	//String details = "NEW_ID " + userName + " " + id;
    	String details = "NEW_ID " + id;
    	logger.info("Sent : " + details);
    	return details;
    }

    /*
     * Returns a formatted msg with the invalid player name
     * @param playerNameName
     * @return formatted String
     */
      public String disapprovePlayerName(String playerName) {
      	String details = "INVALID_PLAYER_NAME : " + playerName;
      	logger.info("Sent : " + details);
      	return details;
      }

	/*
	 * Assigns a new clientID and store the clientID and address onto the idMap
	 */
	public int assignNewID(String userName, String clientAddress) {
		if (idMap.size() == 0) {
	        idMap.put(1, userName);
			addressMap.put(1, clientAddress);
			return 1;
		}
		else {

			List<Integer> unsortedID = new ArrayList<Integer>();
			idMap.forEach((key, value) ->  unsortedID.add(key));
			// need to sort the list so that the comparison begins at key = 1
			List<Integer> sortedID = unsortedID.stream().sorted().collect(Collectors.toList());
			id = 1;
			// set id to the lowest possible player id
			// Note: if a client lost connection, the player id will be returned to the server and reassigned here.
			sortedID.forEach((key  ->  {
				if (key == id)
					id++;
			}));

	        //System.out.println("id is " + id);
	        idMap.put(id, userName);
	        addressMap.put(id, clientAddress);
			return id;
		}
	}

	public boolean verifyPlayerName(String userName, String clientAddress) {
		if (idMap.containsValue(userName))
			return true; // it already exists
		else
			return false;
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
    	 String sponsor = st.nextToken().trim();
    	 double lat = Double.parseDouble( st.nextToken().trim() );
    	 double lo = Double.parseDouble( st.nextToken().trim() );
    	 // TODO: check if name has been used. If it does, change it to name_x, where x is the next increment digit
    	 // TODO: inform the user of the change and make the change automatically
    	 settlementList.add(new SettlementRegistry(playerName, clientID, name, template, pop, bots, sponsor, lat, lo));
    	 //System.out.println("settlementList.size() is now " + settlementList.size());
     }
     catch(Exception e) {
    	 logger.info("Problem parsing new entry:\n" + e);
			e.printStackTrace();
	}
  }

  /*
   * Updates the settlement info
   */
   public void updateEntry(String line) {
      StringTokenizer st = new StringTokenizer(line, "&");
      try {
     	 String playerName = st.nextToken().trim();
     	 int clientID = Integer.parseInt( st.nextToken().trim() );
     	 String name = st.nextToken().trim();
     	 String template = st.nextToken().trim();
     	 int pop = Integer.parseInt( st.nextToken().trim() );
     	 int bots = Integer.parseInt( st.nextToken().trim() );
     	 String sponsor = st.nextToken().trim();
     	 double lat = Double.parseDouble( st.nextToken().trim() );
     	 double lo = Double.parseDouble( st.nextToken().trim() );

     	 settlementList.forEach( s -> {
     			 String pn = s.getPlayerName();
     			 String sn = s.getName();
     			 if (pn.equals(playerName) && sn.equals(name))
     					 s.updateRegistry(playerName, clientID, name, template,
     							 pop, bots, sponsor, lat, lo);
     	 });
      }

      catch(Exception e) {
     	 logger.info("Problem updating entry:\n" + e);
 			e.printStackTrace();
 	}
   }

   /*
    * Removes a settlement entry
    */
    public void removeEntry(String line) {
       StringTokenizer st = new StringTokenizer(line, "&");
       try {
      	 String playerName = st.nextToken().trim();
      	 int clientID = Integer.parseInt( st.nextToken().trim() );
      	 String name = st.nextToken().trim();

      	settlementList.removeIf(s ->
      		 ( s.getPlayerName().equals(playerName)
      		&& s.getClientID() == clientID
      		&& s.getName().equals(name) )
        );

       }
       catch(Exception e) {
    	   logger.info("Problem removing a settlement entry:\n" + e);
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
      int size = settlementList.size();
      for (int i=0; i < size ; i++) {
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

	public Map<Integer, String> getAddressMap() {
		return addressMap;
	}

	public Map<Integer, String> getIdMap() {
		return idMap;
	}

	public void destroy() {
		//registry = null;
		settlementList.clear();
		settlementList = null;
	}

}