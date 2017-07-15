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

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author YTENG
 */
public class InteractiveModeVisitor extends Visitor<NtValue> {

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

    private final CoreLambda FUNC_EVAL = new CoreLambda(new CoreLambda.Info("eval", "any -> any OR () -> unit", "Returns the result if parameter is a quoted expression. The parameter is returned otherwise")) {
        @Override
        public NtValue applyCall(final NtValue[] params) {
            switch (params.length) {
            case 0:
                return CoreUnit.getInstance();
            case 1:
                final NtValue base = params[0];
                if (base instanceof AST) {
                    return InteractiveModeVisitor.this.eval((AST) base);
                }
                return base;
            default:
                throw new DispatchException("eval", "Expected less than two parameters, got " + params.length);
            }
        }
    };

    private final Map<String, NtValue> vars;
    private final Frontend env;

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
    public AST visitQexprVal(final QexprVal qexpr) {
        return qexpr.expr;
    }

    @Override
    public NtValue visitVariableVal(final VariableVal variable) {
        final String name = variable.val.text;
        NtValue val = vars.get(name);
        if (val == null) {
            val = env.findDefinition(name);
            if (val == null) {
                switch (name) {
                case "load_file":
                    return FUNC_LOAD_FILE;
                case "eval":
                    return FUNC_EVAL;
                default:
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
                return visit(test.expr);
            }
        }
        throw new UndefinedHandleException("Piecewise function did not handle all possible values!");
    }

    @Override
    public NtValue visitApplyExpr(final ApplyExpr apply) {
        final NtValue instance = eval(apply.instance);
        final NtValue[] params = Arrays.stream(apply.params)
                .map(this::eval)
                .toArray(NtValue[]::new);
        throw new TailCallTrigger(instance, params);
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
                throw new TailCallTrigger(applicant, params);
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
    public NtValue visitAssignExpr(final AssignExpr assign) {
        // check if variable exists. crash if not
        if (!assign.allocateNew && !vars.containsKey(assign.to.text)) {
            throw new UndefinedHandleException("Attempt to mutate value of non-existent variable " + assign.to.text);
        }

        final NtValue val = eval(assign.value);
        vars.put(assign.to.text, val);
        return val;
    }

    @Override
    public NtValue visitDoEndExpr(final DoEndExpr assign) {
        if (assign.exprs.length == 0) {
            // throws exception
            throw new UndefinedHandleException("Do-end expression does not contain body");
        }
        // do <expr 1> ... <expr n> end => <expr n> is tail-called
        for (int i = 0; i < assign.exprs.length - 1; ++i) {
            eval(assign.exprs[i]);
        }
        return visit(assign.exprs[assign.exprs.length - 1]);
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
                    final String varName = assign.to.text;
                    if (!assign.allocateNew && !lambdaLocals.containsKey(varName)) {
                        // only mutate the outer scope, do *not* save it locally
                        vars.put(varName, val);
                        return val;
                    }
                    // save it as a local variable
                    lambdaLocals.put(varName, val);
                    return val;
                }
            };
            // add local variables
            vis.vars.putAll(lambdaLocals);
            for (int i = 0; i < params.length; ++i) {
                vis.vars.put(decl.inputs[i].text, params[i]);
            }
            return vis.visit(decl.output);
        }
    }
}
