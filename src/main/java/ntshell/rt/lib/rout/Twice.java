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

/**
 *
 * @author YTENG
 */
public final class Twice extends CoreLambda {

    private static class Helper {

        static final Twice INSTANCE = new Twice();
    }

    private Twice() {
        super(new CoreLambda.Info("twice", "[applyCall] -> func", "Returns a function that behaves like the following: <code>twice(f) => x -> f(f(x))</code>"));
    }

    public static NtValue getInstance() {
        return Helper.INSTANCE;
    }

    @Override
    public NtValue applyCall(final NtValue[] input) {
        // twice (f) => x -> f(f(x))
        if (input.length == 1) {
            return input[0].applyCompose(input[0]);
        }
        throw new DispatchException("twice", "Expected one parameter, got " + input.length + " instead");
    }
}
