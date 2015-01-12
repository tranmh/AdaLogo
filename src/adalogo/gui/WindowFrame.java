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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.UIManager.LookAndFeelInfo;

import adalogo.Engine;
import adalogo.gui.editor.Editor;
import adalogo.gui.varmonitor.VarMonitor;

public class WindowFrame extends JFrame {

    private Engine engine;

    private Editor editor;
    private Console console;
    private TurtleCanvas canvas;
    private VarMonitor varMonitor;
    private MenuBar menuBar;
    private ToolBar toolBar;
    private StatusBar statusBar;

    private JSplitPane splitEditor;
    private JSplitPane splitConsole;
    private JSplitPane splitCanvas;

    public WindowFrame(Engine en) {
        super("AdaLogo");

        engine = en;

        //let engine handle closing
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        editor = engine.getEditor();
        console = engine.getConsole();
        canvas = engine.getCanvas();
        varMonitor = engine.getVarMonitor();
        menuBar = engine.getMenuBar();
        toolBar = engine.getToolBar();
        statusBar = engine.getStatusBar();

        splitCanvas = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitCanvas.setBorder(BorderFactory.createEmptyBorder());
        splitCanvas.setRightComponent(varMonitor);
        splitCanvas.setLeftComponent(canvas);

        splitConsole = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitConsole.setBorder(BorderFactory.createEmptyBorder());
        splitConsole.setTopComponent(splitCanvas);
        splitConsole.setBottomComponent(console);

        splitEditor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitEditor.setBorder(BorderFactory.createEmptyBorder());
        splitEditor.setRightComponent(splitConsole);
        splitEditor.setLeftComponent(editor);

        //canvas gets most extra width and height
        splitCanvas.setResizeWeight(0.5);
        splitConsole.setResizeWeight(0.9);
        splitEditor.setResizeWeight(0.5);

        splitCanvas.setOneTouchExpandable(true);
        splitConsole.setOneTouchExpandable(true);
        splitEditor.setOneTouchExpandable(true);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(splitEditor, BorderLayout.CENTER);
        content.add(statusBar, BorderLayout.SOUTH);

        //toolbar needs own panel so it can dock on the borders
        JPanel toolbarpane = new JPanel();
        toolbarpane.setLayout(new BorderLayout());
        toolbarpane.add(content, BorderLayout.CENTER);
        toolbarpane.add(toolBar, BorderLayout.NORTH);

        setContentPane(toolbarpane);
        setJMenuBar(menuBar);

    }

    /**
     * set title.
     * this will be called by editor.
     * @see java.awt.Frame#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        //TODO redesign this
        super.setTitle("AdaLogo "+title);
    }

    //-------------------------------------------------------------------------
    //look and feel action

    private class LookAndFeelAction extends AbstractAction {

        LookAndFeelInfo lafi;

        public LookAndFeelAction(LookAndFeelInfo lafi) {
            super(lafi.getName());
            this.lafi = lafi;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                UIManager.setLookAndFeel(lafi.getClassName());
                SwingUtilities.updateComponentTreeUI(WindowFrame.this);
                WindowFrame.this.pack();
                statusBar.setText("changed look and feel to " + lafi.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
                statusBar.setText("error changing look and feel to "
                        + lafi.getName());
            }
        }
    }

    public Action[] getLAFActions() {
        LookAndFeelInfo[] lafi = UIManager.getInstalledLookAndFeels();
        Action[] action = new Action[lafi.length];
        for (int i=0; i<lafi.length; i++) {
            action[i] = new LookAndFeelAction(lafi[i]);
        }
        return action;
    }

}
