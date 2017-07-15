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
package ntshell.rt.lib.rout.trig;

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
        INSTANCES.put("asin", new ArcSine());
        INSTANCES.put("acos", new ArcCosine());
        INSTANCES.put("atan", new ArcTangent());
        INSTANCES.put("cos", new Cosine());
        INSTANCES.put("cosh", new HyperCosine());
        INSTANCES.put("deg", new Degrees());
        INSTANCES.put("rad", new Radians());
        INSTANCES.put("sin", new Sine());
        INSTANCES.put("sinh", new HyperSine());
        INSTANCES.put("tan", new Tangent());
        INSTANCES.put("tanh", new HyperTangent());
    }

    @Override
    public AbstractNtValue findDefinition(String name) {
        return INSTANCES.get(name);
    }
}
