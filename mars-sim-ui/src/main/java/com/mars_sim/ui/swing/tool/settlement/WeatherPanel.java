/*
 * Mars Simulation Project
 * WeatherPanel.java
 * @date 2026-09-12
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.environment.SunData;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * Displays weather information for a settlement, including sunlight, temperature, wind, and optical conditions.
 */
@SuppressWarnings({ "serial" })
class WeatherPanel extends JPanel {

    private static final SimLogger logger = SimLogger.getLogger(WeatherPanel.class.getName());

    private static final String PROJECTED_SUNRISE = "  Projected Sunrise: ";
    private static final String PROJECTED_SUNSET = "   Projected Sunset: ";
    private static final String PROJECTED_DAYLIGHT = " Projected Daylight: ";
    private static final String SUNRISE = "  Yestersol Sunrise: ";
    private static final String SUNSET = "   Yestersol Sunset: ";
    private static final String DAYLIGHT = " Yestersol Daylight: ";
    private static final String ZENITH = "        Zenith Time: ";
    private static final String MAX_LIGHT = "       Max Sunlight: ";
    private static final String CURRENT_LIGHT = "   Current Sunlight: ";
    private static final String WM = " W/m\u00B2 ";
    private static final String PENDING = " ...  ";

    private static final Font SUN_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private static final Font SUN_BOLD_FONT = new Font(Font.MONOSPACED, Font.BOLD, 12);
    private static final Font TIME_BOLD_FONT = new Font(Font.DIALOG, Font.BOLD, 13);

    private final SettlementMapPanel mapPanel;
    private final Weather weather;
    private final SurfaceFeatures surfaceFeatures;
    private final OrbitInfo orbitInfo;
    private final MasterClock masterClock;

    private JLabel martianTimeLabel;
    private JLabel projectSunriseLabel;
    private JLabel projectSunsetLabel;
    private JLabel projectDaylightLabel;
    private JLabel sunriseLabel;
    private JLabel sunsetLabel;
    private JLabel zenithLabel;
    private JLabel maxSunLabel;
    private JLabel daylightLabel;
    private JLabel currentSunLabel;

    private JLabel temperatureIcon;
    private JLabel windIcon;
    private JLabel opticalIcon;

    WeatherPanel(SettlementMapPanel mapPanel, Weather weather, SurfaceFeatures surfaceFeatures,
            OrbitInfo orbitInfo, MasterClock masterClock) {
        super(new BorderLayout());
        this.mapPanel = mapPanel;
        this.weather = weather;
        this.surfaceFeatures = surfaceFeatures;
        this.orbitInfo = orbitInfo;
        this.masterClock = masterClock;

        setBackground(new Color(0, 0, 0, 128));
        setOpaque(false);

        buildWeatherPanel();

        JPanel weatherPane = new JPanel(new GridLayout(1, 3, 5, 5));
        weatherPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        weatherPane.setBackground(new Color(0, 0, 0, 128));
        weatherPane.setOpaque(false);
        weatherPane.add(temperatureIcon);
        weatherPane.add(windIcon);
        weatherPane.add(opticalIcon);

        JPanel sunlightPanel = new JPanel(new BorderLayout(5, 5));
        sunlightPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        sunlightPanel.setBackground(new Color(0, 0, 0, 128));
        sunlightPanel.setOpaque(false);
        sunlightPanel.add(buildSunPane(), BorderLayout.NORTH);

        add(weatherPane, BorderLayout.NORTH);
        add(sunlightPanel, BorderLayout.CENTER);
    }

    private void buildWeatherPanel() {
        temperatureIcon = new JLabel();
        windIcon = new JLabel();
        opticalIcon = new JLabel();

        updateIcon(0, 0, 0);
    }

    private void updateIcon(double temperatureCache, double windSpeedCache, double opticalDepthCache) {
        if (temperatureIcon == null || windIcon == null || opticalIcon == null) {
            return;
        }

        Icon updatedIcon;
        String tooltip;
        if (temperatureCache < -40) {
            updatedIcon = ImageLoader.getIconByName("weather/ice");
            tooltip = "Frigid";
        }
        else if (temperatureCache < 0) {
            updatedIcon = ImageLoader.getIconByName("weather/snowflake");
            tooltip = "Freezing";
        }
        else if (temperatureCache < 10) {
            updatedIcon = ImageLoader.getIconByName("weather/cloudy");
            tooltip = "Cool";
        }
        else if (temperatureCache < 22) {
            updatedIcon = ImageLoader.getIconByName("weather/spinningSun");
            tooltip = "Balmy";
        }
        else {
            updatedIcon = ImageLoader.getIconByName("weather/desert_sun");
            tooltip = "Sunny";
        }

        temperatureIcon.setIcon(updatedIcon);
        temperatureIcon.setToolTipText(tooltip);

        if (windSpeedCache > 120) {
            if (opticalDepthCache > 0.7) {
                updatedIcon = ImageLoader.getIconByName("weather/sandstorm");
                tooltip = "Sandstorm";
            }
            else {
                updatedIcon = ImageLoader.getIconByName("weather/highWind");
                tooltip = "High Wind";
            }

        }
        else if (windSpeedCache > 80) {
            updatedIcon = ImageLoader.getIconByName("weather/dust_devil");
            tooltip = "Low Wind";
        }
        else if (windSpeedCache > 40) {
            if (temperatureCache < 0) {
                updatedIcon = ImageLoader.getIconByName("weather/frost_wind");
                tooltip = "Frosty Wind";
            }
            else {
                updatedIcon = ImageLoader.getIconByName("weather/cold_wind");
                tooltip = "Cool Wind";
            }
        }
        else {
            updatedIcon = ImageLoader.getIconByName("weather/lowWind");
            tooltip = "Low Wind";
        }

        windIcon.setIcon(updatedIcon);
        windIcon.setToolTipText(tooltip);

        if (opticalDepthCache > 1.0) {
            updatedIcon = ImageLoader.getIconByName("weather/sand");
            tooltip = "Sandy";
        }
        else if (opticalDepthCache > 0.6) {
            updatedIcon = ImageLoader.getIconByName("weather/hazy");
            tooltip = "Hazy";
        }
        else if (opticalDepthCache > 0.3) {
            updatedIcon = ImageLoader.getIconByName("weather/dry");
            tooltip = "Dry";
        }
        else {
            updatedIcon = ImageLoader.getIconByName("weather/line_of_sight");
            tooltip = "Clear Line of Sight";
        }

        opticalIcon.setIcon(updatedIcon);
        opticalIcon.setToolTipText(tooltip);
    }

    private JPanel buildSunPane() {
        JPanel sunPane = new JPanel(new BorderLayout(0, 1));
        sunPane.setBackground(new Color(0, 0, 0, 128));
        sunPane.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.ORANGE, new Color(210, 105, 30)));

        JPanel marsTimePane = new JPanel(new BorderLayout(0, 1));
        String ts = updateMarsTime();
        martianTimeLabel = new JLabel(ts, SwingConstants.CENTER);
        martianTimeLabel.setOpaque(false);
        martianTimeLabel.setBackground(new Color(0, 0, 0, 128));
        martianTimeLabel.setForeground(Color.LIGHT_GRAY);
        martianTimeLabel.setFont(TIME_BOLD_FONT);
        marsTimePane.add(martianTimeLabel, BorderLayout.CENTER);

        sunPane.add(marsTimePane, BorderLayout.NORTH);

        JPanel roundPane = new JPanel(new GridLayout(9, 1, 0, 0));
        roundPane.setBackground(new Color(0, 0, 0, 128));
        roundPane.setOpaque(false);
        roundPane.setPreferredSize(new Dimension(230, 185));

        JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
        taskPaneContainer.setBackground(new Color(0, 0, 0, 128));
        taskPaneContainer.setOpaque(false);
        JXTaskPane actionPane = new JXTaskPane();
        actionPane.setBackground(new Color(0, 0, 0, 128));
        actionPane.setOpaque(false);
        actionPane.getContentPane().setBackground(new Color(0, 0, 0, 128));
        actionPane.setTitle("Solar Data");
        actionPane.add(roundPane, BorderLayout.CENTER);
        taskPaneContainer.add(actionPane);
        sunPane.add(taskPaneContainer, BorderLayout.CENTER);

        double[] projectSunTime = {0, 0, 0};
        if (mapPanel.getSettlement() != null) {
            projectSunTime = orbitInfo.getSunTimes(mapPanel.getSettlement());
        }

        projectSunriseLabel = new JLabel(PROJECTED_SUNRISE + StyleManager.DECIMAL1_MSOL.format(projectSunTime[0]));
        projectSunsetLabel = new JLabel(PROJECTED_SUNSET + StyleManager.DECIMAL1_MSOL.format(projectSunTime[1]));
        projectDaylightLabel = new JLabel(PROJECTED_DAYLIGHT + StyleManager.DECIMAL1_MSOL.format(projectSunTime[2]));

        sunriseLabel = new JLabel(SUNRISE + PENDING);
        sunsetLabel = new JLabel(SUNSET + PENDING);
        daylightLabel = new JLabel(DAYLIGHT + PENDING);

        zenithLabel = new JLabel(ZENITH + PENDING);
        maxSunLabel = new JLabel(MAX_LIGHT + PENDING);

        currentSunLabel = new JLabel(CURRENT_LIGHT + PENDING);

        projectSunriseLabel.setFont(SUN_FONT);
        sunriseLabel.setFont(SUN_FONT);
        projectSunsetLabel.setFont(SUN_FONT);
        sunsetLabel.setFont(SUN_FONT);
        projectDaylightLabel.setFont(SUN_FONT);
        daylightLabel.setFont(SUN_FONT);

        zenithLabel.setFont(SUN_FONT);

        currentSunLabel.setFont(SUN_BOLD_FONT);
        maxSunLabel.setFont(SUN_FONT);

        Color orange = Color.orange;
        Color brown = new Color(153, 102, 0).brighter();
        Color yellow = Color.yellow.brighter().brighter();
        Color white = Color.white;
        Color red = Color.pink;
        Color lightBlue = new Color(189, 240, 255);

        projectSunriseLabel.setForeground(red);
        sunriseLabel.setForeground(red);

        projectSunsetLabel.setForeground(brown);
        sunsetLabel.setForeground(brown);

        projectDaylightLabel.setForeground(yellow);
        daylightLabel.setForeground(yellow);

        zenithLabel.setForeground(white);
        maxSunLabel.setForeground(lightBlue);
        currentSunLabel.setForeground(orange);

        projectSunriseLabel.setToolTipText("The projected time of sunrise");
        sunriseLabel.setToolTipText("The time of yestersol sunrise");
        projectSunsetLabel.setToolTipText("The projected time of sunset");
        sunsetLabel.setToolTipText("The time of yestersol sunset");
        projectDaylightLabel.setToolTipText("The projected duration of time in a sol having sunlight");
        daylightLabel.setToolTipText("The duration of time in a sol having sunlight");
        zenithLabel.setToolTipText("The time at which the solar irradiance is at max");
        maxSunLabel.setToolTipText("The max solar irradiance of yestersol as recorded");
        currentSunLabel.setToolTipText("The current solar irradiance as recorded");

        roundPane.add(currentSunLabel);
        roundPane.add(maxSunLabel);

        roundPane.add(projectSunriseLabel);
        roundPane.add(sunriseLabel);

        roundPane.add(zenithLabel);

        roundPane.add(projectSunsetLabel);
        roundPane.add(sunsetLabel);

        roundPane.add(projectDaylightLabel);
        roundPane.add(daylightLabel);

        return sunPane;
    }

    private void updateMarsTimeLabel() {
        if (martianTimeLabel == null) {
            return;
        }

        String ts = updateMarsTime();
        SwingHelper.runInEDT(() -> {
            if (martianTimeLabel != null) {
                martianTimeLabel.setText(ts);
            }
        });
    }

    private void displaySunData(Coordinates location) {
        double[] time = orbitInfo.getSunTimes(mapPanel.getSettlement());

        weather.calculateSunRecord(location);
        SunData data = weather.getSunRecord(location);

        int offset = mapPanel.getSettlement().getTimeZone().getMSolOffset();

        double adj0 = getAdjustedTime(time[0], offset);
        double adj1 = getAdjustedTime(time[1], offset);

        String projRise = PROJECTED_SUNRISE + StyleManager.DECIMAL1_MSOL.format(adj0);
        String projSet = PROJECTED_SUNSET + StyleManager.DECIMAL1_MSOL.format(adj1);
        String projDay = PROJECTED_DAYLIGHT + StyleManager.DECIMAL1_MSOL.format(time[2]);

        String ts = updateMarsTime();

        SwingHelper.runInEDT(() -> {
            if (martianTimeLabel != null) {
                martianTimeLabel.setText(ts);
            }

            if (projectSunriseLabel != null) {
                projectSunriseLabel.setText(projRise);
            }
            if (projectSunsetLabel != null) {
                projectSunsetLabel.setText(projSet);
            }
            if (projectDaylightLabel != null) {
                projectDaylightLabel.setText(projDay);
            }

            if (data == null) {
                logger.warning(0, "Yestersol sunlight data unavailable at " + location + ".");
                return;
            }

            double adj3 = getAdjustedTime(data.getSunrise(), offset);
            double adj4 = getAdjustedTime(data.getSunset(), offset);
            double adj5 = getAdjustedTime(data.getZenith(), offset);

            if (sunriseLabel != null) {
                sunriseLabel.setText(SUNRISE + StyleManager.DECIMAL1_MSOL.format(adj3));
            }
            if (sunsetLabel != null) {
                sunsetLabel.setText(SUNSET + StyleManager.DECIMAL1_MSOL.format(adj4));
            }

            if (daylightLabel != null) {
                daylightLabel.setText(DAYLIGHT + StyleManager.DECIMAL1_MSOL.format(data.getDaylight()));
            }

            if (zenithLabel != null) {
                zenithLabel.setText(ZENITH + StyleManager.DECIMAL1_MSOL.format(adj5));
            }

            if (maxSunLabel != null) {
                maxSunLabel.setText(MAX_LIGHT + data.getMaxSun() + WM);
            }
        });
    }

    private void updateCurrentSunlight(Settlement settlement) {
        if (currentSunLabel == null) {
            return;
        }

        int irr = (int) getSolarIrradiance(settlement.getCoordinates());
        SwingHelper.runInEDT(() -> {
            if (currentSunLabel != null) {
                currentSunLabel.setText(CURRENT_LIGHT + irr + WM);
            }
        });
    }

    /**
     * Updates the weather panel with the latest data for the given settlement.
     * @param settlement the settlement for which to update the weather panel
     */
    void update(Settlement settlement) {
        if (settlement == null) {
            return;
        }

        Coordinates coordinates = settlement.getCoordinates();
        double temperatureCache = Math.round(weather.getTemperature(coordinates) * 100.0) / 100.0;
        double windSpeedCache = Math.round(weather.getWindSpeed(coordinates) * 100.0) / 100.0;
        double opticalDepthCache = surfaceFeatures.getOpticalDepth(coordinates);

        updateIcon(temperatureCache, windSpeedCache, opticalDepthCache);
        displaySunData(coordinates);
        updateMarsTimeLabel();
        updateCurrentSunlight(settlement);
    }

    private double getSolarIrradiance(Coordinates c) {
        return surfaceFeatures.getSolarIrradiance(c);
    }

    private String updateMarsTime() {
        if (mapPanel.getSettlement() == null) {
            return "";
        }

        int offset = mapPanel.getSettlement().getTimeZone().getMSolOffset();
        String zoneID = mapPanel.getSettlement().getTimeZone().getId();
        return masterClock.getMarsTimeWithOffset(offset).getZonedDateTimeStamp(zoneID);
    }

    private double getAdjustedTime(double time, int offset) {
        double adjTime = time + offset;
        if (adjTime > 999) {
            adjTime -= 1000;
        }
        else if (adjTime < 0) {
            adjTime += 1000;
        }
        return adjTime;
    }
}
