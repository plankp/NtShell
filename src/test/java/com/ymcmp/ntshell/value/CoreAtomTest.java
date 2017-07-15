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
package com.ymcmp.ntshell.value;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author YTENG
 */
public class CoreAtomTest {

    @Test
    public void atomsAreInterned() {
        assertSame(CoreAtom.from("Hello"), CoreAtom.from("Hello"));
    }

    @Test
    public void flipOnXDoesNothing() {
        assertSame(CoreAtom.from("Hello"), CoreAtom.from("Hello").flipOnX());
    }

    @Test
    public void flipOnYReversesAtom() {
        assertSame(CoreAtom.from("Hello"), CoreAtom.from("olleH").flipOnY());
    }

    @Test
    public void toAtomReturnsItself() {
        assertSame(CoreAtom.from("Hello"), CoreAtom.from("Hello").toAtom());
    }

    @Test
    public void getCharAtReturnsUnitOnFail() {
        assertSame(CoreUnit.getInstance(), CoreAtom.from("").getCharAt(0));
    }

    @Test
    public void getCharAtReturnsCorrectItem() {
        final CoreAtom atom = CoreAtom.from("AB");
        assertEquals(CoreNumber.from('A'), atom.getCharAt(0));
        assertEquals(CoreNumber.from('B'), atom.getCharAt(-1));
    }

    @Test
    public void sliceReturnsUnitOnFail() {
        assertSame(CoreUnit.getInstance(), CoreAtom.from("").slice(0, 1));
    }

    @Test
    public void sliceReturnsCorrectSubAtom() {
        final CoreAtom atom = CoreAtom.from("ABCDEF");
        assertSame(CoreAtom.from("ABC"), atom.slice(0, 3));
        assertSame(CoreAtom.from("DEF"), atom.slice(-3, atom.length()));
        assertSame(CoreAtom.from("CD"), atom.slice(2, -2));
    }

    @Test
    public void lengthReturnsCorrectly() {
        assertEquals(5, CoreAtom.from("Hello").length());
    }

    @Test
    public void toStringReturnsAtom() {
        assertEquals("Hello", CoreAtom.from("Hello").toString());
    }

    @Test
    public void testToMatrix() {
        final CoreMatrix expected = CoreMatrix.from(new AbstractNtValue[][]{
                    {CoreNumber.from('h'), CoreNumber.from('e'), CoreNumber.from('l'), CoreNumber.from('l'), CoreNumber.from('o')}
                }
        );
        assertEquals(expected, CoreAtom.from("hello").toMatrix());
    }

    @Test
    public void testDelegatesForApplyCall() {
        final CoreAtom atom = CoreAtom.from("Abc");

        // Char at when number
        assertEquals(CoreNumber.from('A'), atom.applyCall(new AbstractNtValue[]{CoreNumber.from(1)}));
        assertEquals(CoreNumber.from('c'), atom.applyCall(new AbstractNtValue[]{CoreNumber.from(-1)}));

        // Slice when two numbers
        assertEquals(CoreAtom.from("Ab"), atom.applyCall(new AbstractNtValue[]{CoreNumber.from(1), CoreNumber.from(3)}));
        assertEquals(CoreAtom.from("bc"), atom.applyCall(new AbstractNtValue[]{CoreNumber.from(-2), CoreNumber.from(-1)}));

        // Unit on everything else
        assertSame(CoreUnit.getInstance(), atom.applyCall(new AbstractNtValue[0]));
    }

    @Test
    public void testCompareTo() {
        assertEquals(0, CoreAtom.from("Abc").compareTo(CoreAtom.from("Abc")));
        assertEquals("Abc".compareTo("abc"), CoreAtom.from("Abc").compareTo(CoreAtom.from("abc")));
        assertEquals("abc".compareTo("Abc"), CoreAtom.from("abc").compareTo(CoreAtom.from("Abc")));
    }
}
