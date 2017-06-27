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

import com.ymcmp.ntshell.rte.DispatchException;
import com.ymcmp.ntshell.func.Plot2d;
import com.ymcmp.ntshell.func.Plot3d;
import com.ymcmp.ntshell.value.*;

import net.ericaro.surfaceplotter.Mapper;
import net.ericaro.surfaceplotter.JSurfacePanel;
import net.ericaro.surfaceplotter.ProgressiveSurfaceModel;

import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author YTENG
 */
public class SwingMode implements Frontend {

    private static final SimpleAttributeSet STYLE_ERR = new SimpleAttributeSet();
    private static final SimpleAttributeSet STYLE_OUT = new SimpleAttributeSet();

    static {
        StyleConstants.setForeground(STYLE_ERR, Color.red);
        StyleConstants.setBold(STYLE_ERR, false);

        StyleConstants.setForeground(STYLE_OUT, Color.black);
        StyleConstants.setBold(STYLE_OUT, true);
    }

    private JFrame frame = new JFrame("NtShell");
    private DefaultStyledDocument document = new DefaultStyledDocument();
    private JTextPane area = new JTextPane(document);
    private JScrollPane scroller = new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private StringBuffer input = new StringBuffer();
    private boolean inputOn = false;

    public SwingMode() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        area.setEditable(false);
        area.setFont(Font.decode(Font.MONOSPACED));

        frame.add(scroller, BorderLayout.CENTER);

        KeyEventDispatcher ked = e -> {
            if (inputOn && e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (input.length() > 0) {
                        input.deleteCharAt(input.length() - 1);
                        final int len = document.getLength();
                        try {
                            document.replace(len - 1, 1, "", null);
                        } catch (BadLocationException ex) {
                        }
                    }
                    return true;
                }

                final char c = e.getKeyChar();
                if (c != KeyEvent.CHAR_UNDEFINED) {
                    try {
                        document.insertString(document.getLength(), Character.toString(c), null);
                        final JScrollBar bar = scroller.getVerticalScrollBar();
                        bar.setValue(bar.getMaximum());
                    } catch (BadLocationException ex) {
                    }
                    input.append(c);
                }
            }
            return true;
        };
        KeyboardFocusManager
                .getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(ked);

        frame.setVisible(true);
    }

    @Override
    public String readLine() {
        final StringBuilder sb = new StringBuilder();
        while (true) {
            write("> ");
            final String s;
            if ((s = rawReadLine()) != null) {
                if (s.isEmpty()) {
                    break;
                }

                sb.append(s);
                if (sb.charAt(sb.length() - 1) == '\\') {
                    sb.setCharAt(sb.length() - 1, '\n');
                } else {
                    break;
                }
            }
        }
        return sb.toString();
    }

    private String rawReadLine() {
        inputOn = true;
        while (input.indexOf("\n") == -1) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException ex) {
            }
        }

        inputOn = false;
        final int index = input.indexOf("\n");
        if (index == 0) {
            input.deleteCharAt(0);
            return "";
        }

        final String substr = input.substring(0, index);
        input.delete(0, index + 1); // consume the \n itself also
        return substr; // note: substr does *not* contain the \n
    }

    @Override
    public void write(char c) {
        try {
            document.insertString(document.getLength(),
                                  Character.toString(c), STYLE_OUT);
        } catch (BadLocationException ex) {
        }
    }

    @Override
    public void write(Object o) {
        if (o instanceof CoreLambda) {
            final CoreLambda.Info fInfo = ((CoreLambda) o).info;
            if (fInfo != null) {
                final JLabel label = new JLabel(String.format("<%s>", fInfo.name));
                label.setToolTipText(toToolTipText(fInfo.toString()));
                appendComponent(label);
                return;
            }
        }
        Frontend.super.write(o);
    }

    private String toToolTipText(final String str) {
        return new StringBuilder()
                .append("<html><p width=\"250\">")
                .append(str.replace(" ", "&nbsp;").replace("\n", "<br>"))
                .append("</p></html>")
                .toString();
    }

    @Override
    public void errWrite(char c) {
        try {
            document.insertString(document.getLength(),
                                  Character.toString(c), STYLE_ERR);
        } catch (BadLocationException ex) {
        }
    }

    @Override
    public void close() {
        frame.dispose();
    }

    public void appendComponent(final Component comp) {
        area.setSelectionStart(document.getLength());
        area.setSelectionEnd(document.getLength());
        area.insertComponent(comp);
        final JScrollBar bar = scroller.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
    }

    @Override
    public NtValue findDefinition(final String name) {
        switch (name) {
        case "illuminati":
            return new CoreLambda(new CoreLambda.Info("Illuminati", "... -> number", "Illuminati confirmed!")) {
                @Override
                public NtValue applyCall(NtValue... params) {
                    if (params.length == 3
                            && params[0].equals(CoreNumber.from(3))
                            && params[1].equals(CoreNumber.from(3))
                            && params[2].equals(CoreNumber.from(3))) {
                        appendComponent(new JLabel(" ILLUMINATI CONFIRMED "));
                        writeLine();
                        return CoreNumber.from(3);
                    }
                    return CoreNumber.from(0);
                }
            };
        case "plot":
        case "plot2d":
            return new CoreLambda(new CoreLambda.Info("plot2d", "(func(number) -> number) -> func", "Plots the by feeding only the x values")) {
                @Override
                public NtValue applyCall(final NtValue[] f) {
                    if (f.length != 1) {
                        throw new DispatchException("plot2d", "Expected one parameter, got " + f.length + " instead");
                    }
                    return new CoreLambda(new CoreLambda.Info("$$plot2d", "(xrange?:mat, yrange?:mat) -> unit", "Specify the x and y ranges (both optional)")) {
                        @Override
                        public NtValue applyCall(final NtValue[] params) {
                            appendComponent(new Plot2d(f[0]).draw(params));
                            return CoreUnit.getInstance();
                        }
                    };
                }
            };
        case "plot3d":
            return new CoreLambda(new CoreLambda.Info("plot3d", "(func(number, number) -> number) -> func", "Plots the by feeding the x and y values")) {
                @Override
                public NtValue applyCall(NtValue... f) {
                    if (f.length != 1) {
                        throw new DispatchException("plot3d", "Expected one parameter, got " + f.length + " instead");
                    }
                    return new CoreLambda(new CoreLambda.Info("$$plot3d", "(xrange?:mat, yrange?:mat, zrange?:mat) -> unit", "Specify the x, y, and z ranges (all optional)")) {
                        @Override
                        public NtValue applyCall(final NtValue[] params) {
                            appendComponent(new Plot3d(f[0]).draw(params));
                            return CoreUnit.getInstance();
                        }
                    };
                }
            };
        default:
        }
        return null;
    }
}
