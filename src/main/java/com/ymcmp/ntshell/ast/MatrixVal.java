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
package com.ymcmp.ntshell.ast;

import com.ymcmp.ntshell.AST;
import com.ymcmp.ntshell.Visitor;

import java.util.Arrays;

/**
 *
 * @author YTENG
 */
public class MatrixVal extends AST {

    public final Column[] columns;

    public static class Column {

        public final AST[] row;

        public Column(AST[] row) {
            this.row = row;
        }

        @Override
        public String toString() {
            return String.format("column{ row:%s }", Arrays.toString(row));
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + Arrays.deepHashCode(this.row);
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
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Column other = (Column) obj;
            return Arrays.deepEquals(this.row, other.row);
        }

        public Column transformNegatives() {
            final AST[] nrow = new AST[row.length];
            for (int i = 0; i < nrow.length; ++i) {
                nrow[i] = row[i].transformNegatives();
            }
            return new Column(nrow);
        }

        public Column levelOperators() {
            final AST[] nrow = new AST[row.length];
            for (int i = 0; i < nrow.length; ++i) {
                nrow[i] = row[i].levelOperators();
            }
            return new Column(nrow);
        }

        public Column simplifyRationals() {
            final AST[] nrow = new AST[row.length];
            for (int i = 0; i < nrow.length; ++i) {
                nrow[i] = row[i].simplifyRationals();
            }
            return new Column(nrow);
        }

        public Column unfoldConstant() {
            final AST[] nrow = new AST[row.length];
            for (int i = 0; i < nrow.length; ++i) {
                nrow[i] = row[i].unfoldConstant();
            }
            return new Column(nrow);
        }
    }

    public MatrixVal(Column[] columns) {
        this.columns = columns;
    }

    public AST getCell(int row, int column) {
        return columns[column].row[row];
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitMatrixVal(this);
    }

    @Override
    public AST transformNegatives() {
        final Column[] ncolumns = new Column[columns.length];
        for (int i = 0; i < columns.length; ++i) {
            ncolumns[i] = columns[i].transformNegatives();
        }
        return new MatrixVal(ncolumns);
    }

    @Override
    public AST levelOperators() {
        final Column[] ncolumns = new Column[columns.length];
        for (int i = 0; i < columns.length; ++i) {
            ncolumns[i] = columns[i].levelOperators();
        }
        return new MatrixVal(ncolumns);
    }

    @Override
    public AST simplifyRationals() {
        final Column[] ncolumns = new Column[columns.length];
        for (int i = 0; i < columns.length; ++i) {
            ncolumns[i] = columns[i].simplifyRationals();
        }
        return new MatrixVal(ncolumns);
    }

    @Override
    public AST unfoldConstant() {
        final Column[] ncolumns = new Column[columns.length];
        for (int i = 0; i < columns.length; ++i) {
            ncolumns[i] = columns[i].unfoldConstant();
        }
        return new MatrixVal(ncolumns);
    }

    @Override
    public String toString() {
        return String.format("matrix{ columns:%s }", Arrays.toString(columns));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Arrays.deepHashCode(this.columns);
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MatrixVal other = (MatrixVal) obj;
        return Arrays.deepEquals(this.columns, other.columns);
    }
}
