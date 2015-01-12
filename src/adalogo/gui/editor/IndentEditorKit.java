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
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TextAction;

public class IndentEditorKit extends StyledEditorKit {

    //TODOlater tab width option
    //keep StringBuffer space in sync with TABWIDTH
    final int TABWIDTH = 2;
    final StringBuffer space = new StringBuffer("  ");

    //TODOlater make language independent
    final String COMMENT = "--";

    public IndentEditorKit() {
        super();
        initClipboardActions();
        initFontSizeActions();
        initCommentActions();
    }

    /**
     * helper to init clipboard actions. called from constructor.
     * clipboard actions instantiated manually
     * because of bugs in java:<br>
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4760425<br>
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4841767<br>
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4902837<br>
     */
    private void initClipboardActions() {

        cutAction = new CutAction();
        cutAction.putValue(Action.NAME, "Cut");
        cutAction.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
        cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));

        copyAction = new CopyAction();
        copyAction.putValue(Action.NAME, "Copy");
        copyAction.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));

        pasteAction = new PasteAction();
        pasteAction.putValue(Action.NAME, "Paste");
        pasteAction.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));

    }

    /**
     * helper to init font size actions. called from constructor.
     */
    private void initFontSizeActions() {
        //TODOlater fix this
        //problem: font size actions call get source from event object
        //but source is menu item, not text component
        fontSizeActions = new Action[] {
                new FontSizeAction("font-size-8", 8),
                new FontSizeAction("font-size-10", 10),
                new FontSizeAction("font-size-12", 12),
                new FontSizeAction("font-size-14", 14),
                new FontSizeAction("font-size-16", 16),
                new FontSizeAction("font-size-18", 18),
                new FontSizeAction("font-size-24", 24),
                new FontSizeAction("font-size-36", 36),
                new FontSizeAction("font-size-48", 48)};
    }

    private void initCommentActions() {
        commentAction = new ToggleCommentAction("Comment", true);
        uncommentAction = new ToggleCommentAction("Uncomment", false);
    }

    //-------------------------------------------------------------------------
    //actions will be initialized by constructor

    public Action cutAction;
    public Action copyAction;
    public Action pasteAction;

    public Action[] fontSizeActions;

    public Action commentAction;
    public Action uncommentAction;

    //-------------------------------------------------------------------------
    //overriden methods to plant custom actions

    /**
     * overriden getActions to replace certain actions with
     * the analog indent actions (tab enter delete backspace home).
     * all but the shift tab action is inserted here.
     * this is because there is no input map entry for shift tab
     * (or at least i have no idea where or how).
     * the shift tab action is bound to the textpane directly in
     * the overridden install method
     *
     * @see IndentEditorKit#install(JEditorPane)
     */
    public Action[] getActions() {
        Action[] actions = super.getActions();
        for (int i = 0; i < actions.length; i++) {
            actions[i] = replaceWithIndentActions(actions[i]);
        }
        return actions;
    }

    /**
     * helper method. called from getActions().
     * this will look for certain default actions
     * and replace them with the analog indent action.
     * this will also initialize the cut copy paste actions
     * to be taken for the menu bar.
     *
     * @see IndentEditorKit#getActions()
     */
    private Action replaceWithIndentActions(Action action) {

        String name = (String)action.getValue(Action.NAME);

        if (name.equals(DefaultEditorKit.insertTabAction))
            return new TabIndentAction(name, true);

        if (name.equals(DefaultEditorKit.insertBreakAction))
            return new EnterIndentAction(name, action);

        if (name.equals(DefaultEditorKit.deleteNextCharAction))
            return new DeleteIndentAction(name, action);

        if (name.equals(DefaultEditorKit.deletePrevCharAction))
            return new BackspaceIndentAction(name, action);

        if (name.equals(DefaultEditorKit.beginLineAction))
            return new HomeIndentAction(name, false);

        if (name.equals(DefaultEditorKit.selectionBeginLineAction))
            return new HomeIndentAction(name, true);

        return action;
    }

    /**
     * override install to add shift tab action.
     * the getActions() was not able to install shift tab.
     * so it is added here using the traditional inputmap actionmap method.
     *
     * @see IndentEditorKit#getActions()
     */
    public void install(JEditorPane pane) {
        super.install(pane);

        KeyStroke shiftTabKey = KeyStroke.getKeyStroke("shift TAB");
        Action shiftTabAction = new TabIndentAction("unindent text", false);

        InputMap inputMap = pane.getInputMap();
        ActionMap actionMap = pane.getActionMap();

        Object name = shiftTabAction.getValue(Action.NAME);

        inputMap.put(shiftTabKey, name);
        actionMap.put(name, shiftTabAction);
    }

    //---------------------------------------------------------------------------
    //custom actions

    /**
     * multiline tab indent unindent.
     * insert spaces instead of tab.
     * use this as action for tab key.
     * <br>
     * <br>
     * code from camickr.
     * <br>
     * http://forum.java.sun.com/thread.jspa?threadID=468400
     *
     * @see DefaultEditorKit#insertTabAction
     */
    class TabIndentAction extends TextAction {

        private boolean indent;

        public TabIndentAction(String name, boolean indent) {
            super(name);
            this.indent = indent;
        }

        public void actionPerformed(ActionEvent e) {
            //System.out.println("TabIndentAction.actionPerformed()");

            JTextComponent editor = getTextComponent(e);

            if (editor == null)
                return;

            int selectionStart = editor.getSelectionStart();
            //int selectionEnd = editor.getSelectionEnd();
            int selectionEnd = Math.max(selectionStart, editor.getSelectionEnd() - 1);
            Document doc = editor.getDocument();
            Element root = doc.getDefaultRootElement();
            int lineStart = root.getElementIndex(selectionStart);
            int lineEnd = root.getElementIndex(selectionEnd);

            try {

                for (int i = lineStart; i <= lineEnd; i++) {

                    Element line = root.getElement(i);
                    int offset = line.getStartOffset();

                    //this would have been nice
                    //but it returns offset for next line if line was empty
                    //int spaceCount = Utilities.getNextWord(editor, offset) - offset;

                    int spaceCount = 0;
                    while (doc.getText(offset + spaceCount, 1).equals(" "))
                        spaceCount++;

                    int indentCount = TABWIDTH - (spaceCount % TABWIDTH);

                    if (indent)
                        doc.insertString(offset, space.substring(0, indentCount), null);
                    else
                        doc.remove(offset, Math.min(TABWIDTH, spaceCount));

                }

                //TODO fix beginning of selection in first line

            } catch (BadLocationException ble) {
                System.out.println("never happens");
            }

        }

    }

    /**
     * enter will indent as many spaces there were in the previous line.
     *
     * @see DefaultEditorKit#insertBreakAction
     */
    class EnterIndentAction extends TextAction {

        Action orig;

        public EnterIndentAction(String name, Action orig) {
            super(name);
            this.orig = orig;
        }

        public void actionPerformed(ActionEvent e) {
            //System.out.println("EnterIndentAction.actionPerformed()");

            orig.actionPerformed(e);
            //after this there will be no selection, only the caret

            JTextComponent editor = getTextComponent(e);

            if (editor == null)
                return;

            Document doc = editor.getDocument();
            Element root = doc.getDefaultRootElement();
            int caretOffset = editor.getCaretPosition();
            int lineIndex = root.getElementIndex(caretOffset);

            Element line = root.getElement(lineIndex);
            int lineOffset = line.getStartOffset();

            Element prevLine = root.getElement(lineIndex-1);
            int prevLineOffset = prevLine.getStartOffset();

            try {

                StringBuffer spaceCount = new StringBuffer();
                while (doc.getText(prevLineOffset + spaceCount.length(), 1).equals(" ")) {
                    spaceCount.append(' ');
                }

                doc.insertString(lineOffset, spaceCount.toString(), null);

            } catch (BadLocationException ble) {
                System.out.println("never happens");
            }

        }

    }

    /**
     * delete will remove TABWIDTH many spaces at once,
     * if cursor at beginning of line.
     *
     * @see DefaultEditorKit#deleteNextCharAction
     */
    class DeleteIndentAction extends TextAction {

        Action orig;

        public DeleteIndentAction(String name, Action orig) {
            super(name);
            this.orig = orig;
        }

        public void actionPerformed(ActionEvent e) {
            //System.out.println("DeleteIndentAction.actionPerformed()");

            JTextComponent editor = getTextComponent(e);

            if (editor == null)
                return;

            int selectionStart = editor.getSelectionStart();
            int selectionEnd = editor.getSelectionEnd();

            Document doc = editor.getDocument();
            Element root = doc.getDefaultRootElement();
            int caretOffset = editor.getCaretPosition();
            int lineIndex = root.getElementIndex(caretOffset);
            Element line = root.getElement(lineIndex);
            int lineOffset = line.getStartOffset();

            try {

                int spaceCount = 0;
                while (doc.getText(lineOffset+spaceCount, 1).equals(" "))
                    spaceCount++;

                int indentCount = spaceCount % TABWIDTH;
                indentCount = (indentCount == 0)? TABWIDTH : indentCount;

                if (selectionStart < selectionEnd
                        || caretOffset + indentCount > lineOffset + spaceCount
                        || caretOffset >= lineOffset + spaceCount) {
                    //selection
                    //not enough space to indent
                    //middle of line
                    orig.actionPerformed(e);
                } else {
                    //begin of line
                    //doc.remove(caretOffset, Math.min(TABWIDTH, spaceCount - (caretOffset - lineOffset)));
                    doc.remove(caretOffset, indentCount);
                }

            } catch (BadLocationException ble) {
                System.out.println("never happens");
            }

        }

    }

    /**
     * backspace will remove TABWIDTH many spaces at once,
     * if cursor at beginning of line.
     *
     * @see DefaultEditorKit#deletePrevCharAction
     */
    class BackspaceIndentAction extends TextAction {

        Action orig;

        public BackspaceIndentAction(String name, Action orig) {
            super(name);
            this.orig = orig;
        }

        public void actionPerformed(ActionEvent e) {
            //System.out.println("BackspaceIndentAction.actionPerformed()");

            JTextComponent editor = getTextComponent(e);

            if (editor == null)
                return;

            int selectionStart = editor.getSelectionStart();
            int selectionEnd = editor.getSelectionEnd();

            Document doc = editor.getDocument();
            Element root = doc.getDefaultRootElement();
            int caretOffset = editor.getCaretPosition();
            int lineIndex = root.getElementIndex(caretOffset);
            Element line = root.getElement(lineIndex);
            int lineOffset = line.getStartOffset();

            try {

                int spaceCount = 0;
                while (doc.getText(lineOffset+spaceCount, 1).equals(" "))
                    spaceCount++;

                int indentCount = spaceCount % TABWIDTH;
                indentCount = (indentCount == 0)? TABWIDTH : indentCount;

                if (selectionStart < selectionEnd
                        || caretOffset == lineOffset
                        || caretOffset != lineOffset + spaceCount) {
                    //selection
                    //caret at absolute beginning of line
                    //caret in middle of line
                    orig.actionPerformed(e);
                } else {
                    //caret at first non space character of line
                    doc.remove(caretOffset-indentCount, indentCount);
                }

            } catch (BadLocationException ble) {
                System.out.println("never happens");
            }
        }

    }

    /**
     * home will toggle between absolute beginning of line
     * and relative beginning of line (first non whitespace character).
     *
     * @see DefaultEditorKit#beginLineAction
     */
    class HomeIndentAction extends TextAction {

        boolean select;

        public HomeIndentAction(String name, boolean select) {
            super(name);
            this.select = select;
        }

        public void actionPerformed(ActionEvent e) {
            //System.out.println("HomeIndentAction.actionPerformed()");

            JTextComponent editor = getTextComponent(e);

            if (editor == null)
                return;

            Document doc = editor.getDocument();
            Element root = doc.getDefaultRootElement();
            int caretOffset = editor.getCaretPosition();
            int lineIndex = root.getElementIndex(caretOffset);
            Element line = root.getElement(lineIndex);
            int lineOffset = line.getStartOffset();

            try {

                int spaceCount = 0;
                while (doc.getText(lineOffset+spaceCount, 1).equals(" "))
                    spaceCount++;

                int newOffset = 0;

                if (caretOffset == lineOffset + spaceCount)
                    newOffset = lineOffset;
                else
                    newOffset = lineOffset + spaceCount;

                if (select)
                    editor.moveCaretPosition(newOffset);
                else
                    editor.setCaretPosition(newOffset);

            } catch (BadLocationException bl) {
                System.out.println("never happens");
            }

        }

    }

    /**
     * multi line comment uncomment toggle.
     */
    class ToggleCommentAction extends TextAction {

        boolean comment;

        public ToggleCommentAction(String name, boolean comment) {
            super(name);
            this.comment = comment;
            //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
            if (comment)
                putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl D"));
            else
                putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl shift D"));
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent editor = getTextComponent(e);

            if (editor == null)
                return;

            int selectionStart = editor.getSelectionStart();
            //int selectionEnd = editor.getSelectionEnd();
            int selectionEnd = Math.max(selectionStart, editor.getSelectionEnd() - 1);
            Document doc = editor.getDocument();
            Element root = doc.getDefaultRootElement();
            int lineStart = root.getElementIndex(selectionStart);
            int lineEnd = root.getElementIndex(selectionEnd);

            try {

                for (int i = lineStart; i <= lineEnd; i++) {

                    Element line = root.getElement(i);
                    int offset = line.getStartOffset();

                    if (comment) {
                        doc.insertString(offset, COMMENT, null);
                        continue;
                    }

                    //if we are here it means we want uncomment
                    if (doc.getText(offset, 2).equals(COMMENT)) {
                        doc.remove(offset, 2);
                    }

                }


            } catch (BadLocationException ble) {
                System.out.println("never happens");
            }

        }

    }

}
