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

import java.util.Objects;

/**
 *
 * @author YTENG
 */
public class Token implements Serializable {

    private static final long serialVersionUID = 81734928356028769L;

    public final Type type;
    public final String text;

    private static class TokenHelper {

        public static final Token NIL_TOKEN = new Token(Type.S_EOF, null);
    }

    public enum Type {
        S_EOF,
        LCURL, RCURL, LBLK, RBLK, LBRACE, RBRACE,
        DECL, SET, EQL, NEQ, LT, LE, GE, GT,
        ADD, SUB, MUL, DIV, MOD, POW,
        PERCENT, QEXPR,
        SCOPE, SEMI, COMMA, YIELD, COMPOSE,
        K_AND, K_OR,
        K_IF, K_ELSE, K_DO, K_END, K_LAZY,
        IDENT, NUMBER, ATOM,
    }

    public Token(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", type, text);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.type);
        hash = 59 * hash + Objects.hashCode(this.text);
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
        final Token other = (Token) obj;
        if (this.type != other.type) {
            return false;
        }
        return Objects.equals(this.text, other.text);
    }

    public static Token getNilToken() {
        return TokenHelper.NIL_TOKEN;
    }
}
