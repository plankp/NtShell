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
import com.ymcmp.ntshell.Token;
import com.ymcmp.ntshell.Visitor;

/**
 *
 * @author YTENG
 */
public class UnaryExpr implements AST {

    public final AST base;
    public final Token op;

    public final boolean prefix;

    public UnaryExpr(AST base, Token op, boolean isPrefix) {
        this.base = base;
        this.op = op;
        this.prefix = isPrefix;
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitUnaryExpr(this);
    }

    @Override
    public String toString() {
        return String.format("unary{ prefix:%b, base:%s, op:%s }", prefix, base, op);
    }

    @Override
    public AST transformNegatives() {
        final AST nbase = base.transformNegatives();

        if (prefix) {
            switch (op.type) {
            case ADD:
                // (+ 10) => (10)
                return nbase;
            case SUB:
                // (- 10) => (* -1 10)
                return new BinaryExpr(NumberVal.fromLong(-1), nbase, new Token(Token.Type.MUL, null));
            default:
            }
        } else {
            switch (op.type) {
            case PERCENT:
                // (10 %) => (* 10 0.01)
                return new BinaryExpr(NumberVal.fromDouble(0.01), nbase, new Token(Token.Type.MUL, null));
            default:
            }
        }
        return AST.super.transformNegatives();
    }

    @Override
    public AST levelOperators() {
        final AST nbase = base.levelOperators();
        if (nbase == base) {
            return this;
        }
        return new UnaryExpr(nbase, op, prefix);
    }

    @Override
    public AST simplifyRationals() {
        final AST nbase = base.simplifyRationals();
        if (nbase == base) {
            return this;
        }
        return new UnaryExpr(nbase, op, prefix);
    }
}
