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
public class CoreLambdaTest {

    @Test
    public void identityFunctionReturnsItself() {
        assertSame(CoreLambda.getIdentityFunction(), CoreLambda.getIdentityFunction().applyCall(CoreLambda.getIdentityFunction()));
    }

    @Test
    public void isAlwaysTruthy() {
        assertTrue(CoreLambda.getIdentityFunction().isTruthy());
    }

    @Test
    public void toFunctionYieldsSameResult() {
        final CoreLambda f = CoreLambda.from(x -> CoreUnit.getInstance());
        assertEquals(f.applyCall(), f.toFunction().apply(null));
    }

    @Test
    public void additionComposesExcution() {
        final CoreLambda f = CoreLambda.getIdentityFunction();
        final CoreLambda g = CoreLambda.getIdentityFunction();
        assertEquals(CoreNumber.from(2), f.applyAdd(g).applyCall(CoreNumber.from(1)));
    }

    @Test
    public void subtractionComposesExcution() {
        final CoreLambda f = CoreLambda.getIdentityFunction();
        final CoreLambda g = CoreLambda.getIdentityFunction();
        assertEquals(CoreNumber.from(0), f.applySub(g).applyCall(CoreNumber.from(1)));
    }

    @Test
    public void multiplicationComposesExcution() {
        final CoreLambda f = CoreLambda.getIdentityFunction();
        final CoreLambda g = CoreLambda.getIdentityFunction();
        assertEquals(CoreNumber.from(1), f.applyMul(g).applyCall(CoreNumber.from(1)));
    }

    @Test
    public void divisionComposesExcution() {
        final CoreLambda f = CoreLambda.getIdentityFunction();
        final CoreLambda g = CoreLambda.getIdentityFunction();
        assertEquals(CoreNumber.from(1), f.applyDiv(g).applyCall(CoreNumber.from(1)));
    }

    @Test
    public void powComposesExcution() {
        final CoreLambda f = CoreLambda.getIdentityFunction();
        final CoreLambda g = CoreLambda.getIdentityFunction();
        assertEquals(CoreNumber.from(1), f.applyPow(g).applyCall(CoreNumber.from(1)));
    }

    @Test
    public void moduloComposesExcution() {
        final CoreLambda f = CoreLambda.getIdentityFunction();
        final CoreLambda g = CoreLambda.getIdentityFunction();
        assertEquals(CoreNumber.from(0), f.applyMod(g).applyCall(CoreNumber.from(1)));
    }

    @Test
    public void composeComposesExcution() {
        final CoreLambda f = CoreLambda.getIdentityFunction();
        final CoreLambda g = CoreLambda.getIdentityFunction();
        assertEquals(CoreNumber.from(1), f.applyCompose(g).applyCall(CoreNumber.from(1)));
    }

    @Test
    public void positiveComposesExcution() {
        final CoreLambda f = CoreLambda.getIdentityFunction();
        assertEquals(CoreNumber.from(1), f.applyPositive().applyCall(CoreNumber.from(1)));
    }

    @Test
    public void negativeComposesExcution() {
        final CoreLambda f = CoreLambda.getIdentityFunction();
        assertEquals(CoreNumber.from(-1), f.applyNegative().applyCall(CoreNumber.from(1)));
    }

    @Test
    public void percentageComposesExcution() {
        final CoreLambda f = CoreLambda.getIdentityFunction();
        assertEquals(CoreNumber.from(0.01), f.applyPercentage().applyCall(CoreNumber.from(1)));
    }
}
