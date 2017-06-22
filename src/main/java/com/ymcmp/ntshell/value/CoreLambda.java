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

import com.ymcmp.ntshell.DispatchException;
import com.ymcmp.ntshell.NtValue;

import java.util.function.Function;

/**
 *
 * @author YTENG
 */
public abstract class CoreLambda extends NtValue {

    public static class Info {

        public final String name;
        public final String type;
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

    public final Info info;

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

    public Function<NtValue[], NtValue> toFunction() {
        return x -> this.applyCall(x);
    }

    public static CoreLambda getIdentityFunction() {
        return LambdaHelper.ID;
    }

    public static CoreLambda from(Function<NtValue[], NtValue> f) {
        return new CoreLambda() {
            @Override
            public NtValue applyCall(NtValue[] params) {
                return f.apply(params);
            }
        };
    }
}
