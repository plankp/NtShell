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

import com.ymcmp.ntshell.ast.*;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Collection;

import java.util.function.Function;
import java.util.function.BiFunction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author YTENG
 */
public class InteractiveModeVisitor extends Visitor<Object> {

    private static final Pattern LIM_ROUND_UP = Pattern.compile("9\\.9999|99\\.999|999\\.99|9999\\.9|99999");
    private static final Pattern LIM_ROUND_DOWN = Pattern.compile("0\\.0000|00\\.000|000\\.00|0000\\.0|00000");
    private static final Map<String, Object> PREDEF = new HashMap<>();

    static {
        // Constants
        PREDEF.put("pi", Math.PI);
        PREDEF.put("e", Math.E);

        // Math Functions
        PREDEF.put("rad", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.toRadians((Double) input[0]);
               }
               throw new DispatchException("rad", "Expected a number but got " + input.length);
           });
        PREDEF.put("deg", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.toDegrees((Double) input[0]);
               }
               throw new DispatchException("rad", "Expected a number but got " + input.length);
           });
        PREDEF.put("sin", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.sin((Double) input[0]);
               }
               throw new DispatchException("sin", "Expected a number but got " + input.length);
           });
        PREDEF.put("sinh", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.sinh((Double) input[0]);
               }
               throw new DispatchException("sinh", "Expected a number but got " + input.length);
           });
        PREDEF.put("cos", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.cos((Double) input[0]);
               }
               throw new DispatchException("cos", "Expected a number but got " + input.length);
           });
        PREDEF.put("cosh", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.cosh((Double) input[0]);
               }
               throw new DispatchException("cosh", "Expected a number but got " + input.length);
           });
        PREDEF.put("tan", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.tan((Double) input[0]);
               }
               throw new DispatchException("tan", "Expected a number but got " + input.length);
           });
        PREDEF.put("tanh", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.tanh((Double) input[0]);
               }
               throw new DispatchException("tanh", "Expected a number but got " + input.length);
           });
        PREDEF.put("asin", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.asin((Double) input[0]);
               }
               throw new DispatchException("asin", "Expected a number but got " + input.length);
           });
        PREDEF.put("acos", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.acos((Double) input[0]);
               }
               throw new DispatchException("acos", "Expected a number but got " + input.length);
           });
        PREDEF.put("atan", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.atan((Double) input[0]);
               }
               throw new DispatchException("atan", "Expected a number but got " + input.length);
           });
        PREDEF.put("atan2", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 2 && input[0] instanceof Double && input[1] instanceof Double) {
                   return Math.atan2((Double) input[0], (Double) input[1]);
               }
               throw new DispatchException("atan2", "Expected two numbers but got " + input.length);
           });
        PREDEF.put("sqrt", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.sqrt((Double) input[0]);
               }
               throw new DispatchException("sqrt", "Expected a number but got " + input.length);
           });
        PREDEF.put("cbrt", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.cbrt((Double) input[0]);
               }
               throw new DispatchException("cbrt", "Expected a number but got " + input.length);
           });
        PREDEF.put("abs", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.abs((Double) input[0]);
               }
               throw new DispatchException("abs", "Expected a number but got " + input.length);
           });
        PREDEF.put("ceil", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.ceil((Double) input[0]);
               }
               throw new DispatchException("ceil", "Expected a number but got " + input.length);
           });
        PREDEF.put("floor", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.floor((Double) input[0]);
               }
               throw new DispatchException("floor", "Expected a number but got " + input.length);
           });
        PREDEF.put("round", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.round((Double) input[0]);
               }
               throw new DispatchException("round", "Expected a number but got " + input.length);
           });
        PREDEF.put("ln", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   return Math.log((Double) input[0]);
               }
               throw new DispatchException("ln", "Expected a number but got " + input.length);
           });
        PREDEF.put("log", (Function<Object[], Object>) (Object... input) -> {
               if (input.length == 1 && input[0] instanceof Double) {
                   // log (100) => 2
                   return Math.log10((Double) input[0]);
               }
               if (input.length == 2 && input[0] instanceof Double && input[1] instanceof Double) {
                   // log (10, 100) => 2
                   return Math.log((Double) input[1]) / Math.log((Double) input[0]);
               }
               throw new DispatchException("log", "Expected one or two number but got " + input.length);
           });
        PREDEF.put("log_base", (Function<Object[], Function<Object[], Object>>) (Object... x) -> {
               if (x.length == 1 && x[0] instanceof Double) {
                   // log_base (10)(100) => 2
                   final double base = (Double) x[0];
                   return (Object... y) -> {
                       if (y.length == 1 && y[0] instanceof Double) {
                           return Math.log((Double) y[0]) / Math.log(base);
                       }
                       throw new DispatchException("Expected one number but got " + y.length);
                   };
               }
               throw new DispatchException("log_base", "Expected one number but got " + x.length);
           });
        PREDEF.put("lim_left", (Function<Object[], Function<Object[], ?>>) (Object... x) -> {
               if (x.length == 1 && x[0] instanceof Function<?, ?>) {
                   // lim_left(x -> 1/x)(0) => test 0.001, 0.0001, 0.00001, 0.000001 => -Inf
                   return genLimitBody((Function<Object[], Object>) x[0], InteractiveModeVisitor::subtractOperation);
               }
               throw new DispatchException("lim_left", "Expected one function but got " + x.length);
           });
        PREDEF.put("lim_right", (Function<Object[], Function<Object[], ?>>) (Object... x) -> {
               if (x.length == 1 && x[0] instanceof Function<?, ?>) {
                   // see lim_left
                   return genLimitBody((Function<Object[], Object>) x[0], InteractiveModeVisitor::addOperation);
               }
               throw new DispatchException("lim_right", "Expected one function but got " + x.length);
           });
        PREDEF.put("lim", (Function<Object[], Function<Object[], ?>>) (Object... x) -> {
               if (x.length == 1 && x[0] instanceof Function<?, ?>) {
                   return y -> {
                       final double llim = genLimitBody((Function<Object[], Object>) x[0], InteractiveModeVisitor::subtractOperation).apply(y);
                       final double rlim = genLimitBody((Function<Object[], Object>) x[0], InteractiveModeVisitor::addOperation).apply(y);
                       if (llim == rlim && Double.isFinite(llim)) {
                           return llim;
                       }
                       return Double.NaN;
                   };
               }
               throw new DispatchException("lim", "Expected one function but got " + x.length);
           });
    }

    private final Map<String, Object> vars = new HashMap<>();

    public InteractiveModeVisitor() {
    }

    private InteractiveModeVisitor(Map<String, Object> vars) {
        this.vars.putAll(vars);
    }

    @Override
    public Double visitNumberVal(final NumberVal number) {
        return number.toDouble();
    }

    @Override
    public Object visitVariableVal(final VariableVal variable) {
        Object val = vars.get(variable.val.text);
        if (val == null) {
            val = PREDEF.get(variable.val.text);
            if (val == null) {
                throw new RuntimeException("Variable " + variable.val.text + " has not been defined");
            }
        }
        return val;
    }

    @Override
    public Function<Object[], Object> visitAnonFuncVal(final AnonFuncVal anonFunc) {
        return (Object... input) -> {
            if (input.length != anonFunc.inputs.length) {
                throw new DispatchException("Expected " + anonFunc.inputs.length + " parameter(s) but got " + input.length);
            }
            final InteractiveModeVisitor vis = new InteractiveModeVisitor(vars);
            for (int i = 0; i < input.length; ++i) {
                vis.vars.put(anonFunc.inputs[i].text, input[i]);
            }
            return vis.visit(anonFunc.output);
        };
    }

    @Override
    public Object visitPiecewiseFuncVal(final PiecewiseFuncVal piecewiseFunc) {
        for (final PiecewiseFuncVal.CaseBlock test : piecewiseFunc.cases) {
            if (isTruthy(visit(test.pred))) {
                return visit(test.expr);
            }
        }
        throw new RuntimeException("Piecewise function did not handle all possible values!");
    }

    private static double limitRound(final double d) {
        final String s = Double.toString(Math.abs(d));
        final boolean positive = d >= 0;

        final Matcher roundUpMatcher = LIM_ROUND_UP.matcher(s);
        if (roundUpMatcher.find()) {
            final int roundUpIdx = roundUpMatcher.start();
            if (roundUpIdx == 0) {
                // 99999 => 100000
                return Double.parseDouble('1' + s.replaceAll("\\d", "0")) * (positive ? 1 : -1);
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
                return Double.parseDouble(String.valueOf(head) + tail) * (positive ? 1 : -1);
            }
        }

        final Matcher roundDownMatcher = LIM_ROUND_DOWN.matcher(s);
        if (roundDownMatcher.find()) {
            final int roundDownIdx = roundDownMatcher.start();
            if (roundDownIdx == 0) {
                // 0.00001 => 0
                return Double.parseDouble(s.replaceAll("\\d", "0")) * (positive ? 1 : -1);
            }
            if (roundDownIdx >= 0 && roundDownIdx < s.length()) {
                //  100001 =>  100000
                // -100001 => -100000
                final String head = s.substring(0, roundDownIdx);
                final String tail = s.substring(roundDownIdx).replaceAll("\\d", "0");
                return Double.parseDouble(head + tail) * (positive ? 1 : -1);
            }
        }
        // Cannot be rounded: value was already in most rounded form
        return d;
    }

    private static boolean isTruthy(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof Double) {
            final double d = (Double) obj;
            return !(d == 0 || Double.isNaN(d));
        }
        if (obj instanceof Float) {
            final float f = (Float) obj;
            return !(f == 0 || Float.isNaN(f));
        }
        if (obj instanceof Long) {
            return ((Long) obj) != 0;
        }
        if (obj instanceof Integer) {
            return ((Integer) obj) != 0;
        }
        if (obj instanceof Short) {
            return ((Short) obj) != 0;
        }
        if (obj instanceof Byte) {
            return ((Byte) obj) != 0;
        }
        if (obj instanceof Character) {
            return ((Character) obj) != 0;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() != 0;
        }
        if (obj instanceof Collection<?>) {
            return !((Collection<?>) obj).isEmpty();
        }
        return true;
    }

    @Override
    public Object visitApplyExpr(final ApplyExpr apply) {
        final Object instance = visit(apply.instance);
        final Object[] params = Arrays.stream(apply.params).map(this::visit).toArray();
        if (instance instanceof Function<?, ?>) {
            return ((Function<Object[], ?>) instance).apply(params);
        }
        if (instance instanceof Double) {
            if (params.length == 1 || params[0] instanceof Double) {
                return ((Double) instance) * ((Double) params[0]);
            }
            throw new DispatchException("Numerical multiplication is restricted to one rhs number");
        }
        throw new DispatchException("Type of " + instance.getClass() + " cannot be dispatched");
    }

    @Override
    public Object visitUnaryExpr(final UnaryExpr unary) {
        final Object base = visit(unary.base);
        if (unary.prefix) {
            switch (unary.op.type) {
            case ADD:
                return prefixPlusOperation(base);
            case SUB:
                return prefixMinusOperation(base);
            default:
                throw new UnsupportedOperationException("Illegal usage operator of " + unary.op + " as prefix");
            }
        } else {
            switch (unary.op.type) {
            case PERCENT:
                return percentageOperation(base);
            default:
                throw new UnsupportedOperationException("Illegal usage operator of " + unary.op + " as postfix");
            }
        }
    }

    private static Object percentageOperation(final Object base) {
        if (base instanceof Function<?, ?>) {
            final Function<Object[], ?> f = (Function<Object[], ?>) base;
            return (Function<Object[], Object>) (p) -> percentageOperation(f.apply(p));
        }
        if (base instanceof Double) {
            return ((Double) base) / 100.0;
        }
        throw new DispatchException("Percentage must be used on either a function or a number");
    }

    private static Object prefixPlusOperation(final Object base) {
        if (base instanceof Function<?, ?> || base instanceof Double) {
            return base;
        }
        throw new DispatchException("Prefix plus must be used on either a function or a number");
    }

    private static Object prefixMinusOperation(final Object base) {
        if (base instanceof Function<?, ?>) {
            final Function<Object[], ?> f = (Function<Object[], ?>) base;
            return (Function<Object[], Object>) (p) -> prefixMinusOperation(f.apply(p));
        }
        if (base instanceof Double) {
            return -((Double) base);
        }
        throw new DispatchException("Prefix minus must be used on either a function or a number");
    }

    @Override
    public Object visitBinaryExpr(final BinaryExpr binary) {
        final Object lhs = visit(binary.lhs);
        final Object rhs = visit(binary.rhs);
        switch (binary.op.type) {
        case ADD:
            return addOperation(lhs, rhs);
        case SUB:
            return subtractOperation(lhs, rhs);
        case MUL:
            return multiplyOperation(lhs, rhs);
        case DIV:
            return divideOperation(lhs, rhs);
        case MOD:
            return moduloOperation(lhs, rhs);
        case POW:
            return powerOperation(lhs, rhs);
        case LT:
            if (lhs instanceof Double && rhs instanceof Double) {
                return ((Double) lhs) < ((Double) rhs);
            }
            throw new DispatchException("< must be used on two numbers");
        case LE:
            if (lhs instanceof Double && rhs instanceof Double) {
                return ((Double) lhs) <= ((Double) rhs);
            }
            throw new DispatchException("<= must be used on two numbers");
        case GE:
            if (lhs instanceof Double && rhs instanceof Double) {
                return ((Double) lhs) >= ((Double) rhs);
            }
            throw new DispatchException(">= must be used on two numbers");
        case GT:
            if (lhs instanceof Double && rhs instanceof Double) {
                return ((Double) lhs) > ((Double) rhs);
            }
            throw new DispatchException("> must be used on two numbers");
        case EQL:
            if (lhs instanceof Double && rhs instanceof Double) {
                return ((Double) lhs).doubleValue() == ((Double) rhs).doubleValue();
            }
            throw new DispatchException("== must be used on two numbers");
        case NEQ:
            if (lhs instanceof Double && rhs instanceof Double) {
                return ((Double) lhs).doubleValue() != ((Double) rhs).doubleValue();
            }
            throw new DispatchException("/= must be used on two numbers");
        case K_AND: // Shorthanded
            if (isTruthy(lhs)) {
                return isTruthy(rhs);
            }
            return false;
        case K_OR: // Shorthanded
            if (isTruthy(lhs)) {
                return true;
            }
            return isTruthy(rhs);
        case COMPOSE:
            if (lhs instanceof Function<?, ?> && rhs instanceof Function<?, ?>) {
                final Function<Object[], ?> f = (Function<Object[], ?>) lhs;
                final Function<Object[], ?> g = (Function<Object[], ?>) rhs;
                return (Function<Object[], Object>) (p) -> f.apply(new Object[]{g.apply(p)});
            }
            throw new DispatchException("Compose must be used on two functions");
        default:
            throw new UnsupportedOperationException("Illegal usage operator of " + binary.op + " as infix");
        }
    }

    @Override
    public Object visitCommutativeExpr(final CommutativeExpr commutative) {
        switch (commutative.op.type) {
        case ADD: {
            Object ret = null;
            for (int i = 0; i < commutative.nodes.length; ++i) {
                if (ret == null) {
                    ret = visit(commutative.nodes[i]);
                } else {
                    ret = addOperation(ret, visit(commutative.nodes[i]));
                }
            }
            return ret;
        }
        case MUL: {
            Object ret = null;
            for (int i = 0; i < commutative.nodes.length; ++i) {
                if (ret == null) {
                    ret = visit(commutative.nodes[i]);
                } else {
                    ret = multiplyOperation(ret, visit(commutative.nodes[i]));
                }
            }
            return ret;
        }
        default:
            throw new UnsupportedOperationException("Illegal usage operator of " + commutative.op + " as commutative infix");
        }
    }

    private static Object powerOperation(final Object lhs, final Object rhs) {
        if (lhs instanceof Function<?, ?> && rhs instanceof Function<?, ?>) {
            final Function<Object[], ?> f = (Function<Object[], ?>) lhs;
            final Function<Object[], ?> g = (Function<Object[], ?>) rhs;
            return (Function<Object[], Object>) (p) -> powerOperation(f.apply(p), g.apply(p));
        }
        if (lhs instanceof Double && rhs instanceof Double) {
            return Math.pow((Double) lhs, (Double) rhs);
        }
        throw new DispatchException("Modulo must be used on either two functions or two numbers");
    }

    private static Object moduloOperation(final Object lhs, final Object rhs) {
        if (lhs instanceof Function<?, ?> && rhs instanceof Function<?, ?>) {
            final Function<Object[], ?> f = (Function<Object[], ?>) lhs;
            final Function<Object[], ?> g = (Function<Object[], ?>) rhs;
            return (Function<Object[], Object>) (p) -> moduloOperation(f.apply(p), g.apply(p));
        }
        if (lhs instanceof Double && rhs instanceof Double) {
            return ((Double) lhs) % ((Double) rhs);
        }
        throw new DispatchException("Modulo must be used on either two functions or two numbers");
    }

    private static Object divideOperation(final Object lhs, final Object rhs) {
        if (lhs instanceof Function<?, ?> && rhs instanceof Function<?, ?>) {
            final Function<Object[], ?> f = (Function<Object[], ?>) lhs;
            final Function<Object[], ?> g = (Function<Object[], ?>) rhs;
            return (Function<Object[], Object>) (p) -> divideOperation(f.apply(p), g.apply(p));
        }
        if (lhs instanceof Double && rhs instanceof Double) {
            return ((Double) lhs) / ((Double) rhs);
        }
        throw new DispatchException("Divide must be used on either two functions or two numbers");
    }

    private static Object multiplyOperation(final Object lhs, final Object rhs) {
        if (lhs instanceof Function<?, ?> && rhs instanceof Function<?, ?>) {
            final Function<Object[], ?> f = (Function<Object[], ?>) lhs;
            final Function<Object[], ?> g = (Function<Object[], ?>) rhs;
            return (Function<Object[], Object>) (p) -> multiplyOperation(f.apply(p), g.apply(p));
        }
        if (lhs instanceof Double && rhs instanceof Double) {
            return ((Double) lhs) * ((Double) rhs);
        }
        throw new DispatchException("Multiply must be used on either two functions or two numbers");
    }

    private static Object subtractOperation(final Object lhs, final Object rhs) {
        if (lhs instanceof Function<?, ?> && rhs instanceof Function<?, ?>) {
            final Function<Object[], ?> f = (Function<Object[], ?>) lhs;
            final Function<Object[], ?> g = (Function<Object[], ?>) rhs;
            return (Function<Object[], Object>) (p) -> subtractOperation(f.apply(p), g.apply(p));
        }
        if (lhs instanceof Double && rhs instanceof Double) {
            return ((Double) lhs) - ((Double) rhs);
        }
        throw new DispatchException("Subtract must be used on either two functions or two numbers");
    }

    private static Object addOperation(final Object lhs, final Object rhs) {
        if (lhs instanceof Function<?, ?> && rhs instanceof Function<?, ?>) {
            final Function<Object[], ?> f = (Function<Object[], ?>) lhs;
            final Function<Object[], ?> g = (Function<Object[], ?>) rhs;
            return (Function<Object[], Object>) (p) -> addOperation(f.apply(p), g.apply(p));
        }
        if (lhs instanceof Double && rhs instanceof Double) {
            return ((Double) lhs) + ((Double) rhs);
        }
        throw new DispatchException("Add must be used on either two functions or two numbers");
    }

    @Override
    public Object visitAssignExpr(AssignExpr assign) {
        final Object val = visit(assign.value);
        vars.put(assign.to.text, val);
        return val;
    }

    private static Function<Object[], Double> genLimitBody(final Function<Object[], ?> base,
                                                           final BiFunction<Object, Object, Object> trans) {
        return y -> {
            final Object ret = base.apply(y);
            if (ret instanceof Double) {
                final double tmp = (Double) ret;
                if (Double.isFinite(tmp)) {
                    return tmp;
                }

                final Object ky = y[0];
                double gap = 0.01;
                double prev = 0;

                double delta = Double.MAX_VALUE;
                for (int i = 0; i < 5; ++i) {
                    gap *= 0.1;
                    y[0] = trans.apply(ky, gap);

                    final double current = (Double) base.apply(y);
                    final double newDelta = Math.abs(current - prev);

                    if (newDelta <= delta) {
                        delta = newDelta;
                        prev = current;
                    } else if (current < prev) {
                        return Double.NEGATIVE_INFINITY;
                    } else if (current > prev) {
                        return Double.POSITIVE_INFINITY;
                    }
                }
                return limitRound(prev);
            }
            return Double.NaN;
        };
    }
}
