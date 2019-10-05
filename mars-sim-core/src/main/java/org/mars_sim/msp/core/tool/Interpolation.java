package org.mars_sim.msp.core.tool;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.TrivariateGridInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class Interpolation {

//	x and y for AkimaSplineInterpolator
	
    public static void main(String[] args) {
    	
//    	testTri();
//    	test0();
    	
    	findHSB();
    }
    
//    public void test3() {
//        double[] x = { 0, 50, 100 };
//        double[] y = { 0, 50, 200 };
//        double[] z = { 0, 5, 10 };
//        
//    	TricubicSplineInterpolator tsi = new TricubicSplineInterpolator();
//    	PolynomialSplineFunction f = tsi.interpolate(x, y, z);
//
//        System.out.println("Piecewise functions:");
//        Arrays.stream(f.getPolynomials()).forEach(System.out::println);
//
//        double value = f.value(70);
//        System.out.println("y for xi = 70: " + value);
//    	
//    	
//    }
    
    public double[] linearInterp(double[] x, double[] y, double[] xi) {
    	   LinearInterpolator li = new LinearInterpolator(); // or other interpolator
    	   PolynomialSplineFunction psf = li.interpolate(x, y);

    	   double[] yi = new double[xi.length];
    	   for (int i = 0; i < xi.length; i++) {
    	       yi[i] = psf.value(xi[i]);
    	   }
    	   return yi;
    	}
    
    public static void findHSB() {
      int[] b = new int[] {103, 152, 199, 211, 223, 235, 170,  38,  38,
		  38,  63,  67,  69,  72,  91, 105, 116, 128, 141,
		 155, 170, 185, 201, 218, 236, 239, 243, 249, 255, 255, 254};

      int[] g = new int[] {38,  38,  38,  38,  62, 134, 230, 225, 239,
		254, 188, 147, 111,  81, 107, 119, 130, 141, 153, 
		166, 179, 193, 208, 223, 238, 241, 244, 249, 255, 253, 251};

      int[] r = new int[] {104, 129, 130, 76, 38, 38, 38, 66, 148,
		243, 254, 240, 226, 212, 190, 168, 177, 186, 194, 
		203, 211, 221, 229, 238, 246, 248, 249, 252, 255, 218, 181};

      int[] e = new int[] {-9000, -8000, -7000, -6000, -5000, -4000, -3000, -2000, -1000, 
			   0,  1000,  2000,  3000,  4000,  5000,  6000,  7000,  8000, 9000,
			10000, 11000, 12000, 13000, 14000, 15000, 16000, 17000, 18000, 19000, 20000, 21000};
      
//      List<Double> eList = Arrays.asList(e);
 
      int size = e.length;
      
      float[] hue = new float[size];
      float[] saturation = new float[size];
      float[] brightness = new float[size];
      
	  String s0 = String.format("%6s %4s %4s %4s   %6s %6s %6s", 
			  "Elev", "R", "G", "B", "H", "S", "B");
      System.out.println(s0);
      System.out.println(" -------------------------------------------- ");
      for (int i=0; i < size; i++) {	  
    	  float[] hsb = Color.RGBtoHSB(r[i], g[i], b[i], null);
    	  hue[i] = hsb[0];
    	  saturation[i] = hsb[1];
    	  brightness[i] = hsb[2];
    	  String s = String.format("%6d  %4d %4d %4d   %7.4f %7.4f %7.4f", 
    			  e[i], r[i], g[i], b[i], hue[i], saturation[i], brightness[i]);
          System.out.println(s);
      }
      
//      Arrays.stream(hue.toString()).map((x) -> Float.parseFloat(x)).forEach(System.out::println);
      

    }
    
    public static void test0() {
//    	 double[] x = { 0, 50, 100 };
//       double[] y = { 0, 50, 200 };
//        double[] x = new double[] {103, 152, 199, 211, 223, 235, 170,  38,  38,
//        							  38,  63,  67,  69,  72,  91, 105, 116, 128, 141,
//        							 155, 170, 185, 201, 218, 236, 239, 243, 249, 255, 255, 254};
        
//        double[] x = new double[] {38,  38,  38,  38,  62, 134, 230, 225, 239,
//        							254, 188, 147, 111,  81, 107, 119, 130, 141, 153, 
//        							166, 179, 193, 208, 223, 238, 241, 244, 249, 255, 253, 251};
        
    	double[] x = new double[] {104, 129, 130, 76, 38, 38, 38, 66, 148,
        							243, 254, 240, 226, 212, 190, 168, 177, 186, 194, 
        							203, 211, 221, 229, 238, 246, 248, 249, 252, 255, 218, 181};
        
        double[] y = new double[] {-9000, -8000, -7000, -6000, -5000, -4000, -3000, -2000, -1000, 
    		   						   0,  1000,  2000,  3000,  4000,  5000,  6000,  7000,  8000, 9000,
    		   						10000, 11000, 12000, 13000, 14000, 15000, 16000, 17000, 18000, 19000, 20000, 21000};
       
//       LinearInterpolator interp = new LinearInterpolator();
       SplineInterpolator si = new SplineInterpolator();
       PolynomialSplineFunction f = si.interpolate(x, y);

//       System.out.println("Piecewise functions:");
       Arrays.stream(f.getPolynomials()).forEach(System.out::println);

       double value = f.value(250);
       System.out.println("Elevation when x = 250: " + value);
       
    }
    
    public static void testTri() {
//    	see http://commons.apache.org/proper/commons-math/userguide/analysis.html
        double[] xval = new double[] {203, 211, 221, 229, 238, 246, 248, 249, 252, 255, 218, 181};
        double[] yval = new double[] {166, 179, 193, 208, 223, 238, 241, 244, 249, 255, 253, 251};
        double[] zval = new double[] {155, 170, 185, 201, 218, 236, 239, 243, 249, 255, 255, 254};
 
        double[][][] fval = new double[xval.length][yval.length][zval.length];

        TrivariateGridInterpolator interpolator = new TricubicSplineInterpolator();
//        TricubicSplineInterpolator
        
        TrivariateFunction p = interpolator.interpolate(xval, yval, zval, fval);

//        double[] wxval = new double[] {3, 2, 5, 6.5};
//        try {
//            p = interpolator.interpolate(wxval, yval, zval, fval);
////            Assert.fail("an exception should have been thrown");
//        } catch (MathIllegalArgumentException e) {
//            // Expected
//        }

//	      System.out.println("Piecewise functions:");
//	      Arrays.stream(p.getPolynomials()).forEach(System.out::println);
	      
	      double value = p.value(208, 185, 163);
	      System.out.println(value);
    
    
//        double[] wyval = new double[] {-4, -3, -1, -1};
//        try {
//            p = interpolator.interpolate(xval, wyval, zval, fval);
////            Assert.fail("an exception should have been thrown");
//        } catch (MathIllegalArgumentException e) {
//            // Expected
//        }
//
//        double[] wzval = new double[] {-12, -8, -5.5, -3, -4, 2.5};
//        try {
//            p = interpolator.interpolate(xval, yval, wzval, fval);
////            Assert.fail("an exception should have been thrown");
//        } catch (MathIllegalArgumentException e) {
//            // Expected
//        }
//
//        double[][][] wfval = new double[xval.length][yval.length + 1][zval.length];
//        try {
//            p = interpolator.interpolate(xval, yval, zval, wfval);
////            Assert.fail("an exception should have been thrown");
//        } catch (DimensionMismatchException e) {
//            // Expected
//        }
//        wfval = new double[xval.length - 1][yval.length][zval.length];
//        try {
//            p = interpolator.interpolate(xval, yval, zval, wfval);
////            Assert.fail("an exception should have been thrown");
//        } catch (DimensionMismatchException e) {
//            // Expected
//        }
//        wfval = new double[xval.length][yval.length][zval.length - 1];
//        try {
//            p = interpolator.interpolate(xval, yval, zval, wfval);
////            Assert.fail("an exception should have been thrown");
//        } catch (DimensionMismatchException e) {
//            // Expected
//        }
    }
    
    
    public void test1() {
    	double[] xValues = new double[] {36, 36.001, 36.002};
    	double[] yValues = new double[] {-108.00, -107.999, -107.998};

    	double[][] fValues = new double[][] {{1915, 1906, 1931},
    	                                    {1877, 1889, 1894},
    	                                    {1878, 1873, 1888}};

//    	BicubicSplineInterpolator interpolator = new BicubicSplineInterpolator();
    	PiecewiseBicubicSplineInterpolator interpolator = new PiecewiseBicubicSplineInterpolator();
    	
//    	BicubicSplineInterpolatingFunction interpolatorFunction = interpolator.interpolate(xValues, yValues, fValues);
    	PiecewiseBicubicSplineInterpolatingFunction interpolatorFunction = interpolator.interpolate(xValues, yValues, fValues);
    	
    	double[][] results = new double[9][9];

    	double x = 36;
    	int arrayIndexX = 0;
    	int arrayIndexY = 0;

    	while(x <= 36.002) 
    	{

    	    double y = -108;
    	    arrayIndexY = 0;
    	    while (y <= -107.998)
    	    {

    	        results[arrayIndexX][arrayIndexY] = interpolatorFunction.value(x,  y);
    	        System.out.println(results[arrayIndexX][arrayIndexY]);
    	        y = y + 0.00025;
    	        arrayIndexY++;
    	    }

    	    x = x + 0.00025;            
    	    arrayIndexX++;
    	}   
    }
}