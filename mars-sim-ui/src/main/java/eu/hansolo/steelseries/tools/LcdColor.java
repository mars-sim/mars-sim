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
package eu.hansolo.steelseries.tools;

import java.awt.Color;


/**
 * Color definitions for different LCD designs.
 * Some of the colors are taken from images of
 * real lcd's which really leads to a more realistic
 * look of the lcd display.
 * @author hansolo
 */
public enum LcdColor {

    BEIGE_LCD(
        new Color(200, 200, 177),
        new Color(241, 237, 207),
        new Color(234, 230, 194),
        new Color(225, 220, 183),
        new Color(237, 232, 191),
        new Color(0, 0, 0)),
    BLUE_LCD(
        new Color(255, 255, 255),
        new Color(231, 246, 255),
        new Color(170, 224, 255),
        new Color(136, 212, 255),
        new Color(192, 232, 255),
        new Color(0x124564)),
    ORANGE_LCD(
        new Color(255, 255, 255),
        new Color(255, 245, 225),
        new Color(255, 217, 147),
        new Color(255, 201, 104),
        new Color(255, 227, 173),
        new Color(0x503700)),
    RED_LCD(
        new Color(255, 255, 255),
        new Color(255, 225, 225),
        new Color(253, 152, 152),
        new Color(252, 114, 115),
        new Color(254, 178, 178),
        new Color(0x4F0C0E)),
    YELLOW_LCD(
        new Color(255, 255, 255),
        new Color(245, 255, 186),
        new Color(210, 255, 0),
        new Color(158, 205, 0),
        new Color(210, 255, 0),
        new Color(0x405300)),
    WHITE_LCD(
        new Color(255, 255, 255),
        new Color(255, 255, 255),
        new Color(241, 246, 242),
        new Color(229, 239, 244),
        new Color(255, 255, 255),
        Color.BLACK),
    GRAY_LCD(
        new Color(65, 65, 65),
        new Color(117, 117, 117),
        new Color(87, 87, 87),
        new Color(65, 65, 65),
        new Color(81, 81, 81),
        Color.WHITE),
    BLACK_LCD(
        new Color(65, 65, 65),
        new Color(102, 102, 102),
        new Color(51, 51, 51),
        new Color(0, 0, 0),
        new Color(51, 51, 51),
        new Color(0xCCCCCC)),
    GREEN_LCD(
        new Color(33, 67, 67),
        new Color(33, 67, 67),
        new Color(29, 58, 58),
        new Color(28, 57, 57),
        new Color(23, 46, 46),
        new Color(0, 185, 165)),
    BLUE2_LCD(
        new Color(0, 68, 103),
        new Color(8, 109, 165),
        new Color(0, 72, 117),
        new Color(0, 72, 117),
        new Color(0, 68, 103),
        new Color(111, 182, 228)),
    BLUEBLACK_LCD(
        new Color(22, 125, 212),
        new Color(3, 162, 254),
        new Color(3, 162, 254),
        new Color(3, 162, 254),
        new Color(11, 172, 244),
        new Color(0, 0, 0)),
    BLUEDARKBLUE_LCD(
        new Color(18, 33, 88),
        new Color(18, 33, 88),
        new Color(19, 30, 90),
        new Color(17, 31, 94),
        new Color(21, 25, 90),
        new Color(23, 99, 221)),
    BLUELIGHTBLUE_LCD(
        new Color(88, 107, 132),
        new Color(53, 74, 104),
        new Color(27, 37, 65),
        new Color(5, 12, 40),
        new Color(32, 47, 79),
        new Color(71, 178, 254)),
    BLUEGRAY_LCD(
        new Color(135, 174, 255),
        new Color(101, 159, 255),
        new Color(44, 93, 255),
        new Color(27, 65, 254),
        new Color(12, 50, 255),
        new Color(0xB2B4ED)),
    STANDARD_LCD(
        new Color(131, 133, 119),
        new Color(176, 183, 167),
        new Color(165, 174, 153),
        new Color(166, 175, 156),
        new Color(175, 184, 165),
        new Color(35, 42, 52)),
    STANDARD_GREEN_LCD(
        new Color(255, 255, 255),
        new Color(219, 230, 220),
        new Color(179, 194, 178),
        new Color(153, 176, 151),
        new Color(114, 138, 109),
        new Color(0x080C06)),
    BLUEBLUE_LCD(
        new Color(100, 168, 253),
        new Color(100, 168, 253),
        new Color(95, 160, 250),
        new Color(80, 144, 252),
        new Color(74, 134, 255),
        new Color(0x002CBB)),
    REDDARKRED_LCD(
        new Color(72, 36, 50),
        new Color(185, 111, 110),
        new Color(148, 66, 72),
        new Color(83, 19, 20),
        new Color(7, 6, 14),
        new Color(0xFE8B92)),
    DARKBLUE_LCD(
        new Color(14, 24, 31),
        new Color(46, 105, 144),
        new Color(19, 64, 96),
        new Color(6, 20, 29),
        new Color(8, 9, 10),
        new Color(0x3DB3FF)),
    LILA_LCD(
        new Color(175, 164, 255),
        new Color(188, 168, 253),
        new Color(176, 159, 255),
        new Color(174, 147, 252),
        new Color(168, 136, 233),
        new Color(0x076148)),
    BLACKRED_LCD(
        new Color(8, 12, 11),
        new Color(10, 11, 13),
        new Color(11, 10, 15),
        new Color(7, 13, 9),
        new Color(9, 13, 14),
        new Color(0xB50026)),
    DARKGREEN_LCD(
        new Color(25, 85, 0),
        new Color(47, 154, 0),
        new Color(30, 101, 0),
        new Color(30, 101, 0),
        new Color(25, 85, 0),
        new Color(0x233123)),
    AMBER_LCD(
        new Color(182, 71, 0),
        new Color(236, 155, 25),
        new Color(212, 93, 5),
        new Color(212, 93, 5),
        new Color(182, 71, 0),
        new Color(0x593A0A)),
    LIGHTBLUE_LCD(
        new Color(125, 146, 184),
        new Color(197, 212, 231),
        new Color(138, 155, 194),
        new Color(138, 155, 194),
        new Color(125, 146, 184),
        new Color(0x090051)),
    SECTIONS_LCD(
        new Color(0xb2b2b2),
        new Color(0xffffff),
        new Color(0xc4c4c4),
        new Color(0xc4c4c4),
        new Color(0xb2b2b2),
        new Color(0x000000)),
    CUSTOM(
    new Color(0xb2b2b2),
        new Color(0xffffff),
        new Color(0xc4c4c4),
        new Color(0xc4c4c4),
        new Color(0xb2b2b2),
        new Color(0x000000));
    public final Color GRADIENT_START_COLOR;
    public final Color GRADIENT_FRACTION1_COLOR;
    public final Color GRADIENT_FRACTION2_COLOR;
    public final Color GRADIENT_FRACTION3_COLOR;
    public final Color GRADIENT_STOP_COLOR;
    public final Color TEXT_COLOR;

    LcdColor(final Color GRADIENT_START_COLOR, final Color GRADIENT_FRACTION1_COLOR,
             final Color GRADIENT_FRACTION2_COLOR, final Color GRADIENT_FRACTION3_COLOR,
             final Color GRADIENT_STOP_COLOR, final Color TEXT_COLOR) {
        this.GRADIENT_START_COLOR = GRADIENT_START_COLOR;
        this.GRADIENT_FRACTION1_COLOR = GRADIENT_FRACTION1_COLOR;
        this.GRADIENT_FRACTION2_COLOR = GRADIENT_FRACTION2_COLOR;
        this.GRADIENT_FRACTION3_COLOR = GRADIENT_FRACTION3_COLOR;
        this.GRADIENT_STOP_COLOR = GRADIENT_STOP_COLOR;
        this.TEXT_COLOR = TEXT_COLOR;
    }
}
