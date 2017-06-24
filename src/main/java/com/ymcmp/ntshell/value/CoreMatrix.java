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
package com.ymcmp.ntshell.value;

import com.ymcmp.ntshell.NtValue;
import com.ymcmp.ntshell.DispatchException;

import java.util.Arrays;

import java.util.function.Function;
import java.util.function.BiFunction;

/**
 *
 * @author YTENG
 */
public class CoreMatrix extends NtValue {

    public final NtValue[][] mat;

    private static class Helper {

        static final CoreMatrix EMPTY_MAT = new CoreMatrix(0, 0);
    }

    public static class MatrixBoundMismatchException extends Exception {

        public MatrixBoundMismatchException(String msg) {
            super(msg);
        }

        public DispatchException toDispatchException(final String dispatcher) {
            return new DispatchException(dispatcher, getMessage());
        }
    }

    protected CoreMatrix(final NtValue[][] mat) {
        this.mat = mat;
    }

    public CoreMatrix(int rows, int columns) {
        this.mat = new NtValue[rows][columns];
    }

    public NtValue getCell(int row, int column) {
        return mat[row - 1][column - 1];
    }

    public void setCell(int row, int column, final NtValue val) {
        mat[row - 1][column - 1] = val;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Arrays.deepHashCode(this.mat);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof CoreMatrix) {
            final CoreMatrix other = (CoreMatrix) obj;
            return Arrays.deepEquals(this.mat, other.mat);
        }
        return false;
    }

    public CoreAtom toAtom() {
        if (mat.length == 0) {
            return CoreAtom.from("");
        }
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < mat[0].length; ++i) {
            final NtValue el = mat[0][i];
            if (el instanceof CoreMatrix) {
                buf.append(((CoreMatrix) el).toAtom().str);
            } else if (el instanceof CoreNumber) {
                buf.append((char) ((CoreNumber) el).toDouble());
            } else {
                throw new ClassCastException("Cannot convert " + el.getClass().getSimpleName() + " to character");
            }
        }
        return CoreAtom.from(buf.toString());
    }

    public static CoreMatrix getEmptyMatrix() {
        return Helper.EMPTY_MAT;
    }

    public static CoreMatrix from(final NtValue[][] mat) {
        if (mat.length == 0) {
            return getEmptyMatrix();
        }
        if (mat.length == 1) {
            final int testAgainst = mat[0].length;
            for (int i = 1; 1 < mat.length; ++i) {
                if (testAgainst != mat[i].length) {
                    throw new IllegalArgumentException("Array is not does not form a rectangle");
                }
            }
        }
        return new CoreMatrix(mat);
    }

    @Override
    public CoreMatrix applyPositive() {
        return this.map(NtValue::applyPositive);
    }

    @Override
    public CoreMatrix applyNegative() {
        return this.map(NtValue::applyNegative);
    }

    @Override
    public CoreMatrix applyPercentage() {
        return this.map(NtValue::applyPercentage);
    }

    @Override
    public NtValue applyCall(final NtValue[] params) {
        // mat(1, 2) => getCell(1, 2)
        if (params.length == 2
                && params[0] instanceof CoreNumber
                && params[1] instanceof CoreNumber) {
            final int row = (int) ((CoreNumber) params[0]).toDouble();
            final int column = (int) ((CoreNumber) params[1]).toDouble();
            try {
                return getCell(row, column);
            } catch (ArrayIndexOutOfBoundsException ex) {
                return CoreUnit.getInstance();
            }
        }
        return super.applyCall(params);
    }

    @Override
    public NtValue applyAdd(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            try {
                return this.bimap((CoreMatrix) rhs, (a, b) -> a.applyAdd(b));
            } catch (MatrixBoundMismatchException ex) {
                throw ex.toDispatchException("+");
            }
        }

        return map(el -> el.applyAdd(rhs));
    }

    public NtValue applyRSub(NtValue lhs) {
        if (lhs instanceof CoreMatrix) {
            try {
                return this.bimap((CoreMatrix) lhs, (a, b) -> b.applySub(a));
            } catch (MatrixBoundMismatchException ex) {
                throw ex.toDispatchException("-");
            }
        }

        return this.map(lhs::applySub);
    }

    @Override
    public NtValue applySub(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            try {
                return this.bimap((CoreMatrix) rhs, (a, b) -> a.applySub(b));
            } catch (MatrixBoundMismatchException ex) {
                throw ex.toDispatchException("-");
            }
        }

        return this.map(el -> el.applySub(rhs));
    }

    public NtValue applyRMod(NtValue lhs) {
        if (lhs instanceof CoreMatrix) {
            try {
                return this.bimap((CoreMatrix) lhs, (a, b) -> b.applyPow(a));
            } catch (MatrixBoundMismatchException ex) {
                throw ex.toDispatchException("mod");
            }
        }

        return this.map(lhs::applyMod);
    }

    @Override
    public NtValue applyMod(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            try {
                return this.bimap((CoreMatrix) rhs, (a, b) -> a.applyMod(b));
            } catch (MatrixBoundMismatchException ex) {
                throw ex.toDispatchException("mod");
            }
        }

        return this.map(el -> el.applyMod(rhs));
    }

    public NtValue applyRPow(NtValue lhs) {
        if (lhs instanceof CoreMatrix) {
            try {
                return this.bimap((CoreMatrix) lhs, (a, b) -> b.applyPow(a));
            } catch (MatrixBoundMismatchException ex) {
                throw ex.toDispatchException("^");
            }
        }

        return this.map(lhs::applyPow);
    }

    @Override
    public NtValue applyPow(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            try {
                return this.bimap((CoreMatrix) rhs, (a, b) -> a.applyPow(b));
            } catch (MatrixBoundMismatchException ex) {
                throw ex.toDispatchException("^");
            }
        }
        return this.map(el -> el.applyPow(rhs));
    }

    @Override
    public NtValue applyMul(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            final CoreMatrix rhsMat = (CoreMatrix) rhs;
            if (mat.length == 0) {
                if (rhsMat.mat.length == 0) {
                    return Helper.EMPTY_MAT;
                }
                throw new DispatchException("*", "Matricies do not have capatible shape");
            }
            if (mat[0].length != rhsMat.mat.length) {
                throw new DispatchException("*", "Matricies do not have capatible shape");
            }
            // Here, shape is capatible: (m, n) * (n, p) => (m, p)
            final NtValue[][] rows = new NtValue[mat.length][rhsMat.mat[0].length];
            for (int x = 0; x < rows.length; ++x) {
                final int columnCount = rows[x].length;
                for (int y = 0; y < columnCount; ++y) {
                    NtValue acc = null;
                    for (int k = 0; k < rhsMat.mat.length; ++k) {
                        final NtValue r = mat[x][k].applyMul(rhsMat.mat[k][y]);
                        if (acc == null) {
                            acc = r;
                            continue;
                        }
                        acc = acc.applyAdd(r);
                    }
                    rows[x][y] = acc;
                }
            }
            return new CoreMatrix(rows);
        }

        return this.map(el -> el.applyMul(rhs));
    }

    public NtValue applyRDiv(NtValue lhs) {
        if (lhs instanceof CoreMatrix) {
            throw new DispatchException("/", "Currently not supported");
        }

        return this.map(lhs::applyDiv);
    }

    @Override
    public NtValue applyDiv(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            throw new DispatchException("/", "Currently not supported");
        }

        return this.map(el -> el.applyDiv(rhs));
    }

    public boolean sameShape(final CoreMatrix other) {
        if (mat.length == other.mat.length) {
            if (mat.length == 0) {
                return true;
            }
            return mat[0].length == other.mat[0].length;
        }
        return false;
    }

    public CoreMatrix bimap(final CoreMatrix rhs,
                            final BiFunction<NtValue, NtValue, NtValue> transformer)
            throws MatrixBoundMismatchException {
        if (!sameShape(rhs)) {
            throw new MatrixBoundMismatchException("Two matricies have different shapes");
        }

        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = transformer.apply(columns[y], rhs.mat[x][y]);
            }
        }
        return new CoreMatrix(rows);
    }

    public CoreMatrix map(final Function<NtValue, NtValue> transformer) {
        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = transformer.apply(columns[y]);
            }
        }
        return new CoreMatrix(rows);
    }

    public CoreMatrix map(final NtValue transformer) {
        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = transformer.applyCall(columns[y]);
            }
        }
        return new CoreMatrix(rows);
    }

    public CoreMatrix transpose() {
        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat[0].length][mat.length];
        for (int x = 0; x < rows.length; ++x) {
            final int columnCount = rows[x].length;
            for (int y = 0; y < columnCount; ++y) {
                rows[x][y] = mat[y][x];
            }
        }
        return new CoreMatrix(rows);
    }

    public CoreMatrix flipOnY() {
        // 1 2 => 2 1
        // 3 4    4 3
        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }
        final NtValue[][] ret = Arrays.copyOf(mat, mat.length);
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = reverse(ret[i]);
        }
        return new CoreMatrix(ret);
    }

    public CoreMatrix flipOnX() {
        // 1 2 => 3 4
        // 3 4    1 2
        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }
        final NtValue[][] ret = reverse(Arrays.copyOf(mat, mat.length));
        return new CoreMatrix(ret);
    }

    private static <T> T[] reverse(final T[] arr) {
        if (arr.length < 2) {
            return arr;
        }

        final int upTo = arr.length / 2;
        for (int i = 0; i < upTo; ++i) {
            final int outerBound = arr.length - 1;
            final T tmp = arr[i];
            arr[i] = arr[outerBound];
            arr[outerBound] = tmp;
        }
        return arr;
    }

    @Override
    public boolean isTruthy() {
        return mat.length > 0;
    }

    public CoreMatrix reshape(int rows, int columns) throws MatrixBoundMismatchException {
        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }
        /*
        [1, 2, 3, 4, 5, 6, 7, 8, 9, 10].reshape(2, 5) =>
        [1, 2, 3, 4, 5;
         6, 7, 8, 9, 10]
            
        [1, 2, 3, 4, 5, 6].reshape(1, 5) =>
        [1, 2, 3, 4, 5]
         */
        final int oldLinearLength = mat.length * mat[0].length;
        final int newLinearLength = rows * columns;
        if (newLinearLength <= oldLinearLength) {
            final NtValue[][] ret = new NtValue[rows][columns];
            int xOld = 0;
            int yOld = 0;

            for (int xNew = 0; xNew < ret.length; ++xNew) {
                final int columnCount = ret[xNew].length;
                for (int yNew = 0; yNew < columnCount; ++yNew) {
                    ret[xNew][yNew] = mat[xOld][yOld++];
                    if (yOld >= mat[xOld].length) {
                        yOld = 0;
                        ++xOld;
                    }
                }
            }
            return new CoreMatrix(ret);
        }
        throw new MatrixBoundMismatchException("New shape is bigger than old shape: (linear length) " + newLinearLength + " > " + oldLinearLength);
    }

    protected LogicalLine toLogicalLine() {
        if (mat.length == 0) {
            return new LogicalLine().wrapInBox();
        }

        LogicalLine outer = null;
        for (int x = 0; x < mat.length; ++x) {
            LogicalLine inner = null;
            final int columnCount = mat[x].length;
            for (int y = 0; y < columnCount; ++y) {
                final NtValue val = mat[x][y];
                final LogicalLine ln;
                if (val instanceof CoreMatrix) {
                    ln = ((CoreMatrix) val).toLogicalLine();
                } else {
                    ln = new LogicalLine(val.toString());
                }

                if (inner == null) {
                    inner = ln;
                } else {
                    inner = inner.mergeWith(ln);
                }
            }
            if (outer == null) {
                outer = inner;
            } else {
                outer = outer.appendLine(inner);
            }
        }
        return outer.wrapInBox();
    }

    @Override
    public String toString() {
        return toLogicalLine().toString();
    }
}
