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
public class AssignExpr implements AST {

    public final Token to;
    public final AST value;

    public AssignExpr(Token to, AST value) {
        this.to = to;
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitAssignExpr(this);
    }

    @Override
    public String toString() {
        return String.format("assign{ to:%s, value:%s }", to.text, value);
    }

    @Override
    public AST transformNegatives() {
        final AST nval = value.transformNegatives();
        if (nval == value) {
            return this;
        }
        return new AssignExpr(to, nval);
    }

    @Override
    public AST levelOperators() {
        final AST nval = value.levelOperators();
        if (nval == value) {
            return this;
        }
        return new AssignExpr(to, nval);
    }

    @Override
    public AST simplifyRationals() {
        final AST nval = value.simplifyRationals();
        if (nval == value) {
            return this;
        }
        return new AssignExpr(to, nval);
    }

    @Override
    public AST unfoldConstant() {
        final AST nval = value.toCanonicalOrder().unfoldConstant();
        if (nval == value) {
            return this;
        }
        return new AssignExpr(to, nval);
    }

    @Override
    public int compareTo(AST o) {
        if (o instanceof AssignExpr) {
            return 0;
        }
        if (o instanceof NumberVal || o instanceof VariableVal) {
            return 1;
        }
        return -1;
    }
}
