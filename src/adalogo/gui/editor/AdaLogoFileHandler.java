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

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;

import adalogo.Engine;
import adalogo.Examples;
import adalogo.gui.StatusBar;
import adalogo.gui.WindowFrame;

/**
 * file handler costumized for adalogo.
 */
public class AdaLogoFileHandler extends FileHandler implements WindowFocusListener {

    //TODO write better messages
    //search for "error message" or "message"

    private StatusBar statusBar;
    private WindowFrame window;

    private AdaLogoFileChooser fileChooser;

    public AdaLogoFileHandler(Engine engine, JTextPane textPane) {
        super(textPane);

        this.statusBar = engine.getStatusBar();
        this.window = engine.getWindow();

        this.window.addWindowFocusListener(this);

        fileChooser = new AdaLogoFileChooser();

    }

    /**
     * return true if it is ok to close.
     * this method will check for unsaved changes.
     */
    public boolean okToClose() {
        return okToLoadNewDocument();
    }

    //-------------------------------------------------------------------------

    public void windowGainedFocus(WindowEvent e) {
        dontCheckFileOnDisk = false;
    }

    public void windowLostFocus(WindowEvent e) {
        dontCheckFileOnDisk = true;
    }

    //-------------------------------------------------------------------------

    protected File getNewDocumentFile() {
        return null;
    }

    protected InputStream getNewDocumentStream() {
        return Examples.getTemplate();
    }

    protected boolean okToLoadNewDocument() {

        if (!currentFileChanged) return true;

        //message
        int opt = JOptionPane.showConfirmDialog(window,
                    "you have unsaved changes in the document\n" +
                    "discard all changes and continue?",
                    "hold yer horses!",
                    JOptionPane.OK_CANCEL_OPTION);

        switch (opt) {
        case JOptionPane.OK_OPTION:
            System.out.println("FileHandler.userConfirmed()");
            return true;
        case JOptionPane.CANCEL_OPTION:
        case JOptionPane.CLOSED_OPTION:
            System.out.println("FileHandler.userConfirmed() NOT");
            return false;
        default:
            //error message
            statusBar.setText("unexpected error");
            JOptionPane.showMessageDialog(window,
                    "unexpected error.\n" +
                    "it is recommended that you save your work\n" +
                    "and restart the application");
            return false;
        }

    }

    protected boolean okToOpenDocument() {
        //delegate
        return okToLoadNewDocument();
    }

    protected boolean okToRevertDocument(String name) {
        //delegate
        return okToLoadNewDocument();
    }

    protected boolean okToReloadFileFromDisk(String name) {

        //message
        int opt = JOptionPane.showConfirmDialog(window,
                "file "+name+" has changed on disk.\n" +
                "do you want to reload the file from disk?\n" +
                "your changes here will be lost.",
                "adalogo dialog",
                JOptionPane.OK_CANCEL_OPTION);

        switch (opt) {
        case JOptionPane.OK_OPTION:
            System.out.println("FileHandler.checkFileOnDisk() YES");
            return true;
        case JOptionPane.CANCEL_OPTION:
        case JOptionPane.CLOSED_OPTION:
            System.out.println("FileHandler.checkFileOnDisk() NO");
            return false;
        default:
            //error message
            statusBar.setText("unexpected error");
            JOptionPane.showMessageDialog(window,
                    "unexpected error.\n" +
                    "it is recommended that you save your work\n" +
                    "and restart the application");
            return false;
        }

    }

    protected boolean okToOverwriteFile(String name) {

        //message
        int option = JOptionPane.showConfirmDialog(window,
                "the file "+name+" already exists\n" +
                "do you want to overwrite?",
                "hold yer horses!",
                JOptionPane.OK_CANCEL_OPTION);

        switch (option) {

        case JOptionPane.OK_OPTION:
            return true;
        case JOptionPane.CANCEL_OPTION:
        case JOptionPane.CLOSED_OPTION:
            statusBar.setText("save cancelled by user.");
            return false;
        default:
            //error message
            statusBar.setText("unexpected error");
            JOptionPane.showMessageDialog(window,
                    "unexpected error.\n" +
                    "it is recommended that you save your work\n" +
                    "and restart the application");
            return false;
        }

    }

    protected void errorFileDoesNotExist(String name) {
        //error message
        statusBar.setText("error opening file");
        JOptionPane.showMessageDialog(window,
                "error: file \""+name+"\" does not exist");
    }

    protected void errorCannotReadFromFile(String name) {
        //error message
        statusBar.setText("error opening file");
        JOptionPane.showMessageDialog(window,
                "error: cannot read from file \""+name+"\"");
    }

    protected void errorCannotCreateFile(String name) {
        //error message
        statusBar.setText("error: cannot create file");
        JOptionPane.showMessageDialog(window,
                "error: cannot create file \""+name+"\"");
    }

    protected void errorCannotWriteToFile(String name) {
        //error message
        statusBar.setText("error: cannot write to file");
        JOptionPane.showMessageDialog(window,
                "error: cannot write to file \""+name+"\"");
    }

    protected FileChooser getFileChooser() {
        return fileChooser;
    }

    //-------------------------------------------------------------------------

    /**
     * *.adl filter.
     * used by open save dialogs.
     */
    private FileFilter adlFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory() || file.getName().endsWith(".adl");
        }
        public String getDescription() {
            return "AdaLogo files (*.adl)";
        }
    };

    private class AdaLogoFileChooser implements FileChooser {

        private JFileChooser fc;

        public AdaLogoFileChooser() {
            fc = new JFileChooser();
            fc.setFileFilter(adlFilter);
        }

        public boolean selectedFileToOpen() {
            int opt = fc.showOpenDialog(window);

            switch (opt) {
            case JFileChooser.APPROVE_OPTION:
                return true;
            case JFileChooser.CANCEL_OPTION:
                statusBar.setText("open cancelled by user.");
                return false;
            case JFileChooser.ERROR_OPTION:
            default:
                //error message
                statusBar.setText("unexpected error");
                JOptionPane.showMessageDialog(window,
                        "unexpected error.\n" +
                        "it is recommended that you save your work\n" +
                        "and restart the application");
                return false;
            }
        }

        public boolean selectedFileToSave() {
            int opt = fc.showSaveDialog(window);

            File file = null;

            switch (opt) {
            case JFileChooser.APPROVE_OPTION:
                file = fc.getSelectedFile();
                break;
            case JFileChooser.CANCEL_OPTION:
                statusBar.setText("save cancelled by user.");
                return false;
            case JFileChooser.ERROR_OPTION:
            default:
                //error message
                statusBar.setText("unexpected error");
                JOptionPane.showMessageDialog(window,
                        "unexpected error.\n" +
                        "it is recommended that you save your work\n" +
                        "and restart the application");
                return false;
            }

            //append .adl if no extension
            if (file.getName().indexOf('.') == -1) {
                file = new File(file.getParent(), file.getName()+".adl");
                System.out.println(file); //DEBUG
                fc.setSelectedFile(file);
            }

            return true;

        }

        public File getSelectedFile() {
            return fc.getSelectedFile();
        }

    }

}
