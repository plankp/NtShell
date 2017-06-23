/**
 *     Copyright (C) 2017  Paul Teng
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ymcmp.ntshell;

import de.erichseifert.gral.graphics.AbstractDrawable;
import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.graphics.DrawingContext;

import de.erichseifert.gral.plots.DataPoint;
import de.erichseifert.gral.plots.lines.AbstractLineRenderer2D;

import de.erichseifert.gral.util.GraphicsUtils;

import java.awt.Paint;
import java.awt.Shape;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import java.util.List;

/**
 *
 * @author YTENG
 */
public class BrokenLineRenderer extends AbstractLineRenderer2D {

    // Based off the DefaultLineRenderer2D
    private static final int INITIAL_LINE_CAPACITY = 10000;

    @Override
    public Shape getLineShape(List<DataPoint> points) {
        // Construct shape
        Path2D shape = new Path2D.Double(
                Path2D.WIND_NON_ZERO, INITIAL_LINE_CAPACITY);
        for (DataPoint point : points) {
            Point2D pos = point.position.getPoint2D();
            if (shape.getCurrentPoint() == null) {
                shape.moveTo(pos.getX(), pos.getY());
            } else {
                final Comparable<?> get = point.data.row.get(1);
                if (get instanceof Double) {
                    final double yval = (Double) get;
                    if (Double.isNaN(yval)) {
                        // Let the next point link through AS-IF it was there
                        continue;
                    }
                    if (Double.isInfinite(yval)) {
                        // Draw the current line segment, and draw a line
                        // along the Y axis
                        shape.lineTo(pos.getX(), pos.getY());
                        shape.lineTo(pos.getX(), -pos.getY());
                        continue;
                    }
                }
                shape.lineTo(pos.getX(), pos.getY());
            }
        }
        return stroke(shape);
    }

    @Override
    public Drawable getLine(List<DataPoint> list, Shape shape) {
        return new AbstractDrawable() {
            @Override
            public void draw(DrawingContext context) {
                // Draw line
                Paint paint = BrokenLineRenderer.this.getColor();
                GraphicsUtils.fillPaintedShape(
                        context.getGraphics(), shape, paint, null);
            }
        };
    }
}