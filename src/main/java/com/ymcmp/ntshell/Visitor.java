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
package com.ymcmp.ntshell;

import com.ymcmp.ntshell.ast.*;

/**
 *
 * @author YTENG
 * @param <T>
 */
public abstract class Visitor<T> {

    public T visit(final AST node) {
        if (node == null) {
            return null;
        }
        return node.accept(this);
    }

    public abstract T visitAtomVal(AtomVal atom);

    public abstract T visitNumberVal(NumberVal number);

    public abstract T visitVariableVal(VariableVal variable);

    public abstract T visitMatrixVal(MatrixVal matrix);

    public abstract T visitAnonFuncVal(AnonFuncVal anonFunc);

    public abstract T visitPiecewiseFuncVal(PiecewiseFuncVal piecewiseFunc);

    public abstract T visitApplyExpr(ApplyExpr apply);

    public abstract T visitPartialApplyExpr(PartialApplyExpr apply);

    public abstract T visitUnaryExpr(UnaryExpr unary);

    public abstract T visitBinaryExpr(BinaryExpr binary);

    public abstract T visitCommutativeExpr(CommutativeExpr commutative);

    public abstract T visitAssignExpr(AssignExpr assign);
}
