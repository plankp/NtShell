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
package ntshell.rt.lib.rout.matrix;

import com.ymcmp.ntshell.NtValue;

import com.ymcmp.ntshell.rte.DispatchException;

import com.ymcmp.ntshell.value.CoreLambda;
import com.ymcmp.ntshell.value.CoreMatrix;

/**
 *
 * @author YTENG
 */
public final class FlipY extends CoreLambda {

    private static class Helper {

        static final FlipY INSTANCE = new FlipY();
    }

    private FlipY() {
        super(new CoreLambda.Info("flip_y", "mat -> mat", "Flips a matrix by the y axis. The original matrix is left untouched."));
    }

    public static NtValue getInstance() {
        return Helper.INSTANCE;
    }

    @Override
    public NtValue applyCall(final NtValue[] params) {
        if (params.length == 1 && params[0] instanceof CoreMatrix) {
            return ((CoreMatrix) params[0]).flipOnY();
        }
        throw new DispatchException("flip_y", "Expected a matrix, got " + params.length + " instead");
    }
}
