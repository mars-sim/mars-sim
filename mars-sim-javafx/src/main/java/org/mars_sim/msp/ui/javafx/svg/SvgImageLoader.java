package org.mars_sim.msp.ui.javafx.svg;
//
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//
//import org.apache.batik.transcoder.TranscoderException;
//import org.apache.batik.transcoder.TranscoderInput;
//
////import com.sun.javafx.iio.ImageFrame;
////import com.sun.javafx.iio.ImageStorage;
////import com.sun.javafx.iio.common.ImageLoaderImpl;
//
//import de.codecentric.centerdevice.javafxsvg.SvgDescriptor;
//
////@SuppressWarnings("restriction")
//public class SvgImageLoader extends ImageLoaderImpl {
//
//	private static final int BYTES_PER_PIXEL = 4; // RGBA
//
//	private final InputStream input;
//
//	protected SvgImageLoader(InputStream input) {
//		super(SvgDescriptor.getInstance());
//
//		if (input == null) {
//			throw new IllegalArgumentException("input == null!");
//		}
//
//		this.input = input;
//	}
//
//	@Override
//	public ImageFrame load(int imageIndex, int width, int height,
//			boolean preserveAspectRatio, boolean smooth) throws IOException {
//		if (0 != imageIndex) {
//			return null;
//		}
//
//		try {
//			return createImageFrame(width, height);
//		} catch (TranscoderException ex) {
//			throw new IOException(ex);
//		}
//	}
//
//	private ImageFrame createImageFrame(int width, int height)
//			throws TranscoderException {
//		BufferedImage bufferedImage = getTranscodedImage(width, height);
//		ByteBuffer imageData = getImageData(bufferedImage);
//
//		return new ImageFrame(ImageStorage.ImageType.RGBA, imageData,
//				bufferedImage.getWidth(), bufferedImage.getHeight(),
//				getStride(bufferedImage), null, null);
//	}
//
//	private BufferedImage getTranscodedImage(int width, int height)
//			throws TranscoderException {
//		BufferedImageTranscoder trans = new BufferedImageTranscoder(
//				BufferedImage.TYPE_INT_ARGB);
//		trans.setImageSize(width, height);
//		trans.transcode(new TranscoderInput(input), null);
//
//		return trans.getBufferedImage();
//	}
//
//	private int getStride(BufferedImage bufferedImage) {
//		return bufferedImage.getWidth() * BYTES_PER_PIXEL;
//	}
//
//	private ByteBuffer getImageData(BufferedImage bufferedImage) {
//		int[] rgb = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(),
//				bufferedImage.getHeight(), null, 0, bufferedImage.getWidth());
//
//		byte[] imageData = new byte[getStride(bufferedImage)
//				* bufferedImage.getHeight()];
//
//		copyColorToBytes(rgb, imageData);
//		return ByteBuffer.wrap(imageData);
//	}
//
//	private void copyColorToBytes(int[] rgb, byte[] imageData) {
//		if (rgb.length * BYTES_PER_PIXEL != imageData.length) {
//			throw new ArrayIndexOutOfBoundsException();
//		}
//
//		ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
//
//		for (int i = 0; i < rgb.length; i++) {
//			byte[] bytes = byteBuffer.putInt(rgb[i]).array();
//
//			int dataOffset = BYTES_PER_PIXEL * i;
//			imageData[dataOffset] = bytes[1];
//			imageData[dataOffset + 1] = bytes[2];
//			imageData[dataOffset + 2] = bytes[3];
//			imageData[dataOffset + 3] = bytes[0];
//
//			byteBuffer.clear();
//		}
//	}
//
//	@Override
//	public void dispose() {
//		// Nothing to do
//	}
//}
