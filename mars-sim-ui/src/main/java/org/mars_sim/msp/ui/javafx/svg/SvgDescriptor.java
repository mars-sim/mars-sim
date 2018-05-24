package org.mars_sim.msp.ui.javafx.svg;
//
//import com.sun.javafx.iio.common.ImageDescriptor;
//
////@SuppressWarnings("restriction")
//public class SvgDescriptor extends ImageDescriptor {
//
//	private static final String formatName = "SVG";
//
//	private static final String[] extensions = { "svg" };
//
//	private static final Signature[] signatures = {
//			new Signature("<svg".getBytes()), new Signature("<?xml".getBytes()) };
//
//	private static ImageDescriptor theInstance = null;
//
//	private SvgDescriptor() {
//		super(formatName, extensions, signatures);
//	}
//
//	public static synchronized ImageDescriptor getInstance() {
//		if (theInstance == null) {
//			theInstance = new SvgDescriptor();
//		}
//		return theInstance;
//	}
//}
