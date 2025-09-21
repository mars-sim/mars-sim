package com.mars_sim.core.map.location;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.tool.Msg;

class CoordinatesFormatTest {
    private static final double ERROR_MARGIN_RAD = .005D;
	private static final double DEG_TO_RADIAN  = Math.PI / 180;

    private static final String DEG_SIGN = Msg.getString("direction.degreeSign");

    private static final String NORTH = Msg.getString("direction.northShort");
	private static final String EAST = Msg.getString("direction.eastShort");
	private static final String SOUTH = Msg.getString("direction.southShort");
	private static final String WEST = Msg.getString("direction.westShort");
    
    /**
     * Test the parseLongitude method.
     */
    @Test
    void testParseLongitude() throws CoordinatesException {

        assertEquals(226.98 * DEG_TO_RADIAN, CoordinatesFormat.parseLongitude2Theta("226.98" + DEG_SIGN + " " + EAST), ERROR_MARGIN_RAD);
        assertEquals(46.98 * DEG_TO_RADIAN, CoordinatesFormat.parseLongitude2Theta("46.98" + DEG_SIGN + " " + EAST), ERROR_MARGIN_RAD);
        assertEquals(0D, CoordinatesFormat.parseLongitude2Theta("0" + DEG_SIGN + " " + EAST), 0);
        assertEquals(3D * Math.PI / 2D, CoordinatesFormat.parseLongitude2Theta("90.0" + DEG_SIGN + " " + WEST), 0);
        assertEquals(3D * Math.PI / 2D, CoordinatesFormat.parseLongitude2Theta("90.0 " + WEST), 0);
        assertEquals(3D * Math.PI / 2D, CoordinatesFormat.parseLongitude2Theta("-90.0"), 0);
        assertEquals(0, CoordinatesFormat.parseLongitude2Theta("0 W"), 0);
    }
    
    /**
     * Test the parseLatitude method.
     * @throws CoordinatesException 
     */
    @Test
    void testParseLatitude() throws CoordinatesException {
        assertEquals(Math.PI / 2D, CoordinatesFormat.parseLatitude2Phi("0.0" + DEG_SIGN + " " + NORTH), 0);
        assertEquals(Math.PI, CoordinatesFormat.parseLatitude2Phi("90.0" + DEG_SIGN + " " + SOUTH), 0);
        assertEquals(1.27, CoordinatesFormat.parseLatitude2Phi("17.23" + DEG_SIGN + " " + NORTH), ERROR_MARGIN_RAD);
        assertEquals(0D, CoordinatesFormat.parseLatitude2Phi("90.0 " + NORTH), 0);
        assertEquals(0D, CoordinatesFormat.parseLatitude2Phi("+90"), 0);
        assertEquals(Math.PI, CoordinatesFormat.parseLatitude2Phi("-90"), 0);
    }

    /**
     * Test the getFormattedLongitudeString method.
     */
    @Test
    void testGetFormattedLongitudeString() {
        Coordinates loc1 = new Coordinates (0D, 0D);
        String lonString1 = CoordinatesFormat.getFormattedLongitudeString(loc1);
        DecimalFormat format = new DecimalFormat();
        char decimalPoint = format.getDecimalFormatSymbols().getDecimalSeparator();
        String s2 = "0" + decimalPoint + "0000 " + EAST ;
        assertEquals(s2, lonString1);
    }
    
    /**
     * Test the getFormattedLongitudeString method.
     */
    @Test
    void testGetFormattedLongitudeString2() {
        
        Coordinates loc1 = new Coordinates (0D, Math.PI/2D);
        String lonString1 = CoordinatesFormat.getFormattedLongitudeString(loc1);
        DecimalFormat format = new DecimalFormat();
        char decimalPoint = format.getDecimalFormatSymbols().getDecimalSeparator();
        String s2 = "90" + decimalPoint + "0000 " + EAST ;
        assertEquals(s2, lonString1);
    }
    
    /**
     * Test the getFormattedLatitudeString method.
     */
    @Test
    void testGetFormattedLatitudeString() {
        
        Coordinates loc1 = new Coordinates (0D, 0D);
        String latString1 = CoordinatesFormat.getFormattedLatitudeString(loc1);
        DecimalFormat format = new DecimalFormat();
        char decimalPoint = format.getDecimalFormatSymbols().getDecimalSeparator();
        String s2 = "90"+ decimalPoint + "0000 N";
        assertEquals(s2, latString1);
    }

    
    @Test
    void testFormatParseDecimal() throws CoordinatesException {
        double[][] values = {{10.1000, 20.2000}, {10.1000, -10.0000}, {-10.0000, 20.200}, {-10.0000, -20.2000},
                           {0.0000, 0.0000}, {90.0000, 180.0000}, {-45.0000, 180.0000},
                           {45.1234, 100.0000}, {-45.1234, -170.0000}
                          };

        for(var v : values) {
            String txt = CoordinatesFormat.DIGIT_FORMAT.format(v[0])
                        + " "
                        + CoordinatesFormat.DIGIT_FORMAT.format(v[1]);
            var coord = CoordinatesFormat.fromString(txt);
            var formatted = CoordinatesFormat.getDecimalString(coord);

            assertEquals(txt, formatted);
        }
    }

    @Test
    void testFormatParseFull() throws ParseException, CoordinatesException {
        double[][] values = {{10.1000,20.2000}, {10.1000,10.0000},
                           {0.5000,1.0000}, {90.0000,175.0000},
                           {45.0000 ,179.0000}, {45.1234,100.0000},
                           {45.1234 ,170.0000}
                          };
        // Do North & West
        for(var v : values) {
            var s = CoordinatesFormat.DIGIT_FORMAT.format(v[0]);
            System.out.println("Parsing: " + s);
            var c = CoordinatesFormat.DIGIT_FORMAT.parse(s).doubleValue();
            assertEquals(c, v[0], 0.001);

            String txt = CoordinatesFormat.DIGIT_FORMAT.format(v[0])
                            + " " + NORTH + " "
                            + CoordinatesFormat.DIGIT_FORMAT.format(v[1])
                            + " " + WEST;
            var coord = CoordinatesFormat.fromString(txt);
            var formatted = CoordinatesFormat.getFormattedString(coord);

            assertEquals(txt, formatted);
        }

        // Do South & West
        for(var v : values) {
            String txt = CoordinatesFormat.DIGIT_FORMAT.format(v[0])
                            + " " + SOUTH + " "
                            + CoordinatesFormat.DIGIT_FORMAT.format(v[1])
                            + " " + EAST;
            var coord = CoordinatesFormat.fromString(txt);
            var formatted = CoordinatesFormat.getFormattedString(coord);

            assertEquals(txt, formatted);
        }
    }
}
