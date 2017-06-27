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
import java.util.Objects;

/**
 *
 * @author YTENG
 */
public class PartialApplyExpr implements AST {

    public final AST[] placeholders;
    public final AST applicant;

    public PartialApplyExpr(AST[] placeholders, AST applicant) {
        this.placeholders = placeholders;
        this.applicant = applicant;
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitPartialApplyExpr(this);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Arrays.deepHashCode(this.placeholders);
        hash = 83 * hash + Objects.hashCode(this.applicant);
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
        final PartialApplyExpr other = (PartialApplyExpr) obj;
        if (!Objects.equals(this.applicant, other.applicant)) {
            return false;
        }
        return Arrays.deepEquals(this.placeholders, other.placeholders);
    }

    @Override
    public String toString() {
        return String.format("partialApply{ placeholders: %s, applicant: %s }",
                             Arrays.toString(placeholders),
                             applicant);
    }

    @Override
    public AST transformNegatives() {
        final AST[] np = Arrays.stream(placeholders)
                .map(AST::transformNegatives)
                .toArray(AST[]::new);
        final AST na = applicant.transformNegatives();
        return new PartialApplyExpr(np, na);
    }

    @Override
    public AST levelOperators() {
        final AST[] np = Arrays.stream(placeholders)
                .map(AST::levelOperators)
                .toArray(AST[]::new);
        final AST na = applicant.levelOperators();
        return new PartialApplyExpr(np, na);
    }

    @Override
    public AST simplifyRationals() {
        final AST[] np = Arrays.stream(placeholders)
                .map(AST::simplifyRationals)
                .toArray(AST[]::new);
        final AST na = applicant.simplifyRationals();
        return new PartialApplyExpr(np, na);
    }

    @Override
    public AST unfoldConstant() {
        final AST[] np = Arrays.stream(placeholders)
                .map(AST::unfoldConstant)
                .toArray(AST[]::new);
        final AST na = applicant.unfoldConstant();
        return new PartialApplyExpr(np, na);
    }
}
