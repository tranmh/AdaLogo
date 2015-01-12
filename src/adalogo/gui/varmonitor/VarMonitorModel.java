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

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.RepaintManager;
import javax.swing.tree.TreePath;

import adalogo.Settings;
import adalogo.visitor.ProgramCounter;
import adalogo.visitor.SymbolTable;
import adalogo.visitor.SymbolTable.SymbolTableListener;

public class VarMonitorModel
    extends AbstractTreeTableModel
    implements SymbolTableListener{

    private SymbolTable symboltable;
    private VarMonitor vm;
    private VarNodes vroot;

    private static ProgramCounter pc;
    protected  int changewithinrounds=1;
    protected boolean showonlyvariables=false;
    public String notVisible="<non Visibles>";
    public String symtab="<symtab>";
    public String block="block";


    public VarMonitorModel (SymbolTable symboltable, VarMonitor vm, ProgramCounter pc) {
        super(null);
        //root von AbstractTreeTableModel
        this.vm=vm;
        VarMonitorModel.pc=pc; //LESMANA
        this.symboltable= symboltable;
        symboltable.addSymTabChangedListener(this);
        this.root = new VarNodes(symboltable.getIterator());
        vroot = (VarNodes)root;

    }


    // Names of the columns.
    protected String[]  cNames = {"Name", "Value", "Type"};

    // Types of the columns.
    protected Class[]  cTypes = { TreeTableModel.class,
                     String.class, VarNodes.class};

    public int getColumnCount() {
        return cNames.length;
    }

    public String getColumnName(int column) {
        return cNames[column];
    }

    public Class getColumnClass(int column) {
        return cTypes[column];
    }

    /**
     * Returns the value of the particular column for a treenode.
     */
    public Object getValueAt(Object node, int column) {
        VarNodes     varnode = (VarNodes)node;
        VarNodes returnnode= new VarNodes();
        returnnode.changed=varnode.changed;
        returnnode.visible=varnode.visible;
        try {
            switch(column) {
            case 0:
                return node;

            case 1:if (	varnode.getName().equals(notVisible)) {break;}
            if (	varnode.getName().equals(block)) {break;}
            if (
                varnode.getName().equals(symtab) || symboltable.getType(varnode.getName()).equals("procedure")
                || symboltable.getType(varnode.getName()).equals(block))break;

            returnnode.name=symboltable.getValue(varnode.getName());
            return returnnode;
            case 2:
                if (	varnode.getName().equals(block)) {break;}
                if (
                    varnode.getName().equals(symtab) ||varnode.getName().equals(notVisible)){break;
                }
                returnnode.name=symboltable.getType(varnode.getName());
                return returnnode;
            }
            } catch (Exception e) {
                e.printStackTrace();
        }
            returnnode.name="";
        return returnnode;

    }


    /**
     * Returns the number of children of <code>node</code>.
     */
    public int getChildCount(Object node) {
        if (showonlyvariables&&node.equals(vroot)) return getChildCountOnlyVariables(vroot);
        Object[] children = getChildren(node);
        if (Settings.getInvisibleNodeMode().equals(Settings.inextranode)){	return (children == null) ? 0 : children.length;}
        int count=0;
        for (int i = 0; i < children.length; i++) {
            if (!(((VarNodes)children[i]).name.equals(notVisible))) count++;
        }
        return count;
    }


    private int getChildCountOnlyVariables(VarNodes node) {
        int count=0;
        if (node.children.length==0 &&!node.getName().equals(notVisible)){ return 1;}
        for (int i = 0; i < node.children.length; i++) {
            count=count+getChildCountOnlyVariables(node.children[i]);
        }
        return count;
    }

    /**
     * Returns the child of <code>node</code> at index <code>i</code>.
     */
    public Object getChild(Object node, int index) {
        if (showonlyvariables&&node.equals(vroot)) return getChildOnlyVariables(node, index);
        if (getChildren(node) == null) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
          }
        Object[] children = getChildren(node);
        if (Settings.getInvisibleNodeMode().equals(Settings.inextranode)) {
            return (Object)children[index];
        }

        int realIndex    = -1;
        int visibleIndex = -1;

        while (realIndex+1<children.length) {
            if (!(((VarNodes)children[realIndex+1]).name.equals(notVisible))) {
                visibleIndex++;
            }
            realIndex++;
            if (visibleIndex == index) {
                return children[realIndex];
            }
        }
        throw new ArrayIndexOutOfBoundsException("index unmatched");
    }


    private Object getChildOnlyVariables(Object node, int index) {
        int i=0;
        while (index>=getChildCount(vroot.children[i])){
            index=index-getChildCount(vroot.children[i]);
            i++;
        }
        return getChild(vroot.children[i], index);
    }

    protected Object[] getChildren(Object node) {
        VarNodes varnodes = ((VarNodes)node);
        return varnodes.getChildren();
    }


    public void NewVariablePerformed(Object name){
        /*debugOff
        System.out.println("NewVariable");
        /*EndOfDebug*/

        VarNodes newnode =new VarNodes((String)name, ((VarNodes)root).hashMap());
        if (symboltable.getType(name).equals(block)){ newnode.parent.name=(String)name; return;}
        newnode.parent.addChild(newnode); //LESMANA
        //vroot.expandTree();
        //if (invisiblenode) vroot.hideVariables();
        vm.treechanged();

    }

    public void LevelUpPerformed(){
        /*debugOff
        System.out.println("LevelUp");
        /*EndOfDebug*/


        VarNodes newchild =new VarNodes(new HashMap(0), vroot);
        ((VarNodes)root).addChild(newchild);
        vm.treechanged();
        //vroot.expandTree();
    }

    public void LevelDownPerformed(){
        /*DebugOff
        System.out.println("LevelDown");
        /*EndOfDebug*/
          RepaintManager.currentManager(vm.treeTable).markCompletelyClean(vm.treeTable);
       ((VarNodes)root).removeHashMapNode();
        vm.treechanged();
        //vroot.expandTree();
    }


    public void ValueChangedPerformed(Object name) {
        /*DebugOff
        System.out.println("ValueChanged");
        /*EndOfDebug*/
        vroot.valueChanged((String)name); //LESMANA
        vm.treechanged();
    }



    public void modelUpdate(){
        fireTreeStructureChanged(this,vroot.getPathToRoot(),null ,null);
    }


    /**
     * this is node of tree
     */
    public class VarNodes {

        //private static VarNodes root;
        //private static VarMonitorModel varmonitormodel;


        private String name;
        private VarNodes parent;
        private VarNodes[] children;
        public int changed;
        private boolean visible=true;
        boolean expanded=Settings.isExpandedbydefault();
        //protected boolean shownotvisibles=true;
        //protected JCheckBox checkbox;


        public VarNodes() {}
        public VarNodes(ListIterator symboltabiterator) {
            //symboltab-iterator, cursor position at end of list
            root = this;
            vroot = this;
            name = symtab;
            parent = null;
            changed=pc.getValue();
            int listlength=0;

            while (symboltabiterator.hasPrevious()) {
                symboltabiterator.previous();
                listlength++;
            }
            children = new VarNodes[listlength];

            for (int i = 0; symboltabiterator.hasNext(); i++) {
                children[i] = new VarNodes((HashMap) symboltabiterator.next(),
                        this);
            }

        }


        public VarNodes(HashMap map, VarNodes parent) {

            name= block;
            changed=changed=pc.getValue();
            //vm.getProgramCounter().addProgramCounterListener(this);
            this.parent=parent;
            //if (parent==null) this.parent=vroot;
            Iterator mapiterator= map.keySet().iterator();

            children = new VarNodes[map.size()+1];
            children[0]= new VarNodes(notVisible,this);
            children[0].visible=true;
            children[0].expanded=false;
            children[0].changed=pc.getValue();
            for (int i=1; mapiterator.hasNext(); i++) {
                children[i]= new VarNodes ((String)mapiterator.next(),this);
            }
        }


        public VarNodes(String name, VarNodes parent) {
            this.name = name;
            this.parent = parent;
            visible = true;
            //if for the very first element in a new hashmap
            //if (parent == null)	this.parent = hashMap();
            children = new VarNodes[0];
            changed = changed=pc.getValue();
            if (name.equals(notVisible) == false
                    && name.equalsIgnoreCase("") == false) {

                //vm.getProgramCounter().addProgramCounterListener(this);

                // to get actual hashmap
                int actualhashmap = vroot.children.length;
                for (int i = 0; i < vroot.children.length; i++) {
                    if (vroot.children[i] == null) {
                        actualhashmap = i;
                        break;
                    }
                }
                setVisibility(name, actualhashmap);
            }

        }


        private void setVisibility(String name, int actualhashmap) {

            //System.out.println("Debug coverVar");
            for (int i = 0; i < actualhashmap; i++) {
                //only cover variable if it's not in the actual hashmap
                for (int j = 1; j < vroot.children[i].children.length; j++) {
                    if (vroot.children[i].children[j].name
                            .equalsIgnoreCase(name)) {
                        vroot.children[i].children[j].visible = false;
                        /*if (!vroot.children[i].children[0].shownotvisibles){
                            vroot.children[i].children[0].addChild(vroot.children[i].children[j]);
                        vroot.children[i].removeChild(vroot.children[i].children[j]);}*/

                    break;}
                }
            }
            if (!Settings.getInvisibleNodeMode().equals(Settings.show)) hideVariables();
        }


        public void hideVariables() {

            //System.out.println("Debug coverVar");
            for (int i = 0; i < vroot.children.length; i++) {
                //System.out.println(vroot.children[i]);
                //System.out.println(vroot.children[i].children[0]);
                //if (!vroot.children[i].children[0].shownotvisibles){
                    for (int j = 1; j < vroot.children[i].children.length;) {
                        VarNodes child=vroot.children[i].children[j];
                        VarNodes newparent=vroot.children[i].children[0];
                        if (child.visible==false){
                            newparent.addChild(child);
                            child.parent=newparent;
                            vroot.children[i].removeChild(child);

                        }else j++;

                    //}

                }
            }

        }


        public void showVariables() {
        VarNodes child;
            for (int i = 0; i < vroot.children.length; i++) {

                for (int j = 0; j < vroot.children[i].children[0].children.length;j++) {
                    child=vroot.children[i].children[0].children[j];
                    if (!Settings.getInvisibleNodeMode().equals(Settings.show)) if(!child.visible)continue;

                    vroot.children[i].addChild(child);
                    vroot.children[i].children[0].removeChild(child);

                }
            }

        }

        public void removeHashMapNode(){
            //System.out.println("Debug"+vroot.children.length-1);
            uncoverVariables();
            vroot.removeChild(hashMap());
            if (!Settings.getInvisibleNodeMode().equals(Settings.show)) showVariables();


        }

        private int getChildIndex(VarNodes n){

            for (int i = 0; i < getChildCount(this); i++) {
                if (n.equals(getChild(this,i))) return i;

            }

            return -10;
        }


        /**
         * removes child from this.children
         * @param removenode
         */
        private void removeChild(VarNodes removenode) {
            VarNodes[] temp= new VarNodes[children.length-1];
            int removenodeindex=getChildIndex(removenode);

            //System.out.println("removing"+removenodeindex+getChild(this,removenodeindex));


            int j=-1;
            for (int i=0; i<children.length; i++) {
                if( removenode.equals(children[i])==false){
                    j++;
                    temp[j]=children[i];}
                //else { removenodeindex=i;}
                }

            children=temp;

            //fireTreeNodesRemoved(this,getPathToRoot(),new int[]{removenodeindex} ,new Object[]{removenode});
            //fireTreeNodesRemoved(this,getPathToRoot(),new int[]{removenodeindex} ,new Object[]{removenode});


        }

        /**
         * adds a new child to this.children
         * @param newnode
         */

          public void addChild(VarNodes newnode){
              newnode.parent=this;
            VarNodes[] temp= new VarNodes[children.length+1];

            for ( int i=0; i<children.length; i++) {
                temp[i]=children[i];
            }

            temp[children.length]=newnode;
            children=temp;
            //System.out.println("inserted"+getChild(this,getChildCount(this)-1));
            //fireTreeNodesInserted(this,getPathToRoot(),new int[]{getChildCount(this)-1} ,new VarNodes[]{newnode});

        }


        private void uncoverVariables() {
            for (int i = 0; i < hashMap().children.length; i++) {

                uncover(vroot.coveredby(hashMap().children[i]));
             }


        }

        private void uncover(VarNodes node) {
            node.visible=true;

        }
        private VarNodes coveredby(VarNodes node) {
            if (equals(hashMap())) return vroot;
            if (getName().equalsIgnoreCase(node.getName())){
                return this;}
            for (int i=children.length-1; i>=0; i--) {
                 VarNodes temp=children[i].coveredby(node);
                 if (!temp.equals(vroot)) return temp;
            }
            return vroot;
        }
        public boolean isVisible() {
            return visible;
        }

        /**
         * modifies "changed" value of the node to actual value of the programCounter
         * @param name
         */
        public void valueChanged(String name) {
            for (int i=vroot.children.length-1;i>=0; i--){
                for (int j=0; j<vroot.children[i].children.length;j++){
                    VarNodes child=vroot.children[i].children[j];
                    if (child.getName().equalsIgnoreCase(name)) {

                        child.changed=pc.getValue();
                    //	fireTreeNodesChanged(this,child.parent.getPathToRoot(),new int[]{child.parent.getChildIndex(child)},new VarNodes[]{child});
                        return;
                    }
                }
            }
        }


        public void discardChanges() {
            changed=-1;
        }


        public void counterReset() {
        }

        /**
         * whether a node has changed its value or has been instantiated within "changewithinrounds" rounds
         */
        public boolean hasChanged() {
            /*DebugOff
            System.out.println(getName()+" lastchanged="+changed+"  pc="+pc.getValue()+"  changewithinrounds="+changewithinrounds );
            /*EndOfDebug*/
            if (changed>=pc.getValue()-changewithinrounds){
            return true;
            }
            //if at least one variable of one level changed, the level should also be marked red as well.
            if (this.children==null); //System.out.println(getName());
            else
            for (int i=0;i<this.children.length;i++){
                if (children[i].hasChanged()) return true;
                }
            return false;
        }

        /**
         *
         * @return the path from root to this
         */

        VarNodes[] getPathToRoot(){
            if (this==vroot) return new VarNodes[]{this};
            if (parent==vroot) { VarNodes[] root={vroot,this}; return root;};
            VarNodes[] temp;
            temp=parent.getPathToRoot();
            VarNodes[] path= new VarNodes[temp.length+1];
            for (int i = 0; i < temp.length; i++) {
                path[i]=temp[i];
            }
            path[temp.length]=this;

            return path;

        }

        Integer isChildNumber(){

            VarNodes[] children=parent.children;
            for (int i = 0; i < children.length; i++) {
                if (this.equals(children)) return new Integer(i);
            }
            return null;
        }

        protected void expandTree(){
            if (expanded) {  //if (parent!=null) parent.expanded=true;
                vm.treeTable.tree.expandPath(new TreePath((Object[])this.getPathToRoot()));
                }
            else return;
            for (int i = 0; i < children.length; i++) {
                children[i].expandTree();
            }

        }


        public void expandAll() {
            for (int i = 0; i < children.length; i++) {
                children[i].expandAll();
                children[i].expanded=true;
                vm.treeTable.tree.expandPath(new TreePath((Object[])this.getPathToRoot()));
            }

        }
        public String toString() {
            if (getName().equals(notVisible)) return notVisible;
            return getName();
        }

        public String getName() {
            return name;
        }

        public VarNodes[] getChildren() {
            return children;
        }

        public VarNodes getParent() {
            return parent;
        }



        //actual hashMap node with variableNames as children
        private VarNodes hashMap (){
            return vroot.children[(vroot.children.length)-1];
        }


        public void systemOut(){/*
            System.out.println("pc"+pc.getValue());
            for (int i = 0; i < vroot.children.length; i++) {
                System.out.println(vroot.children[i].getName()+"  Value"+getValueAt(vroot.children[i],1)+"  changedAt"+ vroot.children[i].changed+
                        (vroot.children[i].visible? "  visible  " :"         ") +(vroot.children[i].shownotvisibles? "shownotvisibles" :""));
                for (int j = 0; j < vroot.children[i].children.length; j++) {
                    if (j==1) System.out.println("<VisibleList>");
                    System.out.println(vroot.children[i].children[j].getName()+"  Value"+getValueAt(vroot.children[i].children[j],1)+"  changedAt"+  vroot.children[i].children[j].changed+
                            (vroot.children[i].children[j].visible? "  visible  "  :"         ") +(vroot.children[i].children[j].shownotvisibles? "shownotvisibles" :""));
                    if (j==0){
                        for (int k = 0; k < vroot.children[i].children[j].children.length; k++) {
                            System.out.println(vroot.children[i].children[j].children[k].getName()+"  Value"+getValueAt(vroot.children[i].children[j].children[k],1)+"  changedAt"+ vroot.children[i].children[j].children[k].changed+
                                    (vroot.children[i].children[j].children[k].visible? "  visible  "  :"         ") +(vroot.children[i].children[j].children[k].shownotvisibles? "shownotvisibles" :""));

                        }
                    }

                }
                System.out.println("");
                System.out.println("");
            }}*/
        }


    }



}






