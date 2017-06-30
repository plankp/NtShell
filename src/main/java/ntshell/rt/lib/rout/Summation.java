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
import com.ymcmp.ntshell.rte.TailCallTrigger;

import com.ymcmp.ntshell.value.CoreLambda;
import com.ymcmp.ntshell.value.CoreNumber;

/**
 *
 * @author YTENG
 */
public final class Summation extends CoreLambda {

    private static final Summation INSTANCE = new Summation();

    private Summation() {
        super(new CoreLambda.Info("summation", "f:[applyCall] -> func", "Wraps (f) inside a summation sequence"));
    }

    public static NtValue getInstance() {
        return INSTANCE;
    }

    @Override
    public NtValue applyCall(final NtValue[] f) {
        // summation (f)(m, n) => while ++m <= n { ret += f(m); }
        if (f.length == 1) {
            return new CoreLambda(new CoreLambda.Info("$$summation", "func(start:number, end:number) -> [applyAdd]", "Performs summation from (start) to (end) with the increment of 1")) {
                @Override
                public NtValue applyCall(final NtValue[] params) {
                    final double n;
                    double m;
                    if (params.length == 2
                            && params[0] instanceof CoreNumber
                            && params[1] instanceof CoreNumber) {
                        m = ((CoreNumber) params[0]).toDouble();
                        n = ((CoreNumber) params[1]).toDouble();

                        NtValue ret = null;
                        // Do summation here
                        do {
                            NtValue t = TailCallTrigger.call(f[0], CoreNumber.from(m));
                            if (ret == null) {
                                ret = t;
                            } else {
                                ret = ret.applyAdd(t);
                            }
                            m += 1.0;
                        } while (m <= n);
                        // ret should never be null at this point
                        return ret;
                    }
                    throw new DispatchException("Expected two numbers, got " + params.length + " instead");
                }
            };
        }
        throw new DispatchException("summation", "Expected one parameter, got " + f.length + " instead");
    }
}
