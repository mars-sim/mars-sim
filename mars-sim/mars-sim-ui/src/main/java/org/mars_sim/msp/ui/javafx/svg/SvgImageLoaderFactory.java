package org.mars_sim.msp.ui.javafx.svg;

import java.io.IOException;
import java.io.InputStream;

import com.sun.javafx.iio.ImageFormatDescription;
import com.sun.javafx.iio.ImageLoader;
import com.sun.javafx.iio.ImageLoaderFactory;
import com.sun.javafx.iio.ImageStorage;

@SuppressWarnings("restriction")
public class SvgImageLoaderFactory implements ImageLoaderFactory {
	private static final ImageLoaderFactory instance = new SvgImageLoaderFactory();

	public static final void install() {
		ImageStorage.addImageLoaderFactory(instance);
	}

	public static final ImageLoaderFactory getInstance() {
		return instance;
	}

	@Override
	public ImageFormatDescription getFormatDescription() {
		return SvgDescriptor.getInstance();
	}

	@Override
	public ImageLoader createImageLoader(InputStream input) throws IOException {
		return new SvgImageLoader(input);
	}

}
