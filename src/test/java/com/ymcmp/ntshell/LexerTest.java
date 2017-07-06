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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author YTENG
 */
public class LexerTest {

    @Test
    public void testIsBinary() {
        for (char c = 0; c < 255; ++c) {
            // only 0 and 1 is binary
            assertEquals(c == '0' || c == '1', Lexer.isBinary(c));
        }
    }

    @Test
    public void testIsDecimal() {
        for (char c = 0; c < 255; ++c) {
            // only 0 to 9 is decimal
            assertEquals(c >= '0' && c <= '9', Lexer.isDecimal(c));
        }
    }

    @Test
    public void testIsOctal() {
        for (char c = 0; c < 255; ++c) {
            // only 0 to 7 is octal
            assertEquals(c >= '0' && c <= '7', Lexer.isOctal(c));
        }
    }

    @Test
    public void testIsHex() {
        for (char c = 0; c < 255; ++c) {
            // only 0 to 9 and a to f is hex
            assertEquals((c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'f')
                    || (c >= 'A' && c <= 'F'), Lexer.isHexadecimal(c));
        }
    }

    @Test
    public void testIsIdent() {
        for (char c = 0; c < 255; ++c) {
            // 0 to 9, a to z, A to Z, underscore, dollar sign, single quote
            assertEquals((c >= '0' && c <= '9')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c == '_')
                    || (c == '$')
                    || (c == '\''), Lexer.isIdent(c));
        }
    }

    @Test
    public void testLexAtom() {
        final char[] str = "@atom ".toCharArray();
        final List<Token> arr = new ArrayList<>();
        final int end = Lexer.lexAtom(0, str, arr);
        assertEquals(4, end);
        assertEquals(Arrays.asList(new Token(Token.Type.ATOM, "@atom")), arr);
        assertEquals(1, Lexer.lexAtom(1, str, arr));
    }

    @Test
    public void testLexQuotedAtom() {
        final char[] str = "@\"ab\"".toCharArray();
        final List<Token> arr = new ArrayList<>();
        final int end = Lexer.lexAtom(0, str, arr);
        assertEquals(4, end);
        assertEquals(Arrays.asList(new Token(Token.Type.ATOM, "@\"ab\"")), arr);
    }

    @Test
    public void testLexIdent() {
        final char[] str = " atom".toCharArray();
        final List<Token> arr = new ArrayList<>();
        final int end = Lexer.lexIdentifier(1, str, arr);
        assertEquals(4, end);
        assertEquals(Arrays.asList(new Token(Token.Type.IDENT, "atom")), arr);
        assertEquals(0, Lexer.lexIdentifier(0, str, arr));
    }

    @Test
    public void testLexNumber() {
        final char[] str = " 123 1.23".toCharArray();
        final List<Token> arr = new ArrayList<>();
        int end = Lexer.lexNumber(1, str, arr);
        assertEquals(3, end);
        assertEquals(Arrays.asList(new Token(Token.Type.NUMBER, "123")), arr);

        arr.clear();
        end = Lexer.lexNumber(5, str, arr);
        assertEquals(8, end);
        assertEquals(Arrays.asList(new Token(Token.Type.NUMBER, "1.23")), arr);

        assertEquals(0, Lexer.lexNumber(0, str, arr));
    }

    @Test
    public void testLexZeroPrefixedNumber() {
        final char[] str = " 0 0b0010 0c775 0d012 0xabcd".toCharArray();
        final List<Token> arr = new ArrayList<>();
        int end = Lexer.lexZeroPrefixedNumber(1, str, arr);
        assertEquals(1, end);
        assertEquals(Arrays.asList(new Token(Token.Type.NUMBER, "0")), arr);

        arr.clear();
        end = Lexer.lexZeroPrefixedNumber(3, str, arr);
        assertEquals(8, end);
        assertEquals(Arrays.asList(new Token(Token.Type.NUMBER, "0b0010")), arr);

        arr.clear();
        end = Lexer.lexZeroPrefixedNumber(10, str, arr);
        assertEquals(14, end);
        assertEquals(Arrays.asList(new Token(Token.Type.NUMBER, "0c775")), arr);

        arr.clear();
        end = Lexer.lexZeroPrefixedNumber(16, str, arr);
        assertEquals(20, end);
        assertEquals(Arrays.asList(new Token(Token.Type.NUMBER, "0d012")), arr);

        arr.clear();
        end = Lexer.lexZeroPrefixedNumber(22, str, arr);
        assertEquals(27, end);
        assertEquals(Arrays.asList(new Token(Token.Type.NUMBER, "0xabcd")), arr);

        assertEquals(0, Lexer.lexZeroPrefixedNumber(0, str, arr));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLexingNullString() {
        try {
            Lexer.lexFromString(null);
        } catch (LexerException ex) {
        }
        fail("Illegal argument exception should have been thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLexingNullReader() {
        try {
            Lexer.lexFromReader(null);
        } catch (LexerException ex) {
        }
        fail("Illegal argument exception should have been thrown");
    }

    @Test
    public void testLexingIllegalCharacter() {
        try {
            Lexer.lexFromString("`");
            fail("LexerException should have been thrown");
        } catch (LexerException ex) {
        }
    }

    @Test
    public void testLexingAllPossibleTokens() {
        final String source = "# Comment\n\t\r{[(<;,.>)]}:+-*/%=-><=>=@atom 01 0b0100 0c712 0d0 0xAFcd 0.1923 123abc123 and or if mod==/=^ do end";
        final Object[] expected = {
            // # Comment\n\t\r => Nothing
            // {[(<
            new Token(Token.Type.LCURL, "{"), new Token(Token.Type.LBLK, "["), new Token(Token.Type.LBRACE, "("), new Token(Token.Type.LT, "<"),
            // ;,.
            new Token(Token.Type.SEMI, ";"), new Token(Token.Type.COMMA, ","), new Token(Token.Type.COMPOSE, "."),
            // >)]}
            new Token(Token.Type.GT, ">"), new Token(Token.Type.RBRACE, ")"), new Token(Token.Type.RBLK, "]"), new Token(Token.Type.RCURL, "}"),
            // :+-*/%=
            new Token(Token.Type.SCOPE, ":"),
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
            // ==/=^
            new Token(Token.Type.EQL, "=="), new Token(Token.Type.NEQ, "/="), new Token(Token.Type.POW, "^"),
            // do end
            new Token(Token.Type.K_DO, "do"), new Token(Token.Type.K_END, "end")
        };
        try {
            final Object[] toks1 = Lexer.lexFromString(source).toArray();
            assertArrayEquals(expected, toks1);
            final Object[] toks2 = Lexer.lexFromReader(new StringReader(source)).toArray();
            assertArrayEquals(expected, toks2);
        } catch (LexerException ex) {
            fail("LexerException should not have been thrown");
        }
    }
}
