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

import com.ymcmp.ntshell.NtValue;

import java.util.HashMap;
import java.util.Map;

/**
 * The closest data type to a string in NtShell. An atom is a one dimensional
 * {@link com.ymcmp.ntshell.value.CoreMatrix} where each element is stored as a
 * {@link com.ymcmp.ntshell.vaiue.CoreNumber}. All matrix operations can be
 * applied on an atom, and all atoms can be converted into its equivalent
 * matrix.
 *
 * @author YTENG
 */
public class CoreAtom extends CoreMatrix {

    private static final Map<String, CoreAtom> INTERN_MAP = new HashMap<>();

    /**
     * The atom in text form
     */
    public final String str;

    /**
     * Allocates a new atom.
     * {@link com.ymcmp.ntshell.value.CoreAtom#from(String)} should be used
     * instead of this.
     *
     * @param val The text representation of the atom
     */
    private CoreAtom(final String val) {
        super(1, val.length());
        this.str = val;
        final char[] arr = val.toCharArray();
        for (int i = 0; i < arr.length; ++i) {
            mat[0][i] = CoreNumber.from(arr[i]);
        }
    }

    /**
     *
     * @return The text representation of an atom
     */
    @Override
    public String toString() {
        return str;
    }

    /**
     *
     * @return The length of the atom
     */
    public int length() {
        return str.length();
    }

    /**
     * Returns an atom instance with the same requirements. If an atom with the
     * same text representation already exists, no new atoms are allocated in
     * memory.
     *
     * @param s The text representation of the atom
     * @return An atom instance
     */
    public static CoreAtom from(final String s) {
        CoreAtom get = INTERN_MAP.get(s);
        if (get == null) {
            INTERN_MAP.put(s, get = new CoreAtom(s));
        }
        return get;
    }

    /**
     * Possibilities of {@code params}:<br>
     * <ul>
     * <li>(number): Returns cell at that index. See
     * {@link CoreAtom#getCharAt(int)}.</li>
     * <li>(two numbers): Returns the substring of the atom. See
     * {@link CoreAtom#slice(int, int)}</li>
     * </ul>
     * Everything else results in a {@link com.ymcmp.ntshell.value.CoreUnit}
     *
     * @param params
     * @return
     */
    @Override
    public NtValue applyCall(NtValue[] params) {
        switch (params.length) {
        case 1:
            if (params[0] instanceof CoreNumber) {
                final int loc = ((CoreNumber) params[0]).toDecimal().intValue();
                return getCharAt(translateIndex(loc));
            }
            break;
        case 2:
            if (params[0] instanceof CoreNumber && params[1] instanceof CoreNumber) {
                final int start = (int) ((CoreNumber) params[0]).toDecimal().intValue();
                int end = (int) ((CoreNumber) params[1]).toDecimal().intValue();

                if (end < 0 && ++end == 0) {
                    end = str.length() + 1;
                }

                return slice(translateIndex(start), translateIndex(end));
            }
            break;
        default:
        }
        return CoreUnit.getInstance();
    }

    private static int translateIndex(final int loc) {
        return loc < 0 ? loc : loc - 1;
    }

    /**
     * Creates an atom from a range of characters of the current atom. The
     * current atom is not modified.
     *
     * @param start Starting location
     * @param end Ending location (not included in range)
     * @return The new atom, or {@link CoreUnit} if out of bounds
     */
    public NtValue slice(final int start, final int end) {
        try {
            return CoreAtom.from(str.substring(getLocation(start), getLocation(end)));
        } catch (IndexOutOfBoundsException ex) {
            return CoreUnit.getInstance();
        }
    }

    /**
     * Returns the real location based on the logical location. If the logical
     * location is less than zero, the indexing is from the end. Taking the atom
     * of {@code Hello} as an example:
     *
     * <table>
     * <tr><th>H</th><th>e</th><th>l</th><th>l</th><th>o</th></tr>
     * <tr><th>0</th><th>1</th><th>2</th><th>3</th><th>4</th></tr>
     * <tr><th>-5</th><th>-4</th><th>-3</th><th>-2</th><th>-1</th></tr>
     * </table>
     *
     * @param loc The logical location
     * @return The real location
     */
    private int getLocation(final int loc) {
        return loc < 0 ? str.length() + loc : loc;
    }

    /**
     * Returns the character at the specified position. If the position is
     * positive, indexing starts from the beginning. Indexing starts from the
     * end otherwise.
     *
     * @param pos The position of the char
     * @return The char (wrapped inside a {@link CoreNumber}) or
     * {@link CoreUnit} if index is out of bounds.
     */
    public NtValue getCharAt(final int pos) {
        try {
            return this.mat[0][getLocation(pos)];
        } catch (IndexOutOfBoundsException ex) {
            return CoreUnit.getInstance();
        }
    }

    /**
     * An atom is already an atom (override for performance reasons)
     *
     * @return The atom itself
     */
    @Override

    public CoreAtom toAtom() {
        return this;
    }

    /**
     * Converts the atom into its matrix equivalent. It will be an one
     * dimensional matrix with each element its char value. For example:
     * {@code @hello toMatrix} will result in {@code [104, 101, 108, 108, 111]}
     *
     * @return The equivalent matrix
     */
    public CoreMatrix toMatrix() {
        return new CoreMatrix(this.mat);
    }

    @Override
    public CoreAtom flipOnX() {
        return this;
    }

    @Override
    public CoreAtom flipOnY() {
        return super.flipOnY().toAtom();
    }
}
