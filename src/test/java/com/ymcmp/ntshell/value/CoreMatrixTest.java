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

import com.ymcmp.ntshell.rte.DispatchException;
import com.ymcmp.ntshell.NtValue;

import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author YTENG
 */
public class CoreMatrixTest {

    @Test(expected = IllegalArgumentException.class)
    public void matrixFromEnforcesShape() {
        CoreMatrix.from(new NtValue[][]{
            {null}, {null, null}
        });
    }

    public void matrixFromEmptyArrayYieldsSameInstance() {
        assertSame(CoreMatrix.getEmptyMatrix(), CoreMatrix.from(null));
        assertSame(CoreMatrix.getEmptyMatrix(), CoreMatrix.from(new NtValue[0][0]));
    }

    @Test
    public void testGetSetCell() {
        final CoreMatrix mat = CoreMatrix.from(new NtValue[1][1]);
        assertNull(mat.getCell(0, 0));
        mat.setCell(0, 0, CoreNumber.from(true));
        assertEquals(CoreNumber.from(true), mat.getCell(0, 0));
    }

    @Test
    public void testToAtom() {
        assertSame(CoreAtom.from(""), CoreMatrix.getEmptyMatrix().toAtom());
        assertSame(CoreAtom.from("A"), CoreMatrix.from(new NtValue[][]{{CoreNumber.from('A')}}).toAtom());

        final CoreMatrix mat = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from('A'), CoreAtom.from("bc")}
                }
        );
        assertSame(CoreAtom.from("Abc"), mat.toAtom());
    }

    @Test
    public void testFlipOnX() {
        assertSame(CoreMatrix.getEmptyMatrix(), CoreMatrix.getEmptyMatrix().flipOnX());

        assertEquals(CoreMatrix.from(new NtValue[][]{{CoreNumber.from(true)}, {CoreNumber.from(false)}}),
                     CoreMatrix.from(new NtValue[][]{{CoreNumber.from(false)}, {CoreNumber.from(true)}}).flipOnX());
    }

    @Test
    public void testFlipOnY() {
        assertSame(CoreMatrix.getEmptyMatrix(), CoreMatrix.getEmptyMatrix().flipOnY());

        assertEquals(CoreMatrix.from(new NtValue[][]{{CoreNumber.from(true), CoreNumber.from(false)}}),
                     CoreMatrix.from(new NtValue[][]{{CoreNumber.from(false), CoreNumber.from(true)}}).flipOnY());
    }

    @Test
    public void testTranspose() {
        assertSame(CoreMatrix.getEmptyMatrix(), CoreMatrix.getEmptyMatrix().transpose());

        final CoreMatrix a = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(0), CoreNumber.from(1), CoreNumber.from(0)},
                    {CoreNumber.from(1), CoreNumber.from(0), CoreNumber.from(1)}
                }
        );
        final CoreMatrix expected = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(0), CoreNumber.from(1)},
                    {CoreNumber.from(1), CoreNumber.from(0)},
                    {CoreNumber.from(0), CoreNumber.from(1)}
                }
        );
        assertEquals(expected, a.transpose());
    }

    @Test
    public void testSameShape() {
        final CoreMatrix mat1 = CoreMatrix.from(new NtValue[][]{{CoreNumber.from(true), CoreNumber.from(false)}});
        final CoreMatrix mat2 = CoreMatrix.from(new NtValue[][]{{CoreNumber.from(false), CoreNumber.from(true)}});

        assertFalse(CoreMatrix.getEmptyMatrix().sameShape(mat1));
        assertTrue(mat2.sameShape(mat1));

        assertTrue(CoreMatrix.getEmptyMatrix().sameShape(CoreMatrix.getEmptyMatrix()));
    }

    @Test
    public void emptyMatrixIsFalse() {
        assertFalse(CoreMatrix.getEmptyMatrix().isTruthy());
    }

    @Test
    public void nonEmptyMatrixIsTrue() {
        assertTrue(new CoreMatrix(1, 1).isTruthy());
    }

    @Test
    public void testToString() {
        assertEquals("[]", CoreMatrix.getEmptyMatrix().toString());
    }

    @Test
    public void testCrossProduct() {
        try {
            assertSame(CoreMatrix.getEmptyMatrix(), CoreMatrix.getEmptyMatrix().crossProduct(CoreMatrix.getEmptyMatrix()));
        } catch (CoreMatrix.MatrixBoundMismatchException ex) {
            fail("No exception should be thrown");
        }

        try {
            CoreMatrix.getEmptyMatrix().bimap(CoreMatrix.from(new NtValue[1][1]), (a, b) -> a);
            fail("MatrixBoundMismatchException should have been thrown");
        } catch (CoreMatrix.MatrixBoundMismatchException ex) {
        }

        final CoreMatrix a = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(1), CoreNumber.from(2), CoreNumber.from(3)},
                    {CoreNumber.from(4), CoreNumber.from(5), CoreNumber.from(6)}
                }
        );
        final CoreMatrix b = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(7), CoreNumber.from(8)},
                    {CoreNumber.from(9), CoreNumber.from(10)},
                    {CoreNumber.from(11), CoreNumber.from(12)}
                }
        );
        final CoreMatrix expected = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(58), CoreNumber.from(64)},
                    {CoreNumber.from(139), CoreNumber.from(154)}
                }
        );
        try {
            final CoreMatrix rst = a.crossProduct(b);
            assertEquals(expected, rst);
        } catch (CoreMatrix.MatrixBoundMismatchException ex) {
            fail("No exception is supposed to be thrown");
        }
    }

    @Test
    public void testMap() {
        assertSame(CoreMatrix.getEmptyMatrix(), CoreMatrix.getEmptyMatrix().map(Function.identity()));

        final CoreMatrix a = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(true), CoreNumber.from(false)},
                    {CoreNumber.from(false), CoreNumber.from(true)}
                }
        );
        final CoreMatrix expected = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(true), CoreNumber.from(false)},
                    {CoreNumber.from(false), CoreNumber.from(true)}
                }
        );
        assertEquals(expected, a.map(Function.identity()));
        assertEquals(expected, a.map(CoreLambda.getIdentityFunction()));
    }

    @Test
    public void testBimap() {
        final CoreMatrix a = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(0), CoreNumber.from(0)},
                    {CoreNumber.from(0), CoreNumber.from(0)}
                }
        );
        final CoreMatrix b = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(true), CoreNumber.from(false)},
                    {CoreNumber.from(false), CoreNumber.from(true)}
                }
        );
        final CoreMatrix expected = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(true), CoreNumber.from(false)},
                    {CoreNumber.from(false), CoreNumber.from(true)}
                }
        );
        try {
            assertEquals(expected, a.bimap(b, NtValue::applyAdd));
        } catch (CoreMatrix.MatrixBoundMismatchException ex) {
            fail("No exception should be thrown");
        }
    }

    @Test
    public void newMatrixMustNotBeBiggerThanOriginalWhenReshaping() {
        try {
            CoreMatrix.getEmptyMatrix().reshape(0, 0);
        } catch (CoreMatrix.MatrixBoundMismatchException ex) {
            fail("No exception should be thrown from reshaping into (0, 0)");
        }

        try {
            CoreMatrix.getEmptyMatrix().reshape(2, 5);
            fail("Expected an exception when reshaping into a bigger size");
        } catch (CoreMatrix.MatrixBoundMismatchException ex) {
        }
    }

    @Test
    public void testReshape() {
        try {
            final CoreMatrix a = CoreMatrix.from(new NtValue[][]{
                IntStream.rangeClosed(1, 11).mapToObj(CoreNumber::from).toArray(NtValue[]::new)});
            final CoreMatrix expected = CoreMatrix.from(new NtValue[][]{
                IntStream.rangeClosed(1, 5).mapToObj(CoreNumber::from).toArray(NtValue[]::new),
                IntStream.rangeClosed(6, 10).mapToObj(CoreNumber::from).toArray(NtValue[]::new)});

            assertEquals(expected, a.reshape(2, 5));
        } catch (CoreMatrix.MatrixBoundMismatchException ex) {
            fail("No exception should be thrown");
        }
    }

    @Test
    public void testApplyPositive() {
        final CoreMatrix a = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(1), CoreNumber.from(-1)},
                    {CoreNumber.from(-1), CoreNumber.from(1)}});
        final CoreMatrix expected = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(1), CoreNumber.from(-1)},
                    {CoreNumber.from(-1), CoreNumber.from(1)}});
        assertEquals(expected, a.applyPositive());
    }

    @Test
    public void testApplyNegative() {
        final CoreMatrix a = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(1), CoreNumber.from(-1)},
                    {CoreNumber.from(-1), CoreNumber.from(1)}});
        final CoreMatrix expected = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(-1), CoreNumber.from(1)},
                    {CoreNumber.from(1), CoreNumber.from(-1)}});
        assertEquals(expected, a.applyNegative());
    }

    @Test
    public void testApplyPercentage() {
        final CoreMatrix a = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(1), CoreNumber.from(-1)},
                    {CoreNumber.from(-1), CoreNumber.from(1)}});
        final CoreMatrix expected = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(0.01), CoreNumber.from(-0.01)},
                    {CoreNumber.from(-0.01), CoreNumber.from(0.01)}});
        assertEquals(expected, a.applyPercentage());
    }

    @Test
    public void testApplyCall() {
        final CoreMatrix a = CoreMatrix.from(new NtValue[][]{{CoreNumber.from(true)}});
        assertEquals(CoreNumber.from(true), a.applyCall(new NtValue[]{CoreNumber.from(1), CoreNumber.from(1)}));
        assertEquals(CoreUnit.getInstance(), a.applyCall(new NtValue[]{CoreNumber.from(5), CoreNumber.from(2)}));
    }

    @Test
    public void testApplyAdd() {
        final CoreMatrix a = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(1), CoreNumber.from(-1)},
                    {CoreNumber.from(-1), CoreNumber.from(1)}});
        final CoreMatrix b = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(-1), CoreNumber.from(1)},
                    {CoreNumber.from(1), CoreNumber.from(-1)}});

        final CoreMatrix expectedMat = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(0), CoreNumber.from(0)},
                    {CoreNumber.from(0), CoreNumber.from(0)}});
        assertEquals(expectedMat, a.applyAdd(b));

        final CoreMatrix expectedNum = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(2), CoreNumber.from(0)},
                    {CoreNumber.from(0), CoreNumber.from(2)}});
        assertEquals(expectedNum, a.applyAdd(CoreNumber.from(1)));
    }

    @Test
    public void testApplySub() {
        final CoreMatrix a = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(1), CoreNumber.from(-1)},
                    {CoreNumber.from(-1), CoreNumber.from(1)}});
        final CoreMatrix b = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(-1), CoreNumber.from(1)},
                    {CoreNumber.from(1), CoreNumber.from(-1)}});

        final CoreMatrix expectedMat = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(2), CoreNumber.from(-2)},
                    {CoreNumber.from(-2), CoreNumber.from(2)}});
        assertEquals(expectedMat, a.applySub(b));

        final CoreMatrix expectedNum = CoreMatrix.from(
                new NtValue[][]{
                    {CoreNumber.from(0), CoreNumber.from(-2)},
                    {CoreNumber.from(-2), CoreNumber.from(0)}});
        assertEquals(expectedNum, a.applySub(CoreNumber.from(1)));
    }

    @Test(expected = DispatchException.class)
    public void applyDivOnMatrixIsNotSupported() {
        CoreMatrix.getEmptyMatrix().applyDiv(CoreMatrix.getEmptyMatrix());
    }

    @Test
    public void testReduceLeft() {
        final CoreMatrix mat = CoreMatrix.from(new NtValue[][]{
            {CoreNumber.from(1), CoreNumber.from(2), CoreNumber.from(3)}
        });
        assertEquals(CoreNumber.from(0 - 1 - 2 - 3), mat.reduceLeft(NtValue::applySub, CoreNumber.from(0)));
    }

    @Test
    public void testReduceRight() {
        final CoreMatrix mat = CoreMatrix.from(new NtValue[][]{
            {CoreNumber.from(1), CoreNumber.from(2), CoreNumber.from(3)}
        });
        assertEquals(CoreNumber.from(0 - 3 - 2 - 1), mat.reduceRight(NtValue::applySub, CoreNumber.from(0)));
    }
}
