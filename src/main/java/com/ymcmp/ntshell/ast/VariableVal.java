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
import java.util.Objects;

/**
 *
 * @author YTENG
 */
public class VariableVal implements AST {

    public final Token val;

    public VariableVal(Token val) {
        if (val.type != Token.Type.IDENT) {
            throw new IllegalArgumentException("VariableVal expects an identifier, found " + val);
        }
        this.val = val;
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitVariableVal(this);
    }

    @Override
    public String toString() {
        return val.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.val);
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
        final VariableVal other = (VariableVal) obj;
        return Objects.equals(this.val, other.val);
    }

    @Override
    public int compareTo(AST o) {
        if (o instanceof NumberVal) {
            return 1;
        }
        if (o instanceof VariableVal) {
            return this.val.text.compareTo(((VariableVal) o).val.text);
        }
        return -1;
    }
}
