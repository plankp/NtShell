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
package com.ymcmp.ntshell.rte;

/**
 *
 * @author YTENG
 */
public class BadValueException extends RuntimeException {

    public BadValueException() {
    }

    public BadValueException(String message) {
        super(message);
    }

    public BadValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadValueException(Throwable cause) {
        super(cause);
    }
}
