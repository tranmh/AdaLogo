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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;

/**
 * file handler, this class provides actions and methods
 * to save and load files to a textpane.
 */
public abstract class FileHandler implements DocumentListener {

    private Document doc;
    private EditorKit kit;

    /**
     * constructor
     */
    public FileHandler(JTextPane textPane) {

        this.doc = textPane.getDocument();
        this.kit = textPane.getEditorKit();

        //listen for document changes
        doc.addDocumentListener(this);

        currentFileDate = new Date();

        //timer to track file on disk
        Timer timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkFileOnDisk();
            }
        });
        timer.start();

    }

    //TODO write comments

    protected abstract File getNewDocumentFile();
    protected abstract InputStream getNewDocumentStream();

    protected abstract boolean okToLoadNewDocument();
    protected abstract boolean okToOpenDocument();
    protected abstract boolean okToRevertDocument(String name);
    protected abstract boolean okToReloadFileFromDisk(String name);
    protected abstract boolean okToOverwriteFile(String name);

    protected abstract void errorFileDoesNotExist(String name);
    protected abstract void errorCannotReadFromFile(String name);
    protected abstract void errorCannotCreateFile(String name);
    protected abstract void errorCannotWriteToFile(String name);

    protected abstract FileChooser getFileChooser();

    protected interface FileChooser {
        public boolean selectedFileToOpen();
        public boolean selectedFileToSave();
        public File getSelectedFile();
    }

    //-------------------------------------------------------------------------
    //flags

    /**
     * the currently loaded file.
     * this will be null if load from stream.
     */
    protected File currentFile;

    /**
     * the filename of the currently loaded file.
     * this will always have a value even if load from stream.
     */
    protected String currentFileName;

    /**
     * the date of the current file loaded.
     * this will be used to check the loaded file against the file on disk.
     */
    protected Date currentFileDate;

    /**
     * true if the document is changed after last save.
     */
    protected boolean currentFileChanged = false;

    /**
     * if this is true no change event will be fired.
     * cheap hack to prevent title updating
     * during save load operation.
     */
    protected boolean dontFireChange = false;

    /**
     * if this is true no check file on disk will be done.
     * this is dirty hack to prevent check file on disk to occur on
     * less favourable events (for example when save/load dialog is open).
     */
    protected boolean dontCheckFileOnDisk = false;

    //-------------------------------------------------------------------------
    //save load UI stuff

    /**
     * load a new document in the text component.
     *
     * @see #getNewDocumentFile()
     * @see #getNewDocumentStream()
     */
    public void newDocument() {
        //System.out.println("FileHandler.newDocument()");

        if (!okToLoadNewDocument()) return;

        File file = getNewDocumentFile();
        if (file != null) {
            loadFromFile(file);
            return;
        }

        InputStream stream = getNewDocumentStream();
        if (stream != null) {
            loadFromStream(stream, "Untitled");
            return;
        }

        throw new RuntimeException("no new document template given");

    }

    /**
     * let user open a new document.
     * called from action.
     *
     * @see #okToOpenDocument()
     * @see #getFileChooser()
     * @see FileHandler.FileChooser
     * @see #errorFileDoesNotExist(String)
     * @see #errorCannotReadFromFile(String)
     */
    public void openDocument() {
        //System.out.println("FileHandler.openDocument()");

        if (!okToOpenDocument()) return;

        FileChooser fc = getFileChooser();

        if (!fc.selectedFileToOpen()) return;

        File file = fc.getSelectedFile();

        if (!file.exists()) {
            errorFileDoesNotExist(file.getName());
            return;
        }

        if (!file.canRead()) {
            errorCannotReadFromFile(file.getName());
            return;
        }

        loadFromFile(file);

    }

    /**
     * this method might never be called, but it is here anyway
     */
    public void openDocument(File file) {
        //System.out.println("FileHandler.openDocument(File)");
        if (okToOpenDocument())
            loadFromFile(file);
    }

    /**
     * this method is called to load examples from the jar as stream
     */
    public void openDocument(InputStream in, String name) {
        //System.out.println("FileHandler.openDocument(Stream)");
        if (okToOpenDocument())
            loadFromStream(in, name);
    }

    /**
     * let user save document to file.
     * called from action.
     *
     * @see #getFileChooser()
     * @see FileHandler.FileChooser
     * @see #okToOverwriteFile(String)
     * @see #errorCannotCreateFile(String)
     * @see #errorCannotWriteToFile(String)
     */
    public void saveAsDocument() {
        //System.out.println("FileHandler.saveAsDocument()");

        FileChooser fc = getFileChooser();

        if (!fc.selectedFileToSave()) return;

        File file = fc.getSelectedFile();

        try {
            //true if file created
            //false if file exists
            //exception if cannot create
            if (file.createNewFile()) {
                //continue saving file
            } else {
                if (!okToOverwriteFile(file.getName())) return;
                //else continue saving file
            }
        } catch (IOException e) {
            errorCannotCreateFile(file.getName());
            return;
        }

        if (!file.canWrite()) {
            errorCannotWriteToFile(file.getName());
            return;
        }

        saveAsDocument(file);

    }

    /**
     * this method might never be called, but it is here anyway
     */
    public void saveAsDocument(File file) {
        //System.out.println("FileHandler.saveAsDocument(File)");
        saveToFile(file);
    }

    /**
     * this method might never be called, but it is here anyway
     */
    public void saveAsDocument(OutputStream out, String name) {
        //System.out.println("FileHandler.saveAsDocument(Stream)");
        saveToStream(out, name);
    }

    /**
     * save the current loaded file.
     */
    public void saveDocument() {
        //System.out.println("FileHandler.saveDocument()");
        saveCurrentFile();
    }

    /**
     * revert the current loaded file with the file on disk.
     */
    public void revertDocument() {
        //System.out.println("FileHandler.revertDocument()");
        if (okToRevertDocument(currentFileName))
            revertCurrentFile();
    }

    /**
     * this will check the currently loaded file against
     * the file on disk, and warn the user if file on disk has changed.
     * this will be called from the timer started in constructor.
     *
     */
    public void checkFileOnDisk() {

        //dirty hack
        if (dontCheckFileOnDisk) return;

        //nothing to do if no file loaded
        if (currentFile == null) return;

        //TODO what do when file deleted?
        if (!currentFile.exists()) return;

        //nothing to do if date same
        if (currentFile.lastModified() == currentFileDate.getTime()) return;

        if (okToReloadFileFromDisk(currentFileName)) {
            loadFromFile(currentFile);
        } else {
            currentFileDate.setTime(currentFile.lastModified());
        }

    }

    //-------------------------------------------------------------------------
    //internal save load methods

    /**
     * load document from stream.
     * the stream is necessary because files in jar are only
     * accessible as stream, and if run as applet access to file objects
     * are not allowed.
     *
     * @see loadFromFile()
     */
    private void loadFromStream(InputStream in, String name) {
        //System.out.println("FileHandler.loadFromStream()");
        try {

            dontFireChange = true;
            doc.remove(0, doc.getLength());
            kit.read(in, doc, 0);
            dontFireChange = false;

            currentFile = null;
            currentFileName = name;
            currentFileChanged = false;

            fireDocumentLoaded();

        } catch (Exception e) {
            e.printStackTrace();
            fireDocumentLoadFailed();
        }
    }

    /**
     * load document from file.
     *
     * @see loadFromStream()
     */
    private void loadFromFile(File file) {
        //System.out.println("FileHandler.loadFromFile()");
        try {

            InputStream in = new FileInputStream(file);
            loadFromStream(in, file.getName());
            in.close();

            currentFile = file;
            currentFileDate.setTime(file.lastModified());

        } catch (Exception e) {
            e.printStackTrace();
            fireDocumentLoadFailed();
        }
    }

    /**
     * save document to stream.
     * the stream was created from a file from the save file dialog.
     *
     * @see saveToFile()
     */
    private void saveToStream(OutputStream out, String name) {
        //System.out.println("FileHandler.saveToStream()");
        try {

            dontFireChange = true;
            kit.write(out, doc, 0, doc.getLength());
            dontFireChange = false;

            currentFile = null;
            currentFileName = name;
            currentFileChanged = false;

            fireDocumentSaved();

        } catch (Exception e) {
            e.printStackTrace();
            fireDocumentSaveFailed();
        }
    }

    /**
     * save to file.
     *
     * @see saveToStream()
     */
    private void saveToFile(File file) {
        //System.out.println("FileHandler.saveToFile()");
        try {
            OutputStream out = new FileOutputStream(file);
            saveToStream(out, file.getName());
            out.close();
            currentFile = file;
            currentFileDate.setTime(file.lastModified());
        } catch (Exception e) {
            e.printStackTrace();
            fireDocumentSaveFailed();
        }
    }

    /**
     * save currently loaded file.
     * this should only be called if currentfile != null
     * and if file access is allowed.
     */
    private void saveCurrentFile() {
        //System.out.println("FileHandler.saveCurrentFile()");

        //this is just precaution
        if (currentFile == null) {
            throw new RuntimeException(
                    "currentFile == null! this should never happen!");
        }

        saveToFile(currentFile);
    }

    /**
     * revert document to file on disk.
     * this should only be called if currentfile != null
     * and if file access is allowed.
     */
    private void revertCurrentFile() {
        //System.out.println("FileHandler.revertCurrentFile()");

        //this is just precaution
        if (currentFile == null) {
            throw new RuntimeException(
                    "currentFile == null! this should never happen!");
        }

        loadFromFile(currentFile);

    }

    //-------------------------------------------------------------------------
    //document listener

    public void insertUpdate(DocumentEvent e) {
        currentFileChanged = true;
        fireDocumentChanged();
    }
    public void removeUpdate(DocumentEvent e) {
        currentFileChanged = true;
        fireDocumentChanged();
    }
    public void changedUpdate(DocumentEvent e) {
        currentFileChanged = true;
        fireDocumentChanged();
    }

    //-------------------------------------------------------------------------
    //event listener architecture

    private Set listeners = new HashSet();

    public interface FileHandlerListener extends EventListener {
        public void documentChanged(FileHandlerEvent e);
        public void documentLoaded(FileHandlerEvent e);
        public void documentLoadFailed(FileHandlerEvent e);
        public void documentSaved(FileHandlerEvent e);
        public void documentSaveFailed(FileHandlerEvent e);
    }

    public class FileHandlerEvent extends EventObject {
        public FileHandlerEvent(Object source) {
            super(source);
        }
        public File getCurrentFile() {
            return currentFile;
        }
        public String getCurrentFileName() {
            return currentFileName;
        }
        public boolean isCurrentFileChanged() {
            return currentFileChanged;
        }

    }

    public void addFileHandlerListener(FileHandlerListener listener) {
        listeners.add(listener);
    }

    private void fireDocumentChanged() {
        //dont fire change when change because of loading
        if (dontFireChange) return;
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            FileHandlerListener l = (FileHandlerListener)i.next();
            l.documentChanged(new FileHandlerEvent(this));
        }
    }

    private void fireDocumentLoaded() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            FileHandlerListener l = (FileHandlerListener)i.next();
            l.documentLoaded(new FileHandlerEvent(this));
        }
    }

    private void fireDocumentLoadFailed() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            FileHandlerListener l = (FileHandlerListener)i.next();
            l.documentLoadFailed(new FileHandlerEvent(this));
        }
    }

    private void fireDocumentSaved() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            FileHandlerListener l = (FileHandlerListener)i.next();
            l.documentSaved(new FileHandlerEvent(this));
        }
    }

    private void fireDocumentSaveFailed() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            FileHandlerListener l = (FileHandlerListener)i.next();
            l.documentSaveFailed(new FileHandlerEvent(this));
        }
    }

    //-------------------------------------------------------------------------
    //actions

    private class NewAction extends AbstractAction {
        public NewAction() {
            super("New");
            putValue(Action.SHORT_DESCRIPTION, "create new empty document");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl N"));
        }
        public void actionPerformed(ActionEvent e) {
            //System.out.println("NewAction.actionPerformed()");
            newDocument();
        }
    }
    private class OpenAction extends AbstractAction {
        public OpenAction() {
            super("Open...");
            putValue(Action.SHORT_DESCRIPTION, "open file from disk");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl O"));
        }
        public void actionPerformed(ActionEvent e) {
            //System.out.println("OpenAction.actionPerformed()");
            openDocument();
        }
    }
    private class SaveAction extends AbstractAction {
        public SaveAction() {
            super("Save");
            putValue(Action.SHORT_DESCRIPTION, "save file to disk");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl S"));
        }
        public void actionPerformed(ActionEvent e) {
            //System.out.println("SaveAction.actionPerformed()");
            saveDocument();
        }
    }
    private class SaveAsAction extends AbstractAction {
        public SaveAsAction() {
            super("Save As...");
            putValue(Action.SHORT_DESCRIPTION, "save to new file");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl shift S"));
        }
        public void actionPerformed(ActionEvent e) {
            //System.out.println("SaveAsAction.actionPerformed()");
            saveAsDocument();
        }
    }
    private class RevertAction extends AbstractAction {
        public RevertAction() {
            super("Revert");
            putValue(Action.SHORT_DESCRIPTION, "revert to saved version");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F5"));
        }
        public void actionPerformed(ActionEvent e) {
            //System.out.println("RevertAction.actionPerformed()");
            revertDocument();
        }
    }

    public Action newAction = new NewAction();
    public Action openAction = new OpenAction();
    public Action saveAction = new SaveAction();
    public Action saveAsAction = new SaveAsAction();
    public Action revertAction = new RevertAction();

}
