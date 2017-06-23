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
package com.ymcmp.ntshell.value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author YTENG
 */
public class LogicalLine implements Serializable {

    private static final long serialVersionUID = 209187243189L;

    private static final Pattern SEPARATOR_PAD = Pattern.compile("(?:^ *-+ *$)");

    public final String[] lines;

    public LogicalLine() {
        lines = new String[0];
    }

    public LogicalLine(String line) {
        lines = line.split("\n");
    }

    private LogicalLine(final String[] lines) {
        this.lines = lines;
    }

    public LogicalLine appendLine(final LogicalLine other) {
        /*
        1 2 append 3 4 yields  1 2
                              -----
                               3 4
         */
        final List<String> ret = new ArrayList<>(lines.length + other.lines.length + 1);
        final int sepLength = Stream.concat(Arrays.stream(lines), Arrays.stream(other.lines))
                .mapToInt(String::length)
                .max()
                .orElse(0);
        for (final String s : lines) {
            ret.add(String.format(" %-" + sepLength + "s ", s));
        }
        {
            final char[] rsep = new char[sepLength + 2];
            Arrays.fill(rsep, '-');
            ret.add(String.valueOf(rsep));
        }
        for (final String s : other.lines) {
            ret.add(String.format(" %-" + sepLength + "s ", s));
        }
        return new LogicalLine(ret.toArray(new String[ret.size()]));
    }

    public LogicalLine mergeWith(final LogicalLine other) {
        /*
        (0):
        1 2 merge 3 4 yields 1 2 | 3 4
        
        (1):
        1 2 merge 3   yields 1 2 | 3
                  4              | 4
        
        (2):
        1 merge 3 4   yields 1 | 3 4
        2                    2 |
         */
        if (lines.length == other.lines.length) {
            // (0) No padding in between strings, easiest case
            final String[] ret = new String[lines.length];
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = String.format("%s | %s", lines[i], other.lines[i]);
            }
            return new LogicalLine(ret);
        }
        if (lines.length < other.lines.length) {
            // (1) Padding on left side
            final String[] ret = new String[other.lines.length];
            final int padLength = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = String.format("%-" + padLength + "s | %s", i < lines.length ? lines[i] : "", other.lines[i]);
            }
            return new LogicalLine(ret);
        }
        // (2) Padding on right side
        final String[] ret = new String[lines.length];
        final int padLength = Arrays.stream(other.lines).mapToInt(String::length).max().orElse(0);
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = String.format("%s | %-" + padLength + "s", lines[i], i < other.lines.length ? other.lines[i] : "");
        }
        return new LogicalLine(ret);
    }

    @Override
    public String toString() {
        return Arrays.stream(lines).collect(Collectors.joining("\n"));
    }

    public LogicalLine wrapInBox() {
        switch (this.lines.length) {
        case 0:
            return new LogicalLine("[]");
        case 1:
            return new LogicalLine("[" + this.lines[0] + "]");
        default:
            final String[] arr = new String[this.lines.length + 2];
            System.arraycopy(lines, 0, arr, 1, lines.length);
            if (arr[1].length() == 0) {
                // => []
                return new LogicalLine("[]");
            } else {
                final char[] box = new char[arr[1].length() + 2];
                Arrays.fill(box, '-');
                box[box.length - 1] = box[0] = '+';
                arr[arr.length - 1] = arr[0] = String.valueOf(box);
                for (int i = 1; i < arr.length - 1; ++i) {
                    final Matcher match = SEPARATOR_PAD.matcher(arr[i]);
                    if (match.matches()) {
                        arr[i] = "|" + match.group().replace(' ', '-') + "|";
                    } else {
                        arr[i] = "|" + arr[i] + "|";
                    }
                }
                return new LogicalLine(arr);
            }
        }
    }
}
