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

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.impl.Arguments;

import com.ymcmp.ntshell.ast.*;

import java.awt.HeadlessException;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Subparsers;

import ntshell.rt.lib.Core;

/**
 *
 * @author YTENG
 */
public class App {

    private final Parser parser = new Parser();

    private boolean showAST = false;
    private boolean transNeg = true;
    private boolean levelOp = true;
    private boolean simplifyRat = true;
    private boolean unfoldConst = true;

    private InteractiveModeVisitor session = null;
    private Frontend environment = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("ntshell")
                .description("Not quite your conventional shell");
        parser.addArgument("-m", "--mode")
                .setDefault(true)
                .type(Arguments.booleanType("gui", "term"))
                .help("specifies the mode to launch in (defaults to gui with terminal as fallback)");
        parser.addArgument("-f", "--file")
                .nargs("?")
                .type(FileReader.class)
                .setDefault((Object) null)
                .help("executes the specified file");
        final Namespace res = parser.parseArgsOrFail(args);

        final App app = new App();
        final FileReader reader = res.get("file");
        if (res.getBoolean("mode")) {
            try (final Frontend inst = new SwingMode()) {
                app.executeFrontend(inst, reader);
                return;
            } catch (HeadlessException ex) {
                System.err.println("Fallback to terminal!");
            }
        }

        try (final Frontend inst = new ConsoleMode()) {
            app.executeFrontend(inst, reader);
        }
    }

    public void executeFrontend(final Frontend inst, final FileReader reader) {
        switchFrontend(inst);
        initSession();
        if (reader != null) {
            loadStartupFile(reader, session);
            // release reader
            try {
                reader.close();
            } catch (IOException ex) {
                // do nothing
            }
        }
        interactiveMode();
    }

    public void switchFrontend(final Frontend env) {
        parser.switchFrontend(this.environment = env);
    }

    public void initSession() {
        environment.linkLibrary(Core.getInstance());
        environment.writeLine("NtShell (interactive mode)\nType `~help` for help\n");
        session = new InteractiveModeVisitor(environment);
    }

    public static void loadStartupFile(final FileReader reader, final Visitor<?> session) {
        try {
            final List<Token> toks = Lexer.lexFromReader(reader);
            while (!toks.isEmpty()) {
                final AST tree = new Parser().consumeExpr(toks);
                session.visit(tree);

                while (!toks.isEmpty() && toks.get(0).type == Token.Type.SEMI) {
                    toks.remove(0);
                }
            }
        } catch (LexerException ex) {
        }
    }

    public void interactiveMode() {
        boolean evaluate = true;

        while (true) {
            final String input = environment.readLine();
            switch (input) {
            case "~exit":
                return;
            case "~help":
                environment.writeLine("Enter the expression you want to test\nEnd the line with `\\` to wrap on the next line\nWhen the expression is done, punch in a `;`\n\nCommands:\n  ~help ~exit ~restart ~showast ~hideast\n  ~transneg ~no-transneg ~levelop ~no-levelop\n  ~simprat ~no-simprat ~unfoldc ~no-unfoldc\n  ~eval ~no-eval");
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
                session.reset();
                continue;
            case "":
                continue;
            default:
                if (input.charAt(0) == '~') {
                    environment.errWriteLine("Unrecognized command " + input);
                    environment.errWriteLine("Type `~help` for help");
                    continue;
                }
            }

            try {
                final List<Token> toks = Lexer.lexFromString(input);

                while (!toks.isEmpty()) {
                    AST ast = parser.consumeExpr(toks);
                    if (showAST) {
                        environment.writeLine("showast:  " + ast);
                    }

                    ast = procRuleRewrite(ast.unfoldConstant());

                    if (evaluate) {
                        environment.writeLine(session.visit(ast));
                    }
                    while (!toks.isEmpty() && toks.get(0).type == Token.Type.SEMI) {
                        toks.remove(0);
                    }
                }
            } catch (IllegalArgumentException ex) {
                environment.errWriteLine(ex);
            } catch (NullPointerException ex) {
                environment.errWriteLine("Syntax error?");
            } catch (LexerException | RuntimeException ex) {
                environment.errWriteLine(ex);
            }
        }
    }

    private AST procRuleRewrite(final AST tree) {
        AST ast = tree;
        if (transNeg) {
            ast = ast.transformNegatives();
            if (showAST) {
                environment.writeLine("transneg: " + ast);
            }
        }
        if (levelOp) {
            ast = ast.levelOperators();
            if (showAST) {
                environment.writeLine("levelop:  " + ast);
            }
        }
        if (simplifyRat) {
            ast = ast.simplifyRationals();
            if (showAST) {
                environment.writeLine("simprat:  " + ast);
            }
        }
        if (unfoldConst) {
            ast = ast.unfoldConstant();
            if (showAST) {
                environment.writeLine("unfoldc:  " + ast);
            }
        }
        return ast;
    }
}

class MatrixRowUnclosedException extends Exception {

    public final MatrixVal.Column currentColumn;

    public MatrixRowUnclosedException(final MatrixVal.Column currentColumn) {
        super("Matrix row unclosed");
        this.currentColumn = currentColumn;
    }
}
