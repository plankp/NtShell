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

/**
 *
 * @author YTENG
 */
public interface NtLibrary {

    /**
     * Looks for a definition (typically a function or variable) that is
     * specific to the particular front end. Search order is local definitions,
     * then here, then predefinitions.
     *
     * @param name The name of the definition
     * @return The value of the definition. Returns {@code null} if none was
     * found.
     */
    NtValue findDefinition(String name);
}
