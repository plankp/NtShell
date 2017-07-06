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
public final class DoEndExpr implements AST {

    public final AST[] exprs;

    public DoEndExpr(final AST... exprs) {
        this.exprs = exprs;
    }

    @Override
    public <T> T accept(final Visitor<T> vis) {
        return vis.visitDoEndExpr(this);
    }

    @Override
    public String toString() {
        return "doEnd{ exprs:" + Arrays.toString(exprs) + " }";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Arrays.deepHashCode(this.exprs);
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
        final DoEndExpr other = (DoEndExpr) obj;
        return Arrays.deepEquals(this.exprs, other.exprs);
    }

    @Override
    public AST levelOperators() {
        return new DoEndExpr(Arrays.stream(exprs)
                .map(AST::levelOperators).toArray(AST[]::new));
    }

    @Override
    public AST simplifyRationals() {
        return new DoEndExpr(Arrays.stream(exprs)
                .map(AST::simplifyRationals).toArray(AST[]::new));
    }

    @Override
    public AST transformNegatives() {
        return new DoEndExpr(Arrays.stream(exprs)
                .map(AST::transformNegatives).toArray(AST[]::new));
    }

    @Override
    public AST unfoldConstant() {
        return new DoEndExpr(Arrays.stream(exprs)
                .map(AST::unfoldConstant).toArray(AST[]::new));
    }
}
