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
package com.ymcmp.ntshell.func;

import com.ymcmp.ntshell.NtValue;
import com.ymcmp.ntshell.value.CoreMatrix;
import com.ymcmp.ntshell.value.CoreNumber;

import net.ericaro.surfaceplotter.ProgressiveSurfaceModel;

/**
 *
 * @author YTENG
 */
class PlotUtils {

    public static void decodeRangeX(final NtValue[] range, final ProgressiveSurfaceModel model) {
        final CoreMatrix xrange = (CoreMatrix) range[0];
        final float min = (float) ((CoreNumber) xrange.getCell(0, 0)).toDecimal().floatValue();
        final float max = (float) ((CoreNumber) xrange.getCell(0, 1)).toDecimal().floatValue();
        model.setXMin(min);
        model.setXMax(max);
    }

    public static void decodeRangeY(final NtValue[] range, final ProgressiveSurfaceModel model) {
        final CoreMatrix yrange = (CoreMatrix) range[1];
        final float ymin = (float) ((CoreNumber) yrange.getCell(0, 0)).toDecimal().floatValue();
        final float ymax = (float) ((CoreNumber) yrange.getCell(0, 1)).toDecimal().floatValue();
        model.setYMin(ymin);
        model.setYMax(ymax);
    }

    public static void decodeRangeZ(final NtValue[] range, final ProgressiveSurfaceModel model) {
        final CoreMatrix zrange = (CoreMatrix) range[2];
        final float zmin = (float) ((CoreNumber) zrange.getCell(0, 0)).toDecimal().floatValue();
        final float zmax = (float) ((CoreNumber) zrange.getCell(0, 1)).toDecimal().floatValue();
        model.setZMin(zmin);
        model.setZMax(zmax);
    }

    public static float toGraphFloat(final NtValue ret) {
        if (ret instanceof CoreNumber) {
            final CoreNumber n = (CoreNumber) ret;
            if (n.isNaN()) {
                return Float.NaN;
            }
            if (n.isInfinite()) {
                if (n.isNegative()) {
                    return Float.NEGATIVE_INFINITY;
                }
                return Float.POSITIVE_INFINITY;
            }
            return n.toDecimal().floatValue();
        }
        return Float.NaN;
    }
}
