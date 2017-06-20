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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;

import java.awt.event.KeyEvent;
import java.util.function.Function;

import javax.swing.JFrame;
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

    private JFrame frame = new JFrame();
    private DefaultStyledDocument document = new DefaultStyledDocument();
    private JTextPane area = new JTextPane(document);
    private StringBuffer input = new StringBuffer();
    private boolean inputOn = false;

    public SwingMode() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        area.setEditable(false);
        area.setFont(Font.decode(Font.MONOSPACED));

        frame.add(new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

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
            document.insertString(document.getLength(), Character.toString(c), STYLE_OUT);
        } catch (BadLocationException ex) {
        }
    }

    @Override
    public void errWrite(char c) {
        try {
            document.insertString(document.getLength(), Character.toString(c), STYLE_ERR);
        } catch (BadLocationException ex) {
        }
    }

    @Override
    public void close() {
        frame.dispose();
    }

    @Override
    public Object findDefinition(final String name) {
        // Add interfaces to jzy3d
        if (name.equals("illuminati")) {
            return (Function<Object[], ?>) x -> {
                if (x.length == 3 && x[0].equals(3.0) && x[1].equals(3.0) && x[2].equals(3.0)) {
                    area.setSelectionStart(document.getLength());
                    area.setSelectionEnd(document.getLength());
                    area.insertComponent(new javax.swing.JLabel(" ILLUMINATI CONFIRMED "));
                    return 3.0;
                }
                return "";
            };
        }
        return null;
    }
}
