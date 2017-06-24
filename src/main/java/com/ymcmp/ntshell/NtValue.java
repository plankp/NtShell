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

    /**
     * Overrides the behavior of the unary percentage operator {@code %} in
     * NtShell. By default, {@code f%} will be invoked as {@code x -> f(x)%}.
     *
     * @return
     */
    public NtValue applyPercentage() {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyPercentage();
            }
        };
    }

    /**
     * Overrides the behavior of the unary negative operator {@code -} in
     * NtShell. By default, {@code -f} will be invoked as {@code x -> -f(x)}.
     *
     * @return
     */
    public NtValue applyNegative() {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyNegative();
            }
        };
    }

    /**
     * Overrides the behavior of the unary positive operator {@code +} in
     * NtShell. By default, {@code +f} will be invoked as {@code x -> +f(x)}.
     *
     * @return
     */
    public NtValue applyPositive() {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyPositive();
            }
        };
    }

    /**
     * Overrides the behavior of the addition operator {@code +} in NtShell. By
     * default, {@code (f + g)} will be invoked as {@code x -> f(x) + g(x)}.
     *
     * @param rhs
     * @return
     */
    public NtValue applyAdd(NtValue rhs) {
        // (f + 2)(4) => f(4) + (2)(4)
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyAdd(rhs.applyCall(params));
            }
        };
    }

    /**
     * Overrides the behavior of the subtraction operator {@code -} in NtShell.
     * By default, {@code (f - g)} will be invoked as {@code x -> f(x) - g(x)}.
     *
     * @param rhs
     * @return
     */
    public NtValue applySub(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applySub(rhs.applyCall(params));
            }
        };
    }

    /**
     * Overrides the behavior of the multiplication operator {@code *} in
     * NtShell. By default, {@code (f * g)} will be invoked as
     * {@code x -> f(x) * g(x)}.
     *
     * @param rhs
     * @return
     */
    public NtValue applyMul(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyMul(rhs.applyCall(params));
            }
        };
    }

    /**
     * Overrides the behavior of the division operator {@code /} in NtShell. By
     * default, {@code (f / g)} will be invoked as {@code x -> f(x) / g(x)}.
     *
     * @param rhs
     * @return
     */
    public NtValue applyDiv(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyDiv(rhs.applyCall(params));
            }
        };
    }

    /**
     * Overrides the behavior of the modulo operator {@code mod} in NtShell. By
     * default, {@code (f mod g)} will be invoked as {@code x -> f(x) mod g(x)}.
     *
     * @param rhs
     * @return
     */
    public NtValue applyMod(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyMod(rhs.applyCall(params));
            }
        };
    }

    /**
     * Overrides the behavior of the power operator {@code ^} in NtShell. By
     * default, {@code (f ^ g)} will be invoked as {@code x -> f(x) ^ g(x)}.
     *
     * @param rhs
     * @return
     */
    public NtValue applyPow(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return NtValue.this.applyCall(params).applyPow(rhs.applyCall(params));
            }
        };
    }

    /**
     * Overrides the behavior of the compose operator {@code .} in NtShell. In
     * most cases, this returns a function (default implementation). For
     * example: {@code (f . g)} is invoked as
     * {@code x -> f.applyCall(g.applyCall(x))}.
     *
     * @param rhs
     * @return
     */
    public NtValue applyCompose(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                // (f . g)(x) => f(g(x))
                return NtValue.this.applyCall(rhs.applyCall(params));
            }
        };
    }

    /**
     * Similar to the effect of overriding the {@code operator()} inside a class
     * in C++
     *
     * @param params The parameters applied
     * @return The result of the call
     */
    public NtValue applyCall(NtValue... params) {
        throw new DispatchException("()", this.getClass().getSimpleName() + " cannot be apply with " + params.length + " parameters");
    }

    /**
     * Indicates whether the value is truthy. Typically, a value resembling zero
     * or empty returns false on such query.
     *
     * @return if the value is truthy
     */
    public boolean isTruthy() {
        return true;
    }
}
