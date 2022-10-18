/*
 * Mars Simulation Project
 * ShrinkIcon.java
 * @date 2022-10-18
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.ImageObserver;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * An <CODE>Icon</CODE> that when necessary reduces the size of its image to
 * fit within the component area, excluding any border or insets, optionally
 * maintaining the image's aspect ratio by padding and centering the scaled
 * image horizontally or vertically.  When the component is larger than the image,
 * the image will be drawn at its natural size and padded and centered
 * horizontally and/or vertically.
 * <P>
 * The class is a drop-in replacement for <CODE>ImageIcon</CODE>, except that
 * the no-argument constructor is not supported.
 * <P>
 * As the size of this icon is determined by the size of the component in
 * which it is displayed, <CODE>ShrinkIcon</CODE> must only be used in
 * conjunction with a component and layout that does not depend on the size
 * of the component's Icon.
 *
 * @version 1.0 04/05/12
 * @author Darryl
 */
public class ShrinkIcon extends StretchIcon {

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from an array of bytes.
   *
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   *
   * @see ImageIcon#ImageIcon(byte[])
   */
  public ShrinkIcon(byte[] imageData) {
    super(imageData);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from an array of bytes with the specified behavior.
   *
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fit the
   *        component.
   *
   * @see ImageIcon#ImageIcon(byte[])
   */
  public ShrinkIcon(byte[] imageData, boolean proportionate) {
    super(imageData, proportionate);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from an array of bytes.
   *
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(byte[], java.lang.String)
   */
  public ShrinkIcon(byte[] imageData, String description) {
    super(imageData, description);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from an array of bytes with the specified behavior.
   *
   * @see ImageIcon#ImageIcon(byte[])
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   * @param  description a brief textual description of the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fit the
   *        component.
   *
   * @see ImageIcon#ImageIcon(byte[], java.lang.String)
   */
  public ShrinkIcon(byte[] imageData, String description, boolean proportionate) {
    super(imageData, description, proportionate);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the image.
   *
   * @param image the image
   *
   * @see ImageIcon#ImageIcon(java.awt.Image)
   */
  public ShrinkIcon(Image image) {
    super(image);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the image with the specified behavior.
   *
   * @param image the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fit the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.awt.Image)
   */
  public ShrinkIcon(Image image, boolean proportionate) {
    super(image, proportionate);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the image.
   *
   * @param image the image
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(java.awt.Image, java.lang.String)
   */
  public ShrinkIcon(Image image, String description) {
    super(image, description);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the image with the specified behavior.
   *
   * @param image the image
   * @param  description a brief textual description of the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fit the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.awt.Image, java.lang.String)
   */
  public ShrinkIcon(Image image, String description, boolean proportionate) {
    super(image, description, proportionate);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the specified file.
   *
   * @param filename a String specifying a filename or path
   *
   * @see ImageIcon#ImageIcon(java.lang.String)
   */
  public ShrinkIcon(String filename) {
    super(filename);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the specified file with the specified behavior.
   *
   * @param filename a String specifying a filename or path
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fit the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.lang.String)
   */
  public ShrinkIcon(String filename, boolean proportionate) {
    super(filename, proportionate);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the specified file.
   *
   * @param filename a String specifying a filename or path
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(java.lang.String, java.lang.String)
   */
  public ShrinkIcon(String filename, String description) {
    super(filename, description);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the specified file with the specified behavior.
   *
   * @param filename a String specifying a filename or path
   * @param  description a brief textual description of the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fit the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.awt.Image, java.lang.String)
   */
  public ShrinkIcon(String filename, String description, boolean proportionate) {
    super(filename, description, proportionate);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the specified URL.
   *
   * @param location the URL for the image
   *
   * @see ImageIcon#ImageIcon(java.net.URL)
   */
  public ShrinkIcon(URL location) {
    super(location);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the specified URL with the specified behavior.
   *
   * @param location the URL for the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fit the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.net.URL)
   */
  public ShrinkIcon(URL location, boolean proportionate) {
    super(location, proportionate);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the specified URL.
   *
   * @param location the URL for the image
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(java.net.URL, java.lang.String)
   */
  public ShrinkIcon(URL location, String description) {
    super(location, description);
  }

  /**
   * Creates a <CODE>ShrinkIcon</CODE> from the specified URL with the specified behavior.
   *
   * @param location the URL for the image
   * @param  description a brief textual description of the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fit the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.net.URL, java.lang.String)
   */
  public ShrinkIcon(URL location, String description, boolean proportionate) {
    super(location, description, proportionate);
  }

  /**
   * Paints the icon.  If necessary, the image is reduced to fit the component to
   * which it is painted, otherwise the image is centered horizontally and/or
   * vertically and painted with a width and height not exceeding its natural size.
   * <P>
   * If the proportion has not been specified, or has been specified as
   * <code>true</code>, the aspect ratio of the image will be preserved when
   * reducing the size, by padding and centering the image horizontally or
   * vertically.
   * <P>
   * If the proportion has been specified as <code>false</code> the image may be
   * reduced on one or both axes, each independent of the other, to fit the component.
   * <P>
   * If this icon has no image observer,this method uses the <code>c</code> component
   * as the observer.
   *
   * @param c the component to which the Icon is painted.  This is used as the
   *          observer if this icon has no image observer
   * @param g the graphics context
   * @param x not used
   * @param y not used
   *
   * @see StretchIcon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
   */
  @Override
  public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
    Image image = getImage();
    if (image == null) {
      return;
    }
    Insets insets = ((Container) c).getInsets();
    x = insets.left;
    y = insets.top;

    int w = c.getWidth() - x - insets.right;
    int h = c.getHeight() - y - insets.bottom;

    int iw = image.getWidth(c);
    int ih = image.getHeight(c);

    if (proportionate && (w < iw || h < ih)) {
      super.paintIcon(c, g, x, y);
    } else {
      if (w > iw) {
        x += (w - iw) / 2;
      }
      if (h > ih) {
        y += (h - ih) / 2;
      }

      ImageObserver io = getImageObserver();
      g.drawImage(image, x, y, io == null ? c : io);
    }
  }
}
