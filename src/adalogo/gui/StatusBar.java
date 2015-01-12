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

package adalogo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import adalogo.Engine;
import adalogo.Turtle;
import adalogo.Turtle.TurtleEvent;
import adalogo.Turtle.TurtleListener;
import adalogo.visitor.VisitorMaster.VisitorEvent;
import adalogo.visitor.VisitorMaster.VisitorListener;

public class StatusBar extends JPanel implements CaretListener, TurtleListener, VisitorListener {

    /**
     * hack to prevent labels from resizing when text changes.
     * this label will adapt to the size of its parent.
     * that means if it is put in a panel it will get as big as the panel.
     * the final size, however, will be set by the layout manager.
     */
    private class SubmissiveLabel extends JLabel {
        public SubmissiveLabel() {
            super();
        }
        public Dimension getMaximumSize() {
            return getParent().getSize();
        }
        public Dimension getMinimumSize() {
            return getParent().getSize();
        }
        public Dimension getPreferredSize() {
            return getParent().getSize();
        }
    }

    /**
     * hack to prevent labels from resizing when text changes.
     * the height of this panel is set to the height of the font,
     * width is set to zero.
     * the rest will be taken care of a suitable layout manager.
     */
    private class DominaPanel extends JPanel {
        public DominaPanel(JLabel label) {
            super();
            int width = getMinimumSize().width;
            int height = getFontMetrics(getFont()).getHeight();
            Dimension hack = new Dimension(width, height);
            setMaximumSize(hack);
            setMinimumSize(hack);
            setPreferredSize(hack);
            setLayout(new BorderLayout());
            add(label, BorderLayout.CENTER);
            setBorder(new CompoundBorder(
                    new MatteBorder(0, 1, 0, 0, Color.BLACK),
                    new EmptyBorder(0, 5, 0, 0)));
        }
    }

    Engine engine;

    Timer clearTimer;

    JLabel common;
    JLabel lineNumber;
    JLabel turtleCoord;
    JLabel turtleDir;
    JLabel turtlePen;
    JLabel visitorState;

    public StatusBar(Engine engine) {
        this.engine = engine;

        common = new SubmissiveLabel();
        lineNumber = new SubmissiveLabel();
        turtleCoord = new SubmissiveLabel();
        turtleDir = new SubmissiveLabel();
        turtlePen = new SubmissiveLabel();
        visitorState = new SubmissiveLabel();

        setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        setLayout(gbl);

        gbc.anchor = GridBagConstraints.LINE_START; //left alligned
        gbc.fill = GridBagConstraints.BOTH;

        gbc.weightx = 0.33; //x space distribution
        add(new DominaPanel(common), gbc);

        gbc.weightx = 0.16;
        add(new DominaPanel(lineNumber), gbc);

        gbc.weightx = 0.16;
        add(new DominaPanel(turtleCoord), gbc);
        gbc.weightx = 0.07;
        add(new DominaPanel(turtleDir), gbc);
        gbc.weightx = 0.08;
        add(new DominaPanel(turtlePen), gbc);

        gbc.weightx = 0.20;
        add(new DominaPanel(visitorState), gbc);

        //timer to empty statusbar
        clearTimer = new Timer(5000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearText();
            }
        });
        clearTimer.setRepeats(false);

    }

    public void init() {

        //dummy init values
        common.setText("main screen turn on");
        lineNumber.setText("line:1 col:1");
        turtleCoord.setText("turtle: (0,0)");
        turtleDir.setText("dir: 0");
        turtlePen.setText("pen: true");
        visitorState.setText("interpreter: none");

        engine.getEditor().addCaretListener(this);
        engine.getTurtle().addTurtleListener(this);
        engine.getVisitor().addVisitorListener(this);

    }

    /**
     * set text in status bar.
     * this will only set the text of the first label in the status bar.
     * the remaining labels are reserved for their specific purposes.
     */
    public synchronized void setText(String text) {
        clearTimer.restart();
        common.setText(text);
    }

    public synchronized void clearText() {
        common.setText("");
    }

    //-------------------------------------------------------------------------
    //caret listener

    public void caretUpdate(CaretEvent e) {
        int caret = e.getDot();
        JTextComponent textComponent = (JTextComponent)e.getSource();
        Document doc = textComponent.getDocument();
        Element root = doc.getDefaultRootElement();
        int line = root.getElementIndex(caret);
        Element paragraph = root.getElement(line);
        int column = caret - paragraph.getStartOffset();

        //document starts count at 0, we want start at 1
        lineNumber.setText("line:"+(line+1)+" col:"+(column+1));
    }

    //-------------------------------------------------------------------------
    //turtle listener

    public void turtleStateChanged(TurtleEvent e) {
        Turtle t = (Turtle) e.getSource();
        Point2D position = t.getPosition();
        int posX = (int)position.getX();
        int posY = (int)position.getY();
        int direction = (int)t.getDirection();
        boolean penDown = t.isPenDown();

        turtleCoord.setText("turtle: ("+posX+","+posY+")");
        turtleDir.setText("dir: "+direction);
        turtlePen.setText("pen: "+penDown);

    }

    public void turtleReset(TurtleEvent e) {
        //delegate
        turtleStateChanged(e);
    }

    //-------------------------------------------------------------------------
    //visitor listener

    public void visitorStarted(VisitorEvent e) {
        visitorState.setText("interpeter: running");
    }

    public void visitorWaiting(VisitorEvent e) {
        visitorState.setText("interpeter: waiting ("+e.getLine()+")");
    }

    public void visitorRunning(VisitorEvent e) {
        visitorState.setText("interpeter: running");
    }

    public void visitorStopped(VisitorEvent e) {
        visitorState.setText("interpeter: stopped");
    }

}
