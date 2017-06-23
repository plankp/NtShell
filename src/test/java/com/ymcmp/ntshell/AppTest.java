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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author YTENG
 */
public class AppTest {

    @Test(expected = IllegalArgumentException.class)
    public void testLexingNullString() {
        try {
            App.lexString(null, null);
        } catch (LexerException ex) {
        }
        fail("Illegal argument exception should have been thrown");
    }

    @Test
    public void testLexingAllPossibleTokens() {
        final String source = "# Comment\n\t\r{[(<;,.>)]}:+-*/%=-><=>=@atom 01 0b0100 0c712 0d0 0xAFcd 0.1923 123abc123 and or if mod==/=";
        final Object[] expected = {
            // # Comment\n\t\r => Nothing
            // {[(<
            new Token(Token.Type.LCURL, "{"), new Token(Token.Type.LBLK, "["), new Token(Token.Type.LBRACE, "("), new Token(Token.Type.LT, "<"),
            // ;,.
            new Token(Token.Type.SEMI, ";"), new Token(Token.Type.COMMA, ","), new Token(Token.Type.COMPOSE, "."),
            // >)]}
            new Token(Token.Type.GT, ">"), new Token(Token.Type.RBRACE, ")"), new Token(Token.Type.RBLK, "]"), new Token(Token.Type.RCURL, "}"),
            // :+-*/%=
            new Token(Token.Type.SCOPE, ":"), // this operator is currently not used
            new Token(Token.Type.ADD, "+"), new Token(Token.Type.SUB, "-"), new Token(Token.Type.MUL, "*"), new Token(Token.Type.DIV, "/"),
            new Token(Token.Type.PERCENT, "%"), new Token(Token.Type.SET, "="),
            // -><=>=
            new Token(Token.Type.YIELD, "->"), new Token(Token.Type.LE, "<="), new Token(Token.Type.GE, ">="),
            // @atom
            new Token(Token.Type.ATOM, "@atom"),
            // 01
            new Token(Token.Type.NUMBER, "0"), new Token(Token.Type.NUMBER, "1"),
            // 0b0100 0c712 0d0
            new Token(Token.Type.NUMBER, "0b0100"), new Token(Token.Type.NUMBER, "0c712"), new Token(Token.Type.NUMBER, "0d0"),
            // 0xAFcd 0.1923
            new Token(Token.Type.NUMBER, "0xAFcd"), new Token(Token.Type.NUMBER, "0.1923"),
            // 123abc123
            new Token(Token.Type.NUMBER, "123"), new Token(Token.Type.IDENT, "abc123"),
            // and or if mod
            new Token(Token.Type.K_AND, "and"), new Token(Token.Type.K_OR, "or"), new Token(Token.Type.K_IF, "if"), new Token(Token.Type.MOD, "mod"),
            // ==/=
            new Token(Token.Type.EQL, "=="), new Token(Token.Type.NEQ, "/=")
        };
        try {
            final Object[] toks = App.lexString(source, null).toArray();
            assertArrayEquals(expected, toks);
        } catch (LexerException ex) {
            fail("LexerException should not have been thrown");
        }
    }
}
