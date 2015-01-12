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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;

import adalogo.Engine;
import adalogo.Settings;
import adalogo.visitor.VisitorMaster.VisitorEvent;
import adalogo.visitor.VisitorMaster.VisitorListener;

public class Console extends JPanel implements VisitorListener {

    private Engine engine;

    private JTextPane console;
    private DefaultStyledDocument doc;
    private StyledEditorKit kit;

    private String newline;

    private SimpleAttributeSet normal;
    private SimpleAttributeSet error;
    private SimpleAttributeSet debug;
    private SimpleAttributeSet internal;

    private Color normalColor = Color.BLACK;
    private Color errorColor = new Color(160, 0, 0);
    private Color debugColor = new Color(120, 140, 120);
    private Color internalColor = new Color(0, 180, 0);

    public Console(Engine en) {
        engine = en;

        newline = System.getProperty("line.separator");

        console = new JTextPane();
        console.setFont(Settings.getConsoleFont());
        console.setEditable(false);

        doc = (DefaultStyledDocument) console.getDocument();
        kit = (StyledEditorKit) console.getEditorKit();

        normal = new SimpleAttributeSet();
        StyleConstants.setForeground(normal, normalColor);

        error = new SimpleAttributeSet();
        StyleConstants.setForeground(error, errorColor);
        //StyleConstants.setBold(error, true);

        debug = new SimpleAttributeSet();
        StyleConstants.setForeground(debug, debugColor);
        //StyleConstants.setBold(error, true);

        internal = new SimpleAttributeSet();
        StyleConstants.setForeground(internal, internalColor);

        //TODOlater remove dummy button
        //put in toolbar, menu, whatever
        JButton butt = new JButton("clear");
        butt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                console.setText("");
            }
        });

        setLayout(new BorderLayout());
        add(new JScrollPane(console), BorderLayout.CENTER);
        add(butt, BorderLayout.SOUTH);

    }

    public void init() {
        engine.getVisitor().addVisitorListener(this);
    }

    private void append(String message, AttributeSet attr) {

        try {

            doc.insertString(doc.getLength(), message, attr);

            //scroll to bottom
            console.setCaretPosition(doc.getLength());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("console append error! this should never happen");
        }

    }

    /**
     * put message in console.
     * a newline is appended to the message.
     * this message will have the default style.
     */
    public synchronized void append(String message) {
        append(message + newline, normal);
    }

    /**
     * put message in console without appended newline.
     */
    public synchronized void appendWithoutNewline(String message) {
        append(message, normal);
    }

    /**
     * put message in console.
     * this message will have the error style (currently red).
     */
    public synchronized void appendError(String message) {
        append(message + newline, error);
    }

    /**
     * put message in console.
     * this message will have the debug style (currently grey).
     */
    public synchronized void appendDebug(String message) {
        append(message + newline, debug);
    }

    /**
     * put message in console.
     * internal messages are messages from gui components,
     * for example error saving/loading file message.
     */
    public synchronized void appendInternal(String message) {
        append(message + newline, internal);
    }

    //-------------------------------------------------------------------------
    //visitor listener
    //TODO make use of this

    /**
     * this will be true when a visitor is running.
     * when this is true no repainting is done.
     */
    private boolean visitorRunning;

    public void visitorStarted(VisitorEvent e) {
        visitorRunning = true;
    }

    public void visitorWaiting(VisitorEvent e) {
        visitorRunning = false;
    }

    public void visitorRunning(VisitorEvent e) {
        visitorRunning = true;
    }

    public void visitorStopped(VisitorEvent e) {
        visitorRunning = false;
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
        return new Dimension(500, 200);
    }

}
