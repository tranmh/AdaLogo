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

//search for string and replace to switch mode
/*DebugOff
/*DebugOff

/*EndOfDebug*/

package adalogo.gui.varmonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;

import adalogo.Engine;
import adalogo.Settings;
import adalogo.gui.varmonitor.VarMonitorModel.VarNodes;
import adalogo.visitor.ProgramCounter;
import adalogo.visitor.SymbolTable;
import adalogo.visitor.ProgramCounter.ProgramCounterListener;
import adalogo.visitor.VisitorMaster.VisitorEvent;
import adalogo.visitor.VisitorMaster.VisitorListener;

public class VarMonitor
extends JPanel
implements  ProgramCounterListener,ChangeListener,TreeExpansionListener,VisitorListener{

    static Engine engine;
    public JTreeTable         treeTable;
    private VarMonitorModel    model;
    public VarMonitorModel testmodel;
    public SymbolTable symboltable;
    public boolean monitoring=false;
    private int lastwaitat;
    static Color changecolor=Color.red.darker();
    static Color invisiblecolor=Color.lightGray;
    static Color standardcolor=Color.black;



    ProgramCounter programcounter;
    JSpinner variablechanges;

    URL bunselectedurl = this.getClass().getResource("/images/treenodessymbols/b.gif");
    URL bselectedurl = this.getClass().getResource("/images/treenodessymbols/b-selected.gif");
    URL iunselectedurl = this.getClass().getResource("/images/treenodessymbols/i.gif");
    URL iselectedurl = this.getClass().getResource("/images/treenodessymbols/i-selected.gif");
    URL punselectedurl = this.getClass().getResource("/images/treenodessymbols/p.gif");
    URL pselectedurl = this.getClass().getResource("/images/treenodessymbols/p-selected.gif");
    URL tableurl = this.getClass().getResource("/images/treenodessymbols/table.gif");
    URL invisibleopenedurl = this.getClass().getResource("/images/treenodessymbols/invisibleopened.gif");
    URL invisibleclosedurl = this.getClass().getResource("/images/treenodessymbols/invisibleclosed.gif");
    URL invisibleiconurl = this.getClass().getResource("/images/treenodessymbols/invisibleicon.gif");

    ImageIcon bunselected = new ImageIcon(bunselectedurl);
    ImageIcon bselected = new ImageIcon(bselectedurl);
    ImageIcon iunselected = new ImageIcon(iunselectedurl);
    ImageIcon iselected = new ImageIcon(iselectedurl);
    ImageIcon punselected = new ImageIcon(punselectedurl);
    ImageIcon pselected = new ImageIcon(pselectedurl);
    ImageIcon table = new ImageIcon(tableurl);
    ImageIcon invisibleopened = new ImageIcon(invisibleopenedurl);
    ImageIcon invisibleclosed = new ImageIcon(invisibleclosedurl);
    ImageIcon invisibleicon = new ImageIcon(invisibleiconurl);
    ImageIcon block=null;




    public VarMonitor(Engine en) {
        engine = en;
        /*DebugOff
        programcounter = new ProgramCounter();
        createTestVarMonitor();
        programcounter.addProgramCounterListener(this);
        /*EndOfDebug*/

    }

    public void beginMonitoring(SymbolTable st, ProgramCounter pc){

        monitoring=true;
        lastwaitat=0;
        engine.getVisitor().addVisitorListener(this);
        this.symboltable=st;

           this.programcounter=pc;

        model = new VarMonitorModel(symboltable, this, pc);

        treeTable = createTreeTable();
        treeTable.tree.setRootVisible(Settings.isShowroot());
        treeTable.tree.addTreeExpansionListener(this);
        programcounter=pc;
        programcounter.addProgramCounterListener(this);
        setLayout(new BorderLayout());
        this.removeAll();
        add(new JScrollPane(treeTable), BorderLayout.CENTER);

        SpinnerModel model = new SpinnerNumberModel(1,0,1,1);
        variablechanges= new JSpinner(model);
        variablechanges.addChangeListener(this);

        JPanel variablechangesPanel=new JPanel();
        JLabel label=new JLabel("changes within");
        label.setLabelFor(variablechanges);
        variablechangesPanel.add(label);

        variablechangesPanel.add(variablechanges);

        variablechangesPanel.add(new JLabel("steps"));
        add(variablechangesPanel, BorderLayout.SOUTH);

        JComponent editor = variablechanges.getEditor();

        if (editor instanceof DefaultEditor) {
               ((DefaultEditor)editor).getTextField().setValue(variablechanges.getValue());

        if (((DefaultEditor)editor) != null ) {
            ((DefaultEditor)editor).getTextField().setColumns(8); //specify more width than we need
            ((DefaultEditor)editor).getTextField().setHorizontalAlignment(JTextField.RIGHT);
        }}


        revalidate();
        System.out.println("beginMointoring");
    }

    public void endMonitoring(){
        model.modelUpdate();
        monitoring=false;

    }

    public ProgramCounter getProgramCounter(){
        return programcounter;
    }


    private JTreeTable createTreeTable() {
        treeTable= new JTreeTable(model);

           MyTreeCellRenderer renderer= new MyTreeCellRenderer() ;

           treeTable.tree.setCellRenderer(renderer);

           DefaultTableCellRenderer tablerenderer= new MyDefaultTableCellRenderer() ;
           treeTable.getColumnModel().getColumn(1).setCellRenderer(tablerenderer);
           treeTable.getColumnModel().getColumn(2).setCellRenderer(tablerenderer);

           treeTable.getColumnModel().getColumn(0).setPreferredWidth(100);
           treeTable.getColumnModel().getColumn(1).setPreferredWidth(80);
           treeTable.getColumnModel().getColumn(2).setPreferredWidth(75);
           treeTable.getColumnModel().getColumn(1).setMaxWidth(80);
           treeTable.getColumnModel().getColumn(2).setMaxWidth(75);



           return treeTable;
    }


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
        return new Dimension(250, 0);
    }


    public void counterReset() {
        // TODO Auto-generated method stub

    }
    private boolean treechanged=false;;

    public void treechanged(){
        treechanged=true;
    }


    public void counterIncremented() {
        /*
        System.out.println("Step: "+programcounter.getValue());

        if (treechanged) {
            model.modelUpdate();
            //treeTable.updateUI();
            ((VarNodes)model.getRoot()).expandTree();

        }
        treechanged=false;
        SpinnerModel Spinnermodel=variablechanges.getModel();
        variablechanges.setModel(new SpinnerNumberModel(((Integer)Spinnermodel.getValue()).intValue(),0,programcounter.getValue(),1));

       // System.out.println("end");*/
    }


    public void updateTree() {
        treeTable.updateUI();
    }
    public void stateChanged(ChangeEvent e) {
        JSpinner source=(JSpinner)e.getSource();
        model.changewithinrounds=((Integer)source.getValue()).intValue();
        updateTree();
    }

    private class MyTreeCellRenderer extends DefaultTreeCellRenderer {

           public Component getTreeCellRendererComponent(JTree tree, Object value,
                  boolean sel,
                  boolean expanded,
                  boolean leaf, int row,
                  boolean hasFocus){

               super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);


               VarNodes node=(VarMonitorModel.VarNodes)value;

               //System.out.println("Debug tree"+node.getName());

               /*DebugOff
               System.out.println("Debug { getTreeCellRendererComponent");
               /*DebugEnd*/

               if (node.getName().equalsIgnoreCase(model.block)) return this;

               if (node.hasChanged()){

                   //BUG von JAVA TextNonSelectionColor funktioniert nicht richtig
                   //this.setTextNonSelectionColor(Color.red);
                   //System.out.println("Debug tree"+node.getName());
                   this.setForeground(changecolor);
               }
               else this.setForeground(standardcolor);


               if (node.getName().equals(model.notVisible)) {
                   setIcon(invisibleclosed);
                   if (expanded) setIcon(invisibleopened);
                   /*DebugOff
                      System.out.println("Debug  getTreeCellRendererComponent }");
                      /*DebugEnd*/
                   return this;

                   }
               if (node.getName().equals(model.symtab)) {
                   setIcon(table);
                   /*DebugOff
                      System.out.println("Debug  getTreeCellRendererComponent }");
                      /*DebugEnd*/
                   return this;
                   }
               try {

                   if (symboltable.getType(node.getName())=="block") {
                       setIcon(block);
                       setText(symboltable.getValue(node.getName()));
                       //if (expanded) setIcon(pselected);


                       return this;
                   }

                   if (symboltable.getType(node.getName())=="integer") {

                       if (sel) setIcon(iselected);
                       else setIcon(iunselected);
                       if (node.isVisible()==false){
                           this.setForeground(invisiblecolor);

                           setIcon(invisibleicon);
                       }
                       /*DebugOff
                        System.out.println("Debug  getTreeCellRendererComponent }");
                        /*DebugEnd*/
                       return this;
                   }

                   if (symboltable.getType(node.getName())=="boolean") {

                       if (sel) setIcon(bselected);
                       else setIcon(bunselected);
                       if (node.isVisible()==false){
                           this.setForeground(invisiblecolor);

                           setIcon(invisibleicon);
                       }
                       /*DebugOff
                        System.out.println("Debug  getTreeCellRendererComponent }");
                        /*DebugEnd*/
                       return this;
                   }

                   if (symboltable.getType(node.getName())=="procedure") {

                       if (sel) setIcon(pselected);
                       else setIcon(punselected);

                       /*DebugOff
                        System.out.println("Debug  getTreeCellRendererComponent }");
                        /*DebugEnd*/
                       return this;
                   }



               } catch (Exception e) {
                   e.printStackTrace();
               }

               /*DebugOff
               System.out.println("Debug  getTreeCellRendererComponent }");
               /*DebugEnd*/

               return this;

           }

    }

    private class MyDefaultTableCellRenderer extends DefaultTableCellRenderer	{
           public Component getTableCellRendererComponent(JTable table,
                   Object value,
                   boolean isSelected,
                   boolean hasFocus,
                   int row, int column) {

               VarNodes varnode=(VarNodes)value;
               super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

               this.setForeground(standardcolor);
             if (varnode.hasChanged()){this.setForeground(changecolor);}
               if (varnode.isVisible()==false){
                   this.setForeground(invisiblecolor);
                   setValue(new String("<not visible>"));
                   }
               return this;}
       }


    public VarMonitorModel getModel() {
        return model;
    }

    public void treeExpanded(TreeExpansionEvent event) {

        VarNodes node=((VarNodes)event.getPath().getLastPathComponent());
        node.expanded=true;

    }

    public void treeCollapsed(TreeExpansionEvent event) {
        VarNodes node=((VarNodes)event.getPath().getLastPathComponent());
        node.expanded=false;
    }

    public void setRootVisible() {

        treeTable.tree.setRootVisible(Settings.isShowroot());

    }

    public void expandAll() {
        ((VarMonitorModel.VarNodes)model.getRoot()).expandAll();

    }

    public void setShowAsTree() {
        model.showonlyvariables=!Settings.isShowAsTree();

    }

    public void visitorStarted(VisitorEvent e) {
        // TODO Auto-generated method stub

    }

    public void visitorWaiting(VisitorEvent e) {

        System.out.println("Step: "+programcounter.getValue());

        if (treechanged) {
            model.modelUpdate();
            //treeTable.updateUI();
            ((VarNodes)model.getRoot()).expandTree();
        }
        treechanged=false;
        SpinnerModel Spinnermodel=variablechanges.getModel();
        variablechanges.setModel(new SpinnerNumberModel(programcounter.getValue()-lastwaitat,0,programcounter.getValue(),1));
        lastwaitat=programcounter.getValue();
       // System.out.println("end");

    }

    public void visitorRunning(VisitorEvent e) {
        // TODO Auto-generated method stub

    }

    public void visitorStopped(VisitorEvent e) {
        // TODO Auto-generated method stub

    }



}