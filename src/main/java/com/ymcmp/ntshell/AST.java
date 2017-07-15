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

import java.io.Serializable;

/**
 *
 * @author YTENG
 */
public abstract class AST implements Serializable, NtValue {

    public abstract <T> T accept(Visitor<T> vis);

    public AST ruleRewrite() {
        return this
                .transformNegatives()
                .levelOperators()
                .simplifyRationals()
                .unfoldConstant();
    }

    public AST transformNegatives() {
        return this;
    }

    public AST levelOperators() {
        return this;
    }

    public AST simplifyRationals() {
        return this;
    }

    public AST unfoldConstant() {
        return this;
    }

    @Override
    public NtValue applyAdd(NtValue rhs) {
        throw new UnsupportedOperationException("Illegal operation on syntax tree");
    }

    @Override
    public NtValue applyCall(NtValue... params) {
        throw new UnsupportedOperationException("Illegal operation on syntax tree");
    }

    @Override
    public NtValue applyDiv(NtValue rhs) {
        throw new UnsupportedOperationException("Illegal operation on syntax tree");
    }

    @Override
    public NtValue applyMod(NtValue rhs) {
        throw new UnsupportedOperationException("Illegal operation on syntax tree");
    }

    @Override
    public NtValue applyMul(NtValue rhs) {
        throw new UnsupportedOperationException("Illegal operation on syntax tree");
    }

    @Override
    public NtValue applyNegative() {
        throw new UnsupportedOperationException("Illegal operation on syntax tree");
    }

    @Override
    public NtValue applyPercentage() {
        throw new UnsupportedOperationException("Illegal operation on syntax tree");
    }

    @Override
    public NtValue applyPositive() {
        throw new UnsupportedOperationException("Illegal operation on syntax tree");
    }

    @Override
    public NtValue applyPow(NtValue rhs) {
        throw new UnsupportedOperationException("Illegal operation on syntax tree");
    }

    @Override
    public NtValue applySub(NtValue rhs) {
        throw new UnsupportedOperationException("Illegal operation on syntax tree");
    }

    @Override
    public boolean isTruthy() {
        // All syntax trees are true by default
        return true;
    }
}
