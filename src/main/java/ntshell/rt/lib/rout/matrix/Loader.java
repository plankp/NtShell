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
import com.ymcmp.ntshell.value.AbstractNtValue;

import java.util.HashMap;

/**
 *
 * @author YTENG
 */
public final class Loader implements NtLibrary {

    private static final HashMap<String, AbstractNtValue> INSTANCES = new HashMap<>();

    static {
        INSTANCES.put("atom", new Atom());
        INSTANCES.put("flip_x", new FlipX());
        INSTANCES.put("flip_y", new FlipY());
        INSTANCES.put("group", new Group());
        INSTANCES.put("iota", new Iota());
        INSTANCES.put("map", new Map());
        INSTANCES.put("matrix", new Matrix());
        INSTANCES.put("reshape", new Reshape());
        INSTANCES.put("transpose", new Transpose());
        INSTANCES.put("foldr", new FoldRight());
        INSTANCES.put("foldl", new FoldLeft());
    }

    @Override
    public AbstractNtValue findDefinition(String name) {
        return INSTANCES.get(name);
    }
}
