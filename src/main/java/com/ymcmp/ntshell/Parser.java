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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author YTENG
 */
public class Parser {

    private Frontend environment;

    public Parser() {
        this(null);
    }

    public Parser(final Frontend env) {
        this.environment = env;
    }

    public void switchFrontend(final Frontend env) {
        this.environment = env;
    }

    public AST consumeExpr(final List<Token> tokens) {
        return consumeLazyExpr(tokens);
    }

    public AST consumeLazyExpr(final List<Token> tokens) {
        // = <addlike>
        // | LAZY <addlike>
        switch (peekNextToken(tokens).type) {
        case K_LAZY:
            tokens.remove(0);
            // Pseudo-NtShell code
            // DEFINE-SYNTAX lazy<<expr>> -> (() -> do
            //   evaled? = ();
            //   value   = ();
            //   () -> {
            //     value  if evaled?,
            //     do
            //       evaled? <- 1;
            //       value   <- EVAL<<expr>>
            //     end    else
            //   };
            // end)();

            // Prefix these two names with space guarantees the user cannot
            // reference its value at runtime!
            final Token tEvaled = new Token(Token.Type.IDENT, " evaled?");
            final Token tValue = new Token(Token.Type.IDENT, " value");

            return new ApplyExpr(new AnonFuncVal(new Token[0], new DoEndExpr(
                                                 new AssignExpr(tEvaled, new UnitVal(), true),
                                                 new AssignExpr(tValue, new UnitVal(), true),
                                                 new AnonFuncVal(new Token[0], new PiecewiseFuncVal(new PiecewiseFuncVal.CaseBlock[]{
                                             new PiecewiseFuncVal.CaseBlock(new VariableVal(tEvaled),
                                                                            new VariableVal(tValue)),
                                             new PiecewiseFuncVal.ElseClause(new DoEndExpr(new AssignExpr(tEvaled, NumberVal.fromLong(1), false),
                                                                                           new AssignExpr(tValue, consumeAddLikeExpr(tokens), false)))})))),
                                 new AST[0]);
        default:
            return consumeAddLikeExpr(tokens);
        }
    }

    public AST consumeAddLikeExpr(final List<Token> tokens) {
        // = <mullike>
        // | <mullike> ((ADD | SUB) <mullike>)+
        AST lhs = consumeMulLikeExpr(tokens);
        cons_loop:
        while (true) {
            switch (peekNextToken(tokens).type) {
            case ADD:
            case SUB:
                final Token op = tokens.remove(0);
                lhs = new BinaryExpr(lhs, consumeMulLikeExpr(tokens), op);
                break;
            default:
                break cons_loop;
            }
        }
        return lhs;
    }

    public AST consumeMulLikeExpr(final List<Token> tokens) {
        // = <upre>
        // | <upre> <pow>
        // | <upre> ((MUL | DIV | MOD) <upre>)+
        AST lhs = consumeUPreExpr(tokens);
        cons_loop:
        while (true) {
            switch (peekNextToken(tokens).type) {
            case MUL:
            case DIV:
            case MOD:
                final Token op = tokens.remove(0);
                lhs = new BinaryExpr(lhs, consumeUPreExpr(tokens), op);
                break;
            default:
                final AST mulexpr = consumePowExpr(tokens);
                if (mulexpr == null) {
                    break cons_loop;
                }
                lhs = new BinaryExpr(lhs, mulexpr, new Token(Token.Type.MUL, null));
                break;
            }
        }
        return lhs;
    }

    public AST consumeUPreExpr(final List<Token> tokens) {
        // = <pow>
        // | (ADD | SUB) <pow>
        switch (peekNextToken(tokens).type) {
        case ADD:
        case SUB:
            final Token op = tokens.remove(0);
            final AST base = consumePowExpr(tokens);
            return new UnaryExpr(base, op, true);
        default:
            return consumePowExpr(tokens);
        }
    }

    public AST consumePowExpr(final List<Token> tokens) {
        // = <upost>
        // | <upost> POW <upre>
        final AST base = consumeUPostExpr(tokens);
        if (peekNextToken(tokens).type == Token.Type.POW) {
            final Token pow = tokens.remove(0);
            return new BinaryExpr(base, consumeUPreExpr(tokens), pow);
        }
        return base;
    }

    public AST consumeUPostExpr(final List<Token> tokens) {
        // = <compose> PERCENT?
        final AST base = consumeCompose(tokens);
        switch (peekNextToken(tokens).type) {
        case PERCENT:
            return new UnaryExpr(base, tokens.remove(0), false);
        default:
            return base;
        }
    }

    public AST consumeCompose(final List<Token> tokens) {
        // = <partial>
        // | <partial> COMPOSE <partial>
        AST base = consumePartialExpr(tokens);
        while (true) {
            if (peekNextToken(tokens).type == Token.Type.COMPOSE) {
                final Token op = tokens.remove(0);
                base = new BinaryExpr(base, consumePartialExpr(tokens), op);
            } else {
                return base;
            }
        }
    }

    public AST consumePartialExpr(final List<Token> tokens) {
        // = <apply>
        // | (<apply> SCOPE)+ <apply>
        final AST base = consumeApplyExpr(tokens);
        if (peekNextToken(tokens).type == Token.Type.SCOPE) {
            final List<AST> params = new ArrayList<>();
            params.add(base);
            while (peekNextToken(tokens).type == Token.Type.SCOPE) {
                tokens.remove(0);
                params.add(consumeApplyExpr(tokens));
            }
            final AST applicant = params.remove(params.size() - 1);
            final AST[] placeholders = params.toArray(new AST[params.size()]);
            if (applicant instanceof ApplyExpr) {
//                partialApply {
//                  placeholders: [iota(5)],
//                  applicant: reshape()(2, 2)
//                } # wrong!
//
//                partialApply {
//                  placeholders: [iota(5)],
//                  applicant: reshape
//                }()(2,2) # correct!
                final ApplyExpr expr = (ApplyExpr) applicant;
                return expr.wrapLeftMostApplicant(e -> new PartialApplyExpr(placeholders, e));
            }
            return new PartialApplyExpr(placeholders, applicant);
        }
        return base;
    }

    public AST consumeApplyExpr(final List<Token> tokens) {
        // = <val>
        // | <val> LBRACE RBRACE <apply>
        // | <val> LBRACE <expr> (COMMA <expr>)* COMMA? RBRACE <apply>
        AST base = consumeVal(tokens);
        while (peekNextToken(tokens).type == Token.Type.LBRACE) {
            tokens.remove(0);
            final List<AST> applicants = new ArrayList<>();
            while (peekNextToken(tokens).type != Token.Type.RBRACE) {
                try {
                    applicants.add(consumeExpr(tokens));
                    switch (peekNextToken(tokens).type) {
                    case COMMA:
                        tokens.remove(0);
                        break;
                    case RBRACE:
                        break; // the loop will deal with it
                    default:
                        throw new ParserException("Expected a , or ) but found " + peekNextToken(tokens) + " instead");
                    }
                } catch (RuntimeException ex) {
                    throw new ParserException("Paramters need to be split with commas", ex);
                }
            }
            tokens.remove(0);
            base = new ApplyExpr(base, applicants.toArray(new AST[applicants.size()]));
        }
        return base;
    }

    public AST consumeVal(final List<Token> tokens) {
        if (tokens.isEmpty()) {
            return null;
        }

        switch (peekNextToken(tokens).type) {
        case NUMBER:
            return new NumberVal(tokens.remove(0));
        case ATOM:
            return new AtomVal(tokens.remove(0));
        case IDENT: {
            final Token name = tokens.remove(0);
            // = IDENT YIELD <expr>  (0)
            // | IDENT DECL <expr>   (1)
            // | IDENT               (2)
            // | IDENT SET <expr>    (3)
            switch (peekNextToken(tokens).type) {
            case YIELD:
                // (0)
                return new AnonFuncVal(name, consumeYield(tokens));
            case DECL:
                // (1)
                tokens.remove(0);
                return new AssignExpr(name, consumeExpr(tokens), true);
            case SET:
                // (3)
                tokens.remove(0);
                return new AssignExpr(name, consumeExpr(tokens), false);
            default:
                // (2)
                return new VariableVal(name);
            }
        }
        case QEXPR:
            // QEXPR <val>
            tokens.remove(0);
            return new QexprVal(consumeVal(tokens));
        case LCURL:
            return consumePiecewiseFunc(tokens);
        case LBLK:
            return consumeMatrix(tokens);
        case K_DO:
            return consumeDoEnd(tokens);
        case LBRACE: {
            // = LBRACE <expr> RBRACE                                        (0)
            // | LBRACE RBRACE YIELD <expr>                                  (1)
            // | LBRACE <ident> (COMMA <ident>)+ COMMA? RBRACE YIELD <expr>  (2)
            // | LBRACE <ident> RBRACE YIELD <expr>                          (3)
            // | LBRACE RBRACE                                               (4)
            tokens.remove(0);
            switch (tokens.get(0).type) {
            case RBRACE:
                // (1)
                tokens.remove(0);
                if (peekNextToken(tokens).type != Token.Type.YIELD) {
                    // (4)
                    return new UnitVal();
                }
                return new AnonFuncVal(new Token[0], consumeYield(tokens));
            case IDENT: {
                final Token first = tokens.remove(0);
                switch (peekNextToken(tokens).type) {
                case RBRACE:
                    // (0)
                    tokens.remove(0);
                    if (peekNextToken(tokens).type == Token.Type.YIELD) {
                        return new AnonFuncVal(first, consumeYield(tokens));
                    }
                    return new VariableVal(first);
                case COMMA: {
                    // (2)
                    final List<Token> inputs = new ArrayList<>();
                    inputs.add(first);
                    tokens.remove(0);
                    while (peekNextToken(tokens).type == Token.Type.IDENT) {
                        inputs.add(tokens.remove(0));
                        if (peekNextToken(tokens).type == Token.Type.COMMA) {
                            tokens.remove(0);
                        } else {
                            break;
                        }
                    }
                    if (peekNextToken(tokens).type == Token.Type.RBRACE) {
                        tokens.remove(0);
                        return new AnonFuncVal(inputs.toArray(new Token[inputs.size()]),
                                               consumeYield(tokens));
                    } else {
                        throw new ParserException("Anonymous function parameter list unclosed");
                    }
                }
                default:
                    // (0)
                    tokens.add(0, first);
                    return consumeTrailBrace(tokens);
                }
            }
            default:
                return consumeTrailBrace(tokens);
            }
        }
        default:
            return null;
        }
    }

    private AST consumeDoEnd(final List<Token> tokens) throws ParserException {
        // = K_DO K_END
        // | K_DO <expr> SEMI? K_END
        // | K_DO <expr> (SEMI <expr>)+ SEMI? K_END
        tokens.remove(0);
        final List<AST> exprs = new ArrayList<>();
        while (peekNextToken(tokens).type != Token.Type.K_END) {
            exprs.add(consumeExpr(tokens));
            switch (peekNextToken(tokens).type) {
            case SEMI:
                tokens.remove(0);
                break;
            case K_END:
                continue; // let while loop handle this case
            default:
                throw new ParserException("Expressions in do-end are separated by semicolons");
            }
        }
        tokens.remove(0);
        return new DoEndExpr(exprs.toArray(new AST[exprs.size()]));
    }

    private AST consumeMatrix(final List<Token> tokens) throws ParserException {
        // = LBLK RBLK         (0)
        // | LBLK <row>+ RBLK  (1)
        tokens.remove(0);
        final List<MatrixVal.Column> elems = new ArrayList<>();
        while (peekNextToken(tokens).type != Token.Type.RBLK) {
            try {
                elems.add(consumeRow(tokens));
            } catch (MatrixRowUnclosedException ex) {
                if (peekNextToken(tokens).type == Token.Type.RBLK) {
                    elems.add(ex.currentColumn);
                    break;
                }
                throw new ParserException(ex.getMessage());
            }
        }
        tokens.remove(0);
        return new MatrixVal(elems.toArray(new MatrixVal.Column[elems.size()]));
    }

    private AST consumePiecewiseFunc(final List<Token> tokens) throws ParserException {
        // = LCURL <$case> (COMMA <$case>)+ COMMA? RCURL
        tokens.remove(0);
        final List<PiecewiseFuncVal.CaseBlock> cases = new ArrayList<>();
        cons_loop:
        while (peekNextToken(tokens).type != Token.Type.RCURL) {
            final PiecewiseFuncVal.CaseBlock clause = consumeCase(tokens);
            cases.add(clause);
            switch (peekNextToken(tokens).type) {
            case COMMA:
                tokens.remove(0);
                // If its an else clause, its the last clause of the function
                if (clause instanceof PiecewiseFuncVal.ElseClause) {
                    if (peekNextToken(tokens).type == Token.Type.RCURL) {
                        break cons_loop;
                    }
                    throw new ParserException("No additional clauses can be placed after an else clause");
                }
                break;
            case RCURL:
                break cons_loop;
            default:
                throw new ParserException("Case statements are split with commas, found " + peekNextToken(tokens));
            }
        }
        tokens.remove(0);
        if (cases.isEmpty()) {
            return new NumberVal(new Token(Token.Type.NUMBER, "0"));
        }
        return new PiecewiseFuncVal(cases.toArray(new PiecewiseFuncVal.CaseBlock[cases.size()]));
    }

    private AST consumeTrailBrace(final List<Token> tokens) {
        // try (0)
        final AST expr = consumeExpr(tokens);
        if (peekNextToken(tokens).type == Token.Type.RBRACE) {
            tokens.remove(0);
            return expr;
        } else {
            throw new ParserException("Missing )");
        }
    }

    private MatrixVal.Column consumeRow(final List<Token> tokens) throws MatrixRowUnclosedException {
        // = <expr> SEMI
        // | <expr> (COMMA <expr>)* COMMA? SEMI
        final List<AST> elems = new ArrayList<>();
        cons_loop:
        while (true) {
            switch (peekNextToken(tokens).type) {
            case SEMI:
                tokens.remove(0);
                break cons_loop;
            case RBLK:
                break cons_loop;
            default:
            }

            elems.add(consumeExpr(tokens));
            switch (peekNextToken(tokens).type) {
            case COMMA:
                tokens.remove(0);
                break;
            case SEMI:
                break;
            default:
                throw new MatrixRowUnclosedException(new MatrixVal.Column(elems.toArray(new AST[elems.size()])));
            }
        }
        return new MatrixVal.Column(elems.toArray(new AST[elems.size()]));
    }

    private AST consumeYield(final List<Token> tokens) {
        if (peekNextToken(tokens).type == Token.Type.YIELD) {
            tokens.remove(0);
            return consumeExpr(tokens);
        } else {
            throw new ParserException("Missing -> for input-less function");
        }
    }

    private PiecewiseFuncVal.CaseBlock consumeCase(final List<Token> tokens) {
        // = <expr> IF <$pred>
        // | <expr> ELSE
        final AST action = consumeExpr(tokens);
        switch (peekNextToken(tokens).type) {
        case K_IF:
            tokens.remove(0);
            return new PiecewiseFuncVal.CaseBlock(consumePred(tokens), action);
        case K_ELSE:
            tokens.remove(0);
            return new PiecewiseFuncVal.ElseClause(action);
        default:
            throw new ParserException("Each piecewise case requires a condition");
        }
    }

    public AST consumePred(final List<Token> tokens) {
        return consumeOr(tokens);
    }

    private AST consumeOr(final List<Token> tokens) {
        // = <and> (OR <and>)+
        // | <and>
        AST base = consumeAnd(tokens);
        while (true) {
            if (peekNextToken(tokens).type == Token.Type.K_OR) {
                final Token op = tokens.remove(0);
                base = new BinaryExpr(base, consumeAnd(tokens), op);
            } else {
                return base;
            }
        }
    }

    private AST consumeAnd(final List<Token> tokens) {
        // = <rel> (AND <rel>)+
        // | <rel>
        AST base = consumeRel(tokens);
        while (true) {
            if (peekNextToken(tokens).type == Token.Type.K_AND) {
                final Token op = tokens.remove(0);
                base = new BinaryExpr(base, consumeRel(tokens), op);
            } else {
                return base;
            }
        }
    }

    private AST consumeRel(final List<Token> tokens) {
        // = <expr> (LT | LE | GE | GT | EQL | NEQ) <expr>
        // | <expr>
        final AST lhs = consumeExpr(tokens);
        switch (peekNextToken(tokens).type) {
        case LT:
        case LE:
        case GE:
        case GT:
        case EQL:
        case NEQ:
            final Token op = tokens.remove(0);
            return new BinaryExpr(lhs, consumeExpr(tokens), op);
        default:
            return lhs;
        }
    }

    public Token peekNextToken(final List<Token> toks) {
        while (toks.isEmpty()) {
            if (environment == null) {
                return Token.getNilToken();
            }
            final String line = environment.readLine();
            if (line == null || line.isEmpty()) {
                return Token.getNilToken();
            }
            try {
                toks.addAll(Lexer.lexFromString(line));
            } catch (LexerException ex) {
                environment.errWriteLine(ex.getMessage());
                environment.errWriteLine("That line will be ignored!");
            }
        }
        return toks.get(0);
    }
}
