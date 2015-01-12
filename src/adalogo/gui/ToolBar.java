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

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import adalogo.Engine;
import adalogo.Turtle;
import adalogo.Turtle.TurtleEvent;
import adalogo.Turtle.TurtleListener;
import adalogo.gui.editor.Editor;
import adalogo.visitor.VisitorMaster;
import adalogo.visitor.VisitorMaster.VisitorEvent;
import adalogo.visitor.VisitorMaster.VisitorListener;

public class ToolBar extends JToolBar implements TurtleListener, VisitorListener{

    int equalWidth = 0;

    /**
     * dirty hack to force all buttons equal width.
     * width is set to width of longest button.
     */
    private class EqualWidthButton extends JButton {
        public Dimension getMinimumSize() {
            Dimension pain = super.getMinimumSize();
            equalWidth = Math.max(equalWidth, pain.width);
            setMinimumSize(new Dimension(equalWidth, pain.height));
            return super.getMinimumSize();
        }
        public Dimension getPreferredSize() {
            Dimension pain = super.getPreferredSize();
            equalWidth = Math.max(equalWidth, pain.width);
            setPreferredSize(new Dimension(equalWidth, pain.height));
            return super.getPreferredSize();
        }
        public Dimension getMaximumSize() {
            Dimension pain = super.getMaximumSize();
            equalWidth = Math.max(equalWidth, pain.width);
            setMaximumSize(new Dimension(equalWidth, pain.height));
            return super.getMaximumSize();
        }
    }

    private Engine engine;
    private Turtle turtle;
    private Editor editor;
    private TurtleCanvas canvas;
    private StatusBar statusBar;
    private WindowFrame window;
    private VisitorMaster visitor;

    //visitor buttons
    private JButton startButton = new EqualWidthButton();
    private JButton debugButton = new EqualWidthButton();
    private JButton stopButton = new EqualWidthButton();

    //turtle buttons
    private JButton forwardButton = new EqualWidthButton();
    private JButton turnLeftButton = new EqualWidthButton();
    private JButton turnRightButton = new EqualWidthButton();
    private JButton penButton = new EqualWidthButton();
    private JButton resetTurtleButton = new EqualWidthButton();

    //canvas buttons
    private JButton resetCanvasButton = new EqualWidthButton();

    public ToolBar(Engine en) {
        this.engine = en;

        add(startButton);
        add(debugButton);
        add(stopButton);
        addSeparator();
        add(forwardButton);
        add(turnLeftButton);
        add(turnRightButton);
        add(penButton);
        add(resetTurtleButton);
        addSeparator();
        add(resetCanvasButton);

    }

    /**
     * init, extended constructor.
     * called by engine.
     */
    public void init() {
        this.turtle = engine.getTurtle();
        this.editor = engine.getEditor();
        this.canvas = engine.getCanvas();
        this.statusBar = engine.getStatusBar();
        this.window = engine.getWindow();
        this.visitor = engine.getVisitor();

        turtle.addTurtleListener(this);
        visitor.addVisitorListener(this);

        startButton.setAction(visitor.getStartAction());
        debugButton.setAction(visitor.getStartDebugAction());
        stopButton.setAction(visitor.getStopAction());

        forwardButton.setAction(turtle.getForwardAction());
        turnLeftButton.setAction(turtle.getTurnLeftAction());
        turnRightButton.setAction(turtle.getTurnRightAction());
        penButton.setAction(turtle.getPenUpAction());
        resetTurtleButton.setAction(turtle.getResetAction());

        resetCanvasButton.setAction(canvas.getResetAction());

        //init with default values
        penButtonAction = turtle.getPenUpAction();
        penButtonUpdatePending = false;

    }

    /**
     * this to save pen button action while visitor is running.
     * after visitor stopped the pen button action will be updated.
     */
    private Action penButtonAction;

    private boolean penButtonUpdatePending;

    //-------------------------------------------------------------------------
    //turtle listener

    public void turtleStateChanged(TurtleEvent e) {
        boolean penDown = ((Turtle)e.getSource()).isPenDown();

        if (penDown) {
            penButtonAction = turtle.getPenUpAction();
        } else {
            penButtonAction = turtle.getPenDownAction();
        }

        if (visitorRunning) {
            //dont update when visitor is running
            penButtonUpdatePending = true;
            return;
        } else {
            penButton.setAction(penButtonAction);
        }

    }

    public void turtleReset(TurtleEvent e) {
        //delegate
        turtleStateChanged(e);
    }

    //-------------------------------------------------------------------------
    //visitor listener

    /**
     * this will be true when a visitor is running.
     */
    private boolean visitorRunning;

    public void visitorStarted(VisitorEvent e) {
        visitorRunning = true;

        //update toolbar buttons
        startButton.setAction(visitor.getMultiStepAction());
        debugButton.setAction(visitor.getSingleStepAction());
    }

    public void visitorWaiting(VisitorEvent e) {
        visitorRunning = false;
    }

    public void visitorRunning(VisitorEvent e) {
        visitorRunning = true;
    }

    public void visitorStopped(VisitorEvent e) {
        visitorRunning = false;

        //update toolbar buttons
        startButton.setAction(visitor.getStartAction());
        debugButton.setAction(visitor.getStartDebugAction());

        if (penButtonUpdatePending) {
            penButton.setAction(penButtonAction);
            penButtonUpdatePending = false;
        }

    }

}
