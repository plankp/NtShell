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
public class CoreUnitTest {

    @Test
    public void isNeverTruthy() {
        assertFalse(CoreUnit.getInstance().isTruthy());
    }

    @Test
    public void hasFixedStringRepresentation() {
        assertEquals("()", CoreUnit.getInstance().toString());
    }

    @Test
    public void additionReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applyAdd(null));
    }

    @Test
    public void subtractionReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applySub(null));
    }

    @Test
    public void multiplicationReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applyMul(null));
    }

    @Test
    public void divisionReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applyDiv(null));
    }

    @Test
    public void moduloReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applyMod(null));
    }

    @Test
    public void powReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applyPow(null));
    }

    @Test
    public void callReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applyCall());
    }

    @Test
    public void composeReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applyCompose(null));
    }

    @Test
    public void positiveReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applyPositive());
    }

    @Test
    public void negativeReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applyNegative());
    }

    @Test
    public void percentageReturnsItself() {
        assertSame(CoreUnit.getInstance(), CoreUnit.getInstance().applyPercentage());
    }
}
