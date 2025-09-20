/*
 * Mars Simulation Project
 * Coordinates.java
 * @date 2022-08-02
 * @author Scott Davis
 */
package com.mars_sim.core.map.location;

import java.io.Serializable;

import com.mars_sim.core.map.IntegerMapData;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Spherical Coordinates. Represents a location on virtual Mars in spherical
 * coordinates. It provides some useful methods involving those coordinates, as
 * well as some static methods for general coordinate calculations.<br/>
 * {@link #theta} is longitude in (0 - 2 PI) radians or (0 - 360) degrees. <br/>
 * {@link #phi} is latitude in (0 - PI) radians or (0 - 180) degrees. <br/>
 */
public final class Coordinates implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Mars average radius in km. */
	public static final double MARS_RADIUS_KM = 3393D;
	
	public static final double MARS_CIRCUMFERENCE = MARS_RADIUS_KM * 2 * Math.PI;
	
	public static final double KM_PER_DEGREE_AT_EQUATOR = MARS_CIRCUMFERENCE / 360;
	public static final double KM_PER_RADIAN_AT_EQUATOR = MARS_CIRCUMFERENCE / (2 * Math.PI);
	
	private static final double RADIAN_TO_DEG  = 180 / Math.PI;
	private static final double PI_HALF = Math.PI / 2;
	private static final double TWO_PI = Math.PI * 2;


	// Data members
	/** Phi value of coordinates PHI is latitude in 0-PI radians.*/
	private final double phi;
	/** Theta value of coordinates, THETA is longitude in 0-2PI radians. */
	private final double theta;

	/** Formatted string of both latitude and longitude. */
	private transient String formattedString;

	/**
	 * Constructs a Coordinates object, hence a constructor.
	 *
	 * @param phi    (latitude) the phi angle of the spherical coordinate
	 * @param theta  (longitude) the theta angle of the spherical coordinate
	 */
	public Coordinates(double phi, double theta) {
		
		double p = phi;
		double t = theta;
		
		// Make sure phi is between 0 and PI in radians
		// Not between -PI/2 and PI/2 in radians

		while (p > Math.PI)
			p -= Math.PI;
		while (p < 0)
			p += Math.PI;

		this.phi = p;
		
		// Make sure theta is between 0 and 2PI in radians
		// Not between -PI and PI in radians

		while (t < 0D)
			t += TWO_PI;
		while (t > TWO_PI)
			t -= TWO_PI;
		
		this.theta = t;		
	}

	/**
	 * Constructor with a latitude and longitude string. Expects direction
	 * abbreviations according to current locale, so for English NESW, for German
	 * NOSW, French NESO, etc.
	 *
	 * Note: Currently, lat and lon are up to 4 decimal places.
	 * - 1 degree at equator is 5.9167 km
	 * - 0.0001 degree at equator is 5.92 m 
	 *
	 * @param latitude  String representing latitude value. ex. "25.3443 N"
	 * @param longitude String representing longitude value. ex. "63.5532 W"
	 */
	public Coordinates(String latitude, String longitude) {
		this(CoordinatesFormat.parseLatitude2PhiUncheck(latitude), CoordinatesFormat.parseLongitude2ThetaUncheck(longitude));
	}

	
	/**
	 * Gets a random location
	 */
	public static Coordinates getRandomLocation() {
		var lat = .7 * RandomUtil.getRandomDouble(Math.PI);
		var lon = RandomUtil.getRandomDouble(2D * Math.PI);

		return new Coordinates(lat, lon);
	}

	/**
	 * Generates a string representation of this object. It will be the same format
	 * as the formattedString method.
	 *
	 * @return String description of Coordinate.
	 * @see #getFormattedString()
	 */
	public String toString() {
		return getFormattedString();
	}

	/**
	 * Returns the phi accessor (related to latitude).
	 *
	 * @return the phi angle value of the coordinate
	 */
	public double getPhi() {
		return phi;
	}

	/**
	 * Returns the theta accessor (related to longitude).
	 *
	 * @return the theta angle value of the coordinate
	 */
	public double getTheta() {
		return theta;
	}

	/**
	 * Gets the arc angle between this location and a given coordinates.
	 *
	 * @param otherCoords the destination location.
	 * @return the arc angle (radians).
	 */
	public double getAngle(Coordinates otherCoords) {
//		Note: May return getAngleSLC(otherCoords)
//		Note: May return getAngleVincenty(otherCoords)
		return getAngleHaversine(otherCoords);
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

		double phi1 = -1D * (phi - PI_HALF);
		double phi2 = -1D * (otherCoords.phi - PI_HALF);
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

		return Math.acos(temp4);
	}

	/**
	 * Calculates the arc angle between this location and a given location using the
	 * haversine formula. http://en.wikipedia.org/wiki/Haversine_formula
	 *
	 * @param otherCoords the destination location.
	 * @return the arc angle (radians).
	 */
	private double getAngleHaversine(Coordinates otherCoords) {	
		// Calculate angleHaversine 
		double phi1 = -1D * (phi - PI_HALF);
		double phi2 = -1D * (otherCoords.phi - PI_HALF);
		double diffPhi = Math.abs(phi1 - phi2);
		double diffTheta = Math.abs(theta - otherCoords.theta);

		double temp1 = Math.pow(Math.sin(diffPhi / 2D), 2D);
		double temp2 = Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(diffTheta / 2D), 2D);
		double temp3 = Math.sqrt(temp1 + temp2);

		return 2D * Math.asin(temp3);
	}

	/**
	 * Calculates the arc angle between this location and a given location using
	 * Vincenty's formula. http://en.wikipedia.org/wiki/Vincenty%27s_formulae
	 *
	 * @param otherCoords the destination location.
	 * @return the arc angle (radians).
	 */
	public double getAngleVincenty(Coordinates otherCoords) {

		double phi1 = -1D * (phi - PI_HALF);
		double phi2 = -1D * (otherCoords.phi - PI_HALF);
		double diffTheta = Math.abs(theta - otherCoords.theta);

		double temp1 = Math.pow(Math.cos(phi2) * Math.sin(diffTheta), 2D);
		double temp2 = Math.cos(phi1) * Math.sin(phi2);
		double temp3 = Math.sin(phi1) * Math.cos(phi2) * Math.cos(diffTheta);
		double temp4 = Math.pow(temp2 - temp3, 2D);
		double temp5 = Math.sqrt(temp1 + temp4);

		double temp6 = Math.sin(phi1) * Math.sin(phi2);
		double temp7 = Math.cos(phi1) * Math.cos(phi2) * Math.cos(diffTheta);
		double temp8 = temp6 + temp7;

		return Math.atan2(temp5, temp8);
	}

	/**
	 * Returns the distance in kilometers between this location and a given
	 * coordinate.
	 *
	 * @param otherCoords remote Coordinates object
	 * @return distance (in km) to the remote Coordinates object
	 */
	public double getDistance(Coordinates otherCoords) {
		if (otherCoords == null) {
			return 0;
		}

		if (otherCoords.equals(this)) {
			return 0;
		}

		double rho = MARS_RADIUS_KM;
		double angle = getAngle(otherCoords);
		return rho * angle;
	}

	/**
	 * Gets a common formatted string to represent this location.
	 * e.g. "3.1244 E 34.4352 S"
	 *
	 * @return formatted longitude & latitude string for this Coordinates object
	 * @see #getFormattedLongitudeString()
	 * @see #getFormattedLatitudeString()
	 */
	public String getFormattedString() {
		if (formattedString == null) {
			formattedString = CoordinatesFormat.getFormattedString(this);
		}
		
		return formattedString;
	}

	/**
	 * Gets a common formatted string to represent longitude for this location.
	 * e.g. "35.6054 E".
	 *
	 * @return formatted longitude string for this Coordinates object
	 */
	public final String getFormattedLongitudeString() {
		return CoordinatesFormat.getFormattedLongitudeString(this);
	}

	/**
	 * Gets a double to represent longitude for this location in deg. 
	 * e.g. "-35.60".
	 *
	 * @return double longitude in deg.
	 */
	public double getLongitudeDouble() {
		double degrees = 0D;

		if ((theta <= Math.PI) && (theta >= 0D)) {
			degrees = Math.toDegrees(theta);
		} else if (theta > Math.PI) {
			degrees = Math.toDegrees(TWO_PI - theta);
			degrees = -degrees;
		}

		return degrees;
	}



	/**
	 * Gets a common formatted string to represent latitude for this location. 
	 * e.g. "35.6780 S"
	 *
	 * @return formatted latitude string for this Coordinates object
	 */
	public final String getFormattedLatitudeString() {
		return CoordinatesFormat.getFormattedLatitudeString(this);
	}

	/**
	 * Gets a double to represent latitude location in deg. 
	 * e.g. "-35.6".
	 *
	 * @return latitude double in deg.
	 */
	public double getLatitudeDouble() {
		double degrees = 0;

		if (phi <= PI_HALF) {
			degrees = ((PI_HALF - phi) / PI_HALF) * 90D;
		} else {
			degrees = ((phi - PI_HALF) / PI_HALF) * 90D;
			degrees = -degrees;
		}

		return degrees;
	}


	/**
	 * Converts phi to latitude.
	 *
	 * @return latitude in degrees
	 */
	public double getPhi2Lat() {
		return getPhi2LatRadian() * RADIAN_TO_DEG;
	}

	/**
	 * Converts phi in radians to lat in radians.
	 *
	 * @return latitude in radians
	 */
	public double getPhi2LatRadian() {
		double p = getPhi();
		double latRadian = 0;
		if (p < PI_HALF) {
			latRadian = PI_HALF - p;
		} else {
			latRadian = p - PI_HALF;
		}
		return latRadian;
	}

	/**
	 * Converts spherical coordinates to rectangular coordinates. Returns integer x
	 * and y display coordinates for spherical location.
	 *
	 * @param newCoords    the offset location
	 * @param rho      diameter of planet (in km)
	 * @param xHalfMap half the map's width (in pixels)
	 * @param xLowEdge lower edge of map (in pixels)
	 * @return pixel offset value for map
	 */
	public IntPoint findRectPosition(Coordinates newCoords, double rho, int xHalfMap, int xLowEdge,
							int yHalfMap, int yLowEdge) {

		double sinPhi = Math.sin(this.phi);
		double cosPhi = Math.cos(this.phi);

		double newTheta = newCoords.getTheta();
		double newPhi = newCoords.getPhi();

		double col = newTheta + (-PI_HALF - theta);
		double x = rho * Math.sin(newPhi);
		
		int buffX = ((int) Math.round(x * Math.cos(col)) + xHalfMap) - xLowEdge;
		int buffY = ((int) Math.round(((x * (0D - cosPhi)) * Math.sin(col)) 
				+ (rho * Math.cos(newPhi) * (0D - sinPhi)))
				+ yHalfMap) - yLowEdge;
		return new IntPoint(buffX, buffY);
	}
 	
	/**
	 * Converts linear rectangular XY position change to spherical coordinates.
	 *
	 * @param x change in x value (in km)
	 * @param y change in y value (in km)
	 * @return new spherical location
	 */
	public Coordinates convertRectToSpherical(double x, double y) {
		return convertRectToSpherical(x, y, MARS_RADIUS_KM);
	}

	/**
	 * Converts linear rectangular XY position change to spherical coordinates.
	 *
	 * @param x change in x value
	 * @param y change in y value
	 * @param rho
	 * @return new spherical location
	 */
	public Coordinates convertRectIntToSpherical(int x, int y, double rho) {
		var point = IntegerMapData.convertRectIntToSpherical(x, y, phi, theta, rho);
		return new Coordinates(point.phi(), point.theta());
	}

	/**
	 * Converts linear rectangular XY position change to spherical coordinates with
	 * rho value for map.
	 *
	 * @param x              change in x value (# of pixels or km)
	 * @param y              change in y value (# of pixels or km)
	 * @param rho            radius (in km) or map box height divided by pi (# of pixels)
	 * @return new spherical location
	 */
	public Coordinates convertRectToSpherical(double x, double y, double rho) {
		var point = IntegerMapData.convertRectToSpherical(x, y, phi, theta, rho);
		return new Coordinates(point.phi(), point.theta());
	}
	
	/**
	 * Returns angle direction to another location on surface of sphere 0 degrees is
	 * north (clockwise).
	 *
	 * @param otherCoords target location
	 * @return angle direction to target (in radians)
	 */
	public Direction getDirectionToPoint(Coordinates otherCoords) {

		double phi1 = -1D * (phi - PI_HALF);
		double phi2 = -1D * (otherCoords.phi - PI_HALF);
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
		double remainder = distance;
		if (distance >= 10D)
			remainder = distance - (iterations * iterationDistance);

		// Get successive iteration locations.
		Coordinates startCoords = this;
		for (int x = 0; x < iterations; x++) {
			double newY = -1D * direction.getCosDirection() * (iterationDistance);
			double newX = direction.getSinDirection() * (iterationDistance);
			startCoords = startCoords.convertRectToSpherical(newX, newY);
		}

		// Get final location based on remainder.
		double finalY = -1D * direction.getCosDirection() * remainder;
		double finalX = direction.getSinDirection() * remainder;
		return startCoords.convertRectToSpherical(finalX, finalY);
	}

	/**
	 * Returns true if coordinates have equal phi and theta values.
	 *
	 * @param otherCoords Coordinates object to be matched against
	 * @return true if Coordinates values match, false otherwise
	 */
	public boolean equals(Object otherCoords) {
		if (this == otherCoords) return true;
		if (otherCoords instanceof Coordinates other) {
            return (phi == other.phi) && (theta == other.theta);
		}

		return false;
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		return (int) ((phi * 1000D) + (theta * 1000D));
	}
}
