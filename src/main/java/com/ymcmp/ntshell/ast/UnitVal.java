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

/**
 *
 * @author YTENG
 */
public final class UnitVal extends AST {

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitUnitVal(this);
    }

    @Override
    public String toString() {
        return "()";
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(final Object val) {
        if (val == null) {
            return false;
        }
        return val.getClass() == this.getClass();
    }
}
