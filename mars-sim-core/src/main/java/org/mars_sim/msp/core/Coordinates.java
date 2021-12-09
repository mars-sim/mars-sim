/*
 * Mars Simulation Project
 * Coordinates.java
 * @date 2021-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.mars_sim.msp.core.environment.Environment;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Spherical Coordinates. Represents a location on virtual Mars in spherical
 * coordinates. It provides some useful methods involving those coordinates, as
 * well as some static methods for general coordinate calculations.<br/>
 * {@link #theta} is longitute in (0 - 2 PI) radians. <br/>
 * {@link #phi} is latitude in (0 - PI) radians. <br/>
 */
public class Coordinates implements Serializable {
	// Note: in future, may add rho as the diameter of planet (in km) or 2* MARS_RADIUS_KM = 3393.0

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Coordinates.class.getName());

	private static final double DEG_TO_RADIAN  = Math.PI / 180;
	private static final double RADIAN_TO_DEG  = 180 / Math.PI;
	private static final double PI_HALF = Math.PI / 2.0;
	private static final double TWO_PI = Math.PI * 2D;

	// stored for efficiency but not serialized.
	private static final transient String NORTH_SHORT = Msg.getString("direction.northShort");
	private static final transient String EAST_SHORT = Msg.getString("direction.eastShort");
	private static final transient String SOUTH_SHORT = Msg.getString("direction.southShort");
	private static final transient String WEST_SHORT = Msg.getString("direction.westShort");
	private static final transient String LON_BAD_FORMAT = Msg.getString("Coordinates.error.longitudeBadFormat");
	private static final transient String LAT_BAD_FORMAT = Msg.getString("Coordinates.error.latitudeBadFormat");
	private static final transient String LON_BEGIN_WITH = Msg.getString("Coordinates.error.longitudeBeginWith");
	private static final transient String LAT_BEGIN_WITH = Msg.getString("Coordinates.error.latitudeBeginWith");

	// Data members
	/** Phi value of coordinates PHI is latitude in 0-PI radians. */
	private double phi;
	/** Theta value of coordinates, THETA is longitude in 0-2PI radians. */
	private double theta;

	/** Formatted string of the latitude. */
	private String latCache;
	/** Formatted string of the longitude. */
	private String lonCache;

	private static DecimalFormat formatter = new DecimalFormat(Msg.getString("direction.decimalFormat")); //$NON-NLS-1$

	/**
	 * Constructs a Coordinates object, hence a constructor.
	 *
	 * @param phi    (latitude) the phi angle of the spherical coordinate
	 * @param theta  (longitude) the theta angle of the spherical coordinate
	 */
	public Coordinates(double phi, double theta) {

		// Set Coordinates
		// Make sure phi is between 0 and PI.
		this.phi = phi;
		while (this.phi > Math.PI)
			this.phi -= Math.PI;
		while (this.phi < 0)
			this.phi += Math.PI;

		// Make sure theta is between 0 and 2 PI.
		this.theta = theta;
		while (this.theta < 0D)
			this.theta += TWO_PI;
		while (this.theta > TWO_PI)
			this.theta -= TWO_PI;
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
	 */
	public Coordinates(String latitude, String longitude) {
		this(parseLatitude2Phi(latitude), parseLongitude2Theta(longitude));
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
	 * phi accessor (related to latitude)
	 *
	 * @return the phi angle value of the coordinate
	 */
	public double getPhi() {
		return phi;
	}

	/**
	 * theta accessor (related to longitude)
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
//		Note: May return getAngleSLC(otherCoords);
//		Note: May return getAngleVincenty(otherCoords);
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
//	public double getAngleSLC(Coordinates otherCoords) {
//
//		double phi1 = -1D * (phi - PI_HALF);
//		double phi2 = -1D * (otherCoords.phi - PI_HALF);
//		double diffTheta = Math.abs(theta - otherCoords.theta);
//
//		double temp1 = Math.cos(phi1) * Math.cos(phi2);
//		double temp2 = Math.sin(phi1) * Math.sin(phi2);
//		double temp3 = Math.cos(diffTheta);
//		double temp4 = temp2 + (temp1 * temp3);
//
//		// Make sure temp4 is in valid -1 to 1 range.
//		if (temp4 > 1D)
//			temp4 = 1D;
//		else if (temp4 < -1D)
//			temp4 = -1D;
//
//		double result = Math.acos(temp4);
//		return result;
//	}

	/**
	 * Calculates the arc angle between this location and a given location using the
	 * haversine formula. http://en.wikipedia.org/wiki/Haversine_formula
	 *
	 * @param otherCoords the destination location.
	 * @return the arc angle (radians).
	 */
	private double getAngleHaversine(Coordinates otherCoords) {

		double phi1 = -1D * (phi - PI_HALF);
		double phi2 = -1D * (otherCoords.phi - PI_HALF);
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
//	public double getAngleVincenty(Coordinates otherCoords) {
//
//		double phi1 = -1D * (phi - PI_HALF);
//		double phi2 = -1D * (otherCoords.phi - PI_HALF);
//		double diffTheta = Math.abs(theta - otherCoords.theta);
//
//		double temp1 = Math.pow(Math.cos(phi2) * Math.sin(diffTheta), 2D);
//		double temp2 = Math.cos(phi1) * Math.sin(phi2);
//		double temp3 = Math.sin(phi1) * Math.cos(phi2) * Math.cos(diffTheta);
//		double temp4 = Math.pow(temp2 - temp3, 2D);
//		double temp5 = Math.sqrt(temp1 + temp4);
//
//		double temp6 = Math.sin(phi1) * Math.sin(phi2);
//		double temp7 = Math.cos(phi1) * Math.cos(phi2) * Math.cos(diffTheta);
//		double temp8 = temp6 + temp7;
//
//		double result = Math.atan2(temp5, temp8);
//		return result;
//	}

	/**
	 * Returns the distance in kilometers between this location and the given
	 * coordinates
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

		double rho = Environment.MARS_RADIUS_KM;
		double angle = getAngle(otherCoords);
		double result = rho * angle;
		return result;
	}

	/**
	 * Computes the distance between the two given coordinates
	 *
	 * @param c0
	 * @param c1
	 * @return distance (in km)
	 */
	public static double computeDistance(Coordinates c0, Coordinates c1) {
		if (c0 == null) {
			return 0;
		}
		return c0.getDistance(c1);
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
	 * Gets a coordinate string (with parenthesis and comma) to represent this location.
	 *
	 * @return formatted longitude & latitude string for this coordinate
	 * @see #getFormattedLongitudeString()
	 * @see #getFormattedLatitudeString()
	 */
	public String getCoordinateString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		buffer.append(getFormattedLatitudeString());
		buffer.append(", ");
		buffer.append(getFormattedLongitudeString());
		buffer.append(')');
		return buffer.toString();
	}

	/**
	 * Gets a common formatted string to represent longitude for this location. ex.
	 * "35.6 E"
	 *
	 * @return formatted longitude string for this Coordinates object
	 */
	public String getFormattedLongitudeString() {
		if (lonCache == null) {
			lonCache = getFormattedLongitudeString(theta);
		}
		return lonCache;
	}

	/**
	 * Gets a double to represent longitude for this location. ex. "-35.6"
	 *
	 * @return double longitude
	 */
	public double getLongitudeDouble() {
		double degrees = 0D;

		if ((theta < Math.PI) && (theta >= 0D)) {
			degrees = Math.toDegrees(theta);
		} else if (theta >= Math.PI) {
			degrees = Math.toDegrees(TWO_PI - theta);
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
	private static String getFormattedLongitudeString(double theta) {
		double degrees = 0;
		String direction = "";

		if ((theta < Math.PI) && (theta >= 0D)) {
			degrees = Math.toDegrees(theta);
			direction = EAST_SHORT; //$NON-NLS-1$
		} else if (theta >= Math.PI) {
			degrees = Math.toDegrees(TWO_PI - theta);
			direction = WEST_SHORT; //$NON-NLS-1$
		}

		// Add a whitespace in between the degree and its directional sign
		return formatter.format(degrees) + " " + direction;
	}

	/**
	 * Gets a common formatted string to represent latitude for this location. ex.
	 * "35.6 S"
	 *
	 * @return formatted latitude string for this Coordinates object
	 */
	public String getFormattedLatitudeString() {
		if (latCache == null) {
			latCache = getFormattedLatitudeString(phi);
		}
		return latCache;
	}

	/**
	 * Gets a double to represent latitude location. ex. "-35.6"
	 *
	 * @return latitude double
	 */
	public double getLatitudeDouble() {
		double degrees;
		degrees = 0D;

		if (phi <= PI_HALF) {
			degrees = ((PI_HALF - phi) / PI_HALF) * 90D;
		} else if (phi > PI_HALF) {
			degrees = ((phi - PI_HALF) / PI_HALF) * 90D;
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
	private static String getFormattedLatitudeString(double phi) {
		double degrees = 0;
		String direction = "";

		if (phi <= PI_HALF) {
			degrees = ((PI_HALF - phi) / PI_HALF) * 90D;
			direction = NORTH_SHORT; //$NON-NLS-1$
		} else if (phi > PI_HALF) {
			degrees = ((phi - PI_HALF) / PI_HALF) * 90D;
			direction = SOUTH_SHORT; //$NON-NLS-1$
		}

		// Note : direction.decimalFormat = 0.0
		// Add a whitespace in between the degree and its directional sign
		return formatter.format(degrees) + " " + direction; //$NON-NLS-1$
	}

	/**
	 * Converts phi to latitude
	 *
	 * @return latitude in degrees
	 */
	public double getPhi2Lat() {
		return getPhi2LatRadian() * RADIAN_TO_DEG;
	}

	/**
	 * Converts phi in radians to lat in radians
	 *
	 * @return latitude in radians
	 */
	public double getPhi2LatRadian() {
		double phi = getPhi();
		double lat_radian = 0;
		if (phi < PI_HALF) {
			lat_radian = PI_HALF - phi;
		} else if (phi > PI_HALF) {
			lat_radian = phi - PI_HALF;
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

		double sinPhi = Math.sin(this.phi);
		double cosPhi = Math.cos(this.phi);

		double temp_col = newTheta + (-PI_HALF - theta);
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
		return convertRectToSpherical(x, y, Environment.MARS_RADIUS_KM);
	}


	/**
	 * Converts linear rectangular XY position change to spherical coordinates with
	 * rho value for map.
	 *
	 * @param x              change in x value (in km)
	 * @param y              change in y value (in km)
	 * @param rho            rho value of map used (in km)
	 */
	public Coordinates convertRectToSpherical(double x, double y, double rho) {

		double sinPhi = Math.sin(this.phi);
		double sinTheta = Math.sin(this.theta);
		double cosPhi = Math.cos(this.phi);
		double cosTheta = Math.cos(this.theta);

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
				theta_new = TWO_PI + theta_new;
		}

		return new Coordinates(phi_new, theta_new);
	}

	/**
	 * Returns angle direction to another location on surface of sphere 0 degrees is
	 * north (clockwise)
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
	 * Parse a latitude string into a phi value. e.g. input: "25.344 N"
	 * For latitude string: North is positive (+); South is negative (-)
	 * For phi : it starts from the north pole (phi = 0) to the south pole (phi = PI)
	 *
	 * @param latitude as string
	 * @return phi value in radians
	 * @throws ParseException if latitude string could not be parsed.
	 */
	public static double parseLatitude2Phi(String latitude) {
		double latValue = 0D;

		String cleanLatitude = latitude.toUpperCase().trim();

		if (cleanLatitude.isEmpty())
			throw new IllegalStateException("Latitude is blank !");

		// Checks if the latitude string is a pure decimal number
		boolean pureDecimal = true;

		try {
			latValue = Double.parseDouble(latitude);
			if ((latValue > 90D) || (latValue < -90))
				throw new IllegalStateException("Latitude value out of range : " + latValue);
			if (latValue >= 0)
				latValue = 90D - latValue;
			else
				latValue = -90D;
		} catch (NumberFormatException e) {
			pureDecimal = false;
		}

		if (!pureDecimal) {
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

			if ((latValue > 90D) || (latValue < -90))
				throw new IllegalStateException("Latitude value out of range : " + latValue);

			// TODO parse latitude depending on locale and validate
			String direction = "" + cleanLatitude.charAt(latitude.length() - 1);
			if (direction.compareToIgnoreCase(NORTH_SHORT) == 0)
				latValue = 90D - latValue;
			else if (direction.compareToIgnoreCase(SOUTH_SHORT) == 0)
				latValue += 90D;
			else
				throw new IllegalStateException("Invalid Latitude direction : " + direction);
		}


		double phi = DEG_TO_RADIAN * latValue;
		return phi;
	}

	/**
	 * Parse a longitude string into a theta value. e.g. input:  "63.5532 W"
	 * Note: East is positive (+), West is negative (-)
	 *
	 * @param longitude as string
	 * @return theta value in radians
	 * @throws ParseException if longitude string could not be parsed.
	 */
	public static double parseLongitude2Theta(String longitude) {
		double longValue = 0D;

		String cleanLongitude = longitude.toUpperCase().trim();

		if (cleanLongitude.isEmpty())
			throw new IllegalStateException("Longitude is blank !");

		// Checks if the longitude string is a pure decimal number
		boolean pureDecimal = true;

		try {
			longValue = Double.parseDouble(longitude);

			while (longValue < 0D)
				longValue += 360;
			while (longValue > 360)
				longValue -= 360;

		} catch (NumberFormatException e) {
			pureDecimal = false;
		}

		if (!pureDecimal) {

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

			// Note: parse longitude depending on locale and validate
			String directionStr = "" + cleanLongitude.charAt(cleanLongitude.length() - 1);

			if (directionStr.compareToIgnoreCase(WEST_SHORT) == 0)
				longValue = 360D - longValue;
			else if (directionStr.compareToIgnoreCase(EAST_SHORT) != 0)
				throw new IllegalStateException("Invalid Longitude direction : " + directionStr);

		}

		double theta = DEG_TO_RADIAN * longValue;
		return theta;
	}

	/**
	 * Gets a random latitude.
	 *
	 * @return latitude
	 */
	public static double getRandomLatitude() {
		// Random latitude should be less likely to be near the poles.
		// Make sure phi is between 0 and PI.
		double phi = .7 * RandomUtil.getRandomDouble(Math.PI);
		return phi;
	}

	/**
	 * Gets a random longitude.
	 *
	 * @return longitude
	 */
	public static double getRandomLongitude() {
		// Make sure theta is between 0 and 2 PI.
		double theta = RandomUtil.getRandomDouble(2D * Math.PI);
		return theta;
	}

	/**
	 * Check for the validity of the input latitude
	 *
	 * @param latitude the input latitude
	 */
	public static String checkLat(String latitude) {

		// Check that settlement latitude is valid.
		if ((latitude == null) || (latitude.isEmpty())) {
			return (Msg.getString("Coordinates.error.latitudeMissing")); //$NON-NLS-1$
		}

		else {
			// check if the second from the last character is a digit or a letter,
			// if a letter, setError
			if (latitude.length() < 3 && Character.isLetter(latitude.charAt(latitude.length() - 2))) {
				return LAT_BAD_FORMAT; //$NON-NLS-1$
			}

			// check if the last character is a digit or a letter,
			// if a digit, setError
			if (latitude.length() < 2 && Character.isDigit(latitude.charAt(latitude.length() - 1))) {
				return LAT_BAD_FORMAT; //$NON-NLS-1$
			}

			String s = latitude.trim().toUpperCase();
			String dir = s.substring(s.length() - 1, s.length());
			Character c = dir.charAt(0);
			if (Character.isDigit(c)) {
				return LAT_BAD_FORMAT; //$NON-NLS-1$
			}

			if (!(s.endsWith(NORTH_SHORT) //$NON-NLS-1$ //$NON-NLS-2$
					|| s.endsWith(SOUTH_SHORT)) //$NON-NLS-1$ //$NON-NLS-2$
				) {
				return Msg.getString("Coordinates.error.latitudeEndWith", //$NON-NLS-1$
						NORTH_SHORT, SOUTH_SHORT);
			} else {
				String numLatitude = s.substring(0, s.length() - 1);
				try {
					double doubleLatitude = Double.parseDouble(numLatitude);
					if ((doubleLatitude < 0) || (doubleLatitude > 90)) {
						return LAT_BEGIN_WITH; //$NON-NLS-1$
					}
				} catch (NumberFormatException e) {
					return LAT_BEGIN_WITH; //$NON-NLS-1$
				}
			}
		}
		return null;
	}

	/**
	 * Check for the validity of the input longitude
	 *
	 * @param longitude the input longitude
	 */
	public static String checkLon(String longitude) {

		// Check that settlement longitude is valid.
		if ((longitude == null) || (longitude.isEmpty())) {
			return Msg.getString("Coordinates.error.longitudeMissing"); //$NON-NLS-1$
		}

		else {
			// check if the second from the last character is a digit or a letter,
			// if a letter, setError
			if (longitude.length() < 3 && Character.isLetter(longitude.charAt(longitude.length() - 2))) {
				return LON_BAD_FORMAT; //$NON-NLS-1$
			}

			// check if the last character is a digit or a letter,
			// if a digit, setError
			if (longitude.length() < 2 && Character.isDigit(longitude.charAt(longitude.length() - 1))) {
				return LON_BAD_FORMAT; //$NON-NLS-1$
			}

			String s = longitude.trim().toUpperCase();
			String dir = s.substring(s.length() - 1, s.length());
			Character c = dir.charAt(0);
			if (Character.isDigit(c)) {
				logger.warning(2_000, "The longitude [" + s + "] is missing the direction sign.");
				return LON_BAD_FORMAT; //$NON-NLS-1$
			}

			if (!s.endsWith(WEST_SHORT)  //$NON-NLS-1$ //$NON-NLS-2$
					&& !s.endsWith(EAST_SHORT)  //$NON-NLS-1$ //$NON-NLS-2$
					) {
				return Msg.getString("Coordinates.error.longitudeEndWith", //$NON-NLS-1$
						EAST_SHORT, //$NON-NLS-1$
						WEST_SHORT //$NON-NLS-1$
				);
			} else {
				String numLongitude = s.substring(0, s.length() - 1);
				try {
					double doubleLongitude = Double.parseDouble(numLongitude);
					if ((doubleLongitude < 0) || (doubleLongitude > 180)) {
						return LON_BEGIN_WITH; //$NON-NLS-1$
					}
				} catch (NumberFormatException e) {
					return LON_BEGIN_WITH; //$NON-NLS-1$
				}
			}
		}
		return null;
	}


	/**
	 * Returns true if coordinates have equal phi and theta values
	 *
	 * @param otherCoords Coordinates object to be matched against
	 * @return true if Coordinates values match, false otherwise
	 */
	public boolean equals(Object otherCoords) {
		if (this == otherCoords) return true;
		if (otherCoords instanceof Coordinates) {
			Coordinates other = (Coordinates) otherCoords;
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

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
	}
}
