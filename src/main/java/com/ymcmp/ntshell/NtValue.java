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

import com.ymcmp.ntshell.value.CoreLambda;

/**
 * A common parent shared by all values used in the NtShell computation.
 *
 * @author YTENG
 */
public class NtValue {

    public NtValue applyPercentage() {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyPercentage();
            }
        };
    }

    public NtValue applyNegative() {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyNegative();
            }
        };
    }

    public NtValue applyPositive() {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyPositive();
            }
        };
    }

    public NtValue applyAdd(NtValue rhs) {
        // (f + 2)(4) => f(4) + (2)(4)
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyAdd(rhs.applyCall(params));
            }
        };
    }

    public NtValue applySub(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applySub(rhs.applyCall(params));
            }
        };
    }

    public NtValue applyMul(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyMul(rhs.applyCall(params));
            }
        };
    }

    public NtValue applyDiv(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyDiv(rhs.applyCall(params));
            }
        };
    }

    public NtValue applyMod(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyMod(rhs.applyCall(params));
            }
        };
    }

    public NtValue applyPow(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyPow(rhs.applyCall(params));
            }
        };
    }

    public NtValue applyCompose(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                // (f . g)(x) => f(g(x))
                return NtValue.this.applyCall(rhs.applyCall(params));
            }
        };
    }

    public NtValue applyCall(NtValue... params) {
        throw new DispatchException("()", "Type " + this.getClass() + " cannot be apply with " + params.length + " parameters");
    }

    public boolean isTruthy() {
        return true;
    }
}
