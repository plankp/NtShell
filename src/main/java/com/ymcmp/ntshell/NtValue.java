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
 * A common parent shared by all values used in the NtShell computation.
 *
 * @author YTENG
 */
public interface NtValue {

    /**
     * Overrides the behavior of the addition operator {@code +} in NtShell
     *
     * @param rhs
     * @return
     */
    NtValue applyAdd(NtValue rhs);

    /**
     * Similar to the effect of overriding the {@code operator()} inside a class
     * in C++
     *
     * @param params The parameters applied
     * @return The result of the call
     */
    NtValue applyCall(NtValue... params);

    /**
     * Overrides the behavior of the compose operator {@code .} in NtShell
     *
     * @param rhs
     * @return
     */
    NtValue applyCompose(NtValue rhs);

    /**
     * Overrides the behavior of the division operator {@code /} in NtShell
     *
     * @param rhs
     * @return
     */
    NtValue applyDiv(NtValue rhs);

    /**
     * Overrides the behavior of the modulo operator {@code mod} in NtShell
     *
     * @param rhs
     * @return
     */
    NtValue applyMod(NtValue rhs);

    /**
     * Overrides the behavior of the multiplication operator {@code *} in
     * NtShell
     *
     * @param rhs
     * @return
     */
    NtValue applyMul(NtValue rhs);

    /**
     * Overrides the behavior of the unary negative operator {@code -} in
     * NtShell
     *
     * @return
     */
    NtValue applyNegative();

    /**
     * Overrides the behavior of the unary percentage operator {@code %} in
     * NtShell
     *
     * @return
     */
    NtValue applyPercentage();

    /**
     * Overrides the behavior of the unary positive operator {@code +} in
     * NtShell
     *
     * @return
     */
    NtValue applyPositive();

    /**
     * Overrides the behavior of the power operator {@code ^} in NtShell
     *
     * @param rhs
     * @return
     */
    NtValue applyPow(NtValue rhs);

    /**
     * Overrides the behavior of the subtraction operator {@code -} in NtShell
     *
     * @param rhs
     * @return
     */
    NtValue applySub(NtValue rhs);

    /**
     * Indicates whether the value is truthy. Typically, a value resembling zero
     * or empty returns false on such query.
     *
     * @return if the value is truthy
     */
    boolean isTruthy();
}
