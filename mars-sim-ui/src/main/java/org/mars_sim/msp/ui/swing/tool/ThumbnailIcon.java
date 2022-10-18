/*
 * Mars Simulation Project
 * ThumbnailIcon.java
 * @date 2022-10-18
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * An <CODE>Icon</CODE> that maintains a defined size independent of the size of the
 * image it contains.  If needed, the size of its image is reduced to fit; otherwise
 * the image is displayed at its natural size, padded and centered horizontally and/or
 * vertically.  The size of an individual <code>ThumbnailIcon</code>, or the default
 * size of all <CODE>ThumbnailIcon</CODE>s, can be changed by the static method provided.
 * <P>
 * Swing components that use icons are not notified when the icon's size is changed.
 * In case the size of a <CODE>ThumbnailIcon</CODE> displayed in an already realized
 * component is changed, either by setting its size or the default size, it is the
 * caller's responsibility to revalidate the component hierarchy affected by the change.
 * <P>
 * The class is a drop-in replacement for <CODE>ImageIcon</CODE>, except that
 * the no-argument constructor is not supported.
 * <P>
 * <CODE>ThumbnailIcon</CODE>, as its name implies, is useful for displaying
 * multiple images of different sizes, reduced or padded as necessary to fit the
 * same dimensions.
 * <P>
 * If not set, the default size of <CODE>ThumbnailIcon</CODE> is 160 X 120 pixels.
 *
 * @version 1.0 04/12/12
 * @author Darryl
 */
public class ThumbnailIcon extends ShrinkIcon {

  /**
   * The constant for setting the width and/or height to use the default
   * for the class.
   */
  public static final int DEFAULT = -1;
  /**
   * The constant for setting the width, height, default width or default height
   * to be computed from the other dimension in a way that maintains the aspect
   * ratio of the contained image.
   * <P>
   * If both the width and height evaluate to <CODE>COMPUTED</CODE>, the image
   * will be displayed at its natural size, essentially mimicking the behavior
   * of <CODE>ImageIcon</CODE>.
   */
  public static final int COMPUTED = 0;
  private static int defaultWidth = 160;
  private static int defaultHeight = 120;
  private int thumbWidth = DEFAULT;
  private int thumbHeight = DEFAULT;

  /**
   * Creates a <CODE>ThumbnailIcon</CODE> from an array of bytes.
   *
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   *
   * @see ImageIcon#ImageIcon(byte[])
   */
  public ThumbnailIcon(byte[] imageData) {
    super(imageData);
  }

  /**
   * Creates a <CODE>ThumbnailIcon</CODE> from an array of bytes.
   *
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(byte[], java.lang.String)
   */
  public ThumbnailIcon(byte[] imageData, String description) {
    super(imageData, description);
  }

  /**
   * Creates a <CODE>ThumbnailIcon</CODE> from the image.
   *
   * @param image the image
   *
   * @see ImageIcon#ImageIcon(java.awt.Image)
   */
  public ThumbnailIcon(Image image) {
    super(image);
  }

  /**
   * Creates a <CODE>ThumbnailIcon</CODE> from the image.
   *
   * @param image the image
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(java.awt.Image, java.lang.String)
   */
  public ThumbnailIcon(Image image, String description) {
    super(image, description);
  }

  /**
   * Creates a <CODE>ThumbnailIcon</CODE> from the specified file.
   *
   * @param filename a String specifying a filename or path
   *
   * @see ImageIcon#ImageIcon(java.lang.String)
   */
  public ThumbnailIcon(String filename) {
    super(filename);
  }

  /**
   * Creates a <CODE>ThumbnailIcon</CODE> from the specified file.
   *
   * @param filename a String specifying a filename or path
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(java.lang.String, java.lang.String)
   */
  public ThumbnailIcon(String filename, String description) {
    super(filename, description);
  }

  /**
   * Creates a <CODE>ThumbnailIcon</CODE> from the specified URL.
   *
   * @param location the URL for the image
   *
   * @see ImageIcon#ImageIcon(java.net.URL)
   */
  public ThumbnailIcon(URL location) {
    super(location);
  }

  /**
   * Creates a <CODE>ThumbnailIcon</CODE> from the specified URL.
   *
   * @param location the URL for the image
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(java.net.URL, java.lang.String)
   */
  public ThumbnailIcon(URL location, String description) {
    super(location, description);
  }

  /**
   * Sets the width of an individual <CODE>ThumbnailIcon</CODE>.
   * <P>
   * Set the width to <CODE>DEFAULT</CODE> to use the default width for the class,
   * or set it to <CODE>COMPUTED</CODE> for the width to be computed form the height,
   * maintaining the aspect ratio of the contained image.
   * <P>
   * If both the width and height evaluate to <CODE>COMPUTED</CODE>, the image
   * will be displayed at its natural size, essentially mimicking the behavior
   * of <CODE>ImageIcon</CODE>.
   * <P>
   * Setting a value less than <CODE>DEFAULT</CODE> is not permitted.
   *
   * @param width  The width to set
   */
  public void setThumbWidth(int width) {
    if (width < DEFAULT) {
      throw new IllegalArgumentException("Width cannot be less than ThumbnailIcon.DEFAULT");
    }
    thumbWidth = width;
  }

  /**
   * Returns the width that has  been set, or <CODE>DEFAULT</CODE>.  Note that this
   * is not the same value as that returned by <CODE>getIconWidth()</CODE>.
   * 
   * @return the set width, or <CODE>DEFAULT</CODE> if none has been set.
   */
  public int getThumbWidth() {
    return thumbWidth;
  }

  /**
   * Sets the height of an individual <CODE>ThumbnailIcon</CODE>.
   * <P>
   * Set the height to <CODE>DEFAULT</CODE> to use the default height for the class,
   * or set it to <CODE>COMPUTED</CODE> for the height to be computed form the width,
   * maintaining the aspect ratio of the contained image.
   * <P>
   * If both the width and height evaluate to <CODE>COMPUTED</CODE>, the image
   * will be displayed at its natural size, essentially mimicking the behavior
   * of <CODE>ImageIcon</CODE>.
   * <P>
   * Setting a value less than <CODE>DEFAULT</CODE> is not permitted.
   *
   * @param height  The height to set
   */
  public void setThumbHeight(int height) {
    if (height < DEFAULT) {
      throw new IllegalArgumentException("Height cannot be less than ThumbnailIcon.DEFAULT");
    }
    thumbHeight = height;
  }

  /**
   * Returns the height that has  been set, or <CODE>DEFAULT</CODE>.  Note that this
   * is not the same value as that returned by <CODE>getIconHeight()</CODE>.
   *
   * @return the set height, or <CODE>DEFAULT</CODE> if none has been set.
   */
  public int getThumbHeight() {
    return thumbHeight;
  }

  /**
   * Sets the default size of all <CODE>ThumbnailIcon</CODE>s.
   * <P>
   * If either the width or height is zero it will be computed from the other
   * dimension, according to the aspect ratio of each <CODE>ThumbnaiIcon</CODE>'s
   * contained image.  Setting the default width to zero may be useful for a horizontal
   * layout of components that display a <CODE>ThumbnailIcon</CODE>; correspondingly,
   * a default height of zero can be useful in a vertical layout.
   * <P>
   * If both width and height are zero, a <CODE>ThumbnailIcon</CODE> that respects the
   * default width and height will be sized to its contained image, essentially
   * mimicking the behavior of <CODE>ImageIcon</CODE>. 
   * 
   * @param width The default width to set.
   * @param height  The default height to set.
   */
  public static void setDefaultSize(int width, int height) {
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Default width and/or height cannot be negative");
    }
    defaultWidth = width;
    defaultHeight = height;
  }

  /**
   * Gets the default width of all <CODE>ThumbnailIcon</CODE>s.  A return value
   * of zero indicates that the default width will be computed from the default
   * height so as to maintain the aspect ratio of the contained image.
   * 
   * @return The default width for all <CODE>ThumbnailIcon</CODE>s
   */
  public static int getDefaultWidth() {
    return defaultWidth;
  }

  /**
   * Gets the default height of all <CODE>ThumbnailIcon</CODE>s.  A return value
   * of zero indicates that the default height will be computed from the default
   * width so as to maintain the aspect ratio of the contained image.
   *
   * @return The default width for all <CODE>ThumbnailIcon</CODE>s
   */
  public static int getDefaultHeight() {
    return defaultHeight;
  }

  /**
   * Gets the width of the icon.
   *
   * @return The width in pixels of this icon
   */
  @Override
  public int getIconWidth() {
    if (thumbWidth > 0) {
      return thumbWidth;
    }

    Image image = getImage();
    int w = image.getWidth(component);
    int h = image.getHeight(component);

    if (thumbWidth < 0) {
      if (defaultWidth > 0) {
        return defaultWidth;
      }
      if (defaultHeight > 0) {
        return (defaultHeight * w) / h;
      }
      if (thumbHeight <= 0) {
        return w;
      }
    }
    return (getIconHeight() * w) / h;
  }

  /**
   * Gets the height of the icon.
   *
   * @return  The height in pixels of this icon
   */
  @Override
  public int getIconHeight() {
    if (thumbHeight > 0) {
      return thumbHeight;
    }

    Image image = getImage();
    int w = image.getWidth(component);
    int h = image.getHeight(component);

    if (thumbHeight < 0) {
      if (defaultHeight > 0) {
        return defaultHeight;
      }
      if (defaultWidth > 0) {
        return (defaultWidth * h) / w;
      }
      if (thumbWidth <= 0) {
        return h;
      }
    }
    return (getIconWidth() * h) / w;
  }

  /**
   * Convenience method to obtain the icon's image at the size at which it is displayed.
   * Note that a thumbnail obtained from an animated image will not itself be animated.
   * 
   * @return The thumbnail image.
   */
  public BufferedImage getThumbnail() {
    Image image = getImage();
    int w = image.getWidth(component);
    int h = image.getHeight(component);

    int tw = getIconWidth();
    int th = getIconHeight();
    if (w * th < h * tw) {
      tw = (th * w) / h;
    } else {
      th = (tw * h) / w;
    }
    
    tw = tw > w ? w : tw;
    th = th > h ? h : th;
    BufferedImage retVal = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
    image.getGraphics().drawImage(getImage(), 0, 0, tw, th, component);
    return retVal;
  }
}
