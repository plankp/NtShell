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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author YTENG
 */
public class BinaryExpr implements AST {

    public final AST lhs;
    public final AST rhs;
    public final Token op;

    public BinaryExpr(AST lhs, AST rhs, Token op) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.op = op;
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitBinaryExpr(this);
    }

    @Override
    public String toString() {
        return String.format("binary{ lhs:%s, op:%s, rhs:%s }", lhs, op, rhs);
    }

    @Override
    public AST transformNegatives() {
        final AST nlhs = lhs.transformNegatives();
        final AST nrhs = rhs.transformNegatives();

        switch (op.type) {
        case SUB:
            // (- a b) => (+ a (* -1 b))
            return new BinaryExpr(nlhs, new BinaryExpr(NumberVal.fromLong(-1), nrhs, new Token(Token.Type.MUL, null)), new Token(Token.Type.ADD, null));
        }
        return new BinaryExpr(nlhs, nrhs, op);
    }

    @Override
    public AST levelOperators() {
        final AST nlhs = lhs.levelOperators();
        final AST nrhs = rhs.levelOperators();

        switch (op.type) {
        case ADD:
        case MUL:
            // (+ (+ a b) (+ c d)) => (+ a b (+ c d)) => (+ a b c d)
            final List<AST> nodes = new ArrayList<>();
            if (nlhs instanceof CommutativeExpr) {
                final CommutativeExpr comLhs = (CommutativeExpr) nlhs;
                if (comLhs.op.type == op.type) {
                    // pull up
                    nodes.addAll(Arrays.asList(comLhs.nodes));
                } else {
                    nodes.add(comLhs);
                }
            } else if (nlhs instanceof BinaryExpr) {
                final BinaryExpr binLhs = (BinaryExpr) nlhs;
                if (binLhs.op.type == op.type) {
                    // pull up
                    nodes.addAll(Arrays.asList(binLhs.lhs));
                } else {
                    nodes.add(binLhs);
                }
            } else {
                nodes.add(nlhs);
            }

            if (nrhs instanceof CommutativeExpr) {
                final CommutativeExpr comRhs = (CommutativeExpr) nrhs;
                if (comRhs.op.type == op.type) {
                    // pull up
                    nodes.addAll(Arrays.asList(comRhs.nodes));
                } else {
                    nodes.add(comRhs);
                }
            } else if (nrhs instanceof BinaryExpr) {
                final BinaryExpr binRhs = (BinaryExpr) nrhs;
                if (binRhs.op.type == op.type) {
                    // pull up
                    nodes.addAll(Arrays.asList(binRhs.lhs));
                } else {
                    nodes.add(binRhs);
                }
            } else {
                nodes.add(nrhs);
            }

            return new CommutativeExpr(nodes.toArray(new AST[nodes.size()]), op);
        }
        return new BinaryExpr(nlhs, nrhs, op);
    }

    @Override
    public AST simplifyRationals() {
        final AST nlhs = lhs.simplifyRationals();
        final AST nrhs = rhs.simplifyRationals();

        switch (op.type) {
        case MUL:
            // (* a (/ b c)) => (/ (* a b) c)
            if (nrhs instanceof BinaryExpr) {
                final BinaryExpr binRhs = (BinaryExpr) nrhs;
                if (binRhs.op.type == Token.Type.DIV) {
                    return new BinaryExpr(new BinaryExpr(nlhs, binRhs.lhs, op), binRhs.rhs, binRhs.op);
                }
            }
            break;
        case DIV:
            // (/ (/ a b) c) => (/ a (* b c))   [0]
            // (/ a (/ b c)) => (/ (* a b) c)   [1]
            if (nlhs instanceof BinaryExpr) {
                final BinaryExpr binLhs = (BinaryExpr) nlhs;
                if (binLhs.op.type == Token.Type.DIV) {
                    // [0]
                    return new BinaryExpr(binLhs.lhs, new BinaryExpr(binLhs.rhs, nrhs, new Token(Token.Type.MUL, null)), op);
                }
            }
            if (nrhs instanceof BinaryExpr) {
                final BinaryExpr binRhs = (BinaryExpr) nrhs;
                if (binRhs.op.type == Token.Type.DIV) {
                    // [1]
                    return new BinaryExpr(new BinaryExpr(nlhs, binRhs.lhs, new Token(Token.Type.MUL, null)), binRhs.rhs, op);
                }
            }
            break;
        }

        return new BinaryExpr(nlhs, nrhs, op);
    }

    private AST promote() {
        switch (op.type) {
        case ADD:
        case MUL:
            return new CommutativeExpr(new AST[]{lhs, rhs}, op);
        }
        return this;
    }

    @Override
    public AST unfoldConstant() {
        final AST nthis = this.promote();
        if (nthis instanceof CommutativeExpr) {
            return nthis.unfoldConstant();
        }
        final BinaryExpr expr = (BinaryExpr) nthis;
        final AST nlhs = expr.lhs.unfoldConstant();
        final AST nrhs = expr.rhs.unfoldConstant();

        switch (expr.op.type) {
        case MOD:
            // (mod a 1) => 0
            if (nrhs.equals(NumberVal.fromLong(1))) {
                return NumberVal.fromLong(0);
            }
            break;
        case POW:
            // (^ a 0)  => 1
            // (^ 1 a)  => 1
            // (^ 0 a)  => 0
            // (^ a 1)  => a
            if (nrhs.equals(NumberVal.fromLong(0))) {
                return NumberVal.fromLong(1);
            }
            if (nlhs.equals(NumberVal.fromLong(1))) {
                return NumberVal.fromLong(1);
            }
            if (nlhs.equals(NumberVal.fromLong(0))) {
                return NumberVal.fromLong(0);
            }
            if (nrhs.equals(NumberVal.fromLong(1))) {
                return nlhs;
            }
            break;
        case SUB:
            // (- a a) => 0
            // (- a 0) => a
            // (- 0 a) => -a
            if (nlhs.equals(nrhs)) {
                return NumberVal.fromLong(0);
            }
            if (nrhs.equals(NumberVal.fromLong(0))) {
                return nlhs;
            }
            if (nlhs.equals(NumberVal.fromLong(0))) {
                return new UnaryExpr(nrhs, op, true);
            }
            break;
        case DIV:
            // (/ a a)  => 1
            // (/ a 1)  => a
            // (/ a 0)  => Inf
            // (/ a -0) => -Inf
            // (/ 0 0)  => NaN
            // (/ (* 3 a) 3)  => a
            // (/ (* 3 a) a)  => 3
            if (nlhs.equals(nrhs)) {
                if (nlhs.equals(NumberVal.fromLong(0))) {
                    return NumberVal.fromDouble(Double.NaN);
                }
                return NumberVal.fromLong(1);
            }
            if (nrhs.equals(NumberVal.fromLong(1))) {
                return nlhs;
            }
            if (nrhs.equals(NumberVal.fromLong(0))) {
                return NumberVal.fromDouble(Double.POSITIVE_INFINITY);
            }
            break;
        }

        return new BinaryExpr(nlhs, nrhs, op);
    }

    @Override
    public int compareTo(AST o) {
        if (o instanceof BinaryExpr || o instanceof CommutativeExpr || o instanceof UnaryExpr) {
            return 0;
        }
        return 1;
    }
}
