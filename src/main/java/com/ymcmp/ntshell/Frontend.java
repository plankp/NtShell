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
public interface Frontend extends AutoCloseable {

    public String readLine();

    public void write(char c);

    public void errWrite(char c);

    public default void write(final Object o) {
        for (final char c : o.toString().toCharArray()) {
            write(c);
        }
    }

    public default void errWrite(final Object o) {
        for (final char c : o.toString().toCharArray()) {
            errWrite(c);
        }
    }

    public default void writeLine() {
        write('\n');
    }

    public default void errWriteLine() {
        errWrite('\n');
    }

    public default void writeLine(final Object o) {
        write(o);
        writeLine();
    }

    public default void errWriteLine(final Object o) {
        errWrite(o);
        errWriteLine();
    }

    @Override
    public default void close() {
    }

    /**
     * Looks for a definition (typically a function or variable) that is
     * specific to the particular front end. Search order is local definitions,
     * then here, then predefinitions.
     *
     * @param name The name of the definition
     * @return The value of the definition. Returns {@code null} if none was
     * found.
     */
    public NtValue findDefinition(String name);
}
