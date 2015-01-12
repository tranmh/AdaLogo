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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import adalogo.Engine;
import adalogo.Settings;
import adalogo.Turtle;
import adalogo.Turtle.TurtleEvent;
import adalogo.Turtle.TurtleListener;
import adalogo.visitor.VisitorMaster.VisitorEvent;
import adalogo.visitor.VisitorMaster.VisitorListener;

/**
 * this is class to hold canvas to draw turtle
 */
public class TurtleCanvas extends JPanel
    implements
    FocusListener,
    MouseListener,
    MouseWheelListener,
    ComponentListener,
    TurtleListener,
    VisitorListener {

    private Engine engine;
    private StatusBar statusBar;

    /**
     * this to store coordinates to draw line
     */
    private GeneralPath lines;

    /**
     * center of panel relative to panel space.
     */
    private Point2D panelCenter;

    /**
     * center of panel relative to turtle space.
     * this is the only connection from panel space to turtle space.
     * this point is used so rest can be calculated with zoom.
     */
    private Point2D panelCenterCoord;

    /**
     * convenience declaration for point of origin.
     */
    private final Point2D pointOfOrigin = new Point2D.Double(0, 0);

    private Point2D turtlePosition;
    private double turtleDirection;
    private boolean turtlePenDown;

    /**
     * zoom factor.
     */
    private double zoom = 1;

    /**
     * a shape which goes along the border of the canvas.
     * this is used to detect turtle on edge (to scroll if autoscroll),
     * and used to draw (clip) the positional hint lines.
     */
    private GeneralPath border;

    private final Color backgroundColor = Color.WHITE;
    private final Color borderColor = Color.RED;
    private final Color axisColor = new Color(200, 200, 255);
    private final Color turtleColor = new Color(50, 155, 50);
    private final Color turtlePenUpColor = Color.GRAY;
    private final Color linesColor = Color.BLACK;
    private final Color pooHintColor = Color.RED;
    private final Color turtleHintColor = Color.GREEN;

    private final double ZOOMFACTOR = 0.1;
    private final double ZOOMMAX = 5;
    private final double ZOOMMIN = 0.1;

    /**
     * constructor.
     */
    public TurtleCanvas(Engine engine) {
        this.engine = engine;

        lines = new GeneralPath();
        lines.moveTo(0, 0);

        setBackground(backgroundColor);
        setBorder(BorderFactory.createEmptyBorder());
        setFocusable(true);

        addFocusListener(this); //draw border
        addMouseListener(this); //scroll
        addMouseWheelListener(this); //zoom
        addComponentListener(this); //reinit if resized

        //TODO keybind info in console
        //create keybinds to navigate in canvas
        bindKey(KeyStroke.getKeyStroke("UP"), getScrollUpAction());
        bindKey(KeyStroke.getKeyStroke("DOWN"), getScrollDownAction());
        bindKey(KeyStroke.getKeyStroke("LEFT"), getScrollLeftAction());
        bindKey(KeyStroke.getKeyStroke("RIGHT"), getScrollRigthAction());
        bindKey(KeyStroke.getKeyStroke("PAGE_UP"), getZoomInAction());
        bindKey(KeyStroke.getKeyStroke("PAGE_DOWN"), getZoomOutAction());
        bindKey(KeyStroke.getKeyStroke("HOME"), getResetAction());


    }

    /**
     * init, extended constructor.
     * called by engine.
     */
    public void init() {

        this.statusBar = engine.getStatusBar();

        //dummy default values
        turtlePosition = new Point2D.Double(0, 0);
        turtleDirection = 0;
        turtlePenDown = true;

        engine.getTurtle().addTurtleListener(this);
        engine.getVisitor().addVisitorListener(this);

        resetCanvas();

    }

    /**
     * reset canvas
     */
    public void resetCanvas() {
        zoom = 1;
        panelCenterCoord = null;
        repaint();
    }

    /**
     * helper method, binds key to action.
     */
    private void bindKey(KeyStroke key, Action action) {
        InputMap inputMap = getInputMap();
        ActionMap actionMap = getActionMap();
        Object name = action.getValue(Action.NAME);
        inputMap.put(key, name);
        actionMap.put(name, action);
    }

    //-------------------------------------------------------------------------
    //focus listener to draw border

    public void focusGained(FocusEvent e) {
        if (e.isTemporary()) return;
        setBorder(BorderFactory.createLineBorder(borderColor));
    }
    public void focusLost(FocusEvent e) {
        if (e.isTemporary()) return;
        setBorder(BorderFactory.createEmptyBorder());
    }

    //-------------------------------------------------------------------------
    //mouse listener to scroll

    public void mouseClicked(MouseEvent e) {
        requestFocusInWindow();
        scroll(e.getPoint());
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
    //mouse wheel listener to zoom

    public void mouseWheelMoved(MouseWheelEvent e) {
        //requestFocusInWindow();
        zoom(e.getWheelRotation());
    }

    //-------------------------------------------------------------------------
    //component listener to reinit canvas when resized

    public void componentResized(ComponentEvent e) {
        panelCenter = null;
        initView();
        repaint();
    }
    public void componentMoved(ComponentEvent e) {
    }
    public void componentShown(ComponentEvent e) {
    }
    public void componentHidden(ComponentEvent e) {
    }

    //-------------------------------------------------------------------------
    //turtle listener

    public void turtleStateChanged(TurtleEvent e) {

        Turtle t = (Turtle) e.getSource();
        turtlePosition = t.getPosition();
        turtleDirection = t.getDirection();
        turtlePenDown = t.isPenDown();

        //note that the lines are in turtle space (in contrast to java space)
        if (turtlePenDown) {
            lines.lineTo(
                    (float)turtlePosition.getX(),
                    (float)turtlePosition.getY());
        } else {
            lines.moveTo(
                    (float)turtlePosition.getX(),
                    (float)turtlePosition.getY());
        }

        focusTurtle();
        repaint();

    }

    /**
     * focus view on turtle if setting is set
     * TODOlater revisit
     */
    private void focusTurtle() {

        //nothing to do if canvas not initialized
        if (panelCenterCoord == null) return;

        //do not scroll if visitor running
        if (visitorRunning) return;

        Point2D turtlePS = turtlePositionInPanelSpace();

        switch (Settings.getTurtleFollowMode()) {
        case Settings.TURTLE_FOLLOW_MODE_NEVER:
            break;
        case Settings.TURTLE_FOLLOW_MODE_EDGE:
            if (!getBounds().contains(turtlePS) || border.contains(turtlePS)) {
                scroll(turtlePS);
            }
            break;
        case Settings.TURTLE_FOLLOW_MODE_ALWAYS:
            scroll(turtlePS);
            break;
        }

        repaint();
    }

    public void turtleReset(TurtleEvent e) {

        Turtle t = (Turtle) e.getSource();
        turtlePosition = t.getPosition();
        turtleDirection = t.getDirection();
        turtlePenDown = t.isPenDown();

        lines.reset();
        lines.moveTo(0, 0);

        repaint();

    }

    //-------------------------------------------------------------------------
    //visitor listener

    /**
     * this will be true when a visitor is running.
     * when this is true no repainting is done.
     * @see #repaint()
     */
    private boolean visitorRunning;

    public void visitorStarted(VisitorEvent e) {
        visitorRunning = true;
    }

    public void visitorWaiting(VisitorEvent e) {
        visitorRunning = false;
        focusTurtle();
        repaint();
    }

    public void visitorRunning(VisitorEvent e) {
        visitorRunning = true;
    }

    public void visitorStopped(VisitorEvent e) {
        visitorRunning = false;
        focusTurtle();
        repaint();
    }

    //-------------------------------------------------------------------------

    /**
     * scroll the view so that newPos is center
     */
    private void scroll(Point2D click) {

        panelCenterCoord.setLocation(convertToTurtleSpace(click));

        repaint();
        statusBar.setText("scrolled");

    }

    /**
     * zoom the view in/out.
     */
    private void zoom(int level) {

        if (level < 0) {
            zoom = Math.min(zoom + (zoom * ZOOMFACTOR), ZOOMMAX);
        } else if (level > 0) {
            zoom = Math.max(zoom - (zoom * ZOOMFACTOR), ZOOMMIN);
        } else {
            //nothing
            System.out.println("zoom level == 0? this should never happen");
        }
        //round to nearest 0.01
        zoom = ((double)((int)(zoom * 100))) / 100;
        //round to 1 if between 0.95 and 1.05
        if (Math.abs(((int)(zoom * 100)) - 100) <= 5) {
            zoom = 1;
        }
        repaint();
        statusBar.setText("zoom level now "+zoom);
    }

    //-------------------------------------------------------------------------

    /**
     * controlled repaint.
     * if interpreter is running this will be disabled.
     */
    public void repaint() {
        //need this because repaint is called even before engine is instantiated
        if (visitorRunning)
            return;
        else
            super.repaint();
    }

    /**
     * custom paint.
     * this will paint the turtle lines and the turtle.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        initView();

        //everyone gets a new copy of g2d
        drawAxes((Graphics2D)g.create());
        drawPositionalHints((Graphics2D)g.create());
        drawTurtleLines((Graphics2D)g.create());
        drawTurtle((Graphics2D)g.create());

    }

    public void initView() {
        if (panelCenter == null) {
            Rectangle2D panelBounds = getBounds();
            panelCenter = new Point2D.Double(
                    panelBounds.getCenterX(),
                    panelBounds.getCenterY());

            double pb_x = panelBounds.getX();
            double pb_y = panelBounds.getY();
            double pb_w = pb_x + panelBounds.getWidth();
            double pb_h = pb_y + panelBounds.getHeight();

            float dx = (float)(pb_w / 25);
            float dy = (float)(pb_h / 25);

            border = new GeneralPath();
            border.moveTo((float)pb_x, (float)pb_y);
            border.lineTo((float)pb_x, (float)pb_h);
            border.lineTo((float)pb_w, (float)pb_h);
            border.lineTo((float)pb_w, (float)pb_y);
            border.lineTo((float)pb_x+dx, (float)pb_y);
            border.lineTo((float)pb_x+dx, (float)pb_y+dy);
            border.lineTo((float)pb_w-dx, (float)pb_y+dy);
            border.lineTo((float)pb_w-dx, (float)pb_h-dy);
            border.lineTo((float)pb_x+dx, (float)pb_h-dy);
            border.lineTo((float)pb_x+dx, (float)pb_y);
            border.closePath();
        }

        if (panelCenterCoord == null) {
            panelCenterCoord = new Point2D.Double(0, 0);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * draw the axes and/or the coordinates
     */
    private void drawAxes(Graphics2D g2d) {

        Rectangle2D panelBounds = getBounds();

        double pb_x = panelBounds.getX();
        double pb_y = panelBounds.getY();
        double pb_w = panelBounds.getWidth();
        double pb_h = panelBounds.getHeight();

        Point2D pooPS = pointOfOriginInPanelSpace();
        double poo_x = pooPS.getX();
        double poo_y = pooPS.getY();

        double res = 100 * zoom;

        //TODOlater tweak values
        while (res < 50) res *= 2;
        while (res > 125) res /= 2;

        //light blue
        g2d.setColor(axisColor);

        double y_begin = poo_y % res;
        double y_end = pb_y + pb_h;

        //x axes, y coords are *(-1) because of upside down user space
        for (double y = y_begin; y < y_end; y += res) {
            if (Math.round(y-poo_y) == 0)
                g2d.setColor(axisColor.darker());
            else
                g2d.setColor(axisColor);
            g2d.draw(new Line2D.Double(pb_x, y, pb_x + pb_w, y));
            String ys = new Integer(
                    (int)Math.round((y-poo_y)/zoom)*(-1)).toString();
            g2d.drawString(ys, (int)pb_x+2, (int)y-2);
        }

        double x_begin = poo_x % res;
        double x_end = pb_x + pb_w;

        //y axes
        for (double x = x_begin; x < x_end; x += res) {
            if (Math.round(x-poo_x) == 0)
                g2d.setColor(axisColor.darker());
            else
                g2d.setColor(axisColor);
            g2d.draw(new Line2D.Double(x, pb_y, x, pb_y + pb_h));
            String xs = new Integer(
                    (int)Math.round((x-poo_x)/zoom)).toString();
            g2d.drawString(xs, (int)x+2, (int)(pb_y+pb_h)-2);
        }

    }

    /**
     * draw all turtle lines
     */
    private void drawTurtleLines(Graphics2D g2d) {

        Point2D pooPS = pointOfOriginInPanelSpace();
        double poo_x = pooPS.getX();
        double poo_y = pooPS.getY();

        g2d.setRenderingHints(getRenderingHints());

        //black
        g2d.setColor(linesColor);

        //nice line joins and ends
        g2d.setStroke(new BasicStroke(1,
                BasicStroke.JOIN_ROUND,
                BasicStroke.CAP_ROUND));

        //draw begin at (0|0)
        g2d.translate(poo_x, poo_y);

        //zoom y negative because of upside down java space
        g2d.scale(zoom, -zoom);

        if (lines != null)
            g2d.draw(lines);

    }

    /**
     * draw the turtle
     */
    private void drawTurtle(Graphics2D g2d) {

        //turtle body size
        int radius = 10;

        Ellipse2D.Double turtlePen =
            new Ellipse2D.Double(-1, -1, 2, 2);
        Ellipse2D.Double turtleBody =
            new Ellipse2D.Double(-radius, -radius, radius*2, radius*2);
        Line2D.Double turtleHead =
            new Line2D.Double(0, -radius*1.1, 0, -radius*2.0);

        Point2D turtlePS = turtlePositionInPanelSpace();
        double turtle_x = turtlePS.getX();
        double turtle_y = turtlePS.getY();

        double dir = turtleDirection;

        g2d.setRenderingHints(getRenderingHints());

        if (turtlePenDown)
            g2d.setColor(turtleColor); //dark green
        else
            g2d.setColor(turtlePenUpColor); //gray

        //thicker line
        g2d.setStroke(new BasicStroke(3,
                BasicStroke.JOIN_ROUND,
                BasicStroke.CAP_ROUND));

        //translate to draw turtle at position
        g2d.translate(turtle_x, turtle_y);
        g2d.scale(zoom, zoom);

        g2d.draw(turtlePen);
        g2d.draw(turtleBody);

        //rotate to draw turtle direction
        g2d.rotate(Math.toRadians(-dir) + Math.PI/2);

        g2d.draw(turtleHead);

    }

    /**
     * draw positional hints when either
     * point of origin or turtle are off view.
     */
    private void drawPositionalHints(Graphics2D g2d) {

        double pc_x = panelCenter.getX();
        double pc_y = panelCenter.getY();

        Point2D pooPS = pointOfOriginInPanelSpace();
        double poo_x = pooPS.getX();
        double poo_y = pooPS.getY();

        Point2D turtlePS = turtlePositionInPanelSpace();
        double turtle_x = turtlePS.getX();
        double turtle_y = turtlePS.getY();

        g2d.setRenderingHints(getRenderingHints());
        //g2d.draw(border);
        //g2d.fill(border);
        g2d.setClip(border);

        //TODOlater draw real arrows and/or draw text

        //if point of origin out of view
        if (!getBounds().contains(poo_x, poo_y)) {
            g2d.setColor(pooHintColor);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(new Line2D.Double(pc_x, pc_y, poo_x, poo_y));
        }

        //if turtle out of view
        if (!getBounds().contains(turtle_x, turtle_y)) {
            g2d.setColor(turtleHintColor);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(new Line2D.Double(pc_x, pc_y, turtle_x, turtle_y));
        }
    }

    //-------------------------------------------------------------------------
    /**
     * get the rendering hints (anti alias and render quality)
     * as set in preferences.
     */
    private RenderingHints getRenderingHints() {
        RenderingHints renderHints = new RenderingHints(null);
        if (Settings.isAntiAlias()) {
            renderHints.put(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            renderHints.put(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        if (Settings.isRenderQuality()) {
            renderHints.put(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
        } else {
            renderHints.put(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
        }
        return renderHints;
    }

    //-------------------------------------------------------------------------

    /**
     * convert point from panel space to turtle space.
     * turtle space is math space.
     * origin is somewhere relative to panelCenterCoord.
     * x positive to right.
     * y positive up.
     */
    private Point2D convertToTurtleSpace(Point2D point) {
        double point_x = point.getX();
        double point_y = point.getY();
        double pc_x = panelCenter.getX();
        double pc_y = panelCenter.getY();
        double pcc_x = panelCenterCoord.getX();
        double pcc_y = panelCenterCoord.getY();
        double new_x = pcc_x + ((point_x - pc_x) / zoom);
        double new_y = pcc_y - ((point_y - pc_y) / zoom);
        return new Point2D.Double(new_x, new_y);
    }

    /**
     * convert point from panel space to turtle space.
     * panel space is the java space.
     * origin is top left.
     * x positive to right.
     * y positive down.
     */
    private Point2D convertToPanelSpace(Point2D point) {
        double point_x = point.getX();
        double point_y = point.getY();
        double pc_x = panelCenter.getX();
        double pc_y = panelCenter.getY();
        double pcc_x = panelCenterCoord.getX();
        double pcc_y = panelCenterCoord.getY();
        double new_x = pc_x + ((point_x - pcc_x) * zoom);
        double new_y = pc_y - ((point_y - pcc_y) * zoom);
        return new Point2D.Double(new_x, new_y);
    }

    /**
     * convenience method for turtle position.
     * @see #convertToPanelSpace(Point2D)
     */
    private Point2D turtlePositionInPanelSpace() {
        return convertToPanelSpace(turtlePosition);
    }

    /**
     * convenience method for point of origin.
     * @see #convertToPanelSpace(Point2D)
     */
    private Point2D pointOfOriginInPanelSpace() {
        return convertToPanelSpace(pointOfOrigin);
    }

    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.awt.Component#getMinimumSize()
     */
    public Dimension getMinimumSize() {
        return new Dimension(100, 100);
    }
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        return new Dimension(300, 300);
    }

    //-------------------------------------------------------------------------
    //actions for toolbar

    private class ResetAction extends AbstractAction {
        public ResetAction() {
            super("reset view");
            putValue(Action.SHORT_DESCRIPTION,
                    "reset view of canvas: default zoom, center to (0,0)");
        }
        public void actionPerformed(ActionEvent e) {
            resetCanvas();
        }
    }
    private class ScrollLeftAction extends AbstractAction {
        public ScrollLeftAction() {
            super("scroll left");
        }
        public void actionPerformed(ActionEvent e) {
            scroll(new Point2D.Double(
                    panelCenter.getX() - 10,
                    panelCenter.getY()));
        }
    }
    private class ScrollRigthAction extends AbstractAction {
        public ScrollRigthAction() {
            super("scroll rigth");
        }
        public void actionPerformed(ActionEvent e) {
            scroll(new Point2D.Double(
                    panelCenter.getX() + 10,
                    panelCenter.getY()));
        }
    }
    private class ScrollUpAction extends AbstractAction {
        public ScrollUpAction() {
            super("scroll up");
        }
        public void actionPerformed(ActionEvent e) {
            scroll(new Point2D.Double(
                    panelCenter.getX(),
                    panelCenter.getY() - 10));
        }
    }
    private class ScrollDownAction extends AbstractAction {
        public ScrollDownAction() {
            super("scroll down");
        }
        public void actionPerformed(ActionEvent e) {
            scroll(new Point2D.Double(
                    panelCenter.getX(),
                    panelCenter.getY() + 10));
        }
    }
    private class ZoomInAction extends AbstractAction {
        public ZoomInAction() {
            super("zoom in");
        }
        public void actionPerformed(ActionEvent e) {
            zoom(-1);
        }
    }
    private class ZoomOutAction extends AbstractAction {
        public ZoomOutAction() {
            super("zoom out");
        }
        public void actionPerformed(ActionEvent e) {
            zoom(1);
        }
    }

    private Action resetAction = new ResetAction();
    private Action scrollLeftAction = new ScrollLeftAction();
    private Action scrollRigthAction = new ScrollRigthAction();
    private Action scrollUpAction = new ScrollUpAction();
    private Action scrollDownAction = new ScrollDownAction();
    private Action zoomInAction = new ZoomInAction();
    private Action zoomOutAction = new ZoomOutAction();

    public Action getResetAction() {
        return resetAction;
    }
    public Action getScrollLeftAction() {
        return scrollLeftAction;
    }
    public Action getScrollRigthAction() {
        return scrollRigthAction;
    }
    public Action getScrollUpAction() {
        return scrollUpAction;
    }
    public Action getScrollDownAction() {
        return scrollDownAction;
    }
    public Action getZoomInAction() {
        return zoomInAction;
    }
    public Action getZoomOutAction() {
        return zoomOutAction;
    }

}
