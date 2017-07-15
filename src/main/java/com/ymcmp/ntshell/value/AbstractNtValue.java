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
import com.ymcmp.ntshell.rte.DispatchException;
import com.ymcmp.ntshell.rte.TailCallTrigger;

import com.ymcmp.ntshell.value.CoreLambda;

/**
 *
 * @author YTENG
 */
public abstract class AbstractNtValue implements NtValue {

    /**
     * Overrides the behavior of the unary percentage operator {@code %} in
     * NtShell. By default, {@code f%} will be invoked as {@code x -> f(x)%}.
     *
     * @return
     */
    @Override
    public NtValue applyPercentage() {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return TailCallTrigger.call(AbstractNtValue.this, params).applyPercentage();
            }
        };
    }

    /**
     * Overrides the behavior of the unary negative operator {@code -} in
     * NtShell. By default, {@code -f} will be invoked as {@code x -> -f(x)}.
     *
     * @return
     */
    @Override
    public NtValue applyNegative() {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return TailCallTrigger.call(AbstractNtValue.this, params).applyNegative();
            }
        };
    }

    /**
     * Overrides the behavior of the unary positive operator {@code +} in
     * NtShell. By default, {@code +f} will be invoked as {@code x -> +f(x)}.
     *
     * @return
     */
    @Override
    public NtValue applyPositive() {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return TailCallTrigger.call(AbstractNtValue.this, params).applyPositive();
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
    @Override
    public NtValue applyAdd(NtValue rhs) {
        // (f + 2)(4) => f(4) + (2)(4)
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return TailCallTrigger.call(AbstractNtValue.this, params)
                        .applyAdd(TailCallTrigger.call(rhs, params));
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
    @Override
    public NtValue applySub(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return TailCallTrigger.call(AbstractNtValue.this, params)
                        .applySub(TailCallTrigger.call(rhs, params));
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
    @Override
    public NtValue applyMul(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return TailCallTrigger.call(AbstractNtValue.this, params)
                        .applyMul(TailCallTrigger.call(rhs, params));
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
    @Override
    public NtValue applyDiv(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return TailCallTrigger.call(AbstractNtValue.this, params)
                        .applyDiv(TailCallTrigger.call(rhs, params));
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
    @Override
    public NtValue applyMod(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return TailCallTrigger.call(AbstractNtValue.this, params)
                        .applyMod(TailCallTrigger.call(rhs, params));
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
    @Override
    public NtValue applyPow(NtValue rhs) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return TailCallTrigger.call(AbstractNtValue.this, params)
                        .applyPow(TailCallTrigger.call(rhs, params));
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
    @Override
    public NtValue applyCall(NtValue... params) {
        throw new DispatchException("()", this.getClass().getSimpleName() + " cannot be apply with " + params.length + " parameters");
    }

    /**
     * Indicates whether the value is truthy. Typically, a value resembling zero
     * or empty returns false on such query.
     *
     * @return if the value is truthy
     */
    @Override
    public boolean isTruthy() {
        return true;
    }
}
