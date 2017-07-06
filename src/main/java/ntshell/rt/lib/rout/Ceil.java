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
package ntshell.rt.lib.rout;

import com.ymcmp.ntshell.NtValue;

import com.ymcmp.ntshell.rte.DispatchException;

import com.ymcmp.ntshell.value.CoreLambda;
import com.ymcmp.ntshell.value.CoreNumber;

import org.apfloat.ApfloatMath;

/**
 *
 * @author YTENG
 */
public final class Ceil extends CoreLambda {

    private static final Ceil INSTANCE = new Ceil();

    private Ceil() {
        super(new CoreLambda.Info("ceil", "number -> number", "Calculates the ceiling of a value"));
    }

    public static NtValue getInstance() {
        return INSTANCE;
    }

    @Override
    public NtValue applyCall(final NtValue[] input) {
        if (input.length == 1 && input[0] instanceof CoreNumber) {
            return CoreNumber.from(ApfloatMath.ceil(((CoreNumber) input[0]).toApfloat()));
        }
        throw new DispatchException("ceil", "Expected a number but got " + input.length);
    }
}
