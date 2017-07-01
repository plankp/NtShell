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
package com.ymcmp.ntshell.rte;

import com.ymcmp.ntshell.NtValue;

/**
 * This class uses exception to rewind the stack to emulate a tail call.
 * https://spf-all.com/journal/entries/483-tail-recursion-with-exceptions.html
 *
 * @author YTENG
 */
public class TailCallTrigger extends RuntimeException {

    public final NtValue applicant;
    public final NtValue[] params;

    public TailCallTrigger(final NtValue applicant, final NtValue... params) {
        this.applicant = applicant;
        this.params = params;
    }

    public static NtValue applyTailCall(final TailCallTrigger trigger) {
        TailCallTrigger handle = trigger;
        while (true) {
            try {
                return handle.applicant.applyCall(handle.params);
            } catch (TailCallTrigger ex) {
                handle = ex;
            }
        }
    }

    public static NtValue call(final NtValue applicant, final NtValue... params) {
        try {
            return applicant.applyCall(params);
        } catch (TailCallTrigger ex) {
            return TailCallTrigger.applyTailCall(ex);
        }
    }
}
