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

import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import adalogo.Engine;
import adalogo.Settings;
import adalogo.gui.Console;
import adalogo.gui.editor.FileHandler.FileHandlerEvent;
import adalogo.gui.editor.FileHandler.FileHandlerListener;
import adalogo.lang.Lang;
import adalogo.lang.ParseException;
import adalogo.lang.SimpleNode;
import adalogo.lang.TokenMgrError;
import adalogo.visitor.InterpreterVisitor.AdaLogoSyntaxSemanticException;
import adalogo.visitor.InterpreterVisitor.AssignmentStatementException;
import adalogo.visitor.InterpreterVisitor.ExitStatementException;
import adalogo.visitor.InterpreterVisitor.IdentifierException;
import adalogo.visitor.InterpreterVisitor.InterpreterAbortException;
import adalogo.visitor.InterpreterVisitor.ProcedureCallStatementException;
import adalogo.visitor.InterpreterVisitor.WrongBooleanExpressionException;
import adalogo.visitor.InterpreterVisitor.WrongIntegerExpressionException;
import adalogo.visitor.InterpreterVisitor.WrongNumberOfChildrenException;
import adalogo.visitor.SymbolTable.SymbolTableException;

/**
 * this will manage the visitors in threads
 *
 * TODO make visitors speak only to master
 * all actions to outside should be handled with invokeLater
 */
public class VisitorMaster implements Runnable, FileHandlerListener {

    private Engine engine;
    private Console console;

    /**
     * create a visitor master
     * @param engine
     */
    public VisitorMaster(Engine engine) {
        this.engine = engine;
        this.console = engine.getConsole();

        breakPointTable = new BreakPointTable();

        //init actions
        singleStepAction.setEnabled(false);
        stopAction.setEnabled(false);

    }

    public void init() {
        engine.getEditor().addFileHandlerListener(this);
    }

    /**
     * this will store line numbers where visitors should
     * pause and wait for user interaction.
     */
    private BreakPointTable breakPointTable;

    /**
     * editor will call this to insert break point handler
     * in line number panel.
     */
    public BreakPointTable getBreakPointTable() {
        return breakPointTable;
    }

    //-------------------------------------------------------------------------
    //file handler listener

    public void documentChanged(FileHandlerEvent e) {
    }
    public void documentLoaded(FileHandlerEvent e) {
        breakPointTable.clear();
    }
    public void documentLoadFailed(FileHandlerEvent e) {
    }
    public void documentSaved(FileHandlerEvent e) {
    }
    public void documentSaveFailed(FileHandlerEvent e) {
    }

    //-------------------------------------------------------------------------
    //control flags

    /**
     * this will be true when a visitor is currently running.
     * there can be only one visitor running at any time.
     */
    private boolean running = false;

    /**
     * this is true when thread is waiting for user interaction.
     */
    private boolean waiting = false;

    /**
     * when this is set to true, visitors should stop a.s.a.p.
     * @see #isStopRequested()
     */
    private boolean stopRequested = false;

    /**
     * when this is true visitors are running in debug mode.
     * that means they should stop at break points.
     * this will be set by user interaction.
     */
    private boolean debugRun = true;

    /**
     * when this is true visitors should run step by step.
     * whatever a step might be.
     * @see #isBreak(int)
     */
    private boolean singleStep = false;

    /**
     * the line number where a visitor is waiting.
     * if this is -1 then it is undefined.
     */
    private int line = -1;

    //-------------------------------------------------------------------------
    //visitor info
    //visitors call this to get info

    /**
     * the running visitor should check this frequently and stop
     * as soon as possible if this is true.
     * @return true if user requested interpreter to stop.
     */
    public synchronized boolean isStopRequested() {
        return stopRequested;
    }

    /**
     * visitors should call this to check if debug run.
     */
    public synchronized boolean isDebugRun() {
        return debugRun;
    }

    /**
     * visitors should check this and suspend operation
     * when this returns true.
     * @return true if run single step or
     * if run debug and break point exists
     */
    public synchronized boolean isBreak(int line) {
        //dirty hack to allow gui to react even when visitor runaway
        dirtyhack++;
        if (dirtyhack % 1000 == 0) {
            Thread.yield();
            dirtyhack = 0;
        }
        if (singleStep) return true;
        return debugRun && breakPointTable.isBreakPoint(line);
    }

    //TODOlater redesign dirtyhack
    private int dirtyhack = 0;

    //-------------------------------------------------------------------------
    //visitor control
    //these methods will control the visitor behaviour

    /**
     * thread to process all the parsing and interpreting.
     * this thread will parse the code (javacc) and then start
     * a visitor which will visit the syntax tree (AST*.java).
     * <br>
     * HERE IS PARSER AND VISITOR BEGIN
     * <br>
     * @see #startVisitor(boolean)
     */
    public void run() {

        visitorStarted();

        try {

            //this might change when jjt goes static
            Lang lang = new Lang(new StringReader(engine.getEditor().getText()));

            //this throws ParseException
            SimpleNode node = lang.CompilationUnit();
            //n.dump("+++");

            if (Settings.isPrintSyntaxTree()) {
                new DumpVisitor(engine, this, node);
            }

            //TODO redesign dirty execute hack
            //with seperate visitor

            //first "check" code for errors with pretend execute
            //new InterpreterVisitor(engine, this, node, false);

            //then execute code for real
            new InterpreterVisitor(engine, this, node, true);

        } catch (AssignmentStatementException e) {
            console.appendError(e.getMessage());
            //e.printStackTrace();
        } catch (WrongBooleanExpressionException e) {
            console.appendError(e.getMessage());
            //e.printStackTrace();
        } catch (WrongIntegerExpressionException e) {
            console.appendError(e.getMessage());
            //e.printStackTrace();
        } catch (ProcedureCallStatementException e) {
            console.appendError(e.getMessage());
            //e.printStackTrace();
        } catch (InterpreterAbortException e) {
            console.appendError(e.getMessage());
            //e.printStackTrace();
        } catch (SymbolTableException e) {
            console.appendError(e.getMessage());
            //e.printStackTrace();
        } catch (IdentifierException e) {
            console.appendError(e.getMessage());
            //e.printStackTrace();
        } catch (StackOverflowError e) {
            console.appendError("Execution aborted.");
            console.appendError("Check your code for infinite loops/recursions.");
            //e.printStackTrace();
        } catch (ArithmeticException e) {
            console.appendError(e.getMessage());
            //e.printStackTrace();
        } catch (NumberFormatException e) {
            console.appendError(e.getMessage());
            //e.printStackTrace();
        } catch (WrongNumberOfChildrenException e) {
            console.appendError(e.getMessage() + " has wrong number of children.");
            console.appendError("This should never happen.");
            //e.printStackTrace();
        } catch (ExitStatementException e) {
            console.appendError("Execution aborted.");
            console.appendError("Exit is only allowed in loops.");
            //e.printStackTrace();
        } catch (AdaLogoSyntaxSemanticException e) {
            console.appendError("Execution aborted.");
            console.appendError(e.getMessage());
            //e.printStackTrace();
        } catch (ParseException e) {
            console.appendError(e.toString());
            //e.printStackTrace();
        } catch (TokenMgrError e) {
            console.appendError(e.toString());
            //e.printStackTrace();
        } catch (RuntimeException e) {
            console.appendError(e.toString());
            console.appendError("This should never happen.");
            e.printStackTrace();
        } catch (Exception e) {
            console.appendError(e.toString());
            console.appendError("This should never happen.");
            e.printStackTrace();
        }

        visitorStopped();

    }

    /**
     * this will start a visitor.
     * called by user interaction.
     */
    public synchronized void startVisitor(boolean debug) {

        //DEBUG
        //breakPointTable.dump();

        //reset all flags;
        running = false;
        waiting = false;
        stopRequested = false;

        //will always wait at first step
        singleStep = debug;
        debugRun = debug;

        Thread t = new Thread(this, "visitor thread");
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

    }

    /**
     * make visitor continue if it is waiting.
     * called by user interaction.
     */
    public synchronized void stepVisitor(boolean single) {

        singleStep = single;

        //notify a waiting visitor
        //if there is no one waiting this will do nothing
        notify();

        //reset flag
        waiting = false;

    }

    /**
     * make interpreter stop as soon as possible.
     * called by user interaction.
     */
    public synchronized void stopVisitor() {

        //set stop flag for visitor to check
        stopRequested = true;

        if (waiting) {
            //wake up visitor if it is waiting
            notify();
        }
    }

    //-------------------------------------------------------------------------
    //visitor status report
    //visitors call this to update master about their status

    /**
     * a visitor just started.
     * called by visitor.
     */
    public synchronized void visitorStarted() {

        running = true;

        startAction.setEnabled(false);
        startDebugAction.setEnabled(false);
        stopAction.setEnabled(true);
        singleStepAction.setEnabled(debugRun);
        multiStepAction.setEnabled(debugRun);

        console.append("Interpreter started");

        //notify components that visitor just started
        fireVisitorStarted();

    }

    /**
     * visitor will wait until someone calls notify.
     * called by visitor.
     */
    public synchronized void visitorWaiting(int line) {

        this.line = line;

        fireVisitorWaiting();

        try {
            waiting = true;
            wait();
        } catch (InterruptedException e) {
            System.out.println("this should never happen");
            e.printStackTrace();
        }

    }

    /**
     * the waiting visitor is now running again.
     * called by visitor.
     */
    public synchronized void visitorRunning() {
        fireVisitorRunning();
    }

    /**
     * the running visitor just stopped.
     * called by visitor.
     */
    public synchronized void visitorStopped() {

        running = false;

        startAction.setEnabled(true);
        startDebugAction.setEnabled(true);
        stopAction.setEnabled(false);
        singleStepAction.setEnabled(false);
        multiStepAction.setEnabled(false);

        console.append("Interpreter stopped");

        //notify components that visitor stopped
        fireVisitorStopped();

    }

    //-------------------------------------------------------------------------
    //event listener architecture

    private Set listeners = new HashSet();

    public interface VisitorListener extends EventListener {
        public void visitorStarted(VisitorEvent e);
        public void visitorWaiting(VisitorEvent e);
        public void visitorRunning(VisitorEvent e);
        public void visitorStopped(VisitorEvent e);
    }

    public class VisitorEvent extends EventObject {
        //TODOlater make reasonable getter methods
        public VisitorEvent(Object source) {
            super(source);
        }
        public int getLine() {
            //TODOlater redesign this
            //make line field of event object
            return line;
        }
    }

    public void addVisitorListener(VisitorListener listener) {
        listeners.add(listener);
    }

    private void fireVisitorStarted() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Iterator i = listeners.iterator(); i.hasNext(); ) {
                    VisitorListener l = (VisitorListener) i.next();
                    l.visitorStarted(new VisitorEvent(this));
                }
            }
        });
    }

    private void fireVisitorWaiting() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Iterator i = listeners.iterator(); i.hasNext(); ) {
                    VisitorListener l = (VisitorListener) i.next();
                    l.visitorWaiting(new VisitorEvent(this));
                }
            }
        });
    }

    private void fireVisitorRunning() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Iterator i = listeners.iterator(); i.hasNext(); ) {
                    VisitorListener l = (VisitorListener) i.next();
                    l.visitorRunning(new VisitorEvent(this));
                }
            }
        });
    }

    private void fireVisitorStopped() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Iterator i = listeners.iterator(); i.hasNext(); ) {
                    VisitorListener l = (VisitorListener) i.next();
                    l.visitorStopped(new VisitorEvent(this));
                }
            }
        });
    }

    //-------------------------------------------------------------------------
    //visitor actions
    //no mnemonics for these actions because of strange behaviour in toolbar

    private class StartAction extends AbstractAction {
        Engine engine;
        public StartAction(Engine en) {
            super("run");
            this.engine = en;
            putValue(Action.SHORT_DESCRIPTION, "execute the code");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl F11"));
        }
        public void actionPerformed(ActionEvent e) {
            startVisitor(false);
        }
    }
    private class StartDebugAction extends AbstractAction {
        Engine engine;
        public StartDebugAction(Engine en) {
            super("debug");
            this.engine = en;
            putValue(Action.SHORT_DESCRIPTION, "execute the code in debug mode");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F11"));
        }
        public void actionPerformed(ActionEvent e) {
            startVisitor(true);
        }
    }
    private class SingleStepAction extends AbstractAction {
        Engine engine;
        public SingleStepAction(Engine en) {
            super("single step");
            this.engine = en;
            putValue(Action.SHORT_DESCRIPTION, "step one line");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F5"));
        }
        public void actionPerformed(ActionEvent e) {
            stepVisitor(true);
        }
    }
    private class MultiStepAction extends AbstractAction {
        Engine engine;
        public MultiStepAction(Engine en) {
            super("multi step");
            this.engine = en;
            putValue(Action.SHORT_DESCRIPTION, "step to next break point");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F8"));
        }
        public void actionPerformed(ActionEvent e) {
            stepVisitor(false);
        }
    }
    private class StopAction extends AbstractAction {
        Engine engine;
        public StopAction(Engine en) {
            super("stop");
            this.engine = en;
            putValue(Action.SHORT_DESCRIPTION, "stop interpreter");
        }
        public void actionPerformed(ActionEvent e) {
            stopVisitor();
        }
    }

    private Action startAction = new StartAction(engine);
    private Action startDebugAction = new StartDebugAction(engine);
    private Action singleStepAction = new SingleStepAction(engine);
    private Action multiStepAction = new MultiStepAction(engine);
    private Action stopAction = new StopAction(engine);

    public Action getStartAction() {
        return startAction;
    }
    public Action getStartDebugAction() {
        return startDebugAction;
    }
    public Action getSingleStepAction() {
        return singleStepAction;
    }
    public Action getMultiStepAction() {
        return multiStepAction;
    }
    public Action getStopAction() {
        return stopAction;
    }

}
