/**
 * Mars Simulation Project
 * LocationConverter.java
 * @version
 * @author Dalen Kruse
 */

package org.mars_sim.msp.simulation;


/** The LocationConver class creates a Coordinate object based on
 *  parsed latitude and longitude values.
 */

public class LocationConverter {

    /** Constructor
     */

    public LocationConverter(String latitude, String longitude) {}

    /** Create a coordinates location if parameters are valid.
     *  @param latitudeString the latitude string
     *  @param longitudeString the longitude string
     *  @return coordinates object based on parsed longitude and latitude
     */

    public static Coordinates createLocation(String latitudeString, String longitudeString) {

        double phi = 0D;
        double theta = 0D;

        try {
            phi = parseLatitude(latitudeString);
            theta = parseLongitude(longitudeString);
            return new Coordinates(phi, theta);
        }

        catch(IllegalArgumentException e) {}

        return new Coordinates(phi, theta);
    }


    /** Parse a latitude string into a phi value
     *  ex. "25.344 N"
     *  @param latitude as string
     *  @return phi based on latitude string
     *  @throws java.lang.IllegalArgumentException if bad latitude string
     */

    private static double parseLatitude(String latitude) throws IllegalArgumentException {

        boolean badLatitude = false;
        double latValue = 0D;

        if (latitude.trim().equals("")) badLatitude = true;

        try {
            latValue = Double.parseDouble(latitude.substring(0, latitude.length() - 2));
            if ((latValue > 90D) || (latValue < 0)) badLatitude = true;
        }

        catch(NumberFormatException e) { badLatitude = true; }

        char direction = latitude.charAt(latitude.length() - 1);

        if (direction == 'N') latValue = 90D - latValue;

        else if (direction == 'S') latValue += 90D;

        else badLatitude = true;

        if (badLatitude) throw new IllegalArgumentException();

        double phi = Math.PI * (latValue / 180D);

        return phi;

    }


    /** Parse a longitude string into a theta value
     *  ex. "63.5532 W"
     *  @param longitude as string
     *  @return theta based on longitude string
     *  @throws java.lang.IllegalArgumentException if bad longitude string
     */

    private static double parseLongitude(String longitude) throws IllegalArgumentException {

        boolean badLongitude = false;
        double longValue = 0D;

        if (longitude.trim().equals("")) badLongitude = true;

        try {
            longValue = Double.parseDouble(longitude.substring(0, longitude.length() - 2));
            if ((longValue > 180D) || (longValue < 0)) badLongitude = true;
        }

        catch(NumberFormatException e) { badLongitude = true; }

        char direction = longitude.charAt(longitude.length() - 1);

        if (direction == 'W') longValue = 360D - longValue;

        else if (direction != 'E') badLongitude = true;

        if (badLongitude) throw new IllegalArgumentException();

        double theta = (2 * Math.PI) * (longValue / 360D);

        return theta;

    }
}
