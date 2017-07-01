/**
 *     Copyleft (C) 2017  Paul Teng
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
final class FoldLeft extends CoreLambda {

    public FoldLeft() {
        super(new CoreLambda.Info("Fold left", "mat -> func", "Wraps the matrix in a fold left context. This operation is the equivalent of a reduce left operation."));
    }

    @Override
    public NtValue applyCall(final NtValue[] mat) {
        if (mat.length == 1 && mat[0] instanceof CoreMatrix) {
            return new CoreLambda(new CoreLambda.Info("$$Fold left", "func([supports applyCall], any) -> mat", "Performs a fold left on the matrix with the specified accumulator and the initial value")) {
                @Override
                public NtValue applyCall(final NtValue[] f) {
                    if (f.length == 2) {
                        return ((CoreMatrix) mat[0]).reduceLeft(f[0], f[1]);
                    }
                    throw new DispatchException("Expected an instance supporting applyCall and anything, got " + f.length + " instead");
                }
            };
        }
        throw new DispatchException("Fold left", "Expected a matrix, got " + mat.length + " instead");
    }
}
