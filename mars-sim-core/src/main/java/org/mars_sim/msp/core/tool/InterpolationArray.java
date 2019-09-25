package org.mars_sim.msp.core.tool;

public class InterpolationArray {
//	https://stackoverflow.com/questions/9668821/array-interpolation
	public static void main(String... args) {
		array2D();
//		array3D();
	}
	
	public static void array2D() {
	    double[][] source = new double[][]{{1, 1, 1, 2}, {1, 2, 2, 3}, {1, 2, 2, 3}, {1, 1, 3, 3}};
	    BicubicInterpolator bi = new BicubicInterpolator();
	    for (int i = 0; i <= 30; i++) {
	        double idx = i / 10.0;
	        System.out.printf("Result (%3.1f, %3.1f) : %3.1f%n", idx, idx, bi.getValue(source, idx, idx));
	    }
	    
//	    double[][] source = new double[][]{{1, 1, 1, 2}, {1, 2, 2, 3}, {1, 2, 2, 3}, {1, 1, 3, 3}};
//	    BicubicInterpolator bi = new BicubicInterpolator();
//	    for (int i = -10; i <= 20; i++) {
//	        double idx = i / 10.0;
//	        System.out.printf("Result (%3.1f, %3.1f) : %3.1f%n", idx, idx, bi.getValue(source, idx, idx));
//	    }
	}
	
//	public static void array3D() {
//	    double[][] source = new double[][]{{1, 1, 1, 2}, {1, 2, 2, 3}, {1, 2, 2, 3}, {1, 1, 3, 3}};
//	    CubicInterpolator ci = new CubicInterpolator();
//	    for (int i = -10; i <= 20; i++) {
//	        double idx = i / 10.0;
//	        System.out.printf("Result (%3.1f, %3.1f) : %3.1f%n", idx, idx, ci.getValue(source, idx, idx));
//	    }
//	}

	public static class CubicInterpolator {
	    public static double getValue(double[] p, double x) {
	        int xi = (int) x;
	        x -= xi;
	        double p0 = p[Math.max(0, xi - 1)];
	        double p1 = p[xi];
	        double p2 = p[Math.min(p.length - 1,xi + 1)];
	        double p3 = p[Math.min(p.length - 1, xi + 2)];
	        return p1 + 0.5 * x * (p2 - p0 + x * (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3 + x * (3.0 * (p1 - p2) + p3 - p0)));
	    }
	}

	public static class BicubicInterpolator extends CubicInterpolator {
	    private double[] arr = new double[4];

	    public double getValue(double[][] p, double x, double y) {
	        int xi = (int) x;
	        x -= xi;
	        arr[0] = getValue(p[Math.max(0, xi - 1)], y);
	        arr[1] = getValue(p[xi], y);
	        arr[2] = getValue(p[Math.min(p.length - 1,xi + 1)], y);
	        arr[3] = getValue(p[Math.min(p.length - 1, xi + 2)], y);
	        return getValue(arr, x+ 1);
	    }
	}
}
