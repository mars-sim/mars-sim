package org.mars_sim.networking;

/*
 * The information about a settlement kept by the host server.
 */
public class SettlementRegistry {
	private int clientID;
	private int numOfRobots;
	private int population;
	private String name;
	private String template;
	private double longitude;
	private double latitude;

  public SettlementRegistry(String n, String template, int pop, int bots, double lo, double lat) {
	name = n; this.template = template; population = pop; numOfRobots = bots; longitude = lo; latitude = lat;
  }

  public void setID(int id) {
	  this.clientID = id;
  }

  public int getID() {
	return clientID ;
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

  public String getLatitudeStr() {
	String dir = "N";
	  if (latitude < 0)
		  dir = "S";
	  return Math.abs(latitude) + dir;
  }

  public String getLongitudeStr() {
	String dir = "E";
	  if (latitude < 0)
		  dir = "W";
	  return Math.abs(longitude) + dir;
  }

}

