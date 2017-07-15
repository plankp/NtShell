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
import java.util.ArrayDeque;

import java.util.Arrays;
import java.util.Deque;
import java.util.function.Function;

/**
 *
 * @author YTENG
 */
public class ApplyExpr implements AST {

    public final AST instance;
    public final AST[] params;

    public ApplyExpr(AST instance, AST[] params) {
        this.instance = instance;
        this.params = params;
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitApplyExpr(this);
    }

    @Override
    public String toString() {
        return String.format("apply{ instance: %s, params: %s }", instance, Arrays.toString(params));
    }

    public AST wrapLeftMostApplicant(Function<? super AST, ? extends AST> transformer) {
        final Deque<AST[]> rewriteDeque = new ArrayDeque<>();
        AST apply = this;
        do {
            final ApplyExpr e = (ApplyExpr) apply;
            apply = e.instance;
            rewriteDeque.addFirst(e.params);
        } while (apply instanceof ApplyExpr);

        // XXX: DO NOT CHANGE stream() TO parallelStream(). PARALLEL STREAMS
        // REQUIRE A PROPER COMBINATOR AND WE DO NOT HAVE ONE!
        return rewriteDeque.stream().reduce(transformer.apply(apply),
                                            ApplyExpr::new,
                                            (a, b) -> a);
    }

    @Override
    public AST transformNegatives() {
        final AST ninst = instance.transformNegatives();
        final AST[] nparams = Arrays.stream(params)
                .map(AST::transformNegatives)
                .toArray(AST[]::new);
        return new ApplyExpr(ninst, nparams);
    }

    @Override
    public AST levelOperators() {
        final AST ninst = instance.levelOperators();
        final AST[] nparams = Arrays.stream(params)
                .map(AST::levelOperators)
                .toArray(AST[]::new);
        return new ApplyExpr(ninst, nparams);
    }

    @Override
    public AST simplifyRationals() {
        final AST ninst = instance.simplifyRationals();
        final AST[] nparams = Arrays.stream(params)
                .map(AST::simplifyRationals)
                .toArray(AST[]::new);
        return new ApplyExpr(ninst, nparams);
    }

    @Override
    public AST unfoldConstant() {
        final AST ninst = instance.unfoldConstant();
        final AST[] nparams = Arrays.stream(params)
                .map(AST::unfoldConstant)
                .toArray(AST[]::new);
        return new ApplyExpr(ninst, nparams);
    }
}
