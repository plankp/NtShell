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

import java.awt.HeadlessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author YTENG
 */
public class App {

    private static final Map<String, Token.Type> KWORDS = new HashMap<>();

    static {
        KWORDS.put("mod", Token.Type.MOD);
        KWORDS.put("if", Token.Type.K_IF);
        KWORDS.put("and", Token.Type.K_AND);
        KWORDS.put("or", Token.Type.K_OR);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean gui = true; // terminal as fallback
        if (args.length == 1) {
            switch (args[0]) {
            case "-t":
            case "--term":
                gui = false;
                break;
            case "-g":
            case "--gui":
                gui = true;
                break;
            default:
                System.out.println("Unrecognized option " + args[0]);
            // FALLTHROUGH
            case "-h":
            case "--help":
                System.out.println("Options are --term -t --gui -g --help -h");
                return;
            }
        }

        if (gui) {
            try (final Frontend inst = new SwingMode()) {
                interactiveMode(inst);
                return;
            } catch (HeadlessException ex) {
                System.err.println("Fallback to terminal!");
            }
        }

        try (final Frontend inst = new ConsoleMode()) {
            interactiveMode(inst);
        }
    }

    public static void interactiveMode(final Frontend env) {
        env.writeLine("NtShell (interactive mode)\nType `~help` for help\n");
        boolean showAST = false;
        boolean transNeg = true;
        boolean levelOp = true;
        boolean simplifyRat = true;
        boolean unfoldConst = true;
        boolean evaluate = true;
        InteractiveModeVisitor session = new InteractiveModeVisitor(env);

        while (true) {
            final String input = env.readLine();
            switch (input) {
            case "~exit":
                return;
            case "~help":
                env.writeLine("Enter the expression you want to test\nEnd the line with `\\` to wrap on the next line\nWhen the expression is done, punch in a `;`\n\nCommands:\n  ~help ~exit ~restart ~showast ~hideast\n  ~transneg ~no-transneg ~levelop ~no-levelop\n  ~simprat ~no-simprat ~unfoldc ~no-unfoldc\n  ~eval ~no-eval");
                continue;
            case "~showast":
                showAST = true;
                continue;
            case "~hideast":
                showAST = false;
                continue;
            case "~transneg":
                transNeg = true;
                continue;
            case "~no-transneg":
                transNeg = false;
                continue;
            case "~levelop":
                levelOp = true;
                continue;
            case "~no-levelop":
                levelOp = false;
                continue;
            case "~simprat":
                simplifyRat = true;
                continue;
            case "~no-simprat":
                simplifyRat = false;
                continue;
            case "~unfoldc":
                unfoldConst = true;
                continue;
            case "~no-unfoldc":
                unfoldConst = false;
                continue;
            case "~eval":
                evaluate = true;
                continue;
            case "~no-eval":
                evaluate = false;
                continue;
            case "~restart":
                session = new InteractiveModeVisitor(env);
                continue;
            case "":
                continue;
            default:
                if (input.charAt(0) == '~') {
                    env.errWriteLine("Unrecognized command " + input);
                    env.errWriteLine("Type `~help` for help");
                    continue;
                }
            }

            try {
                final List<Token> toks = lexString(input, env);

                while (!toks.isEmpty()) {
                    AST ast = consumeExpr(toks, env);
                    if (showAST) {
                        env.writeLine("showast:  " + ast);
                    }
                    ast = ast.unfoldConstant();

                    if (transNeg) {
                        ast = ast.transformNegatives();
                        if (showAST) {
                            env.writeLine("transneg: " + ast);
                        }
                    }
                    if (levelOp) {
                        ast = ast.levelOperators();
                        if (showAST) {
                            env.writeLine("levelop:  " + ast);
                        }
                    }
                    if (simplifyRat) {
                        ast = ast.simplifyRationals();
                        if (showAST) {
                            env.writeLine("simprat:  " + ast);
                        }
                    }
                    if (unfoldConst) {
                        ast = ast.unfoldConstant();
                        if (showAST) {
                            env.writeLine("unfoldc:  " + ast);
                        }
                    }

                    if (evaluate) {
                        env.writeLine(session.visit(ast));
                    }
                    while (!toks.isEmpty() && toks.get(0).type == Token.Type.SEMI) {
                        toks.remove(0);
                    }
                }
            } catch (IllegalArgumentException ex) {
                env.errWriteLine(ex);
            } catch (NullPointerException ex) {
                env.errWriteLine("Syntax error?");
            } catch (LexerException | RuntimeException ex) {
                env.errWriteLine(ex);
            }
        }
    }

    public static AST consumeExpr(final List<Token> tokens, final Frontend env) {
        return consumeAddLikeExpr(tokens, env);
    }

    public static AST consumeAddLikeExpr(final List<Token> tokens, final Frontend env) {
        // = <mullike>
        // | <mullike> ((ADD | SUB) <mullike>)+
        AST lhs = consumeMulLikeExpr(tokens, env);
        cons_loop:
        while (true) {
            switch (peekNextToken(tokens, env).type) {
            case ADD:
            case SUB:
                final Token op = tokens.remove(0);
                lhs = new BinaryExpr(lhs, consumeMulLikeExpr(tokens, env), op);
                break;
            default:
                break cons_loop;
            }
        }
        return lhs;
    }

    public static AST consumeMulLikeExpr(final List<Token> tokens, final Frontend env) {
        // = <upre>
        // | <upre> <pow>
        // | <upre> ((MUL | DIV | MOD) <upre>)+
        AST lhs = consumeUPreExpr(tokens, env);
        cons_loop:
        while (true) {
            switch (peekNextToken(tokens, env).type) {
            case MUL:
            case DIV:
            case MOD:
                final Token op = tokens.remove(0);
                lhs = new BinaryExpr(lhs, consumeUPreExpr(tokens, env), op);
                break;
            default:
                final AST mulexpr = consumePowExpr(tokens, env);
                if (mulexpr == null) {
                    break cons_loop;
                }
                lhs = new BinaryExpr(lhs, mulexpr, new Token(Token.Type.MUL, null));
                break;
            }
        }
        return lhs;
    }

    public static AST consumeUPreExpr(final List<Token> tokens, final Frontend env) {
        // = <pow>
        // | (ADD | SUB) <pow>
        switch (peekNextToken(tokens, env).type) {
        case ADD:
        case SUB:
            final Token op = tokens.remove(0);
            final AST base = consumePowExpr(tokens, env);
            return new UnaryExpr(base, op, true);
        default:
            return consumePowExpr(tokens, env);
        }
    }

    public static AST consumePowExpr(final List<Token> tokens, final Frontend env) {
        // = <upost>
        // | <upost> POW <pow>
        final AST base = consumeUPostExpr(tokens, env);
        if (peekNextToken(tokens, env).type == Token.Type.POW) {
            final Token pow = tokens.remove(0);
            return new BinaryExpr(base, consumePowExpr(tokens, env), pow);
        }
        return base;
    }

    public static AST consumeUPostExpr(final List<Token> tokens, final Frontend env) {
        // = <compose> PERCENT?
        final AST base = consumeCompose(tokens, env);
        switch (peekNextToken(tokens, env).type) {
        case PERCENT:
            return new UnaryExpr(base, tokens.remove(0), false);
        default:
            return base;
        }
    }

    public static AST consumeCompose(final List<Token> tokens, final Frontend env) {
        // = <apply> COMPOSE <apply>
        // | <apply>
        AST base = consumeApplyExpr(tokens, env);
        while (true) {
            if (peekNextToken(tokens, env).type == Token.Type.COMPOSE) {
                final Token op = tokens.remove(0);
                base = new BinaryExpr(base, consumeApplyExpr(tokens, env), op);
            } else {
                return base;
            }
        }
    }

    public static AST consumeApplyExpr(final List<Token> tokens, final Frontend env) {
        // = <val>
        // | <val> LBRACE RBRACE
        // | <val> LBRACE <expr> (COMMA <expr>)* COMMA? RBRACE
        AST base = consumeVal(tokens, env);
        while (peekNextToken(tokens, env).type == Token.Type.LBRACE) {
            tokens.remove(0);
            final List<AST> applicants = new ArrayList<>();
            while (peekNextToken(tokens, env).type != Token.Type.RBRACE) {
                try {
                    applicants.add(consumeExpr(tokens, env));
                    if (peekNextToken(tokens, env).type == Token.Type.COMMA) {
                        tokens.remove(0);
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

    public static AST consumeVal(final List<Token> tokens, final Frontend env) {
        if (tokens.isEmpty()) {
            return null;
        }

        switch (peekNextToken(tokens, env).type) {
        case NUMBER:
            return new NumberVal(tokens.remove(0));
        case ATOM:
            return new AtomVal(tokens.remove(0));
        case IDENT: {
            final Token name = tokens.remove(0);
            // = IDENT YIELD <expr>  (0)
            // | IDENT SET <expr>    (1)
            // | IDENT               (2)
            switch (peekNextToken(tokens, env).type) {
            case YIELD:
                // (0)
                return new AnonFuncVal(name, consumeYield(tokens, env));
            case SET:
                // (1)
                tokens.remove(0);
                return new AssignExpr(name, consumeExpr(tokens, env));
            default:
                // (2)
                return new VariableVal(name);
            }
        }
        case LCURL: {
            // = LCURL <$case> (COMMA <$case>)+ COMMA? RCURL
            tokens.remove(0);
            final List<PiecewiseFuncVal.CaseBlock> cases = new ArrayList<>();
            cons_loop:
            while (peekNextToken(tokens, env).type != Token.Type.RCURL) {
                cases.add(consumeCase(tokens, env));
                switch (peekNextToken(tokens, env).type) {
                case COMMA:
                    tokens.remove(0);
                    break;
                case RCURL:
                    break cons_loop;
                default:
                    throw new RuntimeException("Case statements are split with commas, found " + peekNextToken(tokens, env));
                }
            }
            tokens.remove(0);
            if (cases.isEmpty()) {
                return new NumberVal(new Token(Token.Type.NUMBER, "0"));
            }
            return new PiecewiseFuncVal(cases.toArray(new PiecewiseFuncVal.CaseBlock[cases.size()]));
        }
        case LBLK: {
            // = LBLK RBLK         (0)
            // | LBLK <row>+ RBLK  (1)
            tokens.remove(0);
            final List<MatrixVal.Column> elems = new ArrayList<>();
            while (peekNextToken(tokens, env).type != Token.Type.RBLK) {
                try {
                    elems.add(consumeRow(tokens, env));
                } catch (MatrixRowUnclosedException ex) {
                    if (peekNextToken(tokens, env).type == Token.Type.RBLK) {
                        elems.add(ex.currentColumn);
                        break;
                    }
                    throw new ParserException(ex.getMessage());
                }
            }
            tokens.remove(0);
            return new MatrixVal(elems.toArray(new MatrixVal.Column[elems.size()]));
        }
        case LBRACE: {
            // = LBRACE <expr> RBRACE                                        (0)
            // | LBRACE RBRACE YIELD <expr>                                  (1)
            // | LBRACE <ident> (COMMA <ident>)+ COMMA? RBRACE YIELD <expr>  (2)
            tokens.remove(0);
            switch (tokens.get(0).type) {
            case RBRACE:
                // (1)
                tokens.remove(0);
                return new AnonFuncVal(new Token[0], consumeYield(tokens, env));
            case IDENT: {
                final Token first = tokens.remove(0);
                switch (peekNextToken(tokens, env).type) {
                case RBRACE:
                    // (0)
                    tokens.remove(0);
                    return new VariableVal(first);
                case COMMA: {
                    // (2)
                    final List<Token> inputs = new ArrayList<>();
                    inputs.add(first);
                    tokens.remove(0);
                    while (peekNextToken(tokens, env).type == Token.Type.IDENT) {
                        inputs.add(tokens.remove(0));
                        if (peekNextToken(tokens, env).type == Token.Type.COMMA) {
                            tokens.remove(0);
                        } else {
                            break;
                        }
                    }
                    if (peekNextToken(tokens, env).type == Token.Type.RBRACE) {
                        tokens.remove(0);
                        return new AnonFuncVal(inputs.toArray(new Token[inputs.size()]),
                                               consumeYield(tokens, env));
                    } else {
                        throw new ParserException("Anonymous function parameter list unclosed");
                    }
                }
                default:
                    // (0)
                    tokens.add(0, first);
                    return consumeTrailBrace(tokens, env);
                }
            }
            default:
                return consumeTrailBrace(tokens, env);
            }
        }
        default:
            return null;
        }
    }

    private static AST consumeTrailBrace(final List<Token> tokens, final Frontend env) {
        // try (0)
        final AST expr = consumeExpr(tokens, env);
        if (peekNextToken(tokens, env).type == Token.Type.RBRACE) {
            tokens.remove(0);
            return expr;
        } else {
            throw new ParserException("Missing )");
        }
    }

    private static MatrixVal.Column consumeRow(final List<Token> tokens,
                                               final Frontend env) throws MatrixRowUnclosedException {
        // = <expr> SEMI
        // | <expr> (COMMA <expr>)* COMMA? SEMI
        final List<AST> elems = new ArrayList<>();
        cons_loop:
        while (peekNextToken(tokens, env).type != Token.Type.SEMI) {
            elems.add(consumeExpr(tokens, env));
            switch (peekNextToken(tokens, env).type) {
            case COMMA:
                tokens.remove(0);
                break;
            case SEMI:
                break cons_loop;
            default:
                throw new MatrixRowUnclosedException(new MatrixVal.Column(elems.toArray(new AST[elems.size()])));
            }
        }
        tokens.remove(0);
        return new MatrixVal.Column(elems.toArray(new AST[elems.size()]));
    }

    private static AST consumeYield(final List<Token> tokens, final Frontend env) {
        if (peekNextToken(tokens, env).type == Token.Type.YIELD) {
            tokens.remove(0);
            return consumeExpr(tokens, env);
        } else {
            throw new RuntimeException("Missing -> for input-less function");
        }
    }

    private static PiecewiseFuncVal.CaseBlock consumeCase(final List<Token> tokens, final Frontend env) {
        final AST action = consumeExpr(tokens, env);
        if (peekNextToken(tokens, env).type == Token.Type.K_IF) {
            tokens.remove(0);
            return new PiecewiseFuncVal.CaseBlock(consumePred(tokens, env), action);
        }
        throw new ParserException("Each piecewise case requires a condition");
    }

    private static AST consumePred(final List<Token> tokens, final Frontend env) {
        return consumeOr(tokens, env);
    }

    private static AST consumeOr(final List<Token> tokens, final Frontend env) {
        // = <and> (OR <and>)+
        // | <and>
        AST base = consumeAnd(tokens, env);
        while (true) {
            if (peekNextToken(tokens, env).type == Token.Type.K_OR) {
                final Token op = tokens.remove(0);
                base = new BinaryExpr(base, consumeAnd(tokens, env), op);
            } else {
                return base;
            }
        }
    }

    private static AST consumeAnd(final List<Token> tokens, final Frontend end) {
        // = <rel> (AND <rel>)+
        // | <rel>
        AST base = consumeRel(tokens, end);
        while (true) {
            if (peekNextToken(tokens, end).type == Token.Type.K_AND) {
                final Token op = tokens.remove(0);
                base = new BinaryExpr(base, consumeRel(tokens, end), op);
            } else {
                return base;
            }
        }
    }

    private static AST consumeRel(final List<Token> tokens, final Frontend env) {
        // = <expr> (LT | LE | GE | GT | EQL | NEQ) <expr>
        // | <expr>
        final AST lhs = consumeExpr(tokens, env);
        switch (peekNextToken(tokens, env).type) {
        case LT:
        case LE:
        case GE:
        case GT:
        case EQL:
        case NEQ:
            final Token op = tokens.remove(0);
            return new BinaryExpr(lhs, consumeExpr(tokens, env), op);
        }
        return lhs;
    }

    public static Token peekNextToken(final List<Token> toks, final Frontend env) {
        while (toks.isEmpty()) {
            if (env == null) {
                return Token.getNilToken();
            }
            final String line = env.readLine();
            if (line == null || line.isEmpty()) {
                return Token.getNilToken();
            }
            try {
                toks.addAll(lexString(line, env));
            } catch (LexerException ex) {
                env.errWriteLine(ex.getMessage());
                env.errWriteLine("That line will be ignored!");
            }
        }
        return toks.get(0);
    }

    public static List<Token> lexString(final String str, final Frontend env) throws LexerException {
        if (str == null) {
            throw new IllegalArgumentException("Cannot lex a null string");
        }

        final List<Token> tokens = new ArrayList<>();
        char[] arr = str.toCharArray();
        for (int i = 0; i < arr.length; ++i) {
            try {
                switch (arr[i]) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    break;
                case '#':
                    while (i < arr.length && arr[++i] != '\n') {
                        // skip until end of line or end of input
                    }
                    break;
                case '{':
                    tokens.add(new Token(Token.Type.LCURL, "{"));
                    break;
                case '}':
                    tokens.add(new Token(Token.Type.RCURL, "}"));
                    break;
                case '[':
                    tokens.add(new Token(Token.Type.LBLK, "["));
                    break;
                case ']':
                    tokens.add(new Token(Token.Type.RBLK, "]"));
                    break;
                case '(':
                    tokens.add(new Token(Token.Type.LBRACE, "("));
                    break;
                case ')':
                    tokens.add(new Token(Token.Type.RBRACE, ")"));
                    break;
                case ';':
                    tokens.add(new Token(Token.Type.SEMI, ";"));
                    break;
                case ',':
                    tokens.add(new Token(Token.Type.COMMA, ","));
                    break;
                case '.':
                    tokens.add(new Token(Token.Type.COMPOSE, "."));
                    break;
                case ':':
                    tokens.add(new Token(Token.Type.SCOPE, ":"));
                    break;
                case '+':
                    tokens.add(new Token(Token.Type.ADD, "+"));
                    break;
                case '-':
                    if (++i < arr.length && arr[i] == '>') {
                        tokens.add(new Token(Token.Type.YIELD, "->"));
                    } else {
                        --i;
                        tokens.add(new Token(Token.Type.SUB, "-"));
                    }
                    break;
                case '*':
                    tokens.add(new Token(Token.Type.MUL, "*"));
                    break;
                case '/':
                    if (++i < arr.length && arr[i] == '=') {
                        tokens.add(new Token(Token.Type.NEQ, "/="));
                    } else {
                        --i;
                        tokens.add(new Token(Token.Type.DIV, "/"));
                    }
                    break;
                case '%':
                    tokens.add(new Token(Token.Type.PERCENT, "%"));
                    break;
                case '^':
                    tokens.add(new Token(Token.Type.POW, "^"));
                    break;
                case '=':
                    if (++i < arr.length && arr[i] == '=') {
                        tokens.add(new Token(Token.Type.EQL, "=="));
                    } else {
                        --i;
                        tokens.add(new Token(Token.Type.SET, "="));
                    }
                    break;
                case '<':
                    if (++i < arr.length && arr[i] == '=') {
                        tokens.add(new Token(Token.Type.LE, "<="));
                    } else {
                        --i;
                        tokens.add(new Token(Token.Type.LT, "<"));
                    }
                    break;
                case '>':
                    if (++i < arr.length && arr[i] == '=') {
                        tokens.add(new Token(Token.Type.GE, ">="));
                    } else {
                        --i;
                        tokens.add(new Token(Token.Type.GT, ">"));
                    }
                    break;
                case '@': {
                    final StringBuilder buf = new StringBuilder().append('@');
                    if (isIdent(arr[++i])) {
                        while (i < arr.length && isIdent(arr[i])) {
                            buf.append(arr[i++]);
                        }
                        --i;
                        final String ident = buf.toString();
                        tokens.add(new Token(Token.Type.ATOM, ident));
                        break;
                    }
                    break;
                }
                case '0': {
                    final StringBuilder buf = new StringBuilder();
                    buf.append('0');
                    if (++i < arr.length) {
                        switch (arr[i]) {
                        case 'b': // binary
                            buf.append(arr[i++]);
                            while (i < arr.length && isBinary(arr[i])) {
                                buf.append(arr[i++]);
                            }
                            break;
                        case 'c': // octal
                            buf.append(arr[i++]);
                            while (i < arr.length && isOctal(arr[i])) {
                                buf.append(arr[i++]);
                            }
                            break;
                        case 'd': // decimal
                            buf.append(arr[i++]);
                            while (i < arr.length && isDecimal(arr[i])) {
                                buf.append(arr[i++]);
                            }
                            break;
                        case 'x': // hexadecimal
                            buf.append(arr[i++]);
                            while (i < arr.length && isHexadecimal(arr[i])) {
                                buf.append(arr[i++]);
                            }
                            break;
                        case '.': // float-point
                            buf.append(arr[i++]);
                            while (i < arr.length && isDecimal(arr[i])) {
                                buf.append(arr[i++]);
                            }
                            break;
                        default:
                        }
                    }
                    --i;
                    tokens.add(new Token(Token.Type.NUMBER, buf.toString()));
                    break;
                }
                default: {
                    final StringBuilder buf = new StringBuilder();
                    if (isDecimal(arr[i])) {
                        while (i < arr.length && isDecimal(arr[i])) {
                            buf.append(arr[i++]);
                        }
                        if (i < arr.length && arr[i] == '.') {
                            buf.append(arr[i++]);
                            while (i < arr.length && isDecimal(arr[i])) {
                                buf.append(arr[i++]);
                            }
                        }
                        --i;
                        tokens.add(new Token(Token.Type.NUMBER, buf.toString()));
                        break;
                    }
                    if (isIdent(arr[i])) {
                        while (i < arr.length && isIdent(arr[i])) {
                            buf.append(arr[i++]);
                        }
                        --i;
                        final String ident = buf.toString();
                        tokens.add(new Token(KWORDS.getOrDefault(ident, Token.Type.IDENT), ident));
                        break;
                    }
                    throw new LexerException("Unrecognized character of `" + arr[i] + "'");
                }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                arr = env.readLine().toCharArray();
                i = -1;
            }
        }
        return tokens;
    }

    private static boolean isIdent(final char c) {
        return (c >= '0' && c <= '9')
                || (c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || (c == '_')
                || (c == '$')
                || (c == '\'');
    }

    private static boolean isBinary(final char c) {
        return c == '0' || c == '1';
    }

    private static boolean isOctal(final char c) {
        return c >= '0' && c <= '7';
    }

    private static boolean isDecimal(final char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isHexadecimal(final char c) {
        return (c >= '0' && c <= '9')
                || (c >= 'A' && c <= 'F')
                || (c >= 'a' && c <= 'f');
    }
}

class MatrixRowUnclosedException extends Exception {

    public final MatrixVal.Column currentColumn;

    public MatrixRowUnclosedException(final MatrixVal.Column currentColumn) {
        super("Matrix row unclosed");
        this.currentColumn = currentColumn;
    }
}
