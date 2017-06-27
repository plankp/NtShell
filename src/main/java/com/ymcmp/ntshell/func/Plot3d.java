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

import com.ymcmp.ntshell.DispatchException;
import com.ymcmp.ntshell.NtValue;
import com.ymcmp.ntshell.value.CoreMatrix;
import com.ymcmp.ntshell.value.CoreNumber;

import net.ericaro.surfaceplotter.JSurfacePanel;
import net.ericaro.surfaceplotter.Mapper;
import net.ericaro.surfaceplotter.ProgressiveSurfaceModel;

/**
 *
 * @author YTENG
 */
public class Plot3d {

    public final NtValue f;

    public Plot3d(final NtValue f) {
        this.f = f;
    }

    public JSurfacePanel draw(final NtValue[] range) {
        final ProgressiveSurfaceModel model = new ProgressiveSurfaceModel();
        final JSurfacePanel panel = new JSurfacePanel();
        panel.setModel(model);
        model.setMapper(new Mapper() {
            @Override
            public float f1(float x, float y) {
                final NtValue ret = f.applyCall(CoreNumber.from(x), CoreNumber.from(y));
                if (ret instanceof CoreNumber) {
                    return (float) ((CoreNumber) ret).toDouble();
                }
                return Float.NaN;
            }

            @Override
            public float f2(float x, float y) {
                return 0;
            }
        });

        if (0 < range.length && range[0] instanceof CoreMatrix) {
            PlotUtils.decodeRangeX(range, model);
        }
        if (1 < range.length && range[1] instanceof CoreMatrix) {
            PlotUtils.decodeRangeY(range, model);
        }
        if (2 < range.length && range[2] instanceof CoreMatrix) {
            PlotUtils.decodeRangeZ(range, model);
        }

        model.plot().execute();
        return panel;
    }
}
