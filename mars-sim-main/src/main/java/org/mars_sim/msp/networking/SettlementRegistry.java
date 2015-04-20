package org.mars_sim.msp.networking;

import java.io.Serializable;

/*
 * The information about a settlement kept by the host server.
 */
public class SettlementRegistry implements Serializable{

	private static final long serialVersionUID = 1L;

	private String playerName;
	private int clientID;
	private int numOfRobots;
	private int population;
	private String name;
	private String template;
	private double longitude;
	private double latitude;

	public SettlementRegistry(String p, int id, String n, String template, int pop, int bots, double lat, double lo) {
		playerName = p; clientID = id; name = n; this.template = template; population = pop; numOfRobots = bots; latitude = lat; longitude = lo;
  }

  //public void setID(int id) {
	//  this.clientID = id;
  //}

	public void updateRegistry(String pn, int i, String n, String t, int p, int b, double la, double lo){
		playerName = pn;
		clientID = i;
		name = n;
		template = t;
		population = p;
		numOfRobots = b;
		latitude = la;
		longitude = lo;
	}

	public String getPlayerName() {
		return playerName;
	}

  public int getClientID() {
	return clientID;
  }

  public String getName() {
	  return name;
  }

  public String getTemplate() {
	  return template;
  }

  public int getNumOfRobots() {
	return numOfRobots ;
  }

  public int getPopulation() {
	  return population ;
  }

  public double getLatitude() {
	  return latitude;
  }

  public double getLongitude() {
	  return longitude;
  }

  public static double convertLatLong2Double(String latStr) {
	double doubleLat = 0;
	doubleLat = Double.parseDouble(latStr.substring(0, latStr.length() - 1));
	doubleLat = Math.round(doubleLat*10.0)/10.0;
	String dir = latStr.substring(latStr.length() - 1, latStr.length());
	if (dir == "S" || dir == "W")
		doubleLat = -doubleLat;
	return doubleLat;
  }

  public String getLatitudeStr() {
	String dir = "N";
	  if (latitude < 0)
		  dir = "S";
	  return Math.abs(latitude) + " " + dir;
  }

  public String getLongitudeStr() {
	String dir = "E";
	  if (longitude < 0)
		  dir = "W";
	  return Math.abs(longitude) + " " + dir;
  }

}

