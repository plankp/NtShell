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

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.function.Function;
import java.util.function.BiFunction;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            throw new MatrixBoundMismatchException();
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

    @Override
    public boolean isTruthy() {
        return mat.length > 0;
    }

    private LogicalLine toLogicalLine() {
        if (mat.length == 0) {
            return new LogicalLine(";");
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
        return outer;
    }

    @Override
    public String toString() {
        return toLogicalLine().toString();
    }
}

class LogicalLine implements Serializable {

    private static final long serialVersionUID = 209187243189L;

    public final String[] lines;

    public LogicalLine(String line) {
        lines = line.split("\n");
    }

    private LogicalLine(final String[] lines) {
        this.lines = lines;
    }

    public LogicalLine appendLine(final LogicalLine other) {
        /*
        1 2 append 3 4 yields  1 2
                              -----
                               3 4
         */
        final List<String> ret = new ArrayList<>(lines.length + other.lines.length + 1);
        final int sepLength = Stream.concat(Arrays.stream(lines), Arrays.stream(other.lines))
                .mapToInt(String::length)
                .max()
                .orElse(0);
        for (final String s : lines) {
            ret.add(String.format(" %-" + sepLength + "s ", s));
        }
        {
            final char[] rsep = new char[sepLength + 2];
            Arrays.fill(rsep, '-');
            ret.add(String.valueOf(rsep));
        }
        for (final String s : other.lines) {
            ret.add(String.format(" %-" + sepLength + "s ", s));
        }
        return new LogicalLine(ret.toArray(new String[ret.size()]));
    }

    public LogicalLine mergeWith(final LogicalLine other) {
        /*
        (0):
        1 2 merge 3 4 yields 1 2 | 3 4
        
        (1):
        1 2 merge 3   yields 1 2 | 3
                  4              | 4
        
        (2):
        1 merge 3 4   yields 1 | 3 4
        2                    2 |
         */
        if (lines.length == other.lines.length) {
            // (0) No padding in between strings, easiest case
            final String[] ret = new String[lines.length];
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = String.format("%s | %s", lines[i], other.lines[i]);
            }
            return new LogicalLine(ret);
        }
        if (lines.length < other.lines.length) {
            // (1) Padding on left side
            final String[] ret = new String[other.lines.length];
            final int padLength = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = String.format("%-" + padLength + "s | %s", i < lines.length ? lines[i] : "", other.lines[i]);
            }
            return new LogicalLine(ret);
        }
        // (2) Padding on right side
        final String[] ret = new String[lines.length];
        final int padLength = Arrays.stream(other.lines).mapToInt(String::length).max().orElse(0);
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = String.format("%s | %-" + padLength + "s", lines[i], i < other.lines.length ? other.lines[i] : "");
        }
        return new LogicalLine(ret);
    }

    @Override
    public String toString() {
        return Arrays.stream(lines).collect(Collectors.joining("\n"));
    }
}

class MatrixBoundMismatchException extends Exception {

    private static final String MSG = "Two matricies have different shapes";

    public MatrixBoundMismatchException() {
        super(MSG);
    }

    public DispatchException toDispatchException(final String dispatcher) {
        return new DispatchException(dispatcher, MSG);
    }
}
