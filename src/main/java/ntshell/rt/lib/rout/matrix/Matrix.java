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

import com.ymcmp.ntshell.value.CoreAtom;
import com.ymcmp.ntshell.value.CoreLambda;

/**
 *
 * @author YTENG
 */
final class Matrix extends CoreLambda {

    public Matrix() {
        super(new CoreLambda.Info("matrix", "atom -> matrix", "Converts the atom into its equivalent matrix"));
    }

    @Override
    public NtValue applyCall(final NtValue[] params) {
        if (params.length == 1 && params[0] instanceof CoreAtom) {
            return ((CoreAtom) params[0]).toMatrix();
        }
        throw new DispatchException("matrix", "Expected a atom, got " + params.length + " instead");
    }
}
