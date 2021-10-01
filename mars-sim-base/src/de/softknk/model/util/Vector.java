package de.softknk.model.util;

public interface Vector {

    class Vector2D {

        public double x, y;

        public Vector2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    static double distanceBetween(Vector2D vec1, Vector2D vec2) {
        //compute the distance between two coordinates with the help of the theorem of Pythagoras
        return (Math.sqrt(Math.pow(Math.abs(vec1.y - vec2.y), 2) + Math.pow(Math.abs(vec1.x - vec2.x), 2)));
    }
}
