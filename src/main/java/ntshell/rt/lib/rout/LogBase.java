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

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

/**
 *
 * @author YTENG
 */
public final class LogBase extends CoreLambda {

    private static final LogBase INSTANCE = new LogBase();

    private LogBase() {
        super(new CoreLambda.Info("base n log", "func(number) -> func", "Creates a logarithm with the specified base"));
    }

    public static NtValue getInstance() {
        return INSTANCE;
    }

    @Override
    public NtValue applyCall(final NtValue[] params) {
        if (params.length == 1 && params[0] instanceof CoreNumber) {
            // log_base (10)(100) => 2
            final Apfloat base = ((CoreNumber) params[0]).toApfloat();
            return new CoreLambda(new CoreLambda.Info("$$base n log", "func(number) -> number", "Calculates the logarithm of a value with a predefined base")) {
                @Override
                public NtValue applyCall(final NtValue[] params) {
                    if (params.length == 1 && params[0] instanceof CoreNumber) {
                        return CoreNumber.from(ApfloatMath.log(((CoreNumber) params[0]).toApfloat(), base));
                    }
                    throw new DispatchException("Expected one number but got " + params.length);
                }
            };
        }
        throw new DispatchException("base n log", "Expected one number but got " + params.length);
    }
}
