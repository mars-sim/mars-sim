/*
 * Mars Simulation Project
 * TileIcon.java
 * @date 2022-10-18
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * An <CODE>Icon</CODE> that repeats its image horizontally and vertically to
 * fill the component area, excluding any border or insets.
 * <P>
 * The class is a drop-in replacement for <CODE>ImageIcon</CODE>, except that
 * the no-argument constructor is not supported.
 * <P>
 * As the size of the Icon is determined by the size of the component in which
 * it is displayed, <CODE>TileIcon</CODE> must only be used in conjunction
 * with a component and layout that does not depend on the size of the
 * component's Icon.
 *
 * @version 1.0 04/19/12
 * @author Darryl
 */
public class TileIcon extends ImageIcon {

  /**
   * The type of the tiling mode
   */
  public enum TileMode {

    /**
     * The default for a newly constructed <CODE>TileIcon</CODE>.
     * The image will be tiled starting at the top left of the component.
     */
    DEFAULT,
    /**
     * The image will be tiled such that the intersection of tile corners
     * will be placed at the center of the tiled area.
     */
    CENTER_CORNER,
    /**
     * The image will be tiled such that the center of a tile will be
     * placed at the center of the tiled area.
     */
    CENTER_CENTER
  };
  private TileMode tileMode = TileMode.DEFAULT;

  /**
   * Creates a <CODE>TileIcon</CODE> from an array of bytes.
   *
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   *
   * @see ImageIcon#ImageIcon(byte[])
   */
  public TileIcon(byte[] imageData) {
    super(imageData);
  }

  /**
   * Creates a <CODE>TileIcon</CODE> from an array of bytes.
   *
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(byte[], String)
   */
  public TileIcon(byte[] imageData, String description) {
    super(imageData, description);
  }

  /**
   * Creates a <CODE>TileIcon</CODE> from the image.
   *
   * @param image the image
   *
   * @see ImageIcon#ImageIcon(Image)
   */
  public TileIcon(Image image) {
    super(image);
  }

  /**
   * Creates a <CODE>TileIcon</CODE> from the image.
   *
   * @param image the image
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(Image, String)
   */
  public TileIcon(Image image, String description) {
    super(image, description);
  }

  /**
   * Creates a <CODE>TileIcon</CODE> from the specified file.
   *
   * @param filename a String specifying a filename or path
   *
   * @see ImageIcon#ImageIcon(String)
   */
  public TileIcon(String filename) {
    super(filename);
  }

  /**
   * Creates a <CODE>TileIcon</CODE> from the specified file.
   *
   * @param filename a String specifying a filename or path
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(String, String)
   */
  public TileIcon(String filename, String description) {
    super(filename, description);
  }

  /**
   * Creates a <CODE>TileIcon</CODE> from the specified URL.
   *
   * @param location the URL for the image
   *
   * @see ImageIcon#ImageIcon(URL)
   */
  public TileIcon(URL location) {
    super(location);
  }

  /**
   * Creates a <CODE>TileIcon</CODE> from the specified URL.
   *
   * @param location the URL for the image
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(URL, String)
   */
  public TileIcon(URL location, String description) {
    super(location, description);
  }

  /**
   * Sets the tiling mode.  If not explicitly set or if set to <CODE>TileMode.DEFAULT</CODE>,
   * the image will be tiled starting from the top left corner of the component.
   * 
   * @param tileMode the tiling style. One of the following constants
   * defined in enum <code>TileMode</code>:
   * <ul>
   *     <li><code>DEFAULT</code></li>
   *     <li><code>CENTER_CENTER</code></li>
   *     <li><code>CENTER_CORNER</code></li>
   * </ul>
   * @see TileMode
   */
  public void setTileMode(TileMode tileMode) {
    this.tileMode = tileMode;
  }

  /**
   * Returns the tiling mode of this <code>TileIcon</code>
   * @return the TileMode of this Icon: <code>DEFAULT</code>,
   * <code>CENTER_CENTER</code> or <code>CENTER_CORNER</code>
   */
  public TileMode getTileMode() {
    return tileMode;
  }

  /**
   * Paints the icon.  The image is tiled over the area of the component to which
   * it is painted.
   * <P>
   * If this icon has no image observer,this method uses the <code>c</code> component
   * as the observer.
   *
   * @param c the component to which the Icon is painted.  This is used as the
   *          observer if this icon has no image observer
   * @param g the graphics context
   * @param x not used.
   * @param y not used.
   *
   * @see ImageIcon#paintIcon(Component, Graphics, int, int)
   */
  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Image image = getImage();
    if (image == null) {
      return;
    }

    Shape clip = g.getClip();
    int iw = image.getWidth(c);
    int ih = image.getHeight(c);

    Insets insets = ((Container) c).getInsets();
    int w = c.getWidth();
    int h = c.getHeight();

    x = insets.left;
    y = insets.top;
    int x1 = c.getWidth() - insets.right;
    int y1 = c.getHeight() - insets.bottom;

    g.setClip(new Rectangle(x, y, x1 - x, y1 - y));

    if (tileMode != TileMode.DEFAULT) {
      int centerX = (x + x1) / 2;
      int centerY = (y + y1) / 2;

      switch (tileMode) {
        case CENTER_CORNER:
          x += (centerX - x) % iw - iw;
          y += (centerY - y) % ih - ih;
          break;
        case CENTER_CENTER:
          x += (centerX - iw / 2 - x) % iw - iw;
          y += (centerY - ih / 2 - y) % ih - ih;
          break;
      }
    }
    for (int ix = x; ix <= x1; ix += iw) {
      for (int iy = y; iy < y1; iy += ih) {
        super.paintIcon(c, g, ix, iy);
      }
    }
    g.setClip(clip);
  }

  /**
   * Overridden to return 0.  The size of this Icon is determined by
   * the size of the component.
   * 
   * @return 0
   */
  @Override
  public int getIconWidth() {
    return 0;
  }

  /**
   * Overridden to return 0.  The size of this Icon is determined by
   * the size of the component.
   *
   * @return 0
   */
  @Override
  public int getIconHeight() {
    return 0;
  }
}
