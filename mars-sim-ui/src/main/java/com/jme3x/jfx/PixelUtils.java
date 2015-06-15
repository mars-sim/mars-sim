/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3x.jfx;

/**
 * Convertes image formats between jfx and jme
 * 
 * @author Heist
 */
public abstract class PixelUtils {

	static int mergeArgb(final int bg, final int src) {

		final int sa = src >>> 24;

		if (sa == 0) {
			return bg;
		}

		final int ba = bg >>> 24;

		final int rb = (src & 0x00ff00ff) * sa + (bg & 0x00ff00ff) * (0xff - sa) & 0xff00ff00;
		final int g = (src & 0x0000ff00) * sa + (bg & 0x0000ff00) * (0xff - sa) & 0x00ff0000;
		final int a = sa + (ba * (0xff - sa) >> 8);

		return a << 24 | (rb | g) >>> 8;
	}
	
	// this is platform specific... assumes little-endian
	static int mergeBgraPre(final int bg, final int src) {

        final int sa = src >>> 24;

        if (sa == 0) {
            return bg;
        }

        final int ba = bg >>> 24;

        final int rb = (src & 0x00ff00ff) * 0xff + (bg & 0x00ff00ff) * (0xff - sa) & 0xff00ff00;
        final int g = (src & 0x0000ff00) * 0xff + (bg & 0x0000ff00) * (0xff - sa) & 0x00ff0000;
        final int a = sa + (ba * (0xff - sa) >> 8);

        return a << 24 | (rb | g) >>> 8;
    }
	
	

	static int mergeBgra(final int bg, final int src) {

		final int sa = src & 0xff;

		if (sa == 0) {
			return bg;
		}

		final int ba = bg & 0xff;

		final int a = sa + (ba * (0xff - sa) >> 8);

		final int b = ((src & 0xff000000) >> 24) * sa + ((bg & 0xff000000) >> 24) * ba >> 8;
		final int g = ((src & 0xff0000) >> 16) * sa + ((bg & 0xff0000) >> 16) * ba >> 8;
		final int r = ((src & 0xff00) >> 8) * sa + ((bg & 0xff00) >> 8) * ba >> 8;

		return b << 24 | g << 16 | r << 8 | a;
		// return 0xffff0000;
	}
}
