package org.mars_sim.msp.ui.javafx.svg;

//import java.io.IOException;
//import java.io.InputStream;
//
////import com.sun.javafx.iio.ImageFormatDescription;
////import com.sun.javafx.iio.ImageLoader;
////import com.sun.javafx.iio.ImageLoaderFactory;
////import com.sun.javafx.iio.ImageStorage;
//
//import de.codecentric.centerdevice.javafxsvg.SvgDescriptor;
//
//// see https://stackoverflow.com/questions/12436274/svg-image-in-javafx-2-2
//
//// from https://github.com/codecentric/javafxsvg
//// https://blog.codecentric.de/en/2015/03/adding-custom-image-renderer-javafx-8/
//
//@SuppressWarnings("restriction")
//public class SvgImageLoaderFactory implements ImageLoaderFactory {
//	private static final ImageLoaderFactory instance = new SvgImageLoaderFactory();
//
//	public static final void install() {
//		ImageStorage.addImageLoaderFactory(instance);
//	}
//
//	public static final ImageLoaderFactory getInstance() {
//		return instance;
//	}
//
//	@Override
//	public ImageFormatDescription getFormatDescription() {
//		return SvgDescriptor.getInstance();
//	}
//
//	@Override
//	public ImageLoader createImageLoader(InputStream input) throws IOException {
//		return new SvgImageLoader(input);
//	}
//
//}
