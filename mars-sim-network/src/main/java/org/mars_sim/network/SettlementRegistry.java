/**
 * Mars Simulation Project
 * SettlementRegistry.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.network;

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
	private String sponsor;
	private double longitude;
	private double latitude;
	private int maxMSD;

	public SettlementRegistry(String p, int id, String n, String template, int pop, int bots,
			String sponsor, double lat, double lo) {
		playerName = p; clientID = id; name = n; this.template = template;
		population = pop; numOfRobots = bots;
		this.sponsor = sponsor;
		latitude = lat; longitude = lo;
  }

	public void updateRegistry(String pn, int i, String n, String t,
			int p, int b, String s, double la, double lo){
		playerName = pn;
		clientID = i;
		name = n;
		template = t;
		population = p;
		numOfRobots = b;
		sponsor = s;
		latitude = la;
		longitude = lo;
	}

	public void setPop(int p) {
		population = p;
	}

	public void setName(String n) {
		name = n;
	}

	public void setSponsor(String s) {
		sponsor = s;
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

  public String getSponsor() {
	  return sponsor;
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

  public static double convertLatLong2Double(String str) {
	double num = 0;
	
	
	//char decimalPoint = format.getDecimalFormatSymbols().getDecimalSeparator();
	
	str = str.replace(",", ".");
	
	num = Double.parseDouble(str.substring(0, str.length() - 1));
	num = Math.round(num*10.0)/10.0;
	String dir = str.substring(str.length() - 1, str.length());
	if (dir == "S")
		num = -num;
	else if (dir == "W")
		num = -num;
	return num;
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

