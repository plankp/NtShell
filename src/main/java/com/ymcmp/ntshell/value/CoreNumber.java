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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import java.util.Objects;

import org.apfloat.Apfloat;

/**
 * Numbers in NtShell in the form of rational numbers
 *
 * @author YTENG
 */
public class CoreNumber extends AbstractNtValue implements Comparable<CoreNumber> {

    public static final CoreNumber PI = new CoreNumber(884279719003555L, 281474976710656L);
    public static final CoreNumber E = new CoreNumber(6121026514868073L, 2251799813685248L);

    public static final CoreNumber ONE = new CoreNumber(1L);
    public static final CoreNumber ZERO = new CoreNumber(0L);
    public static final CoreNumber TEN = new CoreNumber(10L);

    public static final CoreNumber HALF = new CoreNumber(1L, 2L);
    public static final CoreNumber THIRD = new CoreNumber(1L, 3L);

    public static final CoreNumber TWO = new CoreNumber(2L);
    public static final CoreNumber THREE = new CoreNumber(3L);

    public static final CoreNumber NAN = new CoreNumber(0L, 0L);
    public static final CoreNumber POS_INF = new CoreNumber(1L, 0L);
    public static final CoreNumber NEG_INF = new CoreNumber(-1L, 0L);

    private BigInteger numerator;
    private BigInteger denominator;

    private CoreNumber(final long val) {
        this(BigInteger.valueOf(val), BigInteger.ONE);
    }

    private CoreNumber(final long numer, final long denom) {
        this(BigInteger.valueOf(numer), BigInteger.valueOf(denom));
    }

    private CoreNumber(final BigInteger val) {
        this(val, BigInteger.ONE);
    }

    private CoreNumber(final BigInteger numer, final BigInteger denom) {
        this.numerator = numer;
        this.denominator = denom;
    }

    @Override
    public String toString() {
        simplify();
        if (isNaN()) {
            return "Undefined";
        }
        if (isInfinite()) {
            if (isNegative()) {
                return "-Infinity";
            }
            return "Infinity";
        }
        return toDecimal()
                .stripTrailingZeros()
                .toPlainString();
    }

    public Apfloat toApfloat() {
        return new Apfloat(toDecimal());
    }

    public BigDecimal toDecimal() {
        return toDecimal(12);
    }

    public BigDecimal toDecimal(final int digits) {
        return toDecimal(digits, RoundingMode.HALF_UP);
    }

    public BigDecimal toDecimal(final int digits, final RoundingMode mode) {
        return new BigDecimal(numerator)
                .divide(new BigDecimal(denominator), digits, mode);
    }

    public void simplify() {
        final BigInteger gcd = numerator.gcd(denominator);
        if (gcd.compareTo(BigInteger.ZERO) > 0) {
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
        }

        // canonical form is that numerator takes the sign, denominator is
        // always positive.
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
    }

    public static CoreNumber getPi() {
        return PI;
    }

    public static CoreNumber getE() {
        return E;
    }

    public static CoreNumber from(Apfloat f) {
        return from(f.toString(false));
    }

    public static CoreNumber from(boolean b) {
        return b ? ONE : ZERO;
    }

    public static CoreNumber from(long val) {
        if (val == 0L) {
            return ZERO;
        }
        if (val == 1L) {
            return ONE;
        }
        if (val == 2L) {
            return TWO;
        }
        if (val == 3L) {
            return THREE;
        }
        if (val == 10L) {
            return TEN;
        }
        final CoreNumber n = new CoreNumber(val);
        n.simplify();
        return n;
    }

    public static CoreNumber from(long numer, long denom) {
        if (denom == 1L) {
            return from(numer);
        }

        if (numer == 1L && denom == 2L) {
            return HALF;
        }
        final CoreNumber n = new CoreNumber(numer, denom);
        n.simplify();
        return n;
    }

    public static CoreNumber from(double d) {
        if (Double.isNaN(d)) {
            return NAN;
        }
        if (Double.isInfinite(d)) {
            if (d < 0) {
                return NEG_INF;
            }
            return POS_INF;
        }
        return from(Double.toString(d));
    }

    public static CoreNumber from(String str) {
        final BigDecimal dec = new BigDecimal(str);
        return from(dec);
    }

    public static CoreNumber from(final BigDecimal dec) {
        final CoreNumber n = new CoreNumber(dec.unscaledValue(), BigInteger.TEN.pow(dec.scale()));
        n.simplify();
        return n;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.numerator);
        hash = 71 * hash + Objects.hashCode(this.denominator);
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
        return this.compareTo((CoreNumber) obj) == 0;
    }

    @Override
    public CoreNumber applyPositive() {
        // Does nothing
        return this;
    }

    @Override
    public CoreNumber applyNegative() {
        final CoreNumber n = new CoreNumber(numerator.negate(), denominator);
        n.simplify();
        return n;
    }

    @Override
    public CoreNumber applyPercentage() {
        final CoreNumber n = new CoreNumber(numerator,
                                            denominator.multiply(BigInteger.TEN).multiply(BigInteger.TEN));
        n.simplify();
        return n;
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
            final CoreNumber rhsn = (CoreNumber) rhs;
            final CoreNumber n;
            if (denominator.equals(rhsn.denominator)) {
                // 2/5 + 1/5 => (1+2)/5
                n = new CoreNumber(numerator.add(rhsn.numerator), denominator);
            } else {
                // 1/2 + 2/3 => (1*3 + 2*2)/(2*3)
                n = new CoreNumber(numerator.multiply(rhsn.denominator).add(rhsn.numerator.multiply(denominator)),
                                   denominator.multiply(rhsn.denominator));
            }
            n.simplify();
            return n;
        }
        if (rhs instanceof CoreMatrix) {
            // Addition is commutative
            return rhs.applyAdd(this);
        }
        return super.applyAdd(rhs);
    }

    @Override
    public NtValue applySub(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            final CoreNumber rhsn = (CoreNumber) rhs;
            final CoreNumber n;
            if (denominator.equals(rhsn.denominator)) {
                // 2/5 - 1/5 => (1-2)/5
                n = new CoreNumber(numerator.subtract(rhsn.numerator), denominator);
            } else {
                // 1/2 - 2/3 => (1*3 - 2*2)/(2*3)
                n = new CoreNumber(numerator.multiply(rhsn.denominator).subtract(rhsn.numerator.multiply(denominator)),
                                   denominator.multiply(rhsn.denominator));
            }
            n.simplify();
            return n;
        }
        if (rhs instanceof CoreMatrix) {
            return ((CoreMatrix) rhs).applyRSub(this);
        }
        return super.applySub(rhs);
    }

    @Override
    public NtValue applyMul(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            final CoreNumber rhsn = (CoreNumber) rhs;
            final CoreNumber n = new CoreNumber(numerator.multiply(rhsn.numerator),
                                                denominator.multiply(rhsn.denominator));
            n.simplify();
            return n;
        }
        if (rhs instanceof CoreMatrix) {
            // Multiplication is commutative
            return rhs.applyMul(this);
        }
        return super.applyMul(rhs);
    }

    @Override
    public NtValue applyDiv(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            final CoreNumber rhsn = (CoreNumber) rhs;
            final CoreNumber n = new CoreNumber(numerator.multiply(rhsn.denominator),
                                                denominator.multiply(rhsn.numerator));
            n.simplify();
            return n;
        }
        if (rhs instanceof CoreMatrix) {
            return ((CoreMatrix) rhs).applyRDiv(this);
        }
        return super.applyDiv(rhs);
    }

    @Override
    public NtValue applyMod(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            // a mod b = a - b*floor(a/b)
            final CoreNumber rhsn = (CoreNumber) rhs;
            final CoreNumber beFloor = (CoreNumber) this.applyDiv(rhsn);
            final CoreNumber n = (CoreNumber) applySub(rhsn.applyMul(new CoreNumber(beFloor.toDecimal(1, RoundingMode.FLOOR).toBigInteger())));
            n.simplify();
            return n;
        }
        if (rhs instanceof CoreMatrix) {
            return ((CoreMatrix) rhs).applyRMod(this);
        }
        return super.applyMod(rhs);
    }

    @Override
    public NtValue applyPow(NtValue rhs) {
        if (rhs instanceof CoreNumber) {
            // (1/2)^(2/3) => 1^(2/3)/2^(2/3)
            final CoreNumber rhsn = (CoreNumber) rhs;
            final CoreNumber n = new CoreNumber(numerator, denominator);
            if (rhsn.isNegative()) {
                n.inverse();
            }
            try {
                // only take absolute value of numerator since in canonical form,
                // denominator is always positive, and numerator dictates the sign
                n.numerator = pow(n.numerator, rhsn.numerator.abs());
                n.denominator = pow(n.denominator, rhsn.numerator.abs());
                n.simplify();

                final int cmp = rhsn.denominator.compareTo(BigInteger.ONE);
                if (cmp == 0) {
                    return n;
                }
                return CoreNumber.from(root(rhsn.denominator.intValueExact(), n.toDecimal()));
            } catch (IllegalArgumentException | ArithmeticException ex) {
                // Most likely caused by having a negative base when rooting
                return NAN;
            }
        }
        if (rhs instanceof CoreMatrix) {
            return ((CoreMatrix) rhs).applyRPow(this);
        }
        return super.applyPow(rhs);
    }

    private static BigInteger pow(final BigInteger base, final BigInteger exp) {
        if (exp.equals(BigInteger.ZERO)) {
            return BigInteger.ONE;
        }
        if (exp.equals(BigInteger.ONE)) {
            return base;
        }
        if (exp.signum() < 0) {
            throw new IllegalArgumentException("Exponent must be positive: " + exp);
        }

        // exponent by squaring (based on wikipedia article)
        BigInteger x = base;
        BigInteger y = BigInteger.ONE;
        BigInteger n = exp;
        while (n.compareTo(BigInteger.ONE) > 0) {
            if (n.testBit(0)) {
                // n is odd
                y = x.multiply(y);
            }
            x = x.pow(2);
            n = n.shiftRight(1);
        }
        return x.multiply(y);
    }

    private static BigDecimal root(final int exp, final BigDecimal base) {
        if (base.signum() == 0) {
            return BigDecimal.ZERO;
        }
        // If exp is even, it is impossible (real number range)
        // If exp is odd, calculate as if base was positive and add negative sign
        if (base.signum() < 0 && exp % 2 == 0) {
            throw new IllegalArgumentException("nth root can only be calculated for positive numbers");
        }
        final BigDecimal p = BigDecimal.valueOf(.1).movePointLeft(12);
        BigDecimal xPrev = base;
        BigDecimal x = base.divide(new BigDecimal(exp), 12, RoundingMode.HALF_DOWN);  // starting "guessed" value...
        while (x.subtract(xPrev).abs().compareTo(p) > 0) {
            xPrev = x;
            x = BigDecimal.valueOf(exp - 1.0)
                    .multiply(x)
                    .add(base.divide(x.pow(exp - 1), 12, RoundingMode.HALF_DOWN))
                    .divide(new BigDecimal(exp), 12, RoundingMode.HALF_DOWN);
        }
        return x;
    }

    public void inverse() {
        final BigInteger tmp = denominator;
        denominator = numerator;
        numerator = tmp;
    }

    @Override
    public boolean isTruthy() {
        simplify();
        return numerator.signum() != 0;
    }

    public boolean isNegative() {
        simplify();
        return numerator.signum() < 0;
    }

    public boolean isFinite() {
        // anything with a denominator of non-zero
        return denominator.signum() != 0;
    }

    public boolean isInfinite() {
        // x/0 where x != 0
        return numerator.signum() != 0 && denominator.signum() == 0;
    }

    public boolean isNaN() {
        return numerator.signum() == 0 && denominator.signum() == 0;
    }

    @Override
    public int compareTo(CoreNumber o) {
        // 3/4 vs 5/6 => 3*6=18, 4*5=20 => 5/6 is greater
        return numerator.multiply(o.denominator).compareTo(denominator.multiply(o.numerator));
    }

    public CoreNumber addOne() {
        final CoreNumber n = new CoreNumber(numerator.add(denominator), denominator);
        n.simplify();
        return n;
    }

    public CoreNumber abs() {
        final CoreNumber n = new CoreNumber(numerator.abs(), denominator.abs());
        n.simplify();
        return n;
    }
}
