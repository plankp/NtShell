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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author YTENG
 */
public class ConsoleMode implements Frontend {

    private BufferedReader br;

    public ConsoleMode() {
        br = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public String readLine() {
        try {
            final StringBuilder sb = new StringBuilder();
            while (true) {
                write("> ");
                final String s;
                if ((s = br.readLine()) != null) {
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
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public void write(final char c) {
        System.out.print(c);
    }

    @Override
    public void errWrite(final char c) {
        System.err.print(c);
    }

    @Override
    public void close() {
        try {
            br.close();
        } catch (IOException ex) {
        }
    }
}
