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

import com.ymcmp.ntshell.NtLibrary;
import com.ymcmp.ntshell.NtValue;

/**
 *
 * @author YTENG
 */
public final class Loader implements NtLibrary {

    public Loader() {
    }

    @Override
    public NtValue findDefinition(String name) {
        switch (name) {
        case "matrix":
            return Matrix.getInstance();
        case "group":
            return Group.getInstance();
        case "atom":
            return Atom.getInstance();
        case "iota":
            return Iota.getInstance();
        case "reshape":
            return Reshape.getInstance();
        case "transpose":
            return Transpose.getInstance();
        case "flip_x":
            return FlipX.getInstance();
        case "flip_y":
            return FlipY.getInstance();
        case "map":
            return Map.getInstance();
        default:
        }
        return null;
    }
}
