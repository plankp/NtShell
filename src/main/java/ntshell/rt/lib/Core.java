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
package ntshell.rt.lib;

import com.ymcmp.ntshell.NtLibrary;
import com.ymcmp.ntshell.NtValue;

import com.ymcmp.ntshell.value.*;

import java.util.HashSet;
import java.util.Set;

import ntshell.rt.lib.rout.*;

/**
 *
 * @author YTENG
 */
public final class Core implements NtLibrary {

    private final Set<NtLibrary> submodules = new HashSet<>();

    private static class Helper {

        static final Core INSTANCE = new Core();
    }

    private Core() {
        submodules.add(new ntshell.rt.lib.rout.matrix.Loader());
        submodules.add(new ntshell.rt.lib.rout.trig.Loader());
        submodules.add(new ntshell.rt.lib.rout.type.Loader());
    }

    public static NtLibrary getInstance() {
        return Helper.INSTANCE;
    }

    @Override
    public NtValue findDefinition(final String name) {
        for (final NtLibrary submodule : submodules) {
            final NtValue ret = submodule.findDefinition(name);
            if (ret != null) {
                return ret;
            }
        }

        switch (name) {
        case "nil":
            return CoreUnit.getInstance();
        case "pi":
            return CoreNumber.getPi();
        case "e":
            return CoreNumber.getE();
        case "true":
            return CoreNumber.from(true);
        case "false":
            return CoreNumber.from(false);
        case "id":
            return CoreLambda.getIdentityFunction();
        case "twice":
            return Twice.getInstance();
        case "summation":
            return Summation.getInstance();
        case "sqrt":
            return Sqrt.getInstance();
        case "cbrt":
            return Cbrt.getInstance();
        case "square":
            return Square.getInstance();
        case "cube":
            return Cube.getInstance();
        case "abs":
            return Abs.getInstance();
        case "ceil":
            return Ceil.getInstance();
        case "floor":
            return Floor.getInstance();
        case "round":
            return Round.getInstance();
        case "ln":
            return NaturalLog.getInstance();
        case "log":
            return Log10.getInstance();
        case "log_base":
            return LogBase.getInstance();
        case "lim_left":
            return Limits.getLeftSided();
        case "lim_right":
            return Limits.getRightSided();
        case "lim":
            return Limits.getBothSided();
        case "joining!":
            return new Joining();
        default:
        }
        return null;
    }
}
