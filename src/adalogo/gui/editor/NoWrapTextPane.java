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

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JTextPane;
import javax.swing.JViewport;

/**
 * JTextPane which does not wrap line (option).
 * The code is inspired from JTextArea
 * which has an option to wrap lines.
 */
public class NoWrapTextPane extends JTextPane {

    //TODOlater make wrap option
    private boolean wrap = false;

    /**
     * true to set wrap, false to set no wrap.
     * copied and modified from JTextArea.
     * @see javax.swing.JTextArea#setLineWrap(boolean)
     */
    public void setLineWrap(boolean wrap) {
        this.wrap = wrap;
    }

    /**
     * overriden method to control wrapping behaviour.
     * copied and modified from JTextArea.
     * <br>
     * <br>
     * the original code calls super.getScrollableTracksViewportWidth()
     * which would have called the method from JTextComponent.
     * since there is no way to call super.super.
     * the code from JTextComponent was copied.
     *
     * @see javax.swing.JTextArea#getScrollableTracksViewportWidth()
     * @see javax.swing.text.JTextComponent#getScrollableTracksViewportWidth()
     */
    public boolean getScrollableTracksViewportWidth() {
        if (wrap) {
            return true;
        }
        else {
            if (getParent() instanceof JViewport) {
                return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
            }
            return false;
        }

    }

    /**
     * overriden method to control wrapping behaviour.
     * copied and modified from JComponent (not from JTextArea
     * because it messes around with fields like rows and columns).
     * <br>
     * <br>
     * in JTextArea this method will be called from
     * JTextComponent's getScrollableTracksViewportWidth().
     * overriding this here is necessary because JTextPane
     * does not have this method, and the one from JEditorPane
     * will call getScrollableTracksViewportWidth()
     * which will result in an endless loop.
     * <br>
     * <br>
     * the call to super.getPreferredSize() should have gone to
     * java.awt.Container, here it would go to JEditorPane,
     * but it seems that it is never called anyway.
     *
     * @see javax.swing.JEditorPane#getPreferredSize()
     * @see javax.swing.JTextArea#getPreferredSize()
     * @see javax.swing.JComponent#getPreferredSize()
     * @see java.awt.Container#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            System.err.println("should never happen");
        }
        Dimension size = null;
        if (ui != null) {
            size = ui.getPreferredSize(this);
        }
        if (size == null) {
            System.err.println("should never happen");
        }
        return size;
    }

    /**
     * silly hack to get caret in view when extreme rigth.
     */
    public void scrollRectToVisible(Rectangle r) {
        super.scrollRectToVisible(
                new Rectangle(r.x-3, r.y-3, r.width+7, r.height+3));
    }
}
