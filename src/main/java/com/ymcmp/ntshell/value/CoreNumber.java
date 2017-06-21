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
 *
 * @author YTENG
 */
public class CoreNumber extends NtValue implements Comparable<CoreNumber> {

    private static class Helper {

        static final CoreNumber NEG_INF = new CoreNumber(Double.NEGATIVE_INFINITY);
        static final CoreNumber POS_INF = new CoreNumber(Double.POSITIVE_INFINITY);
        static final CoreNumber NaN = new CoreNumber(Double.NaN);
        static final CoreNumber NEG_ONE = new CoreNumber(-1);
        static final CoreNumber ONE = new CoreNumber(1);
        static final CoreNumber ZERO = new CoreNumber(0);

        static final CoreNumber THREE = new CoreNumber(3);
        static final CoreNumber PI = new CoreNumber(Math.PI);
        static final CoreNumber E = new CoreNumber(Math.E);
    }

    public final double val;

    private CoreNumber(double d) {
        this.val = d;
    }

    @Override
    public String toString() {
        return Double.toString(val);
    }

    public double toDouble() {
        return val;
    }

    public static CoreNumber getPi() {
        return Helper.PI;
    }

    public static CoreNumber getE() {
        return Helper.E;
    }

    public static CoreNumber from(boolean b) {
        return b ? Helper.ONE : Helper.ZERO;
    }

    public static CoreNumber from(double d) {
        // NaN does not equal to NaN, so no d == Double.NaN
        if (Double.isNaN(d)) {
            return Helper.NaN;
        }

        if (d == Double.POSITIVE_INFINITY) {
            return Helper.POS_INF;
        }
        if (d == Double.NEGATIVE_INFINITY) {
            return Helper.NEG_INF;
        }

        if (d == 0) {
            return Helper.ZERO;
        }
        if (d == 1) {
            return Helper.ONE;
        }
        if (d == -1) {
            return Helper.NEG_ONE;
        }
        if (d == 3) {
            return Helper.THREE;
        }

        if (d == Math.PI) {
            return Helper.PI;
        }
        if (d == Math.E) {
            return Helper.E;
        }
        return new CoreNumber(d);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.val) ^ (Double.doubleToLongBits(this.val) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CoreNumber other = (CoreNumber) obj;
        return Double.doubleToLongBits(this.val) == Double.doubleToLongBits(other.val);
    }

    @Override
    public CoreNumber applyPositive() {
        // Does nothing
        return this;
    }

    @Override
    public CoreNumber applyNegative() {
        return CoreNumber.from(-val);
    }

    @Override
    public CoreNumber applyPercentage() {
        return CoreNumber.from(val / 100.0);
    }

    @Override
    public NtValue applyCall(NtValue[] params) {
        if (params.length == 1) {
            return this.applyMul(params[0]);
        }
        return super.applyCall(params);
    }

    @Override
    public NtValue applyAdd(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            return CoreNumber.from(val + ((CoreNumber) rhs).val);
        }
        return super.applyAdd(rhs);
    }

    @Override
    public NtValue applySub(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            return CoreNumber.from(val - ((CoreNumber) rhs).val);
        }
        return super.applySub(rhs);
    }

    @Override
    public NtValue applyMul(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            return CoreNumber.from(val * ((CoreNumber) rhs).val);
        }
        return super.applyMul(rhs);
    }

    @Override
    public NtValue applyDiv(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            return CoreNumber.from(val / ((CoreNumber) rhs).val);
        }
        return super.applyDiv(rhs);
    }

    @Override
    public NtValue applyMod(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            return CoreNumber.from(val % ((CoreNumber) rhs).val);
        }
        return super.applyMod(rhs);
    }

    @Override
    public NtValue applyPow(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            return CoreNumber.from(Math.pow(val, ((CoreNumber) rhs).val));
        }
        return super.applyPow(rhs);
    }

    @Override
    public boolean isTruthy() {
        if (Double.isNaN(val)) {
            return false;
        }
        if (val == 0.0) {
            return false;
        }
        return true;
    }

    public boolean isFinite() {
        return Double.isFinite(val);
    }

    public boolean isInfinite() {
        return Double.isInfinite(val);
    }

    public boolean isNaN() {
        return Double.isNaN(val);
    }

    @Override
    public int compareTo(CoreNumber o) {
        return Double.compare(val, o.val);
    }
}
