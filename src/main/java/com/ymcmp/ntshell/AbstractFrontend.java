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
package com.ymcmp.ntshell;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author YTENG
 */
abstract class AbstractFrontend implements Frontend {

    protected Set<NtLibrary> libraries = new HashSet<>();

    @Override
    public void linkLibrary(final NtLibrary library) {
        if (library != this) {
            libraries.add(library);
        }
    }

    @Override
    public NtValue findDefinition(String name) {
        for (final NtLibrary lib : libraries) {
            final NtValue val = lib.findDefinition(name);
            if (val != null) {
                return val;
            }
        }
        return null;
    }
}
