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

package adalogo.gui.editor;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

/**
 * simple line highlighter.
 * <br>
 * <br>
 * original code idea from weebib.
 * but there is not much left of it.
 * <br>
 * http://forum.java.sun.com/thread.jspa?threadID=589109
 */
public class LineHighlightHandler {

    private JTextComponent textComponent;
    private DefaultHighlighter hiliter;
    private MyHighlightPainter myhiliter;
    private Object myhilites;

    private static final Color HIGHLIGHT_COLOR = new Color(228, 228, 241);

    public LineHighlightHandler(JTextComponent textComponent) {
        this.textComponent = textComponent;

        myhiliter = new MyHighlightPainter(HIGHLIGHT_COLOR);

        hiliter = (DefaultHighlighter)textComponent.getHighlighter();
        hiliter.setDrawsLayeredHighlights(true);

    }

    /**
     * highlight the line line.
     */
    public void addHighlight(int line) {

        Element root = textComponent.getDocument().getDefaultRootElement();
        Element lineElement = root.getElement(line);

        int start = lineElement.getStartOffset();
        int end = lineElement.getEndOffset();

        try {
            myhilites = hiliter.addHighlight(start, end, myhiliter);
            Rectangle view = textComponent.modelToView(start);
            textComponent.scrollRectToVisible(view);
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }

    }

    /**
     * remove the last highlight.
     */
    public void removeHighlight() {

        if (myhilites == null) return;

        hiliter.removeHighlight(myhilites);

    }

    /**
     * primitive highlight painter.
     * only color is given. rest is default.
     */
    class MyHighlightPainter extends DefaultHighlightPainter {
        public MyHighlightPainter(Color color) {
            super(color);
        }
    }

}
