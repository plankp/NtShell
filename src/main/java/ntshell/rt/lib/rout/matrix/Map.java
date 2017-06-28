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
public final class Map extends CoreLambda {

    private static class Helper {

        static final Map INSTANCE = new Map();
    }

    private Map() {
        super(new CoreLambda.Info("map", "mat -> func", "Wraps matrix in a map context. A map is defined as an equivalent application on all elements. The original matrix is left untouched after the transformation."));
    }

    public static NtValue getInstance() {
        return Helper.INSTANCE;
    }

    @Override
    public NtValue applyCall(final NtValue[] mat) {
        if (mat.length == 1 && mat[0] instanceof CoreMatrix) {
            return new CoreLambda(new CoreLambda.Info("$$map", "func([supports applyCall]) -> mat", "Performs the specified transformation on the matrix elements. The original matrix is left untouched.")) {
                @Override
                public NtValue applyCall(final NtValue[] f) {
                    if (f.length == 1) {
                        return ((CoreMatrix) mat[0]).map(f[0]);
                    }
                    throw new DispatchException("Expected an instance supporting applyCall, got " + f.length + " instead");
                }
            };
        }
        throw new DispatchException("map", "Expected a matrix, got " + mat.length + " instead");
    }
}
