/*
 * Mars Simulation Project
 * CoordinatesFormat.java
 * @date 2025-09-16
 * @author Barry Evans
 */
package com.mars_sim.core.map.location;

import java.text.DecimalFormat;
import java.text.ParseException;

import com.mars_sim.core.tool.Msg;

/**
 * Provides methods to parse and generate text representations of Coordinates objects.
 */
public final class CoordinatesFormat {
    
	private static final double DEG_TO_RADIAN  = Math.PI / 180;
	private static final double PI_HALF = Math.PI / 2;
    private static final double TWO_PI = Math.PI * 2;

    private static final String DEG_SIGN = Msg.getString("direction.degreeSign");

    private static final String NORTH = Msg.getString("direction.northShort");
	private static final String EAST = Msg.getString("direction.eastShort");
	private static final String SOUTH = Msg.getString("direction.southShort");
	private static final String WEST = Msg.getString("direction.westShort");

	/** Currently, lat and lon are up to 4 decimal places. Thus the decimal format '0.0000' is used. */
	static final DecimalFormat DIGIT_FORMAT = new DecimalFormat("0.0000"); //$NON-NLS-1$

    private CoordinatesFormat() {
        // Utility class
    }   

    /**
     * Parse a text representation of coordinates into a Coordinates object.
     * @param latitude
     * @param longitude
     * @throws CoordinatesException 
     */
    public static Coordinates fromString(String latitude, String longitude) throws CoordinatesException {
        double phi = parseLatitude2Phi(latitude);
        double theta = parseLongitude2Theta(longitude);
        return new Coordinates(phi, theta);
    }
    
    /**
     * Create a Coordinates object from a string representation.
     * @param string
     * @return
     */
    public static Coordinates fromString(String string) throws CoordinatesException{
        String [] parts = string.trim().split("\\s+");

        switch(parts.length) {
            case 2: return fromString(parts[0], parts[1]);
            case 4: return fromString(parts[0] + " " + parts[1], parts[2] + " " + parts[3]);
            default: throw new CoordinatesException(Msg.getString("CoordinatesFormat.error.invalidFormat"));
        }
    }

    /**
     * Parse latitude string to phi, throwing an unchecked exception if it fails.
     * @param latitude
     * @return
     */
    static double parseLatitude2PhiUncheck(String latitude) {
        try {
            return parseLatitude2Phi(latitude);
        } catch (CoordinatesException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Parse longitude string to theta, throwing an unchecked exception if it fails.
     * @param longitude
     * @return
     */
    static double parseLongitude2ThetaUncheck(String longitude) {
        try {
            return parseLongitude2Theta(longitude);
        } catch (CoordinatesException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
    /**
	 * Parses a latitude string into a phi value. e.g. input: "25.344 N"
	 * For latitude string: North is positive (+); South is negative (-)
	 * For phi : it starts from the north pole (phi = 0) to the south pole (phi = PI)
	 *
	 * @param latitude as string
	 * @return phi value in radians
	 * @throws ParseException if latitude string could not be parsed.
	 */
	static double parseLatitude2Phi(String latitude) throws CoordinatesException {
        var latValue = parseString(latitude, NORTH, SOUTH);
        if ((latValue > 90D) || (latValue < -90))
            throw new CoordinatesException(Msg.getString("CoordinatesFormat.error.latitudeRange"));
        if (latValue >= 0)
            latValue = 90D - latValue;
        else
            latValue = 90D + (-latValue);  // This is a negative value`
        return latValue * DEG_TO_RADIAN;
    }

	/**
	 * Parses a longitude string into a theta value. e.g. input:  "63.5532 W"
	 * Note: East is positive (+), West is negative (-)
	 *
	 * @param longitude as string
	 * @return theta value in radians
	 * @throws ParseException if longitude string could not be parsed.
	 */
	static double parseLongitude2Theta(String longitude) throws CoordinatesException {
        var longValue = parseString(longitude, EAST, WEST);
        if ((longValue > 360) || (longValue < -360))
            throw new CoordinatesException(Msg.getString("CoordinatesFormat.error.longitudeRange"));
        if (longValue < 0)
            longValue = 360D + longValue;  // This is a negative value
        return longValue * DEG_TO_RADIAN;
    }

    public static String checkLat(String latitude) {
        // Attempt to parse the string; will throw exception if invalid
        try {
            parseLatitude2Phi(latitude);
        } catch (CoordinatesException e) {
            return e.getMessage();
        }
        return null;
    }

    public static String checkLon(String longitude) {
        // Attempt to parse the string; will throw exception if invalid
        try {
            parseLongitude2Theta(longitude);
        } catch (CoordinatesException e) {
            return e.getMessage();
        }
        return null;
    }

    private static final double parseString(String valueStr, String positiveDir, String negativeDir)
            throws CoordinatesException {

		String cleanValue = valueStr.toUpperCase().trim();

		if (cleanValue.isEmpty())
			throw new CoordinatesException(Msg.getString("CoordinatesFormat.error.blank"));

		try {
			return Double.parseDouble(cleanValue);

		} catch (NumberFormatException e) {
            // Not a pure decimal number, continue to parse.
		}

        String numberString = cleanValue;
        int factor = 1;
        if (cleanValue.endsWith(negativeDir)) {
            factor = -1;
            numberString = cleanValue.substring(0, cleanValue.length() - negativeDir.length()).trim();
        }
        else if (cleanValue.endsWith(positiveDir)) {
            numberString = cleanValue.substring(0, cleanValue.length() - positiveDir.length()).trim();
        }

        // Must have a direction and/or degree sign
        double value = 0D;
        try {
            if (numberString.endsWith(DEG_SIGN)) //$NON-NLS-1$
                numberString = numberString.substring(0, numberString.length() - 1);

            // Always use a '.' as the decimal separator
            numberString = numberString.replace(DIGIT_FORMAT.getDecimalFormatSymbols().getDecimalSeparator(), '.');
            value = Double.parseDouble(numberString);
        } catch (NumberFormatException e) {
            throw new CoordinatesException(Msg.getString("CoordinatesFormat.error.badNumber", numberString));
        }

        value *= factor;

		return value;
    }

    /**
	 * Gets a common formatted string to represent longitude for a given theta. 
	 * e.g. "35.6670 E". This is localised to the language bundle
	 *
	 * @param coords This is the Coordinates object
	 * @return formatted longitude string for this Coordinates object
	 */
	static String getFormattedLongitudeString(Coordinates coords) {
        double theta = coords.getTheta();
		double degrees = 0;
		String direction = "";

		if ((theta <= Math.PI) && (theta >= 0D)) {
			degrees = Math.toDegrees(theta);
			direction = EAST; //$NON-NLS-1$
		} else if (theta >= Math.PI) {
			degrees = Math.toDegrees(TWO_PI - theta);
			direction = WEST; //$NON-NLS-1$
		}

		// Add a whitespace in between the degree and its directional sign
		return DIGIT_FORMAT.format(degrees) + " " + direction;
	}
    
	/**
	 * Gets a common formatted string to represent latitude for a given phi.
	 * e.g. "35.6230 S". This will be localised to the language bundle
	 *
	 * @param coords the Coordinates object
	 * @return formatted latitude string for this Coordinates object
	 */
	static String getFormattedLatitudeString(Coordinates coords) {
		double degrees = 0;
		String direction = "";
        double phi = coords.getPhi();

		if (phi <= PI_HALF) {
			degrees = ((PI_HALF - phi) / PI_HALF) * 90D;
			direction = NORTH; //$NON-NLS-1$
		} else {
			degrees = ((phi - PI_HALF) / PI_HALF) * 90D;
			direction = SOUTH; //$NON-NLS-1$
		}

		// Add a whitespace in between the degree and its directional sign
		return DIGIT_FORMAT.format(degrees) + " " + direction; //$NON-NLS-1$
	}

    /**
     * This returns a formatted string for both latitude and longitude using any
     * languages bindings.
     * @param coords
     * @return
     */
    public static String getFormattedString(Coordinates coords) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getFormattedLatitudeString(coords));
        buffer.append(' ');
        buffer.append(getFormattedLongitudeString(coords));
        return buffer.toString();
    }

    /**
     * Get a decimal string representation of the coordinates. This is language independent.
     * @param coords
     * @return
     */
    public static String getDecimalString(Coordinates coords) {
        return DIGIT_FORMAT.format(coords.getLatitudeDouble()) + " " + DIGIT_FORMAT.format(coords.getLongitudeDouble());
    }

}
