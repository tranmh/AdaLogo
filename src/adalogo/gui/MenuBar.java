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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import adalogo.Engine;
import adalogo.Settings;
import adalogo.Turtle;
import adalogo.gui.editor.Editor;
import adalogo.gui.varmonitor.VarMonitor;
import adalogo.gui.varmonitor.VarMonitorModel.VarNodes;
import adalogo.visitor.VisitorMaster;

public class MenuBar extends JMenuBar {

    private Engine engine;
    private Turtle turtle;
    private Editor editor;
    private TurtleCanvas canvas;
    private StatusBar statusBar;
    private WindowFrame window;
    private VarMonitor variableMonitor;
    private VisitorMaster visitor;


    public MenuBar(Engine engine) {
        this.engine = engine;
    }

    /**
     * init, extended constructor.
     * called by engine.
     */
    public void init() {
        this.turtle = engine.getTurtle();
        this.editor = engine.getEditor();
        this.canvas = engine.getCanvas();
        this.statusBar = engine.getStatusBar();
        this.window = engine.getWindow();
        this.variableMonitor=engine.getVarMonitor();
        this.visitor = engine.getVisitor();

        add(createFileMenu());
        add(createEditMenu());
        add(createExamplesMenu());
        add(createRunMenu());
        add(createTurtleMenu());
        add(createCanvasMenu());
        add(createOptionsMenu());
        add(createHelpMenu());
    }

    //-------------------------------------------------------------------------
    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(new JMenuItem(editor.getNewAction()));
        fileMenu.add(new JMenuItem(editor.getOpenAction()));
        fileMenu.add(new JMenuItem(editor.getSaveAction()));
        fileMenu.add(new JMenuItem(editor.getSaveAsAction()));
        fileMenu.add(new JMenuItem(editor.getRevertAction()));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(engine.getExitAction()));
        return fileMenu;
    }

    //-------------------------------------------------------------------------
    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.add(new JMenuItem(editor.getUndoAction()));
        editMenu.add(new JMenuItem(editor.getRedoAction()));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(editor.getCutAction()));
        editMenu.add(new JMenuItem(editor.getCopyAction()));
        editMenu.add(new JMenuItem(editor.getPasteAction()));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(editor.getCommentAction()));
        editMenu.add(new JMenuItem(editor.getUncommentAction()));
        return editMenu;
    }

    //-------------------------------------------------------------------------
    private JMenu createExamplesMenu() {
        JMenu examplesMenu = new JMenu("Examples");
        examplesMenu.setMnemonic(KeyEvent.VK_X);
        Action[] action = editor.getExampleActions();
        JMenuItem menu[] = new JMenuItem[action.length];
        for (int i = 0; i < action.length; i++) {
            menu[i] = new JMenuItem(action[i]);
            examplesMenu.add(menu[i]);
        }
        return examplesMenu;
    }

    //-------------------------------------------------------------------------
    private JMenu createRunMenu() {
        JMenu m = new JMenu("Run");
        m.setMnemonic(KeyEvent.VK_R);
        m.add(new JMenuItem(visitor.getStartAction()));
        m.add(new JMenuItem(visitor.getStartDebugAction()));
        m.add(new JMenuItem(visitor.getSingleStepAction()));
        m.add(new JMenuItem(visitor.getMultiStepAction()));
        m.add(new JMenuItem(visitor.getStopAction()));
        return m;
    }

    //-------------------------------------------------------------------------
    private JMenu createTurtleMenu() {
        JMenu m = new JMenu("Turtle");
        m.setMnemonic(KeyEvent.VK_T);
        m.add(new JMenuItem(turtle.getForwardAction()));
        m.add(new JMenuItem(turtle.getTurnLeftAction()));
        m.add(new JMenuItem(turtle.getTurnRightAction()));
        m.add(new JMenuItem(turtle.getPenDownAction()));
        m.add(new JMenuItem(turtle.getPenUpAction()));
        m.add(new JMenuItem(turtle.getResetAction()));
        return m;
    }

    //-------------------------------------------------------------------------
    private JMenu createCanvasMenu() {
        JMenu m = new JMenu("Canvas");
        m.setMnemonic(KeyEvent.VK_C);
        m.add(new JMenuItem(canvas.getZoomInAction()));
        m.add(new JMenuItem(canvas.getZoomOutAction()));
        m.add(new JMenuItem(canvas.getResetAction()));
        return m;
    }

    //-------------------------------------------------------------------------
    private JMenu createOptionsMenu() {
        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O);
        //optionsMenu.add(createLookAndFeelSubMenu());
        //TODOlater fix this
        //optionsMenu.add(createFontSizeSubMenu());
        optionsMenu.addSeparator();
        optionsMenu.add(createTurtleFollowSubMenu());
        optionsMenu.add(createAntiAliasMenuItem());
        optionsMenu.add(createRenderQualityMenuItem());
        optionsMenu.addSeparator();
        optionsMenu.add(createPrintSyntaxTreeMenuItem());
        optionsMenu.add(createVarMonSubMenu());
        return optionsMenu;
    }

    private JMenu createLookAndFeelSubMenu() {
        JMenu lookAndFeelSubMenu = new JMenu("Look and Feel");
        Action[] a = window.getLAFActions();
        JRadioButtonMenuItem m[] = new JRadioButtonMenuItem[a.length];
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < a.length; i++) {
            m[i] = new JRadioButtonMenuItem(a[i]);
            lookAndFeelSubMenu.add(m[i]);
            group.add(m[i]);
        }
        m[0].setSelected(true);
        return lookAndFeelSubMenu;
    }

    private JMenu createFontSizeSubMenu() {
        JMenu fontSizeSubMenu = new JMenu("Font Size");
        Action[] a = editor.getFontSizeActions();
        JRadioButtonMenuItem m[] = new JRadioButtonMenuItem[a.length];
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < a.length; i++) {
            m[i] = new JRadioButtonMenuItem(a[i]);
            fontSizeSubMenu.add(m[i]);
            group.add(m[i]);
        }
        m[0].setSelected(true);
        return fontSizeSubMenu;
    }

    private JMenu createTurtleFollowSubMenu() {
        JMenu menu = new JMenu("Follow turtle");

        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem mi0 =
            new JRadioButtonMenuItem("never");
        JRadioButtonMenuItem mi1 =
            new JRadioButtonMenuItem("on edge");
        JRadioButtonMenuItem mi2 =
            new JRadioButtonMenuItem("always");

        mi0.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Settings.setTurtleFollowMode(
                        Settings.TURTLE_FOLLOW_MODE_NEVER);
            }
        });
        mi1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Settings.setTurtleFollowMode(
                        Settings.TURTLE_FOLLOW_MODE_EDGE);
            }
        });
        mi2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Settings.setTurtleFollowMode(
                        Settings.TURTLE_FOLLOW_MODE_ALWAYS);
            }
        });

        mi1.setSelected(true);

        group.add(mi0);
        group.add(mi1);
        group.add(mi2);

        menu.add(mi0);
        menu.add(mi1);
        menu.add(mi2);

        return menu;
    }

    private JMenu createVarMonSubMenu() {
        JMenu varmonmenu = new JMenu("Variable Monitor");
        varmonmenu.add(createinvisibleVariablesSubMenu());
        varmonmenu.add(createNoTreeMenuItem());
        varmonmenu.add(createShowRootMenuItem());
        varmonmenu.add(createExpandedByDefaultMenuItem());
        return varmonmenu;
    }

    private JCheckBoxMenuItem createNoTreeMenuItem() {

        JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem("show as tree");
        jcbmi.setSelected(true);
        jcbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
                JCheckBoxMenuItem showroot=(JCheckBoxMenuItem)((JMenu)((JMenu)MenuBar.this.getComponent(6)).getMenuComponent(3)).getItem(2);
                JCheckBoxMenuItem expand=(JCheckBoxMenuItem)((JMenu)((JMenu)MenuBar.this.getComponent(6)).getMenuComponent(3)).getItem(3);
                JMenu nonvisible=(JMenu)((JMenu)((JMenu)MenuBar.this.getComponent(6)).getMenuComponent(3)).getMenuComponent(0);


                if (cb.isSelected()) {
                    Settings.setShowAsTree(true);

                    nonvisible.setEnabled(true);
                    showroot.setEnabled(true);
                    expand.setEnabled(true);

                }
                else {

                    Settings.setShowAsTree(false);
                    ((JRadioButtonMenuItem)nonvisible.getItem(0)).setSelected(true);
                    Settings.setInvisibleNodeModeShow();
                    ((VarNodes)variableMonitor.getModel().getRoot()).showVariables();
                    nonvisible.setEnabled(false);
                    showroot.setState(false);
                    showroot.setEnabled(false);
                    expand.setState(true);
                    expand.setEnabled(false);



                }
                if (variableMonitor.monitoring==false) return;
                variableMonitor.setShowAsTree();
                variableMonitor.updateTree();

            }
        });
        return jcbmi;
    }

    private JCheckBoxMenuItem createShowRootMenuItem() {
        JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem("show root");
        jcbmi.setSelected(false);

        jcbmi.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
                Settings.setShowroot(cb.isSelected());
                if (variableMonitor.monitoring==false) return;
                variableMonitor.setRootVisible();
                variableMonitor.updateTree();
            }



        });


        return jcbmi;
    }

    private JCheckBoxMenuItem createExpandedByDefaultMenuItem() {
        JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem("expanded by default");
        jcbmi.setSelected(true);

        jcbmi.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
                Settings.setExpandedbydefault(cb.isSelected());

                if (variableMonitor.monitoring==false) return;
                if (cb.isSelected()){
                    variableMonitor.expandAll();
                    variableMonitor.updateTree();
                }
            }

        });

        return jcbmi;
    }

    private JMenu createinvisibleVariablesSubMenu() {
        JMenu menu = new JMenu("Nonvisible Variables");
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem mi0 =
            new JRadioButtonMenuItem("show");
        JRadioButtonMenuItem mi1 =
            new JRadioButtonMenuItem("in extra node");
        JRadioButtonMenuItem mi2 =
            new JRadioButtonMenuItem("hide");


        mi0.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JRadioButtonMenuItem rb = (JRadioButtonMenuItem)e.getSource();
                if (!rb.isSelected()) return;
                Settings.setInvisibleNodeModeShow();
                 if (variableMonitor.monitoring==false) return;
                  ((VarNodes)variableMonitor.getModel().getRoot()).showVariables();
                  variableMonitor.updateTree();
            }
        });

        mi1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JRadioButtonMenuItem rb = (JRadioButtonMenuItem)e.getSource();
                if (!rb.isSelected()) return;
                Settings.setInvisibleNodeModeInExtraNode();
                 if (variableMonitor.monitoring==false) return;
                  ((VarNodes)variableMonitor.getModel().getRoot()).hideVariables();
                  variableMonitor.updateTree();
            }
        });

        mi2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JRadioButtonMenuItem rb = (JRadioButtonMenuItem)e.getSource();
                if (!rb.isSelected()) return;
                Settings.setInvisibleNodeModeHide();
                 if (variableMonitor.monitoring==false) return;
                  ((VarNodes)variableMonitor.getModel().getRoot()).hideVariables();
                  variableMonitor.updateTree();
            }
        });


        /*
        mi0.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Settings.setInvisibleNodeModeShow();
                if (variableMonitor.monitoring==false) return;
                ((VarNodes)variableMonitor.getModel().getRoot()).showVariables();
                variableMonitor.updateTree();
            }
        });
        mi1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Settings.setInvisibleNodeModeInExtraNode();
                if (variableMonitor.monitoring==false) return;
                ((VarNodes)variableMonitor.getModel().getRoot()).hideVariables();
                variableMonitor.updateTree();
            }
        });
        mi2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Settings.setInvisibleNodeModeHide();
                if (variableMonitor.monitoring==false) return;
                ((VarNodes)variableMonitor.getModel().getRoot()).hideVariables();
                variableMonitor.updateTree();
            }
        });
        */

        mi0.setSelected(true);
        group.add(mi0);
        group.add(mi1);
        group.add(mi2);
        menu.add(mi0);
        menu.add(mi1);
        menu.add(mi2);
        return menu;
    }


    private JCheckBoxMenuItem createAntiAliasMenuItem() {
        JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem("Use Anti Aliasing");
        jcbmi.setSelected(true);
        jcbmi.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Settings.setAntiAlias(true);
                } else {
                    Settings.setAntiAlias(false);
                }
                canvas.repaint();
            }
        });
        return jcbmi;
    }

    private JCheckBoxMenuItem createRenderQualityMenuItem() {
        JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem("High Render Quality");
        jcbmi.setSelected(true);
        jcbmi.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Settings.setRenderQuality(true);
                } else {
                    Settings.setRenderQuality(false);
                }
                canvas.repaint();
            }
        });
        return jcbmi;
    }

    private JCheckBoxMenuItem createPrintSyntaxTreeMenuItem() {
        JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem("Print Syntax Tree");
        jcbmi.setSelected(false);
        jcbmi.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Settings.setPrintSyntaxTree(true);
                } else {
                    Settings.setPrintSyntaxTree(false);
                }
            }
        });
        return jcbmi;
    }

    //-------------------------------------------------------------------------
    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.add(createHelpMenuItem());
        helpMenu.add(createAboutMenuItem());
        return helpMenu;
    }

    //TODOlater write help
    private JMenuItem createHelpMenuItem() {
        JMenuItem help = new JMenuItem("Help");
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(window,
                        "\"Help me Obi-Wan Kenobi; you're my only hope!\" - Princess Leia\n" +
                        "Sorry. Help has not been written yet.\n" +
                        "Please visit http://adalogo.cuong.net");
            }
        });
        return help;
    }

    //TODOlater write about
    private JMenuItem createAboutMenuItem() {
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(window,
                        "AdaLogo version: @BUILDTIME@");
            }
        });
        return about;
    }

}
