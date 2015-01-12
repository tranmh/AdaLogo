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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

/**
 * this panel will draw line numbers.
 * embedded in a scroll pane as rowheader along a textpane.
 * <br>
 * <br>
 * original code from weebib.
 * break point handler by lesmana.
 * <br>
 * http://forum.java.sun.com/thread.jspa?threadID=589109
 */
public class LineNumberPanel extends JPanel implements DocumentListener, MouseListener {

    //TODOlater line number option

    private final Color DEFAULT_BACKGROUND = new Color(230, 163, 4);
    private final int MARGIN = 5;
    private final int BREAKPOINTMARGIN = 10;

    /**
     * interface for a component which will handle
     * break points for the interpreter.
     */
    public interface BreakPointHandler {
        //this is dirty hack
        //break point handler needs to calculate line number from offset
        //that is only possible with the document from textcomponent
        //TODOlater redesign
        public void setTextComponent(JTextComponent textComponent);
        public boolean isBreakPoint(int line);
        public void toggleBreakPoint(int offset);
    }

    private BreakPointHandler breakPointHandler;

    private JTextComponent theTextComponent;
    private FontMetrics theFontMetrics;
    private int currentRowWidth;

    public LineNumberPanel(JTextComponent aTextComponent, BreakPointHandler breakPointHandler) {
        theTextComponent = aTextComponent;
        theTextComponent.getDocument().addDocumentListener(this);
        setOpaque(true);
        setBackground(DEFAULT_BACKGROUND);
        setFont(theTextComponent.getFont());
        theFontMetrics = getFontMetrics(getFont());
        setForeground(theTextComponent.getForeground());
        currentRowWidth = getDesiredRowWidth();

        this.breakPointHandler = breakPointHandler;
        this.breakPointHandler.setTextComponent(theTextComponent);

        addMouseListener(this);

    }

    //-------------------------------------------------------------------------
    //update line numbers when document changes

    public void insertUpdate(DocumentEvent e) {
        update();
    }

    public void removeUpdate(DocumentEvent e) {
        update();
    }

    public void changedUpdate(DocumentEvent e) {
        update();
    }

    //-------------------------------------------------------------------------
    //toggle break points when clicked

    public void mouseClicked(MouseEvent e) {

        Point click = e.getPoint();
        //click.setLocation(0, click.y);
        int offset = theTextComponent.viewToModel(click);
        breakPointHandler.toggleBreakPoint(offset);

        repaint();

    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }

    //-------------------------------------------------------------------------

    /**
     * update row width if document changes.
     */
    private void update() {
        int desiredRowWidth = getDesiredRowWidth();
        if (desiredRowWidth != currentRowWidth) {
            currentRowWidth = desiredRowWidth;
            revalidate();
        }
        repaint();
    }

    /**
     * row width is width of last line number.
     */
    private int getDesiredRowWidth() {
        Document doc = theTextComponent.getDocument();
        int length = doc.getLength();
        Element map = doc.getDefaultRootElement();
        int nbLines = map.getElementIndex(length) + 1;
        return theFontMetrics.stringWidth(Integer.toString(nbLines));
    }

    /**
     * line numbers get painted here.
     * only visible rows are painted.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Rectangle vis = theTextComponent.getVisibleRect();
        int visStart = theTextComponent.viewToModel(
                new Point(vis.x, vis.y));
        int visEnd = theTextComponent.viewToModel(
                new Point(vis.x + vis.width, vis.y + vis.height));

        Document doc = theTextComponent.getDocument();
        Element map = doc.getDefaultRootElement();

        try {

            int rowStart = Utilities.getRowStart(theTextComponent, visStart);
            int rowEnd = Utilities.getRowEnd(theTextComponent, visEnd);

            int startLine = map.getElementIndex(rowStart);
            int endline = map.getElementIndex(rowEnd);

            for (int line = startLine; line <= endline; line++) {
                int offset = map.getElement(line).getStartOffset();
                Rectangle view = theTextComponent.modelToView(offset);

                //TODOlater fix strange bug
                //every once in a while this gets called with
                //start line = 0 and endline = 0
                //simple workaround
                if (view == null) continue;

                int height = view.y
                        + theFontMetrics.getHeight()
                        - theFontMetrics.getDescent();
                String number = Integer.toString(line + 1);
                int width = theFontMetrics.stringWidth(number);
                g.drawString(number, MARGIN + currentRowWidth - width, height);

                //draw break point if exists
                if (breakPointHandler.isBreakPoint(line+1)) {
                    //TODOlater rewrite
                    g.fillRect(
                            getWidth() - BREAKPOINTMARGIN,
                            view.y + view.height/2 - 2,
                            6, 6);
                }

            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

    }

    public Dimension getPreferredSize() {
        return new Dimension(
                2 * MARGIN + currentRowWidth + BREAKPOINTMARGIN,
                theTextComponent.getHeight());
    }

}
