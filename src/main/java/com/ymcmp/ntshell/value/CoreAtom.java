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
package com.ymcmp.ntshell.value;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author YTENG
 */
public class CoreAtom extends CoreMatrix {

    private static final Map<String, CoreAtom> INTERN_MAP = new HashMap<>();

    public final String str;

    private CoreAtom(final String val) {
        super(1, val.length());
        this.str = val;
        final char[] arr = val.toCharArray();
        for (int i = 0; i < arr.length; ++i) {
            mat[0][i] = CoreNumber.from(arr[i]);
        }
    }

    @Override
    public String toString() {
        return str;
    }

    public static CoreAtom from(final String s) {
        CoreAtom get = INTERN_MAP.get(s);
        if (get == null) {
            INTERN_MAP.put(s, get = new CoreAtom(s));
        }
        return get;
    }

    public CoreMatrix toMatrix() {
        return new CoreMatrix(this.mat);
    }

    @Override
    protected LogicalLine toLogicalLine() {
        return new LogicalLine(this.str);
    }
}
