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

import com.ymcmp.ntshell.ast.*;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author YTENG
 */
public class ParserTest {

    final Parser parser = new Parser();

    @Test
    public void parseConsFunction() {
        try {
            final String expr = "cons = (x, y) -> m -> x:y:m()";
            final AST tree = parser.consumeExpr(Lexer.lexFromString(expr));
            final AST expected = new AssignExpr(makeIdent("cons"),
                                                new AnonFuncVal(new Token[]{makeIdent("x"), makeIdent("y")},
                                                                new AnonFuncVal(makeIdent("m"),
                                                                                new ApplyExpr(
                                                                                        new PartialApplyExpr(new AST[]{new VariableVal(makeIdent("x")), new VariableVal(makeIdent("y"))},
                                                                                                             new VariableVal(makeIdent("m"))), new AST[0]))));
            assertEquals(expected.toString(), tree.toString());
        } catch (LexerException ex) {
            fail("No exception should be thrown");
        }
    }

    @Test
    public void parseCarFunction() {
        try {
            final String expr = "car = (z) -> z((p, q) -> p)";
            final AST tree = parser.consumeExpr(Lexer.lexFromString(expr));
            final AST expected = new AssignExpr(makeIdent("car"),
                                                new AnonFuncVal(makeIdent("z"),
                                                                new ApplyExpr(new VariableVal(makeIdent("z")),
                                                                              new AST[]{new AnonFuncVal(new Token[]{makeIdent("p"), makeIdent("q")},
                                                                                                        new VariableVal(makeIdent("p")))})));
            assertEquals(expected.toString(), tree.toString());
        } catch (LexerException ex) {
            fail("No exception should be thrown");
        }
    }

    @Test
    public void parseFibFunction() {
        try {
            final String expr = ""
                    + "fib = n ->\n"
                    + "  (fib = (i, a, b) -> {\n"
                    + "    b                     if i == n,\n"
                    + "    fib(i + 1, b, a + b)  if i /= n,\n"
                    + "  })(0, 0, 1)";
            final AST tree = parser.consumeExpr(Lexer.lexFromString(expr));
            final AST expected = new AssignExpr(makeIdent("fib"),
                                                new AnonFuncVal(makeIdent("n"),
                                                                new ApplyExpr(new AssignExpr(makeIdent("fib"),
                                                                                             new AnonFuncVal(new Token[]{makeIdent("i"), makeIdent("a"), makeIdent("b")},
                                                                                                             new PiecewiseFuncVal(new PiecewiseFuncVal.CaseBlock[]{
                                                                                                         new PiecewiseFuncVal.CaseBlock(new BinaryExpr(new VariableVal(makeIdent("i")), new VariableVal(makeIdent("n")), new Token(Token.Type.EQL, "==")),
                                                                                                                                        new VariableVal(makeIdent("b"))),
                                                                                                         new PiecewiseFuncVal.CaseBlock(new BinaryExpr(new VariableVal(makeIdent("i")), new VariableVal(makeIdent("n")), new Token(Token.Type.NEQ, "/=")),
                                                                                                                                        new ApplyExpr(new VariableVal(makeIdent("fib")),
                                                                                                                                                      new AST[]{new BinaryExpr(new VariableVal(makeIdent("i")), NumberVal.fromLong(1), new Token(Token.Type.ADD, "+")), new VariableVal(makeIdent("b")), new BinaryExpr(new VariableVal(makeIdent("a")), new VariableVal(makeIdent("b")), new Token(Token.Type.ADD, "+"))})
                                                                                                         )}))),
                                                                              new AST[]{NumberVal.fromLong(0), NumberVal.fromLong(0), NumberVal.fromLong(1)})));
            assertEquals(expected.toString(), tree.toString());
        } catch (LexerException ex) {
            fail("No exception should be thrown");
        }
    }

    private static Token makeIdent(final String id) {
        return new Token(Token.Type.IDENT, id);
    }
}
