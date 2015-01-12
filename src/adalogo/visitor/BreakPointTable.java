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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Utilities;

import adalogo.gui.editor.LineNumberPanel.BreakPointHandler;

/**
 * handle break points.
 * used by interpreter to determine where interpreter should stop.
 * interface is from line number panel.
 *
 * @see BreakPointTable
 */
public class BreakPointTable implements BreakPointHandler {

    //TODOlater redesign this mess

    Set breakPoints;

    JTextComponent textComponent;

    public BreakPointTable() {
        breakPoints = new HashSet();
    }

    /**
     * wrapper for position which saves calculated line from position.
     * good news: while interpreter is running,
     * no line calculations needs to be done.
     * bad news: while editing,
     * line calculations is done for every keypress.
     */
    private class LinePosition {
        private Position position;
        private int offset = 0;
        private int line = 0;
        public LinePosition(Position position) {
            this.position = position;
            calcLine();
        }
        public void calcLine() {
            Document doc = textComponent.getDocument();
            Element map = doc.getDefaultRootElement();
            offset = position.getOffset();
            line = map.getElementIndex(offset);
        }
        public int getLine() {
            if (offset != position.getOffset()) calcLine();
            return line;
        }
        public String toString() {
            return ""+line;
        }
    }

    //TODOlater redesign
    //dirty hack to calculate line number from position offset.
    public void setTextComponent(JTextComponent textComponent) {
        this.textComponent = textComponent;
    }

    public boolean isBreakPoint(int line) {
        for (Iterator i = breakPoints.iterator(); i.hasNext(); ) {
            LinePosition p = (LinePosition)i.next();
            if (p.getLine() == line - 1) return true;
        }
        return false;
    }

    public void toggleBreakPoint(int offset) {

        Position pos = null;

        try {
            offset = Utilities.getRowStart(textComponent, offset);
            pos = textComponent.getDocument().createPosition(offset);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
            System.out.println("should never happen");
        }

        LinePosition linePos = new LinePosition(pos);

        for (Iterator i = breakPoints.iterator(); i.hasNext(); ) {
            LinePosition p = (LinePosition)i.next();
            if (p.getLine() == linePos.getLine()) {
                i.remove();
                return;
            }
        }

        breakPoints.add(linePos);

    }

    public void clear() {
        breakPoints.clear();
    }

    //DEBUG
    public void dump() {
        for (Iterator i = breakPoints.iterator(); i.hasNext(); ) {
            System.out.print(i.next()+" ");
        }
        System.out.println();
    }

}
