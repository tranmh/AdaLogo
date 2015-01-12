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
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import adalogo.gui.Console;
import adalogo.gui.MenuBar;
import adalogo.gui.StatusBar;
import adalogo.gui.ToolBar;
import adalogo.gui.TurtleCanvas;
import adalogo.gui.WindowFrame;
import adalogo.gui.editor.Editor;
import adalogo.gui.varmonitor.VarMonitor;
import adalogo.visitor.VisitorMaster;

/**
 * this is the engine for the program.
 * everything goes through here.
 */
public class Engine implements WindowListener {

    private Turtle turtle;

    private Editor editor;
    private Console console;
    private TurtleCanvas canvas;
    private VarMonitor varMonitor;
    private MenuBar menuBar;
    private ToolBar toolBar;
    private StatusBar statusBar;

    private WindowFrame window;

    private VisitorMaster visitor;

    /**
     * this will create a frame,
     * an editor box,
     * a console,
     * a canvas,
     * a variable monitor
     * and a turtle.
     */
    public Engine() {

        turtle = new Turtle(this);

        editor = new Editor(this);
        console = new Console(this);
        canvas = new TurtleCanvas(this);
        varMonitor = new VarMonitor(this);
        menuBar = new MenuBar(this);
        toolBar = new ToolBar(this);
        statusBar = new StatusBar(this);
        visitor = new VisitorMaster(this);

        /*
         * window must be instantiated last
         * because it's constructor takes all the other
         * components from engine.
         */
        window = new WindowFrame(this);

        /*
         * this is necessary because at the time turtle is
         * instantiated canvas is not, so turtle constructor
         * could not have had taken the canvas from engine.
         * same for others.
         */
        turtle.init();
        editor.init();
        console.init();
        canvas.init();
        //varMonitor.init(); //TODO
        menuBar.init();
        toolBar.init();
        statusBar.init();
        visitor.init();

        window.addWindowListener(this);
        
        console.append("Welcome to AdaLogo version: @BUILDTIME@\n");

    }

    /**
     * this will show the window frame.
     */
    public void mainScreenTurnOn() {
        window.pack();
        window.setVisible(true);

    }

    /**
     * this will close the window.
     * if started as applet this will re-enable the button,
     * if started as application this will exit the program.
     */
    public void mainScreenTurnOff() {

        if (!editor.okToClose())
            return;

        if (Settings.isStartedAsApplet()) {
            window.setVisible(false);
            Settings.getApplet().stopEngine();
        } else {
            System.exit(0);
        }

    }

    /**
     * for debug purposes.
     */
    public void finalize() throws Throwable {
        super.finalize();
        System.out.println("engine dead");
    }

    //-------------------------------------------------------------------------
    //implements window listener
    //to close window

    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
        mainScreenTurnOff();
    }
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    //-------------------------------------------------------------------------
    //exit action

    private class ExitAction extends AbstractAction {
        public ExitAction() {
            super("Exit");
            //putValue(Action.SHORT_DESCRIPTION, "exit");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt F4"));
        }
        public void actionPerformed(ActionEvent e) {
            mainScreenTurnOff();
        }
    }

    public Action getExitAction() {
        return new ExitAction();
    }

    //-------------------------------------------------------------------------
    //getters for the components
    //the runtime exceptions are for debugging purposes

    public Turtle getTurtle() {
        if (turtle == null) {
            throw new RuntimeException(
            "turtle == null! this sould never happen");
        }
        return turtle;
    }

    public Console getConsole() {
        if (console == null) {
            throw new RuntimeException(
            "console == null! this sould never happen");
        }
        return console;
    }

    public Editor getEditor() {
        if (editor == null) {
            throw new RuntimeException(
            "editor == null! this sould never happen");
        }
        return editor;
    }

    public TurtleCanvas getCanvas() {
        if (canvas == null) {
            throw new RuntimeException(
            "canvas == null! this sould never happen");
        }
        return canvas;
    }

    public VarMonitor getVarMonitor() {
        if (varMonitor == null) {
            throw new RuntimeException(
            "varMonitor == null! this sould never happen");
        }
        return varMonitor;
    }

    public MenuBar getMenuBar() {
        if (menuBar == null) {
            throw new RuntimeException(
            "menuBar == null! this sould never happen");
        }
        return menuBar;
    }

    public ToolBar getToolBar() {
        if (toolBar == null) {
            throw new RuntimeException(
            "toolBar == null! this sould never happen");
        }
        return toolBar;
    }

    public StatusBar getStatusBar() {
        if (statusBar == null) {
            throw new RuntimeException(
            "statusBar == null! this sould never happen");
        }
        return statusBar;
    }

    public WindowFrame getWindow() {
        if (window == null) {
            throw new RuntimeException(
            "window == null! this sould never happen");
        }
        return window;
    }

    public VisitorMaster getVisitor() {
        if (visitor == null) {
            throw new RuntimeException(
            "visitor == null! this sould never happen");
        }
        return visitor;
    }

}
