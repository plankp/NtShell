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
package ntshell.rt.lib.rout.type;

import com.ymcmp.ntshell.NtValue;
import com.ymcmp.ntshell.value.*;

import com.ymcmp.ntshell.ast.UnitVal;
import com.ymcmp.ntshell.ast.NumberVal;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author YTENG
 */
public class ModuleTest {

    private final Loader loader = new Loader();

    @Test
    public void testNilPred() {
        final NtValue val = loader.findDefinition("nil?");
        assertNotNull(val);
        // NilPred only returns true if all parameters are nil
        // passing no parameters is considered true since there is nothing to test
        assertEquals(CoreNumber.from(true), val.applyCall());
        assertEquals(CoreNumber.from(true), val.applyCall(CoreUnit.getInstance()));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreUnit.getInstance(), CoreNumber.ONE));
        assertEquals(CoreNumber.from(true), val.applyCall(CoreUnit.getInstance(), CoreUnit.getInstance()));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreNumber.ONE));
    }

    @Test
    public void testASTPred() {
        final NtValue val = loader.findDefinition("syntree?");
        assertNotNull(val);
        assertEquals(CoreNumber.from(true), val.applyCall());
        assertEquals(CoreNumber.from(true), val.applyCall(new UnitVal()));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreUnit.getInstance()));
        assertEquals(CoreNumber.from(true), val.applyCall(NumberVal.fromLong(1), new UnitVal()));
    }

    @Test
    public void testComparablePred() {
        final NtValue val = loader.findDefinition("comparable?");
        assertNotNull(val);
        // ComparablePred only returns true if all parameters are comparable
        // Note: the parameters just has to be comparable, having to be able to
        // compare with other parameters is *not* a requirement.
        // passing no parameters is considered true since there is nothing to test
        assertEquals(CoreNumber.from(true), val.applyCall());
        assertEquals(CoreNumber.from(false), val.applyCall(CoreUnit.getInstance()));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreNumber.ONE, CoreUnit.getInstance()));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreUnit.getInstance(), CoreUnit.getInstance()));
        assertEquals(CoreNumber.from(true), val.applyCall(CoreNumber.ONE));
        assertEquals(CoreNumber.from(true), val.applyCall(CoreNumber.ONE, CoreAtom.from("A")));
    }

    @Test
    public void testFunctionPred() {
        final NtValue val = loader.findDefinition("function?");
        assertNotNull(val);
        assertEquals(CoreNumber.from(true), val.applyCall());
        assertEquals(CoreNumber.from(true), val.applyCall(val));
        assertEquals(CoreNumber.from(true), val.applyCall(val, CoreLambda.getIdentityFunction()));
        assertEquals(CoreNumber.from(false), val.applyCall(val, CoreUnit.getInstance()));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreUnit.getInstance()));
    }

    @Test
    public void testMatrixPred() {
        final NtValue val = loader.findDefinition("matrix?");
        assertNotNull(val);
        assertEquals(CoreNumber.from(true), val.applyCall());
        assertEquals(CoreNumber.from(true), val.applyCall(CoreMatrix.getEmptyMatrix()));
        assertEquals(CoreNumber.from(true), val.applyCall(CoreMatrix.from(new NtValue[1][1]), CoreMatrix.from(new NtValue[5][2])));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreMatrix.getEmptyMatrix(), CoreUnit.getInstance()));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreUnit.getInstance()));
        // Atoms are also matrix
        assertEquals(CoreNumber.from(true), val.applyCall(CoreAtom.from("A")));
    }

    @Test
    public void testAtomPred() {
        final NtValue val = loader.findDefinition("atom?");
        assertNotNull(val);
        assertEquals(CoreNumber.from(true), val.applyCall());
        assertEquals(CoreNumber.from(true), val.applyCall(CoreAtom.from("A")));
        assertEquals(CoreNumber.from(true), val.applyCall(CoreAtom.from("Abc"), CoreAtom.from("ZJ")));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreAtom.from("Abc"), CoreUnit.getInstance()));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreUnit.getInstance()));
    }

    @Test
    public void testNumberPred() {
        final NtValue val = loader.findDefinition("number?");
        assertNotNull(val);
        // NumberPred only returns true if all parameters are numbers
        // passing no parameters is considered true since there is nothing to test
        assertEquals(CoreNumber.from(true), val.applyCall());
        assertEquals(CoreNumber.from(true), val.applyCall(CoreNumber.HALF));
        assertEquals(CoreNumber.from(false), val.applyCall(CoreNumber.ONE, CoreUnit.getInstance()));
        assertEquals(CoreNumber.from(true), val.applyCall(CoreNumber.ONE, CoreNumber.TWO));
        // Non-finite values are also numbers! This function test for types
        assertEquals(CoreNumber.from(true), val.applyCall(CoreNumber.NAN));
        assertEquals(CoreNumber.from(true), val.applyCall(CoreNumber.POS_INF));
        assertEquals(CoreNumber.from(true), val.applyCall(CoreNumber.NEG_INF));
    }
}
