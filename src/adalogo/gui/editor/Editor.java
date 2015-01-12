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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.CaretListener;

import adalogo.Engine;
import adalogo.Examples;
import adalogo.Settings;
import adalogo.gui.StatusBar;
import adalogo.gui.WindowFrame;
import adalogo.gui.editor.FileHandler.FileHandlerEvent;
import adalogo.gui.editor.FileHandler.FileHandlerListener;
import adalogo.visitor.VisitorMaster.VisitorEvent;
import adalogo.visitor.VisitorMaster.VisitorListener;

/**
 * This class hold the editor
 */
public class Editor extends JPanel implements FileHandlerListener, VisitorListener {

    private Engine engine;
    private StatusBar statusBar;
    private WindowFrame window;

    private NoWrapTextPane editor;
    private SyntaxDocument doc;
    private IndentEditorKit kit;

    private CompoundUndoManager undoManager;

    private LineHighlightHandler lineHighlightHandler;

    private AdaLogoFileHandler fileHandler;

    //needed because of init method
    private JScrollPane scroll;

    public Editor(Engine en) {
        engine = en;

        editor = new NoWrapTextPane();
        editor.setFont(Settings.getEditorFont());

        //WARNING the document must never be replaced with another document
        //because of the many listeners attached to the document
        //TODOlater fix this

        kit = new IndentEditorKit();
        doc = new SyntaxDocument();

        editor.setEditorKit(kit);
        editor.setDocument(doc);

        undoManager = new CompoundUndoManager(editor);

        lineHighlightHandler = new LineHighlightHandler(editor);

        setLayout(new BorderLayout());

        scroll = new JScrollPane(editor);
        add(scroll, BorderLayout.CENTER);

        //moved to init
        //scroll.setRowHeaderView(new LineNumberPanel(editor));

    }

    /**
     * init, extended constructor.
     * called by engine.
     */
    public void init() {
        this.statusBar = engine.getStatusBar();
        this.window = engine.getWindow();

        //TODO redesign init methods
        //this is here because when cosntructor is executed
        //visitor has not been instantiated
        scroll.setRowHeaderView(new LineNumberPanel(editor, engine.getVisitor().getBreakPointTable()));

        engine.getVisitor().addVisitorListener(this);

        fileHandler = new AdaLogoFileHandler(engine, editor);

        fileHandler.addFileHandlerListener(this);

        //load template in editor
        fileHandler.newDocument();

    }

    /**
     * get text from editor.
     * this is called when starting interpreter.
     */
    public String getText() {
        return editor.getText();
    }

    /**
     * return true if it is ok to close.
     * engine will ask this when it is about to close the window.
     * this will delegate to save load handler.
     */
    public boolean okToClose() {
        return fileHandler.okToClose();
    }

    /**
     * delegate method for external components to add caret listener.
     * currently used by status bar.
     */
    public void addCaretListener(CaretListener listener) {
        editor.addCaretListener(listener);
    }

    /**
     * delegate for external components to add file listener.
     * currently used by visitor master to reset break points.
     */
    public void addFileHandlerListener(FileHandlerListener listener) {
        fileHandler.addFileHandlerListener(listener);
    }

    //-------------------------------------------------------------------------
    //visitor listener

    /**
     * this will be true when a visitor is running.
     * when this is true certain actions are not allowed.
     */
    private boolean visitorRunning;

    public void visitorStarted(VisitorEvent e) {
        editor.setEditable(false);
        visitorRunning = true;
    }

    public void visitorWaiting(VisitorEvent e) {
        int line = e.getLine();
        if (line == -1) return;
        lineHighlightHandler.removeHighlight();
        lineHighlightHandler.addHighlight(line-1);
    }

    public void visitorRunning(VisitorEvent e) {
    }

    public void visitorStopped(VisitorEvent e) {
        editor.setEditable(true);
        lineHighlightHandler.removeHighlight();
        visitorRunning = false;
    }

    //-------------------------------------------------------------------------
    //file handler listener

    /**
     * update title of window frame with currently loaded file name.
     * will be called from document listener and from save load operations.
     * see this like some sort of event listener.
     */
    public void documentChanged(FileHandlerEvent e) {

        File file = e.getCurrentFile();
        String name = e.getCurrentFileName();
        boolean changed = e.isCurrentFileChanged();

        if (file == null) {
            fileHandler.saveAction.setEnabled(false);
            fileHandler.revertAction.setEnabled(false);
        } else {
            fileHandler.saveAction.setEnabled(changed);
            fileHandler.revertAction.setEnabled(changed);
        }

        //TODO move code to window frame
        if (changed)
            window.setTitle("(* "+name+")");
        else
            window.setTitle("("+name+")");

    }

    public void documentLoaded(FileHandlerEvent e) {

        //delegate
        documentChanged(e);

        //reset undo manager
        undoManager.discardAllEdits();
        undoManager.undoAction.updateUndoState();
        undoManager.redoAction.updateRedoState();

        //put caret to top
        editor.setCaretPosition(0);

        //TODO move to statusbar
        String name = e.getCurrentFileName();
        statusBar.setText("load success: "+name);

    }

    public void documentLoadFailed(FileHandlerEvent e) {
        String name = e.getCurrentFileName();
        statusBar.setText("load failed: "+name);
    }

    public void documentSaved(FileHandlerEvent e) {

        //delegate
        documentChanged(e);

        String name = e.getCurrentFileName();
        statusBar.setText("save success: "+name);

    }

    public void documentSaveFailed(FileHandlerEvent e) {
        String name = e.getCurrentFileName();
        statusBar.setText("save failed: "+name);
    }

    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.awt.Component#getMinimumSize()
     */
    public Dimension getMinimumSize() {
        return super.getMinimumSize();
    }
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        return new Dimension(450, 500);
    }

    //-------------------------------------------------------------------------
    //actions

    /**
     * simple wrapper class. this will wrap an existing action and
     * filter the action performed for file permissions.
     */
    private class FilePermissionAction extends AbstractAction {
        Action base;
        public FilePermissionAction(Action base) {
            this.base = base;
        }
        public void actionPerformed(ActionEvent e) {
            if (Settings.isFilePermission())
                base.actionPerformed(e);
            else
                //TODO write better text for denied file permission
                JOptionPane.showMessageDialog(window,
                        "Sorry, I am not allowed to access files." +
                        "You have to accept the certificate.");
        }
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            base.addPropertyChangeListener(listener);
        }
        public boolean equals(Object obj) {
            return base.equals(obj);
        }
        public Object getValue(String key) {
            return base.getValue(key);
        }
        public int hashCode() {
            return base.hashCode();
        }
        public boolean isEnabled() {
            return base.isEnabled();
        }
        public void putValue(String key, Object value) {
            base.putValue(key, value);
        }
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            base.removePropertyChangeListener(listener);
        }
        public void setEnabled(boolean b) {
            base.setEnabled(b);
        }
        public String toString() {
            return base.toString();
        }
    }

    /**
     * simple wrapper class. this will wrap an existing action and
     * filter the action performed when visitor is running.
     * @see Editor#visitorRunning
     */
    private class NoVisitorAction extends AbstractAction {
        Action base;
        public NoVisitorAction(Action base) {
            this.base = base;
        }
        public void actionPerformed(ActionEvent e) {
            if (visitorRunning)
                //TODO write better text for stop interpreter
                JOptionPane.showMessageDialog(window, "stop interpreter first");
            else
                base.actionPerformed(e);
        }
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            base.addPropertyChangeListener(listener);
        }
        public boolean equals(Object obj) {
            return base.equals(obj);
        }
        public Object getValue(String key) {
            return base.getValue(key);
        }
        public int hashCode() {
            return base.hashCode();
        }
        public boolean isEnabled() {
            return base.isEnabled();
        }
        public void putValue(String key, Object value) {
            base.putValue(key, value);
        }
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            base.removePropertyChangeListener(listener);
        }
        public void setEnabled(boolean b) {
            base.setEnabled(b);
        }
        public String toString() {
            return base.toString();
        }
    }

    //get actions from save load handler
    public Action getNewAction() {
        return new NoVisitorAction(fileHandler.newAction);
    }
    public Action getOpenAction() {
        return new FilePermissionAction(new NoVisitorAction(fileHandler.openAction));
    }
    public Action getSaveAction() {
        return new FilePermissionAction(fileHandler.saveAction);
    }
    public Action getSaveAsAction() {
        return new FilePermissionAction(fileHandler.saveAsAction);
    }
    public Action getRevertAction() {
        return new FilePermissionAction(new NoVisitorAction(fileHandler.revertAction));
    }

    //get actions from undo manager
    public Action getUndoAction() {
        return new NoVisitorAction(undoManager.undoAction);
    }
    public Action getRedoAction() {
        return new NoVisitorAction(undoManager.redoAction);
    }

    //get from editor kit
    public Action getCutAction() {
        return new NoVisitorAction(kit.cutAction);
    }
    public Action getCopyAction() {
        return kit.copyAction;
    }
    public Action getPasteAction() {
        return new NoVisitorAction(kit.pasteAction);
    }

    public Action[] getFontSizeActions() {
        return kit.fontSizeActions;
    }

    public Action getCommentAction() {
        return kit.commentAction;
    }
    public Action getUncommentAction() {
        return kit.uncommentAction;
    }

    //-------------------------------------------------------------------------
    //example actions

    /**
     * the action class which will be used to create the menuitems
     * this action will call the static field and method from Example.java
     */
    private class ExampleAction extends AbstractAction {
        int number;
        public ExampleAction(int number) {
            super(number+". "+Examples.example[number]);
            this.number = number;
            putValue(Action.SHORT_DESCRIPTION, "load this example in the editor");
        }
        public void actionPerformed(ActionEvent e) {
            System.out.println("ExampleAction.actionPerformed()");
            InputStream stream = Examples.getExample(number);
            String name = Examples.example[number];
            fileHandler.openDocument(stream, name);
        }
    }

    /**
     * initialize the example actions.
     */
    private Action[] initExampleActions() {
        Action[] action = new Action[Examples.example.length];
        for (int i = 0; i < Examples.example.length; i++) {
            action[i] = new NoVisitorAction(new ExampleAction(i));
        }
        return action;
    }

    public Action[] exampleActions = initExampleActions();

    //get example actions
    public Action[] getExampleActions() {
        return exampleActions;
    }

}
