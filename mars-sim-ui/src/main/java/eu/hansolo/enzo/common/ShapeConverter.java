/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.enzo.common;

import javafx.geometry.Bounds;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.VLineTo;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Created by
 * User: hansolo
 * Date: 02.04.13
 * Time: 12:49
 */
public class ShapeConverter {
    private static final double KAPPA = 0.5522847498307935;

    public static String shapeToSvgString(final Shape SHAPE) {
        final StringBuilder fxPath = new StringBuilder();
        if (Line.class.equals(SHAPE.getClass())) {
            fxPath.append(convertLine((Line) SHAPE));
        } else if (Arc.class.equals(SHAPE.getClass())) {
            fxPath.append(convertArc((Arc) SHAPE));
        } else if (QuadCurve.class.equals(SHAPE.getClass())) {
            fxPath.append(convertQuadCurve((QuadCurve) SHAPE));
        } else if (CubicCurve.class.equals(SHAPE.getClass())) {
            fxPath.append(convertCubicCurve((CubicCurve) SHAPE));
        } else if (Rectangle.class.equals(SHAPE.getClass())) {
            fxPath.append(convertRectangle((Rectangle) SHAPE));
        } else if (Circle.class.equals(SHAPE.getClass())) {
            fxPath.append(convertCircle((Circle) SHAPE));
        } else if (Ellipse.class.equals(SHAPE.getClass())) {
            fxPath.append(convertEllipse((Ellipse) SHAPE));
        } else if (Text.class.equals(SHAPE.getClass())) {
            Path path = (Path)(Shape.subtract(SHAPE, new Rectangle(0, 0)));
            fxPath.append(convertPath(path));
        } else if (Path.class.equals(SHAPE.getClass())) {
            fxPath.append(convertPath((Path) SHAPE));
        } else if (Polygon.class.equals(SHAPE.getClass())) {
            fxPath.append(convertPolygon((Polygon) SHAPE));
        } else if (Polyline.class.equals(SHAPE.getClass())) {
            fxPath.append(convertPolyline((Polyline) SHAPE));
        } else if (SVGPath.class.equals(SHAPE.getClass())) {
            fxPath.append(((SVGPath) SHAPE).getContent());
        }
        return fxPath.toString();
    }

    public static SVGPath shapeToSvgPath(final Shape SHAPE) {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(shapeToSvgString(SHAPE));
        return svgPath;
    }

    public static Path svgPathToPath(final SVGPath SVG_PATH) {
        String data = SVG_PATH.getContent();
        data = data.replaceAll("/(,)/", "/ /");
        data = data.replaceAll("/([A-Za-z])/", "/ $1 /"); // wrap single characters in blanks
        StringTokenizer tokenizer = new StringTokenizer(data, " ");
        List<String> pathList = new ArrayList<>();
        while(tokenizer.hasMoreElements()) {
            pathList.add(tokenizer.nextElement().toString());
        }
        PathReader pathReader = new PathReader(pathList);
        return processPath(pathList, pathReader);
    }

    public static String convertLine(final Line LINE) {
        final StringBuilder fxPath = new StringBuilder();
        fxPath.append("M ").append(LINE.getStartX()).append(" ").append(LINE.getStartY()).append(" ")
              .append("L ").append(LINE.getEndX()).append(" ").append(LINE.getEndY());
        return fxPath.toString();
    }

    public static String convertArc(final Arc ARC) {
        StringBuilder fxPath = new StringBuilder();
        double centerX    = ARC.getCenterX();
        double centerY    = ARC.getCenterY();
        double radiusX    = ARC.getRadiusX();
        double radiusY    = ARC.getRadiusY();
        double startAngle = Math.toRadians(-ARC.getStartAngle());
        double endAngle   = Math.toRadians(-ARC.getStartAngle() - ARC.getLength());
        double length     = ARC.getLength();

        double startX = radiusX * Math.cos(startAngle);
        double startY = radiusY * Math.sin(startAngle);

        double endX   = centerX + radiusX * Math.cos(endAngle);
        double endY   = centerY + radiusY * Math.sin(endAngle);

        int xAxisRot  = 0;
        int largeArc  = (length > 180) ? 1 : 0;
        int sweep     = (length < 0) ? 1 : 0;

        fxPath.append("M ").append(centerX).append(" ").append(centerY).append(" ");

        if (ArcType.ROUND == ARC.getType()) {
            fxPath.append("l ").append(startX).append(" ").append(startY).append(" ");
        }

        fxPath.append("A ").append(radiusX).append(" ").append(radiusY).append(" ")
              .append(xAxisRot).append(" ").append(largeArc).append(" ").append(sweep).append(" ")
              .append(endX).append(" ").append(endY).append(" ");
        if (ArcType.CHORD == ARC.getType() || ArcType.ROUND == ARC.getType()) {
            fxPath.append("Z");
        }
        return fxPath.toString();
    }

    public static String convertQuadCurve(final QuadCurve QUAD_CURVE) {
        final StringBuilder fxPath = new StringBuilder();
        fxPath.append("M ").append(QUAD_CURVE.getStartX()).append(" ").append(QUAD_CURVE.getStartY()).append(" ")
              .append("Q ").append(QUAD_CURVE.getControlX()).append(" ").append(QUAD_CURVE.getControlY())
              .append(QUAD_CURVE.getEndX()).append(" ").append(QUAD_CURVE.getEndY());
        return fxPath.toString();
    }

    public static String convertCubicCurve(final CubicCurve CUBIC_CURVE) {
        final StringBuilder fxPath = new StringBuilder();
        fxPath.append("M ").append(CUBIC_CURVE.getStartX()).append(" ").append(CUBIC_CURVE.getStartY()).append(" ")
              .append("C ").append(CUBIC_CURVE.getControlX1()).append(" ").append(CUBIC_CURVE.getControlY1()).append(" ")
              .append(CUBIC_CURVE.getControlX2()).append(" ").append(CUBIC_CURVE.getControlY2()).append(" ")
              .append(CUBIC_CURVE.getEndX()).append(" ").append(CUBIC_CURVE.getEndY());
        return fxPath.toString();
    }

    public static String convertRectangle(final Rectangle RECTANGLE) {
        final StringBuilder fxPath = new StringBuilder();
        final Bounds bounds = RECTANGLE.getBoundsInLocal();
        if (Double.compare(RECTANGLE.getArcWidth(), 0.0) == 0 && Double.compare(RECTANGLE.getArcHeight(), 0.0) == 0) {
            fxPath.append("M ").append(bounds.getMinX()).append(" ").append(bounds.getMinY()).append(" ")
                  .append("H ").append(bounds.getMaxX()).append(" ")
                  .append("V ").append(bounds.getMaxY()).append(" ")
                  .append("H ").append(bounds.getMinX()).append(" ")
                  .append("V ").append(bounds.getMinY()).append(" ")
                  .append("Z");
        } else {
            double x         = bounds.getMinX();
            double y         = bounds.getMinY();
            double width     = bounds.getWidth();
            double height    = bounds.getHeight();
            double arcWidth  = RECTANGLE.getArcWidth();
            double arcHeight = RECTANGLE.getArcHeight();
            double r         = x + width;
            double b         = y + height;
            fxPath.append("M ").append(x + arcWidth).append(" ").append(y).append(" ")
                  .append("L ").append(r - arcWidth).append(" ").append(y).append(" ")
                  .append("Q ").append(r).append(" ").append(y).append(" ").append(r).append(" ").append(y + arcHeight).append(" ")
                  .append("L ").append(r).append(" ").append(y + height - arcHeight).append(" ")
                  .append("Q ").append(r).append(" ").append(b).append(" ").append(r - arcWidth).append(" ").append(b).append(" ")
                  .append("L ").append(x + arcWidth).append(" ").append(b).append(" ")
                  .append("Q ").append(x).append(" ").append(b).append(" ").append(x).append(" ").append(b - arcHeight).append(" ")
                  .append("L ").append(x).append(" ").append(y + arcHeight).append(" ")
                  .append("Q ").append(x).append(" ").append(y).append(" ").append(x + arcWidth).append(" ").append(y).append(" ")
                  .append("Z");
        }
        return fxPath.toString();
    }

    public static String convertCircle(final Circle CIRCLE) {
        final StringBuilder fxPath = new StringBuilder();
        final double CENTER_X         = CIRCLE.getCenterX() == 0 ? CIRCLE.getRadius() : CIRCLE.getCenterX();
        final double CENTER_Y         = CIRCLE.getCenterY() == 0 ? CIRCLE.getRadius() : CIRCLE.getCenterY();
        final double RADIUS           = CIRCLE.getRadius();
        final double CONTROL_DISTANCE = RADIUS * KAPPA;
        // Move to first point
        fxPath.append("M ").append(CENTER_X).append(" ").append(CENTER_Y - RADIUS).append(" ");
        // 1. quadrant
        fxPath.append("C ").append(CENTER_X + CONTROL_DISTANCE).append(" ").append(CENTER_Y - RADIUS).append(" ")
              .append(CENTER_X + RADIUS).append(" ").append(CENTER_Y - CONTROL_DISTANCE).append(" ")
              .append(CENTER_X + RADIUS).append(" ").append(CENTER_Y).append(" ");
        // 2. quadrant
        fxPath.append("C ").append(CENTER_X + RADIUS).append(" ").append(CENTER_Y + CONTROL_DISTANCE).append(" ")
              .append(CENTER_X + CONTROL_DISTANCE).append(" ").append(CENTER_Y + RADIUS).append(" ")
              .append(CENTER_X).append(" ").append(CENTER_Y + RADIUS).append(" ");
        // 3. quadrant
        fxPath.append("C ").append(CENTER_X - CONTROL_DISTANCE).append(" ").append(CENTER_Y + RADIUS).append(" ")
              .append(CENTER_X - RADIUS).append(" ").append(CENTER_Y + CONTROL_DISTANCE).append(" ")
              .append(CENTER_X - RADIUS).append(" ").append(CENTER_Y).append(" ");
        // 4. quadrant
        fxPath.append("C ").append(CENTER_X - RADIUS).append(" ").append(CENTER_Y - CONTROL_DISTANCE).append(" ")
              .append(CENTER_X - CONTROL_DISTANCE).append(" ").append(CENTER_Y - RADIUS).append(" ")
              .append(CENTER_X).append(" ").append(CENTER_Y - RADIUS).append(" ");
        // Close path
        fxPath.append("Z");
        return fxPath.toString();
    }

    public static String convertEllipse(final Ellipse ELLIPSE) {
        final StringBuilder fxPath = new StringBuilder();
        final double CENTER_X           = ELLIPSE.getCenterX() == 0 ? ELLIPSE.getRadiusX() : ELLIPSE.getCenterX();
        final double CENTER_Y           = ELLIPSE.getCenterY() == 0 ? ELLIPSE.getRadiusY() : ELLIPSE.getCenterY();
        final double RADIUS_X           = ELLIPSE.getRadiusX();
        final double RADIUS_Y           = ELLIPSE.getRadiusY();
        final double CONTROL_DISTANCE_X = RADIUS_X * KAPPA;
        final double CONTROL_DISTANCE_Y = RADIUS_Y * KAPPA;
        // Move to first point
        fxPath.append("M ").append(CENTER_X).append(" ").append(CENTER_Y - RADIUS_Y).append(" ");
        // 1. quadrant
        fxPath.append("C ").append(CENTER_X + CONTROL_DISTANCE_X).append(" ").append(CENTER_Y - RADIUS_Y).append(" ")
              .append(CENTER_X + RADIUS_X).append(" ").append(CENTER_Y - CONTROL_DISTANCE_Y).append(" ")
              .append(CENTER_X + RADIUS_X).append(" ").append(CENTER_Y).append(" ");
        // 2. quadrant
        fxPath.append("C ").append(CENTER_X + RADIUS_X).append(" ").append(CENTER_Y + CONTROL_DISTANCE_Y).append(" ")
              .append(CENTER_X + CONTROL_DISTANCE_X).append(" ").append(CENTER_Y + RADIUS_Y).append(" ")
              .append(CENTER_X).append(" ").append(CENTER_Y + RADIUS_Y).append(" ");
        // 3. quadrant
        fxPath.append("C ").append(CENTER_X - CONTROL_DISTANCE_X).append(" ").append(CENTER_Y + RADIUS_Y).append(" ")
              .append(CENTER_X - RADIUS_X).append(" ").append(CENTER_Y + CONTROL_DISTANCE_Y).append(" ")
              .append(CENTER_X - RADIUS_X).append(" ").append(CENTER_Y).append(" ");
        // 4. quadrant
        fxPath.append("C ").append(CENTER_X - RADIUS_X).append(" ").append(CENTER_Y - CONTROL_DISTANCE_Y).append(" ")
              .append(CENTER_X - CONTROL_DISTANCE_X).append(" ").append(CENTER_Y - RADIUS_Y).append(" ")
              .append(CENTER_X).append(" ").append(CENTER_Y - RADIUS_Y).append(" ");
        // Close path
        fxPath.append("Z");
        return fxPath.toString();
    }

    public static String convertPath(final Path PATH) {
        final StringBuilder fxPath = new StringBuilder();
        for (PathElement element : PATH.getElements()) {
            if (MoveTo.class.equals(element.getClass())) {
                fxPath.append("M ")
                      .append(((MoveTo) element).getX()).append(" ")
                      .append(((MoveTo) element).getY()).append(" ");
            } else if (LineTo.class.equals(element.getClass())) {
                fxPath.append("L ")
                      .append(((LineTo) element).getX()).append(" ")
                      .append(((LineTo) element).getY()).append(" ");
            } else if (CubicCurveTo.class.equals(element.getClass())) {
                fxPath.append("C ")
                      .append(((CubicCurveTo) element).getControlX1()).append(" ")
                      .append(((CubicCurveTo) element).getControlY1()).append(" ")
                      .append(((CubicCurveTo) element).getControlX2()).append(" ")
                      .append(((CubicCurveTo) element).getControlY2()).append(" ")
                      .append(((CubicCurveTo) element).getX()).append(" ")
                      .append(((CubicCurveTo) element).getY()).append(" ");
            } else if (QuadCurveTo.class.equals(element.getClass())) {
                fxPath.append("Q ")
                      .append(((QuadCurveTo) element).getControlX()).append(" ")
                      .append(((QuadCurveTo) element).getControlY()).append(" ")
                      .append(((QuadCurveTo) element).getX()).append(" ")
                      .append(((QuadCurveTo) element).getY()).append(" ");
            } else if (ArcTo.class.equals(element.getClass())) {
                fxPath.append("A ")
                      .append(((ArcTo) element).getX()).append(" ")
                      .append(((ArcTo) element).getY()).append(" ")
                      .append(((ArcTo) element).getRadiusX()).append(" ")
                      .append(((ArcTo) element).getRadiusY()).append(" ");
            } else if (HLineTo.class.equals(element.getClass())) {
                fxPath.append("H ")
                      .append(((HLineTo) element).getX()).append(" ");
            } else if (VLineTo.class.equals(element.getClass())) {
                fxPath.append("V ")
                      .append(((VLineTo) element).getY()).append(" ");
            } else if (ClosePath.class.equals(element.getClass())) {
                fxPath.append("Z");
            }
        }
        return fxPath.toString();
    }

    public static String convertPolygon(final Polygon POLYGON) {
        final StringBuilder fxPath = new StringBuilder();
        final int           size   = POLYGON.getPoints().size();
        if (size % 2 == 0) {
            List<Double> coordinates     = POLYGON.getPoints();
            for (int i = 0 ; i < size ; i += 2) {
                fxPath.append(i == 0 ? "M " : "L ")
                      .append(coordinates.get(i)).append(" ").append(coordinates.get(i + 1)).append(" ");
            }
            fxPath.append("Z");
        }
        return fxPath.toString();
    }

    public static String convertPolyline(final Polyline POLYLINE) {
        final StringBuilder fxPath = new StringBuilder();
        final int           size   = POLYLINE.getPoints().size();
        if (size % 2 == 0) {
            List<Double> coordinates     = POLYLINE.getPoints();
            for (int i = 0 ; i < size ; i += 2) {
                fxPath.append(i == 0 ? "M " : "L ")
                      .append(coordinates.get(i)).append(" ").append(coordinates.get(i + 1)).append(" ");
            }
        }
        return fxPath.toString();
    }

    private static Path processPath(final List<String> PATH_LIST, final PathReader READER) {
        final Path PATH = new Path();
        PATH.setFillRule(FillRule.EVEN_ODD);
        while (!PATH_LIST.isEmpty()) {
            if ("M".equals(READER.read())) {
                PATH.getElements().add(new MoveTo(READER.nextX(), READER.nextY()));
            } else if ("L".equals(READER.read())) {
                PATH.getElements().add(new LineTo(READER.nextX(), READER.nextY()));
            } else if ("C".equals(READER.read())) {
                PATH.getElements().add(new CubicCurveTo(READER.nextX(), READER.nextY(), READER.nextX(), READER.nextY(), READER.nextX(), READER.nextY()));
            } else if ("Q".equals(READER.read())) {
                PATH.getElements().add(new QuadCurveTo(READER.nextX(), READER.nextY(), READER.nextX(), READER.nextY()));
            } else if ("H".equals(READER.read())) {
                PATH.getElements().add(new HLineTo(READER.nextX()));
            } else if ("L".equals(READER.read())) {
                PATH.getElements().add(new VLineTo(READER.nextY()));
            } else if ("A".equals(READER.read())) {
                PATH.getElements().add(new ArcTo(READER.nextX(), READER.nextY(), 0, READER.nextX(), READER.nextY(), false, false));
            } else if ("Z".equals(READER.read())) {
                PATH.getElements().add(new ClosePath());
            }
        }
        return PATH;
    }

    private static class PathReader {
        protected List<String> path;

        PathReader(final List<String> PATH) {
            path = PATH;
        }
        String read() {
            return path.remove(0);
        }
        double nextX() {
            return Double.parseDouble(read());
        }
        double nextY() {
            return Double.parseDouble(read());
        }
    }
}
