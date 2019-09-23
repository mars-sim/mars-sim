/**
 * Mars Simulation Project
 * Coordinates.java
 * @version 3.1.0 2017-09-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Spherical Coordinates. Represents a location on virtual Mars in spherical
 * coordinates. It provides some useful methods involving those coordinates, as
 * well as some static methods for general coordinate calculations.<br/>
 * {@link #theta} is longitute in (0-2PI) radians.<br/>
 * {@link #phi} is latitude in (-PI - PI) radians (although only 0-PI) makes any
 * sense for the renderer.<br/>
 * {@link #rho} rho diameter of planet (in km) or 2* MARS_RADIUS_KM =
 * 3393.0<br/>
 */
public class Coordinates implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/*
	 * default logger. private static Logger logger =
	 * Logger.getLogger(Coordinates.class.getName());
	 */

	// stored for efficiency but not serialized.
	private static final transient String shortNorth = Msg.getString("direction.northShort");
	private static final transient String shortEast = Msg.getString("direction.eastShort");
	private static final transient String shortSouth = Msg.getString("direction.southShort");
	private static final transient String shortWest = Msg.getString("direction.westShort");

	// Data members
	/**
	 * Phi value of coordinates PHI is latitude in (-PI - PI) radians (although only
	 * 0-PI) seem to be legal values.
	 */
	private double phi;
	/** Theta value of coordinates, THETA is longitude in (0-2PI) radians. */
	private double theta;
	/** Sine of phi (stored for efficiency). */
	private double sinPhi;
	/** Sine of theta (stored for efficiency). */
	private double sinTheta;
	/** Cosine of phi (stored for efficiency). */
	private double cosPhi;
	/** Cosine of theta (stored for efficiency). */
	private double cosTheta;
	/** Formatted string of the latitude. */
	private String latCache;
	/** Formatted string of the longitude. */
	private String lonCache;
	/** Track if the coordinate of an unit has been changed */
	private boolean changed;

	/**
	 * Constructs a Coordinates object, hence a constructor.
	 * 
	 * @param phi   the phi angle of the spherical coordinate
	 * @param theta the theta angle of the spherical coordinate
	 */
	public Coordinates(double phi, double theta) {

		// Set Coordinates
		this.setPhi(phi);
		this.setTheta(theta);

		// Set trigonometric functions
		setTrigFunctions();
	}

	/**
	 * Clone constructor
	 * 
	 * @param originalCoordinates the Coordinates object to be cloned
	 */
	public Coordinates(Coordinates originalCoordinates) {
		this(originalCoordinates.phi, originalCoordinates.theta);
	}

	/**
	 * Constructor with a latitude and longitude string. Expects direction
	 * abbreviations according to current locale, so for english NESW, for german
	 * NOSW, french NESO, etc.
	 * 
	 * @param latitude  String representing latitude value. ex. "25.344 N"
	 * @param longitude String representing longitude value. ex. "63.5532 W"
	 * @throws Exception if latitude or longitude strings are invalid.
	 */
	public Coordinates(String latitude, String longitude) {
		this(parseLatitude(latitude), parseLongitude(longitude));
	}

	/** Sets commonly-used trigonometric functions of coordinates */
	private void setTrigFunctions() {
		sinPhi = Math.sin(phi);
		sinTheta = Math.sin(theta);
		cosPhi = Math.cos(phi);
		cosTheta = Math.cos(theta);
	}

	/**
	 * Generate a string representation of this object. It will be the same format
	 * as the formattedString method.
	 * 
	 * @return String description of Coordinate.
	 * @see #getFormattedString()
	 */
	public String toString() {
		return getFormattedString();
	}

	/**
	 * phi accessor
	 * 
	 * @return the phi angle value of the coordinate
	 */
	public double getPhi() {
		return phi;
	}

	/**
	 * phi mutator
	 * 
	 * @param newPhi the new phi angle value for the coordinate
	 */
	public void setPhi(double newPhi) {
		if (newPhi <= 0D) {
			phi = 0D;
		} else if (newPhi > Math.PI) {
			phi = Math.PI;
		} else {
			phi = newPhi;
		}
		setTrigFunctions();
	}

	/**
	 * theta accessor
	 * 
	 * @return the theta angle value of the coordinate
	 */
	public double getTheta() {
		return theta;
	}

	/**
	 * theta mutator
	 * 
	 * @param newTheta the new theta angle value for the coordinate
	 */
	public void setTheta(double newTheta) {
		theta = newTheta;
		while (theta < 0D)
			theta += (Math.PI * 2D);
		while (theta > (Math.PI * 2D))
			theta -= (Math.PI * 2D);
		setTrigFunctions();
	}

	/**
	 * sine of phi.
	 * 
	 * @return the sine of the phi angle value of the coordinate
	 */
	public double getSinPhi() {
		// <tip> would it be help to use lazy evaluation of sinPhi here? </tip>
		return sinPhi;
	}

	/**
	 * sine of theta
	 * 
	 * @return the sine of the theta angle value of the coordinate
	 */
	public double getSinTheta() {
		return sinTheta;
	}

	/**
	 * cosine of phi
	 * 
	 * @return the cosine of the phi angle value of the coordinate
	 */
	public double getCosPhi() {
		return cosPhi;
	}

	/**
	 * cosine of theta
	 * 
	 * @return the cosine of the theta angle value of the coordinate
	 */
	public double getCosTheta() {
		return cosTheta;
	}

	/**
	 * Set coordinates
	 * 
	 * @param newCoordinates Coordinates object who's location should be matched by
	 *                       this Coordinates object
	 */
	public void setCoords(Coordinates newCoordinates) {
		changed = true;
		// Update coordinates
			if (newCoordinates != null) {
			setPhi(newCoordinates.phi);
			setTheta(newCoordinates.theta);
	
			// Update trigonometric functions
			setTrigFunctions();
		}
	}

	/**
	 * Returns true if coordinates have equal phi and theta values
	 * 
	 * @param otherCoords Coordinates object to be matched against
	 * @return true if Coordinates values match, false otherwise
	 */
	public boolean equals(Object otherCoords) {

		if ((otherCoords != null) && (otherCoords instanceof Coordinates)) {
			Coordinates other = (Coordinates) otherCoords;
			if ((phi == other.phi) && (theta == other.theta))
				return true;
		}

		return false;
	}

//	public boolean equals(Object obj) {
//		if (this == obj) return true;
//		if (obj == null) return false;
//		if (this.getClass() != obj.getClass()) return false;
//		Coordinates c = (Coordinates) obj;
//		return (this.phi == c.getPhi()) 
//				&& (this.theta == c.getTheta());
//	}
	
	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		return (int) ((phi * 1000D) + (theta * 1000D));
	}

	/**
	 * Gets the arc angle between this location and a given coordinates.
	 * 
	 * @param otherCoords the destination location.
	 * @return the arc angle (radians).
	 */
	public double getAngle(Coordinates otherCoords) {

		return getAngleVincenty(otherCoords);
	}

	/**
	 * Calculates the arc angle between this location and a given coordinates using
	 * the spherical law of cosines.
	 * http://en.wikipedia.org/wiki/Spherical_law_of_cosines
	 * 
	 * @param otherCoords the destination location.
	 * @return the arc angle (radians)
	 */
	public double getAngleSLC(Coordinates otherCoords) {

		double phi1 = -1D * (phi - (Math.PI / 2D));
		double phi2 = -1D * (otherCoords.phi - (Math.PI / 2D));
		double diffTheta = Math.abs(theta - otherCoords.theta);

		double temp1 = Math.cos(phi1) * Math.cos(phi2);
		double temp2 = Math.sin(phi1) * Math.sin(phi2);
		double temp3 = Math.cos(diffTheta);
		double temp4 = temp2 + (temp1 * temp3);

		// Make sure temp4 is in valid -1 to 1 range.
		if (temp4 > 1D)
			temp4 = 1D;
		else if (temp4 < -1D)
			temp4 = -1D;

		double result = Math.acos(temp4);
		return result;
	}

	/**
	 * Calculates the arc angle between this location and a given location using the
	 * haversine formula. http://en.wikipedia.org/wiki/Haversine_formula
	 * 
	 * @param otherCoords the destination location.
	 * @return the arc angle (radians).
	 */
	public double getAngleHaversine(Coordinates otherCoords) {

		double phi1 = -1D * (phi - (Math.PI / 2D));
		double phi2 = -1D * (otherCoords.phi - (Math.PI / 2D));
		double diffPhi = Math.abs(phi1 - phi2);
		double diffTheta = Math.abs(theta - otherCoords.theta);

		double temp1 = Math.pow(Math.sin(diffPhi / 2D), 2D);
		double temp2 = Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(diffTheta / 2D), 2D);
		double temp3 = Math.sqrt(temp1 + temp2);
		double result = 2D * Math.asin(temp3);
		return result;
	}

	/**
	 * Calculates the arc angle between this location and a given location using
	 * Vincenty's formula. http://en.wikipedia.org/wiki/Vincenty%27s_formulae
	 * 
	 * @param otherCoords the destination location.
	 * @return the arc angle (radians).
	 */
	public double getAngleVincenty(Coordinates otherCoords) {

		double phi1 = -1D * (phi - (Math.PI / 2D));
		double phi2 = -1D * (otherCoords.phi - (Math.PI / 2D));
		double diffTheta = Math.abs(theta - otherCoords.theta);

		double temp1 = Math.pow(Math.cos(phi2) * Math.sin(diffTheta), 2D);
		double temp2 = Math.cos(phi1) * Math.sin(phi2);
		double temp3 = Math.sin(phi1) * Math.cos(phi2) * Math.cos(diffTheta);
		double temp4 = Math.pow(temp2 - temp3, 2D);
		double temp5 = Math.sqrt(temp1 + temp4);

		double temp6 = Math.sin(phi1) * Math.sin(phi2);
		double temp7 = Math.cos(phi1) * Math.cos(phi2) * Math.cos(diffTheta);
		double temp8 = temp6 + temp7;

		double result = Math.atan2(temp5, temp8);
		return result;
	}

	/**
	 * Returns the distance in kilometers between this location and the given
	 * coordinates
	 * 
	 * @param otherCoords remote Coordinates object
	 * @return distance (in km) to the remote Coordinates object
	 */
	public double getDistance(Coordinates otherCoords) {

		double rho = Mars.MARS_RADIUS_KM;
		double angle = getAngle(otherCoords);
		double result = rho * angle;

		return result;
	}

	/**
	 * Gets a common formatted string to represent this location.
	 * 
	 * @return formatted longitude & latitude string for this Coordinates object
	 * @see #getFormattedLongitudeString()
	 * @see #getFormattedLatitudeString()
	 */
	public String getFormattedString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getFormattedLatitudeString());
		buffer.append(' ');
		buffer.append(getFormattedLongitudeString());
		return buffer.toString();
	}

	/**
	 * Gets a common formatted string to represent longitude for this location. ex.
	 * "35.6 E"
	 * 
	 * @return formatted longitude string for this Coordinates object
	 */
	public String getFormattedLongitudeString() {
		if (lonCache == null || changed) {
			changed = false;
			return getFormattedLongitudeString(theta);
		} else
			return lonCache;
	}

	/**
	 * Gets a double to represent longitude for this location. ex. "-35.6"
	 * 
	 * @return double longitude
	 */
	public double getLongitudeDouble() {
		double degrees;

		degrees = 0D;

		if ((theta < Math.PI) && (theta >= 0D)) {
			degrees = Math.toDegrees(theta);
		} else if (theta >= Math.PI) {
			degrees = Math.toDegrees((Math.PI * 2D) - theta);
			degrees = -degrees;
		}

		return degrees;
	}

	/**
	 * Gets a common formatted string to represent longitude for this location. ex.
	 * "35.6 E"
	 * 
	 * @param theta the radian theta value for the location.
	 * @return formatted longitude string for this Coordinates object
	 */
	public static String getFormattedLongitudeString(double theta) {
		double degrees;
		String direction = "";

		degrees = 0D;

		if ((theta < Math.PI) && (theta >= 0D)) {
			degrees = Math.toDegrees(theta);
			direction = Msg.getString("direction.eastShort"); //$NON-NLS-1$
		} else if (theta >= Math.PI) {
			degrees = Math.toDegrees((Math.PI * 2D) - theta);
			direction = Msg.getString("direction.westShort"); //$NON-NLS-1$ ;
		}

		DecimalFormat formatter = new DecimalFormat(Msg.getString("direction.decimalFormat")); //$NON-NLS-1$
		// Add a whitespace in between the degree and its directional sign
		return formatter.format(degrees) + Msg.getString("direction.degreeSign") + " " + direction; //$NON-NLS-1$
	}

	/**
	 * Gets a common formatted string to represent latitude for this location. ex.
	 * "35.6 S"
	 * 
	 * @return formatted latitude string for this Coordinates object
	 */
	public String getFormattedLatitudeString() {
		if (latCache == null || changed) {
			changed = false;
			return getFormattedLatitudeString(phi);
		} else
			return latCache;
	}

	/**
	 * Gets a double to represent latitude location. ex. "-35.6"
	 * 
	 * @return latitude double
	 */
	public double getLatitudeDouble() {
		double degrees;
		double piHalf = Math.PI / 2.0;

		degrees = 0D;

		if (phi <= piHalf) {
			degrees = ((piHalf - phi) / piHalf) * 90D;
		} else if (phi > piHalf) {
			degrees = ((phi - piHalf) / piHalf) * 90D;
			degrees = -degrees;
		}

		return degrees;

	}

	/**
	 * Gets a common formatted string to represent latitude for this location. ex.
	 * "35.6 S"
	 * 
	 * @param phi the radian phi value for the location.
	 * @return formatted latitude string for this Coordinates object
	 */
	public static String getFormattedLatitudeString(double phi) {

		double degrees;
		double piHalf = Math.PI / 2.0;
		String direction = "";

		degrees = 0D;

		if (phi <= piHalf) {
			degrees = ((piHalf - phi) / piHalf) * 90D;
			direction = Msg.getString("direction.northShort"); //$NON-NLS-1$
		} else if (phi > piHalf) {
			degrees = ((phi - piHalf) / piHalf) * 90D;
			direction = Msg.getString("direction.southShort"); //$NON-NLS-1$
		}

		DecimalFormat formatter = new DecimalFormat(Msg.getString("direction.decimalFormat")); //$NON-NLS-1$
		// Add a whitespace in between the degree and its directional sign
		return formatter.format(degrees) + Msg.getString("direction.degreeSign") + " " + direction; //$NON-NLS-1$
	}

	/**
	 * Converts phi to latitude
	 * 
	 * @param phi in radians
	 * @return latitude in degrees
	 */
	public double getPhi2Lat() {
		double phi = getPhi();
		double piHalf = Math.PI / 2.0;
		double lat_degree = 0;
		if (phi < piHalf) {
			lat_degree = ((piHalf - phi) / piHalf) * 90;
			// hemisphere = 1;
		} else if (phi > piHalf) {
			lat_degree = ((phi - piHalf) / piHalf) * 90;
			// hemisphere = 2;
		}
		return lat_degree;
	}

	/**
	 * Converts phi in radian to lat in radian
	 * 
	 * @param latCache in radians
	 * @return latitude in radian
	 */
	public double getPhi2LatRadian() {
		double phi = getPhi();
		double piHalf = Math.PI / 2.0;
		double lat_radian = 0;
		if (phi < piHalf) {
			lat_radian = piHalf - phi;
			// hemisphere = 1;
		} else if (phi > piHalf) {
			lat_radian = phi - piHalf;
			// hemisphere = 2;
		}
		return lat_radian;
	}

	/**
	 * Converts spherical coordinates to rectangular coordinates. Returns integer x
	 * and y display coordinates for spherical location.
	 * 
	 * @param newCoords    the offset location
	 * @param centerCoords location of the center of the map
	 * @param rho          radius of planet (in km)
	 * @param half_map     half the map's width (in pixels)
	 * @param low_edge     lower edge of map (in pixels)
	 * @return pixel offset value for map
	 */
	public static IntPoint findRectPosition(Coordinates newCoords, Coordinates centerCoords, double rho, int half_map,
			int low_edge) {

		return centerCoords.findRectPosition(newCoords.phi, newCoords.theta, rho, half_map, low_edge);
	}

	/**
	 * Converts spherical coordinates to rectangular coordinates. Returns integer x
	 * and y display coordinates for spherical location.
	 *
	 * @param newPhi   the new phi coordinate
	 * @param newTheta the new theta coordinate
	 * @param rho      diameter of planet (in km)
	 * @param half_map half the map's width (in pixels)
	 * @param low_edge lower edge of map (in pixels)
	 * @return pixel offset value for map
	 */
	public IntPoint findRectPosition(double newPhi, double newTheta, double rho, int half_map, int low_edge) {

		double temp_col = newTheta + ((Math.PI / -2D) - theta);
		double temp_buff_x = rho * Math.sin(newPhi);
		int buff_x = ((int) Math.round(temp_buff_x * Math.cos(temp_col)) + half_map) - low_edge;
		int buff_y = ((int) Math
				.round(((temp_buff_x * (0D - cosPhi)) * Math.sin(temp_col)) + (rho * Math.cos(newPhi) * (0D - sinPhi)))
				+ half_map) - low_edge;
		return new IntPoint(buff_x, buff_y);
	}

	/**
	 * Converts linear rectangular XY position change to spherical coordinates
	 * 
	 * @param x change in x value (in km)
	 * @param y change in y value (in km)
	 * @return new spherical location
	 */
	public Coordinates convertRectToSpherical(double x, double y) {
		return convertRectToSpherical(x, y, Mars.MARS_RADIUS_KM);
	}

	/**
	 * Converts linear rectangular XY position change to spherical coordinates with
	 * rho value for map.
	 * 
	 * @param x   change in x value (in km)
	 * @param y   change in y value (in km)
	 * @param rho rho value of map used (in km)
	 * @return new spherical location
	 */
	public Coordinates convertRectToSpherical(double x, double y, double rho) {
		Coordinates result = new Coordinates(0D, 0D);
		convertRectToSpherical(x, y, rho, result);
		return result;
	}

	/**
	 * Converts linear rectangular XY position change to spherical coordinates with
	 * rho value for map.
	 * 
	 * @param x              change in x value (in km)
	 * @param y              change in y value (in km)
	 * @param rho            rho value of map used (in km)
	 * @param newCoordinates Coordinates object to put the result in
	 */
	public void convertRectToSpherical(double x, double y, double rho, Coordinates newCoordinates) {

		double z = Math.sqrt((rho * rho) - (x * x) - (y * y));

		double x2 = x;
		double y2 = (y * cosPhi) + (z * sinPhi);
		double z2 = (z * cosPhi) - (y * sinPhi);

		double x3 = (x2 * cosTheta) + (y2 * sinTheta);
		double y3 = (y2 * cosTheta) - (x2 * sinTheta);
		double z3 = z2;

		double phi_new = Math.acos(z3 / rho);
		double theta_new = Math.asin(x3 / (rho * Math.sin(phi_new)));

		if (x3 >= 0) {
			if (y3 < 0)
				theta_new = Math.PI - theta_new;
		} else {
			if (y3 < 0)
				theta_new = Math.PI - theta_new;
			else
				theta_new = (Math.PI * 2D) + theta_new;
		}

		newCoordinates.setPhi(phi_new);
		newCoordinates.setTheta(theta_new);
	}

	/**
	 * Returns angle direction to another location on surface of sphere 0 degrees is
	 * north (clockwise)
	 * 
	 * @param otherCoords target location
	 * @return angle direction to target (in radians)
	 */
	public Direction getDirectionToPoint(Coordinates otherCoords) {

		double phi1 = -1D * (phi - (Math.PI / 2D));
		double phi2 = -1D * (otherCoords.phi - (Math.PI / 2D));
		double thetaDiff = otherCoords.theta - theta;
		double temp1 = Math.sin(thetaDiff) * Math.cos(phi2);
		double temp2 = Math.cos(phi1) * Math.sin(phi2);
		double temp3 = Math.sin(phi1) * Math.cos(phi2) * Math.cos(thetaDiff);
		double temp4 = temp2 - temp3;
		double result = Math.atan2(temp1, temp4);

		return new Direction(result);
	}

	/**
	 * Gets a new location with a given direction and distance from the current
	 * location.
	 * 
	 * @param direction direction to new location
	 * @param distance  distance to new location (in km)
	 * @return new location coordinates
	 */
	public Coordinates getNewLocation(Direction direction, double distance) {

		// Breaking line into iterative set of 10km plumb lines to estimate
		// cardinal direction on sphere.

		double iterationDistance = 10D;
		int iterations = (int) (distance / iterationDistance);
		double remainder = 0D;
		if (distance > 10D)
			remainder = distance - (iterations * iterationDistance);
		else
			remainder = distance;

		// Get successive iteration locations.
		Coordinates startCoords = this;
		for (int x = 0; x < iterations; x++) {
			double newY = -1D * direction.getCosDirection() * (iterationDistance);
			double newX = direction.getSinDirection() * (iterationDistance);
			startCoords = startCoords.convertRectToSpherical(newX, newY);
		}

		// Get final location based on remainder.
		double finalY = -1D * direction.getCosDirection() * (remainder);
		double finalX = direction.getSinDirection() * (remainder);
		Coordinates finalCoordinates = startCoords.convertRectToSpherical(finalX, finalY);

		return finalCoordinates;
	}

	/**
	 * Parse a latitude string into a phi value. ex. "25.344 N"
	 * 
	 * @param latitude as string
	 * @return phi value
	 * @throws ParseException if latitude string could not be parsed.
	 */
	public static double parseLatitude(String latitude) {
		double latValue = 0D;

		String cleanLatitude = latitude.toUpperCase().trim();

		if (cleanLatitude.isEmpty())
			throw new IllegalStateException("Latitude is blank !");

		try {
			String numberString = cleanLatitude.substring(0, cleanLatitude.length() - 1).trim();
			if (numberString.endsWith(Msg.getString("direction.degreeSign"))) //$NON-NLS-1$
				numberString = numberString.substring(0, numberString.length() - 1);
			// Replace comma with period from internationalization.
			numberString = numberString.replace(',', '.');
			latValue = Double.parseDouble(numberString);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Latitude number invalid : " + latitude);
		}

		if ((latValue > 90D) || (latValue < 0))
			throw new IllegalStateException("Latitude value out of range : " + latValue);

		// TODO parse latitude depending on locale and validate
		String direction = "" + cleanLatitude.charAt(latitude.length() - 1);
		if (direction.compareToIgnoreCase(shortNorth) == 0)
			latValue = 90D - latValue;
		else if (direction.compareToIgnoreCase(shortSouth) == 0)
			latValue += 90D;
		else
			throw new IllegalStateException("Invalid Latitude direction : " + direction);

		double phi = Math.PI * (latValue / 180D);
		return phi;
	}

	/**
	 * Parse a longitude string into a theta value. ex. "63.5532 W"
	 * 
	 * @param longitude as string
	 * @return theta value
	 * @throws ParseException if longitude string could not be parsed.
	 */
	public static double parseLongitude(String longitude) {
		double longValue = 0D;

		String cleanLongitude = longitude.toUpperCase().trim();

		if (cleanLongitude.isEmpty())
			throw new IllegalStateException("Longitude is blank !");

		try {
			String numberString = cleanLongitude.substring(0, cleanLongitude.length() - 1).trim();
			if (numberString.endsWith(Msg.getString("direction.degreeSign")))
				numberString = numberString.substring(0, numberString.length() - 1); // $NON-NLS-1$
			// Replace "comma" (in case of non-US locale) with "period"
			numberString = numberString.replace(',', '.');
			longValue = Double.parseDouble(numberString);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Longitude number invalid: " + longitude);
		}

		// TODO parse longitude depending on locale and validate
		String directionStr = "" + cleanLongitude.charAt(cleanLongitude.length() - 1);

		if (directionStr.compareToIgnoreCase(shortWest) == 0)
			longValue = 360D - longValue;
		else if (directionStr.compareToIgnoreCase(shortEast) != 0)
			throw new IllegalStateException("Invalid Longitude direction : " + directionStr);

		// if ((longValue > 180D) || (longValue < 0)) {
		// throw new IllegalStateException("The value of longitude " + longValue + "
		// needs to be between 0 and 180 degrees.");
		// }

		double theta = (2 * Math.PI) * (longValue / 360D);
		return theta;
	}

	/**
	 * Gets a random latitude.
	 * 
	 * @return latitude
	 */
	public static double getRandomLatitude() {
		// Random latitude should be less likely to be near the poles.
		double phi = RandomUtil.getRandomDouble(Math.PI / 2D) + RandomUtil.getRandomDouble(Math.PI / 2D);
		return phi;
	}

	/**
	 * Gets a random longitude.
	 * 
	 * @return longitude
	 */
	public static double getRandomLongitude() {
		double theta = Math.random() * (2D * Math.PI);
		return theta;
	}
}