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

import com.ymcmp.ntshell.rte.DispatchException;
import com.ymcmp.ntshell.NtValue;

import java.util.function.Function;

/**
 * The equivalent of an anonymous function object
 *
 * @author YTENG
 */
public abstract class CoreLambda extends NtValue {

    /**
     * Optional information of the function. If the function has none, it is set
     * to null or custom information supplied by the NtShell runtime.
     */
    public final Info info;

    /**
     * Additional information of an anonymous function. Typically used to
     * document library routines.
     */
    public static class Info {

        /**
         * The preferred name of the function
         */
        public final String name;

        /**
         * The type signature of the function
         */
        public final String type;

        /**
         * The detailed description of what the function does
         */
        public final String desc;

        public Info(String name, String type, String desc) {
            this.name = name;
            this.type = type;
            this.desc = desc;
        }

        @Override
        public String toString() {
            return String.format("%s :: %s\n  %s", name, type, desc);
        }
    }

    private static class LambdaHelper {

        static final CoreLambda ID = new CoreLambda(new Info("Identity function",
                                                             "any -> any",
                                                             "Takes a parameter of any type and returns it without modifying it.")) {
            @Override
            public NtValue applyCall(NtValue[] params) {
                if (params.length == 1) {
                    return params[0];
                }
                throw new DispatchException("identity", "Identity only takes one parameter: " + params.length + " recieved");
            }
        };
    }

    public CoreLambda() {
        this(null);
    }

    public CoreLambda(Info info) {
        this.info = info;
    }

    @Override
    public abstract NtValue applyCall(NtValue... params);

    @Override
    public String toString() {
        return String.format("<lambda@%s>", Integer.toHexString(this.hashCode()));
    }

    /**
     * {@inheritDoc} For this class, it always returns true.
     *
     * @return Always true
     */
    @Override
    public final boolean isTruthy() {
        return true;
    }

    /**
     * Wraps the underlying function into a {@link java.util.function.Function}
     *
     * @return A wrapper function
     */
    public Function<NtValue[], NtValue> toFunction() {
        return this::applyCall;
    }

    /**
     * Returns the singleton identity function. An identity function is one that
     * takes one parameter of any type and returns it as the result without any
     * modification.
     *
     * @return The identity function
     */
    public static CoreLambda getIdentityFunction() {
        return LambdaHelper.ID;
    }

    /**
     * Wraps a {@link java.util.function.Function} inside a CoreLambda
     *
     * @param f the function being delegated to
     * @return the delegate / wrapper
     */
    public static CoreLambda from(Function<NtValue[], NtValue> f) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return f.apply(params);
            }
        };
    }
}
