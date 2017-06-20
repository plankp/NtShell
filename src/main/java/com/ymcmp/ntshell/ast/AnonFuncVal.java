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

import java.util.stream.Collectors;

/**
 *
 * @author YTENG
 */
public class AnonFuncVal implements AST {

    public final Token[] inputs;
    public final AST output;

    public AnonFuncVal(Token inputs, AST output) {
        this(new Token[]{inputs}, output);
    }

    public AnonFuncVal(Token[] inputs, AST output) {
        for (final Token t : inputs) {
            if (t.type != Token.Type.IDENT) {
                throw new IllegalArgumentException("input must be an identifier, found " + t);
            }
        }
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitAnonFuncVal(this);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("anonFunc{ inputs: ")
                .append(Arrays.stream(inputs)
                        .map(t -> t.text)
                        .collect(Collectors.joining(", ", "[", "]")))
                .append(", output: ").append(output).append(" }").toString();
    }

    public AnonFuncVal uncurry() {
        // x -> y -> x + y =>
        // (x, y) -> x + y
        if (this.output instanceof AnonFuncVal) {
            final List<Token> tokens = new ArrayList<>(Arrays.asList(inputs));
            AST tail = this.output;
            while (tail instanceof AnonFuncVal) {
                final AnonFuncVal g = (AnonFuncVal) tail;
                tokens.addAll(Arrays.asList(g.inputs));
                tail = g.output;
            }
            return new AnonFuncVal(tokens.toArray(new Token[tokens.size()]), tail);
        }
        return this;
    }

    public AnonFuncVal curry() {
        if (this.inputs.length < 2) {
            return this;
        }
        // (x, y) -> x + y =>
        // x -> y -> x + y
        AnonFuncVal ret = null;
        for (int i = inputs.length - 1; i >= 0; --i) {
            if (ret == null) {
                ret = new AnonFuncVal(inputs[i], output);
            } else {
                ret = new AnonFuncVal(inputs[i], ret);
            }
        }
        return ret;
    }

    public AnonFuncVal recursiveCurry() {
        // (a, b) -> (x, y) -> a * x + b * y =>
        // a -> b -> x -> y -> a * x + b * y
        final AnonFuncVal f = this.curry();
        if (f.output instanceof AnonFuncVal) {
            return new AnonFuncVal(f.inputs, ((AnonFuncVal) f.output).recursiveCurry());
        }
        return f;
    }

    @Override
    public AST transformNegatives() {
        final AST out = this.output.transformNegatives();
        if (out == this.output) {
            return this;
        }
        return new AnonFuncVal(inputs, out);
    }

    @Override
    public AST levelOperators() {
        final AST out = this.output.levelOperators();
        if (out == this.output) {
            return this;
        }
        return new AnonFuncVal(inputs, out);
    }

    @Override
    public AST simplifyRationals() {
        final AST out = this.output.simplifyRationals();
        if (out == this.output) {
            return this;
        }
        return new AnonFuncVal(inputs, out);
    }

    @Override
    public AST unfoldConstant() {
        final AST out = this.output.toCanonicalOrder().unfoldConstant();
        if (out == this.output) {
            return this;
        }
        return new AnonFuncVal(inputs, out);
    }

    @Override
    public AST toCanonicalOrder() {
        final AST out = this.output.toCanonicalOrder();
        if (out == this.output) {
            return this;
        }
        return new AnonFuncVal(inputs, out);
    }

    @Override
    public int compareTo(AST o) {
        return 0;
    }
}
