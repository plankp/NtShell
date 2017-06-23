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

import com.ymcmp.ntshell.value.*;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

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
        // Add interfaces to jzy3d
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
            return new CoreLambda(new CoreLambda.Info("plot2d", "(func(number) -> number) -> func", "Wraps a function in another function")) {
                @Override
                public NtValue applyCall(NtValue... f) {
                    if (f.length != 1) {
                        throw new DispatchException("plot2d", "Expected one parameter, got " + f.length + " instead");
                    }

                    return new CoreLambda(new CoreLambda.Info("$$plot2d", "func(start:number, end:number, incr:number) -> number", "Draws the graph from start to end increased by incr. The return value is meaningless.")) {
                        @Override
                        public NtValue applyCall(NtValue... r) {
                            if (r.length == 3
                                    && r[0] instanceof CoreNumber
                                    && r[1] instanceof CoreNumber
                                    && r[2] instanceof CoreNumber) {

                                double x = ((CoreNumber) r[0]).toDouble();

                                final double end = ((CoreNumber) r[1]).toDouble();
                                final double delta = ((CoreNumber) r[2]).toDouble();
                                if (delta == 0) {
                                    throw new DispatchException("$$plot2d", "delta cannot be zero: " + delta);
                                }

                                final DataTable data = new DataTable(Double.class, Double.class);
                                for (; x <= end; x += delta) {
                                    final NtValue ret = f[0].applyCall(CoreNumber.from(x));
                                    if (ret instanceof CoreNumber) {
                                        data.add(x, ((CoreNumber) ret).toDouble());
                                    } else {
                                        data.add(x, Double.NaN);
                                    }
                                }

                                final XYPlot plot = new XYPlot(data);
                                final InteractivePanel panel = new InteractivePanel(plot);
                                final LineRenderer lines = new BrokenLineRenderer();
                                plot.setLineRenderers(data, lines);

                                final Color color = new Color(0.0f, 0.3f, 1.0f);
                                plot.getPointRenderers(data).get(0).setColor(color);
                                plot.getLineRenderers(data).get(0).setColor(color);

                                final JFrame gframe = new JFrame();
                                gframe.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                                gframe.setSize(400, 300);
                                gframe.getContentPane().add(panel);
                                gframe.setVisible(true);
                            } else {
                                throw new DispatchException("$$plot2d", "Expected three numbers, found " + r.length + " instead");
                            }
                            return CoreNumber.from(0);
                        }
                    };
                }
            };
        default:
        }
        return null;
    }
}