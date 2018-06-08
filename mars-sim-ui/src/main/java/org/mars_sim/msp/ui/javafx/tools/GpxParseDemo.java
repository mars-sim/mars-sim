package org.mars_sim.msp.ui.javafx.tools;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.Tile.TileColor;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.tools.Location;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


/**
 * User: hansolo
 * Date: 21.02.17
 * Time: 04:05
 *
 * Needs TilesFX libary (1.3.5 and above)
 */
@SuppressWarnings("restriction")
public class GpxParseDemo extends Application {
    private Tile tile;

    @Override public void init() {
        List<Location> trackPoints;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            InputStream gpxInput  = GpxParseDemo.class.getResourceAsStream("track.gpx");
            SAXParser   saxParser = factory.newSAXParser();
            GpxHandler  handler   = new GpxHandler();
            saxParser.parse(gpxInput, handler);

            trackPoints = handler.getTrack();
        } catch (Throwable err) {
            System.out.println(err);
            trackPoints = new ArrayList<>();
        }

        tile = TileBuilder.create()
                          .prefSize(800, 800)
                          .skinType(SkinType.MAP)
                          .title("Choose B&W, Bright, or Dark")
                          .text("Lago Retico")
                          .currentLocation(new Location(46.57608333, 8.89241667, "Lago Retico", "Map"))
                          .track(trackPoints)
                          .trackColor(TileColor.MAGENTA)
                          .pointsOfInterest(new Location(46.57608333, 8.89241667, "POI 1"),
                                            //new Location(51.912529, 7.631752, "POI 2", TileColor.YELLOW_ORANGE),
                                            new Location(46.57661111, 8.89344444, "POI 3"))
                          .build();
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(tile);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);

        stage.setTitle("mars-sim");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }


    public class GpxHandler extends DefaultHandler {
        private List<Location> track  = new ArrayList<>();
        private StringBuffer   buffer = new StringBuffer();
        private double         lat;
        private double         lon;
        private double         alt;
        private Instant        timestamp;


        public Location[] readTrack(final InputStream IN) throws IOException {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setValidating(true);
                SAXParser  parser  = factory.newSAXParser();
                GpxHandler handler = new GpxHandler();
                parser.parse(IN, handler);
                return handler.getTrackArray();
            } catch (ParserConfigurationException | SAXException e) {
                throw new IOException(e.getMessage());
            }
        }
        public Location[] readTrack(final File FILE) throws IOException {
            InputStream in = new FileInputStream(FILE);
            try {
                return readTrack(in);
            } finally {
                in.close();
            }
        }

        @Override public void startElement(final String URI, final String LOCAL_NAME, final String Q_NAME, final Attributes ATTRIBUTES) throws SAXException {
            buffer.setLength(0);
            if (Q_NAME.equals("trkpt")) {
                lat = Double.parseDouble(ATTRIBUTES.getValue("lat"));
                lon = Double.parseDouble(ATTRIBUTES.getValue("lon"));
            }
        }

        @Override public void endElement(final String URI, final String LOCAL_NAME, final String Q_NAME) throws SAXException {
            if (Q_NAME.equals("trkpt")) {
                track.add(new Location(lat, lon, alt, timestamp, ""));
            } else if (Q_NAME.equals("alt")) {
                alt = Double.parseDouble(buffer.toString());
            } else if (Q_NAME.equals("time")) {
                timestamp = Instant.parse(buffer.toString());
            }
        }

        @Override public void characters(final char[] CHARS, final int START, final int LENGTH) throws SAXException {
            buffer.append(CHARS, START, LENGTH);
        }

        public List<Location> getTrack() { return track; }
        public Location[] getTrackArray() { return track.toArray(new Location[track.size()]); }
    }
}