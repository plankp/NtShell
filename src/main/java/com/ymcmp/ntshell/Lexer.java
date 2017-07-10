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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author YTENG
 */
public class Lexer {

    private static final Map<String, Token.Type> KWORDS = new HashMap<>();

    static {
        KWORDS.put("mod", Token.Type.MOD);
        KWORDS.put("if", Token.Type.K_IF);
        KWORDS.put("else", Token.Type.K_ELSE);
        KWORDS.put("and", Token.Type.K_AND);
        KWORDS.put("or", Token.Type.K_OR);
        KWORDS.put("do", Token.Type.K_DO);
        KWORDS.put("end", Token.Type.K_END);
    }

    public static List<Token> lexFromReader(final Reader reader) throws LexerException {
        if (reader == null) {
            throw new IllegalArgumentException("Cannot lex from a null reader");
        }

        final BufferedReader br = new BufferedReader(reader);
        final List<Token> lst = new ArrayList<>();

        try {
            String s;
            while ((s = br.readLine()) != null) {
                lex(s, lst);
            }
        } catch (IOException ex) {
            // Return what we have so far
        }

        return lst;
    }

    public static List<Token> lexFromString(final String str) throws LexerException {
        final List<Token> tokens = new ArrayList<>();
        lex(str, tokens);
        return tokens;
    }

    private static void lex(final String str, final List<Token> tokens) throws LexerException {
        if (str == null) {
            throw new IllegalArgumentException("Cannot lex a null string");
        }

        char[] arr = str.toCharArray();
        for (int i = 0; i < arr.length; ++i) {
            switch (arr[i]) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break;
            case '#':
                while (i < arr.length && arr[i] != '\n') {
                    // skip until end of line or end of input
                    ++i;
                }
                break;
            case '{':
                tokens.add(new Token(Token.Type.LCURL, "{"));
                break;
            case '}':
                tokens.add(new Token(Token.Type.RCURL, "}"));
                break;
            case '[':
                tokens.add(new Token(Token.Type.LBLK, "["));
                break;
            case ']':
                tokens.add(new Token(Token.Type.RBLK, "]"));
                break;
            case '(':
                tokens.add(new Token(Token.Type.LBRACE, "("));
                break;
            case ')':
                tokens.add(new Token(Token.Type.RBRACE, ")"));
                break;
            case ';':
                tokens.add(new Token(Token.Type.SEMI, ";"));
                break;
            case ',':
                tokens.add(new Token(Token.Type.COMMA, ","));
                break;
            case '.':
                tokens.add(new Token(Token.Type.COMPOSE, "."));
                break;
            case ':':
                tokens.add(new Token(Token.Type.SCOPE, ":"));
                break;
            case '+':
                tokens.add(new Token(Token.Type.ADD, "+"));
                break;
            case '-':
                if (++i < arr.length && arr[i] == '>') {
                    tokens.add(new Token(Token.Type.YIELD, "->"));
                } else {
                    --i;
                    tokens.add(new Token(Token.Type.SUB, "-"));
                }
                break;
            case '*':
                tokens.add(new Token(Token.Type.MUL, "*"));
                break;
            case '/':
                if (++i < arr.length && arr[i] == '=') {
                    tokens.add(new Token(Token.Type.NEQ, "/="));
                } else {
                    --i;
                    tokens.add(new Token(Token.Type.DIV, "/"));
                }
                break;
            case '%':
                tokens.add(new Token(Token.Type.PERCENT, "%"));
                break;
            case '^':
                tokens.add(new Token(Token.Type.POW, "^"));
                break;
            case '=':
                if (++i < arr.length && arr[i] == '=') {
                    tokens.add(new Token(Token.Type.EQL, "=="));
                } else {
                    --i;
                    tokens.add(new Token(Token.Type.DECL, "="));
                }
                break;
            case '<':
                if (++i < arr.length && arr[i] == '=') {
                    tokens.add(new Token(Token.Type.LE, "<="));
                } else if (arr[i] == '-') {
                    tokens.add(new Token(Token.Type.SET, "<-"));
                } else {
                    --i;
                    tokens.add(new Token(Token.Type.LT, "<"));
                }
                break;
            case '>':
                if (++i < arr.length && arr[i] == '=') {
                    tokens.add(new Token(Token.Type.GE, ">="));
                } else {
                    --i;
                    tokens.add(new Token(Token.Type.GT, ">"));
                }
                break;
            case '@':
                try {
                    i = Lexer.lexAtom(i, arr, tokens);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    throw new LexerException("Unclosed atom literal");
                }
                break;
            case '0':
                i = Lexer.lexZeroPrefixedNumber(i, arr, tokens);
                break;
            default:
                if (Lexer.isDecimal(arr[i])) {
                    i = Lexer.lexNumber(i, arr, tokens);
                    break;
                }
                if (Lexer.isIdent(arr[i])) {
                    i = Lexer.lexIdOrKeyword(i, arr, tokens);
                    break;
                }
                throw new LexerException("Unrecognized character of `" + arr[i] + "'");
            }
        }
    }

    public static int lexAtom(final int pos, final char[] arr, final List<Token> tokens) {
        int i = pos;
        if (arr[i] == '@') {
            final StringBuilder buf = new StringBuilder().append(arr[i]);
            ++i;
            if (isIdent(arr[i])) {
                i = consumeIdentifier(i, arr, buf);
            } else if (arr[i] == '"') {
                // @"stuff" => we look for the next ". The quoting *not* nested
                buf.append('"');
                ++i;
                while (i < arr.length && arr[i] != '"') {
                    buf.append(arr[i++]);
                }
                // Append the last " also
                buf.append(arr[i]);
            }
            final String atom = buf.toString();
            tokens.add(new Token(Token.Type.ATOM, atom));
            return i;
        }
        return i;
    }

    public static int consumeIdentifier(final int pos, final char[] arr, final StringBuilder buf) {
        int i = pos;
        if (isIdent(arr[i])) {
            while (i < arr.length && isIdent(arr[i])) {
                buf.append(arr[i++]);
            }
            // Identifiers can end with ? or !
            if (i < arr.length) {
                switch (arr[i]) {
                case '?':
                case '!':
                    buf.append(arr[i++]);
                    break;
                default:
                }
            }
            --i;
        }
        return i;
    }

    public static int lexIdOrKeyword(final int pos, final char[] arr, final List<Token> tokens) {
        int i = pos;
        if (isIdent(arr[i])) {
            final StringBuilder buf = new StringBuilder();
            i = consumeIdentifier(i, arr, buf);
            final String ident = buf.toString();
            tokens.add(new Token(KWORDS.getOrDefault(ident, Token.Type.IDENT), ident));
        }
        return i;
    }

    public static int lexZeroPrefixedNumber(final int pos, final char[] arr, final List<Token> tokens) {
        int i = pos;
        if (arr[i] == '0') {
            final StringBuilder buf = new StringBuilder();
            buf.append('0');
            if (++i < arr.length) {
                switch (arr[i]) {
                case 'b':
                    // binary
                    buf.append(arr[i++]);
                    while (i < arr.length && isBinary(arr[i])) {
                        buf.append(arr[i++]);
                    }
                    break;
                case 'c':
                    // octal
                    buf.append(arr[i++]);
                    while (i < arr.length && isOctal(arr[i])) {
                        buf.append(arr[i++]);
                    }
                    break;
                case 'd':
                    // decimal
                    buf.append(arr[i++]);
                    while (i < arr.length && isDecimal(arr[i])) {
                        buf.append(arr[i++]);
                    }
                    break;
                case 'x':
                    // hexadecimal
                    buf.append(arr[i++]);
                    while (i < arr.length && isHexadecimal(arr[i])) {
                        buf.append(arr[i++]);
                    }
                    break;
                case '.':
                    // float-point
                    buf.append(arr[i++]);
                    while (i < arr.length && isDecimal(arr[i])) {
                        buf.append(arr[i++]);
                    }
                    break;
                default:
                }
            }
            --i;
            tokens.add(new Token(Token.Type.NUMBER, buf.toString()));
        }
        return i;
    }

    public static int lexNumber(final int pos, final char[] arr, final List<Token> tokens) {
        int i = pos;
        if (isDecimal(arr[i])) {
            final StringBuilder buf = new StringBuilder();
            while (i < arr.length && isDecimal(arr[i])) {
                buf.append(arr[i++]);
            }
            if (i < arr.length && arr[i] == '.') {
                buf.append(arr[i++]);
                while (i < arr.length && isDecimal(arr[i])) {
                    buf.append(arr[i++]);
                }
            }
            --i;
            tokens.add(new Token(Token.Type.NUMBER, buf.toString()));
        }
        return i;
    }

    public static boolean isHexadecimal(final char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    public static boolean isDecimal(final char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isBinary(final char c) {
        return c == '0' || c == '1';
    }

    public static boolean isOctal(final char c) {
        return c >= '0' && c <= '7';
    }

    public static boolean isIdent(final char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c == '_') || (c == '$') || (c == '\'');
    }
}
