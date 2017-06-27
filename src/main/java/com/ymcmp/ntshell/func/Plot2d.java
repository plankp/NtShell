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

import net.ericaro.surfaceplotter.Mapper;
import net.ericaro.surfaceplotter.JSurfacePanel;
import net.ericaro.surfaceplotter.ProgressiveSurfaceModel;

/**
 *
 * @author YTENG
 */
public class Plot2d implements Plotter {

    public final NtValue f;
    public final NtValue g;

    public Plot2d(final NtValue f) {
        this(f, null);
    }

    public Plot2d(final NtValue f, final NtValue g) {
        this.f = f;
        this.g = g;
    }

    @Override
    public JSurfacePanel plot(final NtValue[] range) {
        final ProgressiveSurfaceModel model = new ProgressiveSurfaceModel();
        final JSurfacePanel panel = new JSurfacePanel();
        panel.setModel(model);
        model.setMapper(new Mapper() {
            @Override
            public float f1(float x, float y) {
                return PlotUtils.toGraphFloat(f.applyCall(CoreNumber.from(x)));
            }

            @Override
            public float f2(float x, float y) {
                if (g == null) {
                    return 0;
                }
                return PlotUtils.toGraphFloat(g.applyCall(CoreNumber.from(x)));
            }
        });

        if (0 < range.length && range[0] instanceof CoreMatrix) {
            PlotUtils.decodeRangeX(range, model);
        }
        if (1 < range.length && range[1] instanceof CoreMatrix) {
            PlotUtils.decodeRangeY(range, model);
        }

        if (g != null) {
            model.setBothFunction(true);
        }

        model.plot().execute();
        return panel;
    }
}
