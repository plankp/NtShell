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
public class NumberVal implements AST {

    private static final class NumberValHelper {

        public static final NumberVal ZERO = new NumberVal(new Token(Token.Type.NUMBER, "0"));
        public static final NumberVal ONE = new NumberVal(new Token(Token.Type.NUMBER, "1"));
        public static final NumberVal NEG_ONE = new NumberVal(new Token(Token.Type.NUMBER, "-1"));
    }

    public final Token val;

    public NumberVal(Token val) {
        if (val.type != Token.Type.NUMBER) {
            throw new IllegalArgumentException("NumberVal expects a number, found " + val);
        }
        this.val = val;
    }

    @Override
    public <T> T accept(Visitor<T> vis) {
        return vis.visitNumberVal(this);
    }

    @Override
    public String toString() {
        return val.toString();
    }

    public double toDouble() {
        if (val.text.equalsIgnoreCase("infinity")) {
            return Double.POSITIVE_INFINITY;
        }
        if (val.text.equalsIgnoreCase("nan")) {
            return Double.NaN;
        }

        if (val.text.contains(".")) {
            return Double.parseDouble(val.text);
        }
        if (val.text.length() > 1) {
            switch (val.text.charAt(1)) {
            case 'b':
                return Long.parseLong(val.text.substring(2), 2);
            case 'c':
                return Long.parseLong(val.text.substring(2), 8);
            case 'd':
                return Long.parseLong(val.text.substring(2), 10);
            case 'x':
                return Long.parseLong(val.text.substring(2), 16);
            }
        }
        return Long.parseLong(val.text);
    }

    public static NumberVal fromDouble(final double d) {
        if (d == -1) {
            return NumberValHelper.NEG_ONE;
        }
        if (d == 0) {
            return NumberValHelper.ZERO;
        }
        if (d == 1) {
            return NumberValHelper.ONE;
        }
        return new NumberVal(new Token(Token.Type.NUMBER, Double.toString(d)));
    }

    public static NumberVal fromLong(final long i) {
        if (i == -1) {
            return NumberValHelper.NEG_ONE;
        }
        if (i == 0) {
            return NumberValHelper.ZERO;
        }
        if (i == 1) {
            return NumberValHelper.ONE;
        }
        return new NumberVal(new Token(Token.Type.NUMBER, Long.toString(i)));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.val);
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
        final NumberVal other = (NumberVal) obj;
        return this.toDouble() == other.toDouble();
    }
}
