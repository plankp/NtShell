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
package ntshell.rt.lib.rout;

import com.ymcmp.ntshell.NtValue;
import com.ymcmp.ntshell.rte.DispatchException;

import com.ymcmp.ntshell.rte.TailCallTrigger;

import com.ymcmp.ntshell.value.CoreLambda;
import com.ymcmp.ntshell.value.CoreNumber;

import java.util.Arrays;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author YTENG
 */
public final class Limits {

    private static final Pattern LIM_ROUND_UP = Pattern.compile("9\\.9999|99\\.999|999\\.99|9999\\.9|99999");
    private static final Pattern LIM_ROUND_DOWN = Pattern.compile("0\\.0000|00\\.000|000\\.00|0000\\.0|00000");
    private static final String LIM_LEFT_NAME = "left-handed limit";
    private static final String LIM_RIGHT_NAME = "right-handed limit";

    private static class Left extends CoreLambda {

        private static final CoreLambda INSTANCE = new Left();

        public Left() {
            super(new CoreLambda.Info(LIM_LEFT_NAME, "func(func) -> func", "Calculates the left-handed limit of a function"));
        }

        @Override
        public NtValue applyCall(final NtValue[] f) {
            if (f.length == 1) {
                // lim_left (x -> 1/x)(0) => -Inf
                return genLimitBody(f[0], true);
            }
            throw new DispatchException(LIM_LEFT_NAME, "Expected one parameter but got " + f.length);
        }
    }

    private static class Right extends CoreLambda {

        private static final CoreLambda INSTANCE = new Right();

        public Right() {
            super(new CoreLambda.Info(LIM_RIGHT_NAME, "func(func) -> func", "Calculates the right-handed limit of a function"));
        }

        @Override
        public NtValue applyCall(final NtValue[] f) {
            if (f.length == 1) {
                // lim_left (x -> 1/x)(0) => -Inf
                return genLimitBody(f[0], false);
            }
            throw new DispatchException(LIM_RIGHT_NAME, "Expected one parameter but got " + f.length);
        }
    }

    private static class Both extends CoreLambda {

        private static final CoreLambda INSTANCE = new Both();

        public Both() {
            super(new CoreLambda.Info("two-sided limit", "func(func) -> func", "Calculates the two-sided limit of a function"));
        }

        @Override
        public NtValue applyCall(final NtValue[] f) {
            if (f.length == 1) {
                return new CoreLambda() {
                    @Override
                    public NtValue applyCall(final NtValue[] x) {
                        final NtValue[] xRight = Arrays.copyOf(x, x.length);
                        final NtValue llim = genLimitBody(f[0], true).applyCall(x);
                        if (llim instanceof CoreNumber) {
                            final NtValue rlim = genLimitBody(f[0], false).applyCall(xRight);
                            if (((CoreNumber) llim).isFinite() && llim.equals(rlim)) {
                                return llim;
                            }
                        }
                        return CoreNumber.from(Double.NaN);
                    }
                };
            }
            throw new DispatchException("two-sided limit", "Expected one parameter but got " + f.length);
        }
    }

    public static CoreLambda getLeftSided() {
        return Left.INSTANCE;
    }

    public static CoreLambda getRightSided() {
        return Right.INSTANCE;
    }

    public static CoreLambda getBothSided() {
        return Both.INSTANCE;
    }

    private static CoreLambda genLimitBody(final NtValue base,
                                           final boolean leftSide) {
        return new CoreLambda(new CoreLambda.Info("$$" + (leftSide ? LIM_LEFT_NAME : LIM_RIGHT_NAME), "number -> number", "Calculates the value at the specified point. If the point did not yield a number, Undefined is returned.")) {
            @Override
            public NtValue applyCall(NtValue[] y) {
                final NtValue ret = TailCallTrigger.call(base, y);
                if (ret instanceof CoreNumber) {
                    final CoreNumber tmp = (CoreNumber) ret;
                    if (tmp.isFinite()) {
                        return ret;
                    }

                    final NtValue ky = y[0];
                    NtValue gap = CoreNumber.from(1, 100);
                    CoreNumber prev = CoreNumber.from(0);

                    CoreNumber delta = CoreNumber.from(Double.POSITIVE_INFINITY);
                    for (int i = 0; i < 5; ++i) {
                        gap = gap.applyMul(CoreNumber.from(1, 10));
                        if (leftSide) {
                            y[0] = ky.applySub(gap);
                        } else {
                            y[0] = ky.applyAdd(gap);
                        }

                        final CoreNumber current = (CoreNumber) TailCallTrigger.call(base, y);
                        final CoreNumber newDelta = ((CoreNumber) current.applySub(prev)).abs();

                        if (newDelta.compareTo(delta) <= 0) {
                            delta = newDelta;
                            prev = current;
                        } else if (current.compareTo(prev) < 0) {
                            return CoreNumber.from(Double.NEGATIVE_INFINITY);
                        } else if (current.compareTo(prev) > 0) {
                            return CoreNumber.from(Double.POSITIVE_INFINITY);
                        }
                    }
                    return limitRound(prev);
                }
                return CoreNumber.from(Double.NaN);
            }
        };
    }

    /**
     * Attempts to round a finite double when digits form a pattern of 99999 or
     * 00000. The input is directly returned otherwise. This does not always
     * round to the nearest non-floating point number.
     *
     * @param d The double being rounded
     * @return The rounded value (or the value if not finite)
     */
    private static CoreNumber limitRound(final CoreNumber d) {
        if (!d.isFinite()) {
            return d;
        }

        final String s = d.abs().toString();
        final boolean positive = d.compareTo(CoreNumber.from(0)) >= 0;

        final Matcher roundUpMatcher = LIM_ROUND_UP.matcher(s);
        if (roundUpMatcher.find()) {
            final int roundUpIdx = roundUpMatcher.start();
            if (roundUpIdx == 0) {
                // 99999 => 100000
                return CoreNumber.from((positive ? '1' : "-1") + s.replaceAll("\\d", "0"));
            }
            if (roundUpIdx >= 0 && roundUpIdx < s.length()) {
                //  199999 =>  200000
                // -199999 => -200000
                final char[] head = s.substring(0, roundUpIdx).toCharArray();
                final String tail = s.substring(roundUpIdx).replaceAll("\\d", "0");
                // Round
                if (head[head.length - 1] == '.') {
                    ++head[head.length - 2];
                } else {
                    ++head[head.length - 1];
                }
                return CoreNumber.from((positive ? '+' : '-') + String.valueOf(head) + tail);
            }
        }

        final Matcher roundDownMatcher = LIM_ROUND_DOWN.matcher(s);
        if (roundDownMatcher.find()) {
            final int roundDownIdx = roundDownMatcher.start();
            if (roundDownIdx == 0) {
                // 0.00001 => 0
                return CoreNumber.from((positive ? '+' : '-') + s.replaceAll("\\d", "0"));
            }
            if (roundDownIdx >= 0 && roundDownIdx < s.length()) {
                //  100001 =>  100000
                // -100001 => -100000
                final String head = s.substring(0, roundDownIdx);
                final String tail = s.substring(roundDownIdx).replaceAll("\\d", "0");
                return CoreNumber.from((positive ? '+' : '-') + head + tail);
            }
        }
        // Cannot be rounded: value was already in most rounded form
        return d;
    }
}
