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
import com.ymcmp.ntshell.value.CoreNumber;

/**
 *
 * @author YTENG
 */
final class Reshape extends CoreLambda {

    public Reshape() {
        super(new CoreLambda.Info("reshape", "mat -> func", "Reshapes a matrix based. The original matrix is left untouched after the transformation."));
    }

    @Override
    public NtValue applyCall(final NtValue[] matrix) {
        if (matrix.length == 1 && matrix[0] instanceof CoreMatrix) {
            return new CoreLambda(new CoreLambda.Info("$$reshape", "func(row:num, col:num) -> mat", "Reshapes the matrix into (row) * (col). If the new matrix is bigger, an empty matrix is returned. If the new matrix is smaller, the excess values will be truncated. The original matrix is left untouched.")) {
                @Override
                public NtValue applyCall(final NtValue[] params) {
                    if (params.length == 2
                            && params[0] instanceof CoreNumber
                            && params[1] instanceof CoreNumber) {
                        final int rows = ((CoreNumber) params[0]).toDecimal().intValue();
                        final int cols = ((CoreNumber) params[1]).toDecimal().intValue();
                        try {
                            return ((CoreMatrix) matrix[0]).reshape(rows, cols);
                        } catch (CoreMatrix.MatrixBoundMismatchException ex) {
                            return CoreMatrix.getEmptyMatrix();
                        }
                    }
                    throw new DispatchException("Expected two numbers, got " + params.length + " instead");
                }
            };
        }
        throw new DispatchException("reshape", "Expected a matrix, got " + matrix.length + " instead");
    }
}
