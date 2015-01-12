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

package adalogo;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * this is turtle.
 */
public class Turtle {

    private Engine engine;

    private double direction;
    private Point2D position;
    private boolean penDown;

    public Turtle (Engine engine) {
        this.engine = engine;
        position = new Point2D.Double();
    }

    public void init() {
        resetTurtle();
    }

    /**
     * this will be called to remove all drawn lines
     * and reset turtle to default direction and position.
     * no drawing will occur.
     */
    public void resetTurtle() {
        direction = 0;
        position.setLocation(0, 0);
        penDown = true;
        fireTurtleReset();
    }

    /**
     * Turtle turns by degree relativ to current direction.
     * if degree positive turn clockwise,
     * otherwise turn counterclockwise
     * (or the other way around, i don't know).
     */
    public void turn(double degree) {
        direction = (direction + degree) % 360;
        fireTurtleStateChanged();
    }

    /**
     * Tutle turns to given direction.
     */
    public void turnTo(double degree) {
        direction = degree;
        fireTurtleStateChanged();
    }

    /**
     * Turtle will draw line when moving.
     */
    public void penDown() {
        penDown = true;
        fireTurtleStateChanged();
    }

    /**
     * Turtle will not draw line when moving.
     */
    public void penUp() {
        penDown = false;
        fireTurtleStateChanged();
    }

    /**
     * Turtle jumps to position, will draw line if penDown,
     * keeps direction.
     */
    public void moveTo(double x, double y) {
        position.setLocation(x, y);
        fireTurtleStateChanged();
    }

    /**
     * Turtle moves forward, will draw line if penDown.
     */
    public void forward (double step) {
        position.setLocation(
                position.getX()+(Math.cos(Math.toRadians(direction))*step),
                position.getY()+(Math.sin(Math.toRadians(direction))*step));
        fireTurtleStateChanged();
    }

    //-------------------------------------------------------------------------

    public Point2D getPosition() {
        return position;
    }

    public double getDirection() {
        return direction;
    }

    public boolean isPenDown() {
        return penDown;
    }

    //-------------------------------------------------------------------------
    //event listener architecture

    private Set listeners = new HashSet();

    public interface TurtleListener extends EventListener {
        public void turtleStateChanged(TurtleEvent e);
        public void turtleReset(TurtleEvent e);
    }

    public class TurtleEvent extends EventObject {
        //TODOlater make reasonable getter methods
        public TurtleEvent(Object source) {
            super(source);
        }
    }

    public void addTurtleListener(TurtleListener listener) {
        listeners.add(listener);
    }

    private void fireTurtleStateChanged() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            TurtleListener l = (TurtleListener) i.next();
            l.turtleStateChanged(new TurtleEvent(this));
        }
    }

    private void fireTurtleReset() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            TurtleListener l = (TurtleListener) i.next();
            l.turtleReset(new TurtleEvent(this));
        }
    }

    //-------------------------------------------------------------------------
    //turtle actions

    private class ForwardAction extends AbstractAction {
        public ForwardAction() {
            super("forward(10);");
        }
        public void actionPerformed(ActionEvent e) {
            forward(10);
        }
    }
    private class TurnLeftAction extends AbstractAction {
        public TurnLeftAction() {
            super("turn(10);");
        }
        public void actionPerformed(ActionEvent e) {
            turn(10);
        }
    }
    private class TurnRightAction extends AbstractAction {
        public TurnRightAction() {
            super("turn(-10);");
        }
        public void actionPerformed(ActionEvent e) {
            turn(-10);
        }
    }
    private class PenUpAction extends AbstractAction {
        public PenUpAction() {
            super("pen_up;");
        }
        public void actionPerformed(ActionEvent e) {
            penUp();
        }
    }
    private class PenDownAction extends AbstractAction {
        public PenDownAction() {
            super("pen_down;");
        }
        public void actionPerformed(ActionEvent e) {
            penDown();
        }
    }
    private class ResetAction extends AbstractAction {
        public ResetAction() {
            super("turtle_reset;");
            //putValue(Action.SHORT_DESCRIPTION, "reset turtle");
            //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
            //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl R"));
        }
        public void actionPerformed(ActionEvent e) {
            resetTurtle();
        }
    }

    private Action forwardAction = new ForwardAction();
    private Action turnLeftAction = new TurnLeftAction();
    private Action turnRightAction = new TurnRightAction();
    private Action penUpAction = new PenUpAction();
    private Action penDownAction = new PenDownAction();
    private Action resetAction = new ResetAction();


    public Action getForwardAction() {
        return forwardAction;
    }
    public Action getTurnLeftAction() {
        return turnLeftAction;
    }
    public Action getTurnRightAction() {
        return turnRightAction;
    }
    public Action getPenUpAction() {
        return penUpAction;
    }
    public Action getPenDownAction() {
        return penDownAction;
    }
    public Action getResetAction() {
        return resetAction;
    }

}
