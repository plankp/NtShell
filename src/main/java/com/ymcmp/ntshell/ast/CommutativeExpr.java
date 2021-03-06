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
public class CommutativeExpr extends AST {

    public final AST[] nodes;
    public final Token op;

    public CommutativeExpr(AST[] nodes, Token op) {
        this.nodes = nodes;
        this.op = op;
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitCommutativeExpr(this);
    }

    @Override
    public String toString() {
        return String.format("commutative{ nodes: %s, op: %s }", Arrays.toString(nodes), op);
    }

    @Override
    public AST transformNegatives() {
        final AST[] nnodes = new AST[nodes.length];
        for (int i = 0; i < nodes.length; ++i) {
            nnodes[i] = nodes[i].transformNegatives();
        }
        return new CommutativeExpr(nnodes, op);
    }

    @Override
    public AST levelOperators() {
        final List<AST> nnodes = new ArrayList<>(nodes.length);
        for (int i = 0; i < nodes.length; ++i) {
            final AST node = nodes[i].levelOperators();
            if (node instanceof CommutativeExpr) {
                final CommutativeExpr comNode = (CommutativeExpr) node;
                if (comNode.op.type == op.type) {
                    // pull up
                    nnodes.addAll(Arrays.asList(comNode.nodes));
                } else {
                    nnodes.add(node);
                }
            } else if (node instanceof BinaryExpr) {
                final BinaryExpr binNode = (BinaryExpr) node;
                if (binNode.op.type == op.type) {
                    // pull up
                    nnodes.add(binNode.lhs);
                    nnodes.add(binNode.rhs);
                } else {
                    nnodes.add(node);
                }
            } else {
                nnodes.add(node);
            }
        }
        return new CommutativeExpr(nnodes.toArray(new AST[nnodes.size()]), op);
    }

    @Override
    public AST simplifyRationals() {
        final List<AST> nnodes = new ArrayList<>(nodes.length);
        for (int i = 0; i < nodes.length; ++i) {
            nnodes.add(nodes[i].simplifyRationals());
        }
        final CommutativeExpr ret = new CommutativeExpr(nnodes.toArray(new AST[nnodes.size()]), op);
        if (op.type == Token.Type.MUL) {
            // (* a (/ b c))         => (/ (* a b) c)
            // (* a (/ b c) (/ d e)) => (/ (* a b (/ d e)) c) => (/ (/ (* a b d) e) c)
            for (int i = 1; i < ret.nodes.length; ++i) {
                if (ret.nodes[i] instanceof BinaryExpr) {
                    final BinaryExpr e = (BinaryExpr) ret.nodes[i];
                    if (e.op.type == Token.Type.DIV) {
                        ret.nodes[i] = e.lhs;
                        return new BinaryExpr(ret, e.rhs, e.op).simplifyRationals();
                    }
                }
            }
        }
        return ret;
    }

    private AST unfoldUnarySub() {
        if (nodes.length == 2
                && op.type == Token.Type.MUL
                && nodes[0].equals(NumberVal.fromLong(-1))) {
            // (* -1 a) => (- a)
            return new UnaryExpr(nodes[1], new Token(Token.Type.SUB, null), true);
        }
        return this;
    }

    @Override
    public AST unfoldConstant() {
        final AST[] nnodes = new AST[nodes.length];
        for (int i = 0; i < nnodes.length; ++i) {
            nnodes[i] = nodes[i].unfoldConstant();
        }

        switch (op.type) {
        case MUL: {
            if (Arrays.stream(nnodes).anyMatch(e -> {
                if (e instanceof NumberVal) {
                    return ((NumberVal) e).toDouble() == 0;
                }
                return false;
            })) {
                // (* a b 0 c) => (0)
                return NumberVal.fromLong(0);
            }

            // (* a b 1 c) => (* a b c)
            final AST[] t = Arrays.stream(nnodes).filter(e -> {
                if (e instanceof NumberVal) {
                    return ((NumberVal) e).toDouble() != 1;
                }
                return true;
            }).toArray(AST[]::new);
            if (t.length == 1) {
                return t[0];
            }
            return new CommutativeExpr(t, op).unfoldUnarySub();
        }
        case ADD: {
            // (+ a b 0 c) => (+ a b c)
            final AST[] t = Arrays.stream(nnodes).filter(e -> {
                if (e instanceof NumberVal) {
                    return ((NumberVal) e).toDouble() != 0;
                }
                return true;
            }).toArray(AST[]::new);
            if (t.length == 1) {
                return t[0];
            }
            return new CommutativeExpr(nnodes, op);
        }
        default:
        }
        return new CommutativeExpr(nnodes, op);
    }
}
