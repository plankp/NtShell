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

import com.ymcmp.ntshell.Token;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author YTENG
 */
public class AtomValTest {

    @Test
    public void atomValOnlyAcceptsAtoms() {
        for (Token.Type type : Token.Type.values()) {
            if (type == Token.Type.ATOM) {
                final AtomVal noThrow = new AtomVal(new Token(type, ""));
            } else {
                try {
                    final AtomVal mustThrow = new AtomVal(new Token(type, ""));
                    fail("Expected IllegalArgumentException");
                } catch (IllegalArgumentException ex) {
                }
            }
        }
    }

    @Test
    public void toStringDelegatesToken() {
        final Token tok = new Token(Token.Type.ATOM, "@atom");
        assertEquals(tok.toString(), new AtomVal(tok).toString());
    }

    @Test
    public void fromStringCreatesEqualAtom() {
        final Token tok = new Token(Token.Type.ATOM, "@atom");
        assertEquals(new AtomVal(tok), AtomVal.fromString("@atom"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testVisit() {
        MockVisitor.VISITOR.visit(AtomVal.fromString("@atom"));
    }
}
