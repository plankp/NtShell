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

import com.ymcmp.ntshell.AST;
import com.ymcmp.ntshell.NtValue;

import com.ymcmp.ntshell.value.CoreLambda;
import com.ymcmp.ntshell.value.CoreNumber;

/**
 *
 * @author YTENG
 */
final class ASTPred extends CoreLambda {

    public ASTPred() {
        super(new CoreLambda.Info("syntree?", "(...) -> number", "Test if value is a syntax tree"));
    }

    @Override
    public NtValue applyCall(final NtValue[] input) {
        for (int i = 0; i < input.length; ++i) {
            if (!(input[i] instanceof AST)) {
                return CoreNumber.from(false);
            }
        }
        return CoreNumber.from(true);
    }
}
