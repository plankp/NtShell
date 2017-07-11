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

import com.ymcmp.ntshell.value.CoreAtom;
import com.ymcmp.ntshell.value.CoreLambda;

/**
 *
 * @author YTENG
 */
public final class Joining extends CoreLambda {

    private final StringBuilder str;

    public Joining() {
        super(new CoreLambda.Info("joining!", "... -> func OR () -> atom", "Joins a string until unit is passed as parameter"));
        str = new StringBuilder();
    }

    @Override
    public NtValue applyCall(final NtValue[] input) {
        if (input.length == 0) {
            return CoreAtom.from(str.toString());
        }
        str.append(input[0]);
        for (int i = 1; i < input.length; ++i) {
            str.append(' ').append(input[i]);
        }
        return this;
    }
}
