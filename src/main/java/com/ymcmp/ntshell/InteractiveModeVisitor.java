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

import com.ymcmp.ntshell.rte.*;

import com.ymcmp.ntshell.ast.*;

import com.ymcmp.ntshell.value.*;

import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

/**
 *
 * @author YTENG
 */
public class InteractiveModeVisitor extends Visitor<NtValue> {

    private static final Pattern LIM_ROUND_UP = Pattern.compile("9\\.9999|99\\.999|999\\.99|9999\\.9|99999");
    private static final Pattern LIM_ROUND_DOWN = Pattern.compile("0\\.0000|00\\.000|000\\.00|0000\\.0|00000");
    private static final Map<String, NtValue> PREDEF = new HashMap<>();

    static {
        // Math Functions
        PREDEF.put("rad", new CoreLambda(new CoreLambda.Info("rad", "number -> number", "Converts a number into its representation in radians")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.toRadians(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("rad", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("deg", new CoreLambda(new CoreLambda.Info("deg", "number -> number", "Converts a number into its representation in degrees")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.toDegrees(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("deg", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("sin", new CoreLambda(new CoreLambda.Info("sin", "number -> number", "Calculates the sine of value in radians")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.sin(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("sin", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("sinh", new CoreLambda(new CoreLambda.Info("sinh", "number -> number", "Calculates the hyperbolic sine of value in radians")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.sinh(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("sinh", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("cos", new CoreLambda(new CoreLambda.Info("cos", "number -> number", "Calculates the cosine of value in radians")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.cos(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("cos", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("cosh", new CoreLambda(new CoreLambda.Info("sin", "number -> number", "Calculates the hyperbolic cosine of value in radians")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.cosh(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("cosh", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("tan", new CoreLambda(new CoreLambda.Info("tan", "number -> number", "Calculates the tangent of value in radians")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.tan(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("tan", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("tanh", new CoreLambda(new CoreLambda.Info("sin", "number -> number", "Calculates the hyperbolic tangent of value in radians")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.tanh(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("tanh", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("asin", new CoreLambda(new CoreLambda.Info("asin", "number -> number", "Calculates the inverse sine of value in radians")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.asin(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("asin", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("acos", new CoreLambda(new CoreLambda.Info("acos", "number -> number", "Calculates the inverse cosine of value in radians")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.acos(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("acos", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("atan", new CoreLambda(new CoreLambda.Info("atan", "number -> number  OR  (number, number) -> number", "Calculates the inverse tangent of value in radians. Mapped to the Java functions <code>Math.atan</code> and <code>Math.atan2</code>")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.atan(((CoreNumber) input[0]).toApfloat()));
                   }
                   if (input.length == 2
                           && input[0] instanceof CoreNumber
                           && input[1] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.atan2(((CoreNumber) input[0]).toApfloat(),
                                                                ((CoreNumber) input[1]).toApfloat()));
                   }
                   throw new DispatchException("atan", "Expected one or two numbers but got " + input.length);
               }
           });
        PREDEF.put("sqrt", new CoreLambda(new CoreLambda.Info("sqrt", "number -> number", "Calculates the square root of a value")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.sqrt(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("sqrt", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("cbrt", new CoreLambda(new CoreLambda.Info("sqrt", "number -> number", "Calculates the cube root of a value")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.cbrt(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("cbrt", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("square", new CoreLambda(new CoreLambda.Info("square", "number -> number", "Calculates the square of a value")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.pow(((CoreNumber) input[0]).toApfloat(), 2));
                   }
                   throw new DispatchException("square", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("cube", new CoreLambda(new CoreLambda.Info("cube", "number -> number", "Calculates the cube of a value")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.pow(((CoreNumber) input[0]).toApfloat(), 3));
                   }
                   throw new DispatchException("cube", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("abs", new CoreLambda(new CoreLambda.Info("abs", "number -> number", "Calculates the absolute value of a value")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.abs(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("abs", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("ceil", new CoreLambda(new CoreLambda.Info("ceil", "number -> number", "Calculates the ceiling of a value")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.ceil(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("ceil", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("floor", new CoreLambda(new CoreLambda.Info("floor", "number -> number", "Calculates the floor of a value")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.floor(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("floor", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("round", new CoreLambda(new CoreLambda.Info("round", "number -> number", "Rounds value to the nearest integer")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.round(((CoreNumber) input[0]).toApfloat(), 0, RoundingMode.HALF_UP));
                   }
                   throw new DispatchException("round", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("ln", new CoreLambda(new CoreLambda.Info("ln", "number -> number", "Calculates the natural logarithm of a value")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.log(((CoreNumber) input[0]).toApfloat()));
                   }
                   throw new DispatchException("ln", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("log", new CoreLambda(new CoreLambda.Info("log", "number -> number", "Calculates the base 10 logarithm of a value")) {
               @Override
               public NtValue applyCall(NtValue[] input) {
                   if (input.length == 1 && input[0] instanceof CoreNumber) {
                       return CoreNumber.from(ApfloatMath.log(((CoreNumber) input[0]).toApfloat(), new Apfloat(10L)));
                   }
                   throw new DispatchException("log", "Expected a number but got " + input.length);
               }
           });
        PREDEF.put("log_base", new CoreLambda(new CoreLambda.Info("log_base", "func(number) -> func", "Creates a logarithm with the specified base")) {
               @Override
               public NtValue applyCall(final NtValue[] params) {
                   if (params.length == 1 && params[0] instanceof CoreNumber) {
                       // log_base (10)(100) => 2
                       final Apfloat base = ((CoreNumber) params[0]).toApfloat();
                       return new CoreLambda(new CoreLambda.Info("$$log_base", "func(number) -> number", "Calculates the logarithm of a value with a predefined base")) {
                           @Override
                           public NtValue applyCall(final NtValue[] params) {
                               if (params.length == 1 && params[0] instanceof CoreNumber) {
                                   return CoreNumber.from(ApfloatMath.log(((CoreNumber) params[0]).toApfloat(), base));
                               }
                               throw new DispatchException("Expected one number but got " + params.length);
                           }
                       };
                   }
                   throw new DispatchException("log_base", "Expected one number but got " + params.length);
               }
           });
        PREDEF.put("lim_left", new CoreLambda(new CoreLambda.Info("lim_left", "func(func) -> number", "Calculates the left-handed limit of a function")) {
               @Override
               public NtValue applyCall(NtValue... f) {
                   if (f.length == 1) {
                       // lim_left (x -> 1/x)(0) => -Inf
                       return genLimitBody(f[0], true);
                   }
                   throw new DispatchException("lim_left", "Expected one parameter but got " + f.length);
               }
           });
        PREDEF.put("lim_right", new CoreLambda(new CoreLambda.Info("lim_right", "func(func) -> number", "Calculates the right-handed limit of a function")) {
               @Override
               public NtValue applyCall(NtValue... f) {
                   if (f.length == 1) {
                       // lim_right (x -> 1/x)(0) => Inf
                       return genLimitBody(f[0], false);
                   }
                   throw new DispatchException("lim_right", "Expected one parameter but got " + f.length);
               }
           });
        PREDEF.put("lim", new CoreLambda(new CoreLambda.Info("lim", "func(func) -> number", "Calculates the two-sided limit of a function")) {
               @Override
               public NtValue applyCall(NtValue[] f) {
                   if (f.length == 1) {
                       return new CoreLambda() {
                           @Override
                           public NtValue applyCall(NtValue[] x) {
                               final NtValue[] xRight = Arrays.copyOf(x, x.length);
                               System.out.println("APP-LEFT:  " + Arrays.toString(x));
                               final NtValue llim = genLimitBody(f[0], true).applyCall(x);
                               System.out.println("APP-RIGHT: " + Arrays.toString(xRight));
                               final NtValue rlim = genLimitBody(f[0], false).applyCall(xRight);
                               if (llim instanceof CoreNumber && ((CoreNumber) llim).isFinite() && llim.equals(rlim)) {
                                   return llim;
                               }
                               return CoreNumber.from(Double.NaN);
                           }
                       };
                   }
                   throw new DispatchException("lim", "Expected one parameter but got " + f.length);
               }
           });
    }

    private final CoreLambda FUNC_LOAD_FILE = new CoreLambda(new CoreLambda.Info("Load file", "atom -> number", "Tries to load a NtShell script into the current context. Returns anything but zero on success")) {
        @Override
        public NtValue applyCall(final NtValue[] params) {
            if (params.length == 1 && params[0] instanceof CoreAtom) {
                final String path = params[0].toString();
                try (final FileReader reader = new FileReader(path)) {
                    App.loadStartupFile(reader, InteractiveModeVisitor.this);
                    return CoreNumber.from(true);
                } catch (IOException ex) {
                }
            }
            return CoreNumber.from(false);
        }
    };

    private final Map<String, NtValue> vars;
    private final Frontend env;

    private boolean tailCall;

    public InteractiveModeVisitor(final Frontend env) {
        this.vars = new HashMap<>();
        this.env = env;
    }

    private InteractiveModeVisitor(final Map<String, NtValue> vars, final Frontend env) {
        this.vars = new HashMap<>(vars);
        this.env = env;
    }

    public void reset() {
        vars.clear();
    }

    public NtValue eval(final AST ast) {
        try {
            return visit(ast);
        } catch (TailCallTrigger ex) {
            return TailCallTrigger.applyTailCall(ex);
        }
    }

    @Override
    public CoreAtom visitAtomVal(final AtomVal atom) {
        return CoreAtom.from(atom.toAtom());
    }

    @Override
    public CoreUnit visitUnitVal(final UnitVal atom) {
        return CoreUnit.getInstance();
    }

    @Override
    public CoreNumber visitNumberVal(final NumberVal number) {
        return CoreNumber.from(number.toDouble());
    }

    @Override
    public NtValue visitVariableVal(final VariableVal variable) {
        final String name = variable.val.text;
        NtValue val = vars.get(name);
        if (val == null) {
            val = env.findDefinition(name);
            if (val == null) {
                val = PREDEF.get(name);
                if (val == null) {
                    if (name.equals("load_file")) {
                        return FUNC_LOAD_FILE;
                    }
                    throw new UndefinedHandleException("Variable " + name + " has not been defined");
                }
            }
        }
        return val;
    }

    @Override
    public CoreMatrix visitMatrixVal(final MatrixVal matrix) {
        if (matrix.columns.length == 0) {
            return CoreMatrix.getEmptyMatrix();
        }

        try {
            final NtValue[][] rows = new NtValue[matrix.columns[0].row.length][matrix.columns.length];
            for (int x = 0; x < rows.length; ++x) {
                final int columnCount = rows[x].length;
                for (int y = 0; y < columnCount; ++y) {
                    rows[x][y] = eval(matrix.getCell(x, y));
                }
            }
            return CoreMatrix.from(rows).transpose();
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
            throw new BadValueException("Matrix has bad shape", ex);
        }
    }

    @Override
    public CoreLambda visitAnonFuncVal(final AnonFuncVal anonFunc) {
        return new UserDefLambda(anonFunc);
    }

    @Override
    public NtValue visitPiecewiseFuncVal(final PiecewiseFuncVal piecewiseFunc) {
        // { val if cond }   val is guaranteed in tail call position
        for (final PiecewiseFuncVal.CaseBlock test : piecewiseFunc.cases) {
            if (eval(test.pred).isTruthy()) {
                final boolean tcSave = tailCall;
                tailCall = true;
                final NtValue ret = visit(test.expr);
                tailCall = tcSave;
                return ret;
            }
        }
        throw new UndefinedHandleException("Piecewise function did not handle all possible values!");
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

    @Override
    public NtValue visitApplyExpr(final ApplyExpr apply) {
        final NtValue instance = eval(apply.instance);
        final NtValue[] params = Arrays.stream(apply.params)
                .map(this::visit)
                .toArray(NtValue[]::new);
        return constructTailCall(instance, params);
    }

    private NtValue constructTailCall(final NtValue instance, final NtValue[] params) throws TailCallTrigger {
        if (tailCall) {
            throw new TailCallTrigger(instance, params);
        }
        return TailCallTrigger.call(instance, params);
    }

    @Override
    public NtValue visitPartialApplyExpr(final PartialApplyExpr apply) {
        // placeholders are all eagerly evaluated
        final NtValue applicant = eval(apply.applicant);
        final NtValue[] placeholders = Arrays.stream(apply.placeholders)
                .map(this::visit)
                .toArray(NtValue[]::new);
        return new CoreLambda() {
            @Override
            public NtValue applyCall(final NtValue[] remainder) {
                final NtValue[] params = new NtValue[placeholders.length + remainder.length];
                System.arraycopy(placeholders, 0, params, 0, placeholders.length);
                System.arraycopy(remainder, 0, params, placeholders.length, remainder.length);
                return constructTailCall(applicant, params);
            }
        };
    }

    @Override
    public NtValue visitUnaryExpr(final UnaryExpr unary) {
        final NtValue base = eval(unary.base);
        if (unary.prefix) {
            switch (unary.op.type) {
            case ADD:
                return base.applyPositive();
            case SUB:
                return base.applyNegative();
            default:
                throw new UnsupportedOperationException("Illegal usage operator of " + unary.op + " as prefix");
            }
        } else {
            switch (unary.op.type) {
            case PERCENT:
                return base.applyPercentage();
            default:
                throw new UnsupportedOperationException("Illegal usage operator of " + unary.op + " as postfix");
            }
        }
    }

    @Override
    public NtValue visitBinaryExpr(final BinaryExpr binary) {
        final NtValue lhs = eval(binary.lhs);
        final NtValue rhs = eval(binary.rhs);
        switch (binary.op.type) {
        case ADD:
            return lhs.applyAdd(rhs);
        case SUB:
            return lhs.applySub(rhs);
        case MUL:
            return lhs.applyMul(rhs);
        case DIV:
            return lhs.applyDiv(rhs);
        case MOD:
            return lhs.applyMod(rhs);
        case POW:
            return lhs.applyPow(rhs);
        case LT:
            if (lhs instanceof Comparable && rhs instanceof Comparable) {
                return CoreNumber.from(((Comparable<NtValue>) lhs).compareTo(rhs) < 0);
            }
            throw new DispatchException("< must be used on two comparables");
        case LE:
            if (lhs instanceof Comparable && rhs instanceof Comparable) {
                return CoreNumber.from(((Comparable<NtValue>) lhs).compareTo(rhs) <= 0);
            }
            throw new DispatchException("<= must be used on two comparables");
        case GE:
            if (lhs instanceof Comparable && rhs instanceof Comparable) {
                return CoreNumber.from(((Comparable<NtValue>) lhs).compareTo(rhs) >= 0);
            }
            throw new DispatchException(">= must be used on two comparables");
        case GT:
            if (lhs instanceof Comparable && rhs instanceof Comparable) {
                return CoreNumber.from(((Comparable<NtValue>) lhs).compareTo(rhs) > 0);
            }
            throw new DispatchException("> must be used on two comparables");
        case EQL:
            return CoreNumber.from(lhs.equals(rhs));
        case NEQ:
            return CoreNumber.from(!lhs.equals(rhs));
        case K_AND: // Shorthanded
            if (lhs.isTruthy()) {
                return CoreNumber.from(rhs.isTruthy());
            }
            return CoreNumber.from(false);
        case K_OR: // Shorthanded
            if (lhs.isTruthy()) {
                return CoreNumber.from(true);
            }
            return CoreNumber.from(rhs.isTruthy());
        case COMPOSE:
            return lhs.applyCompose(rhs);
        default:
            throw new UnsupportedOperationException("Illegal usage operator of " + binary.op + " as infix");
        }
    }

    @Override
    public NtValue visitCommutativeExpr(final CommutativeExpr commutative) {
        switch (commutative.op.type) {
        case ADD: {
            NtValue ret = null;
            for (int i = 0; i < commutative.nodes.length; ++i) {
                if (ret == null) {
                    ret = eval(commutative.nodes[i]);
                } else {
                    ret = ret.applyAdd(eval(commutative.nodes[i]));
                }
            }
            return ret;
        }
        case MUL: {
            NtValue ret = null;
            for (int i = 0; i < commutative.nodes.length; ++i) {
                if (ret == null) {
                    ret = eval(commutative.nodes[i]);
                } else {
                    ret = ret.applyMul(eval(commutative.nodes[i]));
                }
            }
            return ret;
        }
        default:
            throw new UnsupportedOperationException("Illegal usage operator of " + commutative.op + " as commutative infix");
        }
    }

    @Override
    public NtValue visitAssignExpr(AssignExpr assign) {
        final NtValue val = eval(assign.value);
        vars.put(assign.to.text, val);
        return val;
    }

    private static CoreLambda genLimitBody(final NtValue base,
                                           final boolean leftSide) {
        return new CoreLambda() {
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

    private class UserDefLambda extends CoreLambda {

        public final Map<String, NtValue> lambdaLocals = new HashMap<>();

        public final AnonFuncVal decl;

        public UserDefLambda(final AnonFuncVal decl) {
            super(new Info("<user defined lambda>", "Accepts " + decl.inputs.length + " parameter(s)", "<code>" + decl.toString() + "</code>"));
            this.decl = decl;
        }

        @Override
        public NtValue applyCall(final NtValue[] params) {
            // params -> val     val is guaranteed in tail call position
            if (params.length != decl.inputs.length) {
                throw new DispatchException("Expected " + decl.inputs.length + " parameter(s) but got " + params.length);
            }
            final InteractiveModeVisitor vis = new InteractiveModeVisitor(vars, env) {
                @Override
                public NtValue visitAssignExpr(final AssignExpr assign) {
                    final NtValue val = super.visitAssignExpr(assign);
                    // save it as a local variable
                    lambdaLocals.put(assign.to.text, val);
                    return val;
                }
            };
            vis.tailCall = true;
            // add local variables
            vis.vars.putAll(lambdaLocals);
            for (int i = 0; i < params.length; ++i) {
                vis.vars.put(decl.inputs[i].text, params[i]);
            }
            final NtValue ret = vis.visit(decl.output);
            return ret;
        }
    }
}
