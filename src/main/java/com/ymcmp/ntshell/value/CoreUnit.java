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

/**
 * The equivalent of {@code null} for NtShell. All operations on this data type
 * should result in Unit itself, and the state of the other operand should not
 * be mutated.
 *
 * @author YTENG
 */
public final class CoreUnit implements NtValue {

    private static class Helper {

        static final CoreUnit INST = new CoreUnit();
    }

    private CoreUnit() {
    }

    @Override
    public NtValue applyAdd(NtValue rhs) {
        return this;
    }

    @Override
    public NtValue applyCall(NtValue... params) {
        return this;
    }

    @Override
    public NtValue applyCompose(NtValue rhs) {
        return this;
    }

    @Override
    public NtValue applyDiv(NtValue rhs) {
        return this;
    }

    @Override
    public NtValue applyMod(NtValue rhs) {
        return this;
    }

    @Override
    public NtValue applyMul(NtValue rhs) {
        return this;
    }

    @Override
    public NtValue applyNegative() {
        return this;
    }

    @Override
    public NtValue applyPercentage() {
        return this;
    }

    @Override
    public NtValue applyPositive() {
        return this;
    }

    @Override
    public NtValue applyPow(NtValue rhs) {
        return this;
    }

    @Override
    public NtValue applySub(NtValue rhs) {
        return this;
    }

    @Override
    public String toString() {
        return "()";
    }

    @Override
    public boolean isTruthy() {
        return false;
    }

    public static CoreUnit getInstance() {
        return Helper.INST;
    }
}
