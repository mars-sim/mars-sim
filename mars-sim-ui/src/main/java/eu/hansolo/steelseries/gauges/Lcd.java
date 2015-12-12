/*
 * Copyright (c) 2012, Gerrit Grunwald
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * The names of its contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.hansolo.steelseries.gauges;

import eu.hansolo.steelseries.tools.LcdColor;
import eu.hansolo.steelseries.tools.NumberSystem;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;


/**
 *
 * @author hansolo
 */
public interface Lcd {

    public boolean isValueCoupled();

    public void setValueCoupled(final boolean VALUE_COUPLED);

    public double getLcdValue();

    public void setLcdValue(final double VALUE);

    public void setLcdValueAnimated(final double VALUE);

    public double getLcdThreshold();

    public void setLcdThreshold(final double LCD_THRESHOLD);

    public boolean isLcdThresholdVisible();

    public void setLcdThresholdVisible(final boolean LCD_THRESHOLD_VISIBLE);

    public boolean isLcdThresholdBehaviourInverted();

    public void setLcdThresholdBehaviourInverted(final boolean LCD_THRESHOLD_BEHAVIOUR_INVERTED);

    public boolean isLcdBlinking();

    public void setLcdBlinking(final boolean LCD_BLINKING);

    public int getLcdDecimals();

    public void setLcdDecimals(final int DECIMALS);

    public String getLcdUnitString();

    public void setLcdUnitString(final String UNIT);

    public boolean isLcdUnitStringVisible();

    public void setLcdUnitStringVisible(final boolean UNIT_STRING_VISIBLE);

    public boolean isCustomLcdUnitFontEnabled();

    public void setCustomLcdUnitFontEnabled(final boolean USE_CUSTOM_LCD_UNIT_FONT);

    public Font getCustomLcdUnitFont();

    public void setCustomLcdUnitFont(final Font CUSTOM_LCD_UNIT_FONT);

    public String getLcdInfoString();

    public void setLcdInfoString(final String INFO);

    public Font getLcdInfoFont();

    public void setLcdInfoFont(final Font LCD_INFO_FONT);

    public boolean isDigitalFont();

    public void setDigitalFont(final boolean DIGITAL_FONT);

    public LcdColor getLcdColor();

    public void setLcdColor(final LcdColor COLOR);

    public Paint getCustomLcdBackground();

    public void setCustomLcdBackground(final Paint CUSTOM_LCD_BACKGROUND);

    public Paint createCustomLcdBackgroundPaint(final Color[] LCD_COLORS);

    public boolean isLcdBackgroundVisible();

    public void setLcdBackgroundVisible(final boolean LCD_BACKGROUND_VISIBLE);

    public Color getCustomLcdForeground();

    public void setCustomLcdForeground(final Color CUSTOM_LCD_FOREGROUND);

    public String formatLcdValue(final double VALUE);

    public boolean isLcdScientificFormat();

    public void setLcdScientificFormat(final boolean LCD_SCIENTIFIC_FORMAT);

    public Font getLcdValueFont();

    public void setLcdValueFont(final Font LCD_VALUE_FONT);

    public Font getLcdUnitFont();

    public void setLcdUnitFont(final Font LCD_UNIT_FONT);

    public NumberSystem getLcdNumberSystem();

    public void setLcdNumberSystem(final NumberSystem NUMBER_SYSTEM);

    public Rectangle getLcdBounds();
}
