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

import com.inamik.text.tables.Cell;
import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;

import com.ymcmp.ntshell.NtValue;
import com.ymcmp.ntshell.DispatchException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Arrays;

import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * A two dimensional matrix
 *
 * @author YTENG
 */
public class CoreMatrix extends NtValue {

    private static final Border BORDER_FMT = Border.of(Border.Chars.of('+', '-', '|'));
    private static final Pattern LINE_BREAK_PAT = Pattern.compile("\r?\n");

    public final NtValue[][] mat;

    private static class Helper {

        static final CoreMatrix EMPTY_MAT = new CoreMatrix(0, 0);
    }

    /**
     * Thrown when matrix bounds does not match the requirement of the operation
     */
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
        return mat[row][column];
    }

    public void setCell(int row, int column, final NtValue val) {
        mat[row][column] = val;
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

    /**
     * Tries to convert the current matrix into an atom. For a matrix to have an
     * atom, it must one dimensional. If the matrix contains other matrices, the
     * matrices are converted into atoms and joined in place onto the current
     * atom.
     *
     * @return The atom representation of the matrix
     */
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

    /**
     * Converts a two dimensional array into a matrix. This will fail if the
     * columns have different lengths.
     *
     * @param mat The two dimensional array
     * @return The matrix equivalent
     */
    public static CoreMatrix from(final NtValue[][] mat) {
        if (mat.length == 0) {
            return getEmptyMatrix();
        }
        if (mat.length > 1) {
            final int testAgainst = mat[0].length;
            for (int i = 1; i < mat.length; ++i) {
                if (testAgainst != mat[i].length) {
                    throw new IllegalArgumentException("Array is not does not form a rectangle");
                }
            }
        }
        return new CoreMatrix(mat);
    }

    /**
     * Applies {@code applyPositive} operation on every element
     *
     * @return
     */
    @Override
    public CoreMatrix applyPositive() {
        return this.map(NtValue::applyPositive);
    }

    /**
     * Applies {@code applyNegative} operation on every element
     *
     * @return
     */
    @Override
    public CoreMatrix applyNegative() {
        return this.map(NtValue::applyNegative);
    }

    /**
     * Applies {@code applyPercentage} operation on every element
     *
     * @return
     */
    @Override
    public CoreMatrix applyPercentage() {
        return this.map(NtValue::applyPercentage);
    }

    /**
     * Retrieves a particular cell of the matrix.
     *
     * @param params Two numbers: (row, column)
     * @return The element, or {@link com.ymcmp.ntshell.value.CoreAtom} if
     * boundaries are illegal.
     */
    @Override
    public NtValue applyCall(final NtValue[] params) {
        // mat(1, 2) => getCell(1, 2)
        if (params.length == 2
                && params[0] instanceof CoreNumber
                && params[1] instanceof CoreNumber) {
            final int row = (int) ((CoreNumber) params[0]).toDouble();
            final int column = (int) ((CoreNumber) params[1]).toDouble();
            try {
                return getCell(row - 1, column - 1);
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
            try {
                return crossProduct((CoreMatrix) rhs);
            } catch (MatrixBoundMismatchException ex) {
                throw ex.toDispatchException("*");
            }
        }

        return this.map(el -> el.applyMul(rhs));
    }

    /**
     * Calculates the cross product (matrix multiplication) of the two matrices
     *
     * @param rhs The other matrix
     * @return The new matrix
     * @throws com.ymcmp.ntshell.value.CoreMatrix.MatrixBoundMismatchException
     * If the two matrices do not have a shape of {@code m*n} and {@code n*p}
     */
    public CoreMatrix crossProduct(final CoreMatrix rhs) throws MatrixBoundMismatchException {
        if (mat.length == 0) {
            if (rhs.mat.length == 0) {
                return Helper.EMPTY_MAT;
            }
            throw new MatrixBoundMismatchException("Matrices do not have capatible shape");
        }
        if (mat[0].length != rhs.mat.length) {
            throw new MatrixBoundMismatchException("Matrices do not have capatible shape");
        }
        // Here, shape is capatible: (m, n) * (n, p) => (m, p)
        final NtValue[][] rows = new NtValue[mat.length][rhs.mat[0].length];
        for (int x = 0; x < rows.length; ++x) {
            final int columnCount = rows[x].length;
            for (int y = 0; y < columnCount; ++y) {
                NtValue acc = null;
                for (int k = 0; k < rhs.mat.length; ++k) {
                    final NtValue r = mat[x][k].applyMul(rhs.mat[k][y]);
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

    /**
     * Test to see if two matrices have the same shape (same amount of rows and
     * columns)
     *
     * @param other The other matrix
     * @return {@code true} if same shape, {@code false} otherwise
     */
    public boolean sameShape(final CoreMatrix other) {
        if (mat.length == other.mat.length) {
            if (mat.length == 0) {
                return true;
            }
            return mat[0].length == other.mat[0].length;
        }
        return false;
    }

    /**
     * Applies a transformation on every element of the matrix in relation to
     * the other matrix. Two matrices must have the same size.
     *
     * @param rhs The other matrix
     * @param transformer The transformation
     * @return The new matrix
     * @throws com.ymcmp.ntshell.value.CoreMatrix.MatrixBoundMismatchException
     * If two matrices have different sizes
     */
    public CoreMatrix bimap(final CoreMatrix rhs,
                            final BiFunction<NtValue, NtValue, NtValue> transformer)
            throws MatrixBoundMismatchException {
        if (!sameShape(rhs)) {
            throw new MatrixBoundMismatchException("Two matrices have different shapes");
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

    /**
     * Applies a transformation on every element of the matrix
     *
     * @param transformer The transformation
     * @return The new matrix
     */
    public CoreMatrix map(final Function<NtValue, NtValue> transformer) {
        return map(CoreLambda.from(x -> transformer.apply(x[0])));
    }

    /**
     * Applies a transformation on every element of the matrix
     *
     * @param transformer Must support
     * {@link NtValue#applyCall(com.ymcmp.ntshell.NtValue...)}
     * @return The new matrix
     */
    public CoreMatrix map(final NtValue transformer) {
        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        return new CoreMatrix(Arrays.stream(mat)
                .map(Arrays::stream)
                .map(s -> s.map(transformer::applyCall))
                .map(s -> s.toArray(NtValue[]::new))
                .toArray(NtValue[][]::new));
    }

    /**
     * Transposes a matrix
     *
     * @return The new matrix
     */
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

    /**
     * Flips the matrix on the Y axis
     *
     * @return The new matrix
     */
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

    /**
     * Flips the matrix on the X axis
     *
     * @return The new matrix
     */
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
            final int outerBound = arr.length - 1 - i;
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

    /**
     * Creates a new matrix with the specified rows and columns. The new matrix
     * must not have an area bigger than the original matrix. If the new matrix
     * is smaller than the original matrix, the excess elements are ignored. For
     * example: {@code iota(10) reshape (2, 5)} results in
     * {@code [1, 2, 3, 4, 5; 6, 7, 8, 9, 10]} while
     * {@code iota(6) reshape(1, 5)} results in {@code [1, 2, 3, 4, 5]}.
     *
     * @param rows The amount of rows
     * @param columns The amount of columns
     * @return The new matrix with the specified shape
     * @throws com.ymcmp.ntshell.value.CoreMatrix.MatrixBoundMismatchException
     * If the new matrix has a bigger area than the original matrix
     */
    public CoreMatrix reshape(int rows, int columns) throws MatrixBoundMismatchException {
        /*
        [1, 2, 3, 4, 5, 6, 7, 8, 9, 10].reshape(2, 5) =>
        [1, 2, 3, 4, 5;
         6, 7, 8, 9, 10]
            
        [1, 2, 3, 4, 5, 6].reshape(1, 5) =>
        [1, 2, 3, 4, 5]
         */
        final int newLinearLength = rows * columns;
        if (newLinearLength == 0) {
            return Helper.EMPTY_MAT;
        }

        if (mat.length == 0) {
            if (newLinearLength > 0) {
                throw new MatrixBoundMismatchException("New shape is bigger than old shape: (linear length) " + newLinearLength + " > 0");
            }
        }

        final int oldLinearLength = mat.length * mat[0].length;
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

    @Override
    public String toString() {
        if (mat.length == 0) {
            return "[]";
        }

        final GridTable table = GridTable.of(mat.length, mat[0].length);
        for (int x = 0; x < mat.length; ++x) {
            final int colCount = mat[x].length;
            for (int y = 0; y < colCount; ++y) {
                final String[] lines = LINE_BREAK_PAT.split(mat[x][y].toString());
                table.put(x, y, Cell.of(lines));
                table.apply(x, y, Cell.Functions.VERTICAL_CENTER);
                table.apply(x, y, Cell.Functions.HORIZONTAL_CENTER);
            }
        }

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             final PrintStream pw = new PrintStream(baos)) {
            Util.print(BORDER_FMT.apply(table), pw);
            return baos.toString().trim();
        } catch (IOException ex) {
            // This should not happen
        }
        return "";
    }
}
