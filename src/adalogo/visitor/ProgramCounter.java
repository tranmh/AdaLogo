/*
 * Copyright 2005 Hailang Thai, Minh Cuong Tran, Lesmana Zimmer
 *
 * This file is part of AdaLogo.
 *
 * AdaLogo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * AdaLogo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AdaLogo; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package adalogo.visitor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * programm counter.
 * used mainly for varMonitor to keep track of changed variables.
 * started and counted by interpreter.
 */
public class ProgramCounter {

    private int pc;

    public ProgramCounter() {
        pc = 0;
    }

    public void increment() {
        pc = pc + 1;
        fireCounterIncremented();
    }

    public int getValue() {
        return pc;
    }

    public void reset() {
        pc = 0;
        fireCounterReset();
    }

    //-------------------------------------------------------------------------
    //event listener architecture

    private Set listeners = new HashSet();

    /**
     * interface for pc listener listeners
     * mainly implemented by varmonitor varnodes
     */
    public interface ProgramCounterListener {
        void counterReset();
        void counterIncremented();
    }

    public void addProgramCounterListener(ProgramCounterListener listener) {
        listeners.add(listener);
    }

    public void removeProgramCounterListener(ProgramCounterListener listener) {
        listeners.remove(listener);
    }

    private void fireCounterIncremented() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            ProgramCounterListener l = (ProgramCounterListener) i.next();
            l.counterIncremented();
        }
    }

    private void fireCounterReset() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            ProgramCounterListener l = (ProgramCounterListener) i.next();
            l.counterReset();
        }
    }

}
