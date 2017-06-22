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

import com.ymcmp.ntshell.DispatchException;
import com.ymcmp.ntshell.NtValue;

import java.util.Arrays;

/**
 *
 * @author YTENG
 */
public class CoreMatrix extends NtValue {

    private static class Helper {

        static final CoreMatrix EMPTY_MAT = new CoreMatrix(0, 0);
    }

    public final NtValue[][] mat;

    private CoreMatrix(final NtValue[][] mat) {
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
        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = columns[y].applyPositive();
            }
        }
        return new CoreMatrix(rows);
    }

    @Override
    public CoreMatrix applyNegative() {
        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = columns[y].applyNegative();
            }
        }
        return new CoreMatrix(rows);
    }

    @Override
    public CoreMatrix applyPercentage() {
        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = columns[y].applyPercentage();
            }
        }
        return new CoreMatrix(rows);
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
            final CoreMatrix rhsMat = (CoreMatrix) rhs;
            if (!sameShape(rhsMat)) {
                throw new DispatchException("+", "Two matricies have different shapes");
            }
            if (mat.length == 0) {
                return Helper.EMPTY_MAT;
            }

            final NtValue[][] rows = new NtValue[mat.length][];
            for (int x = 0; x < rows.length; ++x) {
                final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
                rows[x] = columns;
                for (int y = 0; y < columns.length; ++y) {
                    columns[y] = columns[y].applyAdd(rhsMat.mat[x][y]);
                }
            }
            return new CoreMatrix(rows);
        }

        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = columns[y].applyAdd(rhs);
            }
        }
        return new CoreMatrix(rows);
    }

    @Override
    public NtValue applySub(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            final CoreMatrix rhsMat = (CoreMatrix) rhs;
            if (!sameShape(rhsMat)) {
                throw new DispatchException("-", "Two matricies have different shapes");
            }
            if (mat.length == 0) {
                return Helper.EMPTY_MAT;
            }

            final NtValue[][] rows = new NtValue[mat.length][];
            for (int x = 0; x < rows.length; ++x) {
                final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
                rows[x] = columns;
                for (int y = 0; y < columns.length; ++y) {
                    columns[y] = columns[y].applySub(rhsMat.mat[x][y]);
                }
            }
            return new CoreMatrix(rows);
        }

        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = columns[y].applySub(rhs);
            }
        }
        return new CoreMatrix(rows);
    }

    @Override
    public NtValue applyMod(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            final CoreMatrix rhsMat = (CoreMatrix) rhs;
            if (!sameShape(rhsMat)) {
                throw new DispatchException("mod", "Two matricies have different shapes");
            }
            if (mat.length == 0) {
                return Helper.EMPTY_MAT;
            }

            final NtValue[][] rows = new NtValue[mat.length][];
            for (int x = 0; x < rows.length; ++x) {
                final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
                rows[x] = columns;
                for (int y = 0; y < columns.length; ++y) {
                    columns[y] = columns[y].applyMod(rhsMat.mat[x][y]);
                }
            }
            return new CoreMatrix(rows);
        }

        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = columns[y].applyMod(rhs);
            }
        }
        return new CoreMatrix(rows);
    }

    @Override
    public NtValue applyPow(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            final CoreMatrix rhsMat = (CoreMatrix) rhs;
            if (!sameShape(rhsMat)) {
                throw new DispatchException("^", "Two matricies have different shapes");
            }
            if (mat.length == 0) {
                return Helper.EMPTY_MAT;
            }

            final NtValue[][] rows = new NtValue[mat.length][];
            for (int x = 0; x < rows.length; ++x) {
                final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
                rows[x] = columns;
                for (int y = 0; y < columns.length; ++y) {
                    columns[y] = columns[y].applyPow(rhsMat.mat[x][y]);
                }
            }
            return new CoreMatrix(rows);
        }

        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = columns[y].applyPow(rhs);
            }
        }
        return new CoreMatrix(rows);
    }

    @Override
    public NtValue applyMul(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            throw new DispatchException("*", "Currently not supported");
        }

        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = columns[y].applyMul(rhs);
            }
        }
        return new CoreMatrix(rows);
    }

    @Override
    public NtValue applyDiv(NtValue rhs) {
        if (rhs instanceof CoreMatrix) {
            throw new DispatchException("/", "Currently not supported");
        }

        if (mat.length == 0) {
            return Helper.EMPTY_MAT;
        }

        final NtValue[][] rows = new NtValue[mat.length][];
        for (int x = 0; x < rows.length; ++x) {
            final NtValue[] columns = Arrays.copyOf(mat[x], mat[x].length);
            rows[x] = columns;
            for (int y = 0; y < columns.length; ++y) {
                columns[y] = columns[y].applyDiv(rhs);
            }
        }
        return new CoreMatrix(rows);
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

    @Override
    public boolean isTruthy() {
        return mat.length > 0;
    }

    @Override
    public String toString() {
        if (mat.length == 0) {
            return "[]";
        }

        final StringBuilder sb = new StringBuilder().append('[');
        for (final NtValue[] col : mat) {
            final String colstr = Arrays.toString(col);
            sb.append(colstr.substring(1, colstr.length() - 1)).append(';').append('\n').append(' ');
        }
        sb.deleteCharAt(sb.length() - 1).setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }
}
