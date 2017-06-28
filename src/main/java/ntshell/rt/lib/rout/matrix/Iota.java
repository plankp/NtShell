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
package ntshell.rt.lib.rout.matrix;

import com.ymcmp.ntshell.NtValue;

import com.ymcmp.ntshell.rte.DispatchException;

import com.ymcmp.ntshell.value.CoreLambda;
import com.ymcmp.ntshell.value.CoreMatrix;
import com.ymcmp.ntshell.value.CoreNumber;

import java.util.stream.IntStream;

/**
 *
 * @author YTENG
 */
public final class Iota extends CoreLambda {

    private static class Helper {

        static final Iota INSTANCE = new Iota();
    }

    private Iota() {
        super(new CoreLambda.Info("iota", "bound:number -> mat", "Creates a one-row matrix with the elements 1 to (bound). Returns an empty matrix if bound is not bigger than 1."));
    }

    public static NtValue getInstance() {
        return Helper.INSTANCE;
    }

    @Override
    public NtValue applyCall(final NtValue[] params) {
        if (params.length == 1 && params[0] instanceof CoreNumber) {
            return CoreMatrix.from(new CoreNumber[][]{
                IntStream.rangeClosed(1, (int) ((CoreNumber) params[0]).toDouble())
                .mapToObj(CoreNumber::from)
                .toArray(CoreNumber[]::new)});
        }
        throw new DispatchException("iota", "Expected a number, got " + params.length + " instead");
    }
}
