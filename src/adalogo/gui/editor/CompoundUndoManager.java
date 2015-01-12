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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * This class will merge individual edits into a single larger edit.
 * That is, characters entered sequentially will be grouped together and
 * undone as a group. Any attribute changes will be considered as part
 * of the group and will therefore be undone when the group is undone.
 * <br>
 * <br>
 * original code from camickr
 * <br>
 * http://forum.java.sun.com/thread.jspa?threadID=637225
 */
public class CompoundUndoManager extends UndoManager implements DocumentListener {

    private CompoundEdit compoundEdit;
    private JTextComponent textComp;

    /*
     * These fields are used to help determine whether the edit is an
     * incremental edit. For each character added the offset and length
     * should increase by 1 or decrease by 1 for each character removed.
     */
    private int lastOffset;
    private int lastLength;

    public CompoundUndoManager(JTextComponent textComp) {
        this.textComp = textComp;
        textComp.getDocument().addUndoableEditListener(this);

        //TODOlater make undo limit option
        setLimit(2000);
    }

    /**
     * Add a DocumentLister before the undo is done so we can position the Caret
     * correctly as each edit is undone.
     */
    public void undo() {
        textComp.getDocument().addDocumentListener(this);
        super.undo();
        textComp.getDocument().removeDocumentListener(this);
    }

    /**
     * Add a DocumentLister before the redo is done so we can position the Caret
     * correctly as each edit is redone.
     */
    public void redo() {
        textComp.getDocument().addDocumentListener(this);
        super.redo();
        textComp.getDocument().removeDocumentListener(this);
    }

    /**
     * handle undoable edit and then update the actions.
     */
    public void undoableEditHappened(UndoableEditEvent e) {

        //LESMANA
        handleCompoundEdit(e);

        //LESMANA
        undoAction.updateUndoState();
        redoAction.updateRedoState();

    }

    /**
     * Whenever an UndoableEdit happens the edit will either be absorbed by the
     * current compound edit or a new compound edit will be started.
     */
    private void handleCompoundEdit(UndoableEditEvent e) {

        //  Start a new compound edit
        if (compoundEdit == null) {
            compoundEdit = startCompoundEdit( e.getEdit() );
            lastLength = textComp.getDocument().getLength();
            return;
        }

        //  Check for an attribute change
        AbstractDocument.DefaultDocumentEvent event =
            (AbstractDocument.DefaultDocumentEvent)e.getEdit();

        if  (event.getType().equals(DocumentEvent.EventType.CHANGE)) {
            compoundEdit.addEdit( e.getEdit() );
            return;
        }

        //  Check for an incremental edit or backspace.
        //  The change in Caret position and Document length should be either
        //  1 or -1 .
        int offsetChange = textComp.getCaretPosition() - lastOffset;
        int lengthChange = textComp.getDocument().getLength() - lastLength;

        if (Math.abs(offsetChange) == 1 && Math.abs(lengthChange) == 1) {
            compoundEdit.addEdit(e.getEdit());
            lastOffset = textComp.getCaretPosition();
            lastLength = textComp.getDocument().getLength();
            return;
        }

        //  Not incremental edit, end previous edit and start a new one
        compoundEdit.end();
        compoundEdit = startCompoundEdit( e.getEdit() );

    }

    /**
     * Each CompoundEdit will store a group of related incremental edits
     * (ie. each character typed or backspaced is an incremental edit)
     */
    private CompoundEdit startCompoundEdit(UndoableEdit anEdit) {

        //  Track Caret and Document information of this compound edit
        lastOffset = textComp.getCaretPosition();
        lastLength = textComp.getDocument().getLength();

        //  The compound edit is used to store incremental edits
        compoundEdit = new MyCompoundEdit();
        compoundEdit.addEdit( anEdit );

        //  The compound edit is added to the UndoManager. All incremental
        //  edits stored in the compound edit will be undone/redone at once
        addEdit( compoundEdit );
        return compoundEdit;
    }

    //-------------------------------------------------------------------------
    //implement document listener

    /**
     * Updates to the Document as a result of Undo/Redo will cause the
     * Caret to be repositioned
     */
    public void insertUpdate(DocumentEvent e) {
        //System.out.println("CompoundUndoManager.insertUpdate()"); //DEBUG

        //LESMANA removed invoke later
        //because this is in event dispatching thread

        //TODOlater fix strange bug
        //calculated offset is correct
        //but caret does not move to correct position

        int offset = e.getOffset() + e.getLength();
        offset = Math.min(offset, textComp.getDocument().getLength());
        textComp.setCaretPosition( offset );
    }

    public void removeUpdate(DocumentEvent e) {
        //System.out.println("CompoundUndoManager.removeUpdate()"); //DEBUG
        textComp.setCaretPosition(e.getOffset());
    }

    public void changedUpdate(DocumentEvent e) {
        //System.out.println("CompoundUndoManager.changedUpdate()"); //DEBUG
    }


    //-------------------------------------------------------------------------
    //costumized compound edit

    class MyCompoundEdit extends CompoundEdit {

        public boolean isInProgress() {

            //  in order for the canUndo() and canRedo() methods to work
            //  assume that the compound edit is never in progress
            return false;
        }

        public void undo() throws CannotUndoException {

            //  End the edit so future edits don't get absorbed by this edit
            if (compoundEdit != null)
                compoundEdit.end();

            super.undo();

            //  Always start a new compound edit after an undo
            compoundEdit = null;
        }

        public void redo() throws CannotRedoException {
            super.redo();
        }

    }

    //-------------------------------------------------------------------------
    //LESMANA
    //actions

    /**
     * code from java tutorial
     * http://java.sun.com/docs/books/tutorial/uiswing/components/generaltext.html
     */
    public class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
            //putValue(Action.SHORT_DESCRIPTION, "undo last change");
            //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl Z"));
        }
        public void actionPerformed(ActionEvent e) {
            try {
                undo();
            } catch (CannotUndoException ex) {
                System.err.println("Unable to undo: " + ex);
            }
            updateUndoState();
            redoAction.updateRedoState();
        }
        protected void updateUndoState() {
            if (canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    /**
     * code from java tutorial
     * http://java.sun.com/docs/books/tutorial/uiswing/components/generaltext.html
     */
    public class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
            //putValue(Action.SHORT_DESCRIPTION, "undo last change");
            //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl Y"));
        }
        public void actionPerformed(ActionEvent e) {
            try {
                redo();
            } catch (CannotRedoException ex) {
                System.err.println("Unable to redo: " + ex);
            }
            updateRedoState();
            undoAction.updateUndoState();
        }
        protected void updateRedoState() {
            if (canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    public UndoAction undoAction = new UndoAction();
    public RedoAction redoAction = new RedoAction();

}
