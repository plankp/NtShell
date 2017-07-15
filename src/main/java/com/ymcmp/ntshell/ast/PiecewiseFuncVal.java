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
public class PiecewiseFuncVal extends AST {

    public final CaseBlock[] cases;

    public static class CaseBlock {

        public final AST pred;
        public final AST expr;

        public CaseBlock(AST pred, AST expr) {
            this.pred = pred;
            this.expr = expr;
        }

        @Override
        public String toString() {
            return String.format("case{ pred:%s, expr:%s }", pred, expr);
        }

        public CaseBlock transformNegatives() {
            // Ignore the predicate
            final AST nexpr = expr.transformNegatives();
            if (nexpr.equals(expr)) {
                return this;
            }
            return new CaseBlock(pred, nexpr);
        }

        public CaseBlock levelOperators() {
            final AST nexpr = expr.levelOperators();
            if (nexpr.equals(expr)) {
                return this;
            }
            return new CaseBlock(pred, nexpr);
        }

        public CaseBlock simplifyRationals() {
            final AST nexpr = expr.simplifyRationals();
            if (nexpr.equals(expr)) {
                return this;
            }
            return new CaseBlock(pred, nexpr);
        }

        public CaseBlock unfoldConstant() {
            final AST nexpr = expr.unfoldConstant();
            if (nexpr.equals(expr)) {
                return this;
            }
            return new CaseBlock(pred, nexpr);
        }
    }

    public final static class ElseClause extends CaseBlock {

        public ElseClause(final AST expr) {
            // Relies on property of 1 being a truthful value
            super(NumberVal.fromLong(1), expr);
        }

        @Override
        public String toString() {
            return String.format("else:%s", expr);
        }
    }

    public PiecewiseFuncVal(CaseBlock[] cases) {
        this.cases = cases;
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitPiecewiseFuncVal(this);
    }

    @Override
    public String toString() {
        return String.format("piecewiseFunc{ cases: %s }", Arrays.toString(cases));
    }

    @Override
    public AST transformNegatives() {
        final CaseBlock[] ncases = new CaseBlock[cases.length];
        for (int i = 0; i < cases.length; ++i) {
            ncases[i] = cases[i].transformNegatives();
        }
        return new PiecewiseFuncVal(ncases);
    }

    @Override
    public AST levelOperators() {
        final CaseBlock[] ncases = new CaseBlock[cases.length];
        for (int i = 0; i < cases.length; ++i) {
            ncases[i] = cases[i].levelOperators();
        }
        return new PiecewiseFuncVal(ncases);
    }

    @Override
    public AST simplifyRationals() {
        final CaseBlock[] ncases = new CaseBlock[cases.length];
        for (int i = 0; i < cases.length; ++i) {
            ncases[i] = cases[i].simplifyRationals();
        }
        return new PiecewiseFuncVal(ncases);
    }

    @Override
    public AST unfoldConstant() {
        final CaseBlock[] ncases = new CaseBlock[cases.length];
        for (int i = 0; i < cases.length; ++i) {
            ncases[i] = cases[i].unfoldConstant();
        }
        return new PiecewiseFuncVal(ncases);
    }
}
