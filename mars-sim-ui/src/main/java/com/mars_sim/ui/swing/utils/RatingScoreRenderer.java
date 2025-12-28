/*
 * Mars Simulation Project
 * RatingScoreRenderer.java
 * @date 2024-01-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.tool.Msg;

/**
 * This static class provides methods to render a RatingScore.
 */
public class RatingScoreRenderer {

    /**
     * Prefix to the key in the message bundle
     */
    private static final String RATING_KEY = "Rating.modifiers.";
    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.###");

    private RatingScoreRenderer() {}

    /**
     * Render a RatingScore into a HTML fragment that shows the Base and Modifiers
     * @param score
     * @return
     */
    public static String getHTMLFragment(RatingScore score) {
        StringBuilder output = new StringBuilder();
        output.append("<b>Score: ").append(SCORE_FORMAT.format(score.getScore())).append("</b><br>");

        var bases = score.getBases();
        output.append(bases.entrySet().stream()
                                .map(entry -> "  " + Msg.getStringWithFallback(RATING_KEY + entry.getKey(), entry.getKey())
                                                    + ": " + SCORE_FORMAT.format(entry.getValue()))
                                .collect(Collectors.joining("<br>")));

        var modifiers = score.getModifiers();
        if (!modifiers.isEmpty()) {
            output.append("<br>");
        }
        output.append(modifiers.entrySet().stream()
                                .map(entry -> "  " + Msg.getString(RATING_KEY + entry.getKey())
                                                    + ": x" + SCORE_FORMAT.format(entry.getValue()))
                                .collect(Collectors.joining("<br>")));
        return output.toString();
    }
}
